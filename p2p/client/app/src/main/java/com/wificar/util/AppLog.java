package com.wificar.util;

import android.util.Log;

/* loaded from: classes.dex */
public class AppLog {
    private static boolean showLog = true;

    public static void enableLogging(boolean enable) {
        showLog = enable;
    }

    public static void i(String tag, String msg) {
        if (showLog) {
            Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (showLog) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        if (showLog) {
            Log.e("test", msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (showLog) {
            Log.e(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        if (showLog) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (showLog) {
            Log.w(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (showLog) {
            Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (showLog) {
            Log.v(tag, msg);
        }
    }
}
