package com.korkkosebastian.cwpclient.model;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.cwprotocol.CWPProtocolImplementation;
import com.korkkosebastian.cwpclient.cwprotocol.CWPProtocolListener;

import java.io.IOException;
import java.util.Observable;

public class CWPModel extends Observable implements CWPMessaging, CWPControl, CWPProtocolListener {

    private CWPProtocolImplementation cwpProtocolImplementation;

    public CWPModel() {
        cwpProtocolImplementation = new CWPProtocolImplementation(this);
    }

    @Override
    public void lineUp() throws IOException {
        cwpProtocolImplementation.lineUp();
    }

    @Override
    public void lineDown() throws IOException {
        cwpProtocolImplementation.lineDown();
    }

    @Override
    public boolean isConnected() {
        return cwpProtocolImplementation.isConnected();
    }

    @Override
    public boolean lineIsUp() {
        return cwpProtocolImplementation.lineIsUp();
    }

    @Override
    public void connect(String serverAddress, int serverPort, int frequency) throws IOException {
        cwpProtocolImplementation.connect(serverAddress, serverPort, frequency);
    }

    @Override
    public void disconnect() throws IOException {
        cwpProtocolImplementation.disconnect();
    }

    @Override
    public void setFrequency(int frequency) throws IOException {
        cwpProtocolImplementation.setFrequency(frequency);
    }

    @Override
    public int frequency() {
        return cwpProtocolImplementation.frequency();
    }

    @Override
    public void onEvent(CWPEvent event, int param) {
        setChanged();
        notifyObservers(event);
    }
}