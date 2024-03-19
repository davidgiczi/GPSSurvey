package com.david.giczi.gpsurvey.domain;

import com.david.giczi.gpsurvey.utils.EOV;

import java.util.List;

public class MeasPoint {

    private int pointID;
    private double Y;
    private double qY;
    private double X;
    private double qX;
    private double Z;
    private double qZ;
    private double Q;

    public MeasPoint(){
    }
    public MeasPoint(int pointID, List<EOV> preMeasData) {
        this.pointID = pointID;
       setCoordinates(preMeasData);
       setReliability(preMeasData);
    }
    private void setCoordinates(List<EOV> preMeasData){
        for (EOV measData : preMeasData) {
            List<Double> eovData = measData.getCoordinatesForEOV();
            Y += eovData.get(0);
            X += eovData.get(1);
            Z += eovData.get(2);
        }
        Y = (int) (100 * (Y / preMeasData.size())) / 100.0;
        X = (int) (100 * (X / preMeasData.size())) / 100.0;
        Z = (int) (100 * (Z / preMeasData.size())) / 100.0;
    }
    private void setReliability(List<EOV> preMeasData){
        double vY = 0.0;
        double vX = 0.0;
        double vZ = 0.0;
        for (EOV measData : preMeasData) {
            List<Double> eovData = measData.getCoordinatesForEOV();
            vY += Math.pow(Y - eovData.get(0), 2);
            vX += Math.pow(X - eovData.get(1), 2);
            vZ += Math.pow(Z - eovData.get(2), 2);
        }
        qY = (int) 100 * Math.sqrt(vY / (preMeasData.size() - 1)) / 100.0;
        qX = (int) 100 * Math.sqrt(vX / (preMeasData.size() - 1)) / 100.0;
        qZ = (int) 100 * Math.sqrt(vZ / (preMeasData.size() - 1)) / 100.0;
        Q = (int) 100 * Math.sqrt(Math.pow(qY, 2) + Math.pow(qX, 2) + Math.pow(qZ, 2)) / 100.0;
    }
    public int getPointID() {
        return pointID;
    }

    public double getY() {
        return Y;
    }

    public double getX() {
        return X;
    }

    public double getZ() {
        return Z;
    }

    public double getQ() {
        return Q;
    }

    public double getqY() {
        return qY;
    }

    public double getqX() {
        return qX;
    }

    public double getqZ() {
        return qZ;
    }

    public void setPointID(int pointID) {
        this.pointID = pointID;
    }

    public void setY(double y) {
        Y = y;
    }

    public void setX(double x) {
        X = x;
    }

    @Override
    public String toString() {
        return pointID +" {" +
                "Y=" + Y +
                ", ±qY=" + qY +
                ", X=" + X +
                ", ±qX=" + qX +
                ", Z=" + Z +
                ", ±qZ=" + qZ +
                ", ±Q=" + Q +
                '}';
    }
}
