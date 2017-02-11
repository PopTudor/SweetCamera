package org.pixsee.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        findViewById(R.id.simplePreviewButton).setOnClickListener(v -> startActivity(SimplePreview.class));
        findViewById(R.id.simpleTextureView).setOnClickListener(v -> startActivity(SimpleTextureViewListenerActivity.class));
        findViewById(R.id.startStop).setOnClickListener(v -> startActivity(StartStopPreview.class));
        findViewById(R.id.record).setOnClickListener(v -> startActivity(RecordActivity.class));
        findViewById(R.id.photos).setOnClickListener(v -> startActivity(PhotosActivity.class));
    }

    public void startActivity(Class param) {
        Intent intent = new Intent(MainActivity.this, param);
        startActivity(intent);
    }
}
