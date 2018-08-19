package ca.fwe.caweather.radar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.WeatherApp;

public class RadarLocations {

	public static RadarLocation get(String id, int lang) {
		RadarLocation[] all = ALL_EN ;
		if(lang == WeatherApp.LANG_FR)
			all = ALL_FR ;
		
		for(RadarLocation l: all) {
			if(l.getSiteId().equals(id))
				return l ;
		}
		return null ;
	}
	
	public static RadarLocation get(LatLon point, int lang) {
		RadarLocation[] locs = filter(point, 1, lang) ;
		if(locs.length > 0) {
			return locs[0];
		} else {
			return null ;
		}
	}

	public static RadarLocation[] filter(final LatLon point, int numEntries, int lang) {
		double radius = 500 ; //km to search
		ArrayList<RadarLocation> list = new ArrayList<>() ;

		RadarLocation[] all = ALL_EN ;
		if(lang == WeatherApp.LANG_FR)
			all = ALL_FR ;
		
		for(RadarLocation rl: all) {
			LatLon loc = rl.getLocation() ;
			if(loc != null) {
				double distance = loc.distanceTo(point) ;
				if(distance <= radius) {
					list.add(rl) ;
				}
			}
		}

        Collections.sort(list, new Comparator<RadarLocation>() {
            @Override
            public int compare(RadarLocation o1, RadarLocation o2) {
                double d1 = o1.getLocation().distanceTo(point) ;
                double d2 = o2.getLocation().distanceTo(point) ;
                if(d1 == d2) {
                    return 0;
                } else if(d1 > d2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        RadarLocation[] out = new RadarLocation[Math.min(numEntries, list.size())] ;
		
		for(int i=0; i<out.length; i++) {
			out[i] = list.get(i);
		}

		return out ;
	}

	public static RadarLocation[] ALL_EN = {
		new RadarLocation("Britt", "Georgian Bay", "WBI", "Ontario", new LatLon(45.79317,-80.53385),10),
		new RadarLocation("Montreal River Harbour", "Sault Ste Marie", "WGJ", "Ontario", new LatLon(47.24778,-84.59583), 10),
		new RadarLocation("Carvel", "Edmonton", "WHK", "Alberta", new LatLon(53.56056,-114.14495), 10),
		new RadarLocation("Jimmy Lake", "NW Saskatchewan/NE Alberta", "WHN", "Saskatchewan", new LatLon(54.91333,-109.95528), 10),
		new RadarLocation("King City", "Southern Ontario", "WKR", "Ontario", new LatLon(43.96393,-79.57388), 10),
		new RadarLocation("Lac Castor", "Saguenay River", "WMB", "Quebec", new LatLon(48.57581,-70.66784),10),
		new RadarLocation("McGill", "Montreal", "WMN", "Quebec", new LatLon(45.42416,-73.93735),10),
		new RadarLocation("Exeter", "Southwestern Ontario", "WSO", "Ontario", new LatLon(43.37028,-81.38417),10),
		new RadarLocation("Holyrood", "Eastern Newfoundland", "WTP", "Newfoundland and Labrador", new LatLon(47.32556,-53.17861),10),
		new RadarLocation("Aldergrove", "Vancouver", "WUJ", "British Columbia", new LatLon(49.01662,-122.48698),10),
		new RadarLocation("Villeroy", "Southwest of Quebec City", "WVY", "Quebec", new LatLon(46.45,-71.91528),10),
		new RadarLocation("Spirit River", "Grande Prairie", "WWW", "Alberta", new LatLon(55.6925,-119.23),10),
		new RadarLocation("Val d'Irène", "Lower St. Lawrence", "XAM", "Quebec", new LatLon(48.48028,-67.60111),10),
		new RadarLocation("Bethune", "Regina", "XBE", "Saskatchewan", new LatLon(50.57108,-105.18268),10),
		new RadarLocation("Schuler", "Medicine Hat", "XBU", "Alberta", new LatLon(50.3125,-110.19556),10),
		new RadarLocation("Dryden", "Western Ontario", "XDR", "Ontario", new LatLon(49.85823,-92.79698),10),
		new RadarLocation("Franktown", "Eastern Ontario", "XFT", "Ontario", new LatLon(45.04101,-76.11617),10),
		new RadarLocation("Foxwarren", "Eastern Saskatchewan/Western Manitoba", "XFW", "Manitoba", new LatLon(50.54887,-101.0857),10),
		new RadarLocation("Gore", "Central Hants County", "XGO", "Nova Scotia", new LatLon(45.0985,-63.70433),10),
		new RadarLocation("Landrienne", "Amos", "XLA", "Quebec", new LatLon(48.55152,-77.80815),10),
		new RadarLocation("Marion Bridge", "Southeastern Cape Breton County", "XMB", "Nova Scotia", new LatLon(45.94947,-60.20578),10),
		new RadarLocation("Marble Mountain", "Western Newfoundland", "XME", "Newfoundland and Labrador", new LatLon(48.93028,-57.83417),10),
		new RadarLocation("Chipman", "Central New Brunswick", "XNC", "New Brunswick", new LatLon(46.22222,-65.69861),10),
		new RadarLocation("Lasseter Lake", "Superior West", "XNI", "Ontario", new LatLon(48.85352,-89.1215),10),
		new RadarLocation("Prince George", "Northern B.C.", "XPG", "British Columbia", new LatLon(53.61308,-122.95441),10),
		new RadarLocation("Radisson", "Saskatoon", "CASRA", "Saskatchewan", new LatLon(52.52056,-107.44361),6),
		new RadarLocation("Mount Sicker", "Victoria", "XSI", "British Columbia", new LatLon(48.86099,-123.75654),10),
		new RadarLocation("Strathmore", "Calgary", "XSM", "Alberta", new LatLon(51.20628,-113.39906),10),
		new RadarLocation("Mount Silver Star", "Vernon", "XSS", "British Columbia", new LatLon(50.3695,-119.06436),10),
		new RadarLocation("Timmins", "Northeastern Ontario", "XTI", "Ontario", new LatLon(49.28146,-81.79406),10),
		new RadarLocation("Woodlands", "Winnipeg", "XWL", "Manitoba", new LatLon(50.15389,-97.77833),10)
	} ;
	
	public static RadarLocation[] ALL_FR = {
		new RadarLocation("Aldergrove", "Vancouver", "WUJ", "Colombie-Britannique", new LatLon(49.017, -122.487), 10),
		new RadarLocation("Bethune", "Régina", "XBU", "Saskatchewan", new LatLon(50.312, -110.196), 10),
		new RadarLocation("Britt", "Baie Georgienne", "WBI", "Ontario", new LatLon(45.793, -80.534), 10),
		new RadarLocation("Carvel", "Edmonton", "WHK", "Alberta", new LatLon(53.561, -114.145), 10),
		new RadarLocation("Chipman", "Frédéricton", "XNC", "Nouveau-Brunswick", new LatLon(46.222, -65.699), 10),
		new RadarLocation("Dryden", "Ontario ouest", "XDR", "Ontario", new LatLon(49.858, -92.797), 10),
		new RadarLocation("Exeter", "Ontario sud-ouest", "WSO", "Ontario", new LatLon(43.370, -81.384), 10),
		new RadarLocation("Foxwarren", "Saskatchawan est/Manitoba ouest", "XFW", "Manitoba", new LatLon(50.549, -101.086), 10),
		new RadarLocation("Franktown", "Ontario est", "XFT", "Ontario", new LatLon(45.041, -76.116), 10),
		new RadarLocation("Gore", "Comté de Hants", "XGO", "Nouvelle-Écosse", new LatLon(45.098, -63.704), 10),
		new RadarLocation("Holyrood", "Terre-Neuve est", "WTP", "Terre-Neuve et Labrador", new LatLon(47.326, -53.179), 10),
		new RadarLocation("Jimmy Lake", "Saskatchewan NO/Alberta NE", "WHN", "Saskatchewan", new LatLon(54.913, -109.955), 10),
		new RadarLocation("King City", "Nord de Toronto", "WKR", "Ontario", new LatLon(43.964, -79.574), 10),
		new RadarLocation("Lac Castor", "Parc national des Monts-Valin", "WMB", "Québec", new LatLon(48.576, -70.668), 10),
		new RadarLocation("Landrienne", "Landrienne", "XLA", "Québec", new LatLon(48.552, -77.808), 10),
		new RadarLocation("Lasseter Lake (Nipigon)", "Lac Supérieur ouest", "XNI", "Ontario", new LatLon(48.854, -89.122), 10),
		new RadarLocation("Marble Mountain", "Terre-Neuve ouest", "XME", "Terre-Neuve et Labrador", new LatLon(48.930, -57.834), 10),
		new RadarLocation("Marion Bridge", "Île du Cap-Breton", "XMB", "Nouvelle-Écosse", new LatLon(45.949, -60.206), 10),
		new RadarLocation("McGill", "Montréal", "WMN", "Québec", new LatLon(45.424, -73.937), 10),
		new RadarLocation("Montreal River Harbour", "Sault-Sainte-Marie", "WGJ", "Ontario", new LatLon(47.248, -84.596), 10),
		new RadarLocation("Mt. Sicker", "Victoria", "XSI", "Colombie-Britannique", new LatLon(48.861, -123.757), 10),
		new RadarLocation("Mt. Silver Star", "Vernon", "XSS", "Colombie-Britannique", new LatLon(50.370, -119.064), 10),
		new RadarLocation("Radisson", "Saskatoon", "CASRA", "Saskatchewan", new LatLon(52.521, -107.444), 6),
		new RadarLocation("Schuler", "Medicine Hat", "XBU", "Alberta", new LatLon(50.312, -110.196), 10),
		new RadarLocation("Spirit River", "Grande Prairie", "WWW", "Alberta", new LatLon(55.692, -119.230), 10),
		new RadarLocation("Strathmore", "Calgary", "XSM", "Alberta", new LatLon(51.206, -113.399), 10),
		new RadarLocation("Timmins", "Ontario nord-est", "XTI", "Ontario", new LatLon(49.281, -81.794), 10),
		new RadarLocation("Val d'Irène", "Bas-Saint-Laurent", "XAM", "Québec", new LatLon(48.480, -67.601), 10),
		new RadarLocation("Villeroy", "Sud-ouest de la ville de Québec", "WVY", "Québec", new LatLon(46.450, -71.915), 10),
		new RadarLocation("Woodlands", "Winnipeg", "XWL", "Manitoba", new LatLon(50.154, -97.778), 10)
	} ;

}
