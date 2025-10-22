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

public class ElevationDiagram {

    private final MainActivity mainActivity;
    private final Bitmap bitmap;
    private final Canvas canvas;
    private final Paint paint;
    private static float MM;
    private static float SCALE;

    public ElevationDiagram(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        ElevationDiagram.MM = (float) (Math.sqrt(Math.pow(mainActivity.getResources().getDisplayMetrics().widthPixels, 2) +
                Math.pow(mainActivity.getResources().getDisplayMetrics().heightPixels, 2)) / 140F);
        this.bitmap = Bitmap.createBitmap(mainActivity.getResources().getDisplayMetrics().widthPixels,
                mainActivity.getResources().getDisplayMetrics().heightPixels, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(Color.WHITE);
    }

    public void popupElevationDiagram() {
        @SuppressLint("InflateParams") ViewGroup container =
                (ViewGroup) mainActivity.getLayoutInflater().inflate(R.layout.fragment_elevation_diagram, null);
        PopupWindow elevationDiagramWindow = new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);
        elevationDiagramWindow.showAtLocation(mainActivity.binding.getRoot(), Gravity.CENTER, 0, 0);
        ((ImageView)  container.findViewById(R.id.elevation_diagram)).setImageBitmap(bitmap);
        container.findViewById(R.id.button_ok).setOnClickListener(b -> elevationDiagramWindow.dismiss());
    }




}
