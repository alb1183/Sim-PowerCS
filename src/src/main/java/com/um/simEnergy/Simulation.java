package com.um.simEnergy;

import java.util.List;

import com.um.simEnergy.EnergyController.BasicEnergyController;
import com.um.simEnergy.EnergyController.EnergyController;
import com.um.simEnergy.EnergyStorage.BasicBattery;
import com.um.simEnergy.EnergyStorage.Battery;
import com.um.simEnergy.EnergyStorage.Grid;
import com.um.simEnergy.LoadPower.ElectricalLoad;
import com.um.simEnergy.LoadPower.UnexpectedEvents;
import com.um.simEnergy.PowerProducer.PowerProducer;
import com.um.simEnergy.Service.Service;
import com.um.simEnergy.ServiceController.BasicController;
import com.um.simEnergy.ServiceController.DummyController;
import com.um.simEnergy.ServiceController.GreedyController;
import com.um.simEnergy.ServiceController.PriorityBasedController;
import com.um.simEnergy.ServiceController.ServiceController;

public class Simulation {
	public static boolean SIMULAR = false;
	private Config config;
	
	// Lista de fuentes de energias y consumos
	private List<PowerProducer> powerProducer;
	private List<ElectricalLoad> powerDemand;
	
	// Almacenes de energia (baterias y red electrica)
	private Battery batteryStorage;
	private Grid gridStorage;
	
	// Servicios definidos
	private List<Service> servicesList;
	
	// Controlador de servicios
	private ServiceController serviceController;
	
	// Controlador de energia
	private EnergyController energyController;
	
	// Contenedor de los resultados de la última simulación
	private SimulationResults SR;
	
	// Controlador de eventos inesperados
	private UnexpectedEvents unexpectedEvents;
	
	public Simulation(Config config) {
		this.config = config;
		SIMULAR = this.config.isProcedural();
		
		// Lista de fuentes de energias y consumos
		this.powerProducer = this.config.getPowerProducer();
		this.powerDemand = this.config.getPowerDemand();

		// Almacenes de energia (baterias y red electrica)
		this.batteryStorage = new BasicBattery(this.config.getBattery());
		this.gridStorage = this.config.getGrid();
		
		// Listado de servicios definidos
		this.servicesList = this.config.getServicesList();
		
		// Creo el controlador de servicios
		switch (this.config.getController()) {
			case "Basic":
				serviceController = new BasicController(servicesList);
				break;
				
			case "DummyOff":
				serviceController = new DummyController(servicesList, false);
				break;
				
			case "DummyOn":
				serviceController = new DummyController(servicesList, true);
				break;
				
			case "Priority":
				serviceController = new PriorityBasedController(servicesList);
				break;
				
			case "Greedy":
				serviceController = new GreedyController(servicesList);
				break;
	
			default:
				serviceController = new DummyController(servicesList, true);
				break;
		}
		
		// Creo el controlador de energia
		this.energyController = new BasicEnergyController(batteryStorage, gridStorage);
	}

    /**
    * Determine the exact energy production for that minute using the list of energy sources.
    * @param minute Minute
    * @return Watts per hour of production in that minute
    */
	private double getPowerProduction(int minute) {
		double powerProduction = 0.0;

		// Recorro todos los sistemas de generacion de energia y determino la produccion total para este minuto
		for (PowerProducer pP : powerProducer) {
			powerProduction += pP.getPower(minute);
		}
		
		return powerProduction;
	}

    /**
    * Determine the exact load for that minute using the list of energy load (services, pasives, ...).
    * @param minute Minute
    * @return Watts per hour of load in that minute
    */
	private double getLoad(int minute) {
		double load = 0.0;
		
		// Recorro todos los sistemas y determino el consumo total para este minuto
		for (ElectricalLoad eL : powerDemand) {
			load += eL.getLoad(minute);
		}
		
		return load;
	}

    /**
    * Reset daily parameters
    */
	private void initDay(int d) {
		serviceController.initDay(d);
		// Reset services day
		for (Service service : this.servicesList) {
			service.initDay(d);
		}
	}

    /**
    * Main simulation iteration (per minute)
    */
	private void diurnalCyclePerMinute(int day, int minute) {
		// Calculo la potencia actual de las fuentes de energia y el consumo pasivo
		double powerProduction = this.getPowerProduction(minute);
		double electricalLoad = this.getLoad(minute);
		
		// Tengo en cuenta eventos no esperados
		// TODO: Mejorar metodologia
		powerProduction = unexpectedEvents.getPowerProduction(day, minute, powerProduction);
		electricalLoad = unexpectedEvents.getElectricalLoad(day, minute, electricalLoad);
		
		// Ejecuto el controlador de energia
		energyController.run(minute, powerProduction, electricalLoad);
		
		// Guardo el resultado de la simulacion de este minuto
		Result resultSim = SR.addResult(minute, powerProduction, electricalLoad);
		
		// Ejecuto el controlador de servicios
		serviceController.run(minute, resultSim);
		
		// Actualizo todo lo que haya cambiado despues de ejecutar el controlador
		resultSim.update();
		
	}

    /**
    * Method to run the main simulation
    */
	public void run() {
		int days = this.config.getDays();
		
		// Inicio simulacion
		SR = new SimulationResults(days, servicesList, batteryStorage, gridStorage);

		// Generador de eventos no experados
		unexpectedEvents = new UnexpectedEvents(days);
		
		// Dias a simular
		for(int d = 0; d < days; d++) {
			initDay(d);
			// Ciclo en minutos de cada dia
			for(int m = 0; m < 60*24; m++) {
				int minuto = (d*60*24) + m;
				// Minuto a simular
				diurnalCyclePerMinute(d, minuto);
			}
		}
		
	}

    /**
    * Method to print results
    */
	public void results() {
		if(this.config.isPrintResults())
			SR.printResults();
		
		if(this.config.isShowResults())
			SR.showResults(this.config.isSaveGraphs());

		if(this.config.isSaveResults())
			SR.saveResults();

		if(this.config.isSaveResultsOnlyHour())
			SR.saveResultsOnlyHour();

		if(this.config.isPrintStats())
			SR.printStats();
	}
}
