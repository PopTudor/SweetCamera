package com.pixsee.camera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by Tudor Pop on 2/1/2017.
 */

class CameraRecorder implements com.pixsee.camera.Camera.CameraListener {
    private final CameraConfiguration configuration;
    private Camera mCamera;
    private File mOutputFile;
    private MediaRecorder mMediaRecorder;
    /* static because we can have multiple Camera but we record only one camera */
    private boolean isRecording;
    private boolean isPrepared;


    public CameraRecorder(final CameraConfiguration configuration) {
        this.configuration = configuration;
    }

    public void prepareVideoRecorder(@NonNull final TextureView preview) {
        Camera.Size optimalSize = configuration.getOptimalSize(preview);
        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setOrientationHint(configuration.getRotation());

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
            isPrepared = true;
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        }
    }

    public void start(final TextureView preview) {
        if (isRecording) return;
        prepareVideoRecorder(preview);
        if (isPrepared) {
            mMediaRecorder.start();
            isRecording = true;
        } else
            releaseMediaRecorder();
    }

    public void stop() {
        mMediaRecorder.stop();
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            isPrepared = false;
            stop();
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            try {
                mCamera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void cameraAvailable(Camera camera) {
        mCamera = camera;
    }
}
