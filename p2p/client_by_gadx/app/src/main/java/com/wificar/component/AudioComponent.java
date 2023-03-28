package com.wificar.component;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import com.wificar.util.ByteUtility;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class AudioComponent {
    private AudioRecord audioRecord;
    private AudioTrack track;
    AudioTrack trackPlayer;
    private WifiCar wificar;
    private static int[] stepTable = {7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};
    private static int[] indexAdjust = {-1, -1, -1, -1, 2, 4, 6, 8};
    private int sampleRateInHz = 8000;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private int bufferSize = 0;
    private boolean isRecording = false;
    private Thread recordThread = null;
    private Thread sendThread = null;
    private ArrayList audioDataList = new ArrayList();
    private final Object mutex = new Object();
    int audioTrackBufferSize = 0;
    Thread playThread = null;
    private boolean isPlaying = false;

    public AudioComponent(WifiCar wificar) {
        this.wificar = null;
        this.trackPlayer = null;
        this.wificar = wificar;
        initialPlayer(this.sampleRateInHz);
        this.trackPlayer = new AudioTrack(3, this.sampleRateInHz, 2, 2, this.audioTrackBufferSize, 1);
    }

    public void startRecord() {
        if (!this.isRecording) {
            synchronized (this.mutex) {
                this.isRecording = true;
                this.recordThread = new Thread(new Runnable() { // from class: com.wificar.component.AudioComponent.1
                    @Override // java.lang.Runnable
                    public void run() {
                        AudioComponent.this.initialRecorder();
                        if (AudioComponent.this.audioRecord.getState() != 0) {
                            AudioComponent.this.audioRecord.startRecording();
                            int serial = 0;
                            int index = 0;
                            int ticktime = 0;
                            int timestamp = 0;
                            int sample = 0;
                            while (AudioComponent.this.isRecording) {
                                byte[] buffer = new byte[640];
                                int readState = AudioComponent.this.audioRecord.read(buffer, 0, 640);
                                if (readState == -1 || readState != -2) {
                                }
                                TalkData data = AudioComponent.encodeAdpcm(buffer, buffer.length, sample, index);
                                data.setSerial(serial);
                                data.setTicktime(ticktime);
                                data.setTimestamp(timestamp);
                                sample = data.getParaSample();
                                index = data.getParaIndex();
                                try {
                                    AudioComponent.this.wificar.sendTalkData(data, 0);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                ticktime += 40;
                                serial++;
                                timestamp = (int) (System.currentTimeMillis() / 1000);
                                try {
                                    Thread.sleep(20L);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            AudioComponent.this.audioRecord.stop();
                            AudioComponent.this.audioRecord.release();
                            AudioComponent.this.audioRecord = null;
                        }
                    }
                });
                this.recordThread.setName("Recording Thread");
                this.recordThread.start();
            }
        }
    }

    public int initialRecorder() {
        try {
            this.bufferSize = AudioRecord.getMinBufferSize(this.sampleRateInHz, 16, 2);
            this.audioRecord = new AudioRecord(1, this.sampleRateInHz, 16, 2, this.bufferSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.audioRecord.getState();
    }

    public void initialPlayer(int sampleRate) {
        this.audioTrackBufferSize = AudioTrack.getMinBufferSize(sampleRate, 2, 2);
    }

    public void play() {
        initialPlayer(this.sampleRateInHz);
        this.trackPlayer.play();
        this.isPlaying = true;
    }

    public void stopPlayer() {
        this.isPlaying = false;
        if (this.trackPlayer != null) {
            this.trackPlayer.stop();
        }
    }

    public void stopRecord() {
        this.isRecording = false;
    }

    public void writeAudioData(AudioData aData) {
        if (this.isPlaying) {
            this.trackPlayer.write(aData.getPCMData(), 0, aData.getPCMData().length);
            this.trackPlayer.flush();
        }
    }

    public static byte[] encodePCMToADPCM(byte[] raw, int len, int sample, int index) {
        int sb;
        short[] pcm = ByteUtility.bytesToShorts(raw);
        byte[] adpcm = new byte[len / 4];
        int len2 = len >> 1;
        for (int i = 0; i < len2; i++) {
            int delta = pcm[i] - sample;
            if (delta < 0) {
                delta = -delta;
                sb = 8;
            } else {
                sb = 0;
            }
            int code = (delta * 4) / stepTable[index];
            if (code > 7) {
                code = 7;
            }
            int delta2 = ((stepTable[index] * code) / 4) + (stepTable[index] / 8);
            if (sb > 0) {
                delta2 = -delta2;
            }
            sample += delta2;
            if (sample > 32767) {
                sample = 32767;
            } else if (sample < -32768) {
                sample = -32768;
            }
            index += indexAdjust[code];
            if (index < 0) {
                index = 0;
            } else if (index > 88) {
                index = 88;
            }
            if ((i & 1) == 1) {
                int i2 = i >> 1;
                adpcm[i2] = (byte) (adpcm[i2] | code | sb);
            } else {
                adpcm[i >> 1] = (byte) ((code | sb) << 4);
            }
        }
        return adpcm;
    }

    public static TalkData encodeAdpcm(byte[] raw, int len, int sample, int index) {
        int sb;
        short[] pcm = ByteUtility.bytesToShorts(raw);
        byte[] adpcm = new byte[len / 4];
        int len2 = len >> 1;
        for (int i = 0; i < len2; i++) {
            int delta = pcm[i] - sample;
            if (delta < 0) {
                delta = -delta;
                sb = 8;
            } else {
                sb = 0;
            }
            int code = (delta * 4) / stepTable[index];
            if (code > 7) {
                code = 7;
            }
            int delta2 = ((stepTable[index] * code) / 4) + (stepTable[index] / 8);
            if (sb > 0) {
                delta2 = -delta2;
            }
            sample += delta2;
            if (sample > 32767) {
                sample = 32767;
            } else if (sample < -32768) {
                sample = -32768;
            }
            index += indexAdjust[code];
            if (index < 0) {
                index = 0;
            } else if (index > 88) {
                index = 88;
            }
            if ((i & 1) == 1) {
                int i2 = i >> 1;
                adpcm[i2] = (byte) (adpcm[i2] | code | sb);
            } else {
                adpcm[i >> 1] = (byte) ((code | sb) << 4);
            }
        }
        TalkData data = new TalkData(adpcm, sample, index);
        return data;
    }

    public static byte[] decodeADPCMToPCM(byte[] raw, int len, int sample, int index) {
        int code;
        int sb;
        ByteBuffer bDecoded = ByteBuffer.allocate(len * 4);
        int len2 = len << 1;
        for (int i = 0; i < len2; i++) {
            if ((i & 1) != 0) {
                code = raw[i >> 1] & 15;
            } else {
                code = raw[i >> 1] >> 4;
            }
            if ((code & 8) != 0) {
                sb = 1;
            } else {
                sb = 0;
            }
            int code2 = code & 7;
            int delta = ((stepTable[index] * code2) / 4) + (stepTable[index] / 8);
            if (sb != 0) {
                delta = -delta;
            }
            sample += delta;
            if (sample > 32767) {
                sample = 32767;
            } else if (sample < -32768) {
                sample = -32768;
            }
            bDecoded.put(CommandEncoder.int16ToByteArray(sample));
            index += indexAdjust[code2];
            if (index < 0) {
                index = 0;
            }
            if (index > 88) {
                index = 88;
            }
        }
        return bDecoded.array();
    }
}
