package com.um.simEnergy.EnergyController;

import com.um.simEnergy.EnergyStorage.Battery;
import com.um.simEnergy.EnergyStorage.Grid;

public abstract class EnergyController {
	protected Battery batteryStorage;
	protected Grid gridStorage;
	
	public EnergyController(Battery batteryStorage, Grid gridStorage) {
		this.batteryStorage = batteryStorage;
		this.gridStorage = gridStorage;
	}

    /**
    * Main extension point of the energy controller, is invoked at the beginning of each minute of simulation when generation and energy consumption have been determined.
    * @param minute Minute
    * @param powerProduction Watts per hour of energy production in that minute
    * @param electricalLoad Watts per hour of load in that minute
    */
	public abstract void run(int minute, double powerProduction, double electricalLoad);
}
