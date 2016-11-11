package ca.fwe.caweather;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import ca.fwe.weather.WeatherWidgetSettings;
import ca.fwe.weather.backend.UpdatesManager;
import ca.fwe.weather.backend.UpdatesReceiver;
import ca.fwe.weather.core.ForecastLocation;

public class CityPageWeatherWidgetSettings extends WeatherWidgetSettings {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		log("starting widget settings!") ;
		super.onCreate(savedInstanceState);
		this.launchLocationPicker();
	}
	
	@Override
	protected void setLocation(ForecastLocation location) {
		super.setLocation(location);
		if(location != null) {
			log("setting location " + location.getUri()) ;

			this.addWidgetInfoToDatabase();
			this.finish();
		} else {
			log("setting null location, finish without adding widget") ;
			this.finish();
		}
	}
	
	private void addWidgetInfoToDatabase() {
		log("adding widget info to database and sending force update broadcast") ;
		UpdatesManager manager = new UpdatesManager(this) ;
		manager.addWidget(this.getWidgetId(), this.getLocation().getUri(), "{\"testkey\": \"testvalue\"}"); //TODO implement options
		//add other options here as they are added to updates manager

		//force update all widgets
		Intent i = new Intent(UpdatesReceiver.ACTION_FORCE_UPDATE_ALL) ;
		this.sendBroadcast(i) ;
		
		//close
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.getWidgetId());
		
		this.setResult(RESULT_OK, resultValue) ;
	}

	private static void log(String message) {
		Log.i("CityPageWeatherWidgetSe", message) ;
	}
	
}
