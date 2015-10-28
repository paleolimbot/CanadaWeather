package ca.fwe.weather.core;

import android.graphics.drawable.Drawable;

public class ForecastItem {
	
	public static final int ICON_NONE = Integer.MIN_VALUE ;
	
	protected Forecast forecast ;
	protected String title ;
	protected String description ;
	protected Drawable icon ;
	protected int iconId ;
	
	public ForecastItem(Forecast forecast) {
		this(forecast, null, null, null) ;
	}
	
	public ForecastItem(Forecast forecast, String title, String description, Drawable icon) {
		this.forecast = forecast ;
		this.title = title;
		this.description = description;
		this.icon = icon;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	
	public Drawable getIcon() {
		if(iconId != ICON_NONE && icon == null)
			icon = forecast.getContext().getResources().getDrawable(this.getIconId()) ;
		return icon;
	}
	public int getIconId() {
		return iconId ;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId ;
	}
	
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	
	protected String getString(int resId) {
		return forecast.getContext().getString(resId) ;
	}
	
	public String toString() {
		return this.getTitle() ;
	}
	
}
