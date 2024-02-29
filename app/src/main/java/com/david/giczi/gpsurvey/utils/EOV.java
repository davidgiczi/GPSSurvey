package com.david.giczi.gpsurvey.utils;

import java.util.Arrays;
import java.util.List;

public class EOV {

    private static final double a = 6378160.0;
    private static final double b = 6356774.516;
    private static final double R = 6379743.001;
    private static final double m0 = 0.99993;
    private static final double fi_0 = 47.0 + 6.0 / 60.0;
    private static final double n = 1.000719704936;
    private static final double k_EOV = 1.003110007693;
    private static final double lambda_0 = 19.0 + 2.0 / 60.0 + 54.8584 / 3600.0;
    private static final double e = Math.sqrt((Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(a, 2));
    private static final double e_ = Math.sqrt((Math.pow(a, 2) - Math.pow(b, 2)) / Math.pow(b, 2));
    private static final double deltaX = - 54.595;
    private static final double deltaY =  72.495;
    private static final double deltaZ = 14.817;
    private static final double k_WGS = 1 + (- 1.998606 / 1000000);
    private static final double eX = Math.toRadians(- 0.302264 / 3600.0);
    private static final double eY =  Math.toRadians(- 0.161038 / 3600.0);
    private static final double eZ =  Math.toRadians(- 0.292622 / 3600.0);
    private final double[][] Rx =
            {{1.0, 0.0, 0.0},
            {0.0, Math.cos(eX), Math.sin(eX)},
            {0.0, - Math.sin(eX), Math.cos(eX)}};
    private final double[][] Ry =
            {{Math.cos(eY), 0.0, - Math.sin(eY)},
            {0.0, 1.0, 0.0},
            {Math.sin(eY), 0.0, Math.cos(eY)}};
    private final double[][] Rz =
            {{Math.cos(eZ), Math.sin(eZ), 0.0},
            {- Math.sin(eZ), Math.cos(eZ), 0.0},
            {0.0, 0.0, 1.0}};

    private final double wgsX;
    private final double wgsY;
    private final double wgsZ;

    public EOV(double wgsX, double wgsY, double wgsZ) {
        this.wgsX = wgsX;
        this.wgsY = wgsY;
        this.wgsZ = wgsZ;
    }
    public List<Double> getCoordinatesForEOV(){

        List<Double> geoIUGG67 = getGeographicalCoordinatesForIUGG67();
        double sphereFi = 2 * Math.toDegrees(Math.atan(
        k_EOV * Math.pow(Math.tan(Math.toRadians(45 + geoIUGG67.get(0) / 2.0)), n) *
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

        return Arrays.asList((int) (1000 *  y) / 1000.0, (int) (1000 * x) / 1000.0,
                (int) (1000 * geoIUGG67.get(2)) / 1000.0);
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

    double x = deltaX + kRxRyRz_00 * wgsX + kRxRyRz_01 * wgsY + kRxRyRz_02 * wgsZ;
    double y = deltaY + kRxRyRz_10 * wgsX + kRxRyRz_11 * wgsY + kRxRyRz_12 * wgsZ;
    double z = deltaZ + kRxRyRz_20 * wgsX + kRxRyRz_21 * wgsY + kRxRyRz_22 * wgsZ;

    return Arrays.asList(x, y, z);
    }
}
