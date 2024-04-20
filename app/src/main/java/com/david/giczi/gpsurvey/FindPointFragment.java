package com.david.giczi.gpsurvey;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.david.giczi.gpsurvey.databinding.FragmentFindPointBinding;



public class FindPointFragment extends Fragment {


    private FragmentFindPointBinding binding;
    private int findPointDistance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFindPointBinding.inflate(inflater, container, false);
        binding.lookForPointButton.setBackgroundColor(Color.DKGRAY);
        MainActivity.PAGE_NUMBER_VALUE = 3;
        addFindPointDirectionArrowImage(0, 0);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
