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

	public abstract void run(int minute, double powerProduction, double electricalLoad);
}
