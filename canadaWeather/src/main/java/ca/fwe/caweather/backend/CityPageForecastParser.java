package ca.fwe.caweather.backend;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.SharedPreferences;
import android.util.Log;
import ca.fwe.caweather.R;
import ca.fwe.caweather.core.CityPageWeatherWarning;
import ca.fwe.caweather.core.EnvironmentCanadaIcons;
import ca.fwe.caweather.core.EnvironmentCanadaIcons.IconSet;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.ForecastXMLParser;
import ca.fwe.weather.core.CurrentConditions;
import ca.fwe.weather.core.CurrentConditions.Fields;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.TimePeriodForecast;
import ca.fwe.weather.core.Units;
import ca.fwe.weather.core.Units.Unit;
import ca.fwe.weather.core.WeatherWarning;

public class CityPageForecastParser extends ForecastXMLParser {

	private static final String TAG = "CityPageForecastParser" ;
	private static final String PREF_KEY_ICONSET = "xml_iconset" ;
	
	private IconSet iconSet ;
	
	public CityPageForecastParser(Forecast forecast, File file) {
		super(forecast, file);
		iconSet = IconSet.AVMAN ;
		try {
			SharedPreferences p = WeatherApp.prefs(getForecast().getContext()) ;
			iconSet = IconSet.valueOf(p.getString(PREF_KEY_ICONSET, "AVMAN")) ;
		} catch(IllegalArgumentException e) {
			//this is where an illegal iconset will get identified.
			iconSet = IconSet.AVMAN ;
		}
	}

	@Override
	protected String getEncoding() {
		return "ISO-8859-1" ;
	}

