package com.korkkosebastian.cwpclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.model.CWPMessaging;
import com.korkkosebastian.cwpclient.model.CWPService;

public class MainActivity extends AppCompatActivity implements CWPProvider {

    private CWPService cwpService;
    private boolean isBound = false;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new MainActivity.SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        Intent i = new Intent(this, CWPService.class);
        Log.d(TAG, getApplicationContext().toString());
        String activityName = (startService(i)).toString();
        if(activityName == null) {
            Log.d(TAG, "Service returned null");
        } else {
            Log.d(TAG, "Service started");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public CWPMessaging getMessaging() {
        if(isBound && cwpService.getMessaging() != null) {
            return cwpService.getMessaging();
        } else {
            return null;
        }
    }

    @Override
    public CWPControl getControl() {
        if(isBound) {
            return cwpService.getControl();
        } else {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent(this, CWPService.class);
        Log.d(TAG, getApplicationContext().toString());
        boolean bindProcessStarted = bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        if(!bindProcessStarted) {
            Log.d(TAG, "Bind process didn't start");
        } else {
            Log.d(TAG, "Bind process started");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cwpService.stopUsing();
        if(isBound) {
            try {
                Log.d(TAG, getApplicationContext().toString());
                unbindService(mConnection);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            isBound = false;
        }
        Log.d(TAG, "Stopped using CWPService");
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CWPService.CWPBinder binder = (CWPService.CWPBinder) service;
            cwpService = binder.getService();
            isBound = true;
            Fragment fT = mSectionsPagerAdapter.getItem(0);
            if(fT instanceof TappingFragment && fT != null) {
                ((TappingFragment) fT).setCwpMessaging(cwpService.getMessaging());
            }
            Fragment fC = mSectionsPagerAdapter.getItem(1);
            if(fC instanceof ControlFragment && fC != null) {
                ((ControlFragment) fC).setCwpControl(cwpService.getControl());
            }
            cwpService.startUsing();
            Log.d(TAG, "Binding done - started CWPService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Fragment fT = mSectionsPagerAdapter.getItem(0);
            if(fT instanceof TappingFragment) {
                ((TappingFragment) fT).setCwpMessagingToNull();
            }

            Fragment fC = mSectionsPagerAdapter.getItem(1);
            if(fC instanceof ControlFragment) {
                ((ControlFragment) fC).setCwpControlNull();
            }
        }
    };

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private TappingFragment tappingFragment = null;
        private ControlFragment controlFragment = null;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            if(index == 0) {
                if(tappingFragment == null) {
                    tappingFragment = new TappingFragment();
                }
                return tappingFragment;
            } else if (index == 1) {
                if(controlFragment == null) {
                    controlFragment = new ControlFragment();
                }
                return controlFragment;
            }
            return null;
        }

        @Override
        public int getCount() { return 2; }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_tapping_fragment).toUpperCase();
                case 1:
                    return getString(R.string.title_control_fragment).toUpperCase();
            }
            return null;
        }
    }
}