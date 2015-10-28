package ca.fwe.weather.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import ca.fwe.locations.geometry.LatLon;

public class LocationFetcher {

	private static final long UPDATE_TIME = 5000 ; //ten seconds
	
	private GPSLocationListener listener ;
	private LocationManager manager ;
	private Location lastFix ;
	private boolean gpsUpdating ;
	private long lastUpdate ;

	
	public LocationFetcher(Context context, GPSLocationListener listener) {
		this.listener = listener ;
		manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE) ;
		gpsUpdating = false ;
		lastUpdate = 0 ;
	}
	
	public void enableUpdates() {
		manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, 0, networkLocationListener) ;
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIME, 0, gpsLocationListener) ;
	}
	
	public void disableUpdates() {
		manager.removeUpdates(gpsLocationListener) ;
		manager.removeUpdates(networkLocationListener) ;
	}
	
	public Location getFix() {
		return lastFix ;
	}
	
	private LocationListener gpsLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			gpsUpdating = true ;
			if(System.currentTimeMillis() - lastUpdate > UPDATE_TIME)
				updateLocation(location) ;
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {
			manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, 0, networkLocationListener) ;
		}
	};
	
	private LocationListener networkLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if(!gpsUpdating)
				updateLocation(location) ;
			else
				manager.removeUpdates(this) ;
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {}
	};
	
	private void updateLocation(Location location) {
		lastUpdate = System.currentTimeMillis() ;
		lastFix = location ;
		if(listener != null)
			listener.onLocationChange(from(location), (int)Math.round(location.getAltitude()), (int)Math.round(location.getAccuracy())) ; ;
	}
	
	private static LatLon from(Location position) {
		return new LatLon(position.getLatitude(), position.getLongitude()) ;
	}
	
	public interface GPSLocationListener {
		public void onLocationChange(LatLon position, int altitudeMetres, int accuracyMetres) ;
	}

	
}
