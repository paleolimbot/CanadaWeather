package ca.fwe.caweather.radar;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;
import ca.fwe.caweather.R;
import ca.fwe.weather.PreferenceActivity;
import ca.fwe.weather.WeatherApp;

public class RadarPrefs extends PreferenceActivity {

	@Override
	protected int[] getPreferenceXMLResources() {
		return new int[] {R.xml.radar_preferences};
	}

	@Override
	protected void onPreferenceFragmentCreate(PreferenceFragment fragment, Bundle savedInstanceState) {
		//silence is golden
	}
	
}
