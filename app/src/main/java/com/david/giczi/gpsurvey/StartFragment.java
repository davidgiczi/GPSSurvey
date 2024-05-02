package com.david.giczi.gpsurvey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.david.giczi.gpsurvey.databinding.FragmentStartBinding;



public class StartFragment extends Fragment {

    private FragmentStartBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
      binding = FragmentStartBinding.inflate(inflater, container, false);
      binding.startPage.setBackground(((MainActivity) requireActivity()).getDrawable(R.drawable.background_satellite));
      MainActivity.PAGE_NUMBER_VALUE = 0;
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}