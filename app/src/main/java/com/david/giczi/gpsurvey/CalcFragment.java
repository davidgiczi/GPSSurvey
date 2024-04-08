package com.david.giczi.gpsurvey;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.david.giczi.gpsurvey.databinding.FragmentCalcBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.CalcData;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


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
        if( !MainActivity.MEAS_POINT_LIST.isEmpty() ){
            savePointDialog(true);
        }
        return binding.getRoot();
    }

    private void displayMeasuredPoint(List<MeasPoint> chosenMeasPointStore){
        this.displayedMeasuredPointLinearLayoutStore = new ArrayList<>();
        for (MeasPoint measPoint : chosenMeasPointStore) {
            LinearLayout measPointIDLayout = new LinearLayout(getContext());
            measPointIDLayout.setOrientation(LinearLayout.HORIZONTAL);
            measPointIDLayout.setGravity(Gravity.CENTER);
            TextView measPointId = new TextView(getContext());
            measPointId.setOnClickListener( d -> deletePointDialog(((TextView) d).getText().toString()));
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

    private void deletePointDialog(String pointText) {
        String pointNumber = pointText.split("\\.")[0];
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(pointNumber + ". pont törlése");
        builder.setMessage("Biztos, hogy törli a pontot?");
        builder.setPositiveButton("Igen", (dialog, which) -> {
            int pointId = Integer.parseInt(pointNumber);
            for (int i = MainActivity.MEAS_POINT_LIST.size() - 1; i >= 0; i--) {
                if ( pointId == MainActivity.MEAS_POINT_LIST.get(i).getPointID() ) {
                  MeasPoint deletedPoint = MainActivity.MEAS_POINT_LIST.remove(i);
                  chosenMeasPointStore.remove(deletedPoint);
                }
            }
            clearCalculatedData();
            clearDisplayedPointData();
            initSpinner();
            if( binding.allPointsCheckbox.isChecked() ){
                displayCalculatedData(MainActivity.MEAS_POINT_LIST);
                displayMeasuredPoint(MainActivity.MEAS_POINT_LIST);
            }
            else if( !chosenMeasPointStore.isEmpty() ){
                displayCalculatedData(chosenMeasPointStore);
                displayMeasuredPoint(chosenMeasPointStore);
            }
        });

        builder.setNegativeButton("Nem", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void savePointDialog(boolean saveAllPoints) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Adatok fájlba mentése");
        if( saveAllPoints ){
            builder.setMessage("Menteni kívánja az összes mért pontot és/vagy a pontokból számított adatokat?");
        }
        else{
            builder.setMessage("Menteni kívánja a mért és kiválasztott pontokat és/vagy a pontokból számított adatokat?");

        }
        builder.setPositiveButton("Igen", (dialog, which) -> popupSaveWindow());

        builder.setNegativeButton("Nem", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void popupSaveWindow(){
        ViewGroup saveDataContainer =  (ViewGroup) getLayoutInflater().inflate(R.layout.fragment_save, null);
        PopupWindow saveDataWindow = new PopupWindow(saveDataContainer, 900,1600, true);
        saveDataWindow.showAtLocation( binding.getRoot(), Gravity.CENTER, 0, 0);
        saveDataContainer.findViewById(R.id.button_save).setBackgroundColor(Color.DKGRAY);
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
        String distanceValue = String.format(Locale.getDefault(),"%4.2fm", calcData.calcDistance());
        String distanceReliableValue = String.format(Locale.getDefault(), "±%4.2fm", calcData.calcDistanceReliable());
        binding.distanceValue.setText(distanceValue);
        binding.distanceReliable.setText(distanceReliableValue);
        String perimeterValue = String.format(Locale.getDefault(), "%4.2fm", calcData.calcPerimeter());
        String perimeterReliableValue = String.format(Locale.getDefault(), "±%4.2fm", calcData.calcPerimeterReliable());
        binding.perimeterValue.setText(perimeterValue);
        binding.perimeterReliable.setText(perimeterReliableValue);
        String areaValue = String.format(Locale.getDefault(), "%4.1fm2", calcData.calcArea());
        String areaReliableValue = String.format(Locale.getDefault(), "±%4.1fm2", calcData.calcAreaReliable());
        binding.areaValue.setText(areaValue);
        binding.areaReliable.setText(areaReliableValue);
        String elevationValue = String.format(Locale.getDefault(), "%4.2fm", calcData.calcElevation());
        String elevationReliableValue = String.format(Locale.getDefault(), "±%4.2fm", calcData.calcElevationReliable());
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
                    int pointId = Integer.parseInt((String) parent.getItemAtPosition(position));
                    MeasPoint chosenPoint = null;
                    for (MeasPoint measPoint : MainActivity.MEAS_POINT_LIST) {
                        if( pointId == measPoint.getPointID() ){
                            chosenPoint = measPoint;
                        }
                    }
                    if( chosenMeasPointStore.contains(chosenPoint) ){
                        return;
                    }
                    chosenMeasPointStore.add(chosenPoint);
                    clearDisplayedPointData();
                    displayMeasuredPoint(chosenMeasPointStore);
                    displayCalculatedData(chosenMeasPointStore);
                    savePointDialog(false);
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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.point_spinner, ITEMS);
        binding.pointSpinner.setAdapter(arrayAdapter);
        binding.pointSpinner.setEnabled(false);
    }
    private void clearCalculatedData(){
        String zeroValue = String.format(Locale.getDefault(), "%4.2fm", 0.0);
        binding.distanceValue.setText(zeroValue);
        binding.perimeterValue.setText(zeroValue);
        binding.elevationValue.setText(zeroValue);
        String zeroReliableValue = String.format(Locale.getDefault(), "±%4.2fm", 0.0);
        binding.distanceReliable.setText(zeroReliableValue);
        binding.perimeterReliable.setText(zeroReliableValue);
        binding.elevationReliable.setText(zeroReliableValue);
        zeroValue = String.format(Locale.getDefault(), "%4.1fm2", 0.0);
        zeroReliableValue = String.format(Locale.getDefault(), "±%4.1fm2", 0.0);
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

    private void saveProjectFile(String fileName) {

        File projectFile =
                new File(Environment.getExternalStorageDirectory(),
                        "/Documents/" + fileName + ".txt");
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(projectFile));

            bw.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), projectFile.getName() +
                    "\nprojekt fájl mentése sikertelen.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(),
                "Projekt fájl mentve:\n"
                        + projectFile.getName() , Toast.LENGTH_SHORT).show();
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
