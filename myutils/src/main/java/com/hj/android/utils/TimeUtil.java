package com.hj.android.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hj at 2022/4/10 23:07
 */
public class TimeUtil {
    public static String toTimeString(long timeMs) {
        return toTimeString(timeMs, "yyyy-MM-dd HH:mm:ss");
    }

    public static String toTimeString(long timeMs, String fmt) {
        return new SimpleDateFormat(fmt, Locale.CHINA)
                .format(new Date(timeMs));
    }
}
