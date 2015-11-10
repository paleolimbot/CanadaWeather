package ca.fwe.weather;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.backend.UserLocationsList;
import ca.fwe.weather.core.CurrentConditions;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.Units;
import ca.fwe.weather.core.Units.UnitSet;
import ca.fwe.weather.core.WeatherWarning;
import ca.fwe.weather.util.ForecastDownloader;
import ca.fwe.weather.util.ForecastDownloader.Modes;
import ca.fwe.weather.util.ForecastDownloader.ReturnTypes;

public abstract class ForecastActivity extends ListActivity implements ForecastDownloader.OnForecastDownloadListener,
																		OnItemClickListener {
	private static final String TAG = "ForecastActivity" ;
	
	private Spinner locationSpinner ;
	protected LocationsAdapter locationsAdapter ;
	protected UserLocationsList userLocations ;
	protected LocationDatabase locationDatabase ;

	private static final String PREF_KEY_LAST_LOC_POS = "last_location_position" ;
	
	protected static final int REQUEST_LOCATION = 11 ;
	
	private ForecastDownloader downloader = null ;
	private ProgressDialog onDownloadDialog = null ;
	
	protected WeatherApp app ;
	private ForecastAdapter forecastAdapter ;
	private NoDataAdapter noDataAdapter ;
	protected int lang ;
	private SharedPreferences prefs ;
	private TextView fIssuedView ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(WeatherApp.getThemeId(this)) ;
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.forecast);
		app = (WeatherApp) this.getApplication() ;
		prefs = WeatherApp.prefs(this) ;
		lang = WeatherApp.getLanguage(this) ;
		locationSpinner = (Spinner)findViewById(R.id.forecast_locations) ;
		forecastAdapter = new ForecastAdapter(this) ;
		noDataAdapter = new NoDataAdapter() ;
		
		onDownloadDialog = new ProgressDialog(this) ;
		onDownloadDialog.setMessage(getString(R.string.please_wait));
		onDownloadDialog.setTitle(R.string.forecast_loading_forecast);
		onDownloadDialog.setIndeterminate(true);
		onDownloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				if(downloader != null) {
					downloader.cancel();
				}
				
			}
		});
		
		fIssuedView = (TextView)this.getLayoutInflater().inflate(R.layout.forecast_headerfooter, null) ;
		fIssuedView.setText(String.format(getString(R.string.forecast_issuedtext), getString(R.string.unknown)));
		this.getListView().addHeaderView(fIssuedView);
		
		TextView footer = (TextView)this.getLayoutInflater().inflate(R.layout.forecast_headerfooter, null) ;
		footer.setText(R.string.forecast_footertext) ;
		this.getListView().addFooterView(footer);
		
		this.getListView().setOnItemClickListener(this) ;
		this.locationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ForecastLocation l = locationsAdapter.getItem(position) ;
				setLocation(l, false) ;
				Editor e = prefs.edit() ;
				e.putInt(PREF_KEY_LAST_LOC_POS, position) ;
				e.apply() ;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				setListAdapter(noDataAdapter);
				fIssuedView.setText(String.format(getString(R.string.forecast_issuedtext), getString(R.string.unknown)));
				forecastAdapter.clear();
			}
			
		});
		if(app.upgrade()) {
			this.onUpgradeTrue();
		}
		locationDatabase = app.getLocationDatabase(this) ;
		userLocations = new UserLocationsList(locationDatabase) ;
		locationsAdapter = new LocationsAdapter(this) ;
		locationsAdapter.refreshLocations();
			
		Intent launchIntent = this.getIntent() ;
		if(launchIntent != null && launchIntent.getData() != null) {
			locationsAdapter.addUri(launchIntent.getData());
		} else {
			this.setSpinnerPositionFromPref();
		}
		
	}

	protected abstract void onUpgradeTrue() ;
	
	private void setSpinnerPositionFromPref() {
		if(locationsAdapter.getCount() > 0) {
			int lastPos = prefs.getInt(PREF_KEY_LAST_LOC_POS, 0) ;
			if(lastPos >= locationsAdapter.getCount())
				lastPos = locationsAdapter.getCount() - 1 ;
			locationSpinner.setSelection(lastPos);
		} else {
			this.getListView().setAdapter(noDataAdapter);
			this.launchLocationPicker();
		}
	}
	
	protected void launchLocationPicker() {
		Intent i = new Intent(this, LocationPickerActivity.class) ;
		this.startActivityForResult(i, REQUEST_LOCATION);
	}
	
	protected abstract void launchPreferenceActivity() ;
	
	protected void setLocation(ForecastLocation l, boolean forceDownload) {
		log("setting location to " + l.getUri()) ;
		if(downloader != null)
			downloader.cancel();
		
		UnitSet unitset = Units.getUnitSet(prefs.getString(WeatherApp.PREF_KEY_UNITS, Units.UNITS_DEFAULT)) ;
		
		//load cached version first
		Forecast cachedF = new Forecast(this, l, lang) ;
		cachedF.setUnitSet(unitset);
		ForecastDownloader cachedLoader = new ForecastDownloader(cachedF, this, Modes.LOAD_CACHED) ;
		log("starting cached forecast loader") ;
		cachedLoader.download();
		
		Forecast forecast = new Forecast(this, l, lang) ;
		forecast.setUnitSet(unitset);
		Modes mode = Modes.LOAD_RECENT_CACHE_OR_DOWNLOAD ;
		if(forceDownload)
			mode = Modes.FORCE_DOWNLOAD ;
		
		downloader = new ForecastDownloader(forecast, this, mode) ;
		log("starting downloader") ;
		downloader.download();
		onDownloadDialog.show();
		
	}

	@Override
	public void onForecastDownload(Forecast forecast, Modes mode, ReturnTypes result) {
		log("onForecastDownload returned with result " + result + " from mode " + mode) ;
		if(!mode.equals(Modes.LOAD_CACHED)) {
			//TODO crash report says this causes error
			onDownloadDialog.dismiss();
		}
		switch(result) {
		case IO_ERROR:
			toast(R.string.forecast_error_connectivity) ;
			break ;
		case XML_ERROR:
			toast(R.string.forecast_error_xml) ;
			break ;
		case UNKNOWN_ERROR:
			toast(R.string.forecast_error_unknown) ;
			break ;
		case NO_CACHED_FORECAST_ERROR:
			this.setListAdapter(noDataAdapter);
			fIssuedView.setText(String.format(getString(R.string.forecast_issuedtext), getString(R.string.unknown)));
			this.forecastAdapter.clear();
			return ;
		default:
			log("forecast downloaded sucessfully, setting adapter.") ;
			forecastAdapter.setForecast(forecast);
			this.setListAdapter(forecastAdapter);
			String dateText ;
			if(forecast.getIssuedDate() != null)
				dateText = forecast.getDateFormat().format(forecast.getIssuedDate()) ;
			else
				dateText = getString(R.string.unknown) ;
			this.fIssuedView.setText(
					String.format(getString(R.string.forecast_issuedtext), dateText));
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		log("receiving result with code " + requestCode + " and result " + resultCode) ;
		if(requestCode == REQUEST_LOCATION) {
			if(resultCode == RESULT_OK) {
				if(data != null) {
					Uri uri = data.getData() ;
					if(uri != null) {
						locationsAdapter.addUri(uri);
					} else {
						toast(R.string.forecast_error_location_request) ;
						log("result back from location browser with no uri", null) ;
					}
				} else {
					toast(R.string.forecast_error_location_request) ;
					log("result back from location browser with no data intent", null) ;
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		log("processing click on item " + position) ;
		if(forecastAdapter.getCount() > (position-1) && position >= 1) {
			this.onItemClick(forecastAdapter.getItem(position-1)) ;
		} else if(position == 0 || position == forecastAdapter.getCount()) {
			log("first or last item click (ignoring)");
		} else {
			log("non-forecast adapter") ;
		}
		
	}
	
	protected ForecastLocation getCurrentLocation() {
		return locationsAdapter.current() ;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.forecast, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_edit) {
			this.makeLocationsEditor();
			return true;
		} else if(id == R.id.menu_prefs) {
			this.launchPreferenceActivity();
			return true ;
		} else if (id == R.id.menu_refresh) {
			ForecastLocation current = this.getCurrentLocation() ;
			if(current != null) {
				this.setLocation(current, true);
			} else {
				toast(R.string.forecast_error_no_location_selected) ;
				log("trying to refresh location when there is no location!", null) ;
			}
			return true ;
		} else if (id == R.id.menu_view_online) {
			ForecastLocation current = this.getCurrentLocation() ;
			if(current != null) {
				this.launchOnlineEdition(current);
			} else {
				toast(R.string.forecast_error_no_location_selected) ;
				log("trying to launch online edition when there is no location!", null) ;
			}
			return true ;
		} else if (id == R.id.menu_add) {
			this.launchLocationPicker();
			return true ;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	protected void launchOnlineEdition(ForecastLocation l) {
		String mobileUrl = l.getMobileUrl(lang) ;
		if(mobileUrl != null) {
			Uri uri = Uri.parse(mobileUrl) ;
			Intent launch = new Intent(Intent.ACTION_VIEW) ;
			launch.setData(uri) ;
			try {
				startActivity(launch) ;
			} catch(ActivityNotFoundException e) {
				log("no internet browser", e) ;
				toast(R.string.forecast_error_no_browser) ;
			}
		} else {
			toast(R.string.forecast_error_no_location_selected) ;
			log("no mobile url with which to launch online edition", null) ;
		}
	}
	
	protected void toast(int messageId) {
		Toast.makeText(this, messageId, Toast.LENGTH_LONG).show() ;
	}
	
	protected void onItemClick(ForecastItem item) {
		if(item instanceof WeatherWarning) {
			String url = ((WeatherWarning) item).getMobileUrl() ;
			if(url != null) {
				Uri uri = Uri.parse(url) ;
				Intent launch = new Intent(Intent.ACTION_VIEW) ;
				launch.setData(uri) ;
				try {
					startActivity(launch) ;
				} catch(ActivityNotFoundException e) {
					log("no internet browser", e) ;
					toast(R.string.forecast_error_no_browser) ;				
				}
			} else {
				toast(R.string.forecast_error_no_location_selected) ;
				log("no mobile url with which to launch weather warning", null) ;
			}
		} else if(item instanceof CurrentConditions) {
			this.makeCurrentConditionsDialog((CurrentConditions)item) ;
		}
	}
	
	private void makeCurrentConditionsDialog(CurrentConditions c) {
		AlertDialog.Builder b = new AlertDialog.Builder(this) ;
		b.setTitle(R.string.forecast_cc_dialog_title) ;
		b.setMessage(Html.fromHtml(c.getHtmlSummary())) ;
		b.setIcon(c.getIcon()) ;
		b.setNegativeButton(R.string.cancel, null) ;
		b.create().show(); 
	}

	private void makeLocationsEditor() {
		final List<ForecastLocation> locations = userLocations.getList() ;
		final int count = locations.size() ;
		String[] locs = new String[count] ;
		final boolean[] selection = new boolean[locs.length] ;
		for(int i=0; i<locs.length; i++) {
			locs[i] = locations.get(i).getName(i) ;
			selection[i] = true ;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this) ;
		builder.setTitle(R.string.forecast_editloc_dialog_title) ;
		builder.setPositiveButton(R.string.forecast_editloc_update, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int savedPosition = prefs.getInt(PREF_KEY_LAST_LOC_POS, 0) ;
				for(int i=count-1; i>=0; i--) {
					if(!selection[i]) {
						ForecastLocation deleted = locations.get(i) ;
						userLocations.removeLocations(deleted);
						if(i+1<=savedPosition) //i+1 because of MyLocation
							savedPosition-- ;
					}
				}
				Editor e = prefs.edit() ;
				e.putInt(PREF_KEY_LAST_LOC_POS, savedPosition) ;
				e.apply() ;
				locationsAdapter.refreshLocations();
				setSpinnerPositionFromPref();
			}
		}) ;
		builder.setNegativeButton(R.string.cancel, null) ;
		builder.setMultiChoiceItems(locs, selection, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				selection[which] = isChecked ;
			}
		}) ;
		builder.create().show() ;
	}

	
	private class LocationsAdapter extends CustomSpinnerAdapter<ForecastLocation> {

		public LocationsAdapter(Context context) {
			super(context);
		}
		
		@Override
		protected void modifyTextView(ForecastLocation l, TextView v) {
			v.setText(l.toString(lang)) ;
		}

		public void addUri(Uri uri) {
			log("adding uri to userLocations " + uri) ;
			ForecastLocation newLoc = userLocations.addLocation(uri) ;
			if(newLoc != null) {
				this.refreshLocations();
				int newIndex = this.getPosition(newLoc) ;
				if(newIndex != -1) {
					locationSpinner.setSelection(newIndex);
				} else {
					toast(R.string.forecast_error_add_location) ;
					log("added location, but couldn't find it in the list", null) ;
				}
			} else {
				toast(R.string.forecast_error_add_location) ;
				log("error adding location", null) ;
			}
		}
		
		private void refreshLocations() {
			log("refreshing locations") ;
			this.clear();
			for(ForecastLocation l: userLocations.getList())
				this.add(l);
			locationSpinner.setAdapter(this);
		}
		
		public ForecastLocation current() {
			if(locationSpinner != null) {
				int currentpos = locationSpinner.getSelectedItemPosition() ;
				if(currentpos > -1 && currentpos < this.getCount()) {
					return this.getItem(currentpos) ;
				} else {
					return null ;
				}
			} else {
				return null ;
			}
		}
		
	}
	
	
	private class NoDataAdapter extends ArrayAdapter<String> {

		public NoDataAdapter() {
			super(ForecastActivity.this, android.R.layout.simple_list_item_1);
			this.add(getString(R.string.forecast_no_data));
		}
		
	}
	
	private static void log(String message) {
		Log.i(TAG, message) ;
	}
	
	
	private static void log(String message, Exception e) {
		if(e != null)
			Log.e(TAG, message, e) ;
		else
			Log.e(TAG, message) ;
	}
	
}
