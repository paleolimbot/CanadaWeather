package ca.fwe.weather;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.backend.ForecastXMLParser;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.backend.UserLocationsList;
import ca.fwe.weather.core.CurrentConditions;
import ca.fwe.weather.core.PointInTimeForecast;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.Units;
import ca.fwe.weather.core.Units.UnitSet;
import ca.fwe.weather.core.WeatherWarning;
import ca.fwe.weather.util.ForecastDownloader;
import ca.fwe.weather.util.ForecastDownloader.Modes;
import ca.fwe.weather.util.ForecastDownloader.ReturnTypes;

public abstract class ForecastActivity extends AppCompatActivity implements ForecastDownloader.OnForecastDownloadListener,
																		OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
	private static final String TAG = "ForecastActivity" ;

    private DrawerLayout drawer ;
    protected LocationsAdapter locationsAdapter ;
	protected UserLocationsList userLocations ;
	protected LocationDatabase locationDatabase ;

	private static final String PREF_KEY_LAST_LOC_POS = "last_location_position" ;
	
	protected static final int REQUEST_LOCATION = 11 ;
	
	private ForecastDownloader downloader = null ;
	private ProgressDialog onDownloadDialog = null ;
    private boolean onDownloadDialogShowing = false;
	
	protected WeatherApp app ;
	private ForecastAdapter forecastAdapter ;
	private NoDataAdapter noDataAdapter ;
	protected int lang ;
	private SharedPreferences prefs ;
    private ListView listView;
    private SwipeRefreshLayout swipeLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(WeatherApp.getThemeId(this)) ;
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.forecast);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		app = (WeatherApp) this.getApplication() ;
		prefs = WeatherApp.prefs(this) ;
		lang = WeatherApp.getLanguage(this) ;
        drawer = this.findViewById(R.id.forecast_drawer_layout);
        ListView locationSpinner = findViewById(R.id.forecast_left_drawer);
		forecastAdapter = new ForecastAdapter(this) ;
		noDataAdapter = new NoDataAdapter() ;

        swipeLayout = findViewById(R.id.forecast_swiperefresh_container);
        swipeLayout.setOnRefreshListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

		onDownloadDialog = new ProgressDialog(this) ;
		onDownloadDialog.setMessage(getString(R.string.please_wait));
		onDownloadDialog.setTitle(R.string.forecast_loading_forecast);
		onDownloadDialog.setIndeterminate(true);
		onDownloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (downloader != null) {
                    downloader.cancel();
                }

            }
        });
        onDownloadDialogShowing = false;

        listView = findViewById(android.R.id.list);
        listView.setOnItemClickListener(this) ;

		if(app.upgrade()) {
			this.onUpgradeTrue();
		}

		locationDatabase = app.getLocationDatabase(this) ;
		userLocations = new UserLocationsList(locationDatabase) ;
		locationsAdapter = new LocationsAdapter(this) {
            public void onListItemClick(ForecastLocation l, int position) {
                setLocationByIndex(position);
                drawer.closeDrawer(GravityCompat.START);
            }
        } ;

        int addId = getThemeResource(R.attr.ic_action_add_themedep);
        int editId = getThemeResource(R.attr.ic_action_edit_themedep);

        locationsAdapter.addAction(new ActionForecastLocation(getString(R.string.menu_add),
                getResources().getDrawable(addId)) {
            public void doAction() {
                launchLocationPicker();
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        locationsAdapter.addAction(new ActionForecastLocation(getString(R.string.menu_edit),
                getResources().getDrawable(editId)) {
            public void doAction() {
                makeLocationsEditor();
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        //set background of location drawer to that of windowBackground of theme
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            locationSpinner.setBackgroundColor(a.data);
        } else {
            // windowBackground is not a color, probably a drawable
            locationSpinner.setBackground(this.getResources().getDrawable(a.resourceId));
        }

        locationSpinner.setOnItemClickListener(locationsAdapter);
		locationsAdapter.refreshLocations();
			
		Intent launchIntent = this.getIntent() ;
		if(launchIntent != null && launchIntent.getData() != null) {
			locationsAdapter.addUri(launchIntent.getData());
		} else {
			this.setSpinnerPositionFromPref();
		}

        locationSpinner.setAdapter(locationsAdapter);
		
	}

    @Override
    public void onRefresh() {
        ForecastLocation current = this.getCurrentLocation() ;
        if(current != null) {
            this.setLocation(current, true);
        } else {
            toast(R.string.forecast_error_no_location_selected) ;
            log("trying to refresh location when there is no location!", null) ;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(onDownloadDialogShowing) {
            onDownloadDialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(onDownloadDialogShowing) {
            onDownloadDialog.dismiss();
        }
    }

    protected abstract void onUpgradeTrue() ;
	
	private void setSpinnerPositionFromPref() {
		if(locationsAdapter.getNumLocations() > 0) {
			int lastPos = prefs.getInt(PREF_KEY_LAST_LOC_POS, 0) ;
			if(lastPos >= locationsAdapter.getNumLocations())
				lastPos = locationsAdapter.getNumLocations() - 1 ;
			setLocationByIndex(lastPos);
		} else {
            this.setLocation(null, false);
			this.launchLocationPicker();
		}
	}
	
	protected void launchLocationPicker() {
		Intent i = new Intent(this, LocationPickerActivity.class) ;
		this.startActivityForResult(i, REQUEST_LOCATION);
	}
	
	protected abstract void launchPreferenceActivity() ;

    private void setLocationByIndex(int index) {
        ForecastLocation l = locationsAdapter.getItem(index);
        locationsAdapter.setSelectedIndex(index);
        setLocation(l, false);
        Editor e = prefs.edit();
        e.putInt(PREF_KEY_LAST_LOC_POS, index);
        e.apply();
        locationsAdapter.notifyDataSetChanged();
    }

	protected void setLocation(ForecastLocation l, boolean forceDownload) {
        if (l == null) {
            listView.setAdapter(noDataAdapter);
            forecastAdapter.clear();
            try {
                this.setTitle(this.getPackageManager()
                        .getActivityInfo(this.getComponentName(),
                                PackageManager.GET_META_DATA).labelRes);
            } catch (PackageManager.NameNotFoundException e) {
                this.setTitle("");
            }
        } else {
            log("setting location to " + l.getUri());
            if (downloader != null)
                downloader.cancel();

            this.setTitle(l.toString(lang));

            UnitSet unitset = Units.getUnitSet(prefs.getString(WeatherApp.PREF_KEY_UNITS, Units.UNITS_DEFAULT));

            //load cached version first
            Forecast cachedF = new Forecast(this, l, lang);
            cachedF.setUnitSet(unitset);
            ForecastDownloader cachedLoader = new ForecastDownloader(cachedF, this, Modes.LOAD_CACHED, true);
            log("starting cached forecast loader");
            cachedLoader.download();

            Forecast forecast = new Forecast(this, l, lang);
            forecast.setUnitSet(unitset);
            Modes mode = Modes.LOAD_RECENT_CACHE_OR_DOWNLOAD;
            if (forceDownload)
                mode = Modes.FORCE_DOWNLOAD;

            downloader = new ForecastDownloader(forecast, this, mode, true);
            log("starting downloader");
            downloader.download();
            onDownloadDialog.show();
            onDownloadDialogShowing = true;
            swipeLayout.setRefreshing(true);
        }
		
	}

	@Override
	public void onForecastDownload(Forecast forecast, Modes mode, ReturnTypes result) {
		log("onForecastDownload returned with result " + result + " from mode " + mode) ;
		if(!mode.equals(Modes.LOAD_CACHED)) {
            onDownloadDialogShowing = false;
			onDownloadDialog.dismiss();
            swipeLayout.setRefreshing(false);
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
			listView.setAdapter(noDataAdapter);
			this.forecastAdapter.clear();
			return ;
		default:
			log("forecast downloaded sucessfully, setting adapter.") ;
			forecastAdapter.setForecast(forecast);
			listView.setAdapter(forecastAdapter);
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
		if(forecastAdapter.getCount() > position && position >= 0) {
			this.onItemClick(forecastAdapter.getItem(position));
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
        if(id == android.R.id.home) {
            if(drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
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
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                int savedPosition = prefs.getInt(PREF_KEY_LAST_LOC_POS, 0);
                for (int i = count - 1; i >= 0; i--) {
                    if (!selection[i]) {
                        ForecastLocation deleted = locations.get(i);
                        userLocations.removeLocations(deleted);
                        if (i + 1 <= savedPosition) //i+1 because of MyLocation
                            savedPosition--;
                    }
                }
                Editor e = prefs.edit();
                e.putInt(PREF_KEY_LAST_LOC_POS, savedPosition);
                e.apply();
                locationsAdapter.refreshLocations();
                setSpinnerPositionFromPref();
            }
        }) ;
		builder.setNegativeButton(R.string.cancel, null) ;
		builder.setMultiChoiceItems(locs, selection, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selection[which] = isChecked;
            }
        }) ;
		builder.create().show() ;
	}

    private int getThemeResource(int attr){
        TypedValue typedvalueattr = new TypedValue();
        getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    private class LocationsAdapter extends CustomSpinnerAdapter<ForecastLocation> implements OnItemClickListener {

        private int selectedIndex = -1;
        private int selectedBg ;
        private Drawable selectedBgDrw;

        private List<ActionForecastLocation> actions ;

		LocationsAdapter(Context context) {
			super(context);
            TypedValue a = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.colorPressedHighlight, a, true);
            if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                // activatedBackgroundIndicator is a color
                selectedBg = a.data;
            } else {
                // colorPressedHighlight is not a color, probably a drawable
                selectedBgDrw = getResources().getDrawable(a.resourceId);
            }

            actions = new ArrayList<>();

		}

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position >= 0 && position < this.getNumLocations()) {
                this.onListItemClick(this.getItem(position), position);
            } else {
                this.actions.get(position - this.getNumLocations()).doAction();
            }
        }

        protected void onListItemClick(ForecastLocation l, int position) {

        }

		@Override
		protected void modifyTextView(ForecastLocation l, TextView v) {
			v.setText(l.toString(lang)) ;
            if(l.equals(this.current())) {
                if (selectedBgDrw != null) {
                    v.setBackground(selectedBgDrw);
                } else {
                    v.setBackgroundColor(selectedBg);
                }
            } else {
                v.setBackgroundColor(Color.TRANSPARENT);
            }

            boolean isAction = false;
            for(ActionForecastLocation al: actions) {
                if(al.equals(l)) {
                    //apply icon
                    v.setCompoundDrawables(al.getIcon(), null, null, null);
                    v.setCompoundDrawablePadding(10);
                    isAction = true;
                    break;
                }
            }
            if(!isAction) {
                //un-apply icon
                v.setCompoundDrawables(null, null, null, null);
            }


		}

		void addUri(Uri uri) {
			log("adding uri to userLocations " + uri) ;
			ForecastLocation newLoc = userLocations.addLocation(uri) ;
			if(newLoc != null) {
				this.refreshLocations();
				int newIndex = this.getPosition(newLoc) ;
				if(newIndex != -1) {
					setLocationByIndex(newIndex);
				} else {
					toast(R.string.forecast_error_add_location) ;
					log("added location, but couldn't find it in the list", null);
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

            for(ActionForecastLocation l: actions) {
                this.add(l);
            }
		}

        void addAction(ActionForecastLocation l) {
            actions.add(l);
            this.notifyDataSetChanged();
        }

        int getNumLocations() {
            return this.getCount() - actions.size();
        }

        private void setSelectedIndex(int index) {
            this.selectedIndex = index;
        }

		ForecastLocation current() {
            if(selectedIndex > -1 && selectedIndex < this.getNumLocations()) {
                return this.getItem(selectedIndex) ;
            } else {
                return null ;
            }
		}
		
	}

	private class NoDataAdapter extends ArrayAdapter<String> {

		NoDataAdapter() {
			super(ForecastActivity.this, android.R.layout.simple_list_item_1);
			this.add(getString(R.string.forecast_no_data));
		}
		
	}

    public abstract class ActionForecastLocation extends ForecastLocation {

        private String text ;
        private Drawable icon ;

        ActionForecastLocation(String text, Drawable icon) {
            this.text = text ;
            this.icon = icon ;
            this.icon.setBounds(0,0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        }

        public abstract void doAction() ;

        public Drawable getIcon() {
            return icon;
        }

        public Uri getUri() {
            return Uri.parse("dummyforecastitem://" + Uri.encode(text));
        }
        public LatLon getLatLon() {
            return null;
        }
        public String getName(int lang) {
            return null;
        }
        public String getXmlUrl(int lang) {
            return null;
        }
        public ForecastXMLParser getXMLParser(Forecast forecast, File file) {
            return null;
        }
        public String getMobileUrl(int lang) {
            return null;
        }
        public String getWebUrl(int lang) {
            return null;
        }
        public String getCacheFileName(int lang) {
            return null;
        }
        public String toString(int lang) {
            return text ;
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
