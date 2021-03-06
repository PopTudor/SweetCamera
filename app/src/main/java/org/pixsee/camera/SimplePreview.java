package org.pixsee.camera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

import com.pixsee.camera.Camera;
import com.pixsee.camera.CameraFacing;

/**
 * The simplest case of opening the camera and showing the preview frames on screen
 */
public class SimplePreview extends AppCompatActivity {
    Camera camera;
    TextureView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_preview);
        camera = new Camera(this);
        preview = (TextureView) findViewById(R.id.preview);
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
}
