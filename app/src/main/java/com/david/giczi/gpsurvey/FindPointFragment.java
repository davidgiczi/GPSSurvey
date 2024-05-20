package com.david.giczi.gpsurvey;

import android.graphics.Color;
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
import com.david.giczi.gpsurvey.utils.EOV;
import com.david.giczi.gpsurvey.utils.WGS84;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class FindPointFragment extends Fragment {


    private FragmentFindPointBinding binding;
    private MeasPoint findPoint;
    private int findPointDistance;
    private Handler handler;
    private Runnable findPointProcess;
    private boolean isRunningFindPointProcess;
    private static final String CHOOSE_POINT = "Válassz pontot";

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
        if( handler != null ){
            handler.removeCallbacks(findPointProcess);
        }
        initPointSpinner();
        binding.findPoint1stCoordinate.setEnabled(true);
        binding.findPoint2ndCoordinate.setEnabled(true);
        binding.findPoint1stCoordinate.setText("");
        binding.findPoint2ndCoordinate.setText("");
        binding.pointSpinner.setEnabled(true);
        binding.lookForPointButton.setText(R.string.point_catching_option);
        binding.findPointDirectionArrow.setImageResource(android.R.color.transparent);
        binding.directionText.setText(R.string.find_point_direction);
        binding.distanceText.setText(R.string.distance);
        isRunningFindPointProcess = false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.lookForPointButton.setOnClickListener(p -> {
           if( isRunningFindPointProcess ){
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
           setFindPoint(binding.pointSpinner.getSelectedItem().toString());
           isRunningFindPointProcess = true;
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
        else if( input1stData[0].length() >= 6 &&  Double.parseDouble(input1stData[0]) < 400000 ){
            Toast.makeText(requireContext(), "Nem megfelelő az első koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( input2ndData[0].length() >= 6 &&  Double.parseDouble(input2ndData[0]) > 400000 ){
            Toast.makeText(requireContext(), "Nem megfelelő a második koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( input1stData[0].length() == 2 &&  (Double.parseDouble(input1stData[0]) < 45 ||
                Double.parseDouble(input1stData[0]) > 48) ){
            Toast.makeText(requireContext(), "Nem megfelelő az első koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( input2ndData[0].length() == 2 &&  (Double.parseDouble(input2ndData[0]) < 16  ||
                Double.parseDouble(input2ndData[0]) > 22)){
            Toast.makeText(requireContext(), "Nem megfelelő a második koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void initPointSpinner(){
        List<String> ITEMS = new ArrayList<>();
        ITEMS.add(0, CHOOSE_POINT);
        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            ITEMS.add(measPoint.getPointIDAsString());
        }
        binding.pointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if( !parent.getItemAtPosition(position).equals(CHOOSE_POINT) ){
                    String chosenPointId = (String) parent.getItemAtPosition(position);
                    String Y = String.valueOf(getChosenPoint(chosenPointId).getY_EOV());
                    String X = String.valueOf(getChosenPoint(chosenPointId).getX_EOV());
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

    private void setFindPoint(String findPointId){
        if(  findPointId.equals(CHOOSE_POINT) ) {
            String[] input1stData = binding.findPoint1stCoordinate.getText().toString().split("\\.");
            String[] input2ndData = binding.findPoint2ndCoordinate.getText().toString().split("\\.");
            MainActivity.NEXT_POINT_NUMBER++;
            findPoint = new MeasPoint(MainActivity.NEXT_POINT_NUMBER);
            if( input1stData[0].length() == 2 && input2ndData[0].length() == 2  ){
                double fi_WGS = Double.parseDouble(binding.findPoint1stCoordinate.getText().toString());
                double lambda_WGS = Double.parseDouble(binding.findPoint2ndCoordinate.getText().toString());
                EOV eov = new EOV();
                eov.toEOV(fi_WGS, lambda_WGS, 0d);
                findPoint.setY_EOV(eov.getY_EOV());
                findPoint.setX_EOV(eov.getX_EOV());
                findPoint.setFi_WGS(fi_WGS);
                findPoint.setLambda_WGS(lambda_WGS);
            }
            else if(input1stData[0].length() > 2 && input2ndData[0].length() > 2){
                double y_eov = Double.parseDouble(binding.findPoint1stCoordinate.getText().toString());
                double x_eov = Double.parseDouble(binding.findPoint2ndCoordinate.getText().toString());
                WGS84 wgs = new WGS84();
                wgs.toWGS84(y_eov, x_eov, 0d);
                findPoint.setFi_WGS(wgs.getFi_WGS());
                findPoint.setLambda_WGS(wgs.getLambda_WGS());
                findPoint.setH_WGS(wgs.getH_WGS());
                findPoint.setY_EOV(y_eov);
                findPoint.setX_EOV(x_eov);
            }
            MainActivity.MEAS_POINT_LIST.add(findPoint);
        }
        else {
            findPoint = getChosenPoint(findPointId);
        }
    }

    private MeasPoint getChosenPoint(String findPointId){
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
        findPointProcess = () -> {
            handler.postDelayed(findPointProcess, 1000);
            if( MainActivity.ACTUAL_POSITION == null ){
                return;
            }
            MeasPoint actualPosition = new MeasPoint();
            actualPosition.setY_EOV(MainActivity.ACTUAL_POSITION.getY_EOV());
            actualPosition.setX_EOV(MainActivity.ACTUAL_POSITION.getX_EOV());
            AzimuthAndDistance findPointData = new AzimuthAndDistance(actualPosition, findPoint);
            double direction = 0 > Math.toDegrees(findPointData.calcAzimuth()) - MainActivity.AZIMUTH ?
                    Math.toDegrees(findPointData.calcAzimuth()) - MainActivity.AZIMUTH + 360 :
                    Math.toDegrees(findPointData.calcAzimuth()) - MainActivity.AZIMUTH;
            addFindPointDirectionArrowImage((float) direction, (int) Math.round(findPointData.calcDistance()));
            String findPointDirection = getString(R.string.find_point_direction) + " "
                    + String.format(Locale.getDefault(),"%.1f°", direction);
            String findPointDistance = getString(R.string.distance) + " " +
                    String.format(Locale.getDefault(),"%.0fm", findPointData.calcDistance());
            binding.directionText.setText(findPointDirection);
            binding.distanceText.setText(findPointDistance);
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
        init();
        super.onDestroyView();
    }
}
