package com.david.giczi.gpsurvey;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.david.giczi.gpsurvey.databinding.ActivityMainBinding;
import com.david.giczi.gpsurvey.utils.WGS84;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION = 1;
    public static boolean GO_MEAS_FRAGMENT;
    private boolean decimalFormat = true;
    private boolean angleMinSecFormat;
    private boolean xyzFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if( locationListener == null ){
            startMeasureDialog();
        }
        else if( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationListener != null  ){
            Toast.makeText(this, "A mérés elindítva.", Toast.LENGTH_SHORT).show();
        }
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
        else if( id == R.id.start_measure_option ){

         if( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationListener != null){
                Toast.makeText(this, "A mérés elindítva.", Toast.LENGTH_SHORT).show();
            }
         else {
             startMeasure();
         }

            if(  GO_MEAS_FRAGMENT ) {
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_StartFragment_to_MeasFragment);
            }
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    private String setAngleMinSecFormat(double data){

        int angle = (int) data;
        int min = (int) ((data - angle) * 60);
        double sec = ((data - angle) * 3600 - min * 60);
        return angle + "° " + min + "' " + sec + "\"";
    }

    private void startMeasure(){

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if( angleMinSecFormat ){
                    binding.latitudeText.setText(R.string.latitude);
                    binding.longitudeText.setText(R.string.longitude);
                    binding.altitudeText.setText(R.string.altitude);
                    binding.latitudeData.setText(setAngleMinSecFormat(location.getLatitude()));
                    binding.longitudeData.setText(setAngleMinSecFormat(location.getLongitude()));
                    String altitude = String.valueOf(location.getAltitude()) + "m";
                    binding.altitudeData.setText(altitude);
                }
                else if( decimalFormat ){
                    binding.latitudeText.setText(R.string.latitude);
                    binding.longitudeText.setText(R.string.longitude);
                    binding.altitudeText.setText(R.string.altitude);
                    String latitude = String.valueOf(location.getLatitude()) + "°";
                    String longitude = String.valueOf(location.getLongitude()) + "°";
                    binding.latitudeData.setText(latitude);
                    binding.longitudeData.setText(longitude);
                    String altitude = String.valueOf(location.getAltitude()) + "m";
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
                            location.getLongitude(),
                            location.getAltitude()));
                }
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 0, locationListener);
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    private void startMeasureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mérés indítása");
        builder.setMessage("Indítja a mérést?");

        builder.setPositiveButton("Igen", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                startMeasure();
                Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_StartFragment_to_MeasFragment);
            }
        });

        builder.setNegativeButton("Nem", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Alkalmazás bezárása");
        builder.setMessage("Biztos, hogy ki akarsz lépni az alkalmazásból?\n\nA nem mentett adatok elvesznek.");

        builder.setPositiveButton("Igen", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });

        builder.setNegativeButton("Nem", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}