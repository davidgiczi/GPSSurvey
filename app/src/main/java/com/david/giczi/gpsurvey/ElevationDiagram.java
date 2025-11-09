package com.david.giczi.gpsurvey;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.david.giczi.gpsurvey.domain.ElevPoint;
import com.david.giczi.gpsurvey.domain.MeasPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElevationDiagram {

    private final MainActivity mainActivity;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private List<ElevPoint> elevationPointList;
    private int HR_SCALE;
    private int VR_SCALE;
    private static float X_CENTER;
    private static float Y_CENTER;
    private static float X_ORIGIN;
    private static float Y_ORIGIN;
    private static float MM;


    public ElevationDiagram(MainActivity mainActivity){
        this.mainActivity = mainActivity;
      if( !setElevationPointList() ){
          return;
      }
        ElevationDiagram.MM = (float) (mainActivity.getResources().getDisplayMetrics().xdpi / 25.4);
        ElevationDiagram.X_CENTER = mainActivity.getResources().getDisplayMetrics().widthPixels / 2f;
        ElevationDiagram.Y_CENTER = mainActivity.getResources().getDisplayMetrics().heightPixels / 2f;
        ElevationDiagram.X_ORIGIN = X_CENTER - 15 * MM;
        ElevationDiagram.Y_ORIGIN = Y_CENTER - 45 * MM;
        this.bitmap = Bitmap.createBitmap(mainActivity.getResources().getDisplayMetrics().widthPixels,
                mainActivity.getResources().getDisplayMetrics().heightPixels, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(Color.WHITE);
        popupElevationDiagram();
    }

    private boolean setElevationPointList() {

     if( 2 > MainActivity.CHOSEN_MEAS_POINT_LIST.size() && 2 > MainActivity.MEAS_POINT_LIST.size() ){
         Toast.makeText(mainActivity, "A megjelenítéshez legalább 2 db pont szükséges.", Toast.LENGTH_SHORT).show();
         return false;
     }
     this.elevationPointList = new ArrayList<>();
     double distance = 0d;
     if( !MainActivity.CHOSEN_MEAS_POINT_LIST.isEmpty() ){
         elevationPointList.add(new ElevPoint(MainActivity.CHOSEN_MEAS_POINT_LIST.get(0).getPointID(),
                 distance,
                 MainActivity.CHOSEN_MEAS_POINT_LIST.get(0).getZ_EOV() > 0 ?
                 MainActivity.CHOSEN_MEAS_POINT_LIST.get(0).getZ_EOV() : 0));
         for (int i = 0; i < MainActivity.CHOSEN_MEAS_POINT_LIST.size() - 1; i++) {
             distance = MainActivity.CHOSEN_MEAS_POINT_LIST.get(i + 1).getEAST() -
                        MainActivity.CHOSEN_MEAS_POINT_LIST.get(i).getEAST() >= 0 ?
                        distance + calcDistance(MainActivity.CHOSEN_MEAS_POINT_LIST.get(i),
                                    MainActivity.CHOSEN_MEAS_POINT_LIST.get(i + 1)) :
                        distance - calcDistance(MainActivity.CHOSEN_MEAS_POINT_LIST.get(i),
                             MainActivity.CHOSEN_MEAS_POINT_LIST.get(i + 1));
             elevationPointList.add(
                     new ElevPoint(MainActivity.CHOSEN_MEAS_POINT_LIST.get(i + 1).getPointID(),
                             distance,
                        MainActivity.CHOSEN_MEAS_POINT_LIST.get(i + 1).getZ_EOV() > 0 ?
                        MainActivity.CHOSEN_MEAS_POINT_LIST.get(i + 1).getZ_EOV() : 0));
         }
     }
     else {
         elevationPointList.add(new ElevPoint(MainActivity.MEAS_POINT_LIST.get(0).getPointID(),
                 distance,
                 MainActivity.MEAS_POINT_LIST.get(0).getZ_EOV() > 0 ?
                         MainActivity.MEAS_POINT_LIST.get(0).getZ_EOV() : 0));
         for (int i = 0; i < MainActivity.MEAS_POINT_LIST.size() - 1; i++) {
             distance = MainActivity.MEAS_POINT_LIST.get(i + 1).getEAST() -
                     MainActivity.MEAS_POINT_LIST.get(i).getEAST() >= 0 ?
                     distance + calcDistance(MainActivity.MEAS_POINT_LIST.get(i),
                             MainActivity.MEAS_POINT_LIST.get(i + 1)) :
                     distance - calcDistance(MainActivity.MEAS_POINT_LIST.get(i),
                             MainActivity.MEAS_POINT_LIST.get(i + 1));
             elevationPointList.add(
                     new ElevPoint(MainActivity.MEAS_POINT_LIST.get(i + 1).getPointID(),
                             distance,
                             MainActivity.MEAS_POINT_LIST.get(i + 1).getZ_EOV() > 0 ?
                                     MainActivity.MEAS_POINT_LIST.get(i + 1).getZ_EOV() : 0));
         }
     }
        Collections.sort(elevationPointList);
     return true;
    }

    private double calcDistance(MeasPoint startPoint, MeasPoint endPoint){
        return Math.sqrt(Math.pow(startPoint.getEAST() - endPoint.getEAST(), 2) +
                Math.pow(startPoint.getNORTH() - endPoint.getNORTH(), 2));
    }

    private void popupElevationDiagram() {
        @SuppressLint("InflateParams") ViewGroup container =
                (ViewGroup) mainActivity.getLayoutInflater().inflate(R.layout.fragment_elevation_diagram, null);
        PopupWindow elevationDiagramWindow = new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);
        elevationDiagramWindow.showAtLocation(mainActivity.binding.getRoot(), Gravity.CENTER, 0, 0);
        ((ImageView)  container.findViewById(R.id.elevation_diagram)).setImageBitmap(bitmap);
        drawElevationDiagramSystem();
        displayPoints();
        container.findViewById(R.id.button_ok).setOnClickListener(b -> elevationDiagramWindow.dismiss());
    }

    private void drawElevationDiagramSystem(){
        paint.setColor(ContextCompat.getColor(mainActivity, R.color.steel_gray));
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        canvas.drawLine(X_ORIGIN - 5 * MM, Y_ORIGIN, X_ORIGIN - 5 * MM, Y_ORIGIN + 100 * MM , paint);
        canvas.drawLine(X_ORIGIN - 5 * MM, Y_ORIGIN + 0.1f * MM, X_ORIGIN - 4 * MM, Y_ORIGIN + 0.1f * MM , paint);
        canvas.drawLine(X_ORIGIN - 5 * MM, Y_ORIGIN  + 25 * MM, X_ORIGIN - 4.5f * MM, Y_ORIGIN + 25 * MM , paint);
        canvas.drawLine(X_ORIGIN - 5 * MM, Y_ORIGIN  + 50 * MM, X_ORIGIN - 4 * MM, Y_ORIGIN + 50 * MM , paint);
        canvas.drawLine(X_ORIGIN - 5 * MM, Y_ORIGIN  + 75 * MM, X_ORIGIN - 4.5f * MM, Y_ORIGIN + 75 * MM , paint);
        canvas.drawLine(X_ORIGIN - 5 * MM, Y_ORIGIN + 99.9f * MM, X_ORIGIN - 4 * MM, Y_ORIGIN + 99.9f * MM , paint);
        canvas.drawLine(X_ORIGIN, Y_ORIGIN - 5 * MM, X_ORIGIN + 40 * MM, Y_ORIGIN - 5 * MM , paint);
        canvas.drawLine(X_ORIGIN + 0.1f * MM, Y_ORIGIN - 5 * MM, X_ORIGIN + 0.1f * MM, Y_ORIGIN - 6 * MM , paint);
        canvas.drawLine(X_ORIGIN + 10 * MM, Y_ORIGIN - 5 * MM, X_ORIGIN + 10 * MM, Y_ORIGIN - 5.5f * MM , paint);
        canvas.drawLine(X_ORIGIN + 20 * MM, Y_ORIGIN - 5 * MM, X_ORIGIN + 20 * MM, Y_ORIGIN - 6 * MM , paint);
        canvas.drawLine(X_ORIGIN + 30 * MM, Y_ORIGIN - 5 * MM, X_ORIGIN + 30 * MM, Y_ORIGIN - 5.5f * MM , paint);
        canvas.drawLine(X_ORIGIN + 39.9f * MM, Y_ORIGIN - 5 * MM, X_ORIGIN + 39.9f * MM, Y_ORIGIN - 6 * MM , paint);
    }

    private void displayPoints(){
        paint.setColor(Color.BLACK);
        paint.setTextSize(50f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        transformationPointDataForScreen();
        for (ElevPoint elevPoint : elevationPointList) {
            canvas.save();
            canvas.rotate(90, elevPoint.getX_onScreen(), elevPoint.getY_onScreen());
            canvas.drawText(mainActivity.getString(R.string.dot_symbol), elevPoint.getX_onScreen(), elevPoint.getY_onScreen(), paint);
            canvas.drawText(elevPoint.getPointID(), elevPoint.getX_onScreen(), elevPoint.getY_onScreen(), paint);
            canvas.restore();
        }
    }

    private void transformationPointDataForScreen(){
        setHorizontalScaleValue();
        setVerticalScaleValue();
        for (ElevPoint elevPoint : elevationPointList) {
            elevPoint.setX_onScreen((float) (X_ORIGIN +
                    ((elevPoint.getElevation() - getTheStartElevationValue()) / VR_SCALE) * 1000 * MM));
          elevPoint.setY_onScreen((float) (Y_ORIGIN +
                  ((elevPoint.getDistance() - elevationPointList.get(0).getDistance()) / HR_SCALE) * 1000 * MM));
        }
    }

    private void setHorizontalScaleValue(){
        this.HR_SCALE = 10 * (int)  Math.ceil( getHorizontalAxisDistance() );
    }

    private double getHorizontalAxisDistance(){
        return Math.abs(elevationPointList.get(0).getDistance()) +
                Math.abs(elevationPointList.get(elevationPointList.size() - 1).getDistance());
    }

    private void setVerticalScaleValue(){
    this.VR_SCALE = (int) Math.ceil((getTheTopElevationValue() - getTheStartElevationValue()) / 0.04);
    }

    private int getTheStartElevationValue() {
        return (int) Math.floor(elevationPointList.stream().mapToDouble(ElevPoint::getElevation).min().orElse(0.0));
    }

    private int getTheTopElevationValue(){
            return (int) Math.ceil(elevationPointList.stream().mapToDouble(ElevPoint::getElevation).max().orElse(0.0));
    }

}
