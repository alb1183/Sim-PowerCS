# Getting Started 
TODO

## Run simulator


## Configuration of the simulation environment via XML

## Configuration of services via XML

## Procedural data models

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
		
		// Determino la demanda de energia teniendo en cuenta la energia necesitada y la producida
		double demandedEnergy = electricalLoad - powerProduction;
		
		// Diferencio entre descarga o carga
		if(demandedEnergy > 0) {
			// Si la demanada de energia es positiva se deben descargar las baterias o usar el grid
			
			// Si la bateria puede asumir la carga sin descargarse la uso
			if(this.batteryStorage.isFeasible(demandedEnergy)) {
				this.batteryStorage.loadWmperMinute(demandedEnergy);
			} else { // Si no uso el grid
				this.gridStorage.loadWmperMinute(minuteDay, demandedEnergy);
			}
			
		} if(demandedEnergy < 0) {
			// Si la demanada es negativa significa que sobra energia y se puede usar para cargar las baterias o vender al grid
			
			// Si la bateria no está llena la cargo
			if(!this.batteryStorage.isFull())
				this.batteryStorage.loadWmperMinute(demandedEnergy);
			else // Si no, vendo al grid
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
		
		// Si sobra energia enciendo cosas y si no apago
		if(resultSim.getBatteryPercentage() >= 100.0 && resultSim.getPowerProduction()-resultSim.getElectricalLoad() > 0)
			this.turnOnFeasible(minute, resultSim);
		else
			this.turnAllOff(minute, resultSim);
		
		
		return 0.0;
	}
	

	protected void turnOnFeasible(int minute, Result resultSim) {
		int minuteDay = minute % 1440;
		double energyAvailable = resultSim.getPowerProduction()-resultSim.getElectricalLoad();
		
		// Recorro los servicios administrables ordenados por prioridad para tomar decisiones
		for (Service service : this.smartServices) {
			// En caso de poder alimentar el dispositivo
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
