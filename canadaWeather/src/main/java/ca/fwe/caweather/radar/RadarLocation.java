package ca.fwe.caweather.radar;

import android.net.Uri;
import android.support.annotation.NonNull;

import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.WeatherApp;

public class RadarLocation {
	
	public static final String PRODUCT_PRECIP = RadarImageType.PRODUCT_PRECIP ;
	public static final String PRODUCT_24_HR_ACCUM = RadarImageType.PRODUCT_24_HR_ACCUM ;
	public static final String PRODUCT_CAPPI = RadarImageType.PRODUCT_CAPPI ;
		
	private static final String LANG_EXT_ENG = "_e" ;
	private static final String LANG_EXT_FR = "_f" ;

	// https is not configured properly yet
	private static final String SERVER = "http://dd.weatheroffice.ec.gc.ca/radar/%product%/GIF/" ;
	private static final String SERVER_WEB = "https://weather.gc.ca/radar/index%lang_ext%.html?id=%site_id%" ;
	
	private static final String URI_PATTERN = "radar:///ca/%s" ;
	
	public enum Overlays {RIVERS, ROADS, ROAD_LABELS, CITIES, MORE_CITIES, RADAR_CIRCLES}
	//this is the preferred order, so have a method for determining this
	// have to keep ROAD_LABELS for legacy preferences, new terminology on the EC site is
	// ROAD_NUMBERS
	public static int getOverlayPosition(Overlays overlay) {
		switch(overlay) {
		case CITIES:
			return 4 ;
		case MORE_CITIES:
			return 5 ;
		case RIVERS:
			return 0 ;
		case ROADS:
			return 1 ;
		case ROAD_LABELS:
			return 2 ;
		case RADAR_CIRCLES:
			return 3;
		default:
			return -1 ;
		
		}
	}

	private String nameEn ;
	private String nameFr;
	private String aliasEn ;
	private String aliasFr;
	private String regionEn ;
	private String regionFr;
	private String siteId ;
	private String webId;
	private LatLon location ;
	private int updateFrequency ;
	
	public RadarLocation(@NonNull String nameEn, @NonNull String nameFr, @NonNull String aliasEn, @NonNull String aliasFr,
                         @NonNull String regionEn, @NonNull String regionFr, @NonNull String siteId, @NonNull String webId,
                         @NonNull LatLon location, int updateFrequency) {
		this.nameEn = nameEn;
		this.nameFr = nameFr;
		this.aliasEn = aliasEn;
		this.aliasFr = aliasFr;
		this.regionEn = regionEn;
		this.regionFr = regionFr;
		this.siteId = siteId;
		this.webId = webId;
		this.location = location;
		this.updateFrequency = updateFrequency;
	}

	public Uri getUri() {
		return Uri.parse(String.format(URI_PATTERN, this.getSiteId())) ;
	}
	
	public String getName(int lang) {
		if(lang == WeatherApp.LANG_FR) {
			return this.nameFr;
		} else {
			return this.nameEn;
		}
	}

	public String getAlias(int lang) {
		if(lang == WeatherApp.LANG_FR) {
			return this.aliasFr;
		} else {
			return this.aliasEn;
		}
	}

	public String getSiteId() {
		return siteId;
	}

	public String getWebId() {
		return webId;
	}

	public String getRegion(int lang) {
		if(lang == WeatherApp.LANG_FR) {
			return this.regionFr;
		} else {
			return this.regionEn;
		}
	}

	public LatLon getLocation() {
		return location;
	}
	
	public String toString() {
		return this.getAlias(WeatherApp.LANG_EN) + " (" + this.getName(WeatherApp.LANG_EN) + ")" ;
	}

	public int getUpdateFrequency() {
	    return this.updateFrequency;
    }
	
	public String getImageListURL(String product) {
		//addition on the end ensures most recent radar images are listed first
		return this.getImageBaseURL(product) + "?C=N;O=D" ;
	}
	
	public String getImageBaseURL(String product) {
		return SERVER.replace("%product%", product) + this.getSiteId() + "/" ;
	}
	
	public String getMobileURL(int lang) {
		return this.getWebURL(lang) ;
	}
	
	public String getWebURL(int lang) {
		String langExt = LANG_EXT_ENG ;
		if(lang == WeatherApp.LANG_FR) {
			langExt = LANG_EXT_FR ;
		}
		return SERVER_WEB.replace("%lang_ext%", langExt).replace("%site_id%", this.getWebId()) ;
	}
	
	public String getOverlayAssetName(Overlays which) {
		switch (which) {
			case ROADS:
				return this.getSiteId() + "_roads.png";
			case CITIES:
				return this.getSiteId() + "_cities.png";
			case MORE_CITIES:
				return this.getSiteId() + "_more_cities.png";
			case RIVERS:
				return this.getSiteId() + "_rivers.png";
			case ROAD_LABELS:
				return this.getSiteId() + "_road_numbers.png";
			case RADAR_CIRCLES:
				return this.getSiteId() + "_radar_circles.png";
			default:
				return null;
		}
	}
	
}
