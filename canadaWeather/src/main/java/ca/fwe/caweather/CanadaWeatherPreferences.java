package ca.fwe.caweather;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import ca.fwe.weather.PreferenceActivity;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.UpdatesReceiver;

public class CanadaWeatherPreferences extends PreferenceActivity {

	@Override
	protected int[] getPreferenceXMLResources() {
		return new int[] {R.xml.prefs_base, R.xml.additional_prefs};
	}

	@Override
	protected void onPreferenceFragmentCreate(PreferenceFragment fragment, Bundle savedInstanceState) {
		Preference pref = fragment.findPreference("xml_edit_notifications") ;
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(CanadaWeatherPreferences.this, NotificationsEditor.class) ;
				startActivity(i) ;
				return true;
			}

		});
	}

	@Override
	public void onPause() {
		if(this.isFinishing()) {
			Toast.makeText(this, R.string.pref_restart_app, Toast.LENGTH_SHORT).show() ;
			WeatherApp app = (WeatherApp)this.getApplication() ;
			app.setLocale() ;

			//send intent to update all widgets/notifications
			Intent i = new Intent(UpdatesReceiver.ACTION_FORCE_UPDATE_ALL) ;
			app.broadcastManager(this).sendBroadcast(i);
		}
		super.onPause();
	}

}
