package com.pixsee.camera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

import static android.content.ContentValues.TAG;
import static com.pixsee.camera.CameraFacing.BACK;
import static com.pixsee.camera.CameraFacing.FRONT;

/**
 * Created by Tudor on 09-Feb-17.
 */

final class CameraWrapper implements CameraInterface {
    private static final Object lock = new Object();
    private static boolean isOpened = false;
    private android.hardware.Camera mCamera;
    private FeatureChecker featureChecker;
    private byte[] mBuffer;
    private Camera.PreviewCallback callback;
    private Size size;

    public CameraWrapper(Activity activity) {
        featureChecker = new FeatureChecker(activity.getPackageManager());

    }

    @Override
    public void open(@CameraFacing int facing) {
        if (!featureChecker.hasCamera(facing))
            throw new IllegalArgumentException("Specified camera [i] does not exist!");
        // from here on we consider the camera as open and any subsequent request will throw Camera already open exception
        synchronized (lock) {
            isOpened = true;
            // BEGIN_INCLUDE (configure_preview)
            switch (facing) {
                case FRONT:
                    mCamera = CameraHelper.getDefaultFrontFacingCameraInstance();
                    break;
                case BACK:
                    mCamera = CameraHelper.getDefaultBackFacingCameraInstance();
                    break;
                default:
                    mCamera = CameraHelper.getDefaultCameraInstance();
            }
        }
    }


    @Override
    public void startPreview(@NonNull TextureView preview) {
        size = new Size(preview.getWidth(), preview.getHeight());
        startPreviewSurfaceTexture(preview.getSurfaceTexture());
        addCallbacks();
    }

    @Override
    public void stopPreview() {
        mCamera.stopPreview();
    }

    @Override
    public void close() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
            isOpened = false;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpened;
    }

    private void startPreviewSurfaceTexture(@NonNull final SurfaceTexture previewTexture) {
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(previewTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
        }
    }

    android.hardware.Camera getCamera() {
        return mCamera;
    }

    public void previewCallback(final Camera.PreviewCallback callback) {
        this.callback = callback;
    }

    private void addCallbacks() {
        setupBuffer();
        mCamera.addCallbackBuffer(mBuffer);
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                callback.onPreviewFrame(data, camera);
                if (mCamera != null)
                    mCamera.addCallbackBuffer(mBuffer);
            }
        });
    }

    private void setupBuffer() {
        int size = this.size.getWidth() * this.size.getHeight();
        size = size * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8;
        if (mBuffer != null && size == mBuffer.length)
            return;
        mBuffer = new byte[size];
    }
}
