package com.korkkosebastian.cwpclient.cwprotocol;

public interface CWPProtocolListener {
    public enum CWPEvent {EConnected, EDisconnected, EChangedFrequency, ELineup, ELineDown}
    public void onEvent(CWPEvent event, int param);
}
