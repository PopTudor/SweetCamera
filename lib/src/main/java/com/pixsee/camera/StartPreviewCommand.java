package com.pixsee.camera;

import android.view.TextureView;

/**
 * Created by Tudor on 25-Feb-17.
 */

class StartPreviewCommand implements Command {
    private CameraWrapper cameraWrapper;
    private CameraConfiguration cameraConfiguration;
    private TextureView preview;

    public StartPreviewCommand(CameraWrapper cameraWrapper, CameraConfiguration cameraConfiguration, TextureView preview) {
        this.cameraWrapper = cameraWrapper;
        this.cameraConfiguration = cameraConfiguration;
        this.preview = preview;
    }

    @Override
    public void execute() {
        cameraConfiguration.configurePreviewSize(preview);
        cameraWrapper.startPreview(preview);
    }
}
