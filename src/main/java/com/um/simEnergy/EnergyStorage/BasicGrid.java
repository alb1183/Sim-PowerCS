package com.um.simEnergy.EnergyStorage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.um.simEnergy.Simulation;
import com.um.simEnergy.utils.NoiseGenerator;

public class BasicGrid extends Grid {
	//private double buyPrice; // Price in Kw per hour
	//private double sellPrice;
	private double[][] pricePerMinute; // Price Kw per minute
	private Random rand;
	private NoiseGenerator ng;
	
	// Stats
	private double buyMoney;
	private double buyWm;
	private double sellMoney;
	private double sellWm;
	
	private double lastUsage;
	
	public BasicGrid(String modelFile, double seed) {
		this(modelFile);
		this.ng = new NoiseGenerator(seed);
	}

	public BasicGrid(String modelFile) {
		this.pricePerMinute = new double[24 * 60][2];
		
		
		// Leo el fichero
		// "h","mean"
		try (CSVReader reader = new CSVReader(new FileReader(modelFile))) {
			String[] lineInArray;
			int minuto = 0;
			while ((lineInArray = reader.readNext()) != null) {
				this.pricePerMinute[minuto][0] = Integer.parseInt(lineInArray[0]); // "h"
				this.pricePerMinute[minuto][1] = Double.parseDouble(lineInArray[1]); // "mean"
				minuto += 60;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CsvValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Relleno los huecos entre horas
		for(int i = 0; i < 1440; i += 60) {
			for(int j = 1; j < 60; j++) {
				this.pricePerMinute[i+j][1] = this.pricePerMinute[i][1]; // "mean"	
			}
		}
		
	}

	public void loadWmperMinute(int minute, double wm) {
		double load = wm;

		// Calculo el coste de comprar esa energia
		this.buyMoney += load * (getBuyPriceKwHMinute(minute)/1000);
		this.buyWm += wm;
		
		this.lastUsage = load;
	}
	
	public void sellWmperMinute(int minute, double wm) {

		// Calculo el coste de vender esa energia
		this.sellMoney += -wm * (getSellPriceKwHMinute(minute)/1000);
		this.sellWm -= wm;

		this.lastUsage = wm;
	}

	private double lastHourPrice = 0;
	private int lastHour = -1;
	public double getBuyPriceKwHMinute(int m) {
		int dayMinute = m % 1440;
		double price = this.pricePerMinute[dayMinute][1];
			
		if(Simulation.SIMULAR == true) {
			int dHour = dayMinute/60;
			if(dHour != lastHour) {
				double mean = this.pricePerMinute[dayMinute][1];
				
				double min = mean - mean*0.3;
				double max = mean + mean*0.4;
	
				double d = (max-min) / 3.0;
				
				lastHourPrice = mean + d * this.ng.noise(m);
				lastHour = dHour;
			}
			
			price = lastHourPrice;
		}
				
		return price;
	}
	
	public double getSellPriceKwHMinute(int m) {		
		return getBuyPriceKwHMinute(m)*0.63;
	}
	
	public double getBuyMoney() {
		return buyMoney;
	}

	public double getSellMoney() {
		return sellMoney;
	}

	public double getBuyWm() {
		return buyWm;
	}

	public double getSellWm() {
		return sellWm;
	}

	public double getLastUsage() {
		// Reinicio a cero
		double lastUsageT = lastUsage;
		lastUsage = 0;
		
		return lastUsageT;
	}
	
}
