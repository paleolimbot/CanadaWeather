package ca.fwe.weather.backend;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.ForecastRegion;

public abstract class LocationDatabase {

	private Context context ;
	
	public LocationDatabase(Context context) {
		this.context = context ;
	}
	
	public abstract List<? extends ForecastRegion> getRegions() ;
	
	public abstract List<? extends ForecastLocation> getLocations(String regionCode) ;
		
	public abstract List<? extends ForecastLocation> filterLocations(String filterText) ;
	
	public abstract List<? extends ForecastLocation> locationsNear(LatLon location) ;
		
	public abstract ForecastLocation getLocation(Uri uri) ;

	public Context getContext() {
		return context ;
	}
	
}
