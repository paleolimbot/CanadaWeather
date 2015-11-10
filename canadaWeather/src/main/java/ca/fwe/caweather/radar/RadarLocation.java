package ca.fwe.caweather.radar;

import android.net.Uri;
import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.WeatherApp;

public class RadarLocation {
	
	public static final String PRODUCT_PRECIP = RadarImageType.PRODUCT_PRECIP ;
	public static final String PRODUCT_24_HR_ACCUM = RadarImageType.PRODUCT_24_HR_ACCUM ;
	public static final String PRODUCT_CAPPI = RadarImageType.PRODUCT_CAPPI ;
		
	private static final String LANG_EXT_ENG = "_e" ;
	private static final String LANG_EXT_FR = "_f" ;
	
	private static final String SERVER = "http://dd.weatheroffice.ec.gc.ca/radar/%product%/GIF/" ;
	private static final String SERVER_WEB = "http://weather.gc.ca/radar/index%lang_ext%.html?id=%site_id%" ;
	
	private static final String URI_PATTERN = "radar:///ca/%s" ;
	
	public enum Overlays {RIVERS, ROADS, ROAD_LABELS, CITIES, MORE_CITIES}
	//this is the preferred order, so have a method for determining this
	public static int getOverlayPosition(Overlays overlay) {
		switch(overlay) {
		case CITIES:
			return 3 ;
		case MORE_CITIES:
			return 4 ;
		case RIVERS:
			return 0 ;
		case ROADS:
			return 1 ;
		case ROAD_LABELS:
			return 2 ;
		default:
			return -1 ;
		
		}
	}
	
	
//	public static final String OVERLAY_CITIES = "http://www.weatheroffice.gc.ca/radar/images/layers/default_cities/%id_lc%_towns.gif" ;
//	public static final String OVERLAY_MORE_CITIES = "http://www.weatheroffice.gc.ca/radar/images/layers/additional_cities/%id_lc%_towns.gif" ;
//	public static final String OVERLAY_ROADS = "http://www.weatheroffice.gc.ca/radar/images/layers/roads/%id_uc%_roads.gif" ;
//	public static final String OVERLAY_ROAD_LABELS = "http://www.weatheroffice.gc.ca/radar/images/layers/road_labels/%id_lc%_labs.gif" ;
//	public static final String OVERLAY_RIVERS = "http://www.weatheroffice.gc.ca/radar/images/layers/rivers/%id_lc%_rivers.gif" ;
//	public static final String OVERLAY_RADAR_CIRCLE = "http://www.weatheroffice.gc.ca/radar/images/layers/radar_circle/radar_circle.gif" ;
	
	// overlays: server http://www.weatheroffice.gc.ca
	// ["/radar/images/layers/additional_cities/wtp_towns.gif"]
	// ["/radar/images/layers/default_cities/wtp_towns.gif"]
	// ["/radar/images/layers/radar_circle/radar_circle.gif"]
	// ["/radar/images/layers/rivers/wtp_rivers.gif"]
	// ["/radar/images/layers/road_labels/wtp_labs.gif"]
	// ["/radar/images/layers/roads/WTP_roads.gif"]

	private String name ;
	private String alias ;
	private String siteId ;
	private String region ;
	private LatLon location ;
	
	public RadarLocation(String name, String alias, String siteId, String region, LatLon location) {
		this.name = name;
		this.alias = alias;
		this.siteId = siteId;
		this.region = region;
		this.location = location;
	}

	public Uri getUri() {
		return Uri.parse(String.format(URI_PATTERN, this.getSiteId())) ;
	}
	
	public String getName() {
		return name;
	}

	public String getAlias() {
		return alias;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getRegion() {
		return region;
	}

	public LatLon getLocation() {
		return location;
	}
	
	public String toString() {
		return this.getAlias() + " (" + this.getName() + ")" ;
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
		return SERVER_WEB.replace("%lang_ext%", langExt).replace("%site_id%", this.getSiteId()) ;
	}
	
	public String getOverlayAssetName(Overlays which) {
		switch(which) {
		case ROADS:
			return this.getSiteId() + "_roads.png" ;
		case CITIES:
			return this.getSiteId() + "_cities.png" ;
		case MORE_CITIES:
			return this.getSiteId() + "_more_cities.png" ;
		case RIVERS:
			return this.getSiteId() + "_rivers.png" ;
		case ROAD_LABELS:
			return this.getSiteId() + "_road_labels.png" ;
		default: return null ;
		}
	}
	
}
