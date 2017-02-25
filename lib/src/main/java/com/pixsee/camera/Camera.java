package com.pixsee.camera;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.TextureView;

import java.io.File;


/**
 * Created by Tudor Pop on 2/1/2017.
 */

final public class Camera implements CameraInterface {
    private static final HandlerThread mHandlerThread = new HandlerThread("openCameraAndVideoRecorder", Thread.MAX_PRIORITY);
    private static final Object sObject = new Object();
    private final Handler mHandler;
    private final CameraConfiguration mConfiguration;
    private final CameraRecorder mCameraRecorder;
    private CameraWrapper mCameraWrapper;
    private TextureView mPreview;

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
        mCameraWrapper = new CameraWrapper(activity, mConfiguration);
        mCameraWrapper.attach(mConfiguration, mCameraRecorder);
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
    synchronized public void open(@CameraFacing final int facing) {
        new OpenCameraCommand(mCameraWrapper, mConfiguration, facing).execute();
    }

    /**
     * Open the camera and openAndStart the preview
     *
     * @param facing  {@link CameraFacing} is front or back
     * @param preview the "sheet" where to display camera preview frames
     */
    public void openAndStart(@CameraFacing final int facing, @NonNull final TextureView preview) {
        if (isOpen()) return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                open(facing);
                startPreview(preview);
            }
        });
    }

    public void open() {
        open(mConfiguration.getCameraFacing());
    }

    @Override
    public void startPreview(@NonNull final TextureView preview) {
        mPreview = preview;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                new StartPreviewCommand(mCameraWrapper, mConfiguration, preview).execute();
            }
        });
    }

    public boolean isFrontFacing() {
        return mConfiguration.isFrontFacing();
    }

    public boolean isBackFacing() {
        return mConfiguration.isBackFacing();
    }


    public void startRecording() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraRecorder.start(mPreview);
            }
        });
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
        close();
        mConfiguration.switchFacing();
        open();
        startPreview(mPreview);
    }

    @Override
    public void close() {
        synchronized (sObject) {
            new CloseCameraCommand(mCameraWrapper, mCameraRecorder, mConfiguration).execute();
        }
    }

    public void takePicture(final ShutterCallback shutterCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraWrapper.takePicture(shutterCallback);
            }
        });
    }

    public void takePicture(final PictureBitmapCallback pictureBitmapCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraWrapper.takePicture(pictureBitmapCallback);
            }
        });
    }

    public void addPreviewCallback(android.hardware.Camera.PreviewCallback callback) {
        mCameraWrapper.previewCallback(callback);
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

}
