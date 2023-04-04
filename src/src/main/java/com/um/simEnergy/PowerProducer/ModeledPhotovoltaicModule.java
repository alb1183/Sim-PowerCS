package com.um.simEnergy.PowerProducer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.um.simEnergy.utils.NoiseGenerator;

public class ModeledPhotovoltaicModule extends PowerProducer {

	private double[][] powerPerMinute;
	private Random rand;
	private NoiseGenerator ng;

	public ModeledPhotovoltaicModule() {
		this.powerPerMinute = new double[24 * 60][6];
		this.rand = new Random();
		this.ng = new NoiseGenerator();
	}

	public void printPowerPerMinute() {
		System.out.println(Arrays.deepToString(this.powerPerMinute).replace("], ", "]\n"));
	}

	public void readFrom10mCSV(String file) {
		// "minuto","n","mean","sd","sem","CI_lower","CI_upper"
		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			String[] lineInArray;
			int minuto = 0;
			while ((lineInArray = reader.readNext()) != null) {
				// minuto = lineInArray[0] 
				this.powerPerMinute[minuto][0] = Double.parseDouble(lineInArray[1]); // "n"
				this.powerPerMinute[minuto][1] = Double.parseDouble(lineInArray[2]); // "mean"
				this.powerPerMinute[minuto][2] = Double.parseDouble(lineInArray[3]); // "sd"
				this.powerPerMinute[minuto][3] = Double.parseDouble(lineInArray[4]); // "sem"
				this.powerPerMinute[minuto][4] = Double.parseDouble(lineInArray[5]); // "CI_lower"
				this.powerPerMinute[minuto][5] = Double.parseDouble(lineInArray[6]); // "CI_upper"
				minuto += 10;
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

		// Interpolo linealmente los valores que falta
		for(int i = 0; i < 1430; i += 10) {
			for(int j = 1; j < 10; j++) {
				this.powerPerMinute[i+j][1] = (this.powerPerMinute[i][1] * (1-0.1*j)) + (this.powerPerMinute[i+10][1] * (0.1*j)); // "mean"
				this.powerPerMinute[i+j][4] =(this.powerPerMinute[i][4] * (1-0.1*j)) + (this.powerPerMinute[i+10][4] * (0.1*j)); // "CI_lower"
				this.powerPerMinute[i+j][5] = (this.powerPerMinute[i][5] * (1-0.1*j)) + (this.powerPerMinute[i+10][5] * (0.1*j)); // "CI_upper"			
			}
		}
	}
	
	public double[] getMinuteData(int m) {
		if(m < 0 || m >= 1440)
			return null;
		return this.powerPerMinute[m];
	}
	
	private double getDefinedPower(int m) {
		return this.powerPerMinute[m % 1440][1];
	}
	
	private double getRandomPower(int m) {
		int min = (int)(this.powerPerMinute[m % 1440][4] * 100);
		int max = (int)(this.powerPerMinute[m % 1440][5] * 100);
		return Double.valueOf(this.rand.nextInt((max - min) + 1) + min) / 100.0;
	}
	
	private double getRandomGaussianPower(int m) {
		double min = this.powerPerMinute[m % 1440][4];
		double max = this.powerPerMinute[m % 1440][5];

		double d = (max-min) / 4.0;
		double mean = (min+max) / 2.0;
		
		return this.rand.nextGaussian() * d + mean;
	}
	
	private double getPerlinPower(int m) {
		double min = this.powerPerMinute[m % 1440][4];
		double max = this.powerPerMinute[m % 1440][5];

		double d = (max-min) / 2.0;
		double mean = (min+max) / 2.0;
		
		return mean + d * this.ng.noise(m);
	}
	
	public double getPower(int m) {
		return getDefinedPower(m);
		//return getPerlinPower(m);
	}
}
