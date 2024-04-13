package com.david.giczi.gpsurvey.utils;

import com.david.giczi.gpsurvey.MainActivity;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WrapDataInKML {

    private final List<MeasPoint> measPointList;
    private final String dataType;
    private final String fileName;
    private List<String> kmlDataList;


    public WrapDataInKML(List<MeasPoint> measPointList, String dataType, String fileName) {
        this.measPointList = measPointList;
        this.dataType = dataType;
        this.fileName = fileName;
    }

    public List<String> getKmlDataList() {
        return kmlDataList;
    }
    public void createDataListForKML(){
        getTemplateDataForKML();
        if( dataType.equals("Pontok") ){
            wrapPointsInKML();
        }
        else if( dataType.equals("Vonal") ){
            wrapPointsForLineInKML();
        }
        else if( dataType.equals("Kerület") ){
            wrapPointsForPerimeterInKML();
        }
    }

    private void getTemplateDataForKML()  {
        kmlDataList = new ArrayList<>();
        try(InputStream is = MainActivity.CONTEXT.getAssets().open("template.kml");
            BufferedReader bf = new BufferedReader(new InputStreamReader(is))) {
            String row;
            while((row = bf.readLine()) != null){
                if( row.contains("<name>") ){
                    kmlDataList.add("<name>" + fileName + "</name>");
                    continue;
                }
                kmlDataList.add(row.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void wrapPointsInKML(){
        kmlDataList.add("<Folder>");
        kmlDataList.add("<name>Points</name>");
        for (MeasPoint measPoint : measPointList) {
           wrapPoint(measPoint);
        }
        kmlDataList.add("</Folder>");
        kmlDataList.add("</Document>");
        kmlDataList.add("</kml>");
    }

    private void wrapPoint(MeasPoint measPoint){
       kmlDataList.add("<Placemark>");
              kmlDataList.add( "<name>" + measPoint.getPointID() + "</name>");
              kmlDataList.add("<description><![CDATA[Name=" + measPoint.getPointID() + "]]></description>");
              kmlDataList.add("<styleUrl>#placemark</styleUrl>");
              kmlDataList.add("<Point>");
              kmlDataList.add("<coordinates>" + measPoint.getWGSMeasPointDataInDecimalFormat() + "</coordinates>");
              kmlDataList.add("</Point>");
              kmlDataList.add("</Placemark>");
    }

    private void wrapPointsForLineInKML(){
        kmlDataList.add("<Folder>");
        kmlDataList.add("<name>Line</name>");
        kmlDataList.add("<Placemark>");
        kmlDataList.add( "<name>" +
                measPointList.get(0).getPointID() + "-" +
                measPointList.get(measPointList.size() - 1).getPointID() +  "_track</name>");
        kmlDataList.add("<styleUrl>#linestyle</styleUrl>");
        kmlDataList.add("<LineString>");
        kmlDataList.add("<tessellate>1</tessellate>");
        kmlDataList.add("<coordinates>");
        for (MeasPoint measPoint : measPointList) {
            kmlDataList.add(measPoint.getWGSMeasPointDataInDecimalFormat());
        }
        kmlDataList.add("</coordinates>");
        kmlDataList.add("</LineString>");
        kmlDataList.add("</Placemark>");
        kmlDataList.add("</Folder>");
        kmlDataList.add("</Document>");
        kmlDataList.add("</kml>");
    }

    private void wrapPointsForPerimeterInKML(){
        kmlDataList.add("<Folder>");
        kmlDataList.add("<name>Line</name>");
        kmlDataList.add("<Placemark>");
        kmlDataList.add( "<name>" +
                measPointList.get(0).getPointID() + "-" +
                measPointList.get(measPointList.size() - 1).getPointID() +  "_perimeter</name>");
        kmlDataList.add("<styleUrl>#linestyle</styleUrl>");
        kmlDataList.add("<LineString>");
        kmlDataList.add("<tessellate>1</tessellate>");
        kmlDataList.add("<coordinates>");
        for (MeasPoint measPoint : measPointList) {
            kmlDataList.add(measPoint.getWGSMeasPointDataInDecimalFormat());
        }
        kmlDataList.add(measPointList.get(0).getWGSMeasPointDataInDecimalFormat());
        kmlDataList.add("</coordinates>");
        kmlDataList.add("</LineString>");
        kmlDataList.add("</Placemark>");
        kmlDataList.add("</Folder>");
        kmlDataList.add("</Document>");
        kmlDataList.add("</kml>");
    }

}
