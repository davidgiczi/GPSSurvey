package com.david.giczi.gpsurvey.domain;

public class TopoCentricPoint {

    private double X;
    private double Y;
    private double Z;
    public final double fi;
    public final double lambda;
    public final double h;
    public static double FI_0;
    public static double LAMBDA_0;
    public static double H_0;
    public double EAST;
    public double NORTH;
    public double UP;
    private static final double a = 6378137d;
    private static final double e2 = 6.69437999014 / 1000;

    public TopoCentricPoint(double fi, double lambda, double h) {
        this.fi = Math.toRadians(fi);
        this.lambda = Math.toRadians(lambda);
        this.h = h;
        calculateNorthEastUpData();
    }

    public void calculateNorthEastUpData(){
        X = getX(fi, lambda, h);
        Y = getY(fi, lambda, h);
        Z = getZ(fi, h);
        EAST = getEAST();
        NORTH = getNORTH();
        UP = getUP();
    }

    public static void setCentricFi(double fi_centric) {
        TopoCentricPoint.FI_0 = Math.toRadians(fi_centric);
    }

    public static void setCentricLambda(double lambda_centric) {
        TopoCentricPoint.LAMBDA_0 = Math.toRadians(lambda_centric);
    }

    public static void setCentricH(double h_centric) {
        TopoCentricPoint.H_0 = h_centric;
    }

    private double getEAST(){
        return (int) (1000 * (- Math.sin(LAMBDA_0) *
                (this.X - getX(TopoCentricPoint.FI_0, TopoCentricPoint.LAMBDA_0, TopoCentricPoint.H_0)) +
                Math.cos(TopoCentricPoint.LAMBDA_0) *
                        (this.Y - getY(TopoCentricPoint.FI_0, TopoCentricPoint.LAMBDA_0, TopoCentricPoint.H_0)))) / 1000d;
    }

    private double getNORTH(){
        return (int) (1000 * (- Math.sin(TopoCentricPoint.FI_0) * Math.cos(TopoCentricPoint.LAMBDA_0) *
                (this.X - getX(TopoCentricPoint.FI_0, TopoCentricPoint.LAMBDA_0, TopoCentricPoint.H_0)) -
                Math.sin(TopoCentricPoint.FI_0) * Math.sin(TopoCentricPoint.LAMBDA_0) *
                        (this.Y - getY(TopoCentricPoint.FI_0, TopoCentricPoint.LAMBDA_0, TopoCentricPoint.H_0)) +
                        Math.cos(TopoCentricPoint.FI_0) * (this.Z - getZ(TopoCentricPoint.FI_0, TopoCentricPoint.H_0)))) / 1000d;
    }

    private double getUP(){
        return (int) (1000 * (Math.cos(TopoCentricPoint.FI_0) * Math.cos(TopoCentricPoint.LAMBDA_0) *
                (this.X - getX(TopoCentricPoint.FI_0, TopoCentricPoint.LAMBDA_0, TopoCentricPoint.H_0)) +
                Math.cos(TopoCentricPoint.FI_0) * Math.sin(TopoCentricPoint.LAMBDA_0) *
                        (this.Y - getY(TopoCentricPoint.FI_0, TopoCentricPoint.LAMBDA_0, TopoCentricPoint.H_0)) +
                Math.sin(TopoCentricPoint.FI_0) * (this.Z - getZ(TopoCentricPoint.FI_0, TopoCentricPoint.H_0)))) / 1000d;
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
