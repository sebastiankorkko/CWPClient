package com.korkkosebastian.cwpclient.model;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.korkkosebastian.cwpclient.CWPProvider;
import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;

import java.io.IOException;

public class CWPService extends Service implements CWPProvider {

    private CWPModel cwpModel = null;
    private IBinder cwpBinder = new CWPBinder();
    private int clients = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return cwpBinder;
    }

    @Override
    public void onCreate() {
        this.cwpModel = new CWPModel();
    }

    @Override
    public void onDestroy() {
        try {
            this.cwpModel.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cwpModel = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public CWPMessaging getMessaging() {
        return cwpModel;
    }

    @Override
    public CWPControl getControl() {
        return cwpModel;
    }

    public class CWPBinder extends Binder {
        public CWPService getService() {
            return CWPService.this;
        }
    }

    public void startUsing() {
        clients++;
    }

    public void stopUsing() {
        clients--;
    }
}
