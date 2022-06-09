package com.um.simEnergy.Controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.um.simEnergy.Result;
import com.um.simEnergy.Service.Service;

public class RLController extends Controller {
	// Stats
	private double rewardSum = 0;
	private int rewardNum = 0;
	
	// Parametro del algoritmo RL
	private double initialQvalueOff = 0.0;
	private double initialQvalueOn = 50.0;
	private double epsilon = 0.2;
	//private double[] beta = {15, 0.01, -0.01, 1, 1, 1, 1}; // priority, runningTime, waitingTime, energyPenalty, batteryReward, minBatteryPenalty, generatorPenalty
	private double[] beta = {50, 0.02, -0.01, 1, 1, 1, 1}; // priority, runningTime, waitingTime, energyPenalty, batteryReward, minBatteryPenalty, generatorPenalty
	private double gamma = 0.8;
	private double alpha = 0.2;
	
	// QTable
	//public Map<String, Qrow> Qtable = new HashMap<String, Qrow>();
	
	// Multiagent, Qtables por servicio
	public List<Map<String, Qrow>> QtableList;
	public String[][] ServicesLastDecision;
	
	

	public RLController(List<Service> servicesList) {
		super(servicesList);

		QtableList = new ArrayList<>();
		// Q-Table
		for (Service service : this.smartServices) {
			QtableList.add(new HashMap<String, Qrow>());
		}
		
		ServicesLastDecision = new String[this.smartServices.size()][3]; // minuto, estado, accion
	
		//this.loadQTables();
	}

	public double decision(int minute, Result resultSim) {
		int minuteDay = minute % 1440;
		
		double localRewards = 0;
		
		// Ejecuto el controlador de cada servicio (multi-agent reinforcement learning)
		for(int i = 0; i < this.smartServices.size(); i++) {
			Service serv = this.smartServices.get(i);

			// Feeback de la accion anterior
			if(ServicesLastDecision[i][0] != null && Integer.parseInt(ServicesLastDecision[i][0]) == minute-this.waitTime) {
				double feedbackReward = reinforcementFeedback(i, resultSim);
				localRewards += feedbackReward * serv.getPriority();
				//System.out.println(" --- " + resultSim.getMinute() + " ("+(resultSim.getMinute()%1440)/60.0+")" +"| " + avgEnergyProduction + " (" + avgBatteryLevel + ")" + " --> " + i + "(" + serv.getRunningTime() + ") - " + ServicesLastDecision[i][1]);
			}
			
			// Si... /*y hay luz (2% bateria)*/
			if(serv.isWorkingTime(minuteDay) && serv.checkRunTime(minute) /*&& avgBatteryLevel >= 2*/) {
				// Determino la accion a realizar para el servicio i-esimo
				int accion = reinforcementLearning(i, resultSim);
				
				// Ejecuto la accion sobre el servicio
				if(accion == 0) { // Accion de apagar
					serv.turnOff(minute);
				} else if(accion == 1) { // Accion de encender
					serv.turnOn(minute);
				}
				
				// Feeback?...
				System.out.println(resultSim.getMinute() + " ("+(resultSim.getMinute()%1440)/60.0+")" +" | " + avgEnergyProduction + ", " + avgElectricalLoad + " (" + avgBatteryLevel + ", " + minBatteryPercentage + ")" + " --> " + serv.getName() + " (" + serv.getRunningTime(minute) + ") - " + ServicesLastDecision[i][1] + " => " + accion);
			} else {
				// Comportamiento por defecto si no ejecuto el RL
				serv.turnOff(minute);
				
				// Borro la reward media del log para que se vea mas claro cuando se ejecuta RL
				serv.setLastReward(0);
			}
		}

		return localRewards / QtableList.size();
	}
	
	// Algoritmo principal de offloading RL
	public int reinforcementLearning(int servicio, Result resultSim) {	
		// *** Determino el estado ***		
		String estado = getRLState(resultSim);

		// *** Determino el conjunto de acciones ***
		List<Qrow> acciones = getAccionesList(servicio, estado);

		
		// *** Exploración VS Explotación ***
		int accion;
		double e = new Random().nextFloat();
		double decayedEpsilon = epsilon / (1+(resultSim.getMinute()/1120.0)); // Reduzco la epsilon una decima parte por semana (de 0.2 a 0.02) // (60*24*7)/9=1120
		///System.out.println("E: " + resultSim.getMinute() + " --> " + decayedEpsilon);
		double decayedEpsilonAgent = decayedEpsilon/this.smartServices.get(servicio).getPriority(); // Se reduce el factor aleatorio en funcion de la prioridad
		if(e < decayedEpsilonAgent) { // Exploración
			// Acción aleatoria
			//System.out.println("aleatorio");
			accion = acciones.get(new Random().nextInt(acciones.size())).getAccion();
		} else { // Explotación
			accion = getRLAccion(acciones);
		}
		
		// Indico que acción se ha tomado en esta tarea como un metadato
		//task.setMetaData(new String[] { estado + "_" + accion, Integer.toString(accion)});
		ServicesLastDecision[servicio][0] = String.valueOf(resultSim.getMinute());
		ServicesLastDecision[servicio][1] = estado;
		ServicesLastDecision[servicio][2] = String.valueOf(accion);
		
		return accion;
	}
	
