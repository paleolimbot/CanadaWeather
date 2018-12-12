package ca.fwe.caweather;

import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import ca.fwe.caweather.backend.CityPageLocationDatabase;
import ca.fwe.weather.ForecastWidgetProvider;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.core.PointInTimeForecast;
import ca.fwe.weather.core.PointInTimeForecast.Fields;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.TimePeriodForecast;
import ca.fwe.weather.core.WeatherWarning;

public class CityPageWeatherWidget extends ForecastWidgetProvider {

	public static final String PREF_WIDGET_THEME = "xml_theme_widget" ;

	@Override
	protected LocationDatabase getLocationDatabase(Context context) {
		return new CityPageLocationDatabase(context) ;
	}
	
	@Override
	protected RemoteViews createWidgetView(Context context, Forecast f, SharedPreferences prefs, boolean error) {
        if(!error) {
			SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String theme = prefs.getString(PREF_WIDGET_THEME, defaultPrefs.getString(PREF_WIDGET_THEME, "LIGHT")) ;
            int textcol = Color.WHITE;
            if(theme.equals("LIGHT") || theme.equals("TRANSPARENT")) {
                textcol = Color.BLACK;
            }

			int layoutId;
			switch (theme) {
				case "DARK":
					layoutId = R.layout.widget_dark;
					break;
				case "TRANSPARENT":
					layoutId = R.layout.widget_transparent_light;
					break;
				case "TRANSPARENT_LIGHTTEXT":
					layoutId = R.layout.widget_transparent_dark;
					break;
				default:
					layoutId = R.layout.widget_light;
					break;
			}

			PointInTimeForecast c = null ;
			TimePeriodForecast today = null ;
			TimePeriodForecast tonight = null;
			TimePeriodForecast tomorrow = null;
			WeatherWarning warning = null ;

			for(ForecastItem i: f.items) {
				if(i instanceof PointInTimeForecast) {
					c = (PointInTimeForecast)i ;
					break ;
				}
			}

			for(ForecastItem i: f.items) {
				if(i instanceof TimePeriodForecast) {
					if(today == null) {
						today = (TimePeriodForecast) i;
					} else if(tonight == null) {
						tonight = (TimePeriodForecast) i;
					} else {
						tomorrow = (TimePeriodForecast) i;
						break ;
					}
				}
			}
			for(ForecastItem i: f.items) {
				if(i instanceof WeatherWarning) {
					warning = (WeatherWarning) i ;
					break ;
				}
			}


			if(c != null) {
				RemoteViews views = new RemoteViews(context.getPackageName(), layoutId) ;

				views.setTextViewText(R.id.current_city, f.getLocation().toString(WeatherApp.getLanguage(context))) ;

				String temp = c.getFieldSummary(Fields.TEMP) ;
                if(temp == null) {
                    temp = context.getString(R.string.widget_na) ;
                } else {
					temp = temp.replace(" ", "").replace("C", "");
				}
				views.setTextViewText(R.id.current_title, temp) ;

                String feelsLike = c.getFieldSummary(Fields.FEELSLIKE);
                if(feelsLike != null) {
					views.setViewVisibility(R.id.current_feelslike, View.VISIBLE);
                    views.setTextViewText(R.id.current_feelslike, feelsLike.replace(" ", "").replace("C", ""));
                } else {
					views.setViewVisibility(R.id.current_feelslike, View.GONE);
				}

				Date d = c.getObservedDate() ;
				String dateText = context.getString(R.string.unknown) ;
				if(d != null)
					dateText = f.formatTime(d) ;
				views.setTextViewText(R.id.current_subtitle, dateText) ;

                String condition = c.getField(Fields.CONDITION);
                if(prefs.getBoolean("widget_showcc_icon", true)) {
                    views.setViewVisibility(R.id.current_icon, View.VISIBLE);
                    views.setImageViewResource(R.id.current_icon, c.getIconId());
                } else {
                    views.setViewVisibility(R.id.current_icon, View.GONE);
                }

				if(warning != null) {
                    views.setTextColor(R.id.current_city, Color.RED);
                } else {
                    views.setTextColor(R.id.current_city, textcol);
				}
				views.setOnClickPendingIntent(R.id.current_root, getOnClickPendingIntent(context, f.getLocation())) ;

				if(today != null && prefs.getBoolean("widget_day1", true)) {
					views.setViewVisibility(R.id.current_today_forecast_wrap, View.VISIBLE) ;

					String todayTitle = today.getTitle() ;
					String[] words = todayTitle.split(" ") ;
					if(words.length > 2)
						todayTitle = words[0] + " " + words[1] ;

					views.setTextViewText(R.id.current_today_timeperiod, todayTitle) ;
					if(today.getHigh() != null) {
						views.setViewVisibility(R.id.current_today_forecast_high, View.VISIBLE) ;
						views.setTextViewText(R.id.current_today_forecast_high, today.getHigh()) ;
					} else {
						views.setViewVisibility(R.id.current_today_forecast_high, View.GONE) ;
					}

					if(today.getLow() != null) {
						views.setViewVisibility(R.id.current_today_forecast_low, View.VISIBLE) ;
						views.setTextViewText(R.id.current_today_forecast_low, today.getLow()) ;
					} else {
						views.setViewVisibility(R.id.current_today_forecast_low, View.GONE) ;
					}

					views.setImageViewResource(R.id.current_today_icon, today.getIconId()) ;
				} else {
					views.setViewVisibility(R.id.current_today_forecast_wrap, View.GONE) ;
				}

				if(tonight != null && prefs.getBoolean("widget_day2", false)) {
					views.setViewVisibility(R.id.current_tonight_forecast_wrap, View.VISIBLE) ;

					String tonightTitle = tonight.getTitle() ;
					String[] words = tonightTitle.split(" ") ;
					if(words.length > 2)
						tonightTitle = words[0] + " " + words[1] ;

					views.setTextViewText(R.id.current_tonight_timeperiod, tonightTitle) ;
					if(tonight.getHigh() != null) {
						views.setViewVisibility(R.id.current_tonight_forecast_high, View.VISIBLE) ;
						views.setTextViewText(R.id.current_tonight_forecast_high, tonight.getHigh()) ;
					} else {
						views.setViewVisibility(R.id.current_tonight_forecast_high, View.GONE) ;
					}

					if(tonight.getLow() != null) {
						views.setViewVisibility(R.id.current_tonight_forecast_low, View.VISIBLE) ;
						views.setTextViewText(R.id.current_tonight_forecast_low, tonight.getLow()) ;
					} else {
						views.setViewVisibility(R.id.current_tonight_forecast_low, View.GONE) ;
					}

					views.setImageViewResource(R.id.current_tonight_icon, tonight.getIconId()) ;
				} else {
					views.setViewVisibility(R.id.current_tonight_forecast_wrap, View.GONE) ;
				}

				if(tomorrow != null && prefs.getBoolean("widget_day3", false)) {
					views.setViewVisibility(R.id.current_tomorrow_forecast_wrap, View.VISIBLE) ;

					String tomorrowTitle = tomorrow.getTitle() ;
					String[] words = tomorrowTitle.split(" ") ;
					if(words.length > 2)
						tomorrowTitle = words[0] + " " + words[1] ;

					views.setTextViewText(R.id.current_tomorrow_timeperiod, tomorrowTitle) ;
					if(tomorrow.getHigh() != null) {
						views.setViewVisibility(R.id.current_tomorrow_forecast_high, View.VISIBLE) ;
						views.setTextViewText(R.id.current_tomorrow_forecast_high, tomorrow.getHigh()) ;
					} else {
						views.setViewVisibility(R.id.current_tomorrow_forecast_high, View.GONE) ;
					}

					if(tomorrow.getLow() != null) {
						views.setViewVisibility(R.id.current_tomorrow_forecast_low, View.VISIBLE) ;
						views.setTextViewText(R.id.current_tomorrow_forecast_low, tomorrow.getLow()) ;
					} else {
						views.setViewVisibility(R.id.current_tomorrow_forecast_low, View.GONE) ;
					}

					views.setImageViewResource(R.id.current_tomorrow_icon, tomorrow.getIconId()) ;
				} else {
					views.setViewVisibility(R.id.current_tomorrow_forecast_wrap, View.GONE) ;
				}

				return views ;
			} else {
				//don't update
				return null ;
			}
		} else {
			//don't update
			return null ;
		}
	}
	
	private static PendingIntent getOnClickPendingIntent(Context context, ForecastLocation l) {
		Intent i = new Intent(context, MainActivity.class) ;
		i.setData(l.getUri()) ;
		return PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT) ;
	}

}
