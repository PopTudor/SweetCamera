package com.pixsee.camera;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.TextureView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.attr.orientation;

/**
 * Created by Tudor Pop on 2/1/2017.
 */

final public class Camera implements CameraInterface {
    private static final HandlerThread mHandlerThread = new HandlerThread("openCameraAndVideoRecorder", Thread.MAX_PRIORITY);
    private static final Object sObject = new Object();
    private final Handler mHandler;
    private CameraWrapper mCameraWrapper;
    private TextureView mPreview;

    private List<CameraListener> listeners = new ArrayList<>();

    private CameraConfiguration mConfiguration;
    private CameraRecorder mCameraRecorder;

    /**
     * Activity is needed for correct orientation of the camera
     *
     * @param activity that holds the drawing surface of the camera
     */
    public Camera(@NonNull final Activity activity) {
        if (!mHandlerThread.isAlive())
            mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mConfiguration = new CameraConfiguration(activity);
        mCameraRecorder = new CameraRecorder(mConfiguration);
        mCameraWrapper = new CameraWrapper(activity);
        attach(mConfiguration, mCameraRecorder);
    }

    /**
     * Fragment is needed for correct orientation of the camera
     *
     * @param fragment that holds the drawing surface of the camera
     */
    public Camera(@NonNull final Fragment fragment) {
        this(fragment.getActivity());
    }

    @Override
    public void open(@CameraFacing final int facing) {
        if (isOpen()) {
            throw new RuntimeException("Camera is already opened!");
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraWrapper.open(facing);
                notifyListeners(mCameraWrapper.getCamera());
                mConfiguration.setCameraFacing(facing);
                mConfiguration.configurePreviewSize(mPreview, orientation);
                mConfiguration.configureRotation();
            }
        });
    }

    /**
     * Open the camera and openAndStart the preview
     *
     * @param facing  {@link CameraFacing} is front or back
     * @param preview the "sheet" where to display camera preview frames
     */
    public void openAndStart(@CameraFacing final int facing, @NonNull final TextureView preview) {
        if (mCameraWrapper.isOpen()) return;
        open(facing);
        startPreview(preview);
    }

    public void open() {
        open(mConfiguration.getCameraFacing());
    }

    @Override
    public void startPreview(@NonNull final TextureView preview) {
        mPreview = preview;
        startPreview();
    }

    /**
     * Open the camera async
     */
    private void startPreview() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraWrapper.startPreviewSurfaceTexture(mPreview.getSurfaceTexture());
            }
        });
    }

    public void startRecording() {
        mCameraRecorder.start(mPreview);
    }

    /**
     * Save to the given directory. <br>
     * By default it will save recordings/photos to {@code Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}.
     * This way all the media remains on the phone, even after user uninstalls the app. <br>
     * You can also pass null to get the default behavior
     *
     * @param directory where to save the photos/videos
     */
    public void saveDir(@Nullable File directory) {
        if (directory == null)
            return;
        mCameraRecorder.saveDir(directory);
    }

    @Override
    public void stopPreview() {
        mCameraWrapper.stopPreview();
    }

    public void stopRecording() {
        mCameraRecorder.stop();
    }

    public void switchCamera() {
        stopPreview();
        close();
        mConfiguration.switchFacing();
        open();
        startPreview();
    }

    @Override
    public void close() {
        synchronized (sObject) {
            stopPreview();
            if (isRecording()) {
                mCameraRecorder.releaseMediaRecorder();
            }
            mCameraWrapper.close();
        }
    }

    @Override
    public boolean isOpen() {
        return mCameraWrapper.isOpen();
    }

    public boolean isRecording() {
        return mCameraRecorder.isRecording();
    }

    public void setZoom(int zoom) {
        mConfiguration.setZoom(zoom);
    }

    public int getMaxZoom() {
        return mConfiguration.getMaxZoom();
    }

    void attach(@NonNull CameraListener cameraListener) {
        listeners.add(cameraListener);
    }

    void attach(@NonNull CameraListener... cameraListeners) {
        Collections.addAll(listeners, cameraListeners);
    }

    private void notifyListeners(@NonNull android.hardware.Camera mCamera) {
        for (CameraListener listener : listeners) {
            listener.cameraAvailable(mCamera);
        }
    }

    interface CameraListener {
        void cameraAvailable(@NonNull android.hardware.Camera camera);
    }
}
