package ca.fwe.caweather;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ca.fwe.weather.PreferenceActivity;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.WeatherWidgetSettings;
import ca.fwe.weather.backend.UpdatesManager;
import ca.fwe.weather.backend.UpdatesReceiver;
import ca.fwe.weather.core.ForecastLocation;

public class CityPageWeatherWidgetSettings extends WeatherWidgetSettings {

	private Preference locationPreference;

	@Override
	protected int[] getPreferenceXMLResources() {
		return new int[] {R.xml.widget_preferences};
	}

	@Override
	protected void onPreferenceFragmentCreate(PreferenceFragment fragment, Bundle savedInstanceState) {
		locationPreference = fragment.findPreference("widget_location") ;
		locationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				launchLocationPicker();
				return true;
			}
		});
	}

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
            locationPreference.setSummary(location.getName(lang));
		} else {
			log("setting null location, finish without adding widget") ;
			this.finish();
		}
	}

	protected void userOK() {
        //copy SharedPreferences to JSON object
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        JSONObject options = new JSONObject();
        for(String s : new String[] {"widget_location", "xml_theme_widget"}) {
            try {
                if(p.contains(s)) {
                    options.put(s, p.getString(s, null));
                }
            } catch(JSONException e) {
                log("Unable to write key " + s + "; value: " + p.getString(s, null));
            }
        }

        for(String s : new String[] {"widget_showcc_icon", "widget_day1", "widget_day2", "widget_day3"}) {
            try {
                if(p.contains(s)) {
                    options.put(s, String.valueOf(p.getBoolean(s, false)));
                }
            } catch(JSONException e) {
                log("Unable to write key " + s + "; value: " + p.getBoolean(s, false));
            }
        }

		log("adding widget info to database and sending force update broadcast (JSON: " + options.toString() + ")") ;
		UpdatesManager manager = new UpdatesManager(this) ;
		manager.addWidget(this.getWidgetId(), this.getLocation().getUri(), options.toString());

		//force update all widgets
		Intent i = new Intent(UpdatesReceiver.ACTION_FORCE_UPDATE_ALL) ;
		WeatherApp app = (WeatherApp)this.getApplication();
		app.broadcastManager(this).sendBroadcast(i);

		//close
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.getWidgetId());
		
		this.setResult(RESULT_OK, resultValue) ;
        this.finish();
	}

	private static void log(String message) {
		Log.i("CityPageWeatherWidgetSe", message) ;
	}
	
}
