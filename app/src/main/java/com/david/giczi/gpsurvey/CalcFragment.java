package com.david.giczi.gpsurvey;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.david.giczi.gpsurvey.databinding.FragmentCalcBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.CalcData;

import java.util.ArrayList;
import java.util.List;


public class CalcFragment extends Fragment {

    private FragmentCalcBinding binding;
    private List<LinearLayout> displayedMeasuredPointLinearLayoutStore;
    private List<MeasPoint> chosenMeasPointStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentCalcBinding.inflate(inflater, container, false);
        this.chosenMeasPointStore = new ArrayList<>();
        MainActivity.PAGE_NUMBER_VALUE = 2;
        addOnClickListenerForCheckBox();
        initSpinner();
        displayCalculatedData(MainActivity.MEAS_POINT_LIST);
        displayMeasuredPoint(MainActivity.MEAS_POINT_LIST);
        return binding.getRoot();
    }

    private void displayMeasuredPoint(List<MeasPoint> chosenMeasPointStore){
        this.displayedMeasuredPointLinearLayoutStore = new ArrayList<>();
        for (MeasPoint measPoint : chosenMeasPointStore) {
            LinearLayout measPointIDLayout = new LinearLayout(getContext());
            measPointIDLayout.setOrientation(LinearLayout.HORIZONTAL);
            measPointIDLayout.setGravity(Gravity.CENTER);
            TextView measPointId = new TextView(getContext());
            String pointID = measPoint.getPointID() + ". pont\t\t±Qyx=" + measPoint.getQ() + "m";
            measPointId.setText(pointID);
            measPointId.setTextSize(18f);
            measPointId.setTypeface(Typeface.DEFAULT_BOLD);
            measPointIDLayout.addView(measPointId);
            displayedMeasuredPointLinearLayoutStore.add(measPointIDLayout);
            binding.calcLinearlayout.addView(measPointIDLayout);
            TextView measPointData = new TextView(getContext());
            measPointData.setTextIsSelectable(true);
            measPointData.setText(measPoint.getMeasPontData());
            measPointData.setTextSize(16f);
            LinearLayout measPointDataLayout = new LinearLayout(getContext());
            measPointDataLayout.setGravity(Gravity.CENTER);
            measPointDataLayout.addView(measPointData);
            displayedMeasuredPointLinearLayoutStore.add(measPointDataLayout);
            binding.calcLinearlayout.addView(measPointDataLayout);
        }
    }

    private void clearDisplayedPointData(){
        if( displayedMeasuredPointLinearLayoutStore == null ){
            return;
        }
        for (LinearLayout linearLayout : displayedMeasuredPointLinearLayoutStore) {
            binding.calcLinearlayout.removeView(linearLayout);
        }
        displayedMeasuredPointLinearLayoutStore = null;
    }

    private void displayCalculatedData(List<MeasPoint> chosenMeasPointStore){
        CalcData calcData = new CalcData(chosenMeasPointStore);
        String distanceValue = String.format("%4.2fm", calcData.calcDistance());
        String distanceReliableValue = String.format("±%4.2fm", calcData.calcDistanceReliable());
        binding.distanceValue.setText(distanceValue);
        binding.distanceReliable.setText(distanceReliableValue);
        String perimeterValue = String.format("%4.2fm", calcData.calcPerimeter());
        String perimeterReliableValue = String.format("±%4.2fm", calcData.calcPerimeterReliable());
        binding.perimeterValue.setText(perimeterValue);
        binding.perimeterReliable.setText(perimeterReliableValue);
        String areaValue = String.format("%4.1fm2", calcData.calcArea());
        String areaReliableValue = String.format("±%4.1fm2", calcData.calcAreaReliable());
        binding.areaValue.setText(areaValue);
        binding.areaReliable.setText(areaReliableValue);
        String elevationValue = String.format("%4.2fm", calcData.calcElevation());
        String elevationReliableValue = String.format("±%4.2fm", calcData.calcElevationReliable());
        binding.elevationValue.setText(elevationValue);
        binding.elevationReliable.setText(elevationReliableValue);
    }

    private void initSpinner(){
        List<String> ITEMS = new ArrayList<>();
        ITEMS.add(0, "Válassz pontokat");
        for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
            ITEMS.add(measPoint.getPointIDAsString());
        }
        binding.pointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if( !parent.getItemAtPosition(position).equals("Válassz pontokat") ){
                    int pointId = Integer.parseInt((String) parent.getItemAtPosition(position)) - 1;
                    if( !chosenMeasPointStore.contains(MainActivity.MEAS_POINT_LIST.get(pointId)) ){
                        chosenMeasPointStore.add(MainActivity.MEAS_POINT_LIST.get(pointId) );
                    }
                    clearDisplayedPointData();
                    displayMeasuredPoint(chosenMeasPointStore);
                    displayCalculatedData(chosenMeasPointStore);
                }
                else if( !binding.allPointsCheckbox.isChecked() ) {
                    clearDisplayedPointData();
                    clearCalculatedData();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),
                R.layout.point_spinner, ITEMS);
        binding.pointSpinner.setAdapter(arrayAdapter);
        binding.pointSpinner.setEnabled(false);
    }
    private void clearCalculatedData(){
        String zeroValue = String.format("%4.2fm", 0.0);
        binding.distanceValue.setText(zeroValue);
        binding.perimeterValue.setText(zeroValue);
        binding.elevationValue.setText(zeroValue);
        String zeroReliableValue = String.format("±%4.2fm", 0.0);
        binding.distanceReliable.setText(zeroReliableValue);
        binding.perimeterReliable.setText(zeroReliableValue);
        binding.elevationReliable.setText(zeroReliableValue);
        zeroValue = String.format("%4.1fm2", 0.0);
        zeroReliableValue = String.format("±%4.1fm2", 0.0);
        binding.areaValue.setText(zeroValue);
        binding.areaReliable.setText(zeroReliableValue);
        chosenMeasPointStore.clear();
    }

    private void addOnClickListenerForCheckBox(){
        binding.allPointsCheckbox.setChecked(true);
        binding.allPointsCheckbox.setOnClickListener(c -> {

            clearDisplayedPointData();

            if( binding.allPointsCheckbox.isChecked() ){
                binding.pointSpinner.setEnabled(false);
                initSpinner();
                displayMeasuredPoint(MainActivity.MEAS_POINT_LIST);
                displayCalculatedData(MainActivity.MEAS_POINT_LIST);
            }
            else {
                binding.pointSpinner.setEnabled(true);
                clearCalculatedData();
            }
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
