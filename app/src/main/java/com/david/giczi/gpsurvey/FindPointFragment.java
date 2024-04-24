package com.david.giczi.gpsurvey;

import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.david.giczi.gpsurvey.databinding.FragmentFindPointBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.AzimuthAndDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class FindPointFragment extends Fragment {


    private FragmentFindPointBinding binding;
    private String chosenPointId;
    private int findPointDistance;
    private Handler handler;
    private Runnable findPointProcess;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFindPointBinding.inflate(inflater, container, false);
        binding.lookForPointButton.setBackgroundColor(Color.DKGRAY);
        MainActivity.PAGE_NUMBER_VALUE = 3;
        initPointSpinner();
        return binding.getRoot();
    }

    private void init(){

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.lookForPointButton.setOnClickListener(p -> {
           if( findPointProcess != null ){
                init();
                return;
            }
           else if( !validateInputPointData() ){
                return;
            }
           else if( !((MainActivity) requireActivity())
                    .locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    ((MainActivity) requireActivity()).locationListener == null){
                ((MainActivity) requireActivity()).startMeasure();
            }
           binding.lookForPointButton.setText(R.string.stop);
           binding.findPoint1stCoordinate.setEnabled(false);
           binding.findPoint2ndCoordinate.setEnabled(false);
           binding.pointSpinner.setEnabled(false);
           calcFindPointDirectionAndDistance();
        });
    }

    private boolean validateInputPointData(){
        if( binding.findPoint1stCoordinate.getText().toString().isEmpty() ){
            Toast.makeText(requireContext(), "Nincs megadva az első koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( binding.findPoint2ndCoordinate.getText().toString().isEmpty() ){
            Toast.makeText(requireContext(), "Nincs megadva a második koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        String[] input1stData = binding.findPoint1stCoordinate.getText().toString().split("\\.");
        String[] input2ndData = binding.findPoint2ndCoordinate.getText().toString().split("\\.");
        if( 2 > input1stData[0].length() || (2 < input1stData[0].length() && input1stData[0].length() < 6) ){
            Toast.makeText(requireContext(), "Nem megfelelő az első koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( 2 > input2ndData[0].length() || (2 < input2ndData[0].length() && input2ndData[0].length() < 6) ){
            Toast.makeText(requireContext(), "Nem megfelelő a második koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void initPointSpinner(){
        List<String> ITEMS = new ArrayList<>();
        ITEMS.add(0, "Válassz pontot");
        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            ITEMS.add(measPoint.getPointIDAsString());
        }
        binding.pointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if( !parent.getItemAtPosition(position).equals("Válassz pontot") ){
                    chosenPointId = (String) parent.getItemAtPosition(position);
                    String Y = String.valueOf(getFindPoint(chosenPointId).getY());
                    String X = String.valueOf(getFindPoint(chosenPointId).getX());
                    binding.findPoint1stCoordinate.setText(Y);
                    binding.findPoint2ndCoordinate.setText(X);
                }
                else{
                    binding.findPoint1stCoordinate.setText("");
                    binding.findPoint2ndCoordinate.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.point_spinner, ITEMS);
        binding.pointSpinner.setAdapter(arrayAdapter);
    }

    private MeasPoint getFindPoint(String findPointId){
        MeasPoint chosenPoint = null;
        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            if( measPoint.getPointID() == Integer.parseInt(findPointId) ){
               chosenPoint = measPoint;
            }
        }
        return chosenPoint;
    }

    private void calcFindPointDirectionAndDistance() {
        handler = new Handler();
        findPointProcess = new Runnable() {
            @Override
            public void run() {
                MeasPoint actualPosition = new MeasPoint();
                actualPosition.setY(MainActivity.ACTUAL_POSITION.getCoordinatesForEOV().get(0));
                actualPosition.setX(MainActivity.ACTUAL_POSITION.getCoordinatesForEOV().get(1));
                MeasPoint chosenPointPosition= new MeasPoint();
                chosenPointPosition.setY(getFindPoint(chosenPointId).getY());
                chosenPointPosition.setX(getFindPoint(chosenPointId).getX());
                AzimuthAndDistance findPointData = new AzimuthAndDistance(actualPosition, chosenPointPosition);
                double direction = 0 > Math.toDegrees(findPointData.calcAzimuth()) - MainActivity.AZIMUTH ?
                        Math.toDegrees(findPointData.calcAzimuth()) - MainActivity.AZIMUTH + 360 :
                        Math.toDegrees(findPointData.calcAzimuth()) - MainActivity.AZIMUTH;
                addFindPointDirectionArrowImage((float) direction, (int) Math.round(findPointData.calcDistance()));
                String findPointDirection = "Irány: " + String.format(Locale.getDefault(),"%5.1f°", direction);
                String findPointDistance = "Távolság: " +
                        String.format(Locale.getDefault(),"%5.0fm", findPointData.calcDistance());
                binding.directionText.setText(findPointDirection);
                binding.distanceText.setText(findPointDistance);
                handler.postDelayed(findPointProcess, 1000);
            }
        };
        handler.postDelayed(findPointProcess, 1000);
    }
    private void addFindPointDirectionArrowImage(float rotation, int distance){

        if( distance > findPointDistance  ){
            binding.findPointDirectionArrow.setImageResource(R.drawable.red_arrow_up);
        }
        else{
            binding.findPointDirectionArrow.setImageResource(R.drawable.green_arrow_up);
        }
        binding.findPointDirectionArrow.setRotation(rotation);
        findPointDistance = distance;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
