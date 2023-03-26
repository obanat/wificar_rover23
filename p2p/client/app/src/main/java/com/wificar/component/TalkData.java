package com.wificar.component;

/* loaded from: classes.dex */
public class TalkData {
    private int audioFormat;
    private byte[] data;
    private int index;
    private int sample;
    private int serial;
    private int ticktime;
    private int timestamp;

    public TalkData(byte[] data, int sample, int index) {
        this.timestamp = 0;
        this.serial = 0;
        this.ticktime = 0;
        this.audioFormat = 0;
        this.sample = 0;
        this.index = 0;
        this.data = data;
        this.sample = sample;
        this.index = index;
    }

    public TalkData(int ticktime, int serial, int timestamp, int audioFormat, byte[] data, int sample, int index) {
        this.timestamp = 0;
        this.serial = 0;
        this.ticktime = 0;
        this.audioFormat = 0;
        this.sample = 0;
        this.index = 0;
        this.data = data;
        this.timestamp = timestamp;
        this.serial = serial;
        this.ticktime = ticktime;
        this.audioFormat = audioFormat;
        this.sample = sample;
        this.index = index;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public int getSerial() {
        return this.serial;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setParaSample(int paraSample) {
        this.sample = paraSample;
    }

    public int getParaIndex() {
        return this.index;
    }

    public int getParaSample() {
        return this.sample;
    }

    public void setParaIndex(int paraIndex) {
        this.index = paraIndex;
    }

    public int getTicktime() {
        return this.ticktime;
    }

    public void setTicktime(int ticktime) {
        this.ticktime = ticktime;
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
