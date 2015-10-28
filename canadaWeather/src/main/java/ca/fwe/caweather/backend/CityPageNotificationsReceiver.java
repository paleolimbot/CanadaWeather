package ca.fwe.caweather.backend;

import android.content.Context;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.backend.NotificationsReceiver;

public class CityPageNotificationsReceiver extends NotificationsReceiver {

	@Override
	protected LocationDatabase getLocationDatabase(Context context) {
		return new CityPageLocationDatabase(context) ;
	}

}
