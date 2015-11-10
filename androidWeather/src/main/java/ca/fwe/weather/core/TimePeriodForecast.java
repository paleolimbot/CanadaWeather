package ca.fwe.weather.core;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class TimePeriodForecast extends ForecastItem {

	private double high ;
	private double low ;
	private double pop ;
	private String condition ;

	private List<String> extraInfoLabels ;
	private List<String> extraInfoValues ;

	public TimePeriodForecast(Forecast forecast) {
		super(forecast);
		extraInfoLabels = new ArrayList<>() ;
		extraInfoValues = new ArrayList<>() ;
		high = Double.NaN ;
		low = Double.NaN ;
		pop = 0 ;
	}

	public void setHigh(double value, Units.Unit unit) {
		high = Units.toSI(value, unit) ;
	}

	public void setLow(double value, Units.Unit unit) {
		low = Units.toSI(value, unit) ;
	}

	public void setPop(double value) {
		pop = value ;
	}

	public void setCondition(String value) {
		condition = value ;
	}

	public void addExtraInfo(String label, String value) {
		extraInfoLabels.add(label) ;
		extraInfoValues.add(value) ;
	}

	public List<String[]> getExtraInfo() {
		List<String[]> out = new ArrayList<>() ;
		for(int i=0; i<extraInfoLabels.size(); i++) {
			out.add(new String[] {extraInfoLabels.get(i), extraInfoValues.get(i)}) ;
		}
		return out ;
	}

	public String getHigh() {
		if(!Double.isNaN(high)) {
			NumberFormat format = forecast.getNumberFormat(0) ;
			Units.UnitSet us = forecast.getUnitSet() ;
			return format.format(Units.fromSI(high, us.getUnit(Units.Categories.TEMPERATURE))) ;
		} else {
			return null ;
		}
	}

	public String getLow() {
		if(!Double.isNaN(low)) {
			NumberFormat format = forecast.getNumberFormat(0) ;
			Units.UnitSet us = forecast.getUnitSet() ;
			return format.format(Units.fromSI(low, us.getUnit(Units.Categories.TEMPERATURE))) ;
		} else {
			return null ;
		}
	}

	public String getPop() {
		if(pop > 0) {
			NumberFormat format = forecast.getNumberFormat(0) ;
			return format.format(pop) + "%" ;
		} else {
			return null ;
		}
	}

	public String getCondition() {
		return condition ;
	}

}
