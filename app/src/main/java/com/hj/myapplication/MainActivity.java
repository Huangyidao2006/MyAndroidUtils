package com.hj.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import com.hj.android.audio.record.PcmRecorder;
import com.hj.android.audio.visualizer.SiriVisualView;
import com.hj.android.log.CatLogger;
import com.hj.android.log.LogLevel;
import com.hj.android.utils.VolumeUtil;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "hjtest";
    private static int REQUEST_CODE_PERMISSION = 1234;

    private SiriVisualView mSiriVisualView;
    private Button mPressToTalkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("SiriVisualView");

        CatLogger.getInstance().setPrintLogLevel(LogLevel.DEBUG);

        initUI();
        requestPermissions();
    }

    private final String[] mPermissions = new String[]{
            Manifest.permission.RECORD_AUDIO
    };

    private void requestPermissions() {
        if (!EasyPermissions.hasPermissions(this, mPermissions)) {
            EasyPermissions.requestPermissions(this, "", REQUEST_CODE_PERMISSION,
                    mPermissions);
        }
    }

    private float mMaxAmplitude = 1.0f;

    private PcmRecorder mPcmRecorder;
    private PcmRecorder.PcmListener mPcmListener = new PcmRecorder.PcmListener() {
        @Override
        public void onRecordingStarted() {
            runOnUiThread(() -> {
                mMaxAmplitude = mSiriVisualView.getMaxAmplitude();
            });
        }

        @Override
        public void onRecordingStopped() {
            runOnUiThread(() -> {
                mSiriVisualView.updateAmplitude(0, false);
            });
        }

        @Override
        public void onPcm(byte[] pcm, int len) {
            int vol = VolumeUtil.computeVolume(pcm, len);
            runOnUiThread(() -> {
                mSiriVisualView.updateAmplitude(vol / 50.0f, true);
            });
        }
    };

    private void initUI() {
        mSiriVisualView = (SiriVisualView) findViewById(R.id.visual_siri);
        mPressToTalkBtn = (Button) findViewById(R.id.btn_press_to_talk);

        mSiriVisualView.setNumOfWaves(3);
        mSiriVisualView.setWaveLineWidth(6.0f, 2.0f);

        mPressToTalkBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (mPcmRecorder == null) {
                            mPcmRecorder = PcmRecorder.createInstance(mPcmListener);
                        }

                        mPcmRecorder.startRecording();
                    }
                    break;

                    case MotionEvent.ACTION_UP: {
                        if (mPcmRecorder != null) {
                            mPcmRecorder.stopRecording();
                            mPcmRecorder.destroy();
                            mPcmRecorder = null;
                        }
                    }
                    break;
                }

                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}