package ca.fwe.weather.core;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

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
        this.iconId = ICON_NONE;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	
	public Drawable getIcon() {
        try {
            if (iconId != ICON_NONE && icon == null)
                icon = forecast.getContext().getResources().getDrawable(this.getIconId());
        } catch (Resources.NotFoundException e) {
            Log.e("ForecastItem", "Tried to access illegal resource with ID " + iconId);
        }
		return icon;
	}
	public int getIconId() {
		return iconId ;
	}

	public String getHigh() {
	    return null;
    }

    public String getLow() {
	    return null;
    }

    public String getPop() {
	    return null;
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
