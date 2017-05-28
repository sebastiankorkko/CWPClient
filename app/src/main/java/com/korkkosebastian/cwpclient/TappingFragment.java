package com.korkkosebastian.cwpclient;

import java.io.IOException;
import java.util.Observer;
import java.util.Observable;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentContainer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.korkkosebastian.cwpclient.model.CWPMessaging;

public class TappingFragment extends Fragment implements View.OnTouchListener, Observer {

    private ImageView buttonImage;
    private CWPMessaging cwpMessaging;

    private static final String TAG = "Tapping Fragment";

    public TappingFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tapping_fragment, container, false);
        buttonImage = (ImageView) view.findViewById(R.id.imageView1);
        buttonImage.setOnTouchListener(this);
        return view;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (cwpMessaging.isConnected()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                try {
                    cwpMessaging.lineUp();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                try {
                    cwpMessaging.lineDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cwpMessaging.deleteObserver(this);
        cwpMessaging = null;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(cwpMessaging.lineIsUp()) {
            buttonImage.setImageResource(R.mipmap.hal9000_up);
        } else if(cwpMessaging.isConnected()) {
            buttonImage.setImageResource(R.mipmap.hal9000_down);
        } else if(!cwpMessaging.isConnected()) {
            buttonImage.setImageResource(R.mipmap.hal9000_offline);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonImage = (ImageView) view.findViewById(R.id.imageView1);
        if(cwpMessaging != null) {
            if (cwpMessaging.lineIsUp()) {
                buttonImage.setImageResource(R.mipmap.hal9000_up);
            } else if (cwpMessaging.isConnected()) {
                buttonImage.setImageResource(R.mipmap.hal9000_down);
            } else if (!cwpMessaging.isConnected()) {
                buttonImage.setImageResource(R.mipmap.hal9000_offline);
            }
        }
    }

    public void setCwpMessaging(CWPMessaging cwpMessaging) {
        if(cwpMessaging != null) {
            this.cwpMessaging = cwpMessaging;
            cwpMessaging.addObserver(this);
        }
    }

    public void setCwpMessagingToNull() {
        this.cwpMessaging = null;
    }
}


