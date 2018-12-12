package ca.fwe.caweather.core;

import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;

public class AlmanacItem extends ForecastItem {
    public AlmanacItem(Forecast forecast, String title, String description, int iconId) {
        super(forecast, title, description, null);
        this.setIconId(iconId);
    }
}
