package com.um.simEnergy;


/**
 * Main code
 *
 */
public class App {
	public static void main(String[] args) {
		
		Simulation sm = new Simulation();
		sm.run(25);
		sm.results();
	}
}
