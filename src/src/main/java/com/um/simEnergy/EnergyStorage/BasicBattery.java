package com.um.simEnergy.EnergyStorage;

public class BasicBattery extends Battery {
	private double capacity; // Capacidad maxima de la bateria en kWh
	private double level; // Nivel actual de la bateria
	//private double overloadFix = 45.0; // Consumo del inversor en Wh
	private double maxFeasibleLoad;
	
	// Stats
	private double overCapacityLoss;
	private double lastUsage;
	
	public BasicBattery(double c, double mFl) {
		this.capacity = c;
		this.level = c;
		this.maxFeasibleLoad = mFl;
	}
	
	public void loadWmperMinute(double wm) {
		//double load = wm - (overloadFix / 60.0);
		double load = wm;
		
		// Si se llena la bateria
		if(this.level - load > this.capacity) {
			// El uso real corresponde a lo necesario para cargar la bateria, el resto no se usa ni se computa
			double loadReal = this.level - this.capacity;
			
			// No sobrepaso el limite de carga
			this.level = capacity;
			
			// Stat de energia desperdicia cuando se está al maximo de carga
			overCapacityLoss += load+loadReal;

			this.lastUsage = loadReal;
			return;
		}
		
		// TODO : No usar nunca la bateria cuan se estima que se va a agotar
		/*// Si se descarga y está por debajo de 0%
		if(load > 0 && this.level-load < 0) {
			System.err.println("Bateria agotada");
			double loadLoss = load; // La paso a positiva
			
			// Si aun no está descargada quito lo que quedaba en la bateria
			if(this.level > 0)
				loadLoss += this.level;

			// Stat de energia desperdicia cuando se está al maximo de carga
			underCapacityLoss += loadLoss;
		}*/
		
		// Cargo o descargo la bateria segun el signo de la carga (positivo carga, negativo descarga)
		this.level -= load;
		
		this.lastUsage = load;
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
	
	public double getLastUsage() {
		// Reinicio a cero
		double lastUsageT = lastUsage;
		lastUsage = 0;
		
		return lastUsageT;
	}

	public boolean isFeasible(double demandedEnergy) {
		if (demandedEnergy <= 0)
			return true;
		
		return this.level-demandedEnergy >= 0 && demandedEnergy <= this.maxFeasibleLoad;
	}

	public boolean isFull() {
		return level == capacity;
	}
	
}
