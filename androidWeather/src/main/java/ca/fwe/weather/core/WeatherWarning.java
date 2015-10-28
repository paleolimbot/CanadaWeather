package ca.fwe.weather.core;

import java.util.Date;

import ca.fwe.weather.R;

public abstract class WeatherWarning extends ForecastItem {

	public enum Types {WATCH, WARNING, ADVISORY, STATEMENT, ENDED_NOTIFICATION} ;
	
	private Types type ;
	private Date date ;
	
	public WeatherWarning(Forecast forecast) {
		super(forecast);
		this.setDescription(forecast.getContext().getString(R.string.fi_click_for_more));
	}
	
	public Types getType() {
		return type;
	}

	public void setType(Types type) {
		this.type = type;
		int id = 0 ;
		switch(type) {
		case WARNING:
			id = R.drawable.ic_warning ;
			break ;
		case ENDED_NOTIFICATION:
			id = R.drawable.ic_warning_ended ;
			break ;
		case ADVISORY:
			id = R.drawable.ic_advisory ;
			break;
		case STATEMENT:
			id = R.drawable.ic_advisory ;
		case WATCH:
			id = R.drawable.ic_watch ;
			break;
		}
		
		this.setIconId(id);
	}

	public abstract String getUrl() ;
	public abstract String getMobileUrl() ;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
