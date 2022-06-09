package com.um.simEnergy;

import java.util.LinkedList;
import java.util.List;

import com.um.simEnergy.Battery.BasicBattery;
import com.um.simEnergy.Battery.BasicBatteryWithGenerator;
import com.um.simEnergy.Controller.BasicController;
import com.um.simEnergy.Controller.Controller;
import com.um.simEnergy.Controller.DummyController;
import com.um.simEnergy.Controller.GreedyController;
import com.um.simEnergy.Controller.RLController;
import com.um.simEnergy.LoadPower.ElectricalLoad;
import com.um.simEnergy.LoadPower.ModeledPasiveLoad;
import com.um.simEnergy.LoadPower.RandomActiveLoad;
import com.um.simEnergy.LoadPower.ServicesActiveLoad;
import com.um.simEnergy.LoadPower.UnexpectedEvents;
import com.um.simEnergy.PhotovoltaicModule.ModeledPhotovoltaicModule;
import com.um.simEnergy.PhotovoltaicModule.PhotovoltaicModule;
import com.um.simEnergy.PhotovoltaicModule.SimulatedPhotovoltaicModule;
import com.um.simEnergy.Service.Service;

public class Simulation {
	public final static boolean SIMULAR = false;
	
	// Lista de fuentes de energias y consumos
	private List<PhotovoltaicModule> photovoltaicModules;
	private List<ElectricalLoad> powerDemand;
	
	// Bateria del sistema (TODO: pasar a una lista)
	//private BasicBattery Battery;
	private BasicBatteryWithGenerator Battery;

	// Consumo activo de energia basado en los servicios definidos
	private ServicesActiveLoad AL;
	
	// Servicios definidos
	private List<Service> servicesList;
	
	// Controlador de servicios
	private Controller serviceController;
	
	// Contenedor de los resultados de la última simulación
	private SimulationResults SR;
	
	// Controlador de eventos inesperados
	private UnexpectedEvents unexpectedEvents;
	
	public Simulation() {
		// Lista de fuentes de energias y consumos
		this.photovoltaicModules = new LinkedList<PhotovoltaicModule>();
		this.powerDemand = new LinkedList<ElectricalLoad>();
		
		// Panel solar basado en datos reales
		//ModeledPhotovoltaicModule PV = new ModeledPhotovoltaicModule();
		//PV.readFrom10mCSV("./data/PotenciaRangoD_summary.csv");
		
		// Simulacion de panel solar basado en irradiacion usando datos reales
		SimulatedPhotovoltaicModule PVi = new SimulatedPhotovoltaicModule(3.2);
		PVi.readFrom1hCSV("./data/SolarRadiationSumary.csv");
		
		// Consumo de energia base del escenario de pruebas
		ModeledPasiveLoad PL = new ModeledPasiveLoad();
		PL.readFrom1hCSV("./data/LoadPowerSumary.csv");

		// Consumo activo de energia aleatorio (para pruebas)
		//RandomActiveLoad rAL = new RandomActiveLoad();
		
		// Defino servicios de ejemplo
		servicesList = new LinkedList<Service>();
		// Nombre, Inteligente, Consumo Wh, rando de funcionamiento/expresion lambda/prioridad
		//servicesList.add(new Service("Luces valla", false, 15.0, new int[]{2,480,1200})); // Rango de exclusion, no enciendo de dia (8-20)
		servicesList.add(new Service("Luces valla", false, 15.0, (m) -> m < 480 || m >= 1200)); // Como lambda expression
		//servicesList.add(new Service("Luces fachada", false, 10.0, new int[]{2,480,1200})); // Rango de exclusion, no enciendo de dia (8-20)
		servicesList.add(new Service("Luces fachada", false, 10.0, (m) -> m < 480 || m >= 1200)); // Como lambda expression
		servicesList.add(new Service("Frigorifico", false, 120.0)); // A+++
		
		// Servicios administrables/inteligentes (por defecto estan apagados menos los de ejecucion 24h)
		servicesList.add(new Service("Motor piscina", true, 600.0, 4).setSmartParameters(9*60, 17*60, 3*60)); // 600Wh y prioridad 4; De 9 a 17, max 3h
		servicesList.add(new Service("Videograbador", true, 20.0, 10).setSmartParameters(-1, -1, 0)); // 20Wh y prioridad 10;
		servicesList.add(new Service("Internet", true, 40.0, 8).setSmartParameters(-1, -1, 0)); // 40Wh y prioridad 8;
		servicesList.add(new Service("StreamServices", true, 30.0, 2).setSmartParameters(-1, -1, 0)); // 30Wh y prioridad 2;
		servicesList.add(new Service("Fuente de agua", true, 35.0, 1).setSmartParameters(9*60, 15*60, 0)); // 35Wh y prioridad 1; De 9 a 15
		
		// Configuracion 2
		/*servicesList = new LinkedList<Service>();
		servicesList.add(new Service("Luces valla", false, 15.0, (m) -> m < 480 || m >= 1200)); // Como lambda expression
		servicesList.add(new Service("Luces fachada", false, 10.0, (m) -> m < 480 || m >= 1200)); // Como lambda expression
		servicesList.add(new Service("Frigorifico", false, 120.0)); // A+++
		// Servicios administrables/inteligentes (por defecto estan apagados menos los de ejecucion 24h)
		servicesList.add(new Service("Motor piscina", true, 400.0, 8).setSmartParameters(9*60, 17*60, 3*60)); // 600Wh y prioridad 4; De 9 a 17, max 3h
		servicesList.add(new Service("Videograbador", true, 40.0, 2).setSmartParameters(-1, -1, 0)); // 20Wh y prioridad 10;
		servicesList.add(new Service("Internet", true, 50.0, 3).setSmartParameters(-1, -1, 0)); // 40Wh y prioridad 8;
		servicesList.add(new Service("StreamServices", true, 30.0, 1).setSmartParameters(-1, -1, 0)); // 30Wh y prioridad 2;
		servicesList.add(new Service("Fuente de agua", true, 35.0, 6).setSmartParameters(9*60, 15*60, 0)); // 35Wh y prioridad 7; De 9 a 15*/
		
		// Configuracion 3
		/*servicesList = new LinkedList<Service>();
		servicesList.add(new Service("Kubernetes-cluster", true, 150.0, 10).setSmartParameters(-1, -1, 0));
		servicesList.add(new Service("Internet", true, 25.0, 9).setSmartParameters(-1, -1, 0));
		servicesList.add(new Service("VPN-router", true, 15.0, 8).setSmartParameters(-1, -1, 0));
		//servicesList.add(new Service("CCTV", true, 60.0, 7).setSmartParameters(-1, -1, 0));
		//servicesList.add(new Service("Backups", true, 50, 5).setSmartParameters(10*60, 15*60, 2*60));
		servicesList.add(new Service("Dashboard-services", true, 50.0, 4).setSmartParameters(-1, -1, 0));
		servicesList.add(new Service("Game-servers", true, 140, 2).setSmartParameters(-1, -1, 0));
		servicesList.add(new Service("Dashboard-displays", true, 180, 1).setSmartParameters(10*60, 18*60, 0));*/
		
		
		// Creo el controlador de servicios
		//serviceController = new BasicController(servicesList);
		//serviceController = new DummyController(servicesList, false);
		//serviceController = new DummyController(servicesList, true);
		serviceController = new GreedyController(servicesList);
		//serviceController = new RLController(servicesList);
		// Consumo activo de energia basado en los servicios definidos
		AL = new ServicesActiveLoad(servicesList);
		
		// Defino una bateria basica de 7kWh
		//Battery = new BasicBattery(7000);
		Battery = new BasicBatteryWithGenerator(7000);
		
		// Añado las fuentes de energia y de consumo
		photovoltaicModules.add(PVi);
		powerDemand.add(PL); // Pasive load
		powerDemand.add(AL); // Active load (services)
	}

