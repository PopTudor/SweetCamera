package com.pixsee.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;

import com.pixsee.camera.exception.CameraNotOpenException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.pixsee.camera.CameraFacing.BACK;
import static com.pixsee.camera.CameraFacing.FRONT;

/**
 * Created by Tudor on 09-Feb-17.
 */

public final class CameraWrapper implements CameraInterface {
    private static final Object lock = new Object();
    private static boolean isOpened = false;
    private final List<CameraListener> listeners = new ArrayList<>();
    private final Activity activity;
    private android.hardware.Camera mCamera;
    private FeatureChecker featureChecker;
    private byte[] mBuffer;
    private CameraConfiguration cameraConfiguration;
    private Camera.PreviewCallback callback;
    private ShutterCallback shutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };
    private PictureBitmapCallback pictureBitmapCallback;
    private Camera.ShutterCallback shutterCallbackCamera = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            shutterCallback.onShutter();
        }
    };
    private boolean mirrorPicture;
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (pictureBitmapCallback != null) {
                final Bitmap bitmap = getBitmapFromByteArray(data);
                final Bitmap fliped = flip(bitmap);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pictureBitmapCallback.onPicture(fliped);
                    }
                });
            }
        }
    };

    public CameraWrapper(Activity activity, CameraConfiguration cameraConfiguration) {
        featureChecker = new FeatureChecker(activity.getPackageManager());
        this.activity = activity;
        this.cameraConfiguration = cameraConfiguration;
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
            notifyListeners(mCamera);
        }
    }

    @Override
    public void startPreview(@NonNull TextureView preview) {
        startPreviewSurfaceTexture(preview.getSurfaceTexture());
        addCallbacks();
    }

    @Override
    public void stopPreview() {
        if (mCamera == null) // camera is not even initialised
            return;
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
        if (mCamera == null)
            throw new CameraNotOpenException("Camera not opened! To start preview, a camera must be open");
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(previewTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
        }
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
                if (callback != null)
                    callback.onPreviewFrame(data, camera);
                if (mCamera != null)
                    mCamera.addCallbackBuffer(mBuffer);
            }
        });
    }

    private void setupBuffer() {
        Size confSize = cameraConfiguration.getSize();
        int size = confSize.getWidth() * confSize.getHeight();
        size = size * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8;
        if (mBuffer != null && size == mBuffer.length)
            return;
        mBuffer = new byte[size];
    }

    public void takePicture(ShutterCallback shutterCallback) {
        this.shutterCallback = shutterCallback;
        if (mCamera == null)
            return;
        mCamera.takePicture(shutterCallbackCamera, null, pictureCallback);
    }

    public void takePicture(PictureBitmapCallback pictureBitmapCallback) {
        this.pictureBitmapCallback = pictureBitmapCallback;
        if (mCamera == null)
            return;
        mCamera.takePicture(shutterCallbackCamera, null, pictureCallback);
    }

    private Bitmap flip(Bitmap src) {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    private Bitmap getBitmapFromByteArray(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    void attach(@NonNull CameraWrapper.CameraListener... cameraListeners) {
        Collections.addAll(listeners, cameraListeners);
    }

    private void notifyListeners(@NonNull android.hardware.Camera mCamera) {
        for (CameraWrapper.CameraListener listener : listeners) {
            listener.cameraAvailable(mCamera);
        }
    }

    interface CameraListener {
        void cameraAvailable(@NonNull Camera camera);
    }

}
