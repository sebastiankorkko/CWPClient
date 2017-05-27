package com.korkkosebastian.cwpclient.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.korkkosebastian.cwpclient.CWPProvider;
import com.korkkosebastian.cwpclient.MainActivity;
import com.korkkosebastian.cwpclient.R;
import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class CWPService extends Service implements CWPProvider, Observer {

    private CWPModel cwpModel = null;
    private final IBinder cwpBinder = new CWPBinder();
    private int clients = 0;
    private Signaller signaller;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager notificationManager;

    private static final String TAG = "CWPService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return cwpBinder;
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        this.cwpModel = new CWPModel();
        cwpModel.addObserver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            this.cwpModel.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cwpModel.deleteObserver(this);
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

    @Override
    public void update(Observable o, Object arg) {
            if(clients == 0) {
                Log.d(TAG, "Notification service used");
                boolean shouldNotify = false;
                int notificationId = -1;
                if(mBuilder == null) {
                    mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp);
                }
                if(notificationManager == null) {
                    notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                }
                if (cwpModel.lineIsUp()) {
                    mBuilder.setContentTitle(getString(R.string.line_is_up))
                            .setContentText(getString(R.string.line_is_up_def));
                    shouldNotify = true;
                    notificationId = 001;
                } else if (!cwpModel.isConnected()) {
                    mBuilder.setContentTitle(getString(R.string.line_disconnected))
                            .setContentText(getString(R.string.line_disconnected_def));
                    shouldNotify = true;
                    notificationId = 002;
                } else {
                    mBuilder.setContentTitle("I'm still running")
                            .setContentText("Yup");
                    shouldNotify = true;
                    notificationId = 003;
                }
                if(shouldNotify) {
                    Intent resultIntent = new Intent(this, MainActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    notificationManager.notify(notificationId, mBuilder.build());
                }
            }
    }

    public class CWPBinder extends Binder {
        public CWPService getService() {
            return CWPService.this;
        }
    }

    public void startUsing() {
        this.clients++;
        if(clients == 1) {
            if(signaller == null) {
                signaller = new Signaller();
                signaller.setCwpMessaging(cwpModel);
            }
            if(mBuilder != null) {
                mBuilder = null;
            }
            notificationManager.cancelAll();
        }
    }

    public void stopUsing() {
        this.clients--;
        if(clients == 0) {
            if (signaller != null) {
                signaller.setCwpMessagingNull();
                signaller = null;
            }
        }
        Log.d(TAG, "Client stopped using service - clients: " + clients);
    }
}
