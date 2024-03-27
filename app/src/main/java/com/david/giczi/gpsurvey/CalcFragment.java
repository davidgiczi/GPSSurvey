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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.david.giczi.gpsurvey.databinding.FragmentCalcBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.CalcData;

import java.util.ArrayList;
import java.util.List;


public class CalcFragment extends Fragment implements AdapterView.OnItemSelectedListener {

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
        this.chosenMeasPointStore = new ArrayList<>(MainActivity.MEAS_POINT_LIST);
        MainActivity.PAGE_NUMBER_VALUE = 2;
        onClickCheckBox();
        initSpinner();
        displayCalculatedData();
        displayAllMeasuredPoints();
        return binding.getRoot();
    }

    private void displayAllMeasuredPoints(){
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
        for (LinearLayout linearLayout : displayedMeasuredPointLinearLayoutStore) {
            binding.calcLinearlayout.removeView(linearLayout);
        }
        displayedMeasuredPointLinearLayoutStore = null;
    }

    private void displayCalculatedData(){
        CalcData calcData = new CalcData(chosenMeasPointStore);
        String distanceValue = String.format("%4.2fm", calcData.calcDistance());
        binding.distanceValue.setText(distanceValue);
        String perimeterValue = String.format("%4.2fm", calcData.calcPerimeter());
        binding.perimeterValue.setText(perimeterValue);
        String areaValue = String.format("%6.2fm2", calcData.calcArea());
        binding.areaValue.setText(areaValue);
        String elevationValue = String.format("%4.2fm", calcData.calcElevation());
        binding.elevationValue.setText(elevationValue);
    }

    private void initSpinner(){
        List<String> ITEMS = new ArrayList<>();
        ITEMS.add(0, "Válassz pontokat");
        for (MeasPoint measPoint : chosenMeasPointStore) {
            ITEMS.add(measPoint.getPointIDAsString());
        }
        binding.pointSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),  android.R.layout.simple_spinner_item, ITEMS);
        binding.pointSpinner.setAdapter(arrayAdapter);
        binding.pointSpinner.setEnabled(false);
    }

    private void onClickCheckBox(){
        binding.allPointsCheckbox.setOnClickListener(c -> {

            if( binding.allPointsCheckbox.isChecked() ){
                binding.pointSpinner.setEnabled(false);
                initSpinner();
                displayAllMeasuredPoints();
                displayCalculatedData();
            }
            else {
                binding.pointSpinner.setEnabled(true);
                String zeroValue = String.format("%4.2fm", 0.0);
                binding.distanceValue.setText(zeroValue);
                binding.perimeterValue.setText(zeroValue);
                binding.elevationValue.setText(zeroValue);
                zeroValue = String.format("%6.2fm2", 0.0);
                binding.areaValue.setText(zeroValue);
                clearDisplayedPointData();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if( !parent.getItemAtPosition(position).equals("Válassz pontokat") ){
                    String item = (String) parent.getItemAtPosition(position);
                    Toast.makeText(getContext(), item, Toast.LENGTH_SHORT).show();
                }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
