package com.david.giczi.gpsurvey.domain;

public class TopoCentricPoint {

    private final double X;
    private final double Y;
    private final double Z;
    public static double FI_0;
    public static double LAMBDA_0;
    public static double H_0;
    public static double EAST;
    public static double NORTH;
    public static double UP;
    private static final double a = 6378137d;
    private static final double e2 = 6.69437999014 / 1000;

    public TopoCentricPoint(double fi, double lambda, double h) {
        fi = Math.toRadians(fi);
        lambda = Math.toRadians(lambda);
        X = getX(fi, lambda, h);
        Y = getY(fi, lambda, h);
        Z = getZ(fi, h);
        EAST = getEAST();
        NORTH = getNORTH();
        UP = getUP();
    }

    public static void setCentricFi(double fi_centric) {
        FI_0 = Math.toRadians(fi_centric);
    }

    public static void setCentricLambda(double lambda_centric) {
        LAMBDA_0 = Math.toRadians(lambda_centric);
    }

    public static void setCentricH(double h_centric) {
        H_0 = h_centric;
    }

    private double getEAST(){
        return (int) (1000 * (- Math.sin(LAMBDA_0) * (X - getX(FI_0, LAMBDA_0, H_0)) +
                Math.cos(LAMBDA_0) * (Y - getY(FI_0, LAMBDA_0, H_0)))) / 1000d;
    }

    private double getNORTH(){
        return (int) (1000 * (- Math.sin(FI_0) * Math.cos(LAMBDA_0) * (X - getX(FI_0, LAMBDA_0, H_0)) -
                Math.sin(FI_0) * Math.sin(LAMBDA_0) * (Y - getY(FI_0, LAMBDA_0, H_0)) +
                        Math.cos(FI_0) * (Z - getZ(FI_0, H_0)))) / 1000d;
    }

    private double getUP(){
        return (int) (1000 * (Math.cos(FI_0) * Math.cos(LAMBDA_0) * (X - getX(FI_0, LAMBDA_0, H_0)) +
                Math.cos(FI_0) * Math.sin(LAMBDA_0) * (Y - getY(FI_0, LAMBDA_0, H_0)) +
                Math.sin(FI_0) * (Z - getZ(FI_0, H_0)))) / 1000d;
    }

    private double getX(double fi, double lambda, double h){
        return (getN(fi) + h) * Math.cos(fi) * Math.cos(lambda);
    }
    private double getY(double fi, double lambda, double h){
        return (getN(fi) + h) * Math.cos(fi) * Math.sin(lambda);
    }
    private double getZ(double fi, double h){
        return ((1 - e2) * getN(fi) + h) * Math.sin(fi);
    }
    private double getN(double fi){
        return a / Math.pow(1 - e2 * Math.pow(Math.sin(fi), 2), 0.5);
    }

}
