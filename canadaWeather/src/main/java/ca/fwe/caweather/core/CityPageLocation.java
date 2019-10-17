package ca.fwe.caweather.core;

import java.io.File;
import java.util.Locale;

import android.net.Uri;
import ca.fwe.caweather.backend.CityPageForecastParser;
import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.ForecastXMLParser;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastLocation;

public class CityPageLocation extends ForecastLocation {

	public static final String URI_PATTERN = "citypage:///%s/%s" ;
	
	public static final String URL_XML_PATTERN = "https://dd.weather.gc.ca/citypage_weather/xml/%s/%s_%s.xml" ;
	public static final String URL_WEB_PATTERN = "https://weather.gc.ca/city/pages/%s_metric_%s.html" ;
	
	private String sitecode ;
	private String provinceCode ;
	private String nameEn ;
	private String nameFr ;
	private LatLon latLon ;
	private String webId ;
	
	public CityPageLocation(String sitecode, String provinceCode,
			String nameEn, String nameFr, String webId, LatLon latlon) {
		this.sitecode = sitecode;
		this.provinceCode = provinceCode;
		this.nameEn = nameEn;
		this.nameFr = nameFr;
		this.webId = webId ;
		this.latLon = latlon ;
	}
	
	public String getSitecode() {
		return this.sitecode ;
	}
	
	public String getProvinceCode() {
		return this.provinceCode ;
	}
	
	public String getWebId() {
		return this.webId ;
	}
	
	public String toString(int lang) {
		return this.getName(lang) + ", " + this.getProvinceCode().toUpperCase(Locale.CANADA) ;
	}
	
	@Override
	public Uri getUri() {
		return Uri.parse(String.format(URI_PATTERN, this.provinceCode.toLowerCase(Locale.CANADA), this.sitecode)) ;
	}

	@Override
	public LatLon getLatLon() {
		return latLon ;
	}

	@Override
	public String getName(int lang) {
		switch(lang) {
		case WeatherApp.LANG_FR:
			return this.nameFr ;
		default:
			return this.nameEn ;
		}
	}

	@Override
	public String getXmlUrl(int lang) {
		String langSuffix = "e" ;
		if(lang == WeatherApp.LANG_FR)
			langSuffix = "f" ;
		return String.format(URL_XML_PATTERN, this.provinceCode, this.sitecode, langSuffix) ;
	}

	@Override
	public ForecastXMLParser getXMLParser(Forecast forecast, File file) {
		return new CityPageForecastParser(forecast, file) ;
	}

	@Override
	public String getMobileUrl(int lang) {
		return this.getWebUrl(lang) ;
	}

	@Override
	public String getWebUrl(int lang) {
		String langSuffix = "e" ;
		if(lang == WeatherApp.LANG_FR)
			langSuffix = "f" ;
		return String.format(URL_WEB_PATTERN, this.webId, langSuffix) ;
	}

	@Override
	public String getCacheFileName(int lang) {
		String langSuffix = "e" ;
		if(lang == WeatherApp.LANG_FR)
			langSuffix = "f" ;
		return String.format("%s_%s.xml", this.getSitecode(), langSuffix) ;
	}

}
