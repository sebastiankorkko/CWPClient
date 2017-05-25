package com.korkkosebastian.cwpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.model.CWPMessaging;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import static android.content.Context.MODE_PRIVATE;

public class ControlFragment extends Fragment implements View.OnClickListener, Observer {

    private Button changeFrequencyButton;
    private String frequency;
    private ToggleButton toggleButton;
    private CWPControl cwpControl;
    private SharedPreferences prefs;

    public ControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_fragment, container, false);

        toggleButton = (ToggleButton) view.findViewById(R.id.connectionToggleButton);
        toggleButton.setOnClickListener(this);
        toggleButton.setChecked(false);

        changeFrequencyButton = (Button) view.findViewById(R.id.changeButton);
        changeFrequencyButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(cwpControl != null) {
            cwpControl.deleteObserver(this);
        }
        cwpControl = null;
    }

    @Override
    public void update(Observable o, Object arg) {}

    @Override
    public void onClick(View v) {
        if(cwpControl != null) {
            if (changeFrequencyButton.isPressed()) {
                EditText input = (EditText) getView().findViewById(R.id.frequencyInput);
                frequency = input.getText().toString();
                //frequency = getView().findViewById(R.id.frequencyInput).toString();
                int intFre = Integer.parseInt(frequency);
                try {
                    cwpControl.setFrequency(intFre);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(), "Changed frequency", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getActivity().getApplicationContext(),
                    "buttonstate " + toggleButton.isChecked(), Toast.LENGTH_SHORT).show();
            if (toggleButton.isPressed()) {
                if (toggleButton.isChecked()) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.connecting_cwp), Toast.LENGTH_SHORT);
                    prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String address = prefs.getString("server_address", null);
                    String[] parts = address.split(":");
                    int serverFrequency = Integer.parseInt(prefs.getString("default_frequency", null));
                    int port = Integer.parseInt(parts[1]);
                    address = parts[0];
                    if (parts.length == 2 && port > 0 && port < 65536) {
                        if (!cwpControl.isConnected()) {
                            try {
                                cwpControl.connect(address, port, serverFrequency);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Host address:port not valid", Toast.LENGTH_SHORT).show();
                    }
                } else if (!toggleButton.isChecked()) {
                    if (cwpControl.isConnected()) {
                        try {
                            cwpControl.disconnect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), "cwpMessaging null", Toast.LENGTH_SHORT).show();
        }
    }

    public void setCwpControl(CWPControl cwpControl) {
        if(cwpControl != null) {
            this.cwpControl = cwpControl;
            cwpControl.addObserver(this);
        }
    }

    public void setCwpControlNull() {
        this.cwpControl = null;
    }
}
