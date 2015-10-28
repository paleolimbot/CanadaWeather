package ca.fwe.weather.core;

public interface ForecastRegion {

	public String getRegionCode() ;
	public String getName(int lang) ;
	public String getParentRegionCode() ;
	
}
