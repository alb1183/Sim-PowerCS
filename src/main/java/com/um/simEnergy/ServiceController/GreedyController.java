package com.um.simEnergy.ServiceController;

import java.util.List;

import com.um.simEnergy.Result;
import com.um.simEnergy.Service.Service;

public class GreedyController extends ServiceController {
	private double[] beta = {100, 0.01, -0.01, 1, 1, 1}; // priority, runningTime, waitingTime, energyPenalty, batteryReward, generatorPenalty
	
	public GreedyController(List<Service> servicesList) {
		super(servicesList);
		
	}

	public double decision(int minute, Result resultSim) {
		int minuteDay = minute % 1440;

		double minBatteryPercentageI = 100-minBatteryPercentage;
		double threshold = minBatteryPercentageI*minBatteryPercentageI; // min 0, max 10000 (100%->0%)
		
		// Recorro los servicios administrables ordenados por prioridad para tomar decisiones
		for(int i = 0; i < this.smartServices.size(); i++) {
			Service serv = this.smartServices.get(i);
			
			if(serv.isWorkingTime(minuteDay) && serv.checkRunTime(minute)) {
				
				double powerConsumption = serv.getPowerConsumption();
				double consumptionOverBatteryExp = Math.pow(1-(powerConsumption / resultSim.getBatteryCapacity()), 8); // 7
				double priority = Math.sqrt(serv.getPriority());

				double heuristic = 8500*priority*consumptionOverBatteryExp; // 7200
				//if(serv.getName().equals("Fuente de agua"))
				//	System.out.println(heuristic + " < " + threshold + " ("+minBatteryPercentage+")");

				//if((minBatteryPercentage < 2 || avgBatteryLevel < 2) && powerConsumption / resultSim.getBatteryCapacity() > 0.05) {
				if(heuristic < threshold) {
					serv.turnOff(minute);
				} else {
					serv.turnOn(minute);
				}
				//serv.setLastReward(heuristic);
				
				//if(serv.getName().equals("Motor piscina"))
				//System.out.println(resultSim.getMinute() + " ("+(resultSim.getMinute()%1440)/60.0+")" +" | " + avgEnergyProduction + ", " + avgElectricalLoad + " (" + avgBatteryLevel + ", " + minBatteryPercentage + ")" + " --> " + serv.getName() + " (" + serv.getRunningTime(minute) + ") => ON, " + powerConsumption);
			} else {
				// Comportamiento por defecto
				serv.turnOff(minute);
				//serv.setLastReward(0);
			}
		}
		
		return threshold;
	}
	
	// TODO : para pruebas...
	/*private double feedback(Service serv, int accion, Result resultSim) {
		// Acción que se tomó en esa tarea
		int accionSigned = (accion == 0) ? -1 : 1; // 0 off, 1 on --> -1 off, 1 on

		// Calculo la recompensa
		double waitingTime = serv.getWaitingTime();
		double priority = accion*serv.getPriority();
		double runningTime = accion*serv.getRunningTime(resultSim.getMinute());
		
		double powerConsumption = serv.getPowerConsumption();
		double powerConsumptionOverBattery = powerConsumption / resultSim.getBatteryCapacity();
		double energyPenalty = 2 * accionSigned*( powerConsumptionOverBattery * (avgEnergyProduction-avgElectricalLoad)  );
		double batteryReward = -50 * powerConsumptionOverBattery * (100-avgBatteryLevel);
		double generatorPenalty = 0 * -1000 * accionSigned * powerConsumptionOverBattery * avgBatteryGenerator;
		//System.out.println(generatorPenalty);
		// Recompensa final
		double reward = beta[0] * priority + beta[1] * runningTime + beta[2] * waitingTime + beta[3] * energyPenalty + beta[4] * batteryReward + beta[5] * generatorPenalty;
		
		// batteryPenalty
		if(minBatteryPercentage < 2 || maxBatteryGenerator > 0) {
			reward -= accionSigned * powerConsumptionOverBattery * ((50000*accion+maxBatteryGenerator*60));
		} else if(minBatteryPercentage < 10) {
			reward -= accionSigned * powerConsumptionOverBattery * 5000;
		} else if(minBatteryPercentage < 20) {
			reward -= accionSigned * powerConsumptionOverBattery * 2000;
		}

		serv.setAvgReward(reward);
		
		return reward;
	}*/
	
	public void save() {
		
	}
}
