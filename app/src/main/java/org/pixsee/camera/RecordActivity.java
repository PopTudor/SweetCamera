package org.pixsee.camera;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;

import com.pixsee.camera.ui.AutoFitTextureView;

public class RecordActivity extends BaseActivity implements View.OnClickListener {
    Button start;
    Button stop;
    Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        preview = (AutoFitTextureView) findViewById(R.id.preview);
        start = (Button) findViewById(R.id.startRecording);
        stop = (Button) findViewById(R.id.stopRecording);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        switchCamera = (ImageView) findViewById(R.id.switchImageView);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startRecording:
//                File file = new File("/storage/emulated/0/Pictures/XXX");
//                camera.saveDir(file);
                camera.startRecording();
                startChronometer();
                break;
            case R.id.stopRecording:
                camera.stopRecording();
                chronometer.stop();
                break;
        }
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }
}
