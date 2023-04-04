# Getting Started 
TODO

## Run simulator
Import the project into Eclipse and run the main class App.java.



## Configuration of the simulation environment via XML
The simulation environment is configured by using the XML file config.xml, which indicates which energy and service controller will be used, the capacity of the batteries (kW), the number of days to be simulated and whether procedural data generation is activated (including the seed).

In addition, everything related to the simulator log is configured, e.g. generating graphs, saving results in CSV files, displaying statistics in the console, and so on.

...

## Configuration of services via XML
Services are also configured using a XML file services.xml, in which all services are listed together with their properties.

Each service is defined by specifying a name, its power consumption (Wh), its priority, whether it is a smart service (it can be dynamically controlled by the service controller) and the operating range rule.

Operating range rules are used to limit some services to a specific time slot and maximum on-time, for instance, fence lights should only be switched on at night (between 8 pm and 8 am), or the pool pump should only be switched on during the day but for a maximum of three hours, these types of rules allow some services to be switched on at different hours giving the possibility to reduce their energy impact.

...

## Procedural data models
All the information used by the simulator to determine the solar production and passive energy consumption of the system is based on statistical models modelled using data from a real solar infrastructure.

...
where for each hour the mean value of solar radiation and the upper and lower bound of the 95\% confidence interval are indicated.
The confidence interval is used to procedurally generate each day different but correlated values by using Perlin noise, thus enabling a dynamic and realistic behaviour of the simulations.

## Energy management algorithm

```Java
public abstract class EnergyController {
	public abstract void run(int minute, double powerProduction, double electricalLoad);
```

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



```Java
public abstract class ServiceController {
.....
	abstract protected double decision(int minute, Result resultSim);
```

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
