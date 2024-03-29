# Table of contents
- [Getting Started](#getting-started)
  * [Run simulator](#run-simulator)
  * [Configuration of the simulation environment via XML](#configuration-of-the-simulation-environment-via-xml)
  * [Configuration of services via XML](#configuration-of-services-via-xml)
  * [Procedural data models](#procedural-data-models)
  * [Energy management algorithm](#energy-management-algorithm)
  * [Service control algorithm](#service-control-algorithm)
  * [Extension points](#extension-points)
  
# Getting Started 
In the following subsections we will explain how to launch the simulator and how to configure/extend each component.


## Run simulator
Import the project into Eclipse and run the main class App.java.

The App.java sample code shows the required components to be instantiated (configuration object and simulation object), along with the necessary configurations.


```Java
Config config = new Config("./data/config.xml");
...
config.setGrid(new BasicGrid("./data/Models/EnergyPrices.csv", seed));
...
List<PowerProducer> powerProducer = config.getPowerProducer();
SimulatedPhotovoltaicModule PVi = new SimulatedPhotovoltaicModule(3.2, seed);
PVi.readFrom1hCSV("./data/Models/SolarRadiationSumary.csv");
powerProducer.add(PVi);
...
List<ElectricalLoad> powerDemand = config.getPowerDemand();
List<Service> servicesList = config.loadServices("./data/services.xml");
ServicesActiveLoad AL = new ServicesActiveLoad(servicesList);
powerDemand.add(AL); // Active load (services)
...
...
// Simulation		
Simulation sm = new Simulation(config);
sm.run();
sm.results();
```


## Configuration of the simulation environment via XML
The simulation environment is configured by using the XML file config.xml, which indicates which energy and service controller will be used, the capacity of the batteries (Wh), the discharge rate of the batteries (W), the number of days to be simulated and whether procedural data generation is activated (including the seed).

In addition, everything related to the simulator log is configured, e.g. generating graphs, saving results in CSV files, displaying statistics in the console, and so on.

Example configuration of the simulator:
```XML
<Config>
	<controller>Method</controller> <!-- implemented algorithm -->
	<procedural>true</procedural> <!-- boolean, false for repeatability  -->
	<seed>0</seed> <!-- 0: random -->
	<days>7</days> <!-- num days -->
	<battery>4000</battery> <!-- capacity in Wh -->
	<batteryRate>2500</batteryRate> <!-- rate in W -->
	
	<printResults>false</printResults> <!-- print detailed results on the console -->
	<printStats>true</printStats> <!-- print summarized statistics on the console -->
	<showGraphs>true</showGraphs> <!-- show graphs at the end -->
	<saveGraphs>false</saveGraphs> <!-- save graphs png -->
	<saveResults>false</saveResults> <!-- save csv -->
	<saveResultsOnlyHour>false</saveResultsOnlyHour> <!-- save csv summarized by hours -->
</Config>
```

## Configuration of services via XML
Services are also configured using a XML file services.xml, in which all services are listed together with their properties.

Each service is defined by specifying a name, its power consumption (W), its priority, whether it is a smart service (it can be dynamically controlled by the service controller) and the operating range rule.

Operating range rules are used to limit some services to a specific time slot and maximum on-time, for instance, fence lights should only be switched on at night (between 8 pm and 8 am), or the pool pump should only be switched on during the day but for a maximum of three hours, these types of rules allow some services to be switched on at different hours giving the possibility to reduce their energy impact.


XML structure of each service:
```XML
<Services>
	<list>
		....
		<service>
			<name></name>			<!-- string name -->
			<power></power>			<!-- power consumption in W -->
			<smart></smart>			<!-- smart management -->
			<operatingRange>		<!-- time rule -->
				<type></type>		<!-- 1: between, 2: outside -->
				<min></min>			<!-- time of the day in minutes -->
				<max></max>			<!-- time of the day in minutes -->
			</operatingRange>
			<priority></priority>	<!-- service priority, only for smarts -->
			<smartParameters>		<!-- only for smart services, optional -->
				<minTime></minTime>	<!-- min time of the day in minutes to start the service -->
				<maxTime></maxTime>	<!-- max time of the day in minutes to start the service -->
				<runTime></runTime>	<!-- max. switch-on time; 0: unlimited, otherwise minutes -->
			</smartParameters>		<!--  -->
		</service>
		....
	</list>
</Services>
```

Table of configured services in the demo code:
| Service            | Smart | Priority | Load   | Rule              |
|--------------------|-------|----------|--------|-------------------|
| Fence lights       | No    | -        | 15 W   | 8pm-8am           |
| Facade lights      | No    | -        | 10 W   | 8pm-8am           |
| Fridge             | No    | -        | 120 W  | All time          |
| CCTV DVR           | Yes   | 10       | 20 W   | All time          |
| Internet           | Yes   | 8        | 40 W   | All time          |
| Pool Pump          | Yes   | 4        | 600 W  | 9am-5pm (max 3h)  |
| Streaming Services | Yes   | 2        | 30 W   | All time          |
| Fountain           | Yes   | 1        | 35 Wh  | 9am-3pm           |


![services](images/services.png)

Example of a configuration file for the previous services:
```XML
<Services>
	<list>
		<service>
			<name>Fence lights</name>
			<power>15.0</power>
			<smart>false</smart>
			<operatingRange>
				<type>2</type>
				<min>480</min>
				<max>1200</max>
			</operatingRange>
		</service>
		<service>
			<name>Facade lights</name>
			<power>10.0</power>
			<smart>false</smart>
			<operatingRange>
				<type>2</type>
				<min>480</min>
				<max>1200</max>
			</operatingRange>
		</service>
		<service>
			<name>Fridge</name>
			<power>120.0</power>
			<smart>false</smart>
		</service>
		
		<service>
			<name>Pool Pump</name>
			<power>600.0</power>
			<smart>true</smart>
			<priority>4</priority>
			<smartParameters>
				<minTime>540</minTime>
				<maxTime>1020</maxTime>
				<runTime>180</runTime>
			</smartParameters>
		</service>
		<service>
			<name>CCTV DVR</name>
			<power>20.0</power>
			<smart>true</smart>
			<priority>10</priority>
		</service>
		<service>
			<name>Internet</name>
			<power>40.0</power>
			<smart>true</smart>
			<priority>8</priority>
		</service>
		<service>
			<name>StreamServices</name>
			<power>30</power>
			<smart>true</smart>
			<priority>2</priority>
		</service>
		<service>
			<name>Fountain</name>
			<power>35</power>
			<smart>true</smart>
			<priority>1</priority>
			<smartParameters>
				<minTime>540</minTime>
				<maxTime>900</maxTime>
				<runTime>0</runTime>
			</smartParameters>
		</service>
	</list>
</Services>
```

## Procedural data models
All the information used by the simulator to determine the solar production and passive energy consumption of the system is based on statistical models modelled using data from a real solar infrastructure.

As can be seen in the image, each hour the mean value of solar radiation and the upper and lower bound of the 95\% confidence interval are indicated.
The confidence interval is used to procedurally generate each day different but correlated values by using Perlin noise, thus enabling a dynamic and realistic behaviour of the simulations.


![energyProductionModel](images/energyProductionModel.png)

Example of a CSV file with solar radiation statistical data (the first graph in the image above plot this data)

```CSV
hour,numberOfSamples,mean,standardDeviation,StandardErrorMean,CIlower,CIupper
0,7,0,0,0,0,0
1,7,0,0,0,0,0
2,7,0,0,0,0,0
3,7,0,0,0,0,0
4,7,0,0,0,0,0
5,7,0,0,0,0,0
6,7,0,0,0,0,0
7,7,4.49142857142857,2.4819846129756,1.0132659752049,2.01205604833774,6.9708010945194
8,7,53.7557142857143,17.1367819393869,6.99606193080669,36.6369674358793,70.8744611355493
9,7,225.165714285714,62.5169270030506,25.5224285740495,162.714581337871,287.616847233557
10,7,367.952857142857,20.2389160729898,8.26250288763968,347.735240906972,388.170473378742
11,7,619.592857142857,120.864741028182,49.3428239021129,498.855316567818,740.330397717896
12,7,611.468571428571,41.8264088938123,17.0755599271414,569.686181477913,653.25096137923
13,7,663.495714285714,81.9287560134094,33.4472745823053,581.653181721771,745.338246849657
14,7,557.572857142857,204.873464258168,83.6392415448065,352.915005786094,762.230708499621
15,7,475.902857142857,252.938752778499,103.261813413884,223.230302129704,728.575412156011
16,7,351.765714285714,131.000810666339,53.4808570039132,220.90277147345,482.628657097979
17,7,192.311428571429,79.8140061702981,32.5839315740964,112.581420245875,272.041436896982
18,7,33.4114285714286,15.0368585244962,6.13877178657254,18.3903951353898,48.4324620074673
19,7,0,0,0,0,0
20,7,0,0,0,0,0
21,7,0,0,0,0,0
22,7,0,0,0,0,0
23,7,0,0,0,0,0
```


## Energy management algorithm
This is the main point of extension of the simulator with respect to energy management.

At the beginning of each minute of the simulator, the list of energy generators and energy producers is browsed to determine the energy production and demand at that exact minute.

The abstract method "run" is then invoked and this information is passed to it to determine the energy state (lack or surplus of energy) and make the appropriate energy decisions.

```Java
public abstract class EnergyController {
	public abstract void run(int minute, double powerProduction, double electricalLoad);
```

In the simulator we provide a basic but functional example of an energy controller. Our example algorithm first determines whether energy is needed in that iteration because demand exceeds production or whether we otherwise have a surplus of production.

In the case of needing energy, it checks if the batteries are not discharged to use them as an energy source, otherwise it buys the energy from the grid (incurring a cost that is accumulated).

On the other hand, if it has a surplus production it first checks if the batteries are not charged to use the excess energy to charge them, if not it sells the surplus energy to the grid (incurring a monetary benefit that is recorded).

As can be seen, the example energy algorithm is relatively simple but effective in many scenarios, yet it can easily be improved to take into account patterns in power generation, energy consumption or energy prices in the grid.

This is the code needed to implement this behaviour:
```Java
public class BasicEnergyController extends EnergyController {
	
	public BasicEnergyController(Battery batteryStorage, Grid gridStorage) {
		super(batteryStorage, gridStorage);
	}

	public void run(int minute, double powerProduction, double electricalLoad) {
		int minuteDay = minute % 1440;
		
		// Determine the energy demand taking into account the energy needed and the energy produced.
		double demandedEnergy = electricalLoad - powerProduction;
		
		// Difference between discharge or charge
		if(demandedEnergy > 0) {
			// If the energy demand is positive, the batteries should be discharged or the grid should be used.
			
			// If the battery can assume the load without being discharged, I use it.
			if(this.batteryStorage.isFeasible(demandedEnergy)) {
				this.batteryStorage.loadWmperMinute(demandedEnergy);
			} else { // Otherwise, I buy from the grid
				this.gridStorage.loadWmperMinute(minuteDay, demandedEnergy);
			}
			
		} if(demandedEnergy < 0) {
			// If the demand is negative it means that there is energy left over and it can be used to charge the batteries or sold to the grid.
			
			// If the battery is not full I charge it
			if(!this.batteryStorage.isFull())
				this.batteryStorage.loadWmperMinute(demandedEnergy);
			else // Otherwise, I sell to the grid
				this.gridStorage.sellWmperMinute(minuteDay, demandedEnergy);
				
		}
		
	}
}
```


## Service control algorithm

As in the previous section, the service controller is defined as an abstract class with an extension point method, which is called at the end of each minute of the simulation to make decisions about services according to the output of that simulated minute.

```Java
public abstract class ServiceController {
.....
	abstract protected double decision(int minute, Result resultSim);
```

As we have already mentioned, after determining the energy actions and executing them, the controller of the services (energy consumers) is called to decide based on the current energy status what to do with each service (switch them on or off). 
Our simulator has three service control algorithms, the most basic of which is detailed in the code below.
This algorithm only takes into account manageable services, there will be more services that produce energy consumption but that have been defined as not controllable and therefore cannot be controlled.
The behaviour of this algorithm is very simple and serves as a baseline of worst-case performance with respect to any possible smarter implementation.
Essentially, the controller is limited to determining whether a service is within the timeslot where it can be switched on and has not yet exceeded its maximum switch-on time to keep it on or otherwise turn off the service.


```Java
public class BasicController extends ServiceController {
	public double decision(int minute, Result resultSim) {
		int minuteDay = minute % 1440;
		
		// If there is energy left over I turn services on and if not I turn services off.
		if(resultSim.getBatteryPercentage() >= 100.0 && resultSim.getPowerProduction()-resultSim.getElectricalLoad() > 0)
			this.turnOnFeasible(minute, resultSim);
		else
			this.turnAllOff(minute, resultSim);
		
		
		return 0.0;
	}
	

	protected void turnOnFeasible(int minute, Result resultSim) {
		int minuteDay = minute % 1440;
		double energyAvailable = resultSim.getPowerProduction()-resultSim.getElectricalLoad();
		
		// Browse through the manageable services sorted by priority in order to make decisions.
		for (Service service : this.smartServices) {
			// If the device can be powered
			double serviceLoad = service.getLoad(minute);
			if(energyAvailable > serviceLoad && service.isWorkingTime(minuteDay) && service.checkRunTime(minute)) {
				service.turnOn();
				energyAvailable -= serviceLoad;
			} else {
				service.turnOff();
			}
		}
	}
	
	protected void turnAllOn(int minute, Result resultSim) {
		for (Service service : this.smartServices) {
			service.turnOn();
		}
	}
	
	protected void turnAllOff(int minute, Result resultSim) {
		for (Service service : this.smartServices) {
			service.turnOff();
		}
	}
```

## Extension points
In addition to the two main extension points there are several abstract classes defined for other components that can be modified.
For example the battery class, which is implemented as a simple battery that records its charge as a number but does not take into account possible degradations of its performance over time (charge cycles), nor any other more realistic behaviour.
Similarly, the grid and solar panels can easily be improved using the abstract classes and our implementations as example code. 
