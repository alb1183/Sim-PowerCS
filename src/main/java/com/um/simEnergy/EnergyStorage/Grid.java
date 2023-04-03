package com.um.simEnergy.EnergyStorage;

public abstract class Grid {

	// Load control
	public abstract void loadWmperMinute(int minute, double wm);
	public abstract void sellWmperMinute(int minute, double wm);

	// Grid Prices
	public abstract double getBuyPriceKwHMinute(int m);
	public abstract double getSellPriceKwHMinute(int m);
	
	// Mandatory Public Information
	public abstract double getBuyMoney();
	public abstract double getSellMoney();
	public abstract double getBuyWm();
	public abstract double getSellWm();
	public abstract double getLastUsage();
	
}
