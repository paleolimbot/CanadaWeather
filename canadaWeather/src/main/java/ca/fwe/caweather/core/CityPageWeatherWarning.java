package ca.fwe.caweather.core;

import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.WeatherWarning;

public class CityPageWeatherWarning extends WeatherWarning {

	private String url ;
	
	public CityPageWeatherWarning(Forecast forecast) {
		super(forecast);
	}

	public void setUrl(String url) {
		this.url = url ;
	}
	
	@Override
	public String getUrl() {
		return url ;
	}

	@Override
	public String getMobileUrl() {
		return this.getUrl() ;
	}

}
