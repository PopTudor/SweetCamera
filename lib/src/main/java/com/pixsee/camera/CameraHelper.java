package com.pixsee.camera;

/**
 * Created by Tudor Pop on 2/1/2017.
 */

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.util.Log;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Camera related utilities.
 */
class CameraHelper {

	/**
	 * Iterate over supported camera video sizes to see which one best fits the
	 * dimensions of the given view while maintaining the aspect ratio. If none can,
	 * be lenient with the aspect ratio.
	 *
	 * @param supportedVideoSizes Supported camera video sizes.
	 * @param previewSizes        Supported camera preview sizes.
	 * @param width                   The width of the view.
	 * @param height                   The height of the view.
	 * @return Best match camera video size to fit in the view.
	 */
	public static Camera.Size getOptimalVideoSize(List<Camera.Size> supportedVideoSizes,
	                                              List<Camera.Size> previewSizes, int width, int height,
	                                              int orientation) {
		// Use a very small tolerance because we want an exact match.
		final double ASPECT_TOLERANCE = 0.1;
		int targetPreviewWidth = isLandscape(orientation) ? width : height;
		int targetPreviewHeight = isLandscape(orientation) ? height : width;


		double targetRatio = (double) targetPreviewWidth / targetPreviewHeight;

		// Supported video sizes list might be null, it means that we are allowed to use the preview
		// sizes
		List<Camera.Size> videoSizes;
		if (supportedVideoSizes != null && !supportedVideoSizes.isEmpty()) {
			videoSizes = supportedVideoSizes;
		} else {
			videoSizes = previewSizes;
		}
		Camera.Size optimalSize = null;

		// Start with max value and refine as we iterate over available video sizes. This is the
		// minimum difference between view and camera height.
		double minDiff = Double.MAX_VALUE;

		// Target view height
		int targetHeight = targetPreviewHeight;// TODO: 2/1/2017 replace targetPreviewHeight with height if something seems off

		// Try to find a video size that matches aspect ratio and the target view size.
		// Iterate over all available sizes and pick the largest size that can fit in the view and
		// still maintain the aspect ratio.
		for (Camera.Size size : videoSizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find video size that matches the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : videoSizes) {
				if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	private static boolean isLandscape(int orientation) {
		return orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	/**
	 * @return the default camera on the device. Throw if there is no camera on the device.
	 */
	public static Camera getDefaultCameraInstance() {
		Camera camera = Camera.open();
		if (camera == null)
			throw new RuntimeException("Could not open the camera");
		return camera;
	}

	/**
	 * @return the default rear/back facing camera on the device. Returns null if camera is not
	 * available.
	 */
	public static Camera getDefaultBackFacingCameraInstance() {
		return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
	}

	/**
	 * @return the default front facing camera on the device. Returns null if camera is not
	 * available.
	 */
	public static Camera getDefaultFrontFacingCameraInstance() {
		return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
	}

	/**
	 * @param position Physical position of the camera i.e Camera.CameraInfo.CAMERA_FACING_FRONT
	 *                 or Camera.CameraInfo.CAMERA_FACING_BACK.
	 * @return the default camera on the device. Returns null if camera is not available.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static Camera getDefaultCamera(int position) {
		// Find the total number of cameras available
		int mNumberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the back-facing ("default") camera
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int i = 0; i < mNumberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == position) {
				return Camera.open(i);
			}
		}
		return null;
	}

	/**
	 * Creates a media file in the {@code Environment.DIRECTORY_PICTURES} directory. The directory
	 * is persistent and available to other applications like gallery.
	 *
	 * @param type Media type. Can be video or image.
	 * @return A file object pointing to the newly created file.
	 */
	public static File getOutputMediaFile(@MediaType int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			throw new RuntimeException("SDCard not mounted or can't access external storage");
		}

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Playsnap");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("Playsnap", "failed to create directory");
				throw new RuntimeException("Can't create file on external storage");
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		} else {
			throw new IllegalArgumentException("Parameter type must be MEDIA_TYPE_IMAGE or MEDIA_TYPE_VIDEO");
		}

		return mediaFile;
	}

	@IntDef({MEDIA_TYPE_IMAGE, MEDIA_TYPE_VIDEO})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MediaType {
	}

}
