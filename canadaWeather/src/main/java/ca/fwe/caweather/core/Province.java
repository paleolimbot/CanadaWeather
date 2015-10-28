package ca.fwe.caweather.core;

import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.core.ForecastRegion;

public class Province implements ForecastRegion {

	private String provinceCode ;
	private String nameEn ;
	private String nameFr ;
	public Province(String provinceCode, String nameEn, String nameFr) {
		this.provinceCode = provinceCode;
		this.nameEn = nameEn;
		this.nameFr = nameFr;
	}
	public String getRegionCode() {
		return provinceCode;
	}
	public String getName(int lang) {
		switch(lang) {
		case WeatherApp.LANG_FR:
			return this.nameFr ;
		default:
			return this.nameEn ;
		}
	}
	@Override
	public String getParentRegionCode() {
		return null;
	}
	
	
	
}
