package org.pixsee.camera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.pixsee.camera.Camera;
import com.pixsee.camera.CameraFacing;

public class StartStopPreview extends AppCompatActivity implements View.OnClickListener {
    Camera camera;
    TextureView preview;

    Button start;
    Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_stop_preview);
        camera = new Camera(this);
        preview = (TextureView) findViewById(R.id.preview);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start: {
                camera.startPreview(preview);
            }
            break;
            case R.id.stop: {
                camera.stopPreview();
            }
            break;
        }
    }
}
