package com.korkkosebastian.cwpclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.model.CWPMessaging;
import com.korkkosebastian.cwpclient.model.CWPModel;

public class MainActivity extends AppCompatActivity implements CWPProvider {

    private CWPModel cwpModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cwpModel = new CWPModel();
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cwpModel = null;
    }

    @Override
    public CWPMessaging getMessaging() {
        return cwpModel;
    }

    @Override
    public CWPControl getControl() {
        return cwpModel;
    }
}
