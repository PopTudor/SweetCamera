package org.pixsee.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.pixsee.camera.Camera;
import com.pixsee.camera.CameraFacing;
import com.pixsee.camera.ui.AutoFitTextureView;

public class BaseActivity extends AppCompatActivity {
    Camera camera;
    AutoFitTextureView preview;
    ImageView switchCamera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        camera = new Camera(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (switchCamera != null) {
            switchCamera.setOnClickListener(v -> switchCameraAction());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.openAndStart(CameraFacing.FRONT, preview);
        // or
//        camera.open();
//        camera.startPreview(preview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
        // or
//        camera.stopPreview();
//        camera.close();
    }

    protected void switchCameraAction() {
        camera.switchCamera();
        if (switchCamera == null)
            return;
        if (camera.isFrontFacing())
            switchCamera.animate().rotationYBy(90).alpha(0).withEndAction(() -> {
                switchCamera.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_rear_black_24dp));
                switchCamera.animate().rotationYBy(90).alphaBy(1).start();
            });
        else {
            switchCamera.animate().rotationYBy(90).alpha(0).withEndAction(() -> {
                switchCamera.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_front_black_24dp));
                switchCamera.animate().rotationYBy(90).alphaBy(1).start();
            });
        }
    }
}
