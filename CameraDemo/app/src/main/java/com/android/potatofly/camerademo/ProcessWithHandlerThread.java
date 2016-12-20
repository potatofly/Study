package com.android.potatofly.camerademo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * Created by administrator on 16-12-20.
 */
public class ProcessWithHandlerThread extends HandlerThread implements Handler.Callback{
    public static final int WHAT_PROCESS_FRAME = 1;
    public ProcessWithHandlerThread(String name) {
        super(name);
        start();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case WHAT_PROCESS_FRAME:
                byte[] frameDate = (byte[]) msg.obj;
                processFrame(frameDate);
                return true;
            default:
                return false;
        }
    }

    private void processFrame(byte[] frameData){
        Log.e("LG", "test");
    }
}
