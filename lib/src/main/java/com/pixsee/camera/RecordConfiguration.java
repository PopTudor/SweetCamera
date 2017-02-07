package com.pixsee.camera;

import android.hardware.Camera;
import android.view.TextureView;

import java.util.List;

/**
 * Created by Tudor Pop on 2/2/2017.
 */

class RecordConfiguration extends CameraConfiguration {

	private final CameraRecorder mCameraRecorder;

	public RecordConfiguration(final Camera camera, final CameraRecorder cameraRecorder) {
		mCameraRecorder = cameraRecorder;
	}

	@Override
	public void configurePreviewSize(final TextureView preview, final int orientation) {
		// TODO: 2/1/2017 pass only optimal video size as parameters
		// We need to make sure that our preview and recording video size are supported by the
		// camera. Query camera to find all the sizes and choose the optimal size given the
		// dimensions of our preview surface.
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
		List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
		Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes, mSupportedPreviewSizes,
				preview.getWidth(), preview.getHeight(), Camera.CameraInfo.CAMERA_FACING_FRONT);

		// likewise for the camera object itself.
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		mCamera.setParameters(parameters);

		mCameraRecorder.prepareVideoRecorder(optimalSize);
	}
}
