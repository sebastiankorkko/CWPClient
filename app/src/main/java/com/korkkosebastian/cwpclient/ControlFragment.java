package com.korkkosebastian.cwpclient;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.model.CWPMessaging;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class ControlFragment extends Fragment implements View.OnClickListener, Observer {

    private ToggleButton toggleButton;
    private CWPControl cwpControl;

    public ControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_fragment, container, false);

        toggleButton = (ToggleButton) view.findViewById(R.id.connectionToggleButton);
        toggleButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CWPProvider cwpProvider = (CWPProvider) getActivity();
        cwpControl = cwpProvider.getControl();
        cwpControl.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cwpControl.deleteObserver(this);
        cwpControl = null;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(!cwpControl.isConnected()) {
            toggleButton.setText("Disconnect");
        } else {
            toggleButton.setText("Connect");
        }
    }

    @Override
    public void onClick(View v) {
        if(toggleButton.isChecked()) {
           //connect
            try {
                cwpControl.connect("cwp.opimobi.com", 20000, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                cwpControl.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
