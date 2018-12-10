package ca.fwe.caweather.radar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ca.fwe.caweather.R;
import ca.fwe.caweather.radar.RadarLocation.Overlays;
import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.util.Downloader;

public class RadarActivity extends AppCompatActivity implements OnClickListener {
	private static final String PREF_ANIMATION_LENGTH = "xml_radar_length" ;
	private static final String PREF_RADAR_PRECIPTYPE = "xml_radar_preciptype" ;
	private static final String PREF_SAVED_STATION = "radar_saved_station" ;
	private static final String PREF_OVERLAYS = "xml_radar_overlays" ;
	
	private static Set<String> PREF_OVERLAYS_DEFAULT = new HashSet<>() ;
	static {
		PREF_OVERLAYS_DEFAULT.add("ROADS") ;
		PREF_OVERLAYS_DEFAULT.add("CITIES") ;
	}

	private static final int FRAME_DELAY = 180 ;
	private static final int CYCLE_PAUSE = 500 ;

	public static final DateFormat DEFAULT_TIMEDATE = new SimpleDateFormat("d MMM h:mm a", Locale.CANADA) ;

	private Spinner locationsSpinner ;
	private LatLon latLon ;
	private ImageView image ;
	private RadarLocationAdapter adapter ;
    private ArrayAdapter<String> errorAdapter;
	private TextView dateText ;

	private RadarCacheManager cache ;

	private SeekBar animSeek ;
	private ProgressBar animLoading ;
	private ImageView playPause ;
	private RadarAnimator animator ;
	private Bitmap[] imageOverlays ;

	private LayerDrawable blankImageList ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(WeatherApp.getThemeId(this)) ;
		super.onCreate(savedInstanceState) ;		
		this.setContentView(R.layout.radar) ;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//set radar overlays pref key if not already set
		SharedPreferences prefs = WeatherApp.prefs(this) ;
		if(!prefs.contains(PREF_OVERLAYS)) {
			SharedPreferences.Editor edit = prefs.edit() ;
			edit.putStringSet(PREF_OVERLAYS, PREF_OVERLAYS_DEFAULT) ;
			edit.apply();
		}
		
		cache = new RadarCacheManager(this) ;

		blankImageList = (LayerDrawable)this.getResources().getDrawable(R.drawable.radar_overlay_template) ;

		locationsSpinner = findViewById(R.id.radar_locations) ;
		image = findViewById(R.id.radar_image) ;
		image.setImageDrawable(blankImageList) ;

		dateText = findViewById(R.id.radar_date) ;

		animator = new RadarAnimator() ;

		animSeek = findViewById(R.id.radar_seek) ;
		animLoading = findViewById(R.id.radar_loading) ;
		playPause = findViewById(R.id.radar_play_pause) ;
		playPause.setOnClickListener(this) ;

		animSeek.setOnSeekBarChangeListener(animator) ;

        errorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[] {getString(R.string.radar_error_no_stations)});
        adapter = new RadarLocationAdapter(new RadarLocation[] {});

		locationsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				RadarLocation l = adapter.current() ;
				setLocation(l, false) ;
				SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit() ;
				editor.putString(PREF_SAVED_STATION, l.getSiteId()) ;
				editor.apply() ;
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		}) ;

		ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.please_wait)) ;
		progress.setCancelable(true) ;
		progress.setTitle(getString(R.string.radar_fetch_list)) ;

		this.readData() ;
	}

	public void onPause() {
		super.onPause() ;
		animator.onActivityPause() ;
		if(isFinishing()) {
			if(animator.downloader != null) {
				if(animator.downloader.isDownloading) {
					animator.downloader.cancel(false) ;
				}
			}
		}
	}

	public void onResume() {
		super.onResume() ;
		animator.onActivityResume() ;
	}

	public void onDestroy() {
		animator.pause() ;
		if(isFinishing()) {
			if(animator.downloader != null) {
				if(animator.downloader.isDownloading) {
					animator.downloader.cancel(false) ;
				}
			}
		}
		super.onDestroy() ;
	}

	private void readData() {

		Uri data = this.getIntent().getData() ;
		if(data != null && data.getScheme() != null) {
			String lat = data.getQueryParameter("lat") ;
			String lon = data.getQueryParameter("lon") ;
			String near = data.getQueryParameter("near") ;
			if(lat != null && lon != null) {
				try {
					double latd = Double.valueOf(lat) ;
					double lond = Double.valueOf(lon) ;
					this.setByLocation(new LatLon(latd, lond), near);
				} catch(NumberFormatException e) {
					Log.e("Radar", "number format exception on lat/lon") ;
					setToast(R.string.radar_error_uri) ;
				}
			} else if(data.getScheme().equals("radar")) {
				//set by specific radar code
				List<String> pathParts = data.getPathSegments() ;
				if(pathParts != null && pathParts.size() == 2 && pathParts.get(0).equals("ca")) {
					RadarLocation l = RadarLocations.get(pathParts.get(1)) ;
					if(l != null) {
						RadarLocation[] list = RadarLocations.ALL ;
						adapter = new RadarLocationAdapter(list) ;
						locationsSpinner.setAdapter(adapter);

						int position = adapter.getPosition(l) ;
						if(position >= 0) {
							locationsSpinner.setSelection(position);
						} else {
							//the location is somehow not anywhere in the master list
							Log.e("Radar", "can't find perfectly valid location in list: " + l.getSiteId()) ;
							setToast(R.string.radar_error_uri) ;
						}
					} else {
						//valid uri with unknown radar code
						Log.e("Radar", "invalid URI " + data) ;
						setToast(R.string.radar_error_uri) ;
					}
				} else {
					//invalid URI
					Log.e("Radar", "invalid URI " + data) ;
					setToast(R.string.radar_error_uri) ;
				}
			} else {
				Log.e("Radar", "invalid URI " + data) ;
				setToast(R.string.radar_error_uri) ;
			}
		} else {
			Log.i("Radar", "no data passed to activity, launching standalone") ;
			this.loadAllRadarLocations();
		}

	}

	private void loadAllRadarLocations() {
		RadarLocation[] list = RadarLocations.ALL ;
		adapter = new RadarLocationAdapter(list) ;
		locationsSpinner.setAdapter(adapter);
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE) ;
		if(prefs.contains(PREF_SAVED_STATION)) {
			String savedCode = prefs.getString(PREF_SAVED_STATION, null) ;
			RadarLocation l = RadarLocations.get(savedCode) ;
			if(l != null) {
				int position = adapter.getPosition(l) ;
				if(position >= 0)
					locationsSpinner.setSelection(position);
			}
		}
	}

	private int getAnimationLength(RadarLocation l) {
		// value here is number of 10-minute frames
		// need to scale based on location.getUpdateFrequency()
		SharedPreferences prefs = WeatherApp.prefs(this) ;
		String length = prefs.getString(PREF_ANIMATION_LENGTH, "7") ;
		try {
			return Integer.valueOf(length) * 10 / l.getUpdateFrequency() ;
		} catch(NumberFormatException e) {
			return 7 * 10 / l.getUpdateFrequency() ;
		}
	}

	private RadarImageType getImageType() {
		String precipType = WeatherApp.prefs(this).getString(PREF_RADAR_PRECIPTYPE, "AUTO") ;
		if(precipType.equals("AUTO")) {
			Calendar cal = Calendar.getInstance() ;
			int month = cal.get(Calendar.MONTH) ;
			if(month >= Calendar.APRIL)
				precipType = RadarImageType.PRECIP_TYPE_RAIN ;
			else
				precipType = RadarImageType.PRECIP_TYPE_SNOW ;
		}
		return new RadarImageType(RadarImageType.PRODUCT_PRECIP, precipType, null) ;
	}

	private Bitmap[] loadOverlays(final RadarLocation l) {
		SharedPreferences prefs = WeatherApp.prefs(this) ;
		Set<String> overlayNames = prefs.getStringSet(PREF_OVERLAYS, PREF_OVERLAYS_DEFAULT) ;
		List<RadarLocation.Overlays> overlays = new ArrayList<>() ;
		for(String s: overlayNames) {
			try {
				overlays.add(RadarLocation.Overlays.valueOf(s)) ;
			} catch(IllegalArgumentException e) {
				//should never happen
				Log.e("Radar", "tried to add overlay " + s + ", which doesn't exist") ;
			}
		}
		
		Collections.sort(overlays, new Comparator<RadarLocation.Overlays>() {
			public int compare(Overlays lhs, Overlays rhs) {
				int pos1 = RadarLocation.getOverlayPosition(lhs) ;
				int pos2 = RadarLocation.getOverlayPosition(rhs) ;
				if(pos1 > pos2) {
					return 1 ;
				} else if(pos1 < pos2) {
					return -1 ;
				} else {
					return 0 ;
				}
			}
		});
		
		try {
			Bitmap[] out = new Bitmap[overlays.size()] ;
			for(int i=0; i<out.length; i++) {
				InputStream in = getAssets().open(l.getOverlayAssetName(overlays.get(i))) ;
				out[i] = BitmapFactory.decodeStream(in) ;
				in.close() ;
			}
			return out ;
		} catch(IOException e) {
			Log.e("Radar", "error loading overlays", e) ;
			return new Bitmap[0] ;
		}

	}

	private LayerDrawable buildLayerDrawable(Bitmap image, Bitmap[] overlays) {
		BitmapDrawable[] drawables = new BitmapDrawable[overlays.length + 1] ;
		drawables[0] = new BitmapDrawable(this.getResources(), image) ;
		for(int i=0; i<overlays.length; i++) {
			BitmapDrawable bd = new BitmapDrawable(this.getResources(), overlays[i]) ;
			drawables[1 + i] = bd ; 
		}
		LayerDrawable ld = new LayerDrawable(drawables) ;
		for(int i=1; i<drawables.length; i++) {
			ld.setLayerInset(i, 0, 0, 0, 0) ;
		}
		return ld ;
	}

	private void setByLocation(LatLon ll, String near) {
		this.latLon = ll ;
		if(near == null)
			near = "location" ;
		this.setTitle(getString(R.string.radar_title) + " " + near) ;
		RadarLocation[] list = RadarLocations.filter(ll, 3) ;
		if(list.length > 0) {
			adapter = new RadarLocationAdapter(list) ;
			locationsSpinner.setAdapter(adapter) ;
			SharedPreferences prefs = this.getPreferences(MODE_PRIVATE) ;
			if(prefs.contains(PREF_SAVED_STATION)) {
				String savedCode = prefs.getString(PREF_SAVED_STATION, null) ;
				RadarLocation l = RadarLocations.get(savedCode) ;
				if(l != null) {
					int position = adapter.getPosition(l) ;
					if(position >= 0)
						locationsSpinner.setSelection(position);
				}
			}
		} else {
			locationsSpinner.setOnItemSelectedListener(null) ;
			locationsSpinner.setAdapter(errorAdapter) ;
		}
	}

	private void setLocation(final RadarLocation l, boolean refresh) {
		animator.pause();
		animator.setImages(null, false) ;
		imageOverlays = this.loadOverlays(l) ;
		RadarImageList list = RadarImageList.getMostRecent(l, this.getImageType(), this.getAnimationLength(l));
		animator.setImages(list, refresh);
	}

	private String generateDateString(Date d) {
		return getString(R.string.radar_image_date) + " " + DEFAULT_TIMEDATE.format(d) ;
	}

	private void setBitmap(Drawable d) {
		if(d != null) {
			image.setImageDrawable(d) ;
		} else {
			image.setImageDrawable(blankImageList) ;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.radar_play_pause) {
            if(adapter.current() != null) {
                if (animator.downloader.isDownloading) {
                    animator.downloader.cancel(false);
                } else if (animator.playing) {
                    animator.pause();
                } else {
                    animator.play();
                }
            }
		}

	}

	private void setToast(int id) {
		Toast.makeText(this, id, Toast.LENGTH_LONG).show() ;
	}

    private static class RadarListDownloader extends AsyncTask<ArrayList<RadarImage>, Integer, Boolean> {

        Downloader d ;
        boolean playWhenFinished = true ;
        boolean isDownloading = false ;
        boolean latestImageSet = false ;
        boolean refresh;

        RadarAnimator animator;
        // this is possibly causing crashes, but is a battle for another day
        // currently this field is set to null on cancel (which in turn
        // is called from onPause() and onDestroy() which should prevent
        // the memory leak issue, but I don't know enough about the workings of
        // android to say for sure
        RadarActivity activity;

        RadarListDownloader(RadarAnimator animator, RadarActivity activity, boolean refresh) {
            this.animator = animator;
            this.activity = activity;
            this.refresh = refresh;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int val = values[0] ;
            if(val == -1) {
                animator.setTo(animator.size - 1, false, true) ;
                latestImageSet = true ;
            } else {
                activity.animSeek.setSecondaryProgress(val) ;
                if(val == 0 && !latestImageSet) animator.setTo(val, false, true) ;
            }
        }

        @Override
        protected void onCancelled() {
            if(d != null)  d.cancel() ;
            if(activity != null) {
                activity.animLoading.setVisibility(View.GONE);
                activity.playPause.setImageResource(R.drawable.ic_play);
            }
            // release references to activity and animator on cancel
            activity = null;
            animator = null;
            isDownloading = false ;
        }



        @Override
        protected void onPreExecute() {
            activity.setToast(R.string.radar_loading_images) ;
            activity.playPause.setImageResource(R.drawable.ic_stop) ;
            activity.animLoading.setVisibility(View.VISIBLE) ;
        }

        @Override
        @SafeVarargs
        protected final Boolean doInBackground(ArrayList<RadarImage>... arg0) {
            isDownloading = true ;
            List<RadarImage> list = arg0[0] ;

            RadarImage mostRecent = list.get(list.size() - 1) ;
            if(download(mostRecent))
                this.publishProgress(-1) ;

            for(int i=0; i<list.size(); i++) {
                if(this.isCancelled())
                    break ;

                RadarImage link = list.get(i) ;
                download(link) ;
                publishProgress(i) ;
            }
            isDownloading = false ;
            return true ;
        }

        private boolean download(RadarImage link) {
            if(activity == null || animator == null) return false;

            File tmpFile = activity.cache.getRadarCacheFile(link.getFilename()) ;
            if(!tmpFile.exists() || refresh) {
                d = new Downloader(link.getLink(), tmpFile) ;

                try {
                    d.download() ;
                } catch (IOException e) {
                    if(!tmpFile.delete()) Log.i("Radar", "Could not delete file " + tmpFile);
                    Log.e("Radar", "Error downloading image " + link.getFilename()) ;
                }
            }

            if(tmpFile.exists()) {
                Bitmap b = BitmapFactory.decodeFile(tmpFile.getPath()) ;
                animator.setDrawable(link, b) ;
                return true ;
            } else {
                //error
                Log.e("Radar", "error, tmp file doesn't exist or IO exception occurred") ;
                return false ;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(activity != null && animator != null) {
                activity.animLoading.setVisibility(View.GONE);
                activity.playPause.setImageResource(R.drawable.ic_play);
                if(playWhenFinished) animator.play() ;
            }
            activity = null;
            animator = null;
        }
    }

    private class RadarLocationAdapter extends ArrayAdapter<RadarLocation> {

		RadarLocationAdapter(RadarLocation[] objects) {
			super(RadarActivity.this, android.R.layout.simple_spinner_item, objects);
			this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) ;
		}

		@NonNull
        @Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			TextView v = (TextView)super.getView(position, convertView, parent);
			RadarLocation l = this.getItem(position) ;
			int lang = WeatherApp.getLanguage(RadarActivity.this);
			if(l == null) {
			    return v;
            }

			if(latLon != null) {
                double distance = latLon.distanceTo(l.getLocation()) ;
				String distString = l.getAlias(lang) + " (" + l.getName(lang) + ")" +
                        " - " + Long.valueOf(Math.round(distance)).toString() + "km" ;
				v.setText(distString) ;
			} else {
				String text = l.getAlias(lang) + " (" + l.getName(lang) + ")" ;
				v.setText(text) ;
			}
			return v ;
		}

		@Override
		public View getDropDownView(int position, View convertView,
                                    @NonNull ViewGroup parent) {

			TextView v = (TextView)super.getDropDownView(position, convertView, parent);
			RadarLocation l = this.getItem(position) ;
            int lang = WeatherApp.getLanguage(RadarActivity.this);
            if(l == null) {
                return v;
            }

			if(latLon != null) {
				double distance = latLon.distanceTo(l.getLocation()) ;
                String distString = l.getAlias(lang) + " (" + l.getName(lang) + ")" +
                        " - " + Long.valueOf(Math.round(distance)).toString() + "km" ;
				v.setText(distString) ;
			} else {
				String text = l.getAlias(lang) + " (" + l.getName(lang) + ")" ;
				v.setText(text) ;
			}
			return v ;

		}

		RadarLocation current() {
            int ind = locationsSpinner.getSelectedItemPosition();
            if(ind >= 0 && ind < this.getCount()) {
                return this.getItem(ind);
            } else {
                return null;
            }
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        RadarLocation loc = adapter.current();
		if (item.getItemId() == R.id.menu_refresh) {
            if(loc != null) {
                this.setLocation(loc, true);
            }
			return true ;
		} else if (item.getItemId() == R.id.menu_view_online) {
            if(loc != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                String url = loc.getMobileURL(WeatherApp.getLanguage(this));
                i.setData(Uri.parse(url));
                try {
                    this.startActivity(i);
                } catch (ActivityNotFoundException e) {
                    this.setToast(R.string.forecast_error_no_browser);
                }
            }
			return true ;
		} else if (item.getItemId() == R.id.menu_prefs) {
			Intent i = new Intent(this, RadarPrefs.class) ;
			startActivity(i) ;
			return true ;
		} else if(item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        } else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.radar, menu) ;
		return true;
	}

	private class RadarAnimator implements SeekBar.OnSeekBarChangeListener {


		int currentIndex = 0 ;
		int size = 0 ;
		Timer timer = new Timer() ;
		TimerTask timerTask ;

		ArrayList<RadarImage> currentList ;
		Drawable[] drawables ;

		RadarListDownloader downloader ;

		boolean pausedForRepeat = false ;

		boolean playing = false ;

		void play() {
			if(!playing) {
				if(timer == null)
					timer = new Timer() ;
				playing = true ;
				timerTask = getTask() ;
				timer.scheduleAtFixedRate(timerTask, 0, FRAME_DELAY) ;
				playPause.setImageResource(R.drawable.ic_pause) ;
			}
		}

		void onActivityPause() {
			if(playing) {
				if(timerTask != null)
					timerTask.cancel() ;
			}
			if(timer != null) {
				timer.purge() ;
				timer.cancel() ;
				timer = null ;
			}
		}

		void onActivityResume() {
			if(playing) {
				if(timer == null)
					timer = new Timer() ;
				timerTask = getTask() ;
				timer.scheduleAtFixedRate(timerTask, 0, FRAME_DELAY) ;
			}
		}

		private TimerTask getTask() {
			return new TimerTask() {
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							if(!isLast() || (pausedForRepeat)) {
								Log.i("Radar", "animation advancing") ;
								pausedForRepeat = false ;
								forward(false) ;
							} else {
								pausedForRepeat = true ;
								timerTask.cancel() ;
								timerTask = getTask() ;
								if(playing && timer != null) {
									timer.scheduleAtFixedRate(timerTask, CYCLE_PAUSE, FRAME_DELAY) ;
									Log.i("Radar", "animation pausing for " + CYCLE_PAUSE) ;
								}
							}
						}
					}) ;
				}
			} ;
		}

		void pause() {
			if(timerTask != null)
				timerTask.cancel() ;
			if(timer != null)
				timer.purge() ;
			playing = false ;
			playPause.setImageResource(R.drawable.ic_play) ;
		}

		void forward(boolean fromUI) {
			if(isLast())
				currentIndex = 0 ;
			else
				currentIndex++ ;
			setTo(currentIndex, fromUI, true) ;
		}

		public void back() {
			if(isFirst())
				currentIndex = size - 1 ;
			else
				currentIndex-- ;
			setTo(currentIndex, true, true) ;
		}

		boolean isFirst() {
            return currentIndex == 0;
		}

		boolean isLast() {
            return currentIndex == (size - 1);
		}

		void setTo(int index, boolean fromUI, boolean updateProgress) {
			if(fromUI) {
				pause() ;
				if(downloader != null)
					downloader.playWhenFinished = false ;
			}

			if(index >= 0 && index < size) {

				RadarImage link = currentList.get(index) ;
				Drawable d = drawables[index] ;
				currentIndex = index ;
				if(updateProgress)
					animSeek.setProgress(index) ;
				if(link != null) {
					dateText.setText(generateDateString(link.getImageDate()));
				} else {
                    dateText.setText(R.string.radar_no_image);
                }

				if(d != null) {
					setBitmap(d) ;
				} else {
					setBitmap(null) ;
				}
			} else {
				Log.e("Radar", "setTo() called index out of bounds") ;
				pause() ;
			}
		}

		void setImages(RadarImageList list, boolean refresh) {
			if(downloader != null)
				downloader.cancel(false) ;
			pause() ;

			currentList = new ArrayList<>() ;

			drawables = null ;
			currentIndex = 0 ;
			size = 0 ;

			if(list != null) {
				animSeek.setOnSeekBarChangeListener(this) ;

				size = list.size() ;
				drawables = new Drawable[size] ;
				animSeek.setMax(size-1) ;

				for(int i=0; i<size; i++) {
					currentList.add(list.get(i)) ;
				}

				downloader = new RadarListDownloader(this, RadarActivity.this, refresh) ;
				downloader.execute(currentList) ;
			} else {
				setBitmap(null) ;
				dateText.setText(R.string.radar_no_image) ;
				animSeek.setOnSeekBarChangeListener(null) ;
				animSeek.setSecondaryProgress(0) ;
				animSeek.setProgress(0) ;
				animSeek.setMax(0) ;
			}
		}

		private int setDrawable(RadarImage link, Bitmap b) {
			int index = currentList.indexOf(link) ;
			if(index != -1) {
				drawables[index] = buildLayerDrawable(b, imageOverlays) ;
				return index ;
			} else {
				return -1 ;
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(fromUser)
				setTo(progress, true, false) ;
		}
		public void onStartTrackingTouch(SeekBar seekBar) {}
		public void onStopTrackingTouch(SeekBar seekBar) {}

    }
}
