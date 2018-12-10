package ca.fwe.weather.backend;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
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
    private static final String ACTION_NOTIFICATION_USER_LAUNCH = "ca.fwe.weather.ACTION_NOTIFICATION_USER_LAUNCH";

    private static final String PREF_NAME  = "prefs_NOTIFICATIONS" ;
    public static final String PREF_VIBRATE = "pref_notification_vibrate";

	@Override
	public void onReceive(Context context, Intent intent) {
		log("Receiving broadcast with action " + intent.getAction()) ;
		if(intent.getAction() == null) {
		    log("NULL action!");
		    return;
        }

        switch (intent.getAction()) {
            case ForecastDownloader.ACTION_FORECAST_DOWNLOADED: {
                Uri data = intent.getData();
                if (data != null) {
                    UpdatesManager manager = new UpdatesManager(context);
                    if (manager.notificationsEnabled(data)) {
                        //parse forecast
                        LocationDatabase db = this.getLocationDatabase(context);
                        ForecastLocation l = db.getLocation(data);
                        int lang = WeatherApp.getLanguage(context);
                        if (l != null) {
                            Forecast forecast = new Forecast(context, l, lang);
                            //not setting unit set because it is irrelevant here
                            ForecastDownloader d = new ForecastDownloader(forecast, this, Modes.LOAD_CACHED, false);
                            d.download();
                        } else {
                            log("forecast location not found in database! " + data, null);
                        }
                    } else {
                        log("notifications not enabled for location " + data);
                    }
                } else {
                    log("intent has no data!", null);
                }
                break;
            }
            case ACTION_NOTIFICATION_REMOVED: {
                Uri data = intent.getData();
                if (data != null) {
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if(manager == null) {
                        // notifications not a thing
                        log("There is no notification manager!");
                        return;
                    }
                    manager.cancel(NotificationsReceiver.getUniqueNotificationId(data));
                } else {
                    log("intent has no data!", null);
                }
                break;
            }
            case ACTION_NOTIFICATION_USER_CANCEL: {
                //user has cancelled notification: set cancelled key to true
                Uri data = intent.getData();
                if (data != null) {
                    int notificationId = NotificationsReceiver.getUniqueNotificationId(data);
                    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    String cancelledKey = String.valueOf(notificationId) + "_cancelled";
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(cancelledKey, true);
                    edit.apply();
                } else {
                    log("intent has no data!", null);
                }
                break;
            }
            case ACTION_NOTIFICATION_USER_LAUNCH: {
                //user has clicked on notification: launch activity and set cancelled key to true
                Uri data = intent.getData();
                if (data != null) {
                    Uri locUri = Uri.parse(data.getQueryParameter("weathernotificationsrc"));
                    int notificationId = NotificationsReceiver.getUniqueNotificationId(locUri);
                    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    String cancelledKey = String.valueOf(notificationId) + "_cancelled";
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(cancelledKey, true);
                    edit.apply();

                    //launch activity
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //strip location URI out of URL
                    String warningUri = data.toString();
                    String warningpart = "weathernotificationsrc=" + Uri.encode(locUri.toString());
                    i.setData(Uri.parse(warningUri.replace("&" + warningpart, "").replace(warningpart, "")));
                    try {
                        context.startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Log.e("NotificationReceiver", "onReceive: activity not found", e);
                    }

                    //actually remove notification from notificaion bar
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (manager == null) {
                        // notifications not a thing
                        log("There is no notification manager!");
                        return;
                    }
                    manager.cancel(notificationId);
                } else {
                    log("intent has no data!", null);
                }
                break;
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
			if(manager == null) {
			    // notifications not a thing
                log("There is no notification manager!");
                return;
            }

			List<WeatherWarning> warnings = new ArrayList<>() ;
			for(ForecastItem i: forecast.items) {
				if(i instanceof WeatherWarning) {
                    WeatherWarning w = (WeatherWarning)i ;
                    //pretend ended notifications are not a warning at all
                    if(w.getType() != WeatherWarning.Types.ENDED_NOTIFICATION)
					    warnings.add(w);
				}
			}

            int notificationId = NotificationsReceiver.getUniqueNotificationId(forecast.getLocation().getUri());
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
                    boolean newNotification = !title.equals(previousTitle);
                    if(newNotification) {
                        //title has changed, update in prefs
                        log("Updating pref keys: " + typeKey + "=" + title + ", " + cancelledKey + "=false");
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putString(typeKey, title);
                        edit.putBoolean(cancelledKey, false);
                        edit.apply();
                    }
                    //user has not cancelled this notification
                    manager.notify(notificationId, buildNotification(forecast, warnings, newNotification));
                }

			} else {
                //cancellable notifications: remove "uniquenotificationid_type" and
                // "uniquenotificationid_cancelled preference
                if(prefs.contains(typeKey) || prefs.contains(cancelledKey)) {
                    log("Removing pref keys: " + typeKey + ", " + cancelledKey);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.remove(typeKey);
                    edit.remove(cancelledKey);
                    edit.apply();
                }

                //cancel notification if still in notification bar
				manager.cancel(notificationId);
			}
		} else {
			log("error, doing nothing");
		}
	}

	protected Notification buildNotification(Forecast forecast, List<WeatherWarning> warnings, boolean isNew) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(forecast.getContext());
		Notification.Builder builder = new Notification.Builder(forecast.getContext()) ;
		String title = warnings.get(0).getTitle() ;
		String subtitle = forecast.getLocation().toString(forecast.getLang()) ;
		if(warnings.size() > 1)
			title = String.format(forecast.getContext().getString(R.string.notification_howmany), warnings.size()) ;

		Intent i = new Intent(ACTION_NOTIFICATION_USER_LAUNCH) ;
		String url = warnings.get(0).getMobileUrl() ;
        Uri uri = Uri.parse(url);
        uri = uri.buildUpon().appendQueryParameter("weathernotificationsrc", forecast.getLocation().getUri().toString()).build();
		i.setData(uri) ;

        Intent userCancel = new Intent(ACTION_NOTIFICATION_USER_CANCEL);
        userCancel.setData(forecast.getLocation().getUri());

		builder.setContentTitle(title) ;
		builder.setContentText(subtitle) ;
		builder.setSmallIcon(R.drawable.ic_stat_warning) ;
		builder.setOngoing(false) ;
        if(isNew && prefs.getBoolean(PREF_VIBRATE, false)) {
            builder.setVibrate(new long[] {750});
        }
		builder.setContentIntent(PendingIntent.getBroadcast(forecast.getContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT)) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(forecast.getContext(), 0, userCancel, PendingIntent.FLAG_UPDATE_CURRENT));
		Notification n = builder.build() ; //apparently .build() requires a higher API level (16)
        if(isNew && prefs.getBoolean(PREF_VIBRATE, false)) {
            n.defaults |= Notification.DEFAULT_VIBRATE;
        }
        return n;
	}

    protected boolean warningTitleIdentical(String currentTitle, String cachedTitle) {
        if(currentTitle != null) {
            return currentTitle.equals(cachedTitle);
        } else {
            return (null == cachedTitle) ;
        }
    }

	public static int getUniqueNotificationId(Uri locationUri) {
		Uri uri = locationUri.buildUpon().query(null).build();
        return uri.hashCode() ;
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
