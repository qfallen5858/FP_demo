package org.kanq.fingerprint;

import com.sun.jna.win32.StdCallLibrary;

public interface FpCallback extends StdCallLibrary.StdCallCallback {
    public void FpMessageHandler(int enMsgType, FPMsg pMsgData);
}
