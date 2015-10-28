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

import android.content.Context;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.core.Units.UnitSet;

public class Forecast {

	public static final UnitSet DEFAULT_UNITS = UnitSet.METRIC ;
	public static final DateFormat DEFAULT_TIMEDATE = new SimpleDateFormat("d MMM h:mm a", Locale.CANADA) ;
	public static final DateFormat DEFAULT_TIME = new SimpleDateFormat("h:mm", Locale.CANADA) ;
	
	private Context context ;
	private ForecastLocation location ;
	private Units.UnitSet unitSet = DEFAULT_UNITS ;
	private DateFormat dateFormat = DEFAULT_TIMEDATE ;
	private DateFormat timeFormat = DEFAULT_TIME ;
	private int lang ;
	public List<ForecastItem> items ;
	private String timeZone ;
	private Date creationDate ;
	private Date issuedDate ;
	
	public Forecast(Context context, ForecastLocation location, int lang) {
		this.context = context;
		this.location = location;
		this.lang = lang;
		items = new ArrayList<ForecastItem>() ;
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

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
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

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public DateFormat getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(DateFormat timeFormat) {
		this.timeFormat = timeFormat;
	}
}
