package ca.fwe.caweather;

import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;
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
	
	@Override
	protected LocationDatabase getLocationDatabase(Context context) {
		return new CityPageLocationDatabase(context) ;
	}

	private int getLayoutId(Context context) {
		SharedPreferences prefs = WeatherApp.prefs(context) ;
		String theme = prefs.getString(PREF_WIDGET_THEME, "DARK") ;
		if(theme.equals("LIGHT"))
			return R.layout.widget_current_light ;
		else if(theme.equals("TRANSPARENT"))
			return R.layout.widget_current_transparent ;
		else
			return R.layout.widget_current ;
	}
	
	@Override
	protected RemoteViews createWidgetView(Context context, Forecast f, boolean error) {
		if(!error) {
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
				views.setTextViewText(R.id.current_city, f.getLocation().getName(WeatherApp.getLanguage(context))) ;

				String temp = context.getString(R.string.widget_na) ;
				String suffix = "" ;
				String wc = c.getFieldSummary(Fields.WINDCHILL) ;
				String hd = c.getFieldSummary(Fields.HUMIDEX) ;
				String tmp = c.getFieldSummary(Fields.TEMP) ;
				if(wc != null) {
					suffix = "*" ;
				} else if(hd != null) {
					suffix = "*" ;
				}
				
				if(tmp != null)
					temp = tmp ;
					

				views.setTextViewText(R.id.current_title, temp + suffix) ;

				Date d = c.getObservedDate() ;
				String dateText = context.getString(R.string.unknown) ;
				if(d != null)
					dateText = f.getTimeFormat().format(d) ;
				views.setTextViewText(R.id.current_subtitle, dateText) ;


				if(warning == null) {
					views.setImageViewResource(R.id.current_icon, c.getIconId()) ;
				} else {
					views.setImageViewResource(R.id.current_icon, warning.getIconId()) ;
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
