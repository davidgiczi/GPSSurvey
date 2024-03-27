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

    public void addPoint(MeasPoint measPoint){
        if( pointStore.contains(measPoint) ){
            return;
        }
        pointStore.add(measPoint);
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

    public double calcArea(){
        if( 3 > pointStore.size() ){
            return 0.0;
        }

        double area = 0.0;

        return area;
    }

}
