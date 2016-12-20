package com.android.potatofly.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by administrator on 16-12-15.
 */
public class MainActivity extends Activity{
    FrameLayout preview;
    CameraPreview cameraPreview;
    Button buttonSettings;
    Button button_capture_video;
    ImageView mediaPreview;
    public void startPreview(View view){
        initCamera();
    }

    public void stopPreview(View view){
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
    }

    public void btnSetting(View view){
        getFragmentManager().beginTransaction().replace(R.id.camera_preview,new SettingsFragment()).addToBackStack(null).commit();
    }

    public void btnPhoto(View view){

        cameraPreview.takePicture(mediaPreview);
    }

    public void btnVideo(View view){
        if (cameraPreview.isRecording()){
            cameraPreview.stopRecording(mediaPreview);
            button_capture_video.setText("录像");
        }else if (cameraPreview.startRecording()){
            button_capture_video.setText("停止");
        }
    }

    public void btnPreview(View view){
        Intent intent = new Intent(MainActivity.this, ShowPhotoVideo.class);
        intent.setDataAndType(cameraPreview.getOutputMediaFileUri(),cameraPreview.getOutputMediaFileTypet());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_capture_video = (Button)findViewById(R.id.button_capture_video);
        buttonSettings = (Button)findViewById(R.id.button_settings);
        mediaPreview = (ImageView)findViewById(R.id.media_preview);
        Log.e("LG", "onCreate");
    }

    public void initCamera(){
        Log.e("LG", "initCamera");
        cameraPreview = new CameraPreview(this);
        preview  = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        SettingsFragment.passCamera(cameraPreview.getCameraInstance());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("LG", "onResume");
        /*if (preview == null){
            initCamera();
        }*/
    }
}
