package ca.fwe.weather.core;

import java.util.HashMap;
import java.util.Map;

public class Units {
	
	public enum Unit {DEG_F, DEG_C, DEG_K, MILES_PER_HOUR, KILOMETRES_PER_HOUR, METRES_PER_SECOND, KNOTS, METRES, MILES, KILOMETRES, NAUTICAL_MILES,
						PASCALS, MILLIBARS, IN_HG, MM_HG, PERCENT, UNITLESS, KILOPASCALS}
	
	public enum Categories {WIND_SPEED, VISIBILITY, TEMPERATURE, PROBABILITY, PRESSURE}
	
	public static final String UNITS_METRIC = "metric" ;
	public static final String UNITS_IMPERIAL = "imperial" ;
	public static final String UNITS_SI = "si" ;
	public static final String UNITS_DEFAULT = UNITS_METRIC ;
	
	private static Map<Unit, Double> values = new HashMap<>() ;
	private static Map<Unit, String> labels = new HashMap<>() ;
	
	static {
		values.put(Unit.DEG_K, 1.0) ;
		values.put(Unit.MILES_PER_HOUR, 0.44704) ;
		values.put(Unit.KILOMETRES_PER_HOUR, 0.27777777) ;
		values.put(Unit.METRES_PER_SECOND, 1.0) ;
		values.put(Unit.KNOTS, 0.514444444) ;
		values.put(Unit.METRES, 1.0) ;
		values.put(Unit.MILES, 1609.0) ;
		values.put(Unit.NAUTICAL_MILES, 1852.0) ;
		values.put(Unit.KILOMETRES, 1000.0) ;
		values.put(Unit.PASCALS, 1.0) ;
		values.put(Unit.MILLIBARS, 100.0) ;
		values.put(Unit.KILOPASCALS, 1000.0) ;
		values.put(Unit.IN_HG, 3386.0) ;
		values.put(Unit.MM_HG, 133.322368) ;
		values.put(Unit.PERCENT, 100.0);
		values.put(Unit.UNITLESS, 1.0);
	}
	
	static {
		labels.put(Unit.DEG_F, "°F") ;
		labels.put(Unit.DEG_C, "°C") ;
		labels.put(Unit.DEG_K, "kelvins") ;
		labels.put(Unit.MILES_PER_HOUR, "mph") ;
		labels.put(Unit.KILOMETRES_PER_HOUR, "km/hr") ;
		labels.put(Unit.METRES_PER_SECOND, "m/s") ;
		labels.put(Unit.KNOTS, "kts") ;
		labels.put(Unit.METRES, "m") ;
		labels.put(Unit.MILES, "mi") ;
		labels.put(Unit.NAUTICAL_MILES, "nm") ;
		labels.put(Unit.KILOMETRES, "km") ;
		labels.put(Unit.PASCALS, "Pa") ;
		labels.put(Unit.MILLIBARS, "mb") ;
		labels.put(Unit.IN_HG, "in Hg") ;
		labels.put(Unit.MM_HG, "mm Hg") ;
		labels.put(Unit.KILOPASCALS, "kPa") ;
		labels.put(Unit.PERCENT, "%");
		labels.put(Unit.UNITLESS, "");
	}
	
	public static double toSI(double value, Unit unit) {
		if(unit.equals(Unit.DEG_C)) {
			return value + 273.15 ;
		} else if(unit.equals(Unit.DEG_F) ){
			return ((value - 32.0) / 1.8) + 273.15 ;
		} else {
			return value * values.get(unit) ;
		}
	}

	public static double fromSI(double value, Unit unit) {
		if(unit.equals(Unit.DEG_C)) {
			return value - 273.15 ;
		} else if(unit.equals(Unit.DEG_F) ){
			return (value - 273.15) * 1.8 + 32.0 ;
		} else {
			return value / values.get(unit) ;
		}
	}
	
	public static double convert(double fromValue, Unit fromUnit, Unit toUnit) {
		return fromSI(toSI(fromValue, fromUnit), toUnit) ;
	}
	
	public static String getLabel(Unit unit) {
		return labels.get(unit) ;
	}
		
	public static class UnitSet {
				
		public static UnitSet SI = new UnitSet() ;
		static {
			SI.setUnit(Categories.PRESSURE, Unit.PASCALS);
			SI.setUnit(Categories.TEMPERATURE, Unit.DEG_K);
			SI.setUnit(Categories.VISIBILITY, Unit.METRES) ;
			SI.setUnit(Categories.WIND_SPEED, Unit.METRES_PER_SECOND);
			SI.setUnit(Categories.PROBABILITY, Unit.UNITLESS);
		}
		
		public static UnitSet METRIC = new UnitSet() ;
		static {
			METRIC.setUnit(Categories.PRESSURE, Unit.KILOPASCALS);
			METRIC.setUnit(Categories.TEMPERATURE, Unit.DEG_C);
			METRIC.setUnit(Categories.VISIBILITY, Unit.KILOMETRES);
			METRIC.setUnit(Categories.WIND_SPEED, Unit.KILOMETRES_PER_HOUR) ;
			METRIC.setUnit(Categories.PROBABILITY, Unit.PERCENT);
		}
		
		public static UnitSet IMPERIAL = new UnitSet() ;
		static {
			IMPERIAL.setUnit(Categories.PRESSURE, Unit.IN_HG);
			IMPERIAL.setUnit(Categories.TEMPERATURE, Unit.DEG_F);
			IMPERIAL.setUnit(Categories.VISIBILITY, Unit.MILES);
			IMPERIAL.setUnit(Categories.WIND_SPEED, Unit.MILES_PER_HOUR);
			IMPERIAL.setUnit(Categories.PROBABILITY, Unit.PERCENT);
		}
		
		public static UnitSet NAUTICAL = new UnitSet() ;
		static {
			NAUTICAL.setUnit(Categories.PRESSURE, Unit.IN_HG);
			NAUTICAL.setUnit(Categories.TEMPERATURE, Unit.DEG_F);
			NAUTICAL.setUnit(Categories.VISIBILITY, Unit.NAUTICAL_MILES);
			NAUTICAL.setUnit(Categories.WIND_SPEED, Unit.KNOTS);
			NAUTICAL.setUnit(Categories.PROBABILITY, Unit.PERCENT);
		}
		
		private Map<Categories, Unit> units = new HashMap<>() ;
		
		public void setUnit(Categories category, Unit unit) {
			if(unit != null)
				units.put(category, unit) ;
			else
				units.remove(category) ;
		}
		
		public Unit getUnit(Categories category) {
			if(units.containsKey(category)) {
				return units.get(category) ;
			} else {
				return null ;
			}
		}
		
	}

	public static UnitSet getUnitSet(String string) {
		if(string == null)
			return UnitSet.METRIC ;
		if(UNITS_METRIC.equals(string)) {
			return UnitSet.METRIC ;
		} else if(UNITS_IMPERIAL.equals(string)) {
			return UnitSet.IMPERIAL ;
		} else if(UNITS_SI.equals(string)) {
			return UnitSet.SI ;
		} else {
			return UnitSet.METRIC ;
		}
	}
}
