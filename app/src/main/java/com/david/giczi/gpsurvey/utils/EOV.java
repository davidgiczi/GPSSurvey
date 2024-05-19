package com.david.giczi.gpsurvey.utils;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;

public class EOV {

    public static final double a = 6378160.0;
    public static final double b = 6356774.516;
    public static final double R = 6379743.001;
    public static final double m0 = 0.99993;
    public static final double fi_0 = 47.0 + 6.0 / 60.0;
    public static final double n = 1.000719704936;
    public static final double k = 1.003110007693;
    public static final double lambda_0 = 19.0 + 2.0 / 60.0 + 54.8584 / 3600.0;
    public static final double epszilon = 0.0818205679407;
    private static final double e = Math.sqrt((Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(a, 2));
    private static final double e_ = Math.sqrt((Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(b, 2));
    private static final double deltaX = - 54.595;
    private static final double deltaY =  72.495;
    private static final double deltaZ = 14.817;
    private static final double k_WGS = 1 + (- 1.998606 / 1000000);
    private static final double eX = Math.toRadians(- 0.302264 / 3600.0);
    private static final double eY =  Math.toRadians(- 0.161038 / 3600.0);
    private static final double eZ =  Math.toRadians(- 0.292622 / 3600.0);
    private double fi_WGS;
    private double lambda_WGS;
    private double h_WGS;
    private double Y_EOV;
    private double X_EOV;
    private double Z_EOV;
    private static final double[][] Rx =
            {{1.0, 0.0, 0.0},
            {0.0, Math.cos(eX), Math.sin(eX)},
            {0.0, - Math.sin(eX), Math.cos(eX)}};
    private static final double[][] Ry =
            {{Math.cos(eY), 0.0, - Math.sin(eY)},
            {0.0, 1.0, 0.0},
            {Math.sin(eY), 0.0, Math.cos(eY)}};
    private static final double[][] Rz =
            {{Math.cos(eZ), Math.sin(eZ), 0.0},
            {- Math.sin(eZ), Math.cos(eZ), 0.0},
            {0.0, 0.0, 1.0}};

    public void toEOV(double fi_WGS, double lambda_WGS, double h_WGS){
     this.fi_WGS = fi_WGS;
     this.lambda_WGS = lambda_WGS;
     this.h_WGS = h_WGS;
     List<Double> EOV = getCoordinatesForEOV();
     this.Y_EOV = EOV.get(0);
     this.X_EOV = EOV.get(1);
     this.Z_EOV = EOV.get(2);
    }
    public double getFi_WGS() {
        return fi_WGS;
    }


    public double getLambda_WGS() {
        return lambda_WGS;
    }


    public double getH_WGS() {
        return h_WGS;
    }

    public double getY_EOV() {
        return Y_EOV;
    }

    public double getX_EOV() {
        return X_EOV;
    }

    public double getZ_EOV() {
        return Z_EOV;
    }

    private List<Double> getCoordinatesForEOV(){

        List<Double> geoIUGG67 = getGeographicalCoordinatesForIUGG67();
        double sphereFi = 2 * Math.toDegrees(Math.atan(
        k * Math.pow(Math.tan(Math.toRadians(45 + geoIUGG67.get(0) / 2.0)), n) *
       Math.pow( (1 - e * Math.sin(Math.toRadians(geoIUGG67.get(0)))) /
                (1 + e * Math.sin(Math.toRadians(geoIUGG67.get(0)))), n * e / 2.0)
        )) - 90;
        double sphereLambda = n * (geoIUGG67.get(1) - lambda_0);
        double fi_ = Math.toDegrees( Math.asin(
                Math.sin(Math.toRadians(sphereFi)) * Math.cos(Math.toRadians(fi_0)) -
                Math.cos(Math.toRadians(sphereFi)) * Math.sin(Math.toRadians(fi_0)) *
                        Math.cos(Math.toRadians(sphereLambda)) ));
        double lambda_ = Math.toDegrees(
                Math.asin(
                        Math.cos(Math.toRadians(sphereFi))  * Math.sin(Math.toRadians(sphereLambda))
                        / Math.cos(Math.toRadians(fi_))
                ));
        double x = R * m0 * Math.log(Math.tan(Math.toRadians(45 + fi_ / 2))) + 200000;
        double y =  R * m0 * Math.toRadians(lambda_) + 650000;

        return Arrays.asList((int) (100 *  y) / 100.0, (int) (100 * x) / 100.0,
                (int) (100 * geoIUGG67.get(2)) / 100.0);
    }

    private List<Double> getGeographicalCoordinatesForIUGG67(){
        List<Double> xyz = getXYZCoordinatesForIUGG67();

        double p = Math.sqrt(Math.pow(xyz.get(0), 2) + Math.pow(xyz.get(1), 2));
        double theta = Math.atan(xyz.get(2) * a / (p * b));
        double Fi = Math.toDegrees(Math.atan( (xyz.get(2) + Math.pow(e_, 2) * b * Math.pow(Math.sin(theta), 3)) /
                (p - Math.pow(e, 2) * a * Math.pow(Math.cos(theta), 3)) ));
        double Lambda = Math.toDegrees(Math.atan(xyz.get(1) / xyz.get(0)));
        double N = a / Math.sqrt(1 - Math.pow(e, 2) * Math.pow(Math.sin(Math.toRadians(Fi)), 2));
        double h = p / Math.cos(Math.toRadians(Fi)) - N;

        return Arrays.asList(Fi, Lambda, h);
    }

    private List<Double> getXYZCoordinatesForIUGG67(){

    double kRxRy_00 = k_WGS * Rx[0][0] * Ry[0][0] + k_WGS * Rx[0][1] * Ry[1][0] + k_WGS * Rx[0][2] * Ry[2][0];
    double kRxRy_10 = k_WGS * Rx[1][0] * Ry[0][0] + k_WGS * Rx[1][1] * Ry[1][0] + k_WGS * Rx[1][2] * Ry[2][0];
    double kRxRy_20 = k_WGS * Rx[2][0] * Ry[0][0] + k_WGS * Rx[2][1] * Ry[1][0] + k_WGS * Rx[2][2] * Ry[2][0];

    double kRxRy_01 = k_WGS * Rx[0][0] * Ry[0][1] + k_WGS * Rx[0][1] * Ry[1][1] + k_WGS * Rx[0][2] * Ry[2][1];
    double kRxRy_11 = k_WGS * Rx[1][0] * Ry[0][1] + k_WGS * Rx[1][1] * Ry[1][1] + k_WGS * Rx[1][2] * Ry[2][1];
    double kRxRy_21 = k_WGS * Rx[2][0] * Ry[0][1] + k_WGS * Rx[2][1] * Ry[1][1] + k_WGS * Rx[2][2] * Ry[2][1];

    double kRxRy_02 = k_WGS * Rx[0][0] * Ry[0][2] + k_WGS * Rx[0][1] * Ry[1][2] + k_WGS * Rx[0][2] * Ry[2][2];
    double kRxRy_12 = k_WGS * Rx[1][0] * Ry[0][2] + k_WGS * Rx[1][1] * Ry[1][2] + k_WGS * Rx[1][2] * Ry[2][2];
    double kRxRy_22 = k_WGS * Rx[2][0] * Ry[0][2] + k_WGS * Rx[2][1] * Ry[1][2] + k_WGS * Rx[2][2] * Ry[2][2];

    double kRxRyRz_00 = kRxRy_00 * Rz[0][0] + kRxRy_01 * Rz[1][0] + kRxRy_02 * Rz[2][0];
    double kRxRyRz_10 = kRxRy_10 * Rz[0][0] + kRxRy_11 * Rz[1][0] + kRxRy_12 * Rz[2][0];
    double kRxRyRz_20 = kRxRy_20 * Rz[0][0] + kRxRy_21 * Rz[1][0] + kRxRy_22 * Rz[2][0];

    double kRxRyRz_01 = kRxRy_00 * Rz[0][1] + kRxRy_01 * Rz[1][1] + kRxRy_02 * Rz[2][1];
    double kRxRyRz_11 = kRxRy_10 * Rz[0][1] + kRxRy_11 * Rz[1][1] + kRxRy_12 * Rz[2][1];
    double kRxRyRz_21 = kRxRy_20 * Rz[0][1] + kRxRy_21 * Rz[1][1] + kRxRy_22 * Rz[2][1];

    double kRxRyRz_02 = kRxRy_00 * Rz[0][2] + kRxRy_01 * Rz[1][2] + kRxRy_02 * Rz[2][2];
    double kRxRyRz_12 = kRxRy_10 * Rz[0][2] + kRxRy_11 * Rz[1][2] + kRxRy_12 * Rz[2][2];
    double kRxRyRz_22 = kRxRy_20 * Rz[0][2] + kRxRy_21 * Rz[1][2] + kRxRy_22 * Rz[2][2];

    double x = deltaX + kRxRyRz_00 * WGS84.getDoubleX(fi_WGS, lambda_WGS, h_WGS) +
                        kRxRyRz_01 * WGS84.getDoubleY(fi_WGS, lambda_WGS, h_WGS) +
                        kRxRyRz_02 * WGS84.getDoubleZ(fi_WGS, h_WGS);

    double y = deltaY + kRxRyRz_10 * WGS84.getDoubleX(fi_WGS, lambda_WGS, h_WGS) +
                        kRxRyRz_11 *  WGS84.getDoubleY(fi_WGS, lambda_WGS, h_WGS) +
                        kRxRyRz_12 * WGS84.getDoubleZ(fi_WGS, h_WGS);

    double z = deltaZ + kRxRyRz_20 * WGS84.getDoubleX(fi_WGS, lambda_WGS, h_WGS) +
                        kRxRyRz_21 * WGS84.getDoubleY(fi_WGS, lambda_WGS, h_WGS) +
                        kRxRyRz_22 * WGS84.getDoubleZ(fi_WGS, h_WGS);

    return Arrays.asList(x, y, z);
    }

    @NonNull
    @Override
    public String toString() {
        return  getY_EOV() + "m\t" + getX_EOV() + "m\t" + getZ_EOV() + "m";
    }
}
