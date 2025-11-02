package com.david.giczi.gpsurvey;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
        ElevationDiagram.X_ORIGIN = Y_CENTER - 45 * MM;
        ElevationDiagram.Y_ORIGIN = X_CENTER - 15 * MM;
        this.bitmap = Bitmap.createBitmap(mainActivity.getResources().getDisplayMetrics().widthPixels,
                mainActivity.getResources().getDisplayMetrics().heightPixels, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(Color.WHITE);
        popupElevationDiagram();
    }

    private boolean setElevationPointList() {
    MeasPoint topoCenter = getTopoCenterPoint();
     if( 2 > MainActivity.CHOSEN_MEAS_POINT_LIST.size() && 2 > MainActivity.MEAS_POINT_LIST.size() ){
         Toast.makeText(mainActivity, "A megjelenítéshez legalább 2 db pont szükséges.", Toast.LENGTH_SHORT).show();
         return false;
     }
     else if( topoCenter == null ){
         Toast.makeText(mainActivity, "Topocentrum pont megadása szükséges.", Toast.LENGTH_SHORT).show();
         return false;
     }
     this.elevationPointList = new ArrayList<>();
     if( !MainActivity.CHOSEN_MEAS_POINT_LIST.isEmpty() ){
         for (MeasPoint chosenMeasPoint : MainActivity.CHOSEN_MEAS_POINT_LIST) {
             elevationPointList.add( new ElevPoint(chosenMeasPoint.getPointID(),
                     calcDistance(topoCenter, chosenMeasPoint),
                     topoCenter.getZ_EOV() > 0 ? topoCenter.getZ_EOV() : 0) );
         }
     }
     else {
         for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
             elevationPointList.add( new ElevPoint(measPoint.getPointID(),
                     calcDistance(topoCenter, measPoint),
                     topoCenter.getZ_EOV() > 0 ? topoCenter.getZ_EOV() : 0) );
     }
        }

        Collections.sort(elevationPointList);

     return true;
    }

    private MeasPoint getTopoCenterPoint(){
        MeasPoint topoCenter = null;
        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            if( measPoint.isTopoCenter() ){
                topoCenter = measPoint;
            }
        }
        return  topoCenter;
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
        container.findViewById(R.id.button_ok).setOnClickListener(b -> elevationDiagramWindow.dismiss());
    }

    private void drawElevationDiagramSystem(){
        paint.setColor(ContextCompat.getColor(mainActivity, R.color.steel_gray));
        paint.setStrokeWidth(5);
        canvas.drawLine(X_CENTER - 20 * MM, Y_CENTER - 45 * MM, X_CENTER - 20 * MM, Y_CENTER + 55 * MM , paint);
        canvas.drawLine(X_CENTER - 20 * MM, Y_CENTER - 45 * MM, X_CENTER - 19 * MM, Y_CENTER - 45 * MM , paint);
        canvas.drawLine(X_CENTER - 20 * MM, Y_CENTER  + 5 * MM, X_CENTER - 19 * MM, Y_CENTER + 5 * MM , paint);
        canvas.drawLine(X_CENTER - 20 * MM, Y_CENTER + 55 * MM, X_CENTER - 19 * MM, Y_CENTER + 55 * MM , paint);

        canvas.drawLine(X_CENTER - 15 * MM, Y_CENTER - 50 * MM, X_CENTER + 25 * MM, Y_CENTER - 50 * MM , paint);
        canvas.drawLine(X_CENTER - 15 * MM, Y_CENTER - 50 * MM, X_CENTER - 15 * MM, Y_CENTER - 51 * MM , paint);
        canvas.drawLine(X_CENTER + 25 * MM, Y_CENTER - 50 * MM, X_CENTER + 25 * MM, Y_CENTER - 51 * MM , paint);
        canvas.drawLine(X_CENTER + 5 * MM, Y_CENTER - 50 * MM, X_CENTER + 5 * MM, Y_CENTER - 51 * MM , paint);

    }




}
