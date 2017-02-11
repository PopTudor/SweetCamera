package org.pixsee.camera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;

import com.pixsee.camera.Camera;
import com.pixsee.camera.CameraFacing;
import com.pixsee.camera.ui.AutoFitTextureView;

public class PhotosActivity extends AppCompatActivity implements View.OnClickListener {
    Camera camera;
    TextureView preview;
    ImageButton takePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        takePhoto = (ImageButton) findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(this);
        preview = (AutoFitTextureView) findViewById(R.id.preview);
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


                break;
        }
    }
}
