package com.potatofly.servicebestpractice;

/**
 * Created by administrator on 17-1-20.
 */
public interface DownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
