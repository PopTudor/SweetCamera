package com.pixsee.camera;

import android.hardware.Camera;
import android.support.annotation.IntDef;

import static com.pixsee.camera.CameraFacing.BACK;
import static com.pixsee.camera.CameraFacing.FRONT;

/**
 * Created by Tudor Pop on 2/6/2017.
 */

@IntDef({FRONT, BACK})
public @interface CameraFacing {
	int FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
	int BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
}
