package ca.fwe.weather.util;

public final class Haversine {
    private static final int RADIUS = 6371;

    public static double calculate(final double lat1,
                                   final double lat2,
                                   final double lon1,
                                   final double lon2) {
        final double h = hav(lat2 - lat1)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * hav(lon2 - lon1);
        return 2 * RADIUS * Math.asin(Math.sqrt(h));
    }

    private static double hav(final double theta) {
        return (1 - Math.cos(Math.toRadians(theta))) / 2;
    }
}
