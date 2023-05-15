package org.kanq;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.kanq.fingerprint.FPJna;
import org.kanq.fingerprint.FPMsg;
import org.kanq.fingerprint.FpCallback;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Demo {

    static FPJna mFPJna = new FPJna();
    static int index = 0;

    static int current = 0;
    static FpCallback callback = new FpCallback() {
        @Override
        public void fpMessageHandler(int enMsgType, FPMsg pMsgData) {
            switch (enMsgType){
                case FPJna.FP_MSG_PRESS_FINGER:
                    System.out.println(">>>Place your finger");
                    break;
                case FPJna.FP_MSG_RISE_FINGER:
                    System.out.println(">>>Lift your finger");
                    break;
                case FPJna.FP_MSG_ENROLL_TIME:
                    index = pMsgData.dwArg1;
                    System.out.println(">>>当前采集次数：" + index);
                    break;
                case FPJna.FP_MSG_CAPTURED_IMAGE:
                    int width = pMsgData.dwArg1;
                    int heigt = pMsgData.dwArg2;
                    Pointer p = pMsgData.pbyImage;
                    byte[] data = p.getByteArray(0, width * heigt);
                    saveBmp("emroll" + index + ".bmp", data, width, heigt);
                    break;
            }
        }
    };

    public static void saveBmp(String path, byte[] data, int width, int height){
        FileOutputStream fos;
        byte[] d = mFPJna.RawToBmpData(data, width, height);
        try {
            fos = new FileOutputStream(path);
            fos.write(d);
            fos.flush();
            fos.close();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }



    public static void main(String[] args) {
        int iRet = FPJna.FP_SUCCESS;
        System.out.println("LoadLibrary");
        mFPJna.LoadLibrary();
        System.out.println("start OpenDevice...");
        iRet = mFPJna.OpenDevice();
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("open error, exit");
            return;
        }
        System.out.println("start InstallMessageHandler...");
        iRet = mFPJna.InstallMessageHandler(callback);
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

        IntByReference pdwFpStatus = new IntByReference();
        IntByReference pdwWidth = new IntByReference();
        IntByReference pdwHeight = new IntByReference();
        byte[] pbyImageData = new byte[FPJna.FP_IMAGE_WIDTH * FPJna.FP_IMAGE_HEIGHT];
        System.out.println("start Detect Finger");
        System.out.println("Place your finger...");

        long currentTM = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTM < 1){
            iRet = mFPJna.DetectFinger(pdwFpStatus);
            if(FPJna.FP_SUCCESS != iRet){
                System.out.println("DetectFinger failed:" + iRet);
                mFPJna.CloseDevice();
                return;
            }
            if(pdwFpStatus.getValue() == FPJna.FP_STATUS_DETECT_YES){
                System.out.println("检测到指纹输入,生成指纹输入...");
                iRet = mFPJna.CaptureImage(pbyImageData, pdwWidth, pdwHeight);
                if(FPJna.FP_SUCCESS != iRet){
                    System.out.println("CaptureImage failed...:" + iRet);
                    mFPJna.CloseDevice();
                    return;
                }
                saveBmp("CaptureImage.bmp", pbyImageData, pdwWidth.getValue(), pdwHeight.getValue());
                System.out.println("CaptureImage Successed...");
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //            System.out.println(pdwFpStatus.getValue());
        }
        if(pdwFpStatus.getValue() == FPJna.FP_STATUS_DETECT_YES){
            System.out.println("finish DetectFinger");
        }else{
            System.out.println("finish DetectFinger Timeout");
        }

        System.out.println("Set Enroll Timeout");
        iRet = mFPJna.SetTimeout(15);
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("settimeout failed:" + iRet);
            mFPJna.CloseDevice();
            return;
        }

        IntByReference refTimeout = new IntByReference();
        iRet =mFPJna.GetTimeout(refTimeout);
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("GetTimeout failed:" + iRet);
            mFPJna.CloseDevice();
            return;
        }

        System.out.println("GetTimeout:" + refTimeout.getValue());

        byte[] pbyFpTemplate = new byte[FPJna.FP_FEATURE_LEN];
        System.out.println("开始指纹录入...");
        mFPJna.SetCollectTimes(1);
        iRet = mFPJna.FpEnroll(pbyFpTemplate);
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("FpEnroll failed:" + iRet);
            mFPJna.CloseDevice();
            return;
        }
        System.out.println("FpEnroll successed");

        int iQuality = mFPJna.GetQuality(pbyFpTemplate);
        System.out.println("GetQuality:" + iQuality);

        System.out.println("wait mathing...");
        mFPJna.SetCollectTimes(1);
        byte[] pByFpData = new byte[FPJna.FP_FEATURE_LEN];
        iRet = mFPJna.FpEnroll(pByFpData);
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("FpEnroll failed:" + iRet);
            mFPJna.CloseDevice();
            return;
        }
        String path = "emroll1.bmp";
        FileInputStream fis = null;
        byte[] d = new byte[FPJna.FP_IMAGE_WIDTH * FPJna.FP_IMAGE_HEIGHT + 1078];
        try {
            fis = new FileInputStream(path);
            int len = fis.read(d);
            System.out.println("read data:" + len);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }finally {
            if(fis != null){
                try {
                    fis.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        byte[] pByInFile = FPJna.BmpToRawData(d, FPJna.FP_IMAGE_WIDTH, FPJna.FP_IMAGE_HEIGHT);
        iRet = mFPJna.MatchTemplate(pByInFile, pByFpData, 3);
        if(FPJna.FP_SUCCESS != iRet){
            System.out.println("MatchTemplate failed:" + iRet);
            mFPJna.CloseDevice();
            return;
        }
        System.out.println("MatchTemplate succeed:" + iRet);
        mFPJna.CloseDevice();
        System.out.println("exit");
    }
}