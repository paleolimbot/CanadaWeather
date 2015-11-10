package ca.fwe.weather.core;

public interface ForecastRegion {

	String getRegionCode() ;
	String getName(int lang) ;
	String getParentRegionCode() ;
	
}
