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
import com.david.giczi.gpsurvey.utils.WGS84;

import java.text.DecimalFormat;
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
    private boolean isShowingData = true;

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
        String[] input1stData = binding.findPoint1stCoordinate.getText()
                .toString().replace(",", ".").split("\\.");
        String[] input2ndData = binding.findPoint2ndCoordinate.getText()
                .toString().replace(",", ".").split("\\.");
        if( 1 > input1stData[0].length() || 4 == input1stData[0].length() ||
                5 == input1stData[0].length() || input1stData[0].length() > 6 ){
            Toast.makeText(requireContext(), "Nem megfelelő az első koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( 1 > input2ndData[0].length() || input2ndData[0].length() > 6 ){
            Toast.makeText(requireContext(), "Nem megfelelő a második koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( input1stData[0].length() == 6 && (Double.parseDouble(input1stData[0]) < 400000 ||
                Double.parseDouble(input1stData[0]) > 960000 )){
            Toast.makeText(requireContext(), "Nem megfelelő az első koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( (input2ndData[0].length() == 5 || input2ndData[0].length() == 6) &&  (Double.parseDouble(input2ndData[0]) > 384000 ||
                Double.parseDouble(input2ndData[0]) < 32000) ){
            Toast.makeText(requireContext(), "Nem megfelelő a második koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( input1stData[0].length() > 0  &&  input1stData[0].length() < 4 &&  (Double.parseDouble(input1stData[0]) < -90 ||
                Double.parseDouble(input1stData[0]) > 90) ){
            Toast.makeText(requireContext(), "Nem megfelelő az első koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if( input2ndData[0].length() > 0 && input2ndData[0].length() < 5 && (Double.parseDouble(input2ndData[0]) < -180  ||
                Double.parseDouble(input2ndData[0]) > 180)){
            Toast.makeText(requireContext(), "Nem megfelelő a második koordináta érték.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void initPointSpinner(){
        List<String> ITEMS = new ArrayList<>();
        ITEMS.add(0, CHOOSE_POINT);
        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            ITEMS.add(measPoint.getPointID());
        }
        binding.pointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if( !parent.getItemAtPosition(position).equals(CHOOSE_POINT) ){
                    String chosenPointId = (String) parent.getItemAtPosition(position);
                    MeasPoint chosenPoint = getChosenPoint(chosenPointId);
                    String firstData = String.valueOf((chosenPoint.getY_EOV() == 0 ?
                            chosenPoint.getFi_WGS() : chosenPoint.getY_EOV()));
                    String secondData = String.valueOf((chosenPoint.getX_EOV() == 0 ?
                            chosenPoint.getLambda_WGS() : chosenPoint.getX_EOV()));
                    binding.findPoint1stCoordinate.setText(firstData);
                    binding.findPoint2ndCoordinate.setText(secondData);
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
            String[] input1stData = binding.findPoint1stCoordinate.getText().toString()
                    .replace(",", ".").split("\\.");
            String[] input2ndData = binding.findPoint2ndCoordinate.getText().toString()
                    .replace(",", ".").split("\\.");
            MainActivity.NEXT_POINT_NUMBER++;
            findPoint = new MeasPoint(MainActivity.NEXT_POINT_NUMBER + "_kit");
            if( input1stData[0].length() > 0 && input1stData[0].length() < 4 &&
                    input2ndData[0].length() > 0 && input2ndData[0].length() < 4){
                double fi_WGS = Double.parseDouble(binding.findPoint1stCoordinate.getText().toString()
                        .replace(",", "."));
                double lambda_WGS = Double.parseDouble(binding.findPoint2ndCoordinate.getText().toString()
                        .replace(",", "."));
                findPoint.setFi_WGS(fi_WGS);
                findPoint.setLambda_WGS(lambda_WGS);
            }
            else if(input1stData[0].length() == 6 && input2ndData[0].length() > 4 && input2ndData[0].length() < 7){
                double y_eov = Double.parseDouble(binding.findPoint1stCoordinate.getText().toString()
                        .replace(",", "."));
                double x_eov = Double.parseDouble(binding.findPoint2ndCoordinate.getText().toString()
                        .replace(",", "."));
                WGS84 wgs = new WGS84();
                wgs.toWGS84(y_eov, x_eov, 0d);
                findPoint.setFi_WGS(wgs.getFi_WGS());
                findPoint.setLambda_WGS(wgs.getLambda_WGS());
                findPoint.setH_WGS(wgs.getH_WGS());
                findPoint.setY_EOV(y_eov);
                findPoint.setX_EOV(x_eov);
            }
            if( MainActivity.MEAS_POINT_LIST.isEmpty() ) {
               findPoint.setTopoCenter(true);
            }
            else{
               findPoint.calcEastNorthUpData();
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
            if( measPoint.getPointID().equals(findPointId) ){
               chosenPoint = measPoint;
            }
        }
        return chosenPoint;
    }

    private void getFindPointDistancesBetweenTopoCenterAndFindPoints(){

        if( binding.betweenFindAndTopoPointLineCheckbox.isChecked() && isShowingData ){

            MeasPoint topoCenter = getTopoCentricPoint();

            if( topoCenter == null ){
                binding.betweenFindAndTopoPointLineCheckbox.setChecked(false);
                Toast.makeText(requireContext(), "Topocentrum pont választása szükséges.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            else if( topoCenter.getPointID().equals(findPoint.getPointID()) ){
                binding.betweenFindAndTopoPointLineCheckbox.setChecked(false);
                Toast.makeText(requireContext(), "A felkeresendő és a topocentrum pont nem lehet megegyező.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            else if( topoCenter.getEAST() == findPoint.getEAST() && topoCenter.getNORTH() == findPoint.getNORTH() ){
                binding.betweenFindAndTopoPointLineCheckbox.setChecked(false);
                Toast.makeText(requireContext(), "A felkeresendő és a topocentrum pont koordinátái egyenlőek.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            AzimuthAndDistance lineData = new AzimuthAndDistance(findPoint, topoCenter);
            AzimuthAndDistance actualPosition = new AzimuthAndDistance(findPoint, MainActivity.STANDING_POINT);
            double alfa = lineData.calcAzimuth() - actualPosition.calcAzimuth();
            DecimalFormat format = new DecimalFormat("0.00");
            String abscissa = format.format(Math.cos(alfa) * actualPosition.calcDistance()).replace(",", ".");
            String ordinate = format.format(  Math.sin(alfa) * actualPosition.calcDistance() ).replace(",", ".");
            Toast.makeText(getContext(), "Vonalig: " + ordinate + "m Vonalban: " + abscissa + "m", Toast.LENGTH_SHORT).show();
        }

    }

    private MeasPoint getTopoCentricPoint(){

        MeasPoint topoCentric = null;

        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            if( measPoint.isTopoCenter() ){
                topoCentric = measPoint;
            }
        }

        return topoCentric;
    }

    private void calcFindPointDirectionAndDistance() {
        handler = new Handler();
        findPointProcess = () -> {
            handler.postDelayed(findPointProcess, 1000);
            if( MainActivity.STANDING_POINT == null || findPoint == null){
                return;
            }
            getFindPointDistancesBetweenTopoCenterAndFindPoints();
            isShowingData = !isShowingData;
            AzimuthAndDistance findPointAzimuth = new AzimuthAndDistance(MainActivity.STANDING_POINT, findPoint);
            double direction = Math.toDegrees(findPointAzimuth.calcAzimuth()) - MainActivity.AZIMUTH;
            direction = 0 > direction ?  direction + 360 : direction >= 360 ? direction - 360 : direction;
            addFindPointDirectionArrowImage((float) direction, (int) Math.round(findPointAzimuth.calcDistance()));
            String findPointDirection = getString(R.string.find_point_direction) + " "
                    + String.format(Locale.getDefault(),"%.1f°", direction);
            String findPointDistance = getString(R.string.distance) + " " +
                    String.format(Locale.getDefault(),"%.0fm",
                            new AzimuthAndDistance(MainActivity.STANDING_POINT, findPoint).calcDistance());
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
