package com.um.simEnergy.Battery;

public class BasicBattery {
	private double capacity;
	private double level;
	private double overloadFix = 45.0; // Consumo del inversor en Wh
	
	// Stats
	private double overCapacityLoss;
	private double underCapacityLoss;
	
	public BasicBattery(double c) {
		this.capacity = c;
		this.level = c;
	}
	
	/*public void useBatteryWHperMinute(double wh) {
		this.level -= (wh+overloadFix) / 60.0;
	}

	public void chargeBatteryWHperMinute(double wh) {
		this.level += wh / 60.0;
	}*/
	
	public double loadWmperMinute(double wm) {
		//double load = wm - (overloadFix / 60.0);
		double load = wm;
		
		// Si se llena la bateria
		if(this.level + load > this.capacity) {
			// El uso real corresponde a lo necesario para cargar la bateria, el resto no se usa ni se computa
			double loadReal = this.capacity - this.level;
			
			// No sobrepaso el limite de carga
			this.level = capacity;
			
			// Stat de energia desperdicia cuando se está al maximo de carga
			overCapacityLoss += load-loadReal;
			
			return loadReal;
		}
		
		// Si se descarga y está por debajo de 0%
		if(load < 0 && this.level+load < 0) {
			System.err.println("Bateria agotada");
			double loadLoss = -load; // La paso a positiva
			
			// Si aun no está descargada quito lo que quedaba en la bateria
			if(this.level > 0)
				loadLoss -= this.level;

			// Stat de energia desperdicia cuando se está al maximo de carga
			underCapacityLoss += loadLoss;
		}
		
		// Cargo o descargo la bateria segun el signo de la carga (positivo carga, negativo descarga)
		this.level += load;
		
		
		return load;
	}
	
	public double getCapacity() {
		return capacity;
	}
	
	public double getRemaining() {
		return level;
	}
	
	public double getLevel() {
		return (level / capacity) * 100.0;
	}

	public double getOverCapacityLoss() {
		return overCapacityLoss;
	}

	public double getUnderCapacityLoss() {
		double underCapacityLossTemp = this.underCapacityLoss;
		this.underCapacityLoss = 0;
		return underCapacityLossTemp;
	}
	
}
