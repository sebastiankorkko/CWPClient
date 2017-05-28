package com.korkkosebastian.cwpclient.cwprotocol;

import java.io.IOException;
import java.util.Observer;

public interface CWPControl {
    public static final int DEFAULT_FREQUENCY = -1;

    public void addObserver(Observer observer);
    public void deleteObserver(Observer observer);
    public void connect(String serverAddress, int serverPort, int frequency) throws IOException;
    public void disconnect() throws IOException;
    public boolean isConnected();

    public void setFrequency(int frequency) throws IOException;
    public int frequency();
}