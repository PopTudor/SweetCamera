package org.pixsee.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.pixsee.camera.ui.AutoFitTextureView;

public class PhotosActivity extends BaseActivity implements View.OnClickListener {
    ImageButton takePhoto;
    ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

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
            switchCameraAction(); // BaseActivity, switch camera+animation
        });
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
