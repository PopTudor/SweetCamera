package org.pixsee.camera;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import com.pixsee.camera.Camera;
import com.pixsee.camera.CameraFacing;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {
    Camera camera;
    TextureView preview;
    Button start;
    Button stop;
    Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        camera = new Camera(this);
        preview = (TextureView) findViewById(R.id.preview);
        start = (Button) findViewById(R.id.startRecording);
        stop = (Button) findViewById(R.id.stopRecording);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
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
            case R.id.startRecording:
                camera.startRecording();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                break;
            case R.id.stopRecording:
                camera.stopRecording();
                chronometer.stop();
                break;
        }
    }
}
