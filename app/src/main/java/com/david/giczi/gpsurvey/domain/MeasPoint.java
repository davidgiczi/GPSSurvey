package com.david.giczi.gpsurvey.domain;

import androidx.annotation.NonNull;

import com.david.giczi.gpsurvey.MainActivity;
import com.david.giczi.gpsurvey.utils.EOV;
import com.david.giczi.gpsurvey.utils.WGS84;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MeasPoint {

    private int pointID;
    private final List<EOV> preMeasPointData = new ArrayList<>();
    private double Y;
    private double qY;
    private double X;
    private double qX;
    private double Z;
    private double qZ;
    private double Q;
    private double fi_WGS;
    private double lambda_WGS;
    private double h_WGS;

    public MeasPoint() {
    }

    public MeasPoint(int pointID) {
        this.pointID = pointID;
    }
    public void setMeasData(EOV measPointData) {
       preMeasPointData.add(measPointData);
       this.Y = 0.0;
       this.X = 0.0;
       this.Z = 0.0;
       this.fi_WGS = 0.0;
       this.lambda_WGS = 0.0;
       this.h_WGS = 0.0;
       setCoordinates();
       setReliability();
    }
    private void setCoordinates(){
        for (EOV measData : preMeasPointData) {
            List<Double> eovData = measData.getCoordinatesForEOV();
            this.Y += eovData.get(0);
            this.X += eovData.get(1);
            this.Z += eovData.get(2);
            this.fi_WGS += measData.getFi_WGS();
            this.lambda_WGS += measData.getLambda_WGS();
            this.h_WGS += measData.getH_WGS();
        }
        this.Y /= preMeasPointData.size();
        this.X /= preMeasPointData.size();
        this.Z /=  preMeasPointData.size();
        this.fi_WGS /= preMeasPointData.size();
        this.lambda_WGS /= preMeasPointData.size();
        this.h_WGS /=  preMeasPointData.size();
    }
    private void setReliability(){
        double vY = 0.0;
        double vX = 0.0;
        double vZ = 0.0;
        for (EOV measData : preMeasPointData) {
            List<Double> eovData = measData.getCoordinatesForEOV();
            vY += Math.pow(Y - eovData.get(0), 2);
            vX += Math.pow(X - eovData.get(1), 2);
            vZ += Math.pow(Z - eovData.get(2), 2);
        }
        this.qY = Math.sqrt(vY / (preMeasPointData.size() - 1));
        this.qX = Math.sqrt(vX / (preMeasPointData.size() - 1));
        this.qZ = Math.sqrt(vZ / (preMeasPointData.size() - 1));
        this.Q = Math.sqrt(Math.pow(qY, 2) + Math.pow(qX, 2));
    }

    public boolean isNotMeasured(){
        return preMeasPointData.size() < 2;
    }

    public int getPointID() {
        return pointID;
    }
    public String getPointIDAsString() {
        return String.valueOf(pointID);
    }

    public double getY() {
        return (int) (100 * Y) / 100.0;
    }

    public double getX() {
        return (int) (100 * X) / 100.0;
    }

    public double getZ() {
        return (int) (100 * Z) / 100.0;
    }

    public double getQ() {
        return (int) (100 * Q) / 100.0;
    }

    public double getqY() {
        return (int) (100 * qY) / 100.0;
    }

    public double getqX() {
        return (int) (100 * qX) / 100.0;
    }

    public double getqZ() {
        return (int) (100 * qZ) / 100.0;
    }

    public List<EOV> getPreMeasPointData() {
        return preMeasPointData;
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

    public String getMeasPontData(){
        return "Y=" + getY() + "m ±" + getqY() +
                "m\tX=" + getX() + "m ±" + getqX() +
                "m\nh=" + getZ() + "m ±" + getqZ() + "m";
    }

    public String getEOVMeasPontData(){
        return pointID +  "," + getY() + "," + getX() + "," + getZ()
                + "," + getQ() + "," + getqY() + "," + getqX() + "," + getqZ();
    }
    public String getWGSMeasPointDataInDecimalFormat(){
        return pointID + "," + String.format(Locale.getDefault(), "%.6f", lambda_WGS) + "," +
                String.format(Locale.getDefault(), "%.6f", fi_WGS) + "," +
                String.format(Locale.getDefault(), "%.2f", h_WGS);
    }
    public String getWGSMeasPointDataInAngelMinSecFormat(){
        return pointID + "," + MainActivity.convertAngleMinSecFormat(lambda_WGS) + "," +
                MainActivity.convertAngleMinSecFormat(fi_WGS) + "," + ((int) (100 * h_WGS) / 100.0);
    }

    public String getWGSMeasPointDataInXYZFormat(){
        String X = WGS84.getX(lambda_WGS, fi_WGS, h_WGS).substring(0, WGS84.getX(lambda_WGS, fi_WGS, h_WGS).indexOf("m"));
        String Y = WGS84.getY(lambda_WGS, fi_WGS, h_WGS).substring(0, WGS84.getY(lambda_WGS, fi_WGS, h_WGS).indexOf("m"));
        String Z = WGS84.getZ(lambda_WGS,h_WGS).substring(0, WGS84.getZ(lambda_WGS,h_WGS).indexOf("m"));
        return pointID + "," + X + "," + Y + "," + Z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasPoint measPoint = (MeasPoint) o;
        return pointID == measPoint.pointID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointID);
    }

    @NonNull
    @Override
    public String toString() {
        return  pointID +". pont\t\t±Qyx=" + getQ() + "m" +
                "\n\nY=" + getY() +
                "m\t±" + getqY() +
                "m\n\nX=" + getX() +
                "m\t±" + getqX() +
                "m\n\nh=" + getZ() +
                "m\t±" + getqZ() + "m";
    }
}
