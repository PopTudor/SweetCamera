package com.pixsee.camera;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

import static android.content.ContentValues.TAG;
import static com.pixsee.camera.CameraFacing.BACK;
import static com.pixsee.camera.CameraFacing.FRONT;

/**
 * Created by Tudor Pop on 2/1/2017.
 */

public class Camera {
	private static final HandlerThread mHandlerThread = new HandlerThread("openCameraAndVideoRecorder", Thread.MAX_PRIORITY);
	private static final Object sObject = new Object();
	/* static because we can have multiple Camera but we record only one camera */
	private static boolean isRecording;
	/* static because we can have multiple Camera but only one camera is open at a time */
	private static boolean isOpened = false;
	private final Handler mHandler;
	private final Activity mActivity;
	private TextureView mPreview;
	private android.hardware.Camera mCamera;
	private CameraRecorder mCameraRecorder;
	private int orientation = Configuration.ORIENTATION_PORTRAIT;
	@CameraFacing
	private int mCameraFacing = FRONT;
	private CameraConfiguration mConfiguration;
	private FeatureChecker featureChecker;

	/**
	 * Activity is needed for correct orientation of the camera
	 *
	 * @param activity that holds the drawing surface of the camera
	 */
	public Camera(final Activity activity) {
		mActivity = activity;
		if (!mHandlerThread.isAlive())
			mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
		featureChecker = new FeatureChecker(mActivity.getPackageManager());
	}

	/**
	 * Fragment is needed for correct orientation of the camera
	 *
	 * @param fragment that holds the drawing surface of the camera
	 */
	public Camera(final Fragment fragment) {
		this(fragment.getActivity());
	}

	public void setCameraFacing(@CameraFacing final int cameraFacing) {
		mCameraFacing = cameraFacing;
	}

	public void open() {
		if (isOpened()) {
			throw new RuntimeException("Camera is already opened!");
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mCamera = getCamera(mCameraFacing);
			}
		});
	}

	public void startPreview(final TextureView preview) {
		mPreview = preview;
		startPreview();
	}

	public boolean isOpened() {
		return isOpened;
	}

	private void setOpened(final boolean opened) {
		isOpened = opened;
	}

	public void setZoom(int zoom) {
		mConfiguration.setZoom(zoom);
	}

	/**
	 * Open the camera async
	 */
	private void startPreview() {
		mConfiguration = new CameraConfiguration(mCamera);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (isRecording) {
					mCameraRecorder = new CameraRecorder(mCamera, mPreview);
					mConfiguration = new RecordConfiguration(mCamera, mCameraRecorder);
				} else {
					mConfiguration = new CameraConfiguration(mCamera);
				}
				mConfiguration.setZoom(0);
				mConfiguration.configurePreviewSize(mPreview, orientation);
				mConfiguration.configureRotation(mCameraFacing, mActivity);
				startPreview(mPreview.getSurfaceTexture());
			}
		});
	}

	public int getMaxZoom() {
		return mConfiguration.getMaxZoom();
	}

	private void startPreview(@NonNull final SurfaceTexture previewTexture) {
		try {
			// Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
			// with {@link SurfaceView}
			mCamera.setPreviewTexture(previewTexture);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
		}
	}

	public void switchCamera() {
		stopPreview();
		close();
		setCameraFacing(mCameraFacing == FRONT ? BACK : FRONT);
		open();
		startPreview();
	}

	public boolean isRecording() {
		return isRecording;
	}

	public void setRecording(final boolean recording) {
		isRecording = recording;
	}

	public void stopPreview() {
		mCamera.stopPreview();
	}

	public void close() {
		synchronized (sObject) {
			if (isRecording()) {
				mCameraRecorder.releaseMediaRecorder();
			}
			if (mCamera != null) {
				// release the camera for other applications
				mCamera.release();
				mCamera = null;
				setOpened(false);
			}
		}
	}

	private android.hardware.Camera getCamera(int camera) throws IllegalArgumentException {
		if (!featureChecker.hasCamera(camera))
			throw new IllegalArgumentException("Specified camera [i] does not exist!");
		// from here on we consider the camera as open and any subsequent request will throw Camera already open exception
		synchronized (sObject) {
			setOpened(true);
			// BEGIN_INCLUDE (configure_preview)
			switch (camera) {
				case FRONT:
					return CameraHelper.getDefaultFrontFacingCameraInstance();
				case BACK:
					return CameraHelper.getDefaultBackFacingCameraInstance();
				default:
					return CameraHelper.getDefaultCameraInstance();
			}
		}
	}
}
