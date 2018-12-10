package ca.fwe.caweather.radar;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ca.fwe.weather.WeatherApp;

public class RadarImage implements Comparable<RadarImage> {

	public static final String FILENAME_DATE_FORMAT = "yyyyMMddHHmm" ;
	
	private RadarLocation location ;
	private String link ;
	private File cachedFile ;
	private RadarImageType type ;
	private Date imageDate ;
	
	public RadarImage(RadarLocation location, String link, Date date, RadarImageType type) {
		this.location = location ;
		this.type = type ;
		this.link = link ;
		this.imageDate = date ;
	}

	public RadarImage(RadarLocation l, RadarImageType type, Date time) {
		this.location = l ;
		this.type = type ;
		this.imageDate = time ;
		this.link = urlFrom(l, type, time) ;
	}

	public RadarLocation getLocation() {
		return location;
	}

	public String getLink() {
		return link;
	}

	public File getCachedFile(RadarCacheManager manager) {
		if(cachedFile == null)
			cachedFile = manager.getRadarCacheFile(filenameFrom(this.getLocation(), this.getType(), this.getImageDate())) ;
		return cachedFile;
	}

	public RadarImageType getType() {
		return type;
	}

	public Date getImageDate() {
		return imageDate;
	}
	
	public String getFilename() {
		return parseURLForFilename(this.getLink()) ;
	}
	
	public static String urlFrom(RadarLocation l, RadarImageType type, Date time) {
		String filename = filenameFrom(l, type, time) ;
		String folder = l.getImageBaseURL(type.getProduct()) ;
		return folder + filename ;
	}
	
	public static String filenameFrom(RadarLocation l, RadarImageType type, Date time) {
		SimpleDateFormat sdf = new SimpleDateFormat(FILENAME_DATE_FORMAT, Locale.CANADA) ;
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String date = sdf.format(time) ;
		String radarCode = l.getSiteId() ;
		String typeExtra = type.getFilenameSuffix() ;
		return String.format("%s_%s_%s.gif", date, radarCode, typeExtra) ;
	}
	
	public static RadarImage parseFileName(String url) {
		String filename = parseURLForFilename(url) ;
		
		// 201301211830_WBI_PRECIPET_RAIN.gif
        if (filename != null) {

            String[] filenameSplit = filename.split("_");
            if (filenameSplit.length >= 4) {
                Date d = parseDate(filenameSplit[0]);
                RadarLocation l = RadarLocations.get(filenameSplit[1]);
                RadarImageType type = RadarImageType.from(filename);

                if (d != null && l != null && type != null) {
                    return new RadarImage(l, url, d, type);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null ;
        }
	}
	
	private static Date parseDate(String dateString) {
		try {
			return new SimpleDateFormat(FILENAME_DATE_FORMAT + "zzz", Locale.CANADA).parse(dateString + "GMT");
		} catch (ParseException e) {
			return null ;
		}
	}
	
	private static String parseURLForFilename(String url) {
		String[] split = url.split("/") ;
		if(split.length > 0) {
			return split[split.length - 1] ;
		} else {
			return null ;
		}
	}

	@Override
	public int compareTo(RadarImage another) {
		return another.getImageDate().compareTo(this.getImageDate()) * -1 ;
	}
	
}
