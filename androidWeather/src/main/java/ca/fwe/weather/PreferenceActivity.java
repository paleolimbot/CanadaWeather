package ca.fwe.weather;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import ca.fwe.weather.backend.UpdatesReceiver;

/**
 * Created by dewey on 2016-11-10.
 */

public abstract class PreferenceActivity extends AppCompatActivity {

    protected abstract int[] getPreferenceXMLResources() ;
    protected abstract void onPreferenceFragmentCreate(PreferenceFragment fragment, Bundle savedInstanceState);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.setTheme(WeatherApp.getThemeId(this)) ;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.pref_content, new PFrag())
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
            PreferenceActivity a = (PreferenceActivity)getActivity();
            for(int id: a.getPreferenceXMLResources()) {
                addPreferencesFromResource(id);
            }
            a.onPreferenceFragmentCreate(this, savedInstanceState);
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
