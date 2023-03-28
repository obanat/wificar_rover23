package com.wificar.component;

import com.wificar.util.AppLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/* loaded from: classes.dex */
public class AudioData {
    private int audioFormat;
    private long customTimestamp;
    private byte[] data;
    private byte[] encodingData;
    private int index;
    private int sample;
    private int serial;
    private long timestamp;
    private int timetick;

    public AudioData(long timestamp, int serial, int timetick, int audioFormat, byte[] data, int sample, int index) {
        this.serial = 0;
        this.timetick = 0;
        this.timestamp = 0L;
        this.audioFormat = 0;
        this.sample = 0;
        this.index = 0;
        this.customTimestamp = System.currentTimeMillis();
        this.data = data;
        this.timestamp = timestamp;
        this.serial = serial;
        this.timetick = timetick;
        this.audioFormat = audioFormat;
        this.sample = sample;
        this.index = index;
        AppLog.d("wild2", "timestamp:" + timestamp + ",timetick:" + timetick);
    }

    public AudioData(int audioFormat, byte[] data, int sample, int index) {
        this.serial = 0;
        this.timetick = 0;
        this.timestamp = 0L;
        this.audioFormat = 0;
        this.sample = 0;
        this.index = 0;
        this.customTimestamp = System.currentTimeMillis();
        this.data = data;
        this.audioFormat = audioFormat;
        this.sample = sample;
        this.index = index;
    }

    public void setPCMData(byte[] pcmData) {
        this.encodingData = pcmData;
    }

    public byte[] getPCMData() {
        return this.encodingData;
    }

    public byte[] getADPCMData() {
        return this.data;
    }

    public byte[] getADPCMDataWithSample() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(this.sample & CommandEncoder.KEEP_ALIVE);
        baos.write((this.sample & CommandEncoder.KEEP_ALIVE) >> 8);
        baos.write(this.index & CommandEncoder.KEEP_ALIVE);
        baos.write(0);
        baos.write(getADPCMData());
        return baos.toByteArray();
    }

    public byte[] getPCMFromeADPCM() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bDecoded = AudioComponent.decodeADPCMToPCM(getADPCMData(), 160, this.sample, this.index);
        baos.write(bDecoded);
        return baos.toByteArray();
    }

    public long getCustomTimestamp() {
        return this.customTimestamp;
    }

    public void setParaSample(int paraSample) {
        this.sample = paraSample;
    }

    public int getParaIndex() {
        return this.index;
    }

    public void setParaIndex(int paraIndex) {
        this.index = paraIndex;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getTimeTick() {
        return this.timetick;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static AudioData createEmptyPCMData(int length, int index, long timestamp) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = 0;
        }
        byte[] raw = AudioComponent.encodePCMToADPCM(data, length, 0, 0);
        AudioData empty = new AudioData(1, raw, raw[0], index);
        empty.setPCMData(data);
        return empty;
    }

    public int getSerial() {
        return this.serial;
    }
}
