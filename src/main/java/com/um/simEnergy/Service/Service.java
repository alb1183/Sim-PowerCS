package com.um.simEnergy.Service;

import java.util.Arrays;
import java.util.function.IntPredicate;

public class Service {
	private String name;
	private boolean smart;
	private double powerConsumption; // Wh
	private int[] operatingRange = new int[3]; // modo, min, max
	private int priority;
	private IntPredicate intPredicate;

	private boolean onOff = true;
	
	// Smart parameters
	private int lastOn = -1;
	private int minTime = -1;
	private int maxTime = -1;
	private int runTime = 0;
	private int runningTime = 0;

	private double lastReward = 0;
	private double sumRewards = 0; // TODO
	private int numRewards = 0;

	public Service(String name, boolean smart, double powerConsumption, int[] op) {
		this.name = name;
		this.smart = smart;
		this.powerConsumption = powerConsumption;
		this.operatingRange = op;
		this.onOff = !smart; // Si es controlable lo inicio apagado
	}
	
	public Service(String name, boolean smart, double powerConsumption) {
		this(name, smart, powerConsumption, new int[]{0,0,0});
	}
	
	public Service(String name, boolean smart, double powerConsumption, IntPredicate intPredicate) {
		this(name, smart, powerConsumption, new int[]{0,0,0});
		this.intPredicate = intPredicate;
	}
	
	public Service(String name, boolean smart, double powerConsumption, int priority) {
		this(name, smart, powerConsumption, new int[]{0,0,0});
		this.priority = priority;
	}
	
	public Service setSmartParameters(int minTime, int maxTime, int runTime) {
		if(this.smart) {
			this.minTime = minTime;
			this.maxTime = maxTime;
			this.runTime = runTime;
			
			// Si es de ejecucion 24 horas lo dejo ya iniciado
			if(this.minTime == -1)
				this.onOff = true;
		}
		return this;
	}


	// TODO: checkear intPredicate tambien
	public boolean isWorkingTime(int minuteDay) {
		return (this.minTime == -1) || (this.minTime <= minuteDay && minuteDay < this.maxTime);
	}
	
	/*public boolean checkRunTime(int minute) {
		return lastOn == -1 || minute <= this.lastOn+this.runTime;
	}*/
	
	public boolean checkRunTime(int minute) {
		return this.runTime == 0 || this.getRunningTime(minute) < this.runTime;
	}

	public void turnOn(int minute) {
		// Si estaba encendido calculo el tiempo
		if(this.onOff)
			this.runningTime += minute-this.lastOn;
		
		this.lastOn = minute;
		this.onOff = true;
	}
	public void turnOff(int minute) {
		// Si estaba encendido calculo el tiempo
		if(this.onOff)
			this.runningTime += minute-this.lastOn;
			
		this.lastOn = -1;
		this.onOff = false;
	}
	
	public int getRunningTime(int minute) {
		int rTime = this.runningTime;

		// Si estaba encendido calculo el tiempo que le quedaba
		if(this.onOff)
			rTime += minute-this.lastOn;
		
		return rTime;
	}


	// TODO: checkear intPredicate tambien
	public int getWaitingTime() {
		if(this.lastOn == -1)
			return 0;

		if(this.minTime == -1)
			return 0;
		
		return (this.lastOn%1440) - this.minTime;
	}
	
	public double getLastReward() {
		return lastReward;
	}
	
	public void setLastReward(double r) {
		this.lastReward = r;
		
		// 
		this.sumRewards = r;
		this.numRewards = 1;
	}
	
	public void setMeanReward(double r) {
		this.lastReward = (this.lastReward + r) / 2.0;
		
		// 
		this.sumRewards = this.lastReward;
		this.numRewards = 1;
	}
	
	public void setAvgReward(double r) {
		this.sumRewards += r;
		this.numRewards++;
		this.lastReward = this.sumRewards / this.numRewards;
	}

	public void initDay(int d) {
		// Solo modifico cosas en los servicios inteligentes
		if(this.smart) {
			// Reinicio el runningTime a cero
			this.runningTime = 0;
			
			// Si estoy apagado borro el tiempo de ultimo arranque, si no pongo el del inicio del dia
			this.lastOn = (!this.onOff) ? -1 : d * 1440;
			
			// 
			this.sumRewards = this.lastReward;
			this.numRewards = 1;
		}
	}
	
	public int getDefinedRuntime() {
		return (this.runTime != 0) ? this.runTime : (this.minTime != -1) ? this.maxTime-this.minTime : 24*60; // En minutos
	}

	// TODO: checkear intPredicate tambien
	public boolean isDynamic() {
		return (this.minTime != -1);
	}
	
	public String getName() {
		return name;
	}

	public int getPriority() {
		return this.priority;
	}
	
	public boolean isSmart() {
		return this.smart;
	}

	public boolean isOn() {
		return this.onOff;
	}
	
	public void turnOn() {
		this.onOff = true;
	}
	
	public void turnOff() {
		this.onOff = false;
	}

	public double getPowerConsumption() { // Lo usa SimulationResults.java
		return this.powerConsumption/* / 60.0*/;  // Paso de Wh a Wm
	}
	
	public double getLoad(int minute) {
		int minuteDay = minute % 1440;
		double load = powerConsumption / 60.0; // Paso de Wh a Wm
		
		// Si está apagado no consume nada
		if(!this.onOff)
			return 0.0;
		
		// Si tiene definida una expresion lambda la ejecuto
		if(intPredicate != null && intPredicate.test(minuteDay))
			return load;
		
		// Si no tiene definido un rango devuelvo directamente el consumo
		if(this.operatingRange[0] == 0)
			return load;
		
		// Si tiene definido un rango de inclusion compruebo si está dentro del mismo
		if(this.operatingRange[0] == 1 && minuteDay >= this.operatingRange[1] && minuteDay < this.operatingRange[2])
			return load;
		
		// Si tiene definido un rango de exclusion compruebo si está fuera del mismo
		if(this.operatingRange[0] == 2 && !(minuteDay >= this.operatingRange[1] && minuteDay < this.operatingRange[2]))
			return load;
		
		
		// Si no tiene bien defino el rango o está fuera del mismo devuelvo 0
		return 0.0;
	}

	@Override
	public String toString() {
		return "Service [name=" + name + ", smart=" + smart + ", powerConsumption=" + powerConsumption
				+ ", operatingRange=" + Arrays.toString(operatingRange) + ", priority=" + priority + ", intPredicate="
				+ intPredicate + ", onOff=" + onOff + "]";
	}
	
	
}
