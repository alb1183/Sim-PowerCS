package com.um.simEnergy.ServiceController;

import java.util.List;

import com.um.simEnergy.Result;
import com.um.simEnergy.Service.Service;

public class DummyController extends ServiceController {
	private boolean state;
	
	public DummyController(List<Service> servicesList, boolean state) {
		super(servicesList);
		this.state = state;
	}

	public double decision(int minute, Result resultSim) {
		int minuteDay = minute % 1440;

		// Recorro los servicios administrables ordenados por prioridad para tomar decisiones
		for(int i = 0; i < smartServices.size(); i++) {
			Service serv = this.smartServices.get(i);
			
			if(serv.isWorkingTime(minuteDay) && serv.checkRunTime(minute)) {
				if(state)
					serv.turnOn(minute);
				else
					serv.turnOff(minute);
			} else {
				serv.turnOff(minute);
			}
		}
		
		return 0.0;
	}

	public void save() {
		
	}
}
