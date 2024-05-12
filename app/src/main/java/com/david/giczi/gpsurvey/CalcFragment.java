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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.david.giczi.gpsurvey.databinding.FragmentCalcBinding;
import com.david.giczi.gpsurvey.domain.MeasPoint;
import com.david.giczi.gpsurvey.utils.CalcData;
import com.david.giczi.gpsurvey.utils.WrapDataInKML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class CalcFragment extends Fragment {

    private FragmentCalcBinding binding;
    private List<LinearLayout> displayedMeasuredPointLinearLayoutStore;
    private List<MeasPoint> chosenMeasPointList;
    private  ViewGroup saveDataContainer;
    private static final List<String> ITEMS_FOR_KML = Arrays.asList("Pontok", "Vonal", "Kerület");
    private static final List<String> ITEMS_FOR_TXT =
            Arrays.asList("EOV koordináták és számítások", "WGS - decimális",
                    "WGS - fok-perc-mperc", "WGS - XYZ");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentCalcBinding.inflate(inflater, container, false);
        this.chosenMeasPointList = new ArrayList<>();
        MainActivity.PAGE_NUMBER_VALUE = 2;
        addOnClickListenerForCheckBox();
        initPointSpinner();
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
            measPointId.setOnClickListener( d -> deletePointDialog(((TextView) d).getText().toString()));
            String pointID = measPoint.getPointID() + ". pont\t\t±Qyx=" + measPoint.getQ() + "m";
            measPointId.setText(pointID);
            measPointId.setTextSize(18f);
            measPointId.setTypeface(Typeface.DEFAULT_BOLD);
            measPointIDLayout.addView(measPointId);
            displayedMeasuredPointLinearLayoutStore.add(measPointIDLayout);
            binding.calcLinearlayout.addView(measPointIDLayout);
            TextView measPointData = new TextView(getContext());
            if( measPoint.getZ() == 0.0 ){
                measPointData.setTextColor(Color.parseColor("#6750a4"));
            }
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
                  chosenMeasPointList.remove(deletedPoint);
                }
            }
            clearCalculatedData();
            clearDisplayedPointData();
            initPointSpinner();
            if( binding.allPointsCheckbox.isChecked() ){
                displayCalculatedData(MainActivity.MEAS_POINT_LIST);
                displayMeasuredPoint(MainActivity.MEAS_POINT_LIST);
            }
            else if( !chosenMeasPointList.isEmpty() ){
                displayCalculatedData(chosenMeasPointList);
                displayMeasuredPoint(chosenMeasPointList);
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
            builder.setMessage("Menteni kívánja az összes mért pontot és a pontokból számított adatokat?");
        }
        else{
            builder.setMessage("Menteni kívánja a mért, kiválasztott pontokat és a pontokból számított adatokat?");

        }
        builder.setPositiveButton("Igen", (dialog, which) -> popupSaveWindow(saveAllPoints));

        builder.setNegativeButton("Nem", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isNotCorrectDataForSaving(boolean saveAllPoints){
        String dataType = (String) ((Spinner) saveDataContainer.findViewById(R.id.data_type_spinner)).getSelectedItem();
        if( saveAllPoints && MainActivity.MEAS_POINT_LIST.size()  < 2 && dataType.equals(ITEMS_FOR_KML.get(1))){
            Toast.makeText(getContext(), "Vonal mentéséhez legalább 2 pont szükséges.", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if( saveAllPoints && MainActivity.MEAS_POINT_LIST.size()  < 3 && dataType.equals(ITEMS_FOR_KML.get(2))){
            Toast.makeText(getContext(), "Kerület mentéséhez legalább 3 pont szükséges.", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if( !saveAllPoints && chosenMeasPointList.size()  < 2 && dataType.equals(ITEMS_FOR_KML.get(2))){
            Toast.makeText(getContext(), "Vonal mentéséhez legalább 2 pont szükséges.", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if( !saveAllPoints && chosenMeasPointList.size()  < 3 && dataType.equals(ITEMS_FOR_KML.get(2))){
            Toast.makeText(getContext(), "Kerület mentéséhez legalább 3 pont szükséges.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void setFileName(boolean saveAllPoints){
        if(isNotCorrectDataForSaving(saveAllPoints)){
            return;
        }
        if( saveAllPoints ) {
            ((EditText) saveDataContainer.findViewById(R.id.file_name_input_field))
                    .setText(getSaveFileName(MainActivity.MEAS_POINT_LIST));
        }
        else {
            ((EditText) saveDataContainer.findViewById(R.id.file_name_input_field))
                    .setText(getSaveFileName(chosenMeasPointList));
        }
        saveDataContainer.findViewById(R.id.button_save).setEnabled(true);
    }

    private String getSaveFileName(List<MeasPoint> points){
        String dataType = (String) ((Spinner) saveDataContainer.findViewById(R.id.data_type_spinner)).getSelectedItem();
        String fileName = "";
        if( points.size() == 1 && dataType.equals(ITEMS_FOR_KML.get(0))){
            fileName = "_" + points.get(0).getPointID() + "_pont.kml";
        }
        else if( points.size() > 1 && dataType.equals(ITEMS_FOR_KML.get(0))){
            fileName = "_" + points.get(0).getPointID() + "-"
                    + points.get(points.size() - 1).getPointID() + "_pontok.kml";
        }
        else if( points.size() > 1 && dataType.equals(ITEMS_FOR_KML.get(1))){
            fileName = "_" + points.get(0).getPointID() + "-"
                    + points.get(points.size() - 1).getPointID() + "_vonal.kml";
        }
        else if( points.size() > 1 && dataType.equals(ITEMS_FOR_KML.get(2))){
            fileName = "_" + points.get(0).getPointID() + "-"
                            + points.get(points.size() - 1).getPointID() + "_kerulet.kml";
        }
        else if( points.size() == 1 && dataType.equals(ITEMS_FOR_TXT.get(0))){
            fileName = "_" + points.get(0).getPointID() + "_pont_EOV.txt";
        }
        else if( points.size() > 1 && dataType.equals(ITEMS_FOR_TXT.get(0))){
            fileName = "_" + points.get(0).getPointID() + "-"
                    + points.get(points.size() - 1).getPointID() + "_pontok_EOV.txt";
        }
        else if( points.size() == 1 && dataType.equals(ITEMS_FOR_TXT.get(1))){
            fileName = "_" + points.get(0).getPointID() + "_pont_WGS.txt";
        }
        else if( points.size() > 1 && dataType.equals(ITEMS_FOR_TXT.get(1))){
            fileName = "_" + points.get(0).getPointID() + "-"
                    + points.get(points.size() - 1).getPointID() + "_pontok_WGS.txt";
        }
        else if( points.size() == 1 && dataType.equals(ITEMS_FOR_TXT.get(2))){
            fileName = "_" + points.get(0).getPointID() + "_pont_WGS-fpmp.txt";
        }
        else if( points.size() > 1 && dataType.equals(ITEMS_FOR_TXT.get(2))){
            fileName = "_" + points.get(0).getPointID() + "-" + points.get(points.size() - 1).getPointID()
                    + "_pontok_WGS-fpmp.txt";
        }
        else if( points.size() == 1 && dataType.equals(ITEMS_FOR_TXT.get(3))){
            fileName = "_" + points.get(0).getPointID() + "_pont_WGS-XYZ.txt";
        }
        else if( points.size() > 1 && dataType.equals(ITEMS_FOR_TXT.get(3))){
            fileName = "_" + points.get(0).getPointID() + "-" + points.get(points.size() - 1).getPointID()
                    + "_pontok_WGS-XYZ.txt";
        }
        return fileName;
    }

    private void popupSaveWindow(boolean saveAllPoints){
        saveDataContainer =  (ViewGroup) getLayoutInflater().inflate(R.layout.fragment_save, null);
        PopupWindow saveDataWindow = new PopupWindow(saveDataContainer, 900,1600, true);
        saveDataWindow.showAtLocation( binding.getRoot(), Gravity.CENTER, 0, 0);
        Button saveButton = saveDataContainer.findViewById(R.id.button_save);
        saveButton.setBackgroundColor(Color.DKGRAY);
        saveButton.setOnClickListener(s -> {
           if(isNotCorrectDataForSaving(saveAllPoints)){
               return;
            }
            saveDataProcess(saveAllPoints);
            saveDataWindow.dismiss();
        });
        RadioButton radioButtonForKML = saveDataContainer.findViewById(R.id.kml_format);
        radioButtonForKML.setChecked(true);
        radioButtonForKML.setOnClickListener(e -> {
            initDataTypeSpinner(saveAllPoints);
            setFileName(saveAllPoints);});
        RadioButton radioButtonForTXT = saveDataContainer.findViewById(R.id.txt_format);
        radioButtonForTXT.setOnClickListener(e -> {
            initDataTypeSpinner(saveAllPoints);
            setFileName(saveAllPoints);});
        initDataTypeSpinner(saveAllPoints);
        setFileName(saveAllPoints);
    }

    private void initDataTypeSpinner(boolean saveAllPoints){
        ArrayAdapter<String> arrayAdapter;
        RadioButton radioButtonForKMZ = saveDataContainer.findViewById(R.id.kml_format);
        RadioButton radioButtonForTXT = saveDataContainer.findViewById(R.id.txt_format);
        if( radioButtonForKMZ.isChecked() ){
            arrayAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.data_type_spinner, ITEMS_FOR_KML);
        }
        else if( radioButtonForTXT.isChecked() ){
            arrayAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.data_type_spinner, ITEMS_FOR_TXT );
        }
        else{
            arrayAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.data_type_spinner, Collections.singletonList("-"));
        }
        ((Spinner) saveDataContainer.findViewById(R.id.data_type_spinner)).setAdapter(arrayAdapter);
        ((Spinner) saveDataContainer.findViewById(R.id.data_type_spinner))
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setFileName(saveAllPoints);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
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

    private void initPointSpinner(){
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
                    if( chosenMeasPointList.contains(chosenPoint) ){
                        return;
                    }
                    chosenMeasPointList.add(chosenPoint);
                    clearDisplayedPointData();
                    displayMeasuredPoint(chosenMeasPointList);
                    displayCalculatedData(chosenMeasPointList);
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
        chosenMeasPointList.clear();
    }

    private void addOnClickListenerForCheckBox(){
        binding.allPointsCheckbox.setChecked(true);
        binding.allPointsCheckbox.setOnClickListener(c -> {

            clearDisplayedPointData();

            if( binding.allPointsCheckbox.isChecked() ){
                binding.pointSpinner.setEnabled(false);
                initPointSpinner();
                displayMeasuredPoint(MainActivity.MEAS_POINT_LIST);
                displayCalculatedData(MainActivity.MEAS_POINT_LIST);
            }
            else {
                binding.pointSpinner.setEnabled(true);
                clearCalculatedData();
                if( !MainActivity.MEAS_POINT_LIST.isEmpty() ){
                    savePointDialog(true);
                }
            }
        });
    }

    private void saveDataProcess(boolean saveAllPoints){
       String fileName = ((EditText) saveDataContainer.findViewById(R.id.file_name_input_field)).getText().toString();
       if( fileName.isEmpty() ){
           Toast.makeText(getContext(), "Fájlnév megadása szükséges.", Toast.LENGTH_SHORT).show();
           return;
       }
       boolean isTXTFormat = ((RadioButton) saveDataContainer.findViewById(R.id.txt_format)).isChecked();
       String dataType = (String) ((Spinner) saveDataContainer.findViewById(R.id.data_type_spinner)).getSelectedItem();

       if( isTXTFormat ){

           if( dataType.equals(ITEMS_FOR_TXT.get(0)) ){
               saveMeasPointAndCalculatedDataInEOVFormat(fileName, saveAllPoints);
           }
           else if( dataType.equals(ITEMS_FOR_TXT.get(1)) ){
               saveMeasPointDataInWGSDecimalFormat(fileName, saveAllPoints);
           }
           else if( dataType.equals(ITEMS_FOR_TXT.get(2)) ){
                saveMeasPointDataInWGSAngleMinSecFormat(fileName, saveAllPoints);
           }
           else if( dataType.equals(ITEMS_FOR_TXT.get(3)) ){
                saveMeasPointDataInWGSXYZFormat(fileName, saveAllPoints);
           }

       }
       else {
            saveMeasDataInKMLFormat(fileName, dataType, saveAllPoints);
       }
    }

    private void saveMeasDataInKMLFormat(String fileName, String dataType, boolean saveAllPoints){
        WrapDataInKML wrapDataInKML;
        if( saveAllPoints ){
            wrapDataInKML =
                    new WrapDataInKML(MainActivity.MEAS_POINT_LIST, dataType, fileName);
        }
        else{
            wrapDataInKML =
                    new WrapDataInKML(chosenMeasPointList, dataType, fileName);
        }
        wrapDataInKML.createDataListForKML(getContext());
        File projectFile =
                new File(Environment.getExternalStorageDirectory(),
                        "/Documents/" + fileName);

        if( projectFile.exists() ){
            Toast.makeText(getContext(), projectFile.getName() +
                    "\nlétező projekt fájl, mentés sikertelen.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(projectFile));

            for (String dataForKML : wrapDataInKML.getKmlDataList()) {
                bw.write(dataForKML);
                bw.newLine();
            }
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

    private void saveMeasPointAndCalculatedDataInEOVFormat(String fileName, boolean saveAllPoints) {
        File projectFile =
                new File(Environment.getExternalStorageDirectory(),
                        "/Documents/" + fileName);

        if( projectFile.exists() ){
            Toast.makeText(getContext(), projectFile.getName() +
                    "\nlétező projekt fájl, mentés sikertelen.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(projectFile));

            for (MeasPoint measPoint : (saveAllPoints ? MainActivity.MEAS_POINT_LIST : chosenMeasPointList)) {
                bw.write(measPoint.getEOVMeasPontData());
                bw.newLine();
            }
            bw.write(new CalcData(MainActivity.MEAS_POINT_LIST).getCalculatedData());
            bw.newLine();
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

    private void saveMeasPointDataInWGSDecimalFormat(String fileName, boolean saveAllPoints) {
        File projectFile =
                new File(Environment.getExternalStorageDirectory(),
                        "/Documents/" + fileName);

        if( projectFile.exists() ){
            Toast.makeText(getContext(), projectFile.getName() +
                    "\nlétező projekt fájl, mentés sikertelen.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(projectFile));
            for (MeasPoint measPoint : (saveAllPoints ? MainActivity.MEAS_POINT_LIST : chosenMeasPointList)) {
                bw.write(measPoint.getPointID() + "," + measPoint.getWGSMeasPointDataInDecimalFormat());
                bw.newLine();
            }
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

    private void saveMeasPointDataInWGSAngleMinSecFormat(String fileName, boolean saveAllPoints) {
        File projectFile =
                new File(Environment.getExternalStorageDirectory(),
                        "/Documents/" + fileName);

        if( projectFile.exists() ){
            Toast.makeText(getContext(), projectFile.getName() +
                    "\nlétező projekt fájl, mentés sikertelen.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(projectFile));
            for (MeasPoint measPoint : (saveAllPoints ? MainActivity.MEAS_POINT_LIST : chosenMeasPointList)) {
                bw.write(measPoint.getWGSMeasPointDataInAngelMinSecFormat());
                bw.newLine();
            }
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

    private void saveMeasPointDataInWGSXYZFormat(String fileName, boolean saveAllPoints) {
        File projectFile =
                new File(Environment.getExternalStorageDirectory(),
                        "/Documents/" + fileName);

        if( projectFile.exists() ){
            Toast.makeText(getContext(), projectFile.getName() +
                    "\nlétező projekt fájl, mentés sikertelen.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(projectFile));
            for (MeasPoint measPoint : (saveAllPoints ? MainActivity.MEAS_POINT_LIST : chosenMeasPointList)) {
                bw.write(measPoint.getWGSMeasPointDataInXYZFormat());
                bw.newLine();
            }
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
