package ca.fwe.caweather.backend;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import ca.fwe.caweather.R;
import ca.fwe.caweather.core.AlmanacItem;
import ca.fwe.caweather.core.CityPageWeatherWarning;
import ca.fwe.caweather.core.EnvironmentCanadaIcons;
import ca.fwe.caweather.core.EnvironmentCanadaIcons.IconSet;
import ca.fwe.weather.core.CurrentConditions;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.caweather.core.HourlyForecastItem;
import ca.fwe.caweather.core.SunriseSunsetItem;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.ForecastXMLParser;
import ca.fwe.weather.core.PointInTimeForecast;
import ca.fwe.weather.core.PointInTimeForecast.Fields;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.MarkerForecastItem;
import ca.fwe.weather.core.TimePeriodForecast;
import ca.fwe.weather.core.Units;
import ca.fwe.weather.core.Units.Unit;
import ca.fwe.weather.core.WeatherWarning;

public class CityPageForecastParser extends ForecastXMLParser {

	private static final String TAG = "CityPageForecastParser" ;
	private static final String PREF_KEY_ICONSET = "xml_iconset" ;
	
	private IconSet iconSet ;
	private PointInTimeForecast currentConditions;

	public CityPageForecastParser(Forecast forecast, File file) {
		super(forecast, file);
		iconSet = IconSet.AVMAN ;
        currentConditions = null;
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

		// finalize forecast: put components in order, add markers between items
        // this ensures order no matter what EC puts in thier XML files

        ArrayList<WeatherWarning> warnings = new ArrayList<>();
		ArrayList<HourlyForecastItem> hourly = new ArrayList<>();
		ArrayList<TimePeriodForecast> daily = new ArrayList<>();
		ArrayList<AlmanacItem> almanac = new ArrayList<>();
		PointInTimeForecast cc = null;

		Forecast f = getForecast();
        Context context = f.getContext();

		for(ForecastItem fi: f.items) {
		    if(fi instanceof WeatherWarning) {
		        warnings.add((WeatherWarning)fi);
            } else if(fi instanceof HourlyForecastItem) {
		        hourly.add((HourlyForecastItem)fi);
            } else if(fi instanceof CurrentConditions) {
		        cc = (CurrentConditions)fi;
            } else if(fi instanceof TimePeriodForecast) {
                daily.add((TimePeriodForecast) fi);
            } else if(fi instanceof AlmanacItem) {
		        almanac.add((AlmanacItem)fi);
            } else {
                Log.i(TAG, "startParsing: Unrecognized forecast item: " + fi.getTitle());
            }
        }

        f.items.clear();

		if(warnings.size() > 0) {
            f.items.add(new MarkerForecastItem(f, context.getString(R.string.forecast_warnings)));
		    f.items.addAll(warnings);
        }

        if(cc != null) {
            f.items.add(new MarkerForecastItem(f, context.getString(R.string.forecast_current_conditions)));
		    f.items.add(cc);
        }

        if(daily.size() > 0) {
            String dateText ;
            if(f.getIssuedDate() != null)
                dateText = f.formatDate(f.getIssuedDate()) ;
            else
                dateText = context.getString(ca.fwe.weather.R.string.unknown) ;

		    f.items.add(new MarkerForecastItem(f, String.format(context.getString(R.string.forecast_issuedtext_daily), dateText)));
		    f.items.addAll(daily);

		    // update current conditions icon from the first hourly item if there is no icon
            if(cc != null && cc.getIconId() == EnvironmentCanadaIcons.getIconId(iconSet, "29")) {
                cc.setIconId(daily.get(0).getIconId());
            }
        }

        if(hourly.size() > 0) {
            f.items.add(new MarkerForecastItem(f, context.getString(R.string.forecast_issuedtext_hourly)));
            f.items.addAll(hourly);
        }

        if(almanac.size() > 0) {
		    f.items.add(new MarkerForecastItem(f, context.getString(R.string.forecast_almanac)));
		    f.items.addAll(almanac);
        }

        f.items.add(new MarkerForecastItem(getForecast(), context.getString(R.string.forecast_footertext)));
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
					    CurrentConditions c = new CurrentConditions(getForecast());
						if(this.parseCurrentConditions(parser, c)) {
                            getForecast().items.add(c);
                            currentConditions = c;
                        }
						break;
					case "forecastGroup":
						this.parseForecastGroup(parser);
						break;
                    case "hourlyForecastGroup":
                        this.parseHourlyForecastGroup(parser);
                        break;
					case "dateTime":
						Date d = this.parseDateTime(parser);
						if (d != null)
							getForecast().setCreationDate(d);
						break;
                    case "riseSet":
                        SunriseSunsetItem ssi = this.parseRiseSet(parser);
                        if(ssi != null) getForecast().items.add(ssi);
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

	private void parseHourlyForecastGroup(XmlPullParser parser) throws  XmlPullParserException, IOException {
		while (parser.next() != XmlPullParser.END_TAG) {

			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				switch (tagName) {
					case "hourlyForecast":
                        HourlyForecastItem hourly = new HourlyForecastItem(this.getForecast());

                        String dateStr = parser.getAttributeValue(null, "dateTimeUTC");
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        if(dateStr != null) {
                            try {
                                hourly.setObservedDate(format.parse(dateStr));
                            } catch (ParseException e) {
                                // itemDate is already null
                                Log.e(TAG, "parseHourlyForecastGroup: error parsing date", e);
                            }
                        }

                        if(parseCurrentConditions(parser, hourly)) {
                            getForecast().items.add(hourly);
                        }
						break;
					case "dateTime":
					    // for now, skipping setting the forecastIssued
                        // it is often the same as the 7-day forecastIssued
                        this.parseDateTime(parser);
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
		dailyFc.setIconId(EnvironmentCanadaIcons.getIconId(iconSet, "29")); // set to unknown by default

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {
				String tagName = parser.getName();
				switch (tagName) {
					case "period":
						dailyFc.setTitle(parser.getAttributeValue(null, "textForecastName"));
						readTag(parser);
						break;
					case "textSummary":
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
                            String val = readTag(parser);
                            if(val != null) {
                                if (tclass.equals("low")) {
                                    dailyFc.setLow(Double.valueOf(val), Units.Unit.DEG_C);
                                } else {
                                    dailyFc.setHigh(Double.valueOf(val), Units.Unit.DEG_C);
                                }
                            } else {
                                Log.e(TAG, "null temp, not setting high or low");
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
            case "":
                warning.setType(null);
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
		if(warning.getType() != null)
			return warning ;
		else
            return null;
	}

    private static class NamedDate extends Date {

        public String name;

        NamedDate(String name, long date) {
            super(date);
            this.name = name;
        }
    }

	private NamedDate parseDateTime(XmlPullParser parser) throws XmlPullParserException, IOException {
		Date date = null ;
		String utc = parser.getAttributeValue(null, "UTCOffset") ;
        String dateName = parser.getAttributeValue(null, "name") ;
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
			    // parsing a non-utc date, which we just use to set the timezone
                // of the forecast
                try {
                    String tzid = parser.getAttributeValue(null, "zone");
                    double offsetH = Double.valueOf(parser.getAttributeValue(null, "UTCOffset"));
                    int hours = (int)offsetH;
                    int min = (int)((offsetH - hours) * 60);

                    String sign;
                    if(offsetH > 0) {
                        sign = "+";
                    } else {
                        sign = "-";
                        hours = hours * -1;
                        min = min * -1;
                    }

                    // finding the timezone is not trivial, since EC doesn't give us
                    // America/Halifax, for example
                    TimeZone tz = TimeZone.getTimeZone(String.format(Locale.US, "GMT%s%02d%02d", sign, hours, min));
                    getForecast().setTimeZoneId(tzid);
                    getForecast().setTimeZone(tz);
                } catch(NumberFormatException e) {
                    // no worries
                }


				skip(parser) ;
			}
		} else {
			skip(parser) ;
		}
        if(date != null) {
            return new NamedDate(dateName, date.getTime());
        } else {
            return null;
        }
	}
	
	private boolean parseCurrentConditions(XmlPullParser parser, PointInTimeForecast c) throws XmlPullParserException, IOException {
	    // technically, this applies both to the currentConditions and hourlyForecast tags
		c.setIconId(EnvironmentCanadaIcons.getIconId(iconSet, "29")); // set to unknown by default

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
						c.setField(Fields.RELHUMIDITY, readTag(parser), Unit.PERCENT, 0);
						break;
					case "wind":
						parseWind(parser, c);
						break;
                    case "lop":
                        c.setField(Fields.LIKELIHOOD_OF_PRECIP, readTag(parser), Unit.PERCENT, 0);
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

		return true ;
	}

	private void parseWind(XmlPullParser parser, PointInTimeForecast c) throws XmlPullParserException, IOException {
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

	private SunriseSunsetItem parseRiseSet(XmlPullParser parser) throws XmlPullParserException, IOException {
        NamedDate rise = null;
        NamedDate set = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                String tagName = parser.getName();
                switch (tagName) {
                    case "dateTime":
                        NamedDate d = this.parseDateTime(parser);
                        if(d != null && d.name != null) {
                            if(d.name.equals("sunrise")) {
                                rise = d;
                            } else if(d.name.equals("sunset")) {
                                set = d;
                            }
                        }
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
        }

        if(rise != null && set != null) {
            String riseS = getForecast().formatTime(rise);
            String setS = getForecast().formatTime(set);

            if(currentConditions != null) {
                currentConditions.setField(Fields.SUNRISE, riseS);
                currentConditions.setField(Fields.SUNSET, setS);
            }
            return new SunriseSunsetItem(getForecast(), riseS, setS);
        } else {
            return null;
        }
    }

}
