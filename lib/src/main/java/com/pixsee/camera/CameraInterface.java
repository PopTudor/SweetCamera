package com.pixsee.camera;

import android.support.annotation.NonNull;
import android.view.TextureView;

/**
 * Created by Tudor on 09-Feb-17.
 */
interface CameraInterface {
    void open(@CameraFacing int facing);

    void startPreview(@NonNull TextureView preview);

    void stopPreview();

    void close();

    boolean isOpened();
}
