package com.um.simEnergy;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.um.simEnergy.EnergyStorage.Grid;
import com.um.simEnergy.LoadPower.ElectricalLoad;
import com.um.simEnergy.PowerProducer.PowerProducer;
import com.um.simEnergy.Service.Service;

/**
*
* Configuration class
*
*/
public class Config {
	private String controller;
	private boolean procedural;
	private double seed;
	private int days;
	private int battery;
	private int batteryRate;
	private Grid grid;
	
	private boolean printResults;
	private boolean printStats;
	private boolean showResults;
	private boolean saveGraphs;
	private boolean saveResults;
	private boolean saveResultsOnlyHour;
	
	// Lista de fuentes de energias y consumos
	private List<PowerProducer> powerProducer;
	private List<ElectricalLoad> powerDemand;

	// Servicios definidos
	private List<Service> servicesList;

    /**
    * Method (constructor) to load configuration from xml file
    * @param path Path to the configuration xml file
    */
	public Config(String path) {
		// Parseo el XML
        File xmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            this.controller = getTagValue("controller", doc);
            this.days = Integer.parseInt(getTagValue("days", doc));
            this.battery = Integer.parseInt(getTagValue("battery", doc));
            this.batteryRate = Integer.parseInt(getTagValue("batteryRate", doc));
            this.procedural = getTagValue("procedural", doc).equals("true");
            this.seed = Double.parseDouble(getTagValue("seed", doc));
            if(this.seed == 0)
            	this.seed = new Random().nextGaussian() * 255;

            this.printResults = getTagValue("printResults", doc).equals("true");
            this.printStats = getTagValue("printStats", doc).equals("true");
            this.showResults = getTagValue("showGraphs", doc).equals("true");
            this.saveGraphs = getTagValue("saveGraphs", doc).equals("true");
            this.saveResults = getTagValue("saveResults", doc).equals("true");
            this.saveResultsOnlyHour = getTagValue("saveResultsOnlyHour", doc).equals("true");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Inicializo cosas
		this.powerProducer = new LinkedList<PowerProducer>();
		this.powerDemand = new LinkedList<ElectricalLoad>();
		this.servicesList = new LinkedList<Service>();
	}

    /**
    * Method to load services from xml file
    * @param path Path to the services xml file
    * @return List of services as class Service
    */
	public List<Service> loadServices(String path) {
		List<Service> servicesListNew = new LinkedList<Service>();
		
		// Parseo el XML
        File xmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("service");
            
            for (int temp = 0; temp < nList.getLength(); temp++) {
               Node nNode = nList.item(temp);
               if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                  Element eElement = (Element) nNode;
                  
                  String name = getTagValue("name", eElement);
                  double power = Double.parseDouble(getTagValue("power", eElement));

                  if(getTagValue("smart", eElement).equals("false")) {
                	  NodeList operatingRangeNode = eElement.getElementsByTagName("operatingRange");
                	  if (operatingRangeNode.getLength() == 1) {
                		  Element nNodeOR  = (Element) operatingRangeNode.item(0);

                          int type = Integer.parseInt(getTagValue("type", nNodeOR));
                          int min = Integer.parseInt(getTagValue("min", nNodeOR));
                          int max = Integer.parseInt(getTagValue("max", nNodeOR));

                  		// Nombre, Inteligente, Consumo Wh, rando de funcionamiento/expresion lambda/prioridad
                          servicesListNew.add(new Service(name, false, power, new int[]{type,min,max}));
                	  } else {
                		  servicesListNew.add(new Service(name, false, power));
                	  }
                  } else {
                	  int priority = Integer.parseInt(getTagValue("priority", eElement));

                	  int minTime = -1;
                	  int maxTime = -1;
                	  int runTime = 0;
                	  
                	  NodeList smartParametersNode = eElement.getElementsByTagName("smartParameters");
                	  if (smartParametersNode.getLength() == 1) {
                		  Element sPnode  = (Element) smartParametersNode.item(0);
                		
                    	  minTime = Integer.parseInt(getTagValue("minTime", sPnode));
                    	  maxTime = Integer.parseInt(getTagValue("maxTime", sPnode));
                    	  runTime = Integer.parseInt(getTagValue("runTime", sPnode));
                	  }

              		  // Servicios administrables/inteligentes (por defecto estan apagados menos los de ejecucion 24h)
                	  servicesListNew.add(new Service(name, true, power, priority).setSmartParameters(minTime, maxTime, runTime));
                  }
               }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.servicesList = servicesListNew;
        return servicesListNew;
	}
	

    private static String getTagValue(String tag, Element elem) {
        /*NodeList nodeList = elem.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();*/
    	return elem.getElementsByTagName(tag).item(0).getTextContent();
    }
    
    private static String getTagValue(String tag, Document doc) {
        NodeList nodeList = doc.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }


	public String getController() {
		return controller;
	}
	
	public boolean isProcedural() {
		return procedural;
	}
	
	public int getDays() {
		return days;
	}

	public int getBattery() {
		return battery;
	}
	
	public int getBatteryRate() {
		return batteryRate;
	}
	
	public double getSeed() {
		return seed;
	}
	
	public boolean isPrintResults() {
		return printResults;
	}

	public boolean isPrintStats() {
		return printStats;
	}

	public boolean isShowResults() {
		return showResults;
	}

	public boolean isSaveGraphs() {
		return saveGraphs;
	}

	public boolean isSaveResults() {
		return saveResults;
	}

	public boolean isSaveResultsOnlyHour() {
		return saveResultsOnlyHour;
	}

	public List<PowerProducer> getPowerProducer() {
		return powerProducer;
	}

	public List<ElectricalLoad> getPowerDemand() {
		return powerDemand;
	}
	
	public List<Service> getServicesList() {
		return servicesList;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public Grid getGrid() {
		return this.grid;
	}
	
	
    
	
    
}
