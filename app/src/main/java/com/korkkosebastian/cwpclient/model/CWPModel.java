package com.korkkosebastian.cwpclient.model;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;

import java.io.IOException;
import java.util.Observable;

public class CWPModel extends Observable implements CWPMessaging, CWPControl {

    public enum CWPState {Disconnected, Connected, LineUp, LineDown };

    private CWPState currentState = CWPState.Disconnected;
    private int frequency = CWPControl.DEFAULT_FREQUENCY;

    @Override
    public void lineUp() throws IOException {
        currentState = CWPState.LineUp;
        setChanged();
        notifyObservers(currentState);
    }

    @Override
    public void lineDown() throws IOException {
        currentState = CWPState.LineDown;
        setChanged();
        notifyObservers(currentState);
    }

    @Override
    public boolean isConnected() {
        if(currentState != CWPState.Disconnected) {
            return true;
        }
        return false;
    }

    @Override
    public boolean lineIsUp() {
        if(currentState != CWPState.LineUp) {
            return true;
        }
        return false;
    }

    @Override
    public void connect(String serverAddress, int serverPort, int frequency) throws IOException {
        currentState = CWPState.Connected;
        setChanged();
        notifyObservers(currentState);
    }

    @Override
    public void disconnect() throws IOException {
        currentState = CWPState.Disconnected;
        setChanged();
        notifyObservers(currentState);
    }

    @Override
    public void setFrequency(int frequency) throws IOException {

    }

    @Override
    public int frequency() {
        return 0;
    }
}
