package com.pixsee.camera;

/**
 * Created by Tudor on 25-Feb-17.
 */

class OpenCameraCommand implements Command {
    private final CameraConfiguration mConfiguration;
    private final int facing;
    CameraWrapper mCameraWrapper;

    public OpenCameraCommand(CameraWrapper cameraWrapper, CameraConfiguration mConfiguration, @CameraFacing int facing) {
        this.mCameraWrapper = cameraWrapper;
        this.mConfiguration = mConfiguration;
        this.facing = facing;
    }

    @Override
    public void execute() {
        if (mCameraWrapper.isOpen()) {
            throw new RuntimeException("Camera is already opened!");
        }
        mConfiguration.setCameraFacing(facing);
        mCameraWrapper.open(mConfiguration.getCameraFacing());
        mConfiguration.configureRotation();
    }
}
