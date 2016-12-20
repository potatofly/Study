package com.android.potatofly.camerademo;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by administrator on 16-12-20.
 */
public class ProcessWithQueue extends Thread{
    private static final String TAG = "Queue";
    private  LinkedBlockingQueue<byte[]> mQueue;
    public ProcessWithQueue(LinkedBlockingQueue<byte[]> frameQueue) {
        mQueue = frameQueue;
        start();
    }

    public void run() {
        while (true) {
            byte[] frameDate = null;
            try {
                frameDate = mQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            processFrame(frameDate);
        }
    }

    private void processFrame(byte[] frameDate) {
        Log.e("LG", "Queue test");
    }
}
