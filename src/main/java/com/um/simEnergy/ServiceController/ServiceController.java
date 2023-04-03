package com.um.simEnergy.ServiceController;

import java.util.LinkedList;
import java.util.List;

import com.um.simEnergy.Result;
import com.um.simEnergy.Service.Service;

public abstract class ServiceController {
	protected List<Service> servicesList;
	protected List<Service> smartServices;
	
	private int lastRun = 0;
	protected int waitTime = 60; // Cada 60m ejecuto el algoritmo de control
	
	// Stats
	protected double lastEnergyProduction = 0.0; // Decimas partes por iteracion
	protected double lastElectricalLoad = 0.0; // Decimas partes por iteracion
	protected double lastBatteryLevel = 0.0; // Decimas partes por iteracion
	protected double minBatteryPercentage = 100.0;
	// Stats waitTime
	protected double sumEnergyProduction = 0.0;
	protected double sumElectricalLoad = 0.0;
	protected double sumBatteryLevel = 0.0;

	protected double avgEnergyProduction;
	protected double avgElectricalLoad;
	protected double avgBatteryLevel;
	protected double avgBatteryGenerator;
	
	// Stats
	protected double globalRewardLast = 0;
	protected double globalRewardSum = 0;
	protected int globalRewardCount = 0;
	
	protected ServiceController(List<Service> servicesList) {
		this.servicesList = servicesList;
		
		// Servicios administrables ordenados por prioridad
		this.smartServices = new LinkedList<Service>();
		
		// Recorro los servicios para recopilar aquellos que son inteligentes
		for (Service service : this.servicesList)
			// Solo administro los que son inteligentes
			if(service.isSmart())
				this.smartServices.add(service);
		
		// Ordeno la lista segun prioridad de mas a menos
		this.smartServices.sort((s1, s2) -> s2.getPriority() - s1.getPriority());
		
	}

	public void run(int minute, Result resultSim) {
		// Informacion estadistica
		this.lastEnergyProduction = this.lastEnergyProduction*0.9 + resultSim.getPowerProduction()*0.1;
		this.lastElectricalLoad = this.lastElectricalLoad*0.9 + resultSim.getElectricalLoad()*0.1;
		this.lastBatteryLevel = this.lastBatteryLevel*0.9 + resultSim.getBatteryPercentage()*0.1;

		// Informacion estadistica waitTime
		this.sumEnergyProduction += resultSim.getPowerProduction();
		this.sumElectricalLoad += resultSim.getElectricalLoad();
		this.sumBatteryLevel += resultSim.getBatteryPercentage();
		this.minBatteryPercentage = (resultSim.getBatteryPercentage() < this.minBatteryPercentage) ? resultSim.getBatteryPercentage() : this.minBatteryPercentage;
		
		
		// Solo ejecuto si me toca
		if(minute < this.lastRun+this.waitTime)
			return;
		this.lastRun = minute;
		

		// Informacion estadistica media
		this.avgEnergyProduction = (this.sumEnergyProduction / this.waitTime) * 60;
		this.avgElectricalLoad = (this.sumElectricalLoad / this.waitTime) * 60;
		this.avgBatteryLevel = this.sumBatteryLevel / this.waitTime;
		
		double reward = this.decision(minute, resultSim);

		// stats reward
		this.globalRewardLast = reward;
		this.globalRewardSum += reward;
		this.globalRewardCount++;

		// Informacion estadistica waitTime
		this.sumEnergyProduction = 0;
		this.sumElectricalLoad = 0;
		this.sumBatteryLevel = 0;
		
		//this.saveServicesStats(resultSim);
	}

	public void initDay(int d) {
		this.minBatteryPercentage = 100.0;
	}

	public double getGlobalReward() {
		return (this.globalRewardCount == 0) ? 0 : this.globalRewardSum / this.globalRewardCount;
	}
	
	public double getLastGlobalReward() {
		return this.globalRewardLast;
	}
	
	/*private void saveServicesStats(Result resultSim) {
		double[] energyServices = new double[this.smartServices.size()];
		for (int i = 0; i < this.smartServices.size(); i++) {
			//Service sv = this.smartServices.get(i);
			//energyServices[i] = sv.isOn() ? sv.getLoad(resultSim.getMinute()) : 0;
			energyServices[i] = this.smartServices.get(i).getLoad(resultSim.getMinute());
		}
		resultSim.setEnergyServices(energyServices);
	}*/

	// Punto de extension
	abstract protected double decision(int minute, Result resultSim);
}
