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
import java.util.concurrent.Semaphore;

public class CWPProtocolImplementation  implements CWPControl, CWPMessaging, Runnable {

    public enum CWPState {Disconnected, Connected, LineUp, LineDown };

    private final int RESERVED_VALUE = -2147483648;
    private static final String TAG = "CWPProtocol";

    private CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private int currentFrequency = CWPControl.DEFAULT_FREQUENCY;
    private int messageValue;
    private boolean lineUpByUser = false;
    private boolean lineUpByServer = false;
    private int timeStamp = 0;
    private long initTime = 0;

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

    private Semaphore lock = new Semaphore(1);

    public CWPProtocolImplementation(CWPProtocolListener cwpProtocolListener) {
        this.cwpProtocolListener = cwpProtocolListener;
    }

    @Override
    public void addObserver(Observer observer) {

    }

    @Override
    public void lineUp() throws IOException {
        boolean lineSwitchToUp = false;
        try {
            lock.acquire();
            if((currentState == CWPState.LineDown || currentState != CWPState.LineUp)
                    && !lineUpByUser) {
                timeStamp = (int)(System.currentTimeMillis()-initTime);
                sendMessage(timeStamp);
                if(currentState == CWPState.LineDown && !lineUpByServer) {
                    currentState = CWPState.LineUp;
                    lineSwitchToUp = true;
                }
                lineUpByUser = true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if(lineSwitchToUp) {
            cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.ELineup, timeStamp);
        }
    }

    @Override
    public void deleteObserver(Observer observer) {

    }

    @Override
    public void lineDown() throws IOException {
        boolean lineSwitchToDown = false;
        short lineDownMsg = 0;
        try {
            lock.acquire();
            if(currentState == CWPState.LineUp || lineUpByUser) {
                lineDownMsg = (short)(System.currentTimeMillis() - initTime - timeStamp);
                sendMessage(lineDownMsg);
                lineUpByUser = false;
                if(lineIsUp()) {
                    currentState = CWPState.LineDown;
                    lineSwitchToDown = true;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if(lineSwitchToDown) {
            cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.ELineDown, lineDownMsg);
        }
    }

    @Override
    public void connect(String serverAddress, int serverPort, int frequency) throws IOException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        setFrequency(frequency);
        cwpConnectionReader = new CWPConnectionReader(this);
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
        this.currentFrequency = CWPControl.DEFAULT_FREQUENCY;
        cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.EDisconnected, 0);
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
        if(currentFrequency != frequency) {
            if (frequency > 0) {
                currentFrequency = -frequency;
            } else if (currentFrequency == 0) {
                //not valid -> use default
                currentFrequency = CWPControl.DEFAULT_FREQUENCY;
            } else {
                currentFrequency = frequency;
            }
            sendFrequency();
        }
    }

    public void sendFrequency() throws IOException {
        boolean frequencySwitched = false;
        try {
            lock.acquire();
            if(currentState == CWPState.LineDown) {
                Log.d(TAG, "Sending frequency: " + currentFrequency);
                sendMessage(currentFrequency);
                currentState = CWPState.Connected;
                frequencySwitched = true;
            } else {
                Log.d(TAG, "Line not down - unable to change frequency");
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if(frequencySwitched) {
            cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.EConnected, 0);
        }
    }

    @Override
    public int frequency() {
        return currentFrequency;
    }

    public CWPState getCurrentState() {
        return currentState;
    }

    @Override
    public void run() {
        int tempMessageValue = messageValue;
        switch (nextState) {
            case Connected:
                Log.d(CWPConnectionReader.TAG, "State change to connected happening...");
                currentState = nextState;
                lock.release();
                lineUpByServer = false;
                cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.EConnected, tempMessageValue);
                break;
            case Disconnected:
                Log.d(CWPConnectionReader.TAG, "State change to disconnected happening...");
                currentState = nextState;
                lock.release();
                cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.EDisconnected, 0);
                break;
            case LineUp:
                Log.d(CWPConnectionReader.TAG, "State change to line up happening...");
                if(!lineUpByUser) {
                    currentState = nextState;
                    lock.release();
                    cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.ELineup, tempMessageValue);
                } else {
                    lock.release();
                }
                break;
            case LineDown:
                Log.d(CWPConnectionReader.TAG, "State change to line down happening...");
                if(currentState == CWPState.Connected) {
                    currentState = nextState;
                    lock.release();
                    if(messageValue != currentFrequency) {
                        try {
                            sendFrequency();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.EChangedFrequency, tempMessageValue);
                    }
                } else {
                    lineUpByServer = false;
                    if(!lineUpByUser) {
                        currentState = nextState;
                        lock.release();
                        cwpProtocolListener.onEvent(CWPProtocolListener.CWPEvent.ELineDown, tempMessageValue);
                    } else {
                        lock.release();
                    }
                }
                break;
        }
    }

    private void sendMessage(int msg) throws IOException {
        ByteBuffer outgoingIntBuffer = ByteBuffer.allocate(4);
        outgoingIntBuffer.order(ByteOrder.BIG_ENDIAN);
        outgoingIntBuffer.putInt(0, msg);
        outgoingIntBuffer.position(0);
        byte[] outgoingByteArray = outgoingIntBuffer.array();
        nos.write(outgoingByteArray);
        nos.flush();
        outgoingIntBuffer = null;
        Log.d(TAG, "Sent message: " + msg);
    }

    private void sendMessage(short msg) throws IOException {
        ByteBuffer outgoingShortBuffer = ByteBuffer.allocate(2);
        outgoingShortBuffer.order(ByteOrder.BIG_ENDIAN);
        outgoingShortBuffer.putShort(0, msg);
        outgoingShortBuffer.position(0);
        byte[] outgoingByteArray = outgoingShortBuffer.array();
        nos.write(outgoingByteArray);
        nos.flush();
        outgoingShortBuffer = null;
        Log.d(TAG, "Sent message: " + msg);
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
            initTime = System.currentTimeMillis();
            nis = cwpSocket.getInputStream();
            nos = cwpSocket.getOutputStream();
            lineUpByUser = false;
            lineUpByServer = false;
            changeProtocolState(CWPState.Connected, 0);
        }

        public void run() {
            byte[] byteArray = new byte[BUFFER_LENGTH];
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_LENGTH);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            int bytesRead;
            try {
                try {
                    initialize();
                    byteBuffer.flip();
                    while (running) {
                        bytesToRead = 4;
                        bytesRead = readLoop(byteArray, bytesToRead);
                        if(bytesRead > 0) {
                            byteBuffer.clear();
                            byteBuffer.put(byteArray, 0, bytesToRead);
                            byteBuffer.position(0);
                            int rcvValue = byteBuffer.getInt();
                            Log.d(TAG, "Received: " + rcvValue);
                            if(rcvValue >= 0) {
                                changeProtocolState(CWPState.LineUp, rcvValue);
                                bytesToRead = 2;
                                bytesRead = readLoop(byteArray, bytesToRead);
                                if(bytesRead > 0) {
                                    byteBuffer.clear();
                                    byteBuffer.put(byteArray, 0, bytesRead);
                                    byteBuffer.position(0);
                                    short shortValue = byteBuffer.getShort();
                                    changeProtocolState(CWPState.LineDown, shortValue);
                                }
                            } else if( rcvValue != RESERVED_VALUE) {
                                changeProtocolState(CWPState.LineDown, rcvValue);
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
            lock.acquire();
            nextState = state;
            messageValue = param;
            receiveHandler.post(processor);
        }

        private int readLoop(byte [] bytes, int bytesToRead) throws IOException {
            int bytesRead = 0;
            do {
                int readNow = nis.read(bytes, bytesRead, bytesToRead - bytesRead);
                Log.d(TAG, "Bytes read: " + readNow);
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
