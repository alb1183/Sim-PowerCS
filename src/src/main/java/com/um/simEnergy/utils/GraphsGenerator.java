package com.um.simEnergy.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GraphsGenerator {

	static int minutos = 36000;
	static int servicios = 8;
	
	static boolean[][] serviceState;
	
	public static void main(String[] args) {
		serviceState = new boolean[minutos][servicios];
		
		for(int i = 0; i < minutos; i++) {
			//
		}
		
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("L:\\Datos\\Google Drive\\Universidad\\6º Doctorado\\Articulo por hacer\\Casa ruinas\\Resultados\\Configuracion 1 - 25 dias\\RL\\5º - 7415\\output.csv"));
			String line = reader.readLine(); // Me salto la primera linea
			while (line != null) {
				line = reader.readLine();
				String[] columns = line.split(",");
				
				int minute = Integer.parseInt(columns[1].trim());
				
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
