package org.kanq;

import org.kanq.fingerprint.FPJna;
import org.kanq.fingerprint.FPMsg;
import org.kanq.fingerprint.FpCallback;

public class Demo {

    static FPJna mFPJna = new FPJna();
    static int index = 0;
    static FpCallback callback = new FpCallback() {
        @Override
        public void FpMessageHandler(int enMsgType, FPMsg pMsgData) {

        }
    };



    public static void main(String[] args) {
        int iRet = FPJna.FP_SUCCESS;
        System.out.println("LoadLibrary");
        mFPJna.LoadLibrary();
        System.out.println("start OpenDevice...");
        iRet = mFPJna.OpenDevice();
//        System.out.println("finish OpenDevice:" + iRet);
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("open error, exit");
            return;
        }
        System.out.println("start InstallMessageHandler...");
        iRet = mFPJna.InstallMessageHandler(callback);
//        System.out.println("finish InstallMessageHandler:" + iRet);
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("installMessageHandler failed, exit");
            mFPJna.CloseDevice();
            return;
        }

        String devInfo = mFPJna.GetDeviceInfo();
        if(devInfo == null) {
            System.out.println("GetDeviceInfo failed, exit");
            mFPJna.CloseDevice();
            return;
        }
        System.out.println("GetDeviceInfo:" + devInfo);

        String sdkInfo = mFPJna.GetSDKVersion();
        if(sdkInfo == null) {
            System.out.println("GetSDKVersion failed, exit");
            mFPJna.CloseDevice();
            return;
        }
        System.out.println("GetSDKVersion:" + sdkInfo);

    }
}