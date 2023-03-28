package com.wificar.util;

import android.content.Context;
import com.rover2.R;

/* loaded from: classes.dex */
public class MessageUtility {
    public static String MESSAGE_ENABLE_MIC = "";
    public static String MESSAGE_DISABLE_MIC = "";
    public static String MESSAGE_ENABLE_GSENSOR = "";
    public static String MESSAGE_DISABLE_GSENSOR = "";
    public static String MESSAGE_PORT_INPUT_ERROR = "";
    public static String MESSAGE_CONNECTION_ERROR = "";
    public static String MESSAGE_TAKE_PHOTO_SUCCESSFULLY = "";
    public static String MESSAGE_TAKE_PHOTO_FAIL = "";
    private static Context ct = null;

    public MessageUtility(Context ct2) {
        ct = ct2;
        MESSAGE_DISABLE_MIC = ct2.getResources().getString(R.string.mic_disable_label);
        MESSAGE_ENABLE_MIC = ct2.getResources().getString(R.string.mic_enable_label);
        MESSAGE_ENABLE_GSENSOR = ct2.getResources().getString(R.string.g_sensor_enable_label);
        MESSAGE_DISABLE_GSENSOR = ct2.getResources().getString(R.string.g_sensor_disable_label);
        MESSAGE_PORT_INPUT_ERROR = ct2.getResources().getString(R.string.port_input_error);
        MESSAGE_CONNECTION_ERROR = ct2.getResources().getString(R.string.connection_error);
        MESSAGE_TAKE_PHOTO_SUCCESSFULLY = ct2.getResources().getString(R.string.take_photo_successfully);
        MESSAGE_TAKE_PHOTO_FAIL = ct2.getResources().getString(R.string.take_photo_fail);
    }
}
