package com.david.giczi.gpsurvey;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.david.giczi.gpsurvey.databinding.FragmentMeasBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.AzimuthAndDistance;

import java.util.ArrayList;
import java.util.List;


public class MeasFragment extends Fragment {

    private FragmentMeasBinding binding;
    public static boolean IS_RUN_MEAS_PROCESS;
    private static List<MeasPoint> transformedMeasPointStore;
    private Canvas canvas;
    private Paint paint;
    private static float X_CENTER;
    private static float Y_CENTER;
    private static float MM;
    private static float SCALE;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMeasBinding.inflate(inflater, container, false);
        binding.buttonStartMeasure.setBackgroundColor(Color.DKGRAY);
        MainActivity.GO_MEAS_FRAGMENT = false;
        MeasFragment.MM = (float) (Math.sqrt(Math.pow(getResources().getDisplayMetrics().widthPixels, 2) +
                Math.pow(getResources().getDisplayMetrics().heightPixels, 2)) / 140F);
        MeasFragment.X_CENTER = getResources().getDisplayMetrics().widthPixels / 2F;
        MeasFragment.Y_CENTER = 89 * MM / 2F;
        displayMeasuredPoint();
        if( IS_RUN_MEAS_PROCESS && MainActivity.measuredDataWindow != null ){
            MainActivity.measuredDataWindow
                    .showAtLocation( binding.getRoot(), Gravity.CENTER, 0, 700);
        }
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonStartMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.MEAS_POINT = new MeasPoint(MainActivity.MEAS_POINT_LIST.size());
                IS_RUN_MEAS_PROCESS = true;
                popupMeasPointData();
            }
        });
    }
    private void popupMeasPointData() {
       ViewGroup measuredDataContainer = ((MainActivity) getActivity()).measuredDataContainer =
               (ViewGroup) getLayoutInflater().inflate(R.layout.fragment_meas_point, null);
       MainActivity.measuredDataWindow =
               new PopupWindow(measuredDataContainer, 1000, 670, false);
       MainActivity.measuredDataWindow.showAtLocation( binding.getRoot(), Gravity.CENTER, 0, 700);
       measuredDataContainer.findViewById(R.id.button_save_point).setBackgroundColor(Color.DKGRAY);
       TextView measuredDataView = measuredDataContainer.findViewById(R.id.measured_position);
       measuredDataView.setText(MainActivity.MEAS_POINT.toString());
       Button savePointButton = measuredDataContainer.findViewById(R.id.button_save_point);
       savePointButton.setOnClickListener( s -> {
           IS_RUN_MEAS_PROCESS = false;
           MainActivity.measuredDataWindow.dismiss();
           if( MainActivity.MEAS_POINT.isNotMeasured() ){
               return;
           }
           MainActivity.MEAS_POINT.getPreMeasPointData().clear();
           MainActivity.MEAS_POINT_LIST.add(MainActivity.MEAS_POINT);
           displayMeasuredPoint();
       });
    }

    private void displayMeasuredPoint(){
        init();
        setScaleValue();
        transformMeasPoints();
        canvas.drawText("M = 1:" + (int) SCALE, (float) (3 * MM), (float) (87 * MM), paint);
        paint.setTypeface(Typeface.DEFAULT);
        for (MeasPoint measPoint : transformedMeasPointStore) {
            canvas.drawText(String.valueOf(measPoint.getPointID()),
                    (float) measPoint.getY(), (float) (measPoint.getX() - 2 * MM), paint);
            canvas.drawText(getString(R.string.dot_symbol), (float) measPoint.getY(), (float) measPoint.getX(), paint);
        }
    }
    private void init(){
        Bitmap bitmap = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels,
                (int) (89 * MM), Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(50f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        binding.drawingMeasuredPoint.setImageBitmap(bitmap);
    }
    private void transformMeasPoints(){
        transformedMeasPointStore = new ArrayList<>();
        double Y = getMediumY();
        double X = getMediumX();
        for (MeasPoint measuredPoint : MainActivity.MEAS_POINT_LIST) {
            MeasPoint transformedPoint = new MeasPoint();
            transformedPoint.setPointID(measuredPoint.getPointID());
            transformedPoint.setY(X_CENTER + ((measuredPoint.getY() - Y) * 1000.0 * MM) / SCALE);
            transformedPoint.setX(Y_CENTER - ((measuredPoint.getX() - X) * 1000.0 * MM)  / SCALE);
            transformedMeasPointStore.add(transformedPoint);
        }
    }

    private double getMediumY(){
        return MainActivity.MEAS_POINT_LIST.stream().mapToDouble(MeasPoint::getY).summaryStatistics().getAverage();
    }
    private double getMediumX(){
        return MainActivity.MEAS_POINT_LIST.stream().mapToDouble(MeasPoint::getX).summaryStatistics().getAverage();
    }

    private void setScaleValue(){

        double theLongestDistance = getTheLongestDistance();
        if( 0.0 == theLongestDistance ){
            SCALE = 1f;
        }
        else if(1 >= theLongestDistance && 0.0 < theLongestDistance ){
            SCALE = 100f;
        }
        else if(10.0 >= theLongestDistance && 1.0 < theLongestDistance ){
            SCALE = 200f;
        }
        else if(50.0 >= theLongestDistance && 10.0 < theLongestDistance ){
            SCALE = 1000f;
        }
        else if(100.0 >= theLongestDistance && 50.0 < theLongestDistance ){
            SCALE = 2000f;
        }
        else {
            SCALE = 10000f;
        }

    }

    private double getTheLongestDistance(){
        double theLongestDistance = 0.0;
        for (MeasPoint measPoint1 : MainActivity.MEAS_POINT_LIST) {
            for (MeasPoint measPoint2 : MainActivity.MEAS_POINT_LIST) {
                double distance = new AzimuthAndDistance(measPoint1, measPoint2).calcDistance();
                if(  distance > theLongestDistance ){
                    theLongestDistance = distance;
                }
            }
        }

        return theLongestDistance;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        IS_RUN_MEAS_PROCESS = false;
        MainActivity.measuredDataWindow.dismiss();
        MainActivity.MEAS_POINT.getPreMeasPointData().clear();
        MainActivity.MEAS_POINT_LIST.add(MainActivity.MEAS_POINT);
        binding = null;
    }

}