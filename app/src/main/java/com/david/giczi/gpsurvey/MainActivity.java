package com.david.giczi.gpsurvey;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.david.giczi.gpsurvey.databinding.ActivityMainBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.domain.TopoCentricPoint;
import com.david.giczi.gpsurvey.utils.AppExceptionHandler;
import com.david.giczi.gpsurvey.utils.EOV;
import com.david.giczi.gpsurvey.utils.WGS84;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private AppBarConfiguration appBarConfiguration;
    public ActivityMainBinding binding;
    public LocationManager locationManager;
    public LocationListener locationListener;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private float declination;
    private float[] gravityValues = new float[3];
    private float[] geomagneticValues = new float[3];
    private ViewGroup compassContainer;
    public ViewGroup measuredDataContainer;
    public static PopupWindow measuredDataWindow;
    private static final int REQUEST_LOCATION = 1;
    public static List<MeasPoint> MEAS_POINT_LIST;
    public static List<MeasPoint> CHOSEN_MEAS_POINT_LIST;
    public static MeasPoint MEAS_POINT;
    public static MeasPoint STANDING_POINT;
    public static float GPS_ACCURACY;
    public static int NEXT_POINT_NUMBER;
    public static int PAGE_NUMBER_VALUE;
    public static double AZIMUTH;
    private boolean decimalFormat = true;
    private boolean angleMinSecFormat;
    private boolean xyzFormat;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler(this));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationListener == null) {
            startMeasureDialog();
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationListener != null) {
            Toast.makeText(this, "GPS elindítva..", Toast.LENGTH_SHORT).show();
        }
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        MEAS_POINT_LIST = new ArrayList<>();
        CHOSEN_MEAS_POINT_LIST = new ArrayList<>();
        activityResultLauncher =  registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if( result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            readInputPointData(uri);
                        }
                    }
                });
    }

    @SuppressLint("InflateParams")
    private void popupCompassWindow() {
        compassContainer = (ViewGroup) getLayoutInflater().inflate(R.layout.fragment_compass, null);
        PopupWindow compassWindow = new PopupWindow(compassContainer, 600, 600, true);
        compassWindow.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        ImageView compassView = compassContainer.findViewById(R.id.compass);
        compassView.setImageResource(R.drawable.compass);
    }

    private void rotateCompass(float rotateAngle) {
        if (compassContainer == null) {
            return;
        }
        ImageView compassView = compassContainer.findViewById(R.id.compass);
        compassView.setRotation(rotateAngle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.exit_option) {
            exitDialog();
        } else if (id == R.id.point_measure_option) {

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationListener != null) {
                Toast.makeText(this, "GPS elindítva..", Toast.LENGTH_SHORT).show();
            } else {
                startMeasure();
            }
            navigateToMeasFragment();
        } else if (id == R.id.decimal_format) {
            decimalFormat = true;
            angleMinSecFormat = false;
            xyzFormat = false;
        } else if (id == R.id.xyz_format) {
            xyzFormat = true;
            decimalFormat = false;
            angleMinSecFormat = false;
        } else if (id == R.id.angle_min_sec_format) {
            angleMinSecFormat = true;
            decimalFormat = false;
            xyzFormat = false;
        } else if (id == R.id.compass_option) {
            popupCompassWindow();
        }
        else if( id == R.id.input_points_option ){
            navigateToStartFragment();
            inputPointDataDialog();
        } else if (id == R.id.calc_option) {
            navigateToCalcFragment();
        } else if (id == R.id.finding_point_option) {
            navigateToFindPointFragment();
        }
        else if( id == R.id.elevation_diagram){
            new ElevationDiagram(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToFindPointFragment() {
        switch (PAGE_NUMBER_VALUE) {
            case 0:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_StartFragment_to_FindPointFragment);
                break;
            case 1:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_MeasFragment_to_FindPointFragment);
                break;
            case 2:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_CalcFragment_to_FindPointFragment);
                break;

        }
    }

    private void navigateToStartFragment() {
        switch (PAGE_NUMBER_VALUE) {
            case 1:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_MeasFragment_to_StartFragment);
                break;
            case 2:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_CalcFragment_to_StartFragment);
                break;
            case 3:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_FindPointFragment_to_StartFragment);
                break;
        }
    }

    private void navigateToMeasFragment() {
        switch (PAGE_NUMBER_VALUE) {
            case 0:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_StartFragment_to_MeasFragment);
                break;
            case 2:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_CalcFragment_to_MeasFragment);
                break;
            case 3:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_FindPointFragment_to_MeasFragment);
                break;
        }
    }

    private void navigateToCalcFragment() {
        switch (PAGE_NUMBER_VALUE) {
            case 0:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_StartFragment_to_CalcFragment);
                break;
            case 1:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_MeasFragment_to_CalcFragment);
                break;
            case 3:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_FindPointFragment_to_CalcFragment);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static String convertAngleMinSecFormat(double data) {

        int angle = (int) data;
        int min = (int) ((data - angle) * 60);
        double sec = ((int) (10000 * ((data - angle) * 3600 - min * 60))) / 10000.0;
        return angle + "°" + (9 < min ? min : "0" + min) + "'" + (9 < sec ? sec : "0" + sec) + "\"";
    }

    public void startMeasure() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                declination = getDeclination(location.getLatitude(), location.getLongitude(), location.getAltitude());
                if (angleMinSecFormat) {
                    binding.latitudeText.setText(R.string.latitude);
                    binding.longitudeText.setText(R.string.longitude);
                    binding.altitudeText.setText(R.string.altitude);
                    binding.latitudeData.setText(convertAngleMinSecFormat(location.getLatitude()));
                    binding.longitudeData.setText(convertAngleMinSecFormat(location.getLongitude()));
                    String altitude = location.getAltitude() + "m";
                    binding.altitudeData.setText(altitude);
                } else if (decimalFormat) {
                    binding.latitudeText.setText(R.string.latitude);
                    binding.longitudeText.setText(R.string.longitude);
                    binding.altitudeText.setText(R.string.altitude);
                    String latitude = String.format(Locale.getDefault(), "%.6f°", location.getLatitude());
                    String longitude = String.format(Locale.getDefault(), "%.6f°", location.getLongitude());
                    binding.latitudeData.setText(latitude);
                    binding.longitudeData.setText(longitude);
                    String altitude = location.getAltitude() + "m";
                    binding.altitudeData.setText(altitude);
                } else if (xyzFormat) {
                    binding.latitudeText.setText(R.string.X);
                    binding.longitudeText.setText(R.string.Y);
                    binding.altitudeText.setText(R.string.Z);
                    binding.latitudeData.setText(WGS84.getX(location.getLatitude(),
                            location.getLongitude(),
                            location.getAltitude()));
                    binding.longitudeData.setText(WGS84.getY(location.getLatitude(),
                            location.getLongitude(),
                            location.getAltitude()));
                    binding.altitudeData.setText(WGS84.getZ(location.getLatitude(),
                            location.getAltitude()));
                }
                EOV eovPosition = new EOV();
                eovPosition.toEOV(location.getLatitude(), location.getLongitude(), location.getAltitude());
                binding.eovText.setText(R.string.eov);
                binding.eovData.setText(eovPosition.toString());
                STANDING_POINT = new MeasPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
                STANDING_POINT.calcEastNorthUpData();
                GPS_ACCURACY = location.getAccuracy();
                measurePoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                LocationListener.super.onProviderDisabled(provider);
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        sensorManager.registerListener(this, accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                500, 0, locationListener);
    }

    private float getDeclination(double latitude, double longitude, double altitude){
        long time = System.currentTimeMillis();
        GeomagneticField geomagneticField =
                new GeomagneticField((float) latitude, (float) longitude, (float) altitude, time);
        return geomagneticField.getDeclination();
    }

    private void measurePoint(double fi, double lambda, double h) {
        if ( !MeasFragment.IS_RUN_MEAS_PROCESS ) {
            return;
        }
        MEAS_POINT.addMeasData(fi, lambda, h);
        TextView measDataView = measuredDataContainer.findViewById(R.id.measured_position);
        measDataView.setText(MEAS_POINT.toString());
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    private void inputPointDataDialog(){
        if( MEAS_POINT_LIST.isEmpty() ){
            openInputPointDataFile();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pontok beolvasása");
        builder.setMessage("Törli a korábbi pontokat?");

        builder.setPositiveButton("Igen", (dialog, which) -> {
           MEAS_POINT_LIST.clear();
           TopoCentricPoint.initTopoCenter(0d, 0d, 0d, true);
           openInputPointDataFile();
        });
        builder.setNegativeButton("Nem", (dialog, which) -> {
            openInputPointDataFile();
            dialog.dismiss();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startMeasureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS bekapcsolása");
        builder.setMessage("Bekapcsolja a GPS-t?");

        builder.setPositiveButton("Igen", (dialog, which) -> {
            startMeasure();
            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main)
                    .navigate(R.id.action_StartFragment_to_MeasFragment);
        });

        builder.setNegativeButton("Nem", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Alkalmazás bezárása");
        builder.setMessage("Biztos, hogy ki akar lépni az alkalmazásból?\n\nA nem mentett adatok elvesznek.");

        builder.setPositiveButton("Igen", (dialog, which) -> {
            dialog.dismiss();
            System.exit(0);
        });

        builder.setNegativeButton("Nem", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticValues = event.values.clone();
        }
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravityValues, geomagneticValues);

            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                float azimuth = (float) Math.toDegrees(orientation[0]) + declination;
                rotateCompass( - azimuth );
                AZIMUTH = azimuth;
            }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void openInputPointDataFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        activityResultLauncher.launch(intent);
    }

    private void readInputPointData(Uri uri) {
        int pcs = 0;
        int row = 0;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                row++;
            if( !parseInputData(line) ){
                continue;
            }
            pcs++;
            }
            br.close();
            Toast.makeText(this, pcs +  "db / " + row + "db pont beolvasva.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "A fájl nem olvasható.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
        private boolean parseInputData(String dataLine){
        String[] inputData = dataLine.split(";");
        switch ( inputData.length ){
            case 2:
               return setInputData(null, inputData[0], inputData[1], null);
            case 3:
               return setInputData(inputData[0], inputData[1], inputData[2], null);
            case 4:
               return  setInputData(inputData[0], inputData[1], inputData[2], inputData[3]);

        }
        return false;
   }
        private boolean setInputData(String id, String data1, String data2, String h){
        String[] data1Value = data1.replace(",", ".").split("\\.");
        String[] data2Value = data2.replace(",", ".").split("\\.");

        if( (data1Value[0].length() > 0 && data1Value[0].length() < 4) &&
                (data2Value[0].length() > 0 && data2Value[0].length() < 4) ){
            try {
                    double input1stData = Double.parseDouble(data1.replace(",", "."));
                    double input2ndData = Double.parseDouble(data2.replace(",", "."));
        MeasPoint inputPoint = new MeasPoint(id == null ? (NEXT_POINT_NUMBER++) + "_kit" : id + "_kit");
              if( input1stData >= -90 && input1stData <= 90 && input2ndData >= -180 && input2ndData <= 180 ){
                  inputPoint.setFi_WGS(input1stData);
                  inputPoint.setLambda_WGS(input2ndData);
              }
               else if( input2ndData >= -90 && input2ndData <= 90 && input1stData >= -180 && input1stData <= 180 ){
                  inputPoint.setFi_WGS(input2ndData);
                  inputPoint.setLambda_WGS(input1stData);
              }
                double elevation;
                try {
                    if( h != null ) {
                        elevation = Double.parseDouble(h);
                        inputPoint.setH_WGS(elevation);
                    }
                }
                catch (NumberFormatException e){
                    e.printStackTrace();
                }
                    EOV eov = new EOV();
                    eov.toEOV(inputPoint.getFi_WGS(), inputPoint.getLambda_WGS(), inputPoint.getH_WGS());
                    inputPoint.setY_EOV(eov.getY_EOV());
                    inputPoint.setX_EOV(eov.getX_EOV());
                    inputPoint.setZ_EOV(eov.getZ_EOV());
                    if( TopoCentricPoint.FI_0 == 0d && TopoCentricPoint.LAMBDA_0 == 0d &&
                        TopoCentricPoint.H_0 == 0d ){
                        inputPoint.setTopoCenter(true);
                    }
                    inputPoint.calcEastNorthUpData();
                    MEAS_POINT_LIST.add(inputPoint);
                    return true;
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }

        }
        else if((data1Value[0].length() == 5 || data1Value[0].length() == 6) &&
                (data2Value[0].length() == 5 || data2Value[0].length() == 6) ){
            try {
                double input1stData = Double.parseDouble(data1.replace(",", "."));
                double input2ndData = Double.parseDouble(data2.replace(",", "."));
                MeasPoint inputPoint = new MeasPoint(id == null ? (NEXT_POINT_NUMBER++) + "_kit" : id + "_kit");
                if( input1stData > 400000 && input1stData < 960000 && input2ndData > 32000 && input2ndData <  384000){
                   inputPoint.setY_EOV(input1stData);
                   inputPoint.setX_EOV(input2ndData);
                }
                else if( input2ndData > 400000 && input2ndData < 960000 && input1stData > 32000 && input1stData < 384000){
                    inputPoint.setY_EOV(input2ndData);
                    inputPoint.setX_EOV(input1stData);
                }
                double elevation;
                try {
                    if( h != null ) {
                        elevation = Double.parseDouble(h);
                        inputPoint.setZ_EOV(elevation);
                    }
                }
                catch (NumberFormatException e){
                    e.printStackTrace();
                }
                  WGS84 wgs = new WGS84();
                  wgs.toWGS84(inputPoint.getY_EOV(), inputPoint.getX_EOV(), inputPoint.getZ_EOV());
                  inputPoint.setFi_WGS(wgs.getFi_WGS());
                  inputPoint.setLambda_WGS(wgs.getLambda_WGS());
                  inputPoint.setH_WGS(wgs.getH_WGS());
                    if( TopoCentricPoint.FI_0 == 0d && TopoCentricPoint.LAMBDA_0 == 0d &&
                            TopoCentricPoint.H_0 == 0d ){
                        inputPoint.setTopoCenter(true);
                    }
                  inputPoint.calcEastNorthUpData();
                  MEAS_POINT_LIST.add(inputPoint);
                  return true;
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return false;
}

    }

