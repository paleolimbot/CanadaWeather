package ca.fwe.caweather.core;

import ca.fwe.weather.R;
import ca.fwe.weather.core.PointInTimeForecast;
import ca.fwe.weather.core.Forecast;

public class HourlyForecastItem extends PointInTimeForecast {
    public HourlyForecastItem(Forecast forecast) {
        super(forecast);
    }

    @Override
    public String getDescription() {
        String condition = this.getFieldSummary(Fields.CONDITION);
        String windSpeed = this.getFieldSummary(Fields.WINDSPEED);
        String windGust = this.getFieldSummary(Fields.WINDGUST);
        String windDirection = this.getFieldSummary(Fields.WINDDIRECTION);

        String wind;
        if(windSpeed != null && windDirection != null && windGust != null) {
            wind = String.format("%s %s (%s)", windDirection, windSpeed , windGust);
        } else if(windSpeed != null && windDirection != null) {
            wind = String.format("%s %s", windDirection, windSpeed);
        } else {
            wind = null;
        }

        if(condition != null && wind != null) {
            return condition + ", " + this.getLabel(Fields.WINDSUMMARY) + " " + wind;
        } else if(condition != null) {
            return condition;
        } else if(wind != null) {
            return this.getLabel(Fields.WINDSUMMARY) + " " + wind;
        } else {
            return getString(R.string.unknown);
        }
    }
}
