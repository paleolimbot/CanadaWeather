package ca.fwe.caweather;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import ca.fwe.weather.ForecastActivity;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.core.ForecastLocation;

public class MainActivity extends ForecastActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("MainActivity", "error log to start logging") ;
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void launchPreferenceActivity() {
		Intent i = new Intent(this, CanadaWeatherPreferences.class) ;
		startActivity(i) ;
	}

	private void log(String message) {
		Log.i("MainActivity", message) ;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_radar) {
			ForecastLocation l = this.getCurrentLocation() ;
            if(l != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                String lat = String.valueOf(l.getLatLon().getLat());
                String lon = String.valueOf(l.getLatLon().getLon());
                String near = l.getName(lang);
                String requestUri = String.format("radar:///ca?lat=%s&lon=%s&near=%s", lat, lon, Uri.encode(near));
                log("Launching radar with uri " + requestUri);
                i.setData(Uri.parse(requestUri));

                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    //should never happen because it's part of the same app
                    toast(R.string.main_error_noradar);
                    log("radar not properly installed!");
                }
            }
			return true;
		} else if(id == R.id.menu_about) {
			this.showAboutDialog();
			return true ;
		} else if(id == R.id.menu_24hr) {
            ForecastLocation l = this.getCurrentLocation() ;
            if(l != null) {
                String mobileUrl = l.getMobileUrl(lang);
                Uri uri = Uri.parse(mobileUrl.replace("/city/pages/", "/forecast/hourly/"));
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(i);
                } catch(ActivityNotFoundException e) {
                    toast(R.string.forecast_error_no_browser);
                }
            }
            return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void showAboutDialog() {
		AlertDialog.Builder topo = new AlertDialog.Builder(this) ;
		topo.setTitle("Work in the oil patch?") ;
		topo.setMessage("Work in the oil patch and need directions to oilfield sites based on LLD or NTS coordinates? Prairie Coordinates, also by Dewey Dunnington, offers a simple, user-friendly interface to search legal land descriptions (LLD) and NTS coordinates and get directions in seconds. Or, buy the app just to donate a few dollars to the Canada Weather cause. Thanks!") ;
		topo.setPositiveButton("Download Prairie Coordinates", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW) ;
				i.setData(Uri.parse("https://play.google.com/store/apps/details?id=ca.fwe.pcoordplus")) ;
				try {
					startActivity(i) ;
				} catch(ActivityNotFoundException e) {
					toast(R.string.forecast_error_no_browser) ;
				}
			}
		}) ;
		topo.setNegativeButton("No Thanks!", null) ;
		final AlertDialog topoDialog = topo.create() ;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this) ;
		builder.setTitle(R.string.main_about_title) ;
		builder.setMessage(R.string.main_about_message) ;
		builder.setPositiveButton(R.string.main_about_rate, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW) ;
				i.setData(Uri.parse("https://play.google.com/store/apps/details?id=ca.fwe.caweather")) ;
				try {
					startActivity(i) ;
				} catch(ActivityNotFoundException e) {
					toast(R.string.forecast_error_no_browser) ;
				}
			}
		}) ;
		builder.setNegativeButton(R.string.main_about_site, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW) ;
				if(lang == WeatherApp.LANG_FR) {
                    i.setData(Uri.parse("https://apps.fishandwhistle.net/"));
                } else {
                    i.setData(Uri.parse("https://apps.fishandwhistle.net/"));
                }
				try {
					startActivity(i) ;
				} catch(ActivityNotFoundException e) {
					toast(R.string.forecast_error_no_browser) ;
				}
			}
		}) ;
		builder.setNeutralButton(R.string.main_about_gotit, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(lang != WeatherApp.LANG_FR)
					topoDialog.show();
			}
		}) ;
		builder.create().show();
	}



	@Override
	protected void onUpgradeTrue() {
		this.showAboutDialog();
		log("upgrading or installing, displaying dialog") ;

	}
	
}