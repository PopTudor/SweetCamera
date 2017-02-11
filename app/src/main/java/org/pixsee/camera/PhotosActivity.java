package org.pixsee.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.pixsee.camera.Camera;
import com.pixsee.camera.CameraFacing;
import com.pixsee.camera.ui.AutoFitTextureView;

public class PhotosActivity extends AppCompatActivity implements View.OnClickListener {
    Camera camera;
    AutoFitTextureView preview;
    ImageButton takePhoto;
    ImageView picture;
    ImageView switchCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        camera = new Camera(this);
        takePhoto = (ImageButton) findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(this);
        preview = (AutoFitTextureView) findViewById(R.id.previewSurface);
        picture = (ImageView) findViewById(R.id.picture);
        picture.setOnClickListener(v -> {
            v.setVisibility(View.GONE);
            camera.startPreview(preview);
        });

        switchCamera = (ImageView) findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(v -> {
            switchCameraAction();
        });

        setupBitmaps();
    }

    private void setupBitmaps() {

    }

    private void switchCameraAction() {
        camera.switchCamera();
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

    @Override
    protected void onResume() {
        super.onResume();
        camera.openAndStart(CameraFacing.FRONT, preview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takePhoto:
                camera.takePicture(() -> {

                }, (data, camera1) -> {

                }, (data, camera1) -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    runOnUiThread(() -> {
                        picture.setVisibility(View.VISIBLE);
                        picture.setImageBitmap(bitmap);
                    });
                });

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (picture.getVisibility() == View.VISIBLE) {
            picture.callOnClick();
        } else
            super.onBackPressed();
    }
}
