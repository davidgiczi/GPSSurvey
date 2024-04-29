package com.david.giczi.gpsurvey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.david.giczi.gpsurvey.databinding.ActivityMainBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.AzimuthAndDistance;
import com.david.giczi.gpsurvey.utils.EOV;
import com.david.giczi.gpsurvey.utils.WGS84;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public LocationManager locationManager;
    public LocationListener locationListener;
    private SensorManager sensorManager;
    private Sensor sensor;
    private  ViewGroup compassContainer;
    public ViewGroup measuredDataContainer;
    public static PopupWindow measuredDataWindow;
    private static final int REQUEST_LOCATION = 1;
    public static List<MeasPoint> MEAS_POINT_LIST;
    public static MeasPoint MEAS_POINT;
    private MeasPoint prePositionForVelocity;
    public static int NEXT_POINT_NUMBER;
    public static int PAGE_NUMBER_VALUE;
    public static double AZIMUTH;
    public static EOV ACTUAL_POSITION;
    private boolean decimalFormat = true;
    private boolean angleMinSecFormat;
    private boolean xyzFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if( locationListener == null ){
            startMeasureDialog();
        }
        else if( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationListener != null  ){
            Toast.makeText(this, "GPS elindítva", Toast.LENGTH_SHORT).show();
        }
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        MEAS_POINT_LIST = new ArrayList<>();
    }

    private void popupCompassWindow(){
        compassContainer =  (ViewGroup) getLayoutInflater().inflate(R.layout.fragment_compass, null);
        PopupWindow compassWindow = new PopupWindow(compassContainer, 600,600, true);
        compassWindow.showAtLocation( binding.getRoot(), Gravity.CENTER, 0, 0);
        ImageView compassView = compassContainer.findViewById(R.id.compass);
        compassView.setImageResource(R.drawable.compass);
    }

    private void rotateCompass(float rotateAngle){
        if( compassContainer == null ){
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
        }
        else if( id == R.id.point_measure_option ){

         if( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationListener != null){
                Toast.makeText(this, "GPS elindítva", Toast.LENGTH_SHORT).show();
            }
         else {
             startMeasure();
         }
          navigateToMeasFragment();
        }
        else if( id == R.id.decimal_format ){
            decimalFormat = true;
            angleMinSecFormat = false;
            xyzFormat = false;
        }
        else if( id == R.id.xyz_format ){
            xyzFormat = true;
            decimalFormat = false;
            angleMinSecFormat = false;
        }
        else if( id == R.id.angle_min_sec_format ){
            angleMinSecFormat = true;
            decimalFormat = false;
            xyzFormat = false;
        }
        else if( id == R.id.compass_option ){
            popupCompassWindow();
        }
        else if( id == R.id.calc_option ){
            navigateToCalcFragment();
        }
        else if( id == R.id.finding_point_option ){
            navigateToFindPointFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToFindPointFragment(){
        switch (PAGE_NUMBER_VALUE){
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

    private void navigateToMeasFragment(){
        switch (PAGE_NUMBER_VALUE){
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

    private void navigateToCalcFragment(){
        switch (PAGE_NUMBER_VALUE){
            case 0:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_StartFragment_to_CalcFragment);
                break;
            case 1 :
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
    public static String convertAngleMinSecFormat(double data){

        int angle = (int) data;
        int min = (int) ((data - angle) * 60);
        double sec = ((int) (10000 * ((data - angle) * 3600 - min * 60))) / 10000.0;
        return angle + "°" + (9 < min ? min : "0" + min) + "'" + (9 < sec ? sec : "0" + sec) + "\"";
    }

    public void startMeasure(){

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if( angleMinSecFormat ){
                    binding.latitudeText.setText(R.string.latitude);
                    binding.longitudeText.setText(R.string.longitude);
                    binding.altitudeText.setText(R.string.altitude);
                    binding.latitudeData.setText(convertAngleMinSecFormat(location.getLatitude()));
                    binding.longitudeData.setText(convertAngleMinSecFormat(location.getLongitude()));
                    String altitude = location.getAltitude() + "m";
                    binding.altitudeData.setText(altitude);
                }
                else if( decimalFormat ){
                    binding.latitudeText.setText(R.string.latitude);
                    binding.longitudeText.setText(R.string.longitude);
                    binding.altitudeText.setText(R.string.altitude);
                    String latitude = String.format(Locale.getDefault(),"%.6f°", location.getLatitude());
                    String longitude = String.format(Locale.getDefault(), "%.6f°", location.getLongitude());
                    binding.latitudeData.setText(latitude);
                    binding.longitudeData.setText(longitude);
                    String altitude = location.getAltitude() + "m";
                    binding.altitudeData.setText(altitude);
                }
                else if( xyzFormat ){
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
                double X_WGS = Double.parseDouble(WGS84.getX(location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude()).substring(0, WGS84.getX(location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude()).indexOf("m")));
                double Y_WGS = Double.parseDouble(WGS84.getY(location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude()).substring(0, WGS84.getY(location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude()).indexOf("m")));
                double Z_WGS = Double.parseDouble(WGS84.getZ(location.getLatitude(),
                        location.getAltitude()).substring(0, WGS84.getZ(location.getLatitude(),
                        location.getAltitude()).indexOf("m")));
                ACTUAL_POSITION = new EOV(X_WGS, Y_WGS, Z_WGS);
                ACTUAL_POSITION.setFi_WGS(location.getLatitude());
                ACTUAL_POSITION.setLambda_WGS(location.getLongitude());
                ACTUAL_POSITION.setH_WGS(location.getAltitude());
                binding.eovText.setText(R.string.eov);
                binding.eovData.setText(ACTUAL_POSITION.toString());
                measurePoint(ACTUAL_POSITION);
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
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                500, 0, locationListener);
        MEAS_POINT_LIST = new ArrayList<>();
    }

    private void measurePoint(EOV eov){
      if( !MeasFragment.IS_RUN_MEAS_PROCESS){
          return;
      }
       MEAS_POINT.setMeasData(eov);
       TextView measDataView = measuredDataContainer.findViewById(R.id.measured_position);
       measDataView.setText(MEAS_POINT.toString());
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
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
        rotateCompass( - event.values[0] );
        AZIMUTH = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}