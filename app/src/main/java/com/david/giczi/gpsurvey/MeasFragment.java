package com.david.giczi.gpsurvey;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.david.giczi.gpsurvey.databinding.FragmentMeasBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.AzimuthAndDistance;

import java.util.ArrayList;
import java.util.List;


public class MeasFragment extends Fragment {

    private FragmentMeasBinding binding;
    public static boolean IS_SAVED_MEAS_POINT;
    private static List<MeasPoint> transformedMeasPointStore;
    private Bitmap bitmap;
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
        binding.buttonSave.setBackgroundColor(Color.DKGRAY);
        MainActivity.GO_MEAS_FRAGMENT = false;
        MeasFragment.MM = (float) (Math.sqrt(Math.pow(getResources().getDisplayMetrics().widthPixels, 2) +
                Math.pow(getResources().getDisplayMetrics().heightPixels, 2)) / 140F);
        MeasFragment.X_CENTER = getResources().getDisplayMetrics().widthPixels / 2F;
        MeasFragment.Y_CENTER = 89 * MM / 2F;
        this.bitmap = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels,
                (int) (89 * MM), Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.paint = new Paint();
        canvas.drawColor(Color.WHITE);
        binding.drawingMeasuredPoint.setImageBitmap(bitmap);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IS_SAVED_MEAS_POINT = true;
                binding.buttonSave.setEnabled(false);
                displayMeasuredPointData();
            }
        });
    }

    private void displayMeasuredPointData(){

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if( IS_SAVED_MEAS_POINT ){
                    Toast.makeText(getContext(), "Pont mentése sikertelen", Toast.LENGTH_SHORT).show();
                    IS_SAVED_MEAS_POINT = false;
                    MainActivity.PRE_MEAS_POINT_LIST.clear();
                }
                else{
                    Toast.makeText(getContext(),
                         MainActivity.MEAS_POINT_LIST.size() + ". pont mentve", Toast.LENGTH_SHORT).show();
                   displayPoint();
                }
                binding.buttonSave.setEnabled(true);
            }
        }, 4000);

    }

    private void displayPoint(){
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.WHITE);
        canvas.drawText("M= 1:" + (int) SCALE, (float) (1 * MM), (float) (85 * MM) ,paint);
        setScaleValue();
        transformMeasPoints();
        paint.setTextSize(50F);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT);
        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            canvas.drawText(getString(R.string.dot_symbol),
                    (float) measPoint.getY(), (float) measPoint.getX(), paint);
            canvas.drawText(String.valueOf(measPoint.getPointID()),
                    (float) measPoint.getY(), (float) measPoint.getX() - 2 * MM, paint);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
        }
            canvas.drawText("M= 1:" + (int) SCALE, (float) (2 * MM), (float) (87 * MM) ,paint);
    }


    private void transformMeasPoints(){
        transformedMeasPointStore = new ArrayList<>();
        double Y = MainActivity.MEAS_POINT_LIST.get(0).getY();
        double X = MainActivity.MEAS_POINT_LIST.get(0).getX();
        for (MeasPoint measuredPoint : MainActivity.MEAS_POINT_LIST) {
            MeasPoint transformedPoint = new MeasPoint();
            transformedPoint.setPointID(measuredPoint.getPointID());
            transformedPoint.setY(X_CENTER + ((measuredPoint.getY() - Y) * 1000.0 * MM) / SCALE);
            transformedPoint.setX(Y_CENTER - ((measuredPoint.getX() - X) * 1000.0 * MM)  / SCALE);
            transformedMeasPointStore.add(transformedPoint);
        }
    }

    private void setScaleValue(){

        double theLongestDistance = getTheLongestDistance();
        if( 0.0 == theLongestDistance ){
            SCALE = 1f;
        }
        else if(1 > theLongestDistance && 0.0 < theLongestDistance ){
            SCALE = 100f;
        }
        else if(10.0 > theLongestDistance && 1.0 < theLongestDistance ){
            SCALE = 200f;
        }
        else if(20.0 > theLongestDistance && 10.0 < theLongestDistance ){
            SCALE = 500f;
        }
        else{
            SCALE = 1000f;
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
        binding = null;
    }

}