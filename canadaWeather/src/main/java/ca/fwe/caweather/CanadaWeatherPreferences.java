package ca.fwe.caweather;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import ca.fwe.weather.PreferenceActivity;

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

}
