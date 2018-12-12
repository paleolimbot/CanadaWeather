package ca.fwe.weather.core;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;

import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.core.Units.UnitSet;

public class Forecast {

    public static final String PREF_TIMEZONE_HANDLING = "TIMEZONE_HANDLING";
	public static final UnitSet DEFAULT_UNITS = UnitSet.METRIC ;
	
	private Context context ;
	private ForecastLocation location ;
	private Units.UnitSet unitSet = DEFAULT_UNITS ;

	private int lang ;
	public List<ForecastItem> items ;
	private TimeZone timeZone ;
	private String timeZoneId;
	private Date creationDate ;
	private Date issuedDate ;
	
	public Forecast(Context context, ForecastLocation location, int lang) {
		this.context = context;
		this.location = location;
		this.lang = lang;
		items = new ArrayList<>() ;
	}
	
	public Units.UnitSet getUnitSet() {
		return unitSet;
	}

	public void setUnitSet(Units.UnitSet unitSet) {
		this.unitSet = unitSet;
	}

	public NumberFormat getNumberFormat(int precision) {
		String pattern = "0" ;
		if(precision == 1) {
			pattern = "0.0" ;
		} else if(precision == 2) {
			pattern = "0.00" ;
		} else if(precision >= 3) {
			pattern += "." ;
			for(int i=0; i<precision; i++) {
				pattern += "0" ;
			}
		}
		return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(this.getLocale())) ;
	}

	public Context getContext() {
		return context;
	}

	public ForecastLocation getLocation() {
		return location;
	}

	public int getLang() {
		return lang;
	}
	
	public Locale getLocale() {
		return WeatherApp.getLocale() ;
	}

	private TimeZone getTimeZone() {
	    String tzPref = getTzPref();
	    switch (tzPref) {
            case "UTC_ALWAYS":
                return TimeZone.getTimeZone("UTC");
            case "TZ_DEFAULT":
                return TimeZone.getDefault();
            case "FORECAST_LOCAL":
                if(timeZone != null) {
                    return timeZone;
                } else {
                    return TimeZone.getDefault();
                }
            default:
                return TimeZone.getDefault();
        }
	}

	public String getTimeZoneId() {
	    return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
	    this.timeZoneId = timeZoneId;
    }

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}

	private String getTzPref() {
        SharedPreferences preferences = WeatherApp.prefs(context);
        return preferences.getString(PREF_TIMEZONE_HANDLING, "TZ_DEFAULT");
    }

	private DateFormat getDateFormat() {
        DateFormat df;
        if(lang == WeatherApp.LANG_FR) {
            df = new SimpleDateFormat("EEE d MMM h:mm a", Locale.CANADA_FRENCH);
        } else {
            df = new SimpleDateFormat("EEE d MMM h:mm a", Locale.CANADA);
        }
        df.setTimeZone(getTimeZone());
        return df;
	}

	private DateFormat getTimeFormat() {

	    DateFormat df;
	    if(lang == WeatherApp.LANG_FR) {
            df = new SimpleDateFormat("HH:mm", Locale.CANADA_FRENCH);
	    } else {
            df = new SimpleDateFormat("h:mm a", Locale.CANADA);
        }
	    df.setTimeZone(getTimeZone());
	    return df;
	}

	public String formatTime(Date datetime) {
        return getTimeFormat().format(datetime);
    }

    public String formatDate(Date datetime) {
        DateFormat df = getDateFormat();
        String tzid;
	    if(timeZoneId != null && getTzPref().equals("FORECAST_LOCAL")) {
	        tzid = timeZoneId;
        } else {
	        DateFormat tzDf = new SimpleDateFormat("zzz", getLocale());
	        tzDf.setTimeZone(df.getTimeZone());
	        tzid = tzDf.format(datetime);
        }

        return df.format(datetime) + " " + tzid;
    }
}
