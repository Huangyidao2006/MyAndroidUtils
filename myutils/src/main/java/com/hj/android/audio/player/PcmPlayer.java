package com.hj.android.audio.player;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

/**
 * Created by hj at 2022/5/20 17:33
 */
public class PcmPlayer {
    public static final int SAMPLE_RATE_16K = 16000;

    public static final int SAMPLE_RATE_44K = 44100;

    public static final int SAMPLE_RATE_48K = 48000;

    private final AudioTrack mAudioTrack;

    private final int mMinBufferSize;

    private int mTotalWriteBytes = 0;

    private PlayListener mPlayListener;

    public interface PlayListener {
        void onCompleted();
    }

    @SuppressLint("Range")
    public PcmPlayer(int sampleRate) {
        mMinBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT);

        AudioAttributes attr = new AudioAttributes.Builder()
                                            .setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
        AudioFormat audioFormat = new AudioFormat.Builder()
                                            .setSampleRate(sampleRate)
                                            .setChannelMask(AudioFormat.CHANNEL_OUT_DEFAULT)
                                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioTrack.Builder builder = new AudioTrack.Builder();
            builder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .setAudioFormat(audioFormat)
                    .setAudioAttributes(attr)
                    .setBufferSizeInBytes(mMinBufferSize * 4)
                    .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE);

            mAudioTrack = builder.build();
        } else {
            mAudioTrack = new AudioTrack(attr, audioFormat, mMinBufferSize * 4,
                    AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
        }
    }

    public PcmPlayer(byte[] allPcm, int sampleRate) {
        AudioAttributes attr = new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
        mMinBufferSize = 0;

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_DEFAULT)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build();
        mAudioTrack = new AudioTrack(attr, audioFormat, allPcm.length,
                AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE);

        mTotalWriteBytes = allPcm.length;
        mAudioTrack.write(allPcm, 0, allPcm.length);
    }

    public void setPlayListener(PlayListener listener) {
        mPlayListener = listener;

        mAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                if (mPlayListener != null) {
                    mPlayListener.onCompleted();
                }
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {

            }
        });
    }

    public int getMinBufferSize() {
        return mMinBufferSize;
    }

    public void play() {
        mAudioTrack.play();
    }

    public int write(byte[] data, int offset, int len) {
        int ret = mAudioTrack.write(data, offset, len);
        if (ret != AudioTrack.ERROR && ret != AudioTrack.ERROR_BAD_VALUE &&
            ret != AudioTrack.ERROR_DEAD_OBJECT && ret != AudioTrack.ERROR_INVALID_OPERATION) {
            mTotalWriteBytes += ret;
            return ret;
        }

        return -1;
    }

    public void setEndFlag() {
        mAudioTrack.setNotificationMarkerPosition(mTotalWriteBytes / 2);
    }

    public void stop() {
        mAudioTrack.pause();
        mAudioTrack.flush();
    }

    public void destroy() {
        stop();
        mAudioTrack.release();
    }
}
