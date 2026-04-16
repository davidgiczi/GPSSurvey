package com.david.giczi.gpsurvey.utils;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.david.giczi.gpsurvey.domain.MeasPoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WrapDataInKML extends Fragment {

    private List<MeasPoint> measPointList;
    private String dataType;
    private String fileName;
    private List<String> kmlDataList;

    public WrapDataInKML(List<MeasPoint> measPointList, String dataType, String fileName) {
        int pointId = 1;
        for (MeasPoint measPoint : measPointList) {
            measPoint.setPointID(String.valueOf(pointId));
            pointId++;
        }
        this.measPointList = measPointList;
        this.dataType = dataType;
        this.fileName = fileName;
    }
    public List<String> getKmlDataList() {
        return kmlDataList;
    }
    public void createDataListForKML(Context context, boolean isAppendData){
        if( isAppendData ){
            kmlDataList.remove(kmlDataList.size() - 1);
            kmlDataList.remove( kmlDataList.size() - 1);
        }
        else {
            getTemplateDataForKML(context);
        }
        switch (dataType) {
            case "Pontok":
                wrapPointsInKML();
                break;
            case "Vonal":
                wrapPointsForLineInKML();
                break;
            case "Kerület":
                wrapPointsForPerimeterInKML();
        }
        closeKMLDataFile();
    }

    public String getFileName() {
        return fileName;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMeasPointList(List<MeasPoint> measPointList) {
        this.measPointList = measPointList;
    }

    private void getTemplateDataForKML(Context context)  {
        kmlDataList = new ArrayList<>();
        try(InputStream is = context.getAssets().open("template.kml");
            BufferedReader bf = new BufferedReader(new InputStreamReader(is)) ) {
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
    }

    private void wrapPoint(MeasPoint measPoint){
       kmlDataList.add("<Placemark>");
              kmlDataList.add( "<name>" + measPoint.getPointIDForKML() + "</name>");
              kmlDataList.add("<description><![CDATA[Name=" + measPoint.getPointIDForKML() + "]]></description>");
              kmlDataList.add("<styleUrl>#placemark</styleUrl>");
              kmlDataList.add("<Point>");
              kmlDataList.add("<coordinates>" + measPoint.getWGSMeasPointDataInDecimalFormatSeparatedByComma() + "</coordinates>");
              kmlDataList.add("</Point>");
              kmlDataList.add("</Placemark>");
    }

    private void wrapPointsForLineInKML(){
        kmlDataList.add("<Folder>");
        kmlDataList.add("<name>Line</name>");
        kmlDataList.add("<Placemark>");
        kmlDataList.add( "<name>" +
                measPointList.get(0).getPointIDForKML() + "-" +
                measPointList.get(measPointList.size() - 1).getPointIDForKML() +  "_track</name>");
        kmlDataList.add("<styleUrl>#linestyle</styleUrl>");
        kmlDataList.add("<LineString>");
        kmlDataList.add("<tessellate>1</tessellate>");
        kmlDataList.add("<coordinates>");
        for (MeasPoint measPoint : measPointList) {
            kmlDataList.add(measPoint.getWGSMeasPointDataInDecimalFormatSeparatedByComma());
        }
        kmlDataList.add("</coordinates>");
        kmlDataList.add("</LineString>");
        kmlDataList.add("</Placemark>");
        kmlDataList.add("</Folder>");
    }

    private void wrapPointsForPerimeterInKML(){
        kmlDataList.add("<Folder>");
        kmlDataList.add("<name>Perimeter</name>");
        kmlDataList.add("<Placemark>");
        kmlDataList.add( "<name>" +
                measPointList.get(0).getPointIDForKML() + "-" +
                measPointList.get(measPointList.size() - 1).getPointIDForKML() +  "_perimeter</name>");
        kmlDataList.add("<styleUrl>#linestyle</styleUrl>");
        kmlDataList.add("<LineString>");
        kmlDataList.add("<tessellate>1</tessellate>");
        kmlDataList.add("<coordinates>");
        for (MeasPoint measPoint : measPointList) {
            kmlDataList.add(measPoint.getWGSMeasPointDataInDecimalFormatSeparatedByComma());
        }
        kmlDataList.add(measPointList.get(0).getWGSMeasPointDataInDecimalFormatSeparatedByComma());
        kmlDataList.add("</coordinates>");
        kmlDataList.add("</LineString>");
        kmlDataList.add("</Placemark>");
        kmlDataList.add("</Folder>");
    }
    private void closeKMLDataFile(){
        kmlDataList.add("</Document>");
        kmlDataList.add("</kml>");
    }

}
