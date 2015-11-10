package ca.fwe.caweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.UpdatesReceiver;

public class CanadaWeatherPreferences extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(WeatherApp.getThemeId(this)) ;
		super.onCreate(savedInstanceState);
		if(getActionBar() != null) this.getActionBar().setDisplayHomeAsUpEnabled(true);
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new PFrag())
		.commit();

	}

	@Override
	public void onPause() {
		if(this.isFinishing()) {
			Toast.makeText(this, R.string.pref_restart_app, Toast.LENGTH_SHORT).show() ;	
			WeatherApp app = (WeatherApp)this.getApplication() ;
			app.setLocale() ;

			//send intent to update all widgets/notifications
			Intent i = new Intent(UpdatesReceiver.ACTION_FORCE_UPDATE_ALL) ;
			this.sendBroadcast(i) ;
		}
		super.onPause();
	}

	public static class PFrag extends PreferenceFragment {

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.prefs_base) ;
			addPreferencesFromResource(R.xml.additional_prefs) ;
			Preference pref = this.findPreference("xml_edit_notifications") ;
			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), NotificationsEditor.class) ;
					startActivity(i) ;
					return true;
				}
				
			});
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
