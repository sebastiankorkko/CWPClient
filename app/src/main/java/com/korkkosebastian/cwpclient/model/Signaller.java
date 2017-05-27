package com.korkkosebastian.cwpclient.model;

import android.media.AudioManager;
import android.media.ToneGenerator;
import java.util.Observable;
import java.util.Observer;

public class Signaller implements Observer {

    private CWPMessaging cwpMessaging;
    private ToneGenerator toneGenerator;

    public Signaller() {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 50);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(cwpMessaging.lineIsUp()) {
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_A, 5000);
        } else {
            toneGenerator.stopTone();
        }
    }

    public void setCwpMessaging(CWPMessaging cwpMessaging) {
        if(cwpMessaging != null) {
            this.cwpMessaging = cwpMessaging;
            this.cwpMessaging.addObserver(this);
        }
    }
}
