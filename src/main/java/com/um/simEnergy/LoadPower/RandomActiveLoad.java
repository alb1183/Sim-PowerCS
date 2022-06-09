package com.um.simEnergy.LoadPower;

import com.um.simEnergy.utils.NoiseGenerator;

public class RandomActiveLoad extends ElectricalLoad {
	private NoiseGenerator ng;

	public RandomActiveLoad() {
		this.ng = new NoiseGenerator();
	}

	private double getPerlinLoad(int m) {
		double d = 200;		
		return d + d * this.ng.noise(m/50.0);
	}
	
	public double getLoad(int m) {
		return getPerlinLoad(m);
	}

}
