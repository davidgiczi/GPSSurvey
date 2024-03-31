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
    private static double SCALE;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMeasBinding.inflate(inflater, container, false);
        binding.buttonStartMeasure.setBackgroundColor(Color.DKGRAY);
        MeasFragment.MM = (float) (Math.sqrt(Math.pow(getResources().getDisplayMetrics().widthPixels, 2) +
                Math.pow(getResources().getDisplayMetrics().heightPixels, 2)) / 140F);
        MeasFragment.X_CENTER = getResources().getDisplayMetrics().widthPixels / 2F;
        MeasFragment.Y_CENTER = 89 * MM / 2F;
        displayMeasuredPoint();
        if( IS_RUN_MEAS_PROCESS && MainActivity.measuredDataWindow != null ){
            MainActivity.measuredDataWindow
                    .showAtLocation( binding.getRoot(), Gravity.CENTER, 0, 700);
        }
        MainActivity.PAGE_NUMBER_VALUE = 1;
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonStartMeasure.setOnClickListener(meas -> {
            IS_RUN_MEAS_PROCESS = true;
            if( MainActivity.MEAS_POINT_LIST.isEmpty() ){
                MainActivity.NEXT_POINT_NUMBER = 1;
            }
            else {
                MainActivity.NEXT_POINT_NUMBER++;
            }
            MainActivity.MEAS_POINT = new MeasPoint(MainActivity.NEXT_POINT_NUMBER);
            popupMeasPointData();
        });
    }

    private void popupMeasPointData() {
       ViewGroup measuredDataContainer = ((MainActivity) requireActivity()).measuredDataContainer =
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
        canvas.drawText("M = 1:" + (int) SCALE, 3 * MM, 87 * MM, paint);
        paint.setTypeface(Typeface.DEFAULT);
        for (MeasPoint measPoint : transformedMeasPointStore) {
            canvas.drawText(getString(R.string.dot_symbol), (float) measPoint.getY(), (float) measPoint.getX(), paint);
            canvas.drawText(String.valueOf(measPoint.getPointID()),
                    (float) measPoint.getY(), (float) (measPoint.getX() - 2 * MM), paint);
        }
    }
    private void init(){
        Bitmap bitmap = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels,
                (int) (89 * MM), Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
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
        if( 2 > MainActivity.MEAS_POINT_LIST.size() ){
            SCALE = 100.0;
        }
        else {
            SCALE = getTheLongestDistance() / 0.05;
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
        if( MainActivity.measuredDataWindow != null ){
        MainActivity.measuredDataWindow.dismiss();
        }
        MainActivity.MEAS_POINT = null;
        binding = null;
    }

}