package com.um.simEnergy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.knowm.xchart.AnnotationLine;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.um.simEnergy.EnergyStorage.Battery;
import com.um.simEnergy.EnergyStorage.Grid;
import com.um.simEnergy.Service.Service;

public class SimulationResults {
	private int totalDays;
	private int totalMinutes;
	
	// Servicios definidos
	private List<Service> servicesList;
	
	private Battery batteryStorage;
	private Grid gridStorage;
	
	private Result[] resultados;
	
	private String outputFolder = null;

	public SimulationResults(int days, List<Service> servicesList, Battery batteryStorage, Grid gridStorage) {
		this.totalDays = days;
		this.totalMinutes = days * 24 * 60;
		
		this.resultados = new Result[this.totalMinutes];
		
		this.servicesList = servicesList;
		
		this.batteryStorage = batteryStorage;
		this.gridStorage = gridStorage;
		
		String date = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(new Date());
		this.outputFolder = "./data/Outputs/"+date+"/";
	}
	
	public Result addResult(int minute, double powerProduction, double electricalLoad) {		
		this.resultados[minute] = new Result(minute, powerProduction, electricalLoad, servicesList, batteryStorage, gridStorage);
		return this.resultados[minute];
	}

	public void printResults() {
		System.out.println("Simulacion de " + this.totalDays + " dias (" + this.totalMinutes + " minutos)");
		System.out.println("Time, Minute, PowerProduction, ElectricalLoad, BatteryLevel");
		
		for(int i = 0; i < this.totalMinutes; i++) {
			System.out.println(this.resultados[i]);
		}
	}

