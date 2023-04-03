package com.um.simEnergy.EnergyStorage;

public abstract class Battery {
	
	// Load control
	public abstract void loadWmperMinute(double wm);
	
	
	// Mandatory Public Information
	public abstract double getCapacity();
	public abstract double getRemaining();
	public abstract double getLevel();
	public abstract double getOverCapacityLoss();
	public abstract double getLastUsage();

	public abstract boolean isFeasible(double demandedEnergy);
	public abstract boolean isFull();
	
}
