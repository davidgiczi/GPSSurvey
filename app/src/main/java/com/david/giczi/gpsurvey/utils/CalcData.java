package com.david.giczi.gpsurvey.utils;

import com.david.giczi.gpsurvey.domain.MeasPoint;
import java.util.List;

public class CalcData {

    private final List<MeasPoint> pointStore;


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
            double distance2 = Math.pow((pointStore.get( i + 1 ).getY_EOV() - pointStore.get(i).getY_EOV()), 2) +
                    Math.pow((pointStore.get( i + 1 ).getX_EOV() - pointStore.get(i).getX_EOV()), 2);

            reliable += ( Math.pow(pointStore.get(i).getqY(), 2) *
                    (Math.pow((pointStore.get( i + 1 ).getY_EOV() - pointStore.get(i).getY_EOV()), 2) / distance2) +

                    Math.pow(pointStore.get(i + 1).getqY(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getY_EOV() - pointStore.get(i).getY_EOV()), 2) / distance2) +

                    Math.pow(pointStore.get(i).getqX(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getX_EOV() - pointStore.get(i).getX_EOV()), 2) / distance2) +

                    Math.pow(pointStore.get(i + 1).getqX(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getX_EOV() - pointStore.get(i).getX_EOV()), 2) / distance2) );
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

        for (int i = 0; i < pointStore.size() - 1; i++) {

            double distance2 = Math.pow((pointStore.get( i + 1 ).getY_EOV() - pointStore.get(i).getY_EOV()), 2) +
                    Math.pow((pointStore.get( i + 1 ).getX_EOV() - pointStore.get(i).getX_EOV()), 2);

            reliable += ( Math.pow(pointStore.get(i).getqY(), 2) *
                    (Math.pow((pointStore.get( i + 1 ).getY_EOV() - pointStore.get(i).getY_EOV()), 2) / distance2) +

                    Math.pow(pointStore.get(i + 1).getqY(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getY_EOV() - pointStore.get(i).getY_EOV()), 2) / distance2) +

                    Math.pow(pointStore.get(i).getqX(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getX_EOV() - pointStore.get(i).getX_EOV()), 2) / distance2) +

                    Math.pow(pointStore.get(i + 1).getqX(), 2) *
                            (Math.pow((pointStore.get( i + 1 ).getX_EOV() - pointStore.get(i).getX_EOV()), 2) / distance2) );
        }
        double distance2 = Math.pow((pointStore.get(0).getY_EOV() - pointStore.get(pointStore.size() - 1).getY_EOV()), 2) +
                Math.pow((pointStore.get(0).getX_EOV() - pointStore.get(pointStore.size() - 1).getX_EOV()), 2);

        reliable += ( Math.pow(pointStore.get(pointStore.size() - 1).getqY(), 2) *
                (Math.pow((pointStore.get(pointStore.size() - 1).getY_EOV() - pointStore.get(0).getY_EOV()), 2) / distance2) +

                Math.pow(pointStore.get(0).getqY(), 2) *
                        (Math.pow((pointStore.get(pointStore.size() - 1).getY_EOV() - pointStore.get(0).getY_EOV()), 2) / distance2) +

                Math.pow(pointStore.get(pointStore.size() - 1).getqX(), 2) *
                        (Math.pow((pointStore.get(pointStore.size() - 1).getX_EOV() - pointStore.get(0).getX_EOV()), 2) / distance2) +

                Math.pow(pointStore.get(0).getqX(), 2) *
                        (Math.pow((pointStore.get(pointStore.size() - 1).getX_EOV() - pointStore.get(0).getX_EOV()), 2) / distance2) );

        return (int) (100 * Math.sqrt(reliable)) / 100.0;

    }

    public double calcElevation(){
        if( 2 > pointStore.size() ){
            return 0.0;
        }
        double elevation = 0.0;

        for (int i = 0; i < pointStore.size() - 1; i++) {
            elevation += (pointStore.get(i + 1).getZ_EOV() - pointStore.get(i).getZ_EOV());
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
            area += pointStore.get(i).getY_EOV() * pointStore.get(i + 1).getX_EOV();
        }
        area += pointStore.get(pointStore.size() - 1).getY_EOV() * pointStore.get(0).getX_EOV();
        for (int i = 0; i < pointStore.size() - 1; i++) {
            area -= pointStore.get(i).getX_EOV() * pointStore.get(i + 1).getY_EOV();
        }
        area -= pointStore.get(pointStore.size() - 1).getX_EOV() * pointStore.get(0).getY_EOV();

        return (int) (10 * Math.abs(0.5 * area)) / 10.0;
    }

    public double calcAreaReliable(){
        if( 3 > pointStore.size()){
            return 0.0;
        }
        double reliable = 0.0;

        for ( int i = 0; i < pointStore.size() - 2; i++) {
          reliable += Math.pow(0.5 * (pointStore.get( i ).getX_EOV() - pointStore.get( i + 2 ).getX_EOV()), 2)
                  * Math.pow(pointStore.get( i + 1 ).getqY(), 2);
          reliable += Math.pow(0.5 * (pointStore.get( i ).getY_EOV() - pointStore.get( i + 2 ).getY_EOV()), 2)
                  * Math.pow(pointStore.get( i + 1 ).getqX(), 2);
        }
        reliable += Math.pow(0.5 * (pointStore.get(0).getX_EOV() - pointStore.get( pointStore.size() - 2).getX_EOV()), 2)
                * Math.pow(pointStore.get(pointStore.size() - 1).getqY(), 2);
        reliable += Math.pow(0.5 * (pointStore.get( pointStore.size() - 1 ).getX_EOV() - pointStore.get(1).getX_EOV()), 2)
                * Math.pow(pointStore.get(0).getqY(), 2);
        reliable += Math.pow(0.5 * (pointStore.get(0).getY_EOV() - pointStore.get( pointStore.size() - 2).getY_EOV()), 2)
                * Math.pow(pointStore.get(pointStore.size() - 1).getqX(), 2);
        reliable += Math.pow(0.5 * (pointStore.get( pointStore.size() - 1 ).getY_EOV() - pointStore.get(1).getY_EOV()), 2)
                * Math.pow(pointStore.get(0).getqX(), 2);

        return (int) (10 * Math.sqrt(reliable)) / 10.0;
    }

    public String getCalculatedData(){
        return "Távolság: " + calcDistance() + "m ±" + calcDistanceReliable() + "m\n" +
               "Kerület: " + calcPerimeter() + "m ±" + calcPerimeterReliable() + "m\n" +
               "Terület: " + calcArea() + "m2 ±" + calcAreaReliable() + "m2\n" +
               "Δm: " + calcElevation() + "m ±" + calcElevationReliable() + "m";
    }

}
