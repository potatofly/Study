package com.android.potatofly.camerademo;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by administrator on 16-12-20.
 */
public class ProcessWithAsyncTask extends AsyncTask<byte[], Void, String>{
    @Override
    protected String doInBackground(byte[]... params) {
        processFrame(params[0]);
        return "Async Test";
    }

    private void processFrame(byte[] frameData) {
        Log.e("LG", "Async Test");
    }
}
