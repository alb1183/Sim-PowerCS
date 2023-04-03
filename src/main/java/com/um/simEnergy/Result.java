package com.um.simEnergy;

import java.util.List;

import com.um.simEnergy.EnergyStorage.Battery;
import com.um.simEnergy.EnergyStorage.Grid;
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

	private double gridUsage;
	private double buyMoney;
	private double buyWm;
	private double buyPrice;
	private double sellMoney;
	private double sellWm;
	private double sellPrice;
	
	private boolean[] serviceState;
	private double[] serviceRunningTime;
	private List<Service> servicesList;
	
	public Result(int minute, double powerProduction, double electricalLoad, List<Service> servList, Battery batteryStorage, Grid gridStorage) {
		this.minute = minute;
		this.powerProduction = powerProduction;
		this.electricalLoad = electricalLoad;
		
		// Bateria
		this.batteryUsage = batteryStorage.getLastUsage();
		this.batteryCapacity = batteryStorage.getCapacity();
		this.batteryLevel = batteryStorage.getRemaining();
		this.batteryPercentage = batteryStorage.getLevel();
		this.batteryOverCapacityLoss = batteryStorage.getOverCapacityLoss();
		
		// Grid
		this.gridUsage = gridStorage.getLastUsage();
		this.buyMoney = gridStorage.getBuyMoney();
		this.sellMoney = gridStorage.getSellMoney();
		this.buyWm = gridStorage.getBuyWm();
		this.sellWm = gridStorage.getSellWm();
		this.buyPrice = gridStorage.getBuyPriceKwHMinute(minute);
		this.sellPrice = gridStorage.getSellPriceKwHMinute(minute);
		
		// Servicios
		this.servicesList = servList;
		this.serviceState = new boolean[servList.size()];
		this.serviceRunningTime = new double[servList.size()];
		// Guardo el estado actual de los servicios
		for (int i = 0; i < servList.size(); i++) {
			//Service sv = this.smartServices.get(i);
			//energyServices[i] = sv.isOn() ? sv.getLoad(resultSim.getMinute()) : 0;
			this.serviceState[i] = servList.get(i).isOn();
			//this.serviceRunningTime[i] = servList.get(i).getRunningTime(minute);
		}
	}
	
	public void update() {
		// Guardo el estado actual de los servicios
		for (int i = 0; i < this.servicesList.size(); i++) {
			this.serviceState[i] = this.servicesList.get(i).isOn();
		}
	}

	public boolean[] getServiceState() {
		return this.serviceState;	
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

	public double getGridUsage() {
		return this.gridUsage;
	}
	
	public double getBuyMoney() {
		return buyMoney;
	}

	public double getSellMoney() {
		return sellMoney;
	}
	
	public double getBuyWm() {
		return buyWm;
	}

	public double getSellWm() {
		return sellWm;
	}

	public double getBuyPrice() {
		return buyPrice;
	}

	public double getSellPrice() {
		return sellPrice;
	}

	private String getTime() {
		return ((int)this.minute/1440) + " " + String.format("%02d", (int)(this.minute/60)%24) + ":" + String.format("%02d", this.minute%60);
	}

	
	@Override
	public String toString() {
		// Time, Minute, PowerProduction, ElectricalLoad, BatteryUsage, BatteryPercentage, BatteryUnderCapacityLoss, GlobalReward, ServicesID, ServiceName, ServicePriority, ServiceSmart, ServiceState, ServicePowerConsumption, ServiceRunningTime, ServiceReward
		String header = this.getTime() + ", " + this.minute + ", " + this.powerProduction + ", " + this.electricalLoad + ", " + this.batteryUsage + ", " + this.batteryPercentage;
		String line = "";
		
		for (int i = 0; i < this.servicesList.size(); i++) {
			line += header + ", " + i + ", " + this.servicesList.get(i).getName() + ", " + this.servicesList.get(i).getPriority() + ", " + this.servicesList.get(i).isSmart() + ", " + this.serviceState[i] + ", " + this.servicesList.get(i).getPowerConsumption() + ", " + this.serviceRunningTime[i] + "\n";
		}
		
		return line;
	}
	
	
}
