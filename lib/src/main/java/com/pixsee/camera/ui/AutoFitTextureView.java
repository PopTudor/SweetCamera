package com.pixsee.camera.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.pixsee.camera.Size;

import static android.content.ContentValues.TAG;

/**
 * http://stackoverflow.com/questions/19577299/android-camera-preview-stretched/22758359#22758359
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitTextureView extends TextureView {

    private Size mPreviewSize;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param previewSize holds the optimised width/height of the preview
     */
    public void setAspectRatio(@NonNull final Size previewSize) {
        if (previewSize.getWidth() < 0 || previewSize.getHeight() < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mPreviewSize = previewSize;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mPreviewSize == null) {
            setMeasuredDimension(width, height);
            return;
        }

        float ratio;
        if (mPreviewSize.getHeight() >= mPreviewSize.getWidth())
            ratio = (float) mPreviewSize.getHeight() / (float) mPreviewSize.getWidth();
        else
            ratio = (float) mPreviewSize.getWidth() / (float) mPreviewSize.getHeight();

        float camHeight = (int) (width * ratio);
        float newCamHeight;
        float newHeightRatio;

        if (camHeight < height) {
            newHeightRatio = (float) height / (float) mPreviewSize.getHeight();
            newCamHeight = (newHeightRatio * camHeight);
            Log.e(TAG, camHeight + " " + height + " " + mPreviewSize.getHeight() + " " + newHeightRatio + " " + newCamHeight);
            setMeasuredDimension((int) (width * newHeightRatio), (int) newCamHeight);
            Log.e(TAG, mPreviewSize.getWidth() + " | " + mPreviewSize.getHeight() + " | ratio - " + ratio + " | H_ratio - " + newHeightRatio + " | A_width - " + (width * newHeightRatio) + " | A_height - " + newCamHeight);
        } else {
            newCamHeight = camHeight;
            setMeasuredDimension(width, (int) newCamHeight);
            Log.e(TAG, mPreviewSize.getWidth() + " | " + mPreviewSize.getHeight() + " | ratio - " + ratio + " | A_width - " + (width) + " | A_height - " + newCamHeight);
        }
    }

}