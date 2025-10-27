package com.david.giczi.gpsurvey.domain;

import androidx.annotation.NonNull;

import java.util.Locale;

public class ElevPoint implements Comparable<ElevPoint> {

    private final String pointID;
    private final double distance;
    private final double elevation;


    public ElevPoint(String pointID, double distance, double elevation) {
        this.pointID = pointID.endsWith("kit") ? pointID.substring(0, pointID.indexOf("_")) : pointID;
        this.distance = distance;
        this.elevation = elevation;
    }
    public String getPointID() {
        return pointID;
    }

    public double getDistance() {
        return distance;
    }

    public String getElevation() {
        return String.format(Locale.getDefault(),"%.2fm", elevation).replace(",", ".");
    }

    @Override
    public int compareTo(ElevPoint o) {
        return Double.compare(this.distance, o.distance);
    }

    @NonNull
    @Override
    public String toString() {
        return "ElevPoint{" +
                "pointID='" + pointID + '\'' +
                ", distance=" + distance +
                ", elevation=" + elevation +
                '}';
    }
}
