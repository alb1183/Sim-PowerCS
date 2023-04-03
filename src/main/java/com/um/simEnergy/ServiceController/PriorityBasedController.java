package com.um.simEnergy.ServiceController;

import java.util.List;

import com.um.simEnergy.Result;
import com.um.simEnergy.Service.Service;

public class PriorityBasedController extends ServiceController {
	private double[] beta = {100, 0.01, -0.01, 1, 1, 1}; // priority, runningTime, waitingTime, energyPenalty, batteryReward, generatorPenalty
	
	private int position = 0;
	
	public PriorityBasedController(List<Service> servicesList) {
		super(servicesList);
		
	}

	public double decision(int minute, Result resultSim) {
		int minuteDay = minute % 1440;
		
		double minBatteryThreshold = 20.0;
		double maxBatteryThreshold = 45.0;
		double batteryRange = 15.0 * (position/this.smartServices.size());

		
		// Compruebo si la situacion es critica para reducir el uso de servicios
		if(avgBatteryLevel < minBatteryThreshold - batteryRange)
			position = (position < this.smartServices.size()) ? position+1 : position;

		// Compruebo si la situacion ya no es critica para aumentar el uso de servicios
		// TODO: maxBatteryThreshold + batteryRange  /// El MAS antes que MENOS
		else if(avgBatteryLevel > maxBatteryThreshold - batteryRange)
				position = (position > 0) ? position-1 : 0;
		
		// Recorro los servicios administrables ordenados por prioridad para tomar decisiones (mas a menos)
		for(int i = 0; i < this.smartServices.size(); i++) {
			Service serv = this.smartServices.get(i);
			
			// Si deben funcionar
			if(serv.isWorkingTime(minuteDay) && serv.checkRunTime(minute)) {
				if(i < this.smartServices.size() - this.position)
					serv.turnOn(minute);
				else
					serv.turnOff(minute);
				
				//System.out.println(resultSim.getMinute() + " ("+(resultSim.getMinute()%1440)/60.0+")" +" | " + avgEnergyProduction + ", " + avgElectricalLoad + " (" + avgBatteryLevel + ", " + minBatteryPercentage + ")" + " --> " + serv.getName() + " (" + serv.getRunningTime(minute) + ") => ON");
			} else {
				// Comportamiento por defecto
				serv.turnOff(minute);
			}
		}
		
		return 0.0;
	}
	
	public void save() {
		
	}
}
