package ca.fwe.weather.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import ca.fwe.weather.R;

public class CurrentConditions extends ForecastItem {
	
	private static final String TAG = "CurrentConditions" ;
	private static final int DEFAULT_PRECISION = 0 ;
	
	public enum Fields {STATION, DATE, CONDITION, TEMP, WINDCHILL, HUMIDEX, FEELSLIKE, 
						WINDSUMMARY, WINDSPEED, WINDGUST, 
						WINDDIRECTION, PRESSURE, PRESSURETREND, DEWPOINT, VISIBILITY, RELHUMIDITY,
						SUNRISE, SUNSET}
	
	private static Map<Fields, Units.Categories> categories = new HashMap<>() ;
	private static Map<Fields, Integer> labelIds = new HashMap<>() ;

	static {
		categories.put(Fields.TEMP, Units.Categories.TEMPERATURE) ;
		categories.put(Fields.WINDCHILL, Units.Categories.TEMPERATURE) ;
		categories.put(Fields.HUMIDEX, Units.Categories.TEMPERATURE) ;
		categories.put(Fields.FEELSLIKE, Units.Categories.TEMPERATURE) ;
		categories.put(Fields.WINDSPEED, Units.Categories.WIND_SPEED) ;
		categories.put(Fields.WINDGUST, Units.Categories.WIND_SPEED) ;
		categories.put(Fields.PRESSURE, Units.Categories.PRESSURE) ;
		categories.put(Fields.DEWPOINT, Units.Categories.TEMPERATURE) ;
		categories.put(Fields.VISIBILITY, Units.Categories.VISIBILITY) ;
	}
	
	static {
		labelIds.put(Fields.STATION, R.string.cc_field_station) ;
		labelIds.put(Fields.DATE, R.string.cc_field_date) ;
		labelIds.put(Fields.CONDITION,  R.string.cc_field_condition) ;
		labelIds.put(Fields.TEMP, R.string.cc_field_temp) ;
		labelIds.put(Fields.WINDCHILL, R.string.cc_field_windchill) ;
		labelIds.put(Fields.HUMIDEX, R.string.cc_field_humidex) ;
		labelIds.put(Fields.FEELSLIKE, R.string.cc_field_feelslike) ;
		labelIds.put(Fields.WINDSUMMARY, R.string.cc_field_windsummary) ;
		labelIds.put(Fields.WINDSPEED, R.string.cc_field_windspeed) ;
		labelIds.put(Fields.WINDGUST, R.string.cc_field_windgust) ;
		labelIds.put(Fields.WINDDIRECTION, R.string.cc_field_winddirection) ;
		labelIds.put(Fields.PRESSURE, R.string.cc_field_pressure) ;
		labelIds.put(Fields.PRESSURETREND, R.string.cc_field_pressuretrend) ;
		labelIds.put(Fields.DEWPOINT, R.string.cc_field_dewpoint) ;
		labelIds.put(Fields.VISIBILITY, R.string.cc_field_visibility) ;
		labelIds.put(Fields.RELHUMIDITY, R.string.cc_field_relhumidity) ;
        labelIds.put(Fields.SUNRISE, R.string.cc_field_sunrise);
        labelIds.put(Fields.SUNSET, R.string.cc_field_sunset);
	}
	
	private Map<Fields, String> values = new HashMap<>() ;
	private Map<Fields, Units.Unit> units = new HashMap<>() ;
	private List<Fields> fields = new ArrayList<>() ;
	private Map<Fields, Integer> precisionValues = new HashMap<>() ;
	private Date observedDate ;
	
	public CurrentConditions(Forecast forecast) {
		super(forecast) ;
		this.setDescription(getString(R.string.fi_click_for_more)) ;
		this.setTitle(getString(R.string.cc_summary_unknown)) ;
	}
	
	public String getField(Fields field) {
		if (values.containsKey(field))
			return values.get(field) ;
		return null ;
	}
	
	public double getFieldDouble(Fields field) throws NumberFormatException {
		String value = this.getField(field) ;
		if(value != null) {
			return Double.valueOf(value) ;
		} else {
			return Double.NaN ;
		}
	}
	
