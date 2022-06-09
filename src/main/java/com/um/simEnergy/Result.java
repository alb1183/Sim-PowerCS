package com.um.simEnergy;

import java.util.List;

import com.um.simEnergy.Battery.BasicBattery;
import com.um.simEnergy.Battery.BasicBatteryWithGenerator;
import com.um.simEnergy.Service.Service;

// System.out.println("Minute, SolarPower, ElectricityLoad, BatteryLevel");
public class Result {
	private int minute;
	private double powerProduction;
	private double electricalLoad;
	private double batteryUsage;
	private double batteryCapacity;
	private double batteryLevel;
	private double batteryPercentage;
	private double batteryOverCapacityLoss;
	private double batteryUnderCapacityLoss;
	private double globalReward;
	private double lastGlobalReward;
	//private double[] energyServices;
	private boolean[] serviceState;
	private double[] serviceRunningTime;
	private double[] serviceReward;
	private List<Service> servicesList;
	
	public Result(int minute, double powerProduction, double electricalLoad, double batteryUsage, BasicBattery battery, List<Service> servicesList, double lastGlobalReward, double globalReward) {
		this.minute = minute;
		this.powerProduction = powerProduction;
		this.electricalLoad = electricalLoad;
		this.batteryUsage = batteryUsage;
		this.batteryCapacity = battery.getCapacity();
		this.batteryLevel = battery.getRemaining();
		this.batteryPercentage = battery.getLevel();
		this.batteryOverCapacityLoss = battery.getOverCapacityLoss();
		this.batteryUnderCapacityLoss = battery.getUnderCapacityLoss();
		this.lastGlobalReward = lastGlobalReward;
		this.globalReward = globalReward;
		

		this.serviceState = new boolean[servicesList.size()];
		this.serviceReward = new double[servicesList.size()];
		// Guardo el estado actual de los servicios
		for (int i = 0; i < servicesList.size(); i++) {
			//Service sv = this.smartServices.get(i);
			//energyServices[i] = sv.isOn() ? sv.getLoad(resultSim.getMinute()) : 0;
			this.serviceState[i] = servicesList.get(i).isOn();
			this.serviceReward[i] = servicesList.get(i).getLastReward();
		}
	}
	//TODO
	public Result(int minute, double powerProduction, double electricalLoad, double batteryUsage, BasicBatteryWithGenerator battery, List<Service> servList, double lastGlobalReward, double globalReward) {
		this.minute = minute;
		this.powerProduction = powerProduction;
		this.electricalLoad = electricalLoad;
		this.batteryUsage = batteryUsage;
		this.batteryCapacity = battery.getCapacity();
		this.batteryLevel = battery.getRemaining();
		this.batteryPercentage = battery.getLevel();
		this.batteryOverCapacityLoss = battery.getOverCapacityLoss();
		this.batteryUnderCapacityLoss = battery.getUnderCapacityLoss();
		this.lastGlobalReward = lastGlobalReward;
		this.globalReward = globalReward;
		
		this.servicesList = servList;
		this.serviceState = new boolean[servList.size()];
		this.serviceRunningTime = new double[servList.size()];
		this.serviceReward = new double[servList.size()];
		// Guardo el estado actual de los servicios
		for (int i = 0; i < servList.size(); i++) {
			//Service sv = this.smartServices.get(i);
			//energyServices[i] = sv.isOn() ? sv.getLoad(resultSim.getMinute()) : 0;
			this.serviceState[i] = servList.get(i).isOn();
			//this.serviceRunningTime[i] = servList.get(i).getRunningTime(minute);
			this.serviceReward[i] = servList.get(i).getLastReward();
		}
	}
	
	public void update(double lastGlobalReward, double globalReward) {
		this.lastGlobalReward = lastGlobalReward;
		this.globalReward = globalReward;
		
		// Guardo el estado actual de los servicios
		for (int i = 0; i < this.servicesList.size(); i++) {
			this.serviceState[i] = this.servicesList.get(i).isOn();
			this.serviceReward[i] = this.servicesList.get(i).getLastReward();
		}
	}

	public boolean[] getServiceState() {
		return this.serviceState;	
	}

	public double[] getServiceReward() {
		return this.serviceReward;	
	}

	/*public void setEnergyServices(double[] energyServices) {
		this.energyServices = energyServices;
	}

	public double[] getEnergyServices() {
		return this.energyServices;
	}*/
	
	public int getMinute() {
		return minute;
	}
	
	public int getDayHour() {
		return (this.minute/60)%24;
	}

	public double getPowerProduction() {
		return powerProduction;
	}

	public double getBatteryLevel() {
		return batteryLevel;
	}
	
	public double getBatteryCapacity() {
		return batteryCapacity;
	}

	public double getBatteryPercentage() {
		return batteryPercentage;
	}
	
	public double getElectricalLoad() {
		return electricalLoad;
	}

	public double getBatteryUsageOld() {
		return this.powerProduction - this.electricalLoad;
	}

	public double getBatteryUsage() {
		return this.batteryUsage;
	}
	
	public double getBatteryOverCapacityLoss() {
		return batteryOverCapacityLoss;
	}

	public double getBatteryUnderCapacityLoss() {
		return batteryUnderCapacityLoss;
	}

	public double getGlobalReward() {
		return globalReward;
	}

	public double getLastGlobalReward() {
		return lastGlobalReward;
	}

	private String getTime() {
		return ((int)this.minute/1440) + " " + String.format("%02d", (int)(this.minute/60)%24) + ":" + String.format("%02d", this.minute%60);
	}

	@Override
	public String toString() {
		// Time, Minute, PowerProduction, ElectricalLoad, BatteryUsage, BatteryPercentage, BatteryUnderCapacityLoss, GlobalReward, ServicesID, ServiceName, ServicePriority, ServiceSmart, ServiceState, ServicePowerConsumption, ServiceRunningTime, ServiceReward
		String header = this.getTime() + ", " + this.minute + ", " + this.powerProduction + ", " + this.electricalLoad + ", " + this.batteryUsage + ", " + this.batteryPercentage + ", " + this.batteryUnderCapacityLoss + ", " + this.globalReward;
		String line = "";
		
		for (int i = 0; i < this.servicesList.size(); i++) {
			line += header + ", " + i + ", " + this.servicesList.get(i).getName() + ", " + this.servicesList.get(i).getPriority() + ", " + this.servicesList.get(i).isSmart() + ", " + this.serviceState[i] + ", " + this.servicesList.get(i).getPowerConsumption() + ", " + this.serviceRunningTime[i] + ", " + this.serviceReward[i] + "\n";
		}
		
		return line;
	}
	
	
}
