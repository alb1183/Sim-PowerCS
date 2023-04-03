package com.um.simEnergy;

import java.util.List;

import com.um.simEnergy.EnergyStorage.BasicGrid;
import com.um.simEnergy.LoadPower.ElectricalLoad;
import com.um.simEnergy.LoadPower.ModeledPasiveLoad;
import com.um.simEnergy.LoadPower.ServicesActiveLoad;
import com.um.simEnergy.PowerProducer.PowerProducer;
import com.um.simEnergy.PowerProducer.SimulatedPhotovoltaicModule;
import com.um.simEnergy.Service.Service;

/**
 * Codigo Principal
 *
 */
public class App {
	public static void main(String[] args) {
		
		/************************ Configuraciones ************************/
		// Configuro los parametros de la simulacion
		Config config = new Config("./data/config.xml");
		double seed = config.getSeed();
		
		// Paso las listas a variables por comodidad
		List<PowerProducer> powerProducer = config.getPowerProducer();
		List<ElectricalLoad> powerDemand = config.getPowerDemand();
		
		/******* Configuro la generación de energia *******/
		// Panel solar basado en datos reales
		//ModeledPhotovoltaicModule PV = new ModeledPhotovoltaicModule();
		//PV.readFrom10mCSV("./data/Models/PotenciaRangoD_summary.csv");
		
		// Simulacion de panel solar basado en irradiacion usando datos reales
		SimulatedPhotovoltaicModule PVi = new SimulatedPhotovoltaicModule(3.2, seed);
		PVi.readFrom1hCSV("./data/Models/SolarRadiationSumary.csv");
		powerProducer.add(PVi);
		

		/******* Configuro la generación de energia *******/
		config.setGrid(new BasicGrid("./data/Models/EnergyPrices.csv", seed));
		
		
		/******* Configuro los consumos de energia *******/
		// Consumo de energia base del escenario de pruebas, definido como Pasive Load
		//ModeledPasiveLoad PL = new ModeledPasiveLoad();
		//PL.readFrom1hCSV("./data/Models/LoadPowerSumary.csv");
		//powerDemand.add(PL); // Añado el Pasive load

		// Consumo activo de energia aleatorio (para pruebas)
		//RandomActiveLoad rAL = new RandomActiveLoad();
		//powerDemand.add(rAL); // Añado el random load
		
		/*** Consumo basado en servicios ***/
		// Configuracion 1
		List<Service> servicesList = config.loadServices("./data/services.xml");
		// Configuracion 2
		//List<Service> servicesList = config.loadServices("./data/services1.xml");
		// Configuracion 3
		//List<Service> servicesList = config.loadServices("./data/services2.xml");
		
		// Consumo activo de energia basado en los servicios definidos
		ServicesActiveLoad AL = new ServicesActiveLoad(servicesList);
		powerDemand.add(AL); // Active load (services)
		
		
		
		/************************ Simulación ************************/
		// Inicio la simulacion		
		Simulation sm = new Simulation(config);
		sm.run();
		sm.results();
	}
}
