package com.um.simEnergy.ServiceController;

import java.util.List;

import com.um.simEnergy.Result;
import com.um.simEnergy.Service.Service;

public class BasicController extends ServiceController {
	public BasicController(List<Service> servicesList) {
		super(servicesList);
		
	}

	public double decision(int minute, Result resultSim) {
		int minuteDay = minute % 1440;
		
		// Recorro los servicios para tomar decisiones
		/*for (Service service : servicesList) {
			// Solo administro los que son inteligentes
			if(service.isSmart()) {
				// Paso todos los Wm a Wh
				if(resultSim.getPowerProduction() * 60.0 > service.getLoad(minute)*2 && resultSim.getBatteryPercentage() > 90)
					service.turnOn();
				else
					//if(service.checkRunTime(minute))
						service.turnOff();
					
			}
		}*/

		// Si sobra energia enciendo cosas y si no apago
		if(resultSim.getBatteryPercentage() >= 100.0 && resultSim.getPowerProduction()-resultSim.getElectricalLoad() > 0)
		//if(this.lastBatteryLevel >= 95.0 && this.lastEnergyProduction-resultSim.getElectricalLoad() > 0)
			this.turnOnFeasible(minute, resultSim);
		else
			this.turnAllOff(minute, resultSim);
		
		
		return 0.0;
	}
	

	protected void turnOnFeasible(int minute, Result resultSim) {
		int minuteDay = minute % 1440;
		// Variables ...
		double energyAvailable = resultSim.getPowerProduction()-resultSim.getElectricalLoad();
		//double energyAvailable = this.lastEnergyProduction-resultSim.getElectricalLoad();
		
		// Recorro los servicios administrables ordenados por prioridad para tomar decisiones
		for (Service service : this.smartServices) {
			// En caso de poder alimentar el dispositivo
			double serviceLoad = service.getLoad(minute);
			if(energyAvailable > serviceLoad && service.isWorkingTime(minuteDay) && service.checkRunTime(minute)) {
				service.turnOn();
				energyAvailable -= serviceLoad;
			} else {
				service.turnOff();
			}
		}
	}
	
	protected void turnAllOn(int minute, Result resultSim) {
		for (Service service : this.smartServices) {
			service.turnOn();
		}
	}
	
	protected void turnAllOff(int minute, Result resultSim) {
		for (Service service : this.smartServices) {
			service.turnOff();
		}
	}
	
	protected void doNothing() {
		
	}
	
	public void save() {
		
	}
}
