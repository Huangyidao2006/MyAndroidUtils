package com.hj.android.utils;

/**
 * Created by hj at 2022/6/2 10:17
 */
public class DataUtil {
    public static short[] pcmToShort(byte[] data, int len) {
        int count = len / 2;
        short[] buffer = new short[count];
        int j = 0;

        for (int i = 0; i < len; i += 2) {
            buffer[j++] = (short) ((data[i] & 0xff) | ((data[i + 1] & 0xff) << 8));
        }

        return buffer;
    }

    public static double[] pcmNormalize(short[] data, int len) {
        double[] buffer = new double[len];
        for (int i = 0; i < len; i++) {
            buffer[i] = data[i] / 32768.0;
        }

        return buffer;
    }

    public static byte[] pcmToBytes(short[] data, int len) {
        byte[] buffer = new byte[len * 2];
        int j = 0;
        for (int i = 0; i < len; i++) {
            buffer[j] = (byte) (data[i] & 0xff);
            buffer[j + 1] = (byte) ((data[i] >> 8) & 0xff);
            j += 2;
        }

        return buffer;
    }
}
