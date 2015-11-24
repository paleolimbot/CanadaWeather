package ca.fwe.weather.backend;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String ACTION_NOTIFICATION_USER_CANCEL = "ca.fwe.weather.ACTION_NOTIFICATION_USER_CANCEL" ;

    private static final String PREF_NAME  = "prefs_NOTIFICATIONS" ;

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
		} else if(intent.getAction().equals(ACTION_NOTIFICATION_USER_CANCEL)) {
            //user has cancelled notification: set cancelled key to true
            Uri data = intent.getData() ;
            if(data != null) {
                int notificationId = this.getUniqueNotificationId(data);
                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String cancelledKey = String.valueOf(notificationId) + "_cancelled";
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(cancelledKey, true);
                edit.apply();
            } else {
                log("intent has no data!", null) ;
            }
        }
	}

	protected abstract LocationDatabase getLocationDatabase(Context context) ;

	@Override
	public void onForecastDownload(Forecast forecast, Modes mode, ReturnTypes result) {
		log("forecast downloaded with return type: " + result) ;
		boolean error ;
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
			List<WeatherWarning> warnings = new ArrayList<>() ;
			for(ForecastItem i: forecast.items) {
				if(i instanceof WeatherWarning)
					warnings.add((WeatherWarning)i) ;
			}
            //TODO don't display "ended" notifications
            int notificationId = this.getUniqueNotificationId(forecast.getLocation().getUri());
            SharedPreferences prefs = forecast.getContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String typeKey = String.valueOf(notificationId) + "_type";
            String cancelledKey = String.valueOf(notificationId) + "_cancelled";
			if(warnings.size() > 0) {
                String title = warnings.get(0).getTitle();
                String previousTitle = prefs.getString(typeKey, null);
                if(this.warningTitleIdentical(title, previousTitle) && prefs.getBoolean(cancelledKey, false)) {
                    //user has already cancelled this notification...don't notify again!
                    log("User has already cancelled " +
                            title + " for location " +
                            forecast.getLocation().getUri());

                } else {
                    if(!title.equals(previousTitle)) {
                        //title has changed, update in prefs
                        log("Updating pref keys: " + typeKey + "=" + title + ", " + cancelledKey + "=false");
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(typeKey, title);
                        edit.putBoolean(cancelledKey, false);
                        edit.apply();
                    }
                    //user has not cancelled this notification
                    manager.notify(notificationId, buildNotification(forecast, warnings));
                }

			} else {
                //cancellable notifications: remove "uniquenotificationid_type" and
                // "uniquenotificationid_cancelled preference
                log("Removing pref keys: " + typeKey + ", " + cancelledKey);
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove(typeKey);
                edit.remove(cancelledKey);
                edit.apply();

                //cancel notification if still in notification bar
				manager.cancel(notificationId);
			}
		} else {
			log("error, doing nothing");
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

        Intent userCancel = new Intent(ACTION_NOTIFICATION_USER_CANCEL);
        userCancel.setData(forecast.getLocation().getUri());

		builder.setContentTitle(title) ;
		builder.setContentText(subtitle) ;
		builder.setSmallIcon(R.drawable.ic_stat_warning) ;
		builder.setOngoing(false) ;
		builder.setContentIntent(PendingIntent.getActivity(forecast.getContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT)) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(forecast.getContext(), 0, userCancel, PendingIntent.FLAG_UPDATE_CURRENT));
		return builder.getNotification() ; //apparently .build() requires a higher API level (16)
	}

    protected boolean warningTitleIdentical(String currentTitle, String cachedTitle) {
        if(currentTitle != null) {
            return currentTitle.equals(cachedTitle);
        } else {
            return (null == cachedTitle) ;
        }
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
