package ca.fwe.weather.util;

public final class LatLon {
    private final double lat, lon;

    public LatLon(final double lat, final double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double distanceTo(final LatLon point) {
        return Haversine.calculate(lat, point.getLat(), lon, point.getLon());
    }
}