	public double getFieldDouble(Fields field, Units.Unit unit) throws NumberFormatException {
		String value = this.getField(field) ;
		if(value != null) {
			double baseval = Double.valueOf(value) ;
			Units.Unit baseunit = this.getUnit(field) ;
			if(baseunit != null && unit != null && !Double.isNaN(baseval))
				return Units.convert(baseval, this.getUnit(field), unit) ;
			else
				return Double.NaN ;
		} else {
			return Double.NaN ;
		}
	}
	
	public String getFieldSummary(Fields field) {
		if(categories.containsKey(field)) {
			Units.Unit unit = forecast.getUnitSet().getUnit(categories.get(field)) ;
			Units.Unit putUnit = units.get(field) ;
			if(unit != null && putUnit != null) {
				try {
					double value = this.getFieldDouble(field, unit) ;
					if(!Double.isNaN(value)) {
						String strValue = forecast.getNumberFormat(getPrecision(field)).format(value) ;
                        strValue = strValue.replaceAll( "^-(?=0(.0*)?$)", "");
						String unitLabel = Units.getLabel(unit) ;
						return strValue + " " + unitLabel ;
					} else {
						return null ;
					}
				} catch(NumberFormatException e) {
					Log.e(TAG, "number format error while getting field summary for " + field, e) ;
					return null ;
				}
				
			} else {
				return this.getField(field) ;
			}
		} else {
			return this.getField(field) ;
		}
	}
	
	public void setField(Fields field, String value) {
		if(value == null && values.containsKey(field)) {
			values.remove(field) ;
			fields.remove(field) ;
		} else {
			values.put(field, value) ;
			fields.add(field) ;
		}
	}
	
	public void setUnit(Fields field, Units.Unit unit) {
		units.put(field, unit) ;
	}
	
	public Units.Unit getUnit(Fields field) {
		if(units.containsKey(field)) {
			return units.get(field) ;
		} else {
			return null ;
		}
	}
	
	public void setPrecision(Fields field, int precision) {
		precisionValues.put(field, precision) ;
	}
	
	public int getPrecision(Fields field) {
		if(precisionValues.containsKey(field)) {
			return precisionValues.get(field) ;
		} else {
			return DEFAULT_PRECISION ;
		}
	}
	
	public void setField(Fields field, String value, Units.Unit unit, int precision) {
		this.setField(field, value) ;
		this.setUnit(field, unit) ;
		this.setPrecision(field, precision);
		
		if(value != null) {
			if(field == Fields.TEMP) {
				String feelslike = this.getFieldSummary(Fields.FEELSLIKE) ;
				String tempsummary = this.getFieldSummary(Fields.TEMP) ;
				if(feelslike != null) {
					this.setTitle(String.format(getString(R.string.cc_summary_feelslike),
							tempsummary, feelslike)) ;
				} else {
					this.setTitle(String.format(getString(R.string.cc_summary), tempsummary)) ;
				}
			} else if(field == Fields.WINDCHILL || field == Fields.HUMIDEX) {
				this.setField(Fields.FEELSLIKE, value, unit, precision) ;
			} else if(field == Fields.FEELSLIKE) {
				String feelslike = this.getFieldSummary(Fields.FEELSLIKE) ;
				String tempsummary = this.getFieldSummary(Fields.TEMP) ;
				if(tempsummary != null) {
					this.setTitle(String.format(getString(R.string.cc_summary_feelslike),
							tempsummary, feelslike)) ;
				}
			}
		}
	}
	
	public List<Fields> getFields() {
		//this list will be in the order things were added to it, which should be about correct.
		return fields ;
	}
	
	public String getHtmlSummary() {
		String out = "" ;
		
		String nl = "" ;
		for(Fields f: getFields()) {
			String value = this.getFieldSummary(f) ;
			if(value != null) {
				out += String.format("%s<b>%s:</b> %s", nl, getLabel(f), value) ;
				nl = "<br />" ;
			}
		}
		
		return out ;
	}
	
	public String getLabel(Fields field) {
		return forecast.getContext().getString(labelIds.get(field)) ;
	}

	public Date getObservedDate() {
		return observedDate;
	}

	public void setObservedDate(Date date) {
		this.observedDate = date;
		if(date != null) {
			this.setField(Fields.DATE, forecast.getDateFormat().format(date));
			this.setDescription(String.format(getString(R.string.cc_description_format), 
					forecast.getDateFormat().format(date))) ;
		} else {
			this.setField(Fields.DATE, null);
		}
	}
	
}
