package ca.fwe.caweather.backend;

import android.content.Context;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.backend.UpdatesReceiver;

public class CityPageUpdatesReceiver extends UpdatesReceiver {

	@Override
	protected LocationDatabase getLocationDatabase(Context context) {
		return new CityPageLocationDatabase(context) ;
	}

}
