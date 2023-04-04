package com.um.simEnergy.LoadPower;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.um.simEnergy.Simulation;
import com.um.simEnergy.utils.NoiseGenerator;

public class ModeledPasiveLoad extends ElectricalLoad {
	private double[][] loadPerMinute;
	private Random rand;
	private NoiseGenerator ng;

	public ModeledPasiveLoad() {
		this.loadPerMinute = new double[24 * 60][6];
		this.rand = new Random();
		this.ng = new NoiseGenerator();
	}

	public void printLoadPerMinute() {
		System.out.println(Arrays.deepToString(this.loadPerMinute).replace("], ", "]\n"));
	}

	public void readFrom1hCSV(String file) {
		// "n","mean","sd","sem","CI_lower","CI_upper"
		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			String[] lineInArray;
			int minuto = 0;
			while ((lineInArray = reader.readNext()) != null) {
				// minuto = lineInArray[0] 
				this.loadPerMinute[minuto][0] = Double.parseDouble(lineInArray[0]); // "n"
				this.loadPerMinute[minuto][1] = Double.parseDouble(lineInArray[1]); // "mean"
				this.loadPerMinute[minuto][2] = Double.parseDouble(lineInArray[2]); // "sd"
				this.loadPerMinute[minuto][3] = Double.parseDouble(lineInArray[3]); // "sem"
				this.loadPerMinute[minuto][4] = Double.parseDouble(lineInArray[4]); // "CI_lower"
				this.loadPerMinute[minuto][5] = Double.parseDouble(lineInArray[5]); // "CI_upper"
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
				this.loadPerMinute[i+j][1] = (this.loadPerMinute[i][1] * (1-step*j)) + (this.loadPerMinute[(i+60)%1440][1] * (step*j)); // "mean"
				this.loadPerMinute[i+j][4] =(this.loadPerMinute[i][4] * (1-step*j)) + (this.loadPerMinute[(i+60)%1440][4] * (step*j)); // "CI_lower"
				this.loadPerMinute[i+j][5] = (this.loadPerMinute[i][5] * (1-step*j)) + (this.loadPerMinute[(i+60)%1440][5] * (step*j)); // "CI_upper"			
			}
		}
	}
	
	public double[] getMinuteData(int m) {
		if(m < 0 || m >= 1440)
			return null;
		return this.loadPerMinute[m];
	}
	
	private double getDefinedLoad(int m) {
		return this.loadPerMinute[m % 1440][1];
	}
	
	private double getRandomLoad(int m) {
		int min = (int)(this.loadPerMinute[m % 1440][4] * 100);
		int max = (int)(this.loadPerMinute[m % 1440][5] * 100);
		return Double.valueOf(this.rand.nextInt((max - min) + 1) + min) / 100.0;
	}
	
	private double getRandomGaussianLoad(int m) {
		double min = this.loadPerMinute[m % 1440][4];
		double max = this.loadPerMinute[m % 1440][5];

		double d = (max-min) / 4.0;
		double mean = (min+max) / 2.0;
		
		return this.rand.nextGaussian() * d + mean;
	}
	
	private double getPerlinLoad(int m) {
		double min = this.loadPerMinute[m % 1440][4];
		double max = this.loadPerMinute[m % 1440][5];

		double d = (max-min) / 2.0;
		double mean = (min+max) / 2.0;
		
		return mean + d * this.ng.noise(m/10.0);
	}
	
	public double getLoad(int m) {
		if(Simulation.SIMULAR == false)
			return getDefinedLoad(m) / 60.0 + (45.0/60.0); // Paso de Wh a Wm // Sumo el overloadFix del inversor
		
		return getPerlinLoad(m) / 60.0 + (45.0/60.0); // Paso de Wh a Wm // Sumo el overloadFix del inversor
	}

}
