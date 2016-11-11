package ca.fwe.caweather;

import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONObject;

import ca.fwe.caweather.backend.CityPageLocationDatabase;
import ca.fwe.weather.ForecastWidgetProvider;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.core.CurrentConditions;
import ca.fwe.weather.core.CurrentConditions.Fields;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.TimePeriodForecast;
import ca.fwe.weather.core.WeatherWarning;

public class CityPageWeatherWidget extends ForecastWidgetProvider {

	public static final String PREF_WIDGET_THEME = "xml_theme_widget" ;

    //TODO widget customization

	@Override
	protected LocationDatabase getLocationDatabase(Context context) {
		return new CityPageLocationDatabase(context) ;
	}

	private int getLayoutId(Context context) {
		SharedPreferences prefs = WeatherApp.prefs(context) ;
		String theme = prefs.getString(PREF_WIDGET_THEME, "DARK") ;
		switch (theme) {
			case "LIGHT":
				return R.layout.widget_current_light;
			case "TRANSPARENT":
				return R.layout.widget_current_transparent;
			case "TRANSPARENT_LIGHTTEXT":
				return R.layout.widget_current_transparent_whitetext;
			default:
				return R.layout.widget_current;
		}
	}
	
	@Override
	protected RemoteViews createWidgetView(Context context, Forecast f, JSONObject options, boolean error) {
		//TODO implement json options
        Log.i("CityPageWeatherWidget", "createWidgetView: json options are: " + options.toString());
        if(!error) {
            SharedPreferences prefs = WeatherApp.prefs(context) ;
            String theme = prefs.getString(PREF_WIDGET_THEME, "DARK") ;
            int textcol = Color.WHITE;
            if(theme.equals("LIGHT") || theme.equals("TRANSPARENT")) {
                textcol = Color.BLACK;
            }


			CurrentConditions c = null ;
			TimePeriodForecast today = null ;
			WeatherWarning warning = null ;
			for(ForecastItem i: f.items) {
				if(i instanceof CurrentConditions) {
					c = (CurrentConditions)i ;
					break ;
				}
			}
			for(ForecastItem i: f.items) {
				if(i instanceof TimePeriodForecast) {
					today = (TimePeriodForecast)i ;
					break ;
				}
			}
			for(ForecastItem i: f.items) {
				if(i instanceof WeatherWarning) {
					warning = (WeatherWarning) i ;
					break ;
				}
			}


			if(c != null) {
				RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutId(context)) ;
				views.setTextViewText(R.id.current_city, f.getLocation().toString(WeatherApp.getLanguage(context))) ;

				String temp = c.getFieldSummary(Fields.TEMP) ;
                if(temp == null) {
                    temp = context.getString(R.string.widget_na) ;
                }
                views.setTextViewText(R.id.current_title, temp) ;

                String feelsLike = c.getFieldSummary(Fields.FEELSLIKE);
                if(feelsLike != null) {
                    views.setViewVisibility(R.id.feelsliketext, View.VISIBLE);
                    views.setTextViewText(R.id.feelsliketext,
                            context.getString(R.string.cc_field_feelslike) + " " + feelsLike);
                } else {
                    views.setViewVisibility(R.id.feelsliketext, View.GONE);
                }

				Date d = c.getObservedDate() ;
				String dateText = context.getString(R.string.unknown) ;
				if(d != null)
					dateText = f.getTimeFormat().format(d) ;
				views.setTextViewText(R.id.current_subtitle, dateText) ;

                String condition = c.getField(Fields.CONDITION);
                if(condition != null) {
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

				if(today != null) {
					views.setViewVisibility(R.id.current_today_forecast_wrap, View.VISIBLE) ;

					String todayTitle = today.getTitle() ;
					String[] words = todayTitle.split(" ") ;
					if(words.length > 2)
						todayTitle = words[0] + " " + words[1] ;

					views.setTextViewText(R.id.current_next_timeperiod, todayTitle) ;
					if(today.getHigh() != null) {
						views.setViewVisibility(R.id.forecast_high, View.VISIBLE) ;
						views.setTextViewText(R.id.forecast_high, today.getHigh()) ;
					} else {
						views.setViewVisibility(R.id.forecast_high, View.GONE) ;
					}

					if(today.getLow() != null) {
						views.setViewVisibility(R.id.forecast_low, View.VISIBLE) ;
						views.setTextViewText(R.id.forecast_low, today.getLow()) ;
					} else {
						views.setViewVisibility(R.id.forecast_low, View.GONE) ;
					}

					views.setImageViewResource(R.id.current_today, today.getIconId()) ;
				} else {
					views.setViewVisibility(R.id.current_today_forecast_wrap, View.GONE) ;
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
