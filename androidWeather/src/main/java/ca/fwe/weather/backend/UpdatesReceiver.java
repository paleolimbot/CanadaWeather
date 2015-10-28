package ca.fwe.weather.backend;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.Units;
import ca.fwe.weather.core.Units.UnitSet;
import ca.fwe.weather.util.ForecastDownloader;
import ca.fwe.weather.util.ForecastDownloader.Modes;

public abstract class UpdatesReceiver extends BroadcastReceiver {

	private static final String ACTION_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED" ;
	private static final String ACTION_NETWORK_CONNECTED = "android.net.conn.CONNECTIVITY_CHANGE" ;
	public static final String ACTION_ENSURE_UPDATED = "ca.fwe.weather.ENSURE_UPDATED" ;
	public static final String ACTION_FORCE_UPDATE_ALL = "ca.fwe.weather.FORCE_UPDATE_ALL" ;

	@Override
	public void onReceive(Context context, Intent intent) {
		log("receiving intent with action " + intent.getAction()) ;
		if(intent.getAction().equals(ACTION_BOOT_COMPLETE) ||
				intent.getAction().equals(ACTION_FORCE_UPDATE_ALL)) {
			this.startForecastDownloader(context, Modes.FORCE_DOWNLOAD, true);
			//and set alarm, which will also send ACTION_ENSURE_UPDATED broadcast
			this.setOrCancelUpdatePendingIntent(context);
		} else if(intent.getAction().equals(ACTION_ENSURE_UPDATED)) {
			this.startForecastDownloader(context, Modes.LOAD_RECENT_CACHE_OR_DOWNLOAD, true);
		} else if(intent.getAction().equals(ACTION_NETWORK_CONNECTED)) {
			this.setOrCancelUpdatePendingIntent(context);
		}
	}

	public void startForecastDownloader(Context context, Modes downloadMode, boolean forceBroadcast) {
		UpdatesManager manager = new UpdatesManager(context) ;
		List<Uri> uris = manager.getAllUpdateUris() ;
		LocationDatabase locDb = this.getLocationDatabase(context) ;
		log("ensuring " + uris.size() + " widgets/notifications are up to date") ;
		UnitSet unitset = Units.getUnitSet(WeatherApp.prefs(context).getString(WeatherApp.PREF_KEY_UNITS, Units.UNITS_DEFAULT)) ;
		
		for(Uri uri: uris) {
			ForecastLocation l = locDb.getLocation(uri) ;
			Forecast f = new Forecast(context, l, WeatherApp.getLanguage(context)) ;
			f.setUnitSet(unitset);
			ForecastDownloader d = new ForecastDownloader(f, null, downloadMode) ; //null listener to skip parsing
			d.setBroadcastOnLoad(forceBroadcast);
			d.download();
		}
	}
	
	public void setOrCancelUpdatePendingIntent(Context context) {
		log("setting or cancelling update pending intent") ;
		UpdatesManager manager = new UpdatesManager(context) ;
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE) ;
		List<Uri> list = manager.getAllUpdateUris() ;
		
		FilesManager fm = new FilesManager(context) ;
		int updateFrequency = (int)fm.getForecastValidAge() ;
		
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE) ;
		NetworkInfo ni= cm.getActiveNetworkInfo() ;
		if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED && list.size() > 0) {
			log("Network " + ni.getTypeName() + " connected, setting alarm.");
			am.setRepeating(AlarmManager.RTC_WAKEUP, 0, updateFrequency, 
					getPendingIntent(context));		} else if(list.size() == 0){
			log("nothing to set alarm for, cancelling") ;
			am.cancel(this.getPendingIntent(context));
		} else {
			log("network not connected, cancelling") ;
			am.cancel(this.getPendingIntent(context));
		}
	}

	private PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(ACTION_ENSURE_UPDATED) ;
		intent.setPackage(context.getPackageName()) ;
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT) ;
	}
	
	protected abstract LocationDatabase getLocationDatabase(Context context) ;

	private static void log(String message) {
		Log.i("UpdatesReceiver", message) ;
	}

}
