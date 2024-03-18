package com.david.giczi.gpsurvey.domain;

import java.util.List;

public class MeasPoint {

    private int pointID;
    private final double Y;
    private final double X;
    private final double Z;

    public MeasPoint(int pointID, double y, double x, double z) {
        this.pointID = pointID;
        Y = y;
        X = x;
        Z = z;
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

    @Override
    public String toString() {
        return "MeasPoint{" +
                "pointID=" + pointID +
                ", Y=" + Y +
                ", X=" + X +
                ", Z=" + Z +
                '}';
    }
}
