package com.wificar.util;

import android.content.Context;
import android.content.SharedPreferences;

/* loaded from: classes.dex */
public class WificarUtility {
    public static final String ACCOUNT_HOST = "_ACCOUNT_HOST";
    public static final String ACCOUNT_ID = "_ACCOUNT_ID";
    public static final String ACCOUNT_PASSWORD = "_ACCOUNT_PASSWORD";
    public static final String ACCOUNT_PORT = "_ACCOUNT_PORT";
    public static final String CONTROLLER_TYPE = "_CONTROLLER_TYPE";
    private static final String IR_PROPERTY = "_IR_PROPERTY";
    private static final String SOUND_PROPERTY = "_SOUND_PROPERTY";
    public static final String VIDEO_FOLDER = "_VIDEO_FOLDER";
    public static final String WIFICAR_IR = "_WIFICAR_IR";
    public static final String WIFICAR_MIC = "_WIFICAR_MIC";
    private static String appName = "";

    public static int[] getRandamNumber() {
        int[] val = {-402456576, -804847616, 3000, 4000};
        return val;
    }

    public static String getAppName() {
        return appName;
    }

    public static void putStringVariable(Context context, String variableName, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(getAppName(), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(getAppName()) + variableName, value);
        editor.commit();
    }

    public static String getStringVariable(Context context, String variableName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(getAppName(), 0);
        return sharedPreferences.getString(String.valueOf(getAppName()) + variableName, defaultValue);
    }

    public static int getTimeFrom1970() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static void putIntVariable(Context context, String variableName, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(getAppName(), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(String.valueOf(getAppName()) + variableName, value);
        editor.commit();
    }

    public static int getIntVariable(Context context, String variableName, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(getAppName(), 0);
        return sharedPreferences.getInt(String.valueOf(getAppName()) + variableName, defaultValue);
    }
}
