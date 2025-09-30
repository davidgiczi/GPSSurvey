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

    private String pointID;
    private List<TopoCentricPoint> preMeasPointData;
    private double Y_EOV;
    private double X_EOV;
    private double Z_EOV;
    private double Q;
    private double fi_WGS;
    private double lambda_WGS;
    private double h_WGS;
    private double EAST;
    private double NORTH;
    private double UP;
    private double qEAST;
    private double qNORTH;
    private double qUP;
    private boolean isTopoCenter;

    public MeasPoint() {
        this.preMeasPointData = new ArrayList<>();
    }

    public MeasPoint(double fi_WGS, double lambda_WGS, double h_WGS) {
        this.fi_WGS = fi_WGS;
        this.lambda_WGS = lambda_WGS;
        this.h_WGS = h_WGS;
    }

    public MeasPoint(String pointID) {
        this.preMeasPointData = new ArrayList<>();
        this.pointID = pointID;
    }

    public void setTopoCenter(boolean isTopoCenter) {

        this.isTopoCenter = isTopoCenter;

        if( isTopoCenter ){
            this.EAST = 0d;
            this.NORTH = 0d;
            this.UP = 0d;
            if( this.fi_WGS == 0d && this.lambda_WGS == 0d && this.h_WGS == 0d ){
                return;
            }
           TopoCentricPoint.initTopoCenter(this.fi_WGS, this.lambda_WGS, this.h_WGS, true);
        }
    }

    public void calcEastNorthUpData(){
        TopoCentricPoint topo = new TopoCentricPoint(this.fi_WGS, this.lambda_WGS, this.h_WGS);
        this.EAST = topo.EAST;
        this.NORTH = topo.NORTH;
        this.UP = topo.UP;
    }
    public void addMeasData(double fi, double lambda, double h) {
        if( MainActivity.MEAS_POINT_LIST.isEmpty() && preMeasPointData.isEmpty() ) {
            TopoCentricPoint.initTopoCenter(fi, lambda, h, false);
        }
        this.fi_WGS = 0.0;
        this.lambda_WGS = 0.0;
        this.h_WGS = 0.0;
        this.EAST = 0.0;
        this.NORTH = 0.0;
        this.UP = 0.0;
       preMeasPointData.add(new TopoCentricPoint(fi, lambda, h));
       setCoordinates();
       setReliability();
    }
    private void setCoordinates(){
        for (TopoCentricPoint measData : preMeasPointData) {
            this.fi_WGS += Math.toDegrees(measData.fi);
            this.lambda_WGS += Math.toDegrees(measData.lambda);
            this.h_WGS += measData.h;
            this.EAST += measData.EAST;
            this.NORTH += measData.NORTH;
            this.UP += measData.UP;
        }
        this.EAST /= preMeasPointData.size();
        this.NORTH /= preMeasPointData.size();
        this.UP /=  preMeasPointData.size();
        this.fi_WGS /= preMeasPointData.size();
        this.lambda_WGS /= preMeasPointData.size();
        this.h_WGS /=  preMeasPointData.size();
    }
    private void setReliability(){
        double vEAST = 0.0;
        double vNORTH = 0.0;
        double vUP = 0.0;
        for (TopoCentricPoint measData : preMeasPointData) {
            vEAST += Math.pow(this.EAST - measData.EAST, 2);
            vNORTH += Math.pow(this.NORTH - measData.NORTH, 2);
            vUP += Math.pow(this.UP - measData.UP, 2);
        }
        this.qEAST = Math.sqrt(vEAST / (preMeasPointData.size() - 1));
        this.qNORTH = Math.sqrt(vNORTH / (preMeasPointData.size() - 1));
        this.qUP = Math.sqrt(vUP / (preMeasPointData.size() - 1));
        this.Q = Math.sqrt(Math.pow(qEAST, 2) + Math.pow(qNORTH, 2));
    }

    public void calculateEOVData(){
        EOV EOV = new EOV();
        EOV.toEOV(this.fi_WGS, this.lambda_WGS, this.h_WGS);
        this.Y_EOV = EOV.getY_EOV();
        this.X_EOV = EOV.getX_EOV();
        this.Z_EOV = EOV.getZ_EOV();
    }

    public boolean isNotMeasured(){
        return preMeasPointData.size() < 2;
    }

    public String getPointID() {
        return pointID;
    }

    public String getPointIDForKML() {
        return pointID.endsWith("_kit") ? pointID.substring(0, pointID.indexOf("_")) : pointID;
    }
    public double getY_EOV() {return (int) (100 * Y_EOV) / 100.0;}

    public double getX_EOV() {return (int) (100 * X_EOV) / 100.0;}

    public double getZ_EOV() {return (int) (100 * Z_EOV) / 100.0;
    }
    public void setY_EOV(double y_EOV) {
        Y_EOV = y_EOV;
    }
    public void setX_EOV(double x_EOV) {
        X_EOV = x_EOV;
    }
    public void setZ_EOV(double z_EOV) {
        Z_EOV = z_EOV;
    }
    public double getQ() {
        return (int) (100 * Q) / 100.0;
    }

    public double getqEAST() {
        return (int) (100 * qEAST) / 100.0;
    }

    public double getqNORTH() {
        return (int) (100 * qNORTH) / 100.0;
    }

    public double getqUP() {
        return (int) (100 * qUP) / 100.0;
    }

    public double getEAST() {
        return (int) (100 * EAST) / 100.0;
    }
    public double getNORTH() {
        return (int) (100 * NORTH) / 100.0;
    }
    public double getUP() {
        return (int) (100 * UP) / 100.0;
    }

    public boolean isTopoCenter() {
        return isTopoCenter;
    }

    public void setEAST(double EAST) {
        this.EAST = EAST;
    }

    public void setNORTH(double NORTH) {
        this.NORTH = NORTH;
    }

    public List<TopoCentricPoint> getPreMeasPointData() {
        return preMeasPointData;
    }
    public void setPointID(String pointID) {
        this.pointID = pointID;
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

    public double getFi_WGS() {
        return fi_WGS;
    }

    public double getLambda_WGS() {
        return lambda_WGS;
    }

    public double getH_WGS() {
        return h_WGS;
    }

    public String getEOVPointData(){
        return  "Y=" + getY_EOV() + "m" +
                "\tX=" + getX_EOV() + "m" +
                "\tH=" + getZ_EOV() + "m";
    }

    public String getEOVPointSeparatedBySemicolon(){
        return (pointID.endsWith("_kit") ? pointID.substring(0, pointID.indexOf("_")) : pointID) +
                ";" + getY_EOV() + ";" + getX_EOV() + ";" + getZ_EOV();
    }

    public String getEastNorthUpPointData(){
      return  "East=" + getEAST() + "m" +
                "\tNorth=" + getNORTH() + "m" +
                "\tUp=" + getUP() + "m";
    }

    public String getWGSMeasPointDataInDecimalFormatSeparatedByComma(){
        return  String.format(Locale.getDefault(), "%.6f", lambda_WGS) + "," +
                String.format(Locale.getDefault(), "%.6f", fi_WGS) + "," +
                String.format(Locale.getDefault(), "%.2f", h_WGS);
    }
    public String getWGSMeasPointDataInDecimalFormatSeparatedBySemiColon(){
        return  String.format(Locale.getDefault(), "%.6f", fi_WGS) + ";" +
                String.format(Locale.getDefault(), "%.6f", lambda_WGS) + ";" +
                String.format(Locale.getDefault(), "%.2f", h_WGS);
    }
    public String getWGSMeasPointDataInAngelMinSecFormat(){
        return (pointID.endsWith("_kit") ? pointID.substring(0, pointID.indexOf("_")) : pointID)
                + ";" + MainActivity.convertAngleMinSecFormat(lambda_WGS) + ";" +
                MainActivity.convertAngleMinSecFormat(fi_WGS) + ";" + ((int) (100 * h_WGS) / 100.0);
    }

    public String getWGSMeasPointDataInXYZFormat(){
        double X = WGS84.getDoubleX(lambda_WGS, fi_WGS, h_WGS);
        double Y = WGS84.getDoubleY(lambda_WGS, fi_WGS, h_WGS);
        double Z = WGS84.getDoubleZ(lambda_WGS,h_WGS);
        return(pointID.endsWith("_kit") ? pointID.substring(0, pointID.indexOf("_")) : pointID)
                + ";" + X + ";" + Y + ";" + Z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasPoint measPoint = (MeasPoint) o;
        return pointID.equals(measPoint.pointID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointID);
    }

    @NonNull
    @Override
    public String toString() {
        return  pointID +". pont\t\t±Qyx=" + getQ() + "m" +
                "\n\nEast=" + getEAST() +
                "m\t±" + getqEAST() +
                "m\n\nNorth=" + getNORTH() +
                "m\t±" + getqNORTH() +
                "m\n\nUp=" + getUP() +
                "m\t±" + getqUP() + "m";
    }
}
