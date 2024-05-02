package com.david.giczi.gpsurvey.utils;

import java.util.Locale;

public class WGS84 {

    private static final double a = 6378137.0;
    private static final double b = 6356752.314;
    //private static final double f = (a - b) / a;

    private static final double e2 = ( Math.pow(a, 2) - Math.pow(b, 2) ) / Math.pow(a, 2);
    //private static final double  e_ = Math.sqrt(( Math.pow(a, 2) - Math.pow(b, 2) ) / Math.pow(b, 2));

    public static String getX(double latitude, double longitude, double altitude){
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(Math.toRadians(latitude)), 2));
        double X = (N + altitude) * Math.cos(Math.toRadians(latitude))
                * Math.cos(Math.toRadians(longitude));
        return String.format(Locale.getDefault() ,"%.3fm", X);
    }
    public static String getY(double latitude, double longitude, double altitude){
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(Math.toRadians(latitude)), 2));
        double Y = (N + altitude) * Math.cos(Math.toRadians(latitude))
                * Math.sin(Math.toRadians(longitude));
        return String.format(Locale.getDefault(), "%.3fm", Y);
    }

    public static String getZ(double latitude, double altitude){
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(Math.toRadians(latitude)), 2));
        double Z = ((1 - e2) * N + altitude) * Math.sin(Math.toRadians(latitude));
        return String.format(Locale.getDefault(), "%.3fm", Z);
    }

    public static double getDoubleX(double latitude, double longitude, double altitude){
        return Double.parseDouble(getX(latitude, longitude, altitude)
                .substring(0, getX(latitude, longitude, altitude).indexOf("m")));
    }
    public static double getDoubleY(double latitude, double longitude, double altitude){
        return Double.parseDouble(getY(latitude, longitude, altitude)
                .substring(0, getY(latitude, longitude, altitude).indexOf("m")));
    }
    public static double getDoubleZ(double latitude, double altitude){
        return Double.parseDouble(getZ(latitude, altitude)
                .substring(0, getZ(latitude, altitude).indexOf("m")));
    }

}
