package com.um.simEnergy.EnergyController;

import com.um.simEnergy.EnergyStorage.Battery;
import com.um.simEnergy.EnergyStorage.Grid;

public class BasicEnergyController extends EnergyController {
	
	public BasicEnergyController(Battery batteryStorage, Grid gridStorage) {
		super(batteryStorage, gridStorage);
	}

	public void run(int minute, double powerProduction, double electricalLoad) {
		int minuteDay = minute % 1440;
		
		// Determino la demanda de energia teniendo en cuenta la energia necesitada y la producida
		double demandedEnergy = electricalLoad - powerProduction;
		
		// Diferencio entre descarga o carga
		if(demandedEnergy > 0) {
			// Si la demanada de energia es positiva se deben descargar las baterias o usar el grid
			
			// Si la bateria puede asumir la carga sin descargarse la uso
			if(this.batteryStorage.isFeasible(demandedEnergy)) {
				this.batteryStorage.loadWmperMinute(demandedEnergy);
			} else { // Si no uso el grid
				this.gridStorage.loadWmperMinute(minuteDay, demandedEnergy);
			}
			
		} if(demandedEnergy < 0) {
			// Si la demanada es negativa significa que sobra energia y se puede usar para cargar las baterias o vender al grid
			
			// Si la bateria no está llena la cargo
			if(!this.batteryStorage.isFull())
				this.batteryStorage.loadWmperMinute(demandedEnergy);
			else // Si no, vendo al grid
				this.gridStorage.sellWmperMinute(minuteDay, demandedEnergy);
				
		}/* else {
			// Carga cero, el sistema no hace nada
		}*/
		
	}
}
