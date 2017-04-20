package com.korkkosebastian.cwpclient.cwprotocol;

import android.os.Handler;
import android.util.Log;

import com.korkkosebastian.cwpclient.model.CWPMessaging;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Observer;

public class CWPProtocolImplementation  implements CWPControl, CWPMessaging, Runnable {

    public enum CWPState {Disconnected, Connected, LineUp, LineDown };

    private CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private int frequency = CWPControl.DEFAULT_FREQUENCY;
    private int messageValue;
    private boolean lineUpByUser = false;

    private Handler receiveHandler = new Handler();
    private CWPConnectionReader cwpConnectionReader = null;
    private CWPProtocolListener cwpProtocolListener;

    private static final int BUFFER_LENGTH = 64;
    private String serverAddress = null;
    private int serverPort = -1;

    private Socket cwpSocket = null;
    private InputStream nis = null;
    private OutputStream nos = null;

    private ByteBuffer outBufffer = null;

    public CWPProtocolImplementation(CWPProtocolListener cwpProtocolListener) {
        this.cwpProtocolListener = cwpProtocolListener;
    }

    @Override
    public void addObserver(Observer observer) {

    }

    @Override
    public void lineUp() throws IOException {
        try {
            if(currentState == CWPState.LineDown || currentState != CWPState.LineUp
                    && !lineUpByUser) {
                cwpConnectionReader.changeProtocolState(CWPState.LineUp, 0);
                lineUpByUser = true;
            }
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
            if(currentState == CWPState.LineUp && lineUpByUser) {
                cwpConnectionReader.changeProtocolState(CWPState.LineDown, 0);
                lineUpByUser = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String serverAddress, int serverPort, int frequency) throws IOException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.frequency = frequency;

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
        try {
            cwpConnectionReader.stopReading();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cwpConnectionReader = null;
        this.serverAddress = null;
        this.serverPort = -1;
        this.frequency = CWPControl.DEFAULT_FREQUENCY;
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

        private int bytesToRead = 4;
        private int bytesRead = 0;

        public CWPConnectionReader(Runnable processor) {
            this.processor = processor;
        }

        public void startReading() {
            running = true;
            start();
        }

        public void stopReading() throws InterruptedException {
            running = false;
            changeProtocolState(CWPState.Disconnected, 0);
        }

        private void initialize() throws InterruptedException, IOException {
            SocketAddress socketAddress = new InetSocketAddress(serverAddress, serverPort);
            cwpSocket = new Socket();
            cwpSocket.connect(socketAddress, 5000);
            nis = cwpSocket.getInputStream();
            nos = cwpSocket.getOutputStream();
            changeProtocolState(CWPState.Connected, 0);
        }

        public void run() {
            byte[] byteArray = new byte[BUFFER_LENGTH];
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_LENGTH);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            try {
                try {
                    initialize();
                    byteBuffer.flip();
                    while (running) {
                        bytesRead = readLoop(byteArray, bytesToRead);
                        if(bytesRead > 0) {
                            byteBuffer.clear();
                            byteBuffer.put(byteArray, 0, bytesToRead);
                            int rcvValue = byteBuffer.getInt(0);
                            if(rcvValue < 0) {
                                changeProtocolState(CWPState.LineDown, 0);
                            }
                            if(rcvValue > 0) {
                                changeProtocolState(CWPState.LineUp, 0);
                                bytesToRead = 2;
                                bytesRead = readLoop(byteArray, bytesToRead);
                                if(bytesRead > 0) {
                                    byteBuffer.clear();
                                    byteBuffer.put(byteArray, 0, bytesToRead);
                                    byteBuffer.position(0);
                                    short consumeBuffer = byteBuffer.getShort(0);
                                    changeProtocolState(CWPState.LineDown, 0);
                                }
                            }
                        }
                    }

                }catch(IOException e){
                        e.printStackTrace();
                    }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void changeProtocolState(CWPState state, int param) throws InterruptedException {
            Log.d(TAG, "Change protocol state to " + state);
            nextState = state;
            messageValue = param;
            receiveHandler.post(processor);
        }

        private int readLoop(byte [] bytes, int bytesToRead) throws IOException {
            int bytesRead = 0;
            do {
                int readNow = nis.read(bytes, bytesRead, bytesToRead - bytesRead);
                if(readNow == -1) {
                    throw new IOException("Read -1 from stream");
                } else {
                    bytesRead = bytesRead + readNow;
                }
            }while(bytesRead < bytesToRead);
            return bytesRead;
        }
    }
}
