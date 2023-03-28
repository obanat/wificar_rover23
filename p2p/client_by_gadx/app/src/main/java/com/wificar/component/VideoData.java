package com.wificar.component;

/* loaded from: classes.dex */
public class VideoData {
    private byte[] data;
    private int time;
    private long timestamp;
    private long customTimestamp = System.currentTimeMillis();
    int delay = 0;
    int customDelay = 0;

    public VideoData(long timestamp, int time, byte[] data) {
        this.timestamp = 0L;
        this.time = 0;
        this.data = data;
        this.timestamp = timestamp;
        this.time = time;
    }

    public byte[] getData() {
        return this.data;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getCustomTimestamp() {
        return this.customTimestamp;
    }

    public void setCustomTimestamp(long timestamp) {
        this.customTimestamp = timestamp;
    }

    public void setDelay(int timeInterval) {
        this.delay = timeInterval;
    }

    public int getDelay() {
        return this.delay;
    }

    public void setCustomDelay(int timeInterval) {
        this.customDelay = timeInterval;
    }

    public int getCustomDelay() {
        return this.customDelay;
    }
}