	private double getPowerProduction(int minute) {
		double powerProduction = 0.0;

		// Recorro todos los sistemas de generacion de energia y determino la produccion total para este minuto
		for (PhotovoltaicModule pM : photovoltaicModules) {
			powerProduction += pM.getPower(minute);
		}
		
		return powerProduction;
	}

	private double getLoad(int minute) {
		double load = 0.0;
		
		// Recorro todos los sistemas y determino el consumo total para este minuto
		for (ElectricalLoad eL : powerDemand) {
			load += eL.getLoad(minute);
		}
		
		return load;
	}
	
	private void initDay(int d) {
		serviceController.initDay(d);
		// Reset services day
		for (Service service : this.servicesList) {
			service.initDay(d);
		}
	}
	
	private void diurnalCyclePerMinute(int day, int minute) {
		// Calculo la potencia actual de paneles y el consumo pasivo
		double PowerProduction = this.getPowerProduction(minute);
		double ElectricalLoad = this.getLoad(minute);
		
		// Tengo en cuenta eventos no esperados
		PowerProduction = unexpectedEvents.getPowerProduction(day, minute, PowerProduction);
		ElectricalLoad = unexpectedEvents.getElectricalLoad(day, minute, ElectricalLoad);
		
		// Uso la bateria
		double batteryUsage = PowerProduction - ElectricalLoad;
		double batteryUsageReal = Battery.loadWmperMinute(batteryUsage); // Uso real de la bateria (cuando paso el 100% es cero)
		//Battery.loadWmperMinute(batteryUsage);
		//double batteryLevel = Battery.getLevel();
		
		// Guardo el resultado de la simulacion de este minuto
		Result resultSim = SR.addResult(minute, PowerProduction, ElectricalLoad, batteryUsageReal, Battery, serviceController.getLastGlobalReward(), serviceController.getGlobalReward());
		
		// Ejecuto el controlador de servicios
		serviceController.run(minute, resultSim);
		
		// Actualizo todo lo que haya cambiado despues de ejecutar el controlador
		resultSim.update(serviceController.getLastGlobalReward(), serviceController.getGlobalReward());
		
	}
	
	public void run(int days) {
		// Inicio simulacion
		SR = new SimulationResults(days, servicesList);

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
	
	public void results() {
		//SR.printResults();
		//SR.showResults(false);
		SR.showResults(true);
		//SR.saveResults();
		//SR.saveResultsOnlyHour();
		SR.printStats();
		
		serviceController.save();
	}
}
