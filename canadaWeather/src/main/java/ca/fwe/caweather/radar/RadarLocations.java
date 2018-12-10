package ca.fwe.caweather.radar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.WeatherApp;

public class RadarLocations {

	public static RadarLocation get(String id) {
		for(RadarLocation l: ALL) {
			if(l.getSiteId().equals(id))
				return l ;
		}
		return null ;
	}
	
	public static RadarLocation get(LatLon point) {
		RadarLocation[] locs = filter(point, 1) ;
		if(locs.length > 0) {
			return locs[0];
		} else {
			return null ;
		}
	}

	public static RadarLocation[] filter(final LatLon point, int numEntries) {
		double radius = 500 ; //km to search
		ArrayList<RadarLocation> list = new ArrayList<>() ;
		
		for(RadarLocation rl: ALL) {
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

	public static RadarLocation[] ALL = {
			new RadarLocation("Mount Sicker", "Mt. Sicker", "Victoria", "Victoria", "British Columbia", "Colombie-Britannique", "XSI", "XSI", new LatLon(48.86099, -123.75654), 10),
			new RadarLocation("Prince George", "Prince George", "Northern B.C.", "Colombie-Britannique nord", "British Columbia", "Colombie-Britannique", "XPG", "XPG", new LatLon(53.61308, -122.95441), 10),
			new RadarLocation("Aldergrove", "Aldergrove", "Vancouver", "Vancouver", "British Columbia", "Colombie-Britannique", "WUJ", "WUJ", new LatLon(49.01662, -122.48698), 10),
			new RadarLocation("Spirit River", "Spirit River", "Grande Prairie", "Grande Prairie", "Alberta", "Alberta", "WWW", "WWW", new LatLon(55.6925, -119.23), 10),
			new RadarLocation("Mount Silver Star", "Mt. Silver Star", "Vernon", "Vernon", "British Columbia", "Colombie-Britannique", "XSS", "XSS", new LatLon(50.3695, -119.06436), 10),
			new RadarLocation("Carvel", "Carvel", "Edmonton", "Edmonton", "Alberta", "Alberta", "WHK", "WHK", new LatLon(53.56056, -114.14495), 10),
			new RadarLocation("Strathmore", "Strathmore", "Calgary", "Calgary", "Alberta", "Alberta", "XSM", "XSM", new LatLon(51.20628, -113.39906), 10),
			new RadarLocation("Schuler", "Schuler", "Medicine Hat", "Medicine Hat", "Alberta", "Alberta", "XBU", "XBU", new LatLon(50.3125, -110.19556), 10),
			new RadarLocation("Jimmy Lake", "Jimmy Lake", "NW Saskatchewan/NE Alberta", "Saskatchewan NO/Alberta NE", "Saskatchewan", "Saskatchewan", "WHN", "WHN", new LatLon(54.91333, -109.95528), 10),
			new RadarLocation("Radisson", "Radisson", "Saskatoon", "Saskatoon", "Saskatchewan", "Saskatchewan", "CASRA", "XRA", new LatLon(52.52056, -107.44361), 6),
			new RadarLocation("Bethune", "Bethune", "Regina", "Régina", "Saskatchewan", "Saskatchewan", "XBE", "XBE", new LatLon(50.57108, -105.18268), 10),
			new RadarLocation("Foxwarren", "Foxwarren", "Eastern Saskatchewan/Western Manitoba", "Saskatchawan est/Manitoba ouest", "Manitoba", "Manitoba", "CASFW", "XFW", new LatLon(50.54887, -101.0857), 6),
			new RadarLocation("Woodlands", "Woodlands", "Winnipeg", "Winnipeg", "Manitoba", "Manitoba", "XWL", "XWL", new LatLon(50.15389, -97.77833), 10),
			new RadarLocation("Dryden", "Dryden", "Western Ontario", "Ontario ouest", "Ontario", "Ontario", "XDR", "XDR", new LatLon(49.85823, -92.79698), 10),
			new RadarLocation("Lasseter Lake", "Lasseter Lake Nipigon", "Superior West", "Lac Supérieur ouest", "Ontario", "Ontario", "XNI", "XNI", new LatLon(48.85352, -89.1215), 10),
			new RadarLocation("Montreal River Harbour", "Montreal River Harbour", "Sault Ste Marie", "Sault-Sainte-Marie", "Ontario", "Ontario", "WGJ", "WGJ", new LatLon(47.24778, -84.59583), 10),
			new RadarLocation("Smooth Rock Falls", "Smooth Rock Falls", "Northeastern Ontario", "Ontario nord-est", "Ontario", "Ontario", "CASRF", "XTI", new LatLon(49.28146, -81.79406), 6),
			new RadarLocation("Exeter", "Exeter", "Southwestern Ontario", "Ontario sud-ouest", "Ontario", "Ontario", "WSO", "WSO", new LatLon(43.37028, -81.38417), 10),
			new RadarLocation("Britt", "Britt", "Georgian Bay", "Baie Georgienne", "Ontario", "Ontario", "WBI", "WBI", new LatLon(45.79317, -80.53385), 10),
			new RadarLocation("King City", "King City", "Southern Ontario", "Nord de Toronto", "Ontario", "Ontario", "WKR", "WKR", new LatLon(43.96393, -79.57388), 10),
			new RadarLocation("Landrienne", "Landrienne", "Amos", "Landrienne", "Quebec", "Québec", "XLA", "XLA", new LatLon(48.55152, -77.80815), 10),
			new RadarLocation("Franktown", "Franktown", "Eastern Ontario", "Ontario est", "Ontario", "Ontario", "XFT", "XFT", new LatLon(45.04101, -76.11617), 10),
			new RadarLocation("Blainville", "Blainville", "Montreal", "Montréal", "Quebec", "Québec", "CASBV", "WMN", new LatLon(45.70634, -73.85852), 6),
			new RadarLocation("Villeroy", "Villeroy", "Southwest of Quebec City", "Sud-ouest de la ville de Québec", "Quebec", "Québec", "WVY", "WVY", new LatLon(46.45, -71.91528), 10),
			new RadarLocation("Lac Castor", "Lac Castor", "Saguenay River", "Parc national des Monts-Valin", "Quebec", "Québec", "WMB", "WMB", new LatLon(48.57581, -70.66784), 10),
			new RadarLocation("Val d'Irène", "Val d'Irène", "Lower St. Lawrence", "Bas-Saint-Laurent", "Quebec", "Québec", "XAM", "XAM", new LatLon(48.48028, -67.60111), 10),
			new RadarLocation("Chipman", "Chipman", "Central New Brunswick", "Frédéricton", "New Brunswick", "Nouveau-Brunswick", "XNC", "XNC", new LatLon(46.22222, -65.69861), 10),
			new RadarLocation("Gore", "Gore", "Central Hants County", "Comté de Hants", "Nova Scotia", "Nouvelle-Écosse", "XGO", "XGO", new LatLon(45.0985, -63.70433), 10),
			new RadarLocation("Marion Bridge", "Marion Bridge", "Southeastern Cape Breton County", "Île du Cap-Breton", "Nova Scotia", "Nouvelle-Écosse", "XMB", "XMB", new LatLon(45.94947, -60.20578), 10),
			new RadarLocation("Marble Mountain", "Marble Mountain", "Western Newfoundland", "Terre-Neuve ouest", "Newfoundland and Labrador", "Terre-Neuve et Labrador", "XME", "XME", new LatLon(48.93028, -57.83417), 10),
			new RadarLocation("Holyrood", "Holyrood", "Eastern Newfoundland", "Terre-Neuve est", "Newfoundland and Labrador", "Terre-Neuve et Labrador", "WTP", "WTP", new LatLon(47.32556, -53.17861), 10)
	} ;
}
