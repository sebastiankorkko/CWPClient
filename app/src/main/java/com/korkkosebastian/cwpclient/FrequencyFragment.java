package com.korkkosebastian.cwpclient;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class FrequencyFragment extends Fragment implements View.OnClickListener {

    private Button changeFrequencyButton;
    private String frequency;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frequency_fragment, container, false);

        changeFrequencyButton = (Button) view.findViewById(R.id.changeButton);
        changeFrequencyButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(changeFrequencyButton.isPressed()) {
            String inputFrequency = getView().findViewById(R.id.frequencyInput).toString();
            //todo
        }
    }
}
