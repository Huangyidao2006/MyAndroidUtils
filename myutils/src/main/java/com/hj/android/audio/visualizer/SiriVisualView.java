package com.hj.android.audio.visualizer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hj at 2022/5/17 21:26
 */
public class SiriVisualView extends View {
    private static final float defaultFrequency = 1.5f;
    private static final float defaultAmplitude = 1.0f;
    private static final float defaultIdleAmplitude = 0.01f;
    private static final int defaultNumberOfWaves = 5;
    private static final float defaultPhaseShift = -0.15f;
    private static final float defaultDensity = 5.0f;
    private static final float defaultPrimaryLineWidth = 3.0f;
    private static final float defaultSecondaryLineWidth = 1.0f;

    private float mPhase;
    private float mAmplitude;
    private float mFrequency;
    private float mIdleAmplitude;
    private int mNumberOfWaves;
    private float mPhaseShift;
    private float mDensity;
    private float mPrimaryWaveLineWidth;
    private float mSecondaryWaveLineWidth;

    private Paint mPaintColor;
    boolean mIsStraightLine = false;

    public SiriVisualView(Context context) {
        super(context);
        init();
    }

    public SiriVisualView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SiriVisualView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SiriVisualView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.mFrequency = defaultFrequency;
        this.mAmplitude = defaultAmplitude;
        this.mIdleAmplitude = defaultIdleAmplitude;
        this.mNumberOfWaves = defaultNumberOfWaves;
        this.mPhaseShift = defaultPhaseShift;
        this.mDensity = defaultDensity;
        this.mPrimaryWaveLineWidth = defaultPrimaryLineWidth;
        this.mSecondaryWaveLineWidth = defaultSecondaryLineWidth;

        mPaintColor = new Paint();
        mPaintColor.setColor(Color.WHITE);
    }

    public void setNumOfWaves(int num) {
        mNumberOfWaves = num;
    }

    public float getMaxAmplitude() {
        return getHeight() / 2.0f - 4.0f;
    }

    public void setDensity(float density) {
        mDensity = density;
    }

    public void setWaveLineWidth(float primaryLine, float secondaryLine) {
        mPrimaryWaveLineWidth = primaryLine;
        mSecondaryWaveLineWidth = secondaryLine;
    }

    public void updateAmplitude(float amplitude, boolean isSpeaking) {
        this.mAmplitude = Math.max(amplitude, mIdleAmplitude);
        mIsStraightLine = isSpeaking;
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        if (mIsStraightLine) {
            for (int i = 0; i < mNumberOfWaves; i++) {
                mPaintColor.setStrokeWidth(i == 0 ? mPrimaryWaveLineWidth : mSecondaryWaveLineWidth);

                float halfHeight = h / 2.0f;
                float mid = w / 2.0f;

                float maxAmplitude = halfHeight - 4.0f;
                float progress = 1.0f - (float) i / this.mNumberOfWaves;
                float normedAmplitude = (1.5f * progress - 0.5f) * this.mAmplitude;
                Path path = new Path();

                float multiplier = Math.min(1.0f, (progress / 3.0f * 2.0f) + (1.0f / 3.0f));

                for (float x = 0; x < w + mDensity; x += mDensity) {
                    // We use a parable to scale the sinus wave, that has its peak in the middle of the view.
                    float scaling = (float) (-Math.pow(1 / mid * (x - mid), 2) + 1);
                    float y = (float) (scaling * maxAmplitude * normedAmplitude * Math.sin(2 * Math.PI * (x / w) * mFrequency + mPhase) + halfHeight);

                    if (x == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }

                mPaintColor.setStyle(Paint.Style.STROKE);
                mPaintColor.setAntiAlias(true);

                canvas.drawPath(path, mPaintColor);
            }
        } else {
//            canvas.drawLine(5, h / 2, w, h / 2, mPaintColor);
//            canvas.drawLine(0, h / 2, w, h / 2, mPaintColor);
//            canvas.drawLine(-5, h / 2, w, h / 2, mPaintColor);
        }

        this.mPhase += mPhaseShift;
        invalidate();
    }
}
