package ca.fwe.weather.core;

import ca.fwe.weather.R;

public class NullForecastItem extends ForecastItem {
    public NullForecastItem(Forecast forecast) {
        super(forecast);
        setTitle(getString(R.string.unknown));
        setDescription(getString(R.string.unknown));
    }
}
