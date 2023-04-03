package com.um.simEnergy.PowerProducer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.um.simEnergy.Simulation;
import com.um.simEnergy.utils.NoiseGenerator;

public class SimulatedPhotovoltaicModule extends PowerProducer {
	private double proportionParameter;
	private double[][] radiationPerMinute;
	private Random rand;
	private NoiseGenerator ng;

	public SimulatedPhotovoltaicModule(double proportionParameter, double seed) {
		this(proportionParameter);
		this.ng = new NoiseGenerator(seed);
	}
	
	public SimulatedPhotovoltaicModule(double proportionParameter) {
		this.proportionParameter = proportionParameter;
		this.radiationPerMinute = new double[24 * 60][6];
		this.rand = new Random();
		this.ng = new NoiseGenerator();
	}


	public void printRadiationPerMinute() {
		System.out.println(Arrays.deepToString(this.radiationPerMinute).replace("], ", "]\n"));
	}


	public void readFrom1hCSV(String file) {
		// "n","mean","sd","sem","CI_lower","CI_upper"
		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			String[] lineInArray;
			int minuto = 0;
			while ((lineInArray = reader.readNext()) != null) {
				// hora = lineInArray[0] 
				this.radiationPerMinute[minuto][0] = Double.parseDouble(lineInArray[1]); // "n"
				this.radiationPerMinute[minuto][1] = Double.parseDouble(lineInArray[2]); // "mean"
				this.radiationPerMinute[minuto][2] = Double.parseDouble(lineInArray[3]); // "sd"
				this.radiationPerMinute[minuto][3] = Double.parseDouble(lineInArray[4]); // "sem"
				this.radiationPerMinute[minuto][4] = Double.parseDouble(lineInArray[5]); // "CI_lower"
				this.radiationPerMinute[minuto][5] = Double.parseDouble(lineInArray[6]); // "CI_upper"
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

		// Interpolo linealmente los valores que falta entre las horas
		double step = 1.0 / 60.0;
		for(int i = 0; i < 1440; i += 60) {
			for(int j = 1; j < 60; j++) {
				this.radiationPerMinute[i+j][1] = (this.radiationPerMinute[i][1] * (1-step*j)) + (this.radiationPerMinute[(i+60)%1440][1] * (step*j)); // "mean"
				this.radiationPerMinute[i+j][4] =(this.radiationPerMinute[i][4] * (1-step*j)) + (this.radiationPerMinute[(i+60)%1440][4] * (step*j)); // "CI_lower"
				this.radiationPerMinute[i+j][5] = (this.radiationPerMinute[i][5] * (1-step*j)) + (this.radiationPerMinute[(i+60)%1440][5] * (step*j)); // "CI_upper"			
			}
		}
	}
	public double[] getMinuteData(int m) {
		if(m < 0 || m >= 1440)
			return null;
		return this.radiationPerMinute[m];
	}
	
	private double getDefinedRadiation(int m) {
		return this.radiationPerMinute[m % 1440][1];
	}
	
	private double getRandomRadiation(int m) {
		int min = (int)(this.radiationPerMinute[m % 1440][4] * 100);
		int max = (int)(this.radiationPerMinute[m % 1440][5] * 100);
		return Double.valueOf(this.rand.nextInt((max - min) + 1) + min) / 100.0;
	}
	
	private double getRandomGaussianRadiation(int m) {
		double min = this.radiationPerMinute[m % 1440][4];
		double max = this.radiationPerMinute[m % 1440][5];

		double d = (max-min) / 4.0;
		double mean = (min+max) / 2.0;
		
		return this.rand.nextGaussian() * d + mean;
	}
	
	private double getPerlinRadiation(int m) {
		double min = this.radiationPerMinute[m % 1440][4];
		double max = this.radiationPerMinute[m % 1440][5];

		double d = (max-min) / 3.0;
		double mean = (min+max) / 2.0;
		
		return mean + d * this.ng.noise(m/5.0);
	}
	
	public double getPower(int m) {
		//return getPerlinRadiation(m) * this.proportionParameter;
		double radiation = getPerlinRadiation(m);
		
		if(Simulation.SIMULAR == false)
			radiation = getDefinedRadiation(m);
		
		return (radiation * Math.exp(radiation/550.0)) / 60.0; // Paso de Wh a Wm
	}
}