	private String getRLState(Result resultSim) {
		// *** Determino el estado original ***
		/*double batteryPercentage = resultSim.getBatteryPercentage();
		double energyAvailable = resultSim.getPowerProduction()-resultSim.getElectricalLoad();*/
		double batteryPercentage = avgBatteryLevel;
		double energyAvailable = avgEnergyProduction-avgElectricalLoad;
		int dayHour = resultSim.getDayHour();
		
		// *** Discretizo el estado en un conjuntos finitos *** (TODO: Fuzzification)
		String lastMaxBatteryGenerator = (maxBatteryGenerator < 1) ? "none" : (maxBatteryGenerator < 8) ? "low" : (maxBatteryGenerator < 25) ? "medium" : "high";
		String lastMinBatteryPercentageTerm = (minBatteryPercentage < 2) ? "empty" : (minBatteryPercentage < 20) ? "critical" : (batteryPercentage < 40) ? "low" : (batteryPercentage < 60) ? "medium" : (batteryPercentage < 80) ? "normal" : "high";
		String batteryPercentageTerm = (batteryPercentage < 2) ? "empty" : (batteryPercentage < 20) ? "critical" : (batteryPercentage < 40) ? "low" : (batteryPercentage < 60) ? "medium" : (batteryPercentage < 80) ? "normal" : "high";
		String energyAvailableTerm = (energyAvailable < 0) ? "negative" : (energyAvailable < 500) ? "low" : (energyAvailable < 1000) ? "medium" : (energyAvailable < 1500) ? "normal" : "high";
		String dayHourTerm = (dayHour < 8) ? "night" : (dayHour < 11) ? "morning" : (dayHour < 17) ? "afternoon" : "evening";
		
		// Estado del sistema
		String estado = /*dayHourTerm + "-" + lastMaxBatteryGenerator + "-" + */lastMinBatteryPercentageTerm + "-" + batteryPercentageTerm + "-" + energyAvailableTerm;
		
		return estado;
	}
	
	private List<Qrow> getAccionesList(int servicio, String estado) {
		// *** Determino el conjunto de acciones ***
		List<Qrow> acciones = new LinkedList<Qrow>();

		acciones.add(getQTable(servicio, estado + "_0", 0)); // Accion de apagar
		// si la energia da suficiente considero encenderlo, sino nope?
		acciones.add(getQTable(servicio, estado + "_1", 1)); // Accion de encender
		
		return acciones;
	}
	
	private int getRLAccion(List<Qrow> acciones) {
		int accion = acciones.get(0).getAccion();
		//double minQValue = acciones.get(0).getValue();
		double maxQValue = acciones.get(0).getValue();
		
		for(int i = 1; i < acciones.size(); i++) {
			//if(acciones.get(i).getValue() < minQValue) {
			//	minQValue = acciones.get(i).getValue();
			if(acciones.get(i).getValue() > maxQValue) {
				maxQValue = acciones.get(i).getValue();
				accion = acciones.get(i).getAccion();
			}
		}
		
		return accion;
	}
	
