package com.pixsee.camera;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.pixsee.camera.ui.AutoFitTextureView;

import java.util.List;

import static android.content.ContentValues.TAG;
import static com.pixsee.camera.CameraFacing.BACK;
import static com.pixsee.camera.CameraFacing.FRONT;

/**
 * Keeps all the camera configurations
 * Created by Tudor Pop on 2/2/2017.
 */
public final class CameraConfiguration implements CameraWrapper.CameraListener {
    @NonNull
    private final Activity mActivity;
    private Camera mCamera;
    @CameraFacing
    private int cameraFacing = FRONT;
    private int orientation = Configuration.ORIENTATION_PORTRAIT;
    private int rotation;
    private Size mSize;
    private int zoom = -1;

    CameraConfiguration(@NonNull Activity activity) {
        mActivity = activity;
    }

    Camera getCamera() {
        return mCamera;
    }

    void configureRotation() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraFacing, camInfo);
        int cameraRotationOffset = camInfo.orientation;

        int displayRotation;
        if (isFrontFacing()) {
            displayRotation = (cameraRotationOffset + degrees) % 360;
            displayRotation = (360 - displayRotation) % 360; // compensate the mirror
        } else { // back-facing
            displayRotation = (cameraRotationOffset - degrees + 360) % 360;
        }

        Log.v(TAG, "rotation cam / phone = displayRotation: " + cameraRotationOffset + " / " + degrees + " = " + displayRotation);

        if (mCamera == null)
            throw new RuntimeException("Camera configurations must have a camera set when one is available");
        mCamera.setDisplayOrientation(displayRotation);

        if (isFrontFacing()) {
            this.rotation = (360 + cameraRotationOffset + degrees) % 360;
        } else {
            this.rotation = (360 + cameraRotationOffset - degrees) % 360;
        }

        Log.v(TAG, "screenshot rotation: " + cameraRotationOffset + " / " + degrees + " = " + this.rotation);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(this.rotation);
        mCamera.setParameters(parameters);
    }

    public int getRotation() {
        return rotation;
    }

    void configurePreviewSize(@NonNull final TextureView preview) {
        mSize = getOptimalSize(preview);

        if (preview instanceof AutoFitTextureView)
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((AutoFitTextureView) preview).setAspectRatio(mSize);
                }
            });
        if (mCamera == null)
            return;

        Camera.Parameters parameters = mCamera.getParameters();
        // likewise for the camera object itself.
        parameters.setPreviewSize(mSize.getWidth(), mSize.getHeight());
        mCamera.setParameters(parameters);
    }

    Size getOptimalSize(@NonNull TextureView preview) {
        if (mCamera == null)
            return new Size(preview.getWidth(), preview.getHeight());
        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes, mSupportedPreviewSizes, preview.getWidth(), preview.getHeight(), orientation);

        return new Size(optimalSize.width, optimalSize.height);
    }

    public Size getSize() {
        return new Size(mSize.getWidth(), mSize.getHeight());
    }

    /**
     * Set the zoom for the camera. If camera is not open, nothing happens
     *
     * @param zoom
     */
    public void setZoom(int zoom) {
        if (mCamera == null) return;
        Camera.Parameters parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported())
            return;
        if (zoom > parameters.getMaxZoom())
            zoom = parameters.getMaxZoom();
        parameters.setZoom(zoom);
        mCamera.setParameters(parameters);
    }

    public int getMaxZoom() {
        if (mCamera == null || mCamera.getParameters() == null)
            return 100;
        return mCamera.getParameters().getMaxZoom();
    }

    @CameraFacing
    public int getCameraFacing() {
        return cameraFacing;
    }

    public void setCameraFacing(@CameraFacing int mCameraFacing) {
        this.cameraFacing = mCameraFacing;
    }

    void switchFacing() {
        setCameraFacing(cameraFacing == FRONT ? BACK : FRONT);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    @Override
    public void cameraAvailable(@NonNull Camera camera) {
        mCamera = camera;
    }

    public boolean isFrontFacing() {
        return cameraFacing == CameraFacing.FRONT;
    }

    boolean isBackFacing() {
        return cameraFacing == CameraFacing.BACK;
    }

    void setRecordHint(boolean hint) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRecordingHint(hint);
        mCamera.setParameters(parameters);
    }
}
