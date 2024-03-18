package com.david.giczi.gpsurvey;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.david.giczi.gpsurvey.databinding.FragmentMeasBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;

import java.util.ArrayList;
import java.util.List;


public class MeasFragment extends Fragment {

    private FragmentMeasBinding binding;
    public static boolean IS_SAVE_POINT;
    private static List<MeasPoint> transformedMeasPointStore;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private static float X_CENTER;
    private static float Y_CENTER;
    private static float MM;
    private static float SCALE = 1F;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMeasBinding.inflate(inflater, container, false);
        MainActivity.GO_MEAS_FRAGMENT = false;
        MeasFragment.X_CENTER = getResources().getDisplayMetrics().widthPixels / 2F;
        MeasFragment.Y_CENTER = getResources().getDisplayMetrics().heightPixels / 2F;
        MeasFragment.MM = (float) (Math.sqrt(Math.pow(getResources().getDisplayMetrics().widthPixels, 2) +
                Math.pow(getResources().getDisplayMetrics().heightPixels, 2)) / 140F);
        this.bitmap = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.paint = new Paint();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IS_SAVE_POINT = true;
            }
        });
    }
    private void transformMeasPoints(){
        transformedMeasPointStore = new ArrayList<>();
        double Y = MainActivity.MEAS_POINT_LIST.get(0).getY();
        double X = MainActivity.MEAS_POINT_LIST.get(0).getX();
        for (MeasPoint measuredPoint : MainActivity.MEAS_POINT_LIST) {
            MeasPoint transformedPoint = new MeasPoint(measuredPoint.getPointID(),
                    X_CENTER + ((measuredPoint.getY() - Y) * 1000.0 * MM) / SCALE,
                    Y_CENTER - ((measuredPoint.getX() - X) * 1000.0 * MM)  / SCALE,
                    measuredPoint.getZ());
            transformedMeasPointStore.add(transformedPoint);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}