	public void saveResults() {
		// Creo el directorio si no existe
		File folder = new File(this.outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		this.saveResultsCSV(false);
	}
	
	public void saveResultsOnlyHour() {
		this.saveResultsCSV(true);
	}
	
	
	private void saveResultsCSV(boolean hour) {
		String fichero = hour ? "output_h.csv" : "output.csv";
		
	    try {
	        FileWriter outputFile = new FileWriter(this.outputFolder+fichero);
	        
			System.out.println("Simulacion de " + this.totalDays + " dias (" + this.totalMinutes + " minutos)");
			  outputFile.write("Time, Minute, PowerProduction, ElectricalLoad, BatteryUsage, BatteryPercentage, BatteryUnderCapacityLoss, GlobalReward, ServicesID, ServiceName, ServicePriority, ServiceSmart, ServiceState, ServicePowerConsumption, ServiceRunningTime, ServiceReward\n");
			
			for(int i = 0; i < this.totalMinutes; i++) {
				if(!hour || i%60 == 0)
					outputFile.write(this.resultados[i].toString()/* + "\n"*/);
			}
	        
	        outputFile.close();
	        
	        System.out.println("Salida grabada con exito.");
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	}
	

	public void showResults(boolean saveGraph) {
		double[] xData = new double[totalMinutes];
		double[] yDataPowerProduction = new double[totalMinutes];
		double[] yDataElectricalLoad = new double[totalMinutes];
		double[] yDataEnergyUsage = new double[totalMinutes];
		
		double[] yDataBatteryUsage = new double[totalMinutes];
		double[] yDataBatteryPercentage = new double[totalMinutes];
		
		double[] yDataGridUsage = new double[totalMinutes];
		double[] yDataGridBuyMoney = new double[totalMinutes];
		double[] yDataGridSellMoney = new double[totalMinutes];
		double[] yDataGridBuyPrice = new double[totalMinutes];
		double[] yDataGridSellPrice = new double[totalMinutes];

		int servicesNum = servicesList.size();
		double[][] yDataServicesUsage = new double[servicesNum][totalMinutes];
		
		double[] yDataTest = new double[totalMinutes];

		for(int i = 0; i < this.totalMinutes; i++) {
			xData[i] = i; // Minutos
			//xData[i] = i/60.0; // Horas
			
			// Paso todos los Wm a Wh para facilitar la comprension
			yDataPowerProduction[i] = this.resultados[i].getPowerProduction() * 60.0;
			yDataElectricalLoad[i] = this.resultados[i].getElectricalLoad() * 60.0;
			
			yDataEnergyUsage[i] = yDataElectricalLoad[i] - yDataPowerProduction[i];

			yDataBatteryUsage[i] = this.resultados[i].getBatteryUsage() * 60;
			yDataBatteryPercentage[i] = this.resultados[i].getBatteryPercentage() * 10; // Per mille

			yDataGridUsage[i] = this.resultados[i].getGridUsage() * 60;
			yDataGridBuyMoney[i] = this.resultados[i].getBuyMoney();
			yDataGridSellMoney[i] = this.resultados[i].getSellMoney();
			yDataGridBuyPrice[i] = this.resultados[i].getBuyPrice();
			yDataGridSellPrice[i] = this.resultados[i].getSellPrice();
			
			for(int j = 0; j < servicesNum; j++) {
				yDataServicesUsage[j][i] = (this.resultados[i].getServiceState()[j]) ? servicesList.get(j).getPowerConsumption() : 0;
			}
			

			yDataTest[i] = yDataBatteryUsage[i] + yDataElectricalLoad[i];
		}
		
		// Create Charts
		XYChart chartEnergy = new XYChartBuilder().width(1920).height(1080).title("Energy Summary").xAxisTitle("X").yAxisTitle("Y").build();
		chartEnergy.addSeries("Electrical Load", xData, yDataElectricalLoad).setMarker(SeriesMarkers.NONE);
		chartEnergy.addSeries("Electrical Production", xData, yDataPowerProduction).setMarker(SeriesMarkers.NONE);
		chartEnergy.addSeries("Electrical Demand", xData, yDataEnergyUsage).setMarker(SeriesMarkers.NONE);
		chartEnergy.addSeries("Battery Level (‰)", xData, yDataBatteryPercentage).setMarker(SeriesMarkers.NONE);
		chartEnergy.addSeries("Battery Usage", xData, yDataBatteryUsage).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.DASH_DASH);
		chartEnergy.addSeries("Grid Usage", xData, yDataGridUsage).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.DASH_DASH);
		//chartEnergy.addSeries("Test (Wh)", yDataTest).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.DASH_DOT);
		chartEnergy.getStyler().setZoomEnabled(true);
		// Añado la barra de vertical de cada dia
		for(int i = 1; i < this.totalDays; i++)
			chartEnergy.addAnnotation(new AnnotationLine(1440*i, true, false));
	    // Muestro la grafica
		new SwingWrapper(chartEnergy).displayChart();
		
		// Grid Costs
		XYChart chartGridCosts = new XYChartBuilder().width(1920).height(1080).title("Grid Cost").xAxisTitle("X").yAxisTitle("Y").build();
		chartGridCosts.addSeries("Buy Money", xData, yDataGridBuyMoney).setMarker(SeriesMarkers.NONE);
		chartGridCosts.addSeries("Sell Money", xData, yDataGridSellMoney).setMarker(SeriesMarkers.NONE);
		chartGridCosts.getStyler().setZoomEnabled(true);
		// Añado la barra de vertical de cada dia
		for(int i = 1; i < this.totalDays; i++)
			chartGridCosts.addAnnotation(new AnnotationLine(1440*i, true, false));
	    // Muestro la grafica
		new SwingWrapper(chartGridCosts).displayChart();

		// Grid Prices
		XYChart chartGridPrice = new XYChartBuilder().width(1920).height(1080).title("Grid Price").xAxisTitle("X").yAxisTitle("Y").build();
		chartGridPrice.addSeries("Buy Price", xData, yDataGridBuyPrice).setMarker(SeriesMarkers.NONE);
		chartGridPrice.addSeries("Sell Price", xData, yDataGridSellPrice).setMarker(SeriesMarkers.NONE);
		chartGridPrice.getStyler().setZoomEnabled(true);
		chartGridPrice.getStyler().setYAxisMin(0, 0.0);
		// Añado la barra de vertical de cada dia
		for(int i = 1; i < this.totalDays; i++)
			chartGridPrice.addAnnotation(new AnnotationLine(1440*i, true, false));
	    // Muestro la grafica
		new SwingWrapper(chartGridPrice).displayChart();
		
		
		// Services chart
		//XYChart chartServices = QuickChart.getChart("Services", "X", "Y", servicesList.get(0).getName(), xData, yDataServicesUsage[0]);
		XYChart chartServices = new XYChartBuilder().width(1920).height(1080).title("Services").xAxisTitle("X").yAxisTitle("Y").build();
		for(int j = 0; j < servicesNum; j++)
			if(servicesList.get(j).isSmart())
				chartServices.addSeries(servicesList.get(j).getName(), xData, yDataServicesUsage[j]).setMarker(SeriesMarkers.NONE).setLineStyle(servicesList.get(j).isDynamic() ? SeriesLines.DASH_DASH : SeriesLines.SOLID);

		chartServices.getStyler().setZoomEnabled(true);
		// Añado la barra de vertical de cada dia
		for(int i = 1; i < this.totalDays; i++)
			chartServices.addAnnotation(new AnnotationLine(1440*i, true, false));
	    // Muestro la grafica
		new SwingWrapper(chartServices).displayChart();
		
		// Services Reward chart
		/*//XYChart chartServicesReward = QuickChart.getChart("Reward Services", "X", "Y", servicesList.get(0).getName(), xData, yDataServicesReward[0]);
		XYChart chartServicesReward = new XYChartBuilder().width(1920).height(1080).title("Services Rewards").xAxisTitle("X").yAxisTitle("Y").build();
		//chartServicesReward.addSeries("Global", xData, yDataLastGlobalReward).setMarker(SeriesMarkers.NONE);
		for(int j = 0; j < servicesNum; j++)
			if(servicesList.get(j).isSmart())
				chartServicesReward.addSeries(servicesList.get(j).getName(), xData, yDataServicesReward[j]).setMarker(SeriesMarkers.NONE);

		chartServicesReward.getStyler().setZoomEnabled(true);
		// Añado la barra de vertical de cada dia
		for(int i = 1; i < this.totalDays; i++)
			chartServicesReward.addAnnotation(new AnnotationLine(1440*i, true, false));

	    // Muestro la grafica
		new SwingWrapper(chartServicesReward).displayChart();*/
		
		if(saveGraph) {			
			try {
				BitmapEncoder.saveBitmapWithDPI(chartEnergy, this.outputFolder+"chartEnergy.png", BitmapFormat.PNG, 100);
				BitmapEncoder.saveBitmapWithDPI(chartServices, this.outputFolder+"chartServices.png", BitmapFormat.PNG, 100);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public void printStats() {
		//double batteryunderCapacityLossSumTmp = 0;
		int lastMinute = this.totalMinutes-1;
		
		int servicesNum = servicesList.size();
		double[] servicesUsage = new double[servicesNum];
		
		for(int i = 0; i < this.totalMinutes; i++) {
			//batteryunderCapacityLossSumTmp += this.resultados[i].getBatteryUnderCapacityLoss();
			
			for(int j = 0; j < servicesNum; j++) {
				servicesUsage[j] += (this.resultados[i].getServiceState()[j]) ? 1 : 0;
			}
		}
		
		double buyWm = this.resultados[lastMinute].getBuyWm();
		double buyMoney = this.resultados[lastMinute].getBuyMoney();
		double sellWm = this.resultados[lastMinute].getSellWm();
		double sellMoney = this.resultados[lastMinute].getSellMoney();

		System.out.println("Buy energy: " + String.format("%.2f", buyWm) + " (" + String.format("%.2f", buyMoney) + " coins)");
		System.out.println("Sell energy: " + String.format("%.2f", sellWm) + " (" + String.format("%.2f", sellMoney) + " coins)");
		System.out.println("Final energy: " + String.format("%.2f", sellWm-buyWm) + " (" + String.format("%.2f", sellMoney-buyMoney) + " coins)\n");
		
		for(int j = 0; j < servicesNum; j++)
			if(servicesList.get(j).isSmart()) {
				double AvgHoursPerDay = servicesUsage[j]/totalDays /*/ 60 */;
				double definedHoursPerDay = servicesList.get(j).getDefinedRuntime();				
				System.out.println("Service " + servicesList.get(j).getName() + " ("+servicesList.get(j).getPriority()+"): " + AvgHoursPerDay + " (" + String.format("%.1f%%",(AvgHoursPerDay/definedHoursPerDay)*100) + ")");
			}
		
		
	}
}
