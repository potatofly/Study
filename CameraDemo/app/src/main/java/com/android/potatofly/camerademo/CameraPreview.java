package com.android.potatofly.camerademo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public Uri outputMediaFileUri;
    private String outputMediaFileType;
    private MediaRecorder mediaRecorder;
    private float oldDist = 1f;

    private static final int PROCESS_WITH_HANDLER_THREAD = 1;
    private static final int PROCESS_WITH_QUEUE = 2;
    private static final int PROCESS_WITH_ASYNC_TASK = 3;
    private static final int PROCESS_WITH_THREAD_POOL = 4;

    private ProcessWithThreadPool processFrameThreadPool;
    private ProcessWithQueue processframeQueue;
    private LinkedBlockingQueue<byte[]> frameQueue;
    //private int processType = PROCESS_WITH_HANDLER_THREAD;
    //private int processType = PROCESS_WITH_QUEUE;
    //private int processType = PROCESS_WITH_ASYNC_TASK;
    private int processType = PROCESS_WITH_THREAD_POOL;
    private ProcessWithHandlerThread processFrameHandlerThread;
    private Handler processFrameHandler;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        switch (processType) {
            case PROCESS_WITH_HANDLER_THREAD:
                Log.e("LG", "CameraPreview");
                processFrameHandlerThread = new ProcessWithHandlerThread("process frame");
                processFrameHandler = new Handler(processFrameHandlerThread.getLooper(), processFrameHandlerThread);
                break;
            case PROCESS_WITH_QUEUE:
                frameQueue = new LinkedBlockingQueue<>();
                processframeQueue = new ProcessWithQueue(frameQueue);
                break;
            case PROCESS_WITH_THREAD_POOL:
                processFrameThreadPool = new ProcessWithThreadPool();
                break;
        }
    }

    public Camera getCameraInstance() {
       if(mCamera == null) {
           try {
               CameraHandlerThread mThread = new CameraHandlerThread("camera thread");
               synchronized (mThread) {
                   mThread.openCamera();
               }
           } catch (Exception e) {
               Log.e(TAG, "camera is not available");
           }
       }
        return mCamera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        mCamera.setPreviewCallback(this);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Camera.Parameters parameters = mCamera.getParameters();
        int rotation = getDisplayOrientation();
        parameters.setRotation(rotation);
        mCamera.setDisplayOrientation(rotation);
        mCamera.setParameters(parameters);
        //adjustDisplayRatio(rotation);
    }

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),TAG);
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d(TAG,"failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type ==MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        }else if (type == MEDIA_TYPE_VIDEO){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
            outputMediaFileType = "video/*";
        }else{
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);

        return mediaFile;
    }

    public Uri getOutputMediaFileUri(){
        return outputMediaFileUri;
    }

    public String getOutputMediaFileTypet(){
        return outputMediaFileType;
    }

    public void takePicture(final ImageView imageView){
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.e(TAG, "error creating media file, check storage permission");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    //imageView.setImageURI(outputMediaFileUri);
                    camera.startPreview();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found:" + e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, "Error accessing file:" + e.getMessage());
                }
            }
        });
    }

    public boolean startRecording(){
        if (prepareVideoRecorder()){
            mediaRecorder.start();
            return true;
        }else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording(final ImageView imageView){
        if (mediaRecorder != null){
            mediaRecorder.stop();
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            imageView.setImageBitmap(thumbnail);
        }
        releaseMediaRecorder();
    }

    public boolean isRecording(){
        return mediaRecorder != null;
    }

    private boolean prepareVideoRecorder(){

        mCamera = getCameraInstance();
        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefVideoSize = prefs.getString("video_size", "");
        String[] split = prefVideoSize.split("x");
        mediaRecorder.setVideoSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mediaRecorder.setPreviewDisplay(mHolder.getSurface());
        int rotation = getDisplayOrientation();
        mediaRecorder.setOrientationHint(rotation);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    private void releaseMediaRecorder() {
        if (mediaRecorder != null){
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            mCamera.lock();
        }
    }

    public int getDisplayOrientation(){
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case  Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK,cameraInfo);
        int result = (cameraInfo.orientation - degrees + 360) % 360;
        return result;
    }

    private void adjustDisplayRatio(int rotation){
        ViewGroup parent = (ViewGroup) getParent();
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270){
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth){
            final int scaledChildWidth = previewWidth * height / previewHeight;
            layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
        }

    }

    private static Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSIze){
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSIze.width - 1000);
        int centerY = (int) (y / previewSIze.height - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max){
        if (x > max){
            return max;
        }
        if (x < min){
            return min;
        }
        return x;
    }

    private static void handleFocus(MotionEvent event, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        Camera.Size previewSize = params.getPreviewSize();
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, previewSize);
        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, previewSize);

        camera.cancelAutoFocus();

        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.e(TAG, "focus areas not supported");
        }

        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.e(TAG, "metering areas not supported");
        }

        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocus(event, mCamera);
        }else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }

        return true;
    }

    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.e(TAG, "zoom not supported");
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        switch (processType) {
            case PROCESS_WITH_HANDLER_THREAD:
                Log.e("LG", "onPreviewFrame");
                processFrameHandler.obtainMessage(ProcessWithHandlerThread.WHAT_PROCESS_FRAME, data).sendToTarget();
                break;
            case PROCESS_WITH_QUEUE:
                try {
                    frameQueue.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case PROCESS_WITH_ASYNC_TASK:
                new ProcessWithAsyncTask().execute(data);
                break;
            case PROCESS_WITH_THREAD_POOL:
                processFrameThreadPool.post(data);
                break;
        }
    }

    private class CameraHandlerThread extends HandlerThread{
        Handler mHandler;

        public CameraHandlerThread(String name) {
            super(name);
            start();
            mHandler = new Handler(getLooper());
        }
        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    openCamraOriginal();
                    notifyCameraOpened();
                }
            });

            try {
                wait();
            } catch (InterruptedException e) {
                Log.e("LG", "wait was interrupted");
            }
        }
    }

    private void openCamraOriginal(){
        try {
            mCamera = Camera.open();
            Log.e("LG", "camera is open");
        } catch (Exception e) {
            Log.e("LG", "camera is not available");
        }
    }


}
