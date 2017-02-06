package com.pixsee.camera;

import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Created by Tudor Pop on 2/3/2017.
 */

class FeatureChecker {
	private final PackageManager mPackageManager;

	FeatureChecker(final PackageManager packageManager) {
		mPackageManager = packageManager;
	}

	boolean hasCamera(final int camera) {
		switch (camera) {
			case Camera.CameraInfo.CAMERA_FACING_FRONT:
				if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
					return true;
			case Camera.CameraInfo.CAMERA_FACING_BACK:
				if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA))
					return true;
			default:
				return false;
		}
	}

	public boolean hasFlash() {
		return mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	}

	public boolean hasAutoFlash() {
		return mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
	}
}
