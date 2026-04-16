package com.david.giczi.gpsurvey.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.david.giczi.gpsurvey.MainActivity;
import com.david.giczi.gpsurvey.domain.MeasPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    public AppExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        saveCrashReport(e);
        System.exit(1);
    }


    private void saveCrashReport(Throwable e) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "/Documents/" + "GPSurvey_crash_log.txt");
            FileWriter writer = new FileWriter(file, true);
            writer.write("\n\n=== Crash ===\n");
            writer.write(Log.getStackTraceString(e));
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveAllMeasPoints(){
        File projectFile =
                new File(Environment.getExternalStorageDirectory(),
                        "/Documents/" + "SavedPoints.txt");
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(projectFile));
            int pointId = 1;
            for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
                bw.write(pointId + ";" + measPoint.getWGSMeasPointDataInDecimalFormatSeparatedBySemiColon());
                bw.newLine();
                pointId++;
            }
            bw.close();
        } catch (IOException e) {
            Toast.makeText(context, projectFile.getName() +
                    "\nfájl mentése sikertelen.", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(context,
                "Pont adatok fájl mentve:\n"
                        + projectFile.getName() , Toast.LENGTH_LONG).show();
    }
}
