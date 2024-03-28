package com.david.giczi.gpsurvey.utils;

import com.david.giczi.gpsurvey.domain.MeasPoint;
import java.util.ArrayList;
import java.util.List;

public class CalcData {

    private List<MeasPoint> pointStore;

    public CalcData() {
        this.pointStore = new ArrayList<>();
    }

    public CalcData(List<MeasPoint> pointStore) {
        this.pointStore = pointStore;
    }

    public double calcDistance(){
        if( 2 > pointStore.size()){
            return 0.0;
        }
        double distance = 0.0;
        for (int i = 0; i < pointStore.size() - 1; i++) {
            distance += new AzimuthAndDistance(pointStore.get(i),
                    pointStore.get(i + 1)).calcDistance();
        }
        return (int) (100 * distance) / 100.0;
    }

    public double calcDistanceReliable(){
        if( 2 > pointStore.size()){
            return 0.0;
        }
        double reliable = 0.0;
        for (int i = 0; i < pointStore.size() - 1; i++) {
            double distance2 = Math.pow((pointStore.get( i + 1 ).getY() - pointStore.get(i).getY()), 2) +
                    Math.pow((pointStore.get( i + 1 ).getX() - pointStore.get(i).getX()), 2);

            reliable += ( Math.pow(pointStore.get(i).getqY(), 2) *
                    (Math.pow((pointStore.get( i + 1 ).getY() - pointStore.get(i).getY()), 2) / distance2) +

                    Math.pow(pointStore.get(i + 1).getqY(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getY() - pointStore.get(i).getY()), 2) / distance2) +

                    Math.pow(pointStore.get(i).getqX(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getX() - pointStore.get(i).getX()), 2) / distance2) +

                    Math.pow(pointStore.get(i + 1).getqX(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getX() - pointStore.get(i).getX()), 2) / distance2) );
        }

        return (int) (100 * Math.sqrt(reliable)) / 100.0;
    }

    public double calcPerimeter(){
        if( 3 > pointStore.size()){
            return 0.0;
        }
        double perimeter = 0.0;
        for (int i = 0; i < pointStore.size() - 1; i++) {
            perimeter += new AzimuthAndDistance(pointStore.get(i),
                    pointStore.get(i + 1)).calcDistance();
        }

        perimeter += new AzimuthAndDistance(pointStore.get(pointStore.size() - 1),
                pointStore.get(0)).calcDistance();

       return (int) (100 * perimeter) / 100.0;
    }

    public double calcPerimeterReliable(){
        if( 3 > pointStore.size()){
            return 0.0;
        }
        double reliable = 0.0;

        return reliable;
    }

    public double calcElevation(){
        if( 2 > pointStore.size() ){
            return 0.0;
        }
        double elevation = 0.0;

        for (int i = 0; i < pointStore.size() - 1; i++) {
            elevation += (pointStore.get(i + 1).getZ() - pointStore.get(i).getZ());
        }
        return (int) (100 * elevation) / 100.0;
    }

    public double calcElevationReliable(){
        if( 2 > pointStore.size() ){
            return 0.0;
        }
        double reliable = 0.0;

        for (MeasPoint measPoint : pointStore) {
            reliable += Math.pow(measPoint.getqZ(), 2);
        }
        return (int) (100 * Math.sqrt(reliable)) / 100.0;
    }

    public double calcArea(){
        if( 3 > pointStore.size() ){
            return 0.0;
        }
        double area = 0.0;
        for (int i = 0; i < pointStore.size() - 1; i++) {
            area += pointStore.get(i).getY() * pointStore.get(i + 1).getX();
        }
        area += (pointStore.get(pointStore.size() - 1).getY() * pointStore.get(0).getX());
        for (int i = 0; i < pointStore.size() - 1; i++) {
            area -= pointStore.get(i).getX() * pointStore.get(i + 1).getY();
        }
        area -= (pointStore.get(pointStore.size() - 1).getX() * pointStore.get(0).getY());

        return (int) (10 * Math.abs(0.5 * area)) / 10.0;
    }

    public double calcAreaReliable(){
        if( 3 > pointStore.size()){
            return 0.0;
        }
        double reliable = 0.0;

        return reliable;
    }
}
