package com.um.simEnergy.LoadPower;

import java.util.List;

import com.um.simEnergy.Service.Service;

public class ServicesActiveLoad extends ElectricalLoad {
	private List<Service> servicesList;

	public ServicesActiveLoad(List<Service> servicesList) {
		this.servicesList = servicesList;
	}
	
	public void addService(Service service) {
		servicesList.add(service);
	}

	
	public double getLoad(int m) {
		double loadServices = 0.0;
		
		for (Service service : servicesList) {
			loadServices += service.getLoad(m);
		}
		
		return loadServices;
	}

}
