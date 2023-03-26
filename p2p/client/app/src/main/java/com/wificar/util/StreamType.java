package com.wificar.util;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: AVIGenerator.java */
/* loaded from: classes.dex */
public enum StreamType {
    STREAM_AUDIO,
    STREAM_VIDEO_C,
    STREAM_VIDEO_B;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static StreamType[] valuesCustom() {
        StreamType[] valuesCustom = values();
        int length = valuesCustom.length;
        StreamType[] streamTypeArr = new StreamType[length];
        System.arraycopy(valuesCustom, 0, streamTypeArr, 0, length);
        return streamTypeArr;
    }
}
