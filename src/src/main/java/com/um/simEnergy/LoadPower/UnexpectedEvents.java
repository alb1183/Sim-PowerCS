package com.um.simEnergy.LoadPower;

public class UnexpectedEvents {
	private int days;
	
	public UnexpectedEvents(int days) {
		this.days = days;
	}

	public double getPowerProduction(int day, int minute, double powerProduction) {
		if((minute%1440) >= 60*11 && (minute%1440) <= 60*14 && day <= 2)
			return powerProduction * 0.8;
		
		return powerProduction;
	}

	public double getElectricalLoad(int day, int minute, double electricalLoad) {
		if((day == 3 || day == 12) && (minute%1440) >= 60*9+20 && (minute%1440) <= 60*13)
			return electricalLoad + 20;
		
		if((day == 17 || day == 22 || day == 35 || day == 43 || day == 55) && (minute%1440) >= 60*10+30 && (minute%1440) <= 60*12)
			return electricalLoad + 30;
		
		return electricalLoad;
	}
}
