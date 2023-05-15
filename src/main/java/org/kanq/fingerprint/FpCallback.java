package org.kanq.fingerprint;

import com.sun.jna.win32.StdCallLibrary;

public interface FpCallback extends StdCallLibrary.StdCallCallback {
    public void fpMessageHandler(int enMsgType, FPMsg pMsgData);
}
