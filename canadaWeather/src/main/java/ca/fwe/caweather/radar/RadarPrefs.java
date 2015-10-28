package ca.fwe.caweather.radar;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;
import ca.fwe.caweather.R;
import ca.fwe.weather.WeatherApp;

public class RadarPrefs extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(WeatherApp.getThemeId(this)) ;
		super.onCreate(savedInstanceState);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new PFrag())
		.commit();

	}

	@Override
	public void onPause() {
		if(this.isFinishing()) {
			Toast.makeText(this, R.string.pref_restart_app, Toast.LENGTH_SHORT).show() ;	
		}
		super.onPause();
	}

	public static class PFrag extends PreferenceFragment {

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.radar_preferences) ;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			this.onBackPressed();
			return true ;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
}
