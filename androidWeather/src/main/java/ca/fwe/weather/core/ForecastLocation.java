package ca.fwe.weather.core;

import java.io.File;

import android.net.Uri;
import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.backend.ForecastXMLParser;

public abstract class ForecastLocation {
	
	private String alias ;
	
	public abstract Uri getUri();
	public abstract LatLon getLatLon();
	
	public abstract String getName(int lang);
	public abstract String getXmlUrl(int lang);
	public abstract ForecastXMLParser getXMLParser(Forecast forecast, File file) ;
	public abstract String getMobileUrl(int lang);
	public abstract String getWebUrl(int lang);
	public abstract String getCacheFileName(int lang) ;
	public abstract String toString(int lang) ;
	public void setAlias(String alias) {
		this.alias = alias ;
	}
	public String getAlias() {
		return alias ;
	}
	
	public boolean equals(Object o) {
		if(o instanceof ForecastLocation) {
			Uri other = ((ForecastLocation) o).getUri() ;
			return this.getUri().equals(other) ;
		} else {
			return false ;
		}
	}
	
}
