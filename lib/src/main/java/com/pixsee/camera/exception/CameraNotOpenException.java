package com.pixsee.camera.exception;

/**
 * Created by Tudor on 25-Feb-17.
 */

public class CameraNotOpenException extends RuntimeException {
    public CameraNotOpenException() {
        super("Camera is closed! You must open it first. Try camera.open()");
    }

    public CameraNotOpenException(String message) {
        super(message);
    }
}
