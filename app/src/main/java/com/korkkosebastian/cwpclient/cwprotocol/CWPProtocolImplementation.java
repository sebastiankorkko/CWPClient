package com.korkkosebastian.cwpclient.cwprotocol;

import android.os.Handler;
import android.util.Log;

import com.korkkosebastian.cwpclient.model.CWPMessaging;

import java.io.IOException;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class CWPProtocolImplementation  implements CWPControl, CWPMessaging, Runnable {

    public enum CWPState {Disconnected, Connected, LineUp, LineDown };

    private CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private int frequency = CWPControl.DEFAULT_FREQUENCY;
    private int messageValue;

    private Handler receiveHandler = new Handler();
    private CWPConnectionReader cwpConnectionReader = null;
    private CWPProtocolListener cwpProtocolListener;

    public CWPProtocolImplementation(CWPProtocolListener cwpProtocolListener) {
        this.cwpProtocolListener = cwpProtocolListener;
    }

    @Override
    public void addObserver(Observer observer) {

    }

    @Override
    public void lineUp() throws IOException {
        try {
            cwpConnectionReader.changeProtocolState(CWPState.LineUp, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteObserver(Observer observer) {

    }

    @Override
    public void lineDown() throws IOException {
        try {
            cwpConnectionReader.changeProtocolState(CWPState.LineDown, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String serverAddress, int serverPort, int frequency) throws IOException {
        cwpConnectionReader = new CWPConnectionReader(this);
        try {
            cwpConnectionReader.changeProtocolState(CWPState.Connected, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cwpConnectionReader.startReading();
    }

    @Override
    public boolean lineIsUp() {
        if(currentState == CWPState.LineUp) {
            return true;
        }
        return false;
    }

    @Override
    public void disconnect() throws IOException {
        currentState = CWPState.Disconnected;
        try {
            cwpConnectionReader.stopReading();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cwpConnectionReader = null;
    }

    @Override
    public boolean isConnected() {
        if(currentState != CWPState.Disconnected) {
            return true;
        }
        return false;
    }

    @Override
    public void setFrequency(int frequency) throws IOException {
        this.frequency = frequency;
    }

    @Override
    public int frequency() {
        return frequency;
    }

    public CWPState getCurrentState() {
        return currentState;
    }

    @Override
    public void run() {
        switch (nextState) {
            case Connected:
                Log.d(CWPConnectionReader.TAG, "State change to connected happening...");
                currentState = nextState;
                cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.EConnected, 0);
                break;
            case Disconnected:
                Log.d(CWPConnectionReader.TAG, "State change to disconnected happening...");
                currentState = nextState;
                cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.EDisconnected, 0);
                break;
            case LineUp:
                Log.d(CWPConnectionReader.TAG, "State change to line up happening...");
                currentState = nextState;
                cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.ELineup, 0);
                break;
            case LineDown:
                Log.d(CWPConnectionReader.TAG, "State change to line down happening...");
                currentState = nextState;
                cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.ELineDown, 0);
                break;
        }
    }

    private class CWPConnectionReader extends Thread {

        private boolean running = false;
        private Runnable processor = null;
        private static final String TAG = "CWPReader";

        private Timer timer = null;
        private TimerTask timerTask = null;

        public CWPConnectionReader(Runnable processor) {
            this.processor = processor;
        }

        public void startReading() {
            running = true;
            start();
        }

        public void stopReading() throws InterruptedException {
            timer.cancel();
            running = false;
            timer = null;
            timerTask = null;
            changeProtocolState(CWPState.Disconnected, 0);
        }

        private void initialize() throws InterruptedException {
            changeProtocolState(CWPState.Connected, 0);
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(currentState == CWPState.LineUp) {
                        try {
                            changeProtocolState(CWPState.LineDown, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(currentState == CWPState.LineDown) {
                        try {
                            changeProtocolState(CWPState.LineUp, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 3000);

        }

        public void run() {
            try {
                initialize();
                changeProtocolState(CWPState.LineDown, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(running) {

            }
        }

        private void changeProtocolState(CWPState state, int param) throws InterruptedException {
            Log.d(TAG, "Change protocol state to " + state);
            nextState = state;
            messageValue = param;
            receiveHandler.post(processor);
        }


    }
}