	@Override
	protected void startParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
		while(parser.next() != XmlPullParser.END_DOCUMENT) {
			if(parser.getEventType() == XmlPullParser.START_TAG) {
				if(parser.getName().equals("siteData")) {
					this.parseSiteData(parser) ;
				}
			}
		}
	}

	private void parseSiteData(XmlPullParser parser) throws XmlPullParserException, IOException {
		while(parser.next() != XmlPullParser.END_TAG) {
			if(parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName() ;
				switch (tagName) {
					case "warnings":
						this.parseWarnings(parser);
						break;
					case "currentConditions":
						CurrentConditions c = this.parseCurrentConditions(parser);
						if (c != null)
							getForecast().items.add(c);
						break;
					case "forecastGroup":
						this.parseForecastGroup(parser);
						break;
					case "dateTime":
						Date d = this.parseDateTime(parser);
						if (d != null)
							getForecast().setCreationDate(d);
						break;
					default:
						skip(parser);
						break;
				}
			}
		}
	}
	
	private void parseForecastGroup(XmlPullParser parser) throws XmlPullParserException, IOException {
		while (parser.next() != XmlPullParser.END_TAG) {

			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				switch (tagName) {
					case "forecast":
						TimePeriodForecast f = parseForecast(parser);
						if (f != null)
							getForecast().items.add(f);
						break;
					case "dateTime":
						Date date = this.parseDateTime(parser);
						if (date != null)
							getForecast().setIssuedDate(date);
						break;
					default:
						skip(parser);
						break;
				}
			}
		}
	}
	
	private TimePeriodForecast parseForecast(XmlPullParser parser) throws XmlPullParserException, IOException {
		TimePeriodForecast dailyFc = new TimePeriodForecast(getForecast()) ;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				switch (tagName) {
					case "period":
						dailyFc.setTitle(parser.getAttributeValue(null, "textForecastName"));
						readTag(parser);
						break;
					case "textSummary":
						//TODO may have to do some converting of temperatures if units are different
						dailyFc.setDescription(readTag(parser));
						break;
					case "abbreviatedForecast":
						parseAbbreviatedForecast(parser, dailyFc);
						break;
					case "temperatures":
						parseTemperatures(parser, dailyFc);
						break;
					case "relativeHumidity":
						dailyFc.addExtraInfo(getForecast().getContext().getString(R.string.cc_field_relhumidity), readTag(parser));
						break;
					default:
						skip(parser);
						break;
				}
			}
		}
		return dailyFc ;
	}
	
	private void parseAbbreviatedForecast(XmlPullParser parser, TimePeriodForecast dailyFc) throws XmlPullParserException, IOException {
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				switch (tagName) {
					case "iconCode":
						dailyFc.setIconId(EnvironmentCanadaIcons.getIconId(iconSet, readTag(parser)));
						break;
					case "pop":
						try {
							String value = readTag(parser);
							if (value != null)
								dailyFc.setPop(Double.valueOf(value));
						} catch (NumberFormatException e) {
							Log.e(TAG, "error converting POP to type double", e);
						}
						break;
					default:
						skip(parser);
						break;
				}
			}
		}
	}
	
	private void parseTemperatures(XmlPullParser parser, TimePeriodForecast dailyFc) throws XmlPullParserException, IOException {
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				if (tagName.equals("temperature")) {
					String tclass = parser.getAttributeValue(null, "class") ;
					if(tclass != null) {
						try {
							if(tclass.equals("low")) {
								dailyFc.setLow(Double.valueOf(readTag(parser)), Units.Unit.DEG_C) ;
							} else {
								dailyFc.setHigh(Double.valueOf(readTag(parser)), Units.Unit.DEG_C) ;
							}
						} catch(NumberFormatException e) {
							Log.e(TAG, "error converting temperature to type double", e) ;
						}
					}
				} else {
					skip(parser);
				}
			}
		}
	}
	
	private void parseWarnings(XmlPullParser parser) throws XmlPullParserException, IOException {
		String link = parser.getAttributeValue(null, "url") ;
		if(link != null) {
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					if (tagName.equals("event")) {
						WeatherWarning warning = this.parseEvent(parser, link) ;
						if(warning != null) {
							getForecast().items.add(warning) ;
						}
					} else {
						skip(parser);
					}
				}
			}
		} else {
			skip(parser) ;
		}
	}
	
	private CityPageWeatherWarning parseEvent(XmlPullParser parser, String warningUrl) throws XmlPullParserException, IOException {
		CityPageWeatherWarning warning = new CityPageWeatherWarning(this.getForecast()) ;
		warning.setUrl(warningUrl);
		String type = parser.getAttributeValue(null, "type") ;
		String description = parser.getAttributeValue(null, "description") ;
		switch (type) {
			case "warning":
				warning.setType(WeatherWarning.Types.WARNING);
				break;
			case "ended":
				warning.setType(WeatherWarning.Types.ENDED_NOTIFICATION);
				break;
			case "advisory":
				warning.setType(WeatherWarning.Types.ADVISORY);
				break;
			default:
				warning.setType(WeatherWarning.Types.WATCH);
				break;
		}
		
		warning.setTitle(description);

		while (parser.next() != XmlPullParser.END_TAG) {

			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				if (tagName.equals("dateTime")) {
					Date date = this.parseDateTime(parser) ;
					if(date != null) {
						warning.setDate(date) ;
					}
				} else {
					skip(parser);
				}
			}
		}

		return warning ;
	}
	
	private Date parseDateTime(XmlPullParser parser) throws XmlPullParserException, IOException {
		Date date = null ;
		String utc = parser.getAttributeValue(null, "UTCOffset") ;
		if(utc != null) {
			if(utc.equals("0")) {
				int year = 0 ;
				int month = -1 ;
				int day = -1 ;
				int hour = -1 ;
				int minute = 0 ;

				while (parser.next() != XmlPullParser.END_TAG) {
					if (parser.getEventType() == XmlPullParser.START_TAG) {
						String tagName = parser.getName();	
						try {
							switch (tagName) {
								case "year":
									year = Integer.valueOf(readTag(parser));
									break;
								case "month":
									month = Integer.valueOf(readTag(parser));
									break;
								case "day":
									day = Integer.valueOf(readTag(parser));
									break;
								case "hour":
									hour = Integer.valueOf(readTag(parser));
									break;
								case "minute":
									minute = Integer.valueOf(readTag(parser));
									break;
								default:
									skip(parser);
									break;
							}
						} catch(NumberFormatException e) {
							//do nothing
						}
					}
				}

				if(year != -1 && month != -1 && day != -1 && hour != -1) {
					Calendar cal = Calendar.getInstance() ;
					cal.setTimeZone(TimeZone.getTimeZone("UTC")) ;
					cal.set(Calendar.YEAR, year) ;
					cal.set(Calendar.MONTH, month-1) ;
					cal.set(Calendar.DAY_OF_MONTH, day) ;
					cal.set(Calendar.HOUR_OF_DAY, hour) ;
					cal.set(Calendar.MINUTE, minute) ;
					cal.set(Calendar.SECOND, 0) ;
					cal.set(Calendar.MILLISECOND, 0) ;
					date = cal.getTime() ;
				}

			} else {
				getForecast().setTimeZone(parser.getAttributeValue(null, "zone")) ;
				skip(parser) ;
			}
		} else {
			skip(parser) ;
		}
		return date ;
	}
	
	private CurrentConditions parseCurrentConditions(XmlPullParser parser) throws XmlPullParserException, IOException {
		CurrentConditions c = new CurrentConditions(getForecast()) ;
		while (parser.next() != XmlPullParser.END_TAG) {

			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				switch (tagName) {
					case "station":
						c.setField(Fields.STATION, readTag(parser));
						break;
					case "condition":
						c.setField(Fields.CONDITION, readTag(parser));
						break;
					case "iconCode":
						int iconId = EnvironmentCanadaIcons.getIconId(iconSet, readTag(parser));
						c.setIconId(iconId);
						break;
					case "temperature":
						c.setField(Fields.TEMP, readTag(parser), Unit.DEG_C, 0); //probably has more precision

						//this is better for display
						break;
					case "windChill":
						c.setField(Fields.WINDCHILL, readTag(parser), Unit.DEG_C, 0);
						break;
					case "humidex":
						c.setField(Fields.HUMIDEX, readTag(parser), Unit.DEG_C, 0);
						break;
					case "pressure":
						c.setField(Fields.PRESSURETREND, parser.getAttributeValue(null, "tendency"));
						c.setField(Fields.PRESSURE, readTag(parser), Unit.KILOPASCALS, 1);
						break;
					case "dewpoint":
						c.setField(Fields.DEWPOINT, readTag(parser), Unit.DEG_C, 0);
						break;
					case "visibility":
						c.setField(Fields.VISIBILITY, readTag(parser), Unit.KILOMETRES, 0);
						break;
					case "relativeHumidity":
						c.setField(Fields.RELHUMIDITY, readTag(parser));
						break;
					case "wind":
						parseWind(parser, c);
						break;
					case "dateTime":
						Date date = this.parseDateTime(parser);
						if (date != null)
							c.setObservedDate(date);
						break;
					default:
						skip(parser);
						break;
				}
			}
		}
		return c ;
	}

	private void parseWind(XmlPullParser parser, CurrentConditions c) throws XmlPullParserException, IOException {
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				switch (tagName) {
					case "speed":
						c.setField(Fields.WINDSPEED, readTag(parser), Unit.KILOMETRES_PER_HOUR, 0);
						break;
					case "gust":
						c.setField(Fields.WINDGUST, readTag(parser), Unit.KILOMETRES_PER_HOUR, 0);
						break;
					case "direction":
						c.setField(Fields.WINDDIRECTION, readTag(parser));
						break;
					default:
						skip(parser);
						break;
				}
			}
		}
	}

}
