package com.korkkosebastian.cwpclient;

import com.korkkosebastian.cwpclient.cwprotocol.CWPControl;
import com.korkkosebastian.cwpclient.model.CWPMessaging;

public interface CWPProvider {
    CWPMessaging getMessaging();
    CWPControl getControl();
}