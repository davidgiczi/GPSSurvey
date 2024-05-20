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
    private double Y_EOV;
    private double qY;
    private double X_EOV;
    private double qX;
    private double Z_EOV;
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
       this.Y_EOV = 0.0;
       this.X_EOV = 0.0;
       this.Z_EOV = 0.0;
       this.fi_WGS = 0.0;
       this.lambda_WGS = 0.0;
       this.h_WGS = 0.0;
       setCoordinates();
       setReliability();
    }
    private void setCoordinates(){
        for (EOV measData : preMeasPointData) {
            this.Y_EOV += measData.getY_EOV();
            this.X_EOV += measData.getX_EOV();
            this.Z_EOV += measData.getZ_EOV();
            this.fi_WGS += measData.getFi_WGS();
            this.lambda_WGS += measData.getLambda_WGS();
            this.h_WGS += measData.getH_WGS();
        }
        this.Y_EOV /= preMeasPointData.size();
        this.X_EOV /= preMeasPointData.size();
        this.Z_EOV /=  preMeasPointData.size();
        this.fi_WGS /= preMeasPointData.size();
        this.lambda_WGS /= preMeasPointData.size();
        this.h_WGS /=  preMeasPointData.size();
    }
    private void setReliability(){
        double vY = 0.0;
        double vX = 0.0;
        double vZ = 0.0;
        for (EOV measData : preMeasPointData) {
            vY += Math.pow(Y_EOV - measData.getY_EOV(), 2);
            vX += Math.pow(X_EOV - measData.getX_EOV(), 2);
            vZ += Math.pow(Z_EOV - measData.getZ_EOV(), 2);
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
    public double getY_EOV() {return (int) (100 * Y_EOV) / 100.0;}

    public double getX_EOV() {return (int) (100 * X_EOV) / 100.0;}

    public double getZ_EOV() {return (int) (100 * Z_EOV) / 100.0;
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
    public void setY_EOV(double y) {
        this.Y_EOV = y;
    }

    public void setX_EOV(double x) {
        this.X_EOV = x;
    }

    public void setFi_WGS(double fi_WGS) {
        this.fi_WGS = fi_WGS;
    }

    public void setLambda_WGS(double lambda_WGS) {
        this.lambda_WGS = lambda_WGS;
    }

    public void setH_WGS(double h_WGS) {
        this.h_WGS = h_WGS;
    }

    public String getMeasPontData(){
        return "Y=" + getY_EOV() + "m ±" + getqY() +
                "m\tX=" + getX_EOV() + "m ±" + getqX() +
                "m\nh=" + getZ_EOV() + "m ±" + getqZ() + "m";
    }

    public String getEOVMeasPontData(){
        return pointID +  "," + getY_EOV() + "," + getX_EOV() + "," + getZ_EOV()
                + "," + getQ() + "," + getqY() + "," + getqX() + "," + getqZ();
    }
    public String getWGSMeasPointDataInDecimalFormat(){
        return  String.format(Locale.getDefault(), "%.6f", lambda_WGS) + "," +
                String.format(Locale.getDefault(), "%.6f", fi_WGS) + "," +
                String.format(Locale.getDefault(), "%.2f", h_WGS);
    }
    public String getWGSMeasPointDataInAngelMinSecFormat(){
        return pointID + "," + MainActivity.convertAngleMinSecFormat(lambda_WGS) + "," +
                MainActivity.convertAngleMinSecFormat(fi_WGS) + "," + ((int) (100 * h_WGS) / 100.0);
    }

    public String getWGSMeasPointDataInXYZFormat(){
        double X = WGS84.getDoubleX(lambda_WGS, fi_WGS, h_WGS);
        double Y = WGS84.getDoubleY(lambda_WGS, fi_WGS, h_WGS);
        double Z = WGS84.getDoubleZ(lambda_WGS,h_WGS);
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
                "\n\nY=" + getY_EOV() +
                "m\t±" + getqY() +
                "m\n\nX=" + getX_EOV() +
                "m\t±" + getqX() +
                "m\n\nh=" + getZ_EOV() +
                "m\t±" + getqZ() + "m";
    }
}
