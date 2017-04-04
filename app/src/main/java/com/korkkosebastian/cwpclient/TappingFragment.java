package com.korkkosebastian.cwpclient;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class TappingFragment extends Fragment implements View.OnTouchListener {

    private ImageView buttonImage;

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
        if(event.getAction() == MotionEvent.ACTION_UP) {
            buttonImage.setImageResource(R.mipmap.hal9000_down);
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            buttonImage.setImageResource(R.mipmap.hal9000_up);
            return true;
        }

        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
