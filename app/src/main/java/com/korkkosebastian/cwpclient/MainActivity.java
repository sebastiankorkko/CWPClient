package com.korkkosebastian.cwpclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.model.CWPMessaging;
import com.korkkosebastian.cwpclient.model.CWPService;

public class MainActivity extends AppCompatActivity implements CWPProvider {

    private CWPService cwpService;
    private boolean isBound = false;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

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
        String activityName = (this.startService(i)).toString();
        if(activityName == null) {
            Toast.makeText(getApplicationContext(),
                    "Service returned null", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cwpService = null;
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
        boolean bindProcessStarted = bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        if(!bindProcessStarted) {
            Toast.makeText(getApplicationContext(),
                    "Bind process didn't start", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Binding started", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isBound) {
            unbindService(mConnection);
        }
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
            getSupportFragmentManager().beginTransaction().detach(fT).commitNowAllowingStateLoss();
            getSupportFragmentManager().beginTransaction().attach(fT).commitAllowingStateLoss();

            Fragment fC = mSectionsPagerAdapter.getItem(1);
            if(fC instanceof ControlFragment && fC != null) {
                ((ControlFragment) fC).setCwpControl(cwpService.getControl());
            }
            getSupportFragmentManager().beginTransaction().detach(fC).commitNowAllowingStateLoss();
            getSupportFragmentManager().beginTransaction().attach(fC).commitAllowingStateLoss();
/*
            Fragment fF = getSupportFragmentManager().findFragmentById(R.id.frequency_fragment);
            if(fF instanceof FrequencyFragment) {
                ((FrequencyFragment) fF).setCwpControl(cwpService.getControl());
            }
            */
            Toast.makeText(getApplicationContext(),
                    "Binding done", Toast.LENGTH_SHORT).show();
            cwpService.startUsing();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            cwpService.stopUsing();
            isBound = false;

            Fragment fT = mSectionsPagerAdapter.getItem(0);
            if(fT instanceof TappingFragment) {
                ((TappingFragment) fT).setCwpMessagingToNull();
            }

            Fragment fC = mSectionsPagerAdapter.getItem(1);
            if(fC instanceof ControlFragment) {
                ((ControlFragment) fC).setCwpControlNull();
            }

/*
            Fragment fF = getSupportFragmentManager().findFragmentById(R.id.frequency_fragment);
            if(fF instanceof FrequencyFragment) {
                ((FrequencyFragment) fF).setCwpControlNull();
            }
        */
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