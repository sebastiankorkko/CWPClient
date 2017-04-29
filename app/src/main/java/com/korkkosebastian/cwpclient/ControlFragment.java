package com.korkkosebastian.cwpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import static android.content.Context.MODE_PRIVATE;

public class ControlFragment extends Fragment implements View.OnClickListener, Observer {

    private ToggleButton toggleButton;
    private CWPControl cwpControl;
    private SharedPreferences prefs;

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
        toggleButton.setChecked(false);

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
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String address = prefs.getString("server_address", null);
        String[] parts = address.split(":");
        if(parts.length == 2) {
            int port = Integer.parseInt(parts[1]);
            address = parts[0];

            if (!cwpControl.isConnected()) {
                //connect
                try {
                    cwpControl.connect(address, port, 5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(cwpControl.isConnected()) {
                try {
                    cwpControl.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!cwpControl.isConnected()) {
            toggleButton.setChecked(false);
        } else {
            toggleButton.setChecked(true);
        }
    }


}
