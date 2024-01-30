package com.david.giczi.gpsurvey.utils;

public class WGS84 {

    private static final double a = 6378137.0;
    private static final double b = 6356752.314;
    private static final double f = (a - b) / a;

    private static final double e2 = ( Math.pow(a, 2) - Math.pow(b, 2) ) / Math.pow(a, 2);
    private static final double  e_ = Math.sqrt(( Math.pow(a, 2) - Math.pow(b, 2) ) / Math.pow(b, 2));

    public static String getX(double latitude, double longitude, double altitude){
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(Math.toRadians(latitude)), 2));
        double X = (N + altitude) * Math.cos(Math.toRadians(latitude))
                * Math.cos(Math.toRadians(longitude));
        return String.format( "%.3fm", X);
    }
    public static String getY(double latitude, double longitude, double altitude){
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(Math.toRadians(latitude)), 2));
        double Y = (N + altitude) * Math.cos(Math.toRadians(latitude))
                * Math.sin(Math.toRadians(longitude));
        return String.format( "%.3fm", Y);
    }

    public static String getZ(double latitude, double longitude, double altitude){
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(Math.toRadians(latitude)), 2));
        double Z = ((1 - e2) * N + altitude) * Math.sin(Math.toRadians(latitude));
        return String.format( "%.3fm", Z);
    }

}
