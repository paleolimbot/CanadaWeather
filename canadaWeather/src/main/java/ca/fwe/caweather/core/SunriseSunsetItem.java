package ca.fwe.caweather.core;

import ca.fwe.caweather.R;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;

/**
 * Created by dewey on 2016-11-11.
 */

public class SunriseSunsetItem extends ForecastItem {

    public SunriseSunsetItem(Forecast forecast, String riseS, String setS) {
        super(forecast);
        this.setTitle(getString(R.string.forecast_sunriseset));
        this.setDescription(String.format(getString(R.string.forecast_sunriseset_format), riseS, setS));
        this.setIconId(R.drawable.sunset);
    }

}
