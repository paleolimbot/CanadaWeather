package ca.fwe.weather.util;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

public class GeocoderAsync {

	private Geocoder geocoder ;
	private GeocoderTask geocoderTask ;
	private OnGeocodeListener geocodeListener ;
	
	public GeocoderAsync(Context context, OnGeocodeListener listener) {
		geocoder = new Geocoder(context) ;
		geocodeListener = listener ;
	}
	
	public void cancel() {
		if(geocoderTask != null) {
			geocoderTask.cancel(true) ;
			geocoderTask = null ;
		}
	}
	
	public void geocode(String query) {
		this.cancel();
		geocoderTask = new GeocoderTask() ;
		geocoderTask.execute(query) ;
	}
	
	private class GeocoderTask extends AsyncTask<String, Void, Exception> {

		private List<Address> results = null ;
		
		@Override
		protected Exception doInBackground(String... params) {
			try {
				results = geocoder.getFromLocationName(params[0], 5) ;
			} catch (IOException e) {
				return e ;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Exception result) {
			if(result == null) {
				if(geocodeListener != null)
					geocodeListener.onGeocode(results);
			} else {
				if(geocodeListener != null) {
					geocodeListener.onGeocodeError(result);
				}
			}
		}
	}
	
	public interface OnGeocodeListener {
		void onGeocode(List<Address> address) ;
		void onGeocodeError(Exception error) ;
	}
}
