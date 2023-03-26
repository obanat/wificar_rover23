package com.wificar.component;

import com.wificar.util.AVIGenerator;
import com.wificar.util.AppLog;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/* loaded from: classes.dex */
public class VideoComponent implements Runnable {
    private static Vector<AudioData> audioDatas = new Vector<>();
    private static Vector<VideoData> videoDatas = new Vector<>();
    WifiCar car;
    public int debugtemp = 0;
    public int debugtemp2 = 0;
    AVIGenerator aviGenerator = null;
    long lastVideoFrameTimestamp = 0;
    long lastAudioFrameTimestamp = 0;
    long lastVideoFrameCustomTimestamp = 0;
    long lastAudioFrameCustomTimestamp = 0;
    long lastCustomTimestampInterval = 0;
    int state = 0;
    int discard_frame_num = 0;

    public VideoComponent(WifiCar car) {
        this.car = null;
        this.car = car;
    }

    public void pushVideoData(VideoData vData, int mark) throws Exception {
        if (this.state == 1) {
            AppLog.i("record3", String.valueOf(System.currentTimeMillis()) + "videoData:" + vData.getTimestamp() + ":" + mark);
            long customTimestampInterval = vData.getCustomTimestamp() - getLastVideoFrameCustomTimestamp();
            if (customTimestampInterval > 1000) {
                customTimestampInterval = 0;
            }
            this.car.getAudioFlag();
            vData.setCustomDelay((int) customTimestampInterval);
            this.lastVideoFrameTimestamp = vData.getTimestamp();
            this.lastVideoFrameCustomTimestamp = vData.getCustomTimestamp();
            if (this.discard_frame_num == 0) {
                this.debugtemp++;
                videoDatas.add(vData);
                int n = videoDatas.size() / 30;
                if (n > 0) {
                    this.discard_frame_num = n;
                    return;
                }
                return;
            }
            this.discard_frame_num--;
        }
    }

    public long getLastVideoFrameTimestamp() {
        return this.lastVideoFrameTimestamp;
    }

    public long getLastAudioFrameTimestamp() {
        return this.lastAudioFrameTimestamp;
    }

    public long getLastVideoFrameCustomTimestamp() {
        return this.lastVideoFrameCustomTimestamp;
    }

    public long getLastAudioFrameCustomTimestamp() {
        return this.lastAudioFrameCustomTimestamp;
    }

    public void pushAudioData(AudioData aData, int mark) throws Exception {
        if (this.state == 1) {
            aData.getADPCMDataWithSample();
            this.debugtemp2++;
            audioDatas.add(aData);
            this.lastAudioFrameTimestamp = aData.getTimestamp();
            this.lastAudioFrameCustomTimestamp = aData.getCustomTimestamp();
        }
    }

    public void start(String path, String fileName, int width, int height) throws Exception {
        AppLog.e("file", "save as:" + path + "," + fileName);
        File avi = new File(path, fileName);
        this.aviGenerator = new AVIGenerator(avi);
        this.aviGenerator.addVideoStream(height, width);
        this.aviGenerator.addAudioStream();
        this.aviGenerator.startAVI();
        this.state = 1;
        Thread t = new Thread(this);
        t.setName("FLIM Thread");
        t.start();
    }

    public void stop() throws Exception {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        this.state = 0;
    }

    public int check() {
        if (this.state == 0) {
            if (videoDatas.size() == 0 && audioDatas.size() == 0) {
                return 1;
            }
            if (videoDatas.size() == 0 || audioDatas.size() == 0) {
                return -1;
            }
        }
        return 0;
    }

    public void preProcess() {
        if (videoDatas.size() != 0 && audioDatas.size() != 0) {
            AudioData fad = audioDatas.get(0);
            VideoData fvd = videoDatas.get(0);
            long fadTime = fad.getTimestamp();
            long fvdTime = fvd.getTimestamp();
            while (Math.abs(fadTime - fvdTime) > 5 && fadTime > fvdTime) {
                videoDatas.remove(0);
                if (videoDatas.size() == 0) {
                    break;
                }
                VideoData fvd2 = videoDatas.get(0);
                fvdTime = fvd2.getTimestamp();
            }
            while (Math.abs(fadTime - fvdTime) > 5 && fvdTime > fadTime) {
                audioDatas.remove(0);
                if (audioDatas.size() != 0) {
                    AudioData fad2 = audioDatas.get(0);
                    fadTime = fad2.getTimestamp();
                } else {
                    return;
                }
            }
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        int frame_numb = 0;
        long firsttimestamp = 0;
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        preProcess();
        long lastCustomTimestamp = 0;
        long lastTimestamp = 0;
        long currentTimestamp = System.currentTimeMillis();
        videoDatas.size();
        audioDatas.size();
        while (this.state == 1) {
            if (videoDatas.size() > 0) {
                VideoData fvd = videoDatas.get(0);
                if (fvd == null) {
                    AppLog.e("recordvideo", "****fvd is null!!!***");
                } else {
                    lastCustomTimestamp = fvd.getCustomTimestamp();
                    lastTimestamp = fvd.getTimestamp();
                    if (frame_numb == 0) {
                        frame_numb = 1;
                        firsttimestamp = lastCustomTimestamp;
                    }
                    try {
                        long t = System.currentTimeMillis() - currentTimestamp;
                        if (t < fvd.getCustomDelay()) {
                            Thread.sleep(fvd.getCustomDelay() - t);
                        }
                        if (this.aviGenerator.addImage(fvd.getData())) {
                            videoDatas.remove(0);
                            currentTimestamp = System.currentTimeMillis();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
            while (audioDatas.size() > 0 && audioDatas.get(0).getTimestamp() <= lastTimestamp) {
                try {
                    AudioData fad = audioDatas.get(0);
                    fad.getTimestamp();
                    byte[] data = fad.getPCMFromeADPCM();
                    this.aviGenerator.addAudio(data, 0, data.length);
                    audioDatas.remove(0);
                } catch (IOException e3) {
                    e3.printStackTrace();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
        }
        try {
            this.aviGenerator.finishAVI(lastCustomTimestamp - firsttimestamp);
            audioDatas.clear();
            videoDatas.clear();
        } catch (Exception e5) {
            e5.printStackTrace();
        }
    }
}
