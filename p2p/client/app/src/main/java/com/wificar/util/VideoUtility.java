package com.wificar.util;

/* loaded from: classes.dex */
public class VideoUtility {
    public static String getVideoFile(String fileName) {
        if (!fileName.endsWith(".avi")) {
            return String.valueOf(fileName) + ".avi";
        }
        return fileName;
    }
}