	public double reinforcementFeedback(int servicio, Result resultSim) {
		// Acción que se tomó en esa tarea
		String estadoLast = ServicesLastDecision[servicio][1];
		int accion = Integer.parseInt(ServicesLastDecision[servicio][2]); // 0 off, 1 on
		Service serv = this.smartServices.get(servicio);
		int accionSigned = (accion == 0) ? -1 : 1; // -1 off, 1 on

		double minBatteryPercentageI = 100-minBatteryPercentage;
		double minBatteryPercentageIPow = Math.pow(minBatteryPercentageI, 1);
		//double minBatteryPercentageIPow = Math.pow(minBatteryPercentageI, 2);
		double avgBatteryLevelI = 100-avgBatteryLevel;
		double avgBatteryLevelIPow = Math.pow(avgBatteryLevelI, 2);

		// Calculo la recompensa
		double waitingTime = serv.getWaitingTime();
		//double priority = Math.sqrt(accion*serv.getPriority());
		double priority = accion*serv.getPriority();
		//double priority = accion*serv.getPriority()*9; // CHECK
		double runningTime = accion*serv.getRunningTime(resultSim.getMinute());
		
		double powerConsumption = serv.getPowerConsumption();
		double powerConsumptionOverBattery = powerConsumption / resultSim.getBatteryCapacity();
		double consumptionOverBatteryExp = Math.pow(powerConsumptionOverBattery, 2);
		//double consumptionOverBatteryExp = Math.pow(powerConsumptionOverBattery, 1.05)*0.6; // CHECK
		
		//double powerConsumptionOverElectricalLoad = powerConsumption / avgElectricalLoad;
		//double powerConsumptionOverEnergyProduction = powerConsumption / avgEnergyProduction;
		/*double energyPenalty = 0*-5000 * accion*( consumptionOverBatteryExp * (avgEnergyProduction-avgElectricalLoad) );
		double batteryReward = -40 * accion * consumptionOverBatteryExp * avgBatteryLevelIPow;
		double minBatteryPenalty = -400 * accionSigned * consumptionOverBatteryExp * minBatteryPercentageIPow;
		double generatorPenalty = -2000 * accion * consumptionOverBatteryExp * avgBatteryGenerator;*/
		double energyPenalty = /** TODO **/0*-1000 * accion*( consumptionOverBatteryExp * (avgEnergyProduction-avgElectricalLoad) ); 
		double batteryReward = -40 * accion * consumptionOverBatteryExp * avgBatteryLevelIPow;
		double minBatteryPenalty = -200 * accionSigned * consumptionOverBatteryExp * minBatteryPercentageIPow;
		double generatorPenalty = -2000 * accion * consumptionOverBatteryExp * avgBatteryGenerator;

		// Recompensa final
		double reward = beta[0] * priority + beta[1] * runningTime + beta[2] * waitingTime + beta[3] * energyPenalty + beta[4] * batteryReward + beta[5] * minBatteryPenalty + beta[6] * generatorPenalty;
		
		//serv.setLastReward(reward);
		//serv.setMeanReward(reward)
		serv.setAvgReward(reward);
		
		// Nuevo estado		
		String estadoN = getRLState(resultSim);

		// *** Determino el conjunto de acciones y la accion a tomar ***
		List<Qrow> acciones = getAccionesList(servicio, estadoN);
		int accionN = getRLAccion(acciones);
		
		// Valor actual para esa estado-accion
		double q = getQTable(servicio, estadoN + "_" + accionN, accionN).getValue();
		
		// Actualizo la qTable
		updateQTable(servicio, estadoLast + "_" + accion, accion, reward, q);
		
		// Devuelvo la reward
		return reward;
	}

	private void updateQTable(int service, String rule, int accion, double reward, double q) {		
		// Existe la entrada en la tabla
		if(QtableList.get(service).containsKey(rule)) {
			Qrow row = QtableList.get(service).get(rule);
			
			double QValue = row.getValue();
			row.increaseUpdatesCount();

			QValue = QValue*(1-alpha) + alpha*(reward + gamma*q);
			row.setValue(QValue);
		} else { // No está la entrada de esa regla, la creo desde cero
			Qrow row = new Qrow(rule, accion, reward);
			QtableList.get(service).put(rule, row);
		}
		
		// Actualizo el contador de reward media
		updateAvgReward(reward);
	}
	

	private Qrow getQTable(int service, String rule, int accion) {
		// Existe la entrada en la tabla
		if(QtableList.get(service).containsKey(rule)) {
			return QtableList.get(service).get(rule);
		} else { // No está la entrada de esa regla, la creo desde cero
			double qValue = (accion == 0) ? this.initialQvalueOff : this.initialQvalueOn;
			Qrow row = new Qrow(rule, accion, qValue);
			QtableList.get(service).put(rule, row);
			return row;
		}
		
	}
	
	private void updateAvgReward(double reward) {
		this.rewardSum += reward;
		this.rewardNum++;
	}
	
	public double getAvgReward() {
		double avgReward = this.rewardSum / this.rewardNum;
		this.rewardNum = 0;
		this.rewardSum = 0;
		return avgReward;
	}
	
	
	/*** Utils ***/

	public void save() {
		this.saveQTables();
	}
	
	private void saveQTables() {
		for(int i = 0; i < this.smartServices.size(); i++) {
			Map<String, Qrow> Qtable = QtableList.get(i);
			File file = new File("./data/QTables/S"+i+"_qTable.txt");
	        BufferedWriter bf = null;
			try {
				bf = new BufferedWriter(new FileWriter(file));
				for (Map.Entry<String, Qrow> entry : Qtable.entrySet()) {
					bf.write(entry.getKey() + ":" + ((Qrow)entry.getValue()).getAccion() + ":" + ((Qrow)entry.getValue()).getValue() + ":" + ((Qrow)entry.getValue()).getUpdatesCount());
					bf.newLine();
				}
	
				bf.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bf.close();
				} catch (Exception e) {
				}
			}
		}
	}
	

	
	private void loadQTables() {
		for(int i = 0; i < this.smartServices.size(); i++) {
			Map<String, Qrow> Qtable = QtableList.get(i);
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader("./data/QTables/S"+i+"_qTable.txt"));
				String line = reader.readLine();
				while (line != null) {
					String[] linea = line.split(":");
					String rule = linea[0];
					int accion = Integer.parseInt(linea[1]); 
					double value = Double.parseDouble(linea[2]); 
					int count = Integer.parseInt(linea[3]); 
					
					Qrow row = new Qrow(rule, accion, value);
					row.setUpdatesCount(count);
					Qtable.put(rule, row);
					
					line = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/*** Utils ***/
}
