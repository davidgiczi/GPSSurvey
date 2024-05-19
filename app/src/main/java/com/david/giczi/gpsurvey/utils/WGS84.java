package com.david.giczi.gpsurvey.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class WGS84 {

    private static final double a = 6378137.0;
    private static final double b = 6356752.314;
    private static final double e2 = ( Math.pow(a, 2) - Math.pow(b, 2) ) / Math.pow(a, 2);
    private static final double deltaX = - 54.595;
    private static final double deltaY =  72.495;
    private static final double deltaZ = 14.817;
    private static final double k_WGS = 1 + (- 1.998606 / 1000000);
    private static final double eX = Math.toRadians(- 0.302264 / 3600.0);
    private static final double eY =  Math.toRadians(- 0.161038 / 3600.0);
    private static final double eZ =  Math.toRadians(- 0.292622 / 3600.0);
    private double Y_EOV;
    private double X_EOV;
    private double Z_EOV;
    public double fi_WGS;
    public double lambda_WGS;
    public double h_WGS;

    public void toWGS84(double Y_EOV, double X_EOV, double Z_EOV) {
        this.Y_EOV = Y_EOV;
        this.X_EOV = X_EOV;
        this.Z_EOV = Z_EOV;
        List<Double> geoIUGG67 = getGeographicalCoordinatesForIUGG67();
    }
    private List<Double> getGeographicalCoordinatesForIUGG67(){
        double sphereFi_ = 2 * Math.atan( Math.pow(Math.E, (X_EOV - 200000) / (EOV.R * EOV.m0))) - Math.PI / 2;
        double sphereLambda_ = (Y_EOV - 650000) / (EOV.R * EOV.m0);
        double sphereFi = Math.asin(Math.sin(sphereFi_) * Math.cos(Math.toRadians(EOV.fi_0)) +
                Math.cos(sphereFi_) * Math.sin(Math.toRadians(EOV.fi_0) * Math.cos(sphereLambda_)));
        double sphereLambda = Math.asin(Math.cos(sphereFi_) * Math.sin(sphereLambda_) / Math.cos(sphereFi));
        double FI = iterateFi(sphereFi);
        double LAMBDA = Math.toRadians(EOV.lambda_0) + sphereLambda / EOV.n;
        return Arrays.asList(FI, LAMBDA, 0.0);
    }
    private double iterateFi(double preFi){
        double sphereFi = preFi;
        double Fi = preFi;
        for (int i = 0; i < 4; i++) {
            preFi =  2 * Math.atan(Math.pow(
                    Math.tan(Math.PI / 4 + sphereFi / 2) /
                            (EOV.k * Math.pow((1 - EOV.epszilon * Math.sin(Fi)) /
                                    (1 + EOV.epszilon * Math.sin(Fi)), EOV.n * EOV.epszilon / 2)),
                    1 / EOV.n) ) - Math.PI / 2;
            Fi = preFi;
        }
        return Fi;
    }
    public static void main(String[] args) {
        new WGS84().toWGS84(650684.464, 237444.185, 0.0);
    }

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
        return Double.parseDouble(getX(latitude, longitude, altitude).replace(",", ".")
                .substring(0, getX(latitude, longitude, altitude).indexOf("m")));
    }
    public static double getDoubleY(double latitude, double longitude, double altitude){
        return Double.parseDouble(getY(latitude, longitude, altitude).replace(",", ".")
                .substring(0, getY(latitude, longitude, altitude).indexOf("m")));
    }
    public static double getDoubleZ(double latitude, double altitude){
        return Double.parseDouble(getZ(latitude, altitude).replace(",", ".")
                .substring(0, getZ(latitude, altitude).indexOf("m")));
    }

}
