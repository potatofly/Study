package com.android.potatofly.camerademo;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by administrator on 16-12-20.
 */
public class ProcessWithThreadPool {
    private static final int KEEP_ALIVE_TIME = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue;
    private ThreadPoolExecutor mThreadPool;

    public ProcessWithThreadPool() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = corePoolSize * 2;
        workQueue = new LinkedBlockingQueue<>();
        mThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, KEEP_ALIVE_TIME, TIME_UNIT, workQueue);
    }

    public synchronized void post(final byte[] frameData) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                processFrame(frameData);
            }
        });
    }

    private void processFrame(byte[] frameData) {
        Log.e("LG", "ThreadPool Test");
    }
}
