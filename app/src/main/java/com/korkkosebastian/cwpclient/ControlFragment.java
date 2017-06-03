package com.korkkosebastian.cwpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.cwprotocol.CWPProtocolListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class ControlFragment extends Fragment implements View.OnClickListener, Observer {

    private Button changeFrequencyButton;
    private String frequency;
    private ToggleButton toggleButton;
    private EditText frequencyInput;
    private CWPControl cwpControl;
    private SharedPreferences prefs;

    public ControlFragment() { }

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

        changeFrequencyButton = (Button) view.findViewById(R.id.changeButton);
        changeFrequencyButton.setOnClickListener(this);

        frequencyInput = (EditText) view.findViewById(R.id.frequencyInput);

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
    public void update(Observable o, Object arg) {
        CWPProtocolListener.CWPEvent event = (CWPProtocolListener.CWPEvent) arg;
        switch(event) {
            case EConnected:
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.line_connected), Toast.LENGTH_SHORT).show();
                break;
            case EDisconnected:
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.line_disconnected), Toast.LENGTH_SHORT).show();
                break;
            case EChangedFrequency:
                String newFrequency = Integer.toString(cwpControl.frequency());
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.changed_frequency) + " " + newFrequency, Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
        if(cwpControl.isConnected()) {
            if(Integer.toString(cwpControl.frequency()) != frequency) {
                frequency = Integer.toString(cwpControl.frequency());
            }
        }
        updateView(getView());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView(view);
    }

    public void updateView(View view) {
        if(cwpControl != null) {
            ToggleButton toggle = (ToggleButton) view.findViewById(R.id.connectionToggleButton);
            if (cwpControl.isConnected()) {
                toggle.setChecked(true);
            } else {
                toggle.setChecked(false);
            }
            EditText input = (EditText) view.findViewById(R.id.frequencyInput);
            input.setText(frequency);
        }
    }

    @Override
    public void onClick(View v) {
        if(cwpControl != null) {
            if (changeFrequencyButton.isPressed()) {
                EditText input = (EditText) getView().findViewById(R.id.frequencyInput);
                frequency = input.getText().toString();
                String regex = ("^[-]?\\d+");
                if(frequency.matches(regex)) {
                    int intFre = Integer.parseInt(frequency);
                    try {
                        cwpControl.setFrequency(intFre);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
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