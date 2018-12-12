package ca.fwe.weather.core;

import ca.fwe.weather.R;

public class CurrentConditions extends PointInTimeForecast {
    public CurrentConditions(Forecast forecast) {
        super(forecast);
    }

    public String getTimeSummary() {
        return getString(R.string.cc_currently);
    }

}
