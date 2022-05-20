package com.hj.android.audio.record;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by hj at 2021/12/26 11:07
 */
public class PcmRecorder {
    public interface PcmListener {
        void onRecordingStarted();

        void onRecordingStopped();

        void onPcm(byte[] pcm, int len);
    }

    private static PcmRecorder sInstance;

    private final PcmListener mPcmListener;

    private AudioRecord mAudioRecord;

    private final int mSampleRate = 16000;

    private final byte[] mAudioBuffer;

    private PcmRecorder(PcmListener listener) {
        mPcmListener = listener;

        int minBufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioBuffer = new byte[minBufferSize];
    }

    public static PcmRecorder createInstance(PcmListener listener) {
        if (sInstance == null) {
            sInstance = new PcmRecorder(listener);
        }

        return sInstance;
    }

    public static PcmRecorder getInstance() {
        return sInstance;
    }

    @SuppressLint("MissingPermission")
    public int startRecording() {
        if (mRecordThread == null) {
            int minBufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    mSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            try {
                mAudioRecord.startRecording();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return -1;
            }

            mRecordThread = new RecordThread();
            mRecordThread.start();
        }

        return 0;
    }

    public void stopRecording() {
        if (mRecordThread != null) {
            mRecordThread.stopRun();

            try {
                mRecordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mRecordThread = null;

            if (mAudioRecord != null) {
                mAudioRecord.stop();
            }
        }
    }

    public void destroy() {
        if (mRecordThread != null) {
            stopRecording();

            mAudioRecord.release();
            mAudioRecord = null;
        }

        sInstance = null;
    }

    private RecordThread mRecordThread;

    private class RecordThread extends Thread {
        private boolean mNeedStop;

        public void stopRun() {
            mNeedStop = true;
        }

        @Override
        public void run() {
            super.run();

            if (mPcmListener != null) {
                mPcmListener.onRecordingStarted();
            }

            while (!mNeedStop) {
                if (mAudioRecord != null) {
                    mAudioRecord.read(mAudioBuffer, 0, mAudioBuffer.length);

                    if (mPcmListener != null) {
                        byte[] copy = new byte[mAudioBuffer.length];
                        System.arraycopy(mAudioBuffer, 0, copy,
                                0, mAudioBuffer.length);

                        mPcmListener.onPcm(copy, copy.length);
                    }
                } else {
                    break;
                }
            }

            if (mPcmListener != null) {
                mPcmListener.onRecordingStopped();
            }
        }
    }
}
