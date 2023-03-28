package com.wificar.mediaplayer;

/* loaded from: classes.dex */
public class JNIWificarVideoPlay {
    public static native byte[] audioRender(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3);

    public static native int getCurrentTime();

    public static native int getDuration();

    public static native int getVideoHeight();

    public static native byte[] getVideoSnapshot(String str);

    public static native int getVideoWidth();

    public static native int playerInit(String str);

    public static native int playerIsStop();

    public static native int playerPause();

    public static native int playerResume();

    public static native int playerSeek(int i);

    public static native int playerStart();

    public static native int playerStop();

    public static native byte[] videoRender();

    static {
        System.loadLibrary("decoder");
        System.loadLibrary("videodecoder");
    }
}
