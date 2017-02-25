package com.pixsee.camera;

/**
 * Created by Tudor on 25-Feb-17.
 */

final class CloseCameraCommand implements Command {
    private CameraWrapper cameraWrapper;
    private CameraRecorder cameraRecorder;
    private CameraConfiguration cameraConfiguration;

    public CloseCameraCommand(CameraWrapper cameraWrapper, CameraRecorder cameraRecorder, CameraConfiguration cameraConfiguration) {
        this.cameraWrapper = cameraWrapper;
        this.cameraRecorder = cameraRecorder;
        this.cameraConfiguration = cameraConfiguration;
    }

    @Override
    public void execute() {
        cameraWrapper.stopPreview();
        if (cameraRecorder.isRecording()) {
            cameraRecorder.releaseMediaRecorder();
        }
        cameraWrapper.close();
    }
}
