package ca.fwe.weather.backend;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import ca.fwe.weather.R;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.WeatherWarning;
import ca.fwe.weather.util.ForecastDownloader;
import ca.fwe.weather.util.ForecastDownloader.Modes;
import ca.fwe.weather.util.ForecastDownloader.ReturnTypes;

public abstract class NotificationsReceiver extends BroadcastReceiver implements ForecastDownloader.OnForecastDownloadListener {

	public static final String ACTION_NOTIFICATION_REMOVED = "ca.fwe.weather.NOTIFICATION_REMOVED" ;

	@Override
	public void onReceive(Context context, Intent intent) {
		log("Receiving broadcast with action " + intent.getAction()) ;
		if(intent.getAction().equals(ForecastDownloader.ACTION_FORECAST_DOWNLOADED)) {
			Uri data = intent.getData() ;
			if(data != null) {
				UpdatesManager manager = new UpdatesManager(context) ;
				if(manager.notificationsEnabled(data)) {
					//parse forecast
					LocationDatabase db = this.getLocationDatabase(context) ;
					ForecastLocation l = db.getLocation(data) ;
					int lang = WeatherApp.getLanguage(context) ;
					if(l != null) {
						Forecast forecast = new Forecast(context, l, lang) ;
						//not setting unit set because it is irrelevant here
						ForecastDownloader d = new ForecastDownloader(forecast, this, Modes.LOAD_CACHED) ;
						d.download();
					} else {
						log("forecast location not found in database! " + data, null) ;
					}
				} else {
					log("notifications not enabled for location " + data) ;
				}
			} else {
				log("intent has no data!", null) ;
			}
		} else if(intent.getAction().equals(ACTION_NOTIFICATION_REMOVED)) {
			Uri data = intent.getData() ;
			if(data != null) {
				NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE) ;
				manager.cancel(this.getUniqueNotificationId(data));
			} else {
				log("intent has no data!", null) ;
			}
		}
	}

	protected abstract LocationDatabase getLocationDatabase(Context context) ;

	@Override
	public void onForecastDownload(Forecast forecast, Modes mode, ReturnTypes result) {
		log("forecast downloaded with return type: " + result) ;
		boolean error = false ;
		switch(result) {
		case IO_ERROR:
			error = true ;
			break;
		case XML_ERROR:
			error = true ;
			break ;
		case UNKNOWN_ERROR:
			error = true ;
			break;
		case NO_CACHED_FORECAST_ERROR:
			error = true ;
			break ;
		default:
			error = false ;
			break;
		}
		if(!error) {
			NotificationManager manager = (NotificationManager) forecast.getContext().getSystemService(Context.NOTIFICATION_SERVICE) ;
			List<WeatherWarning> warnings = new ArrayList<WeatherWarning>() ;
			for(ForecastItem i: forecast.items) {
				if(i instanceof WeatherWarning)
					warnings.add((WeatherWarning)i) ;
			}

			if(warnings.size() > 0) {
				manager.notify(this.getUniqueNotificationId(forecast.getLocation().getUri()), buildNotification(forecast, warnings));
			} else {
				manager.cancel(this.getUniqueNotificationId(forecast.getLocation().getUri()));
			}
		} else {
			//do nothing, there was an error.
		}
	}

	protected Notification buildNotification(Forecast forecast, List<WeatherWarning> warnings) {
		Notification.Builder builder = new Notification.Builder(forecast.getContext()) ;
		String title = warnings.get(0).getTitle() ;
		String subtitle = forecast.getLocation().toString(forecast.getLang()) ;
		if(warnings.size() > 1)
			title = String.format(forecast.getContext().getString(R.string.notification_howmany), warnings.size()) ;

		Intent i = new Intent(Intent.ACTION_VIEW) ;
		String url = warnings.get(0).getMobileUrl() ;
		i.setData(Uri.parse(url)) ;

		builder.setContentTitle(title) ;
		builder.setContentText(subtitle) ;
		builder.setSmallIcon(R.drawable.ic_stat_warning) ;
		builder.setOngoing(true) ;
		builder.setContentIntent(PendingIntent.getActivity(forecast.getContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT)) ;
		return builder.getNotification() ; //apparently .build() requires a higher API level (16)
	}

	private int getUniqueNotificationId(Uri locationUri) {
		return locationUri.hashCode() ;
	}

	private void log(String message) {
		Log.i("NotificationsReceiver", message) ;
	}

	private void log(String message, Exception error) {
		if(error != null)
			Log.e("NotificationsReceiver", message, error) ;
		else
			Log.e("NotificationsReceiver", message) ;
	}

}
