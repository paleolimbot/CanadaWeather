package ca.fwe.weather;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ca.fwe.weather.util.LatLon;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.ForecastRegion;
import ca.fwe.weather.util.GeocoderAsync;
import ca.fwe.weather.util.GeocoderAsync.OnGeocodeListener;
import ca.fwe.weather.util.LocationFetcher;

public class LocationPickerActivity extends AppCompatActivity implements OnItemClickListener, TextWatcher,
        LocationFetcher.GPSLocationListener, OnGeocodeListener, ActivityCompat.OnRequestPermissionsResultCallback {

	private static final String TAG = "LocationPickerActivity" ;
	private static final String PREF_KEY_SAVED_REGION = "locations_saved_region" ;
    private static final int REQUEST_LOCATION = 291;

	private LocationDatabase locationDb ;
	protected WeatherApp app ;
	private int lang ;
	private Spinner regionSpinner ;
	private RegionAdapter regionAdapter ;
	private LocationsAdapter locationsAdapter ;
	private EditText filterText ;
	private ProgressDialog onGPSLocationDialog ;
	private LocationFetcher locationFetcher ;
	private boolean gpsUpdating = false ;
	private SharedPreferences prefs ;
	private ListView listView;

	private LatLon latLon ;

	private ProgressDialog geocodingDialog ;
	private GeocoderAsync geocoder ;
	
	private TextView searchHeader ;
	private boolean searchOnClickHeader = false ;

	private boolean creating = true ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(WeatherApp.getThemeId(this)) ;
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.location_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		app = (WeatherApp)this.getApplication() ;
		locationDb = app.getLocationDatabase(this) ;
		lang = WeatherApp.getLanguage(this) ;
		locationsAdapter = new LocationsAdapter() ;
		prefs = WeatherApp.prefs(this) ;

		searchHeader = (TextView)this.getLayoutInflater().inflate(R.layout.location_searchheader, null) ;
        listView = (ListView)findViewById(android.R.id.list);
		listView.addHeaderView(searchHeader);
		
		regionSpinner = (Spinner)findViewById(R.id.locations_region_spinner) ;
		List<? extends ForecastRegion> regions = locationDb.getRegions() ;
		if(regions != null) {
			regionAdapter = new RegionAdapter() ;
			regionAdapter.addAll(regions);
			regionSpinner.setAdapter(regionAdapter);
			regionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					setByRegion(regionAdapter.getItem(position)) ;
					Editor edit = prefs.edit() ;
					edit.putInt(PREF_KEY_SAVED_REGION, position) ;
					edit.apply() ;
					if(!creating)
						hideKeyboard() ;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					//set empty list
					locationsAdapter.reset(new ArrayList<ForecastLocation>()) ;
					if(!creating)
						hideKeyboard() ;
				}

			});
		} else {
			regionSpinner.setVisibility(View.GONE);
		}

		locationFetcher = new LocationFetcher(this, this) ;
		onGPSLocationDialog = new ProgressDialog(this) ;
		onGPSLocationDialog.setIndeterminate(true);
		onGPSLocationDialog.setTitle(R.string.location_searching_gps);
		onGPSLocationDialog.setMessage(getString(R.string.please_wait));
		onGPSLocationDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				locationFetcher.disableUpdates();
				gpsUpdating = false ;
			}
		});

		filterText = (EditText)findViewById(R.id.locations_filter) ;
		filterText.addTextChangedListener(this);
		filterText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					geocodeText() ;
					return true ;
				} else {
					return false ;
				}
			}
		});

		geocoder = new GeocoderAsync(this, this) ;
		geocodingDialog = new ProgressDialog(this) ;
		geocodingDialog.setTitle(R.string.location_geocoding);
		geocodingDialog.setMessage(getString(R.string.please_wait)) ;
		geocodingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {

			}
		});

		regionSpinner.setSelection(prefs.getInt(PREF_KEY_SAVED_REGION, 0));

		this.setResult(RESULT_CANCELED);
		listView.setOnItemClickListener(this);
		//hack because keyboard was being hidden somehow at the very beginning
		regionSpinner.postDelayed(new Runnable() {
			public void run() {
				creating = false ;
			}
		}, 200) ;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_picker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_gps) {
            if(checkLocationPermission()) {
                locationFetcher.enableUpdates();
                gpsUpdating = true ;
                onGPSLocationDialog.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] {"android.permission.ACCESS_FINE_LOCATION"}, REQUEST_LOCATION);
            }
			return true ;
		} else if(id == android.R.id.home) {
            this.onBackPressed();
            return true;
        } else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		log("clicked position " + position) ;
		if(position == 0) {
			if(searchOnClickHeader)
				this.geocodeText();
		} else {
			ForecastLocation l = locationsAdapter.getItem(position-1) ;
			Intent data = new Intent() ;
			data.setData(l.getUri()) ;
			this.setResult(RESULT_OK, data);
			this.finish();
		}
	}

	private void setSearchHeader(String value) {
		searchHeader.setText(value);
	}
	
	private void geocodeText() {
		String text = filterText.getText().toString() ;
		log("geocoding: " + text) ;
		geocoder.geocode(text);
		geocodingDialog.show();
	}

	@Override
	public void onPause() {
		super.onPause();
		if(locationFetcher != null)
			locationFetcher.disableUpdates();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(locationFetcher != null && gpsUpdating) {
            locationFetcher.enableUpdates();
        }
	}


	private class LocationsAdapter extends ArrayAdapter<ForecastLocation> {

		public LocationsAdapter() {
			super(LocationPickerActivity.this, android.R.layout.simple_list_item_1) ;
		}

		public void reset(List<? extends ForecastLocation> newList) {
			this.clear();
			this.addAll(latLon == null ? newList : sort(newList));
			listView.setAdapter(this);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v = (TextView) super.getView(position, convertView, parent);
			this.modifyTextView(this.getItem(position), v);
			return v ;
		}

		protected void modifyTextView(ForecastLocation object, TextView v) {
			String distText = "" ;
			if(latLon != null) {
				double dist = latLon.distanceTo(object.getLatLon()) ;
				String distString = new DecimalFormat("0").format(dist) ;
				distText = " (" + distString + " km)" ;
			}
			String text = object.toString(lang) + distText;
			v.setText(text);
		}

		private List<? extends ForecastLocation> sort(List<? extends ForecastLocation> list) {
			final List<? extends ForecastLocation> listCopy = new ArrayList<>(list);
			Collections.sort(listCopy, new Comparator<ForecastLocation>() {
				public int compare(final ForecastLocation o1, final ForecastLocation o2) {
					return Double.compare(latLon.distanceTo(o1.getLatLon()),
										  latLon.distanceTo(o2.getLatLon()));
				}
			});
			return listCopy;
		}
	}

	private class RegionAdapter extends CustomSpinnerAdapter<ForecastRegion> {

		public RegionAdapter() {
			super(LocationPickerActivity.this);
		}

		public ForecastRegion current() {
			if(regionSpinner != null) {
				int currentPos = regionSpinner.getSelectedItemPosition() ;
				if(currentPos >= 0) {
					return this.getItem(currentPos) ;
				} else {
					return null ;
				}
			} else {
				return null ;
			}
		}

		@Override
		protected void modifyTextView(ForecastRegion object, TextView v) {
			v.setText(object.getName(lang));
		}

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		//do nothing
	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String str = s.toString() ;
		if(str.trim().length() > 0) {
			latLon = null ;
			List<? extends ForecastLocation> filtered = locationDb.filterLocations(str) ;
			locationsAdapter.reset(filtered);
			this.setSearchHeader(String.format(getString(R.string.location_searchheader_format), str));
			searchOnClickHeader = true ;
		} else {
			ForecastRegion current = regionAdapter.current() ;
			if(current != null) {
				this.setByRegion(current);
			} else {
				if(locationsAdapter != null)
					locationsAdapter.clear();
				log("region is null! clearing locations adapter", null) ;
			}
		}
	}


	private void toast(int messageId) {
		Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
	}

	@Override
	public void afterTextChanged(Editable s) {
		//do nothing
	}


	@Override
	public void onLocationChange(LatLon position, int altitudeMetres, int accuracyMetres) {
		locationFetcher.disableUpdates();
		gpsUpdating = false ;
		onGPSLocationDialog.dismiss();
		this.setByLatLon(position);
		filterText.removeTextChangedListener(this);
		filterText.setText(R.string.location_my_location);
		filterText.addTextChangedListener(this);
		filterText.clearFocus();
		searchOnClickHeader = false ;
		this.setSearchHeader(String.format(getString(R.string.location_searchheader_showing), getString(R.string.location_my_location)));
	}

	private void setByRegion(ForecastRegion r) {
		latLon = null ;
		List<? extends ForecastLocation> newList = locationDb.getLocations(r.getRegionCode()) ;
		locationsAdapter.reset(newList);
		filterText.removeTextChangedListener(this);
		filterText.setText("");
		filterText.addTextChangedListener(this);
		searchOnClickHeader = false ;
		this.setSearchHeader(String.format(getString(R.string.location_searchheader_region), r.getName(lang)));
	}

	private void setByLatLon(LatLon location) {
		latLon = location ;
		List<? extends ForecastLocation> locs = locationDb.locationsNear(location) ;
		locationsAdapter.reset(locs);
		hideKeyboard() ;
	}

	private void setByAddress(Address address) {
		LatLon ll = latLonFrom(address) ;
        this.setByLatLon(ll);
        searchOnClickHeader = false ;
        this.setSearchHeader(String.format(getString(R.string.location_searchheader_showing), labelFrom(address)));
	}

	@Override
	public void onGeocode(List<Address> address) {
		if(address != null) {
			if(address.size() > 1) {
				log("multiple addresses! making did you mean dialog") ;
				this.showDidYouMeanDialog(address);
			} else if(address.size() == 1) {
				this.setByAddress(address.get(0));
			} else {
				toast(R.string.location_error_none_found) ;
				log("no locations found near address!", null) ;
			}
		} else {
			toast(R.string.location_error_none_found) ;
			log("no locations found near address!", null) ;
		}
		geocodingDialog.dismiss();
	}

	@Override
	public void onGeocodeError(Exception error) {
		toast(R.string.location_error_geocoding) ;
		log("error geocoding!", error) ;
		geocodingDialog.dismiss();
	}

	private static LatLon latLonFrom(Address address) {
		return new LatLon(address.getLatitude(), address.getLongitude()) ;
	}

	private static String labelFrom(Address address) {
		String out = "" ;
		String sep = "" ;
		for(int i=0; i<5; i++) {
			String line = address.getAddressLine(i) ;
			if(line != null) {
				out += sep + line ;
				sep = " " ;
			} else {
				break ;
			}	
		}

		if(out.trim().length() > 0) {
			return out ;
		} else {
			return null ;
		}
	}

	private void showDidYouMeanDialog(final List<Address> address) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this) ;
		builder.setTitle(R.string.location_did_you_mean) ;
		String[] list = new String[address.size()] ;
		for(int i=0; i<list.length; i++) {
			list[i] = labelFrom(address.get(i)) ;
		}
		builder.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setByAddress(address.get(which)) ;
			}
		}) ;
		builder.create().show();
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // enable updates
                    locationFetcher.enableUpdates();
                    gpsUpdating = true ;
                    onGPSLocationDialog.show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Cannot perform GPS lookup without GPS permission!",
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = ContextCompat.checkSelfPermission(this, permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    private void hideKeyboard() {
		log("hiding keyboard") ;
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(filterText.getWindowToken(), 0);
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
