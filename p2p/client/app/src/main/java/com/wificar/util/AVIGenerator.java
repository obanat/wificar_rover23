package com.wificar.util;

import android.content.Context;
import com.wificar.component.CommandEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/* loaded from: classes.dex */
public class AVIGenerator {
    long audiostartime;
    FileChannel aviChannel;
    File aviFile;
    long aviMovieOffset;
    FileOutputStream aviOutput;
    long fileEndPos;
    double framerate;
    long hdrlBeginPos;
    long hdrlEndPos;
    int height;
    File indexFile;
    AVIIndexList indexlist;
    int mAudioIndex;
    boolean mAudioStream;
    int mTalkIndex;
    boolean mTalkStream;
    int mVideoIndex;
    boolean mVideoStream;
    long mainHdrOffset;
    int numFrames;
    int numSamples;
    long riffOffset;
    double samplerate;
    long strlBegPos;
    long strlEndPos;
    long videostarttime;
    int width;

    private AVIGenerator(Context context, File aviFile, int width, int height, double framerate, int numFrames) throws Exception {
        this.width = 0;
        this.height = 0;
        this.framerate = 0.0d;
        this.samplerate = 0.0d;
        this.numFrames = 0;
        this.numSamples = 0;
        this.aviFile = null;
        this.indexFile = null;
        this.aviOutput = null;
        this.aviChannel = null;
        this.riffOffset = 0L;
        this.aviMovieOffset = 0L;
        this.mainHdrOffset = 0L;
        this.videostarttime = 0L;
        this.audiostartime = 0L;
        this.hdrlBeginPos = 0L;
        this.hdrlEndPos = 0L;
        this.fileEndPos = 0L;
        this.strlBegPos = 0L;
        this.strlEndPos = 0L;
        this.indexlist = null;
        this.mVideoStream = false;
        this.mAudioStream = false;
        this.mTalkStream = false;
        this.mVideoIndex = -1;
        this.mAudioIndex = -1;
        this.mTalkIndex = -1;
        this.aviFile = aviFile;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.numFrames = numFrames;
        AppLog.v("framerat", "AVIGenerator framerat=" + framerate);
        this.aviOutput = context.openFileOutput(aviFile.getAbsolutePath(), 1);
        this.aviChannel = this.aviOutput.getChannel();
        RIFFHeader rh = new RIFFHeader();
        this.aviOutput.write(rh.toBytes());
        long position = this.aviChannel.position();
        this.mainHdrOffset = position;
        this.hdrlBeginPos = position;
        this.aviOutput.write(new AVIMainHeader().toBytes());
        this.strlBegPos = this.aviChannel.position();
        this.aviOutput.write(new AVIStreamVideoList().toBytes());
        this.aviOutput.write(new AVIStreamVideoHeader().toBytes());
        this.aviOutput.write(new AVIStreamVideoFormat().toBytes());
        this.aviOutput.write(new AVIStreamAudioList().toBytes());
        this.aviOutput.write(new AVIStreamAudioHeader().toBytes());
        this.aviOutput.write(new AVIStreamAudioFormat().toBytes());
        this.aviOutput.write(new AVIStreamAudioList().toBytes());
        this.aviOutput.write(new AVIStreamAudioHeader().toBytes());
        this.aviOutput.write(new AVIStreamAudioFormat().toBytes());
        long position2 = this.aviChannel.position();
        this.hdrlEndPos = position2;
        this.strlEndPos = position2;
        this.aviOutput.write(new AVIJunk().toBytes());
        this.aviMovieOffset = this.aviChannel.position();
        this.aviOutput.write(new AVIMovieList().toBytes());
        this.indexlist = new AVIIndexList();
    }

    private AVIGenerator(Context context, File aviFile, int width, int height) throws Exception {
        this(context, aviFile, width, height, 30.0d, 0);
    }

    public AVIGenerator(File aviFile) {
        this.width = 0;
        this.height = 0;
        this.framerate = 0.0d;
        this.samplerate = 0.0d;
        this.numFrames = 0;
        this.numSamples = 0;
        this.aviFile = null;
        this.indexFile = null;
        this.aviOutput = null;
        this.aviChannel = null;
        this.riffOffset = 0L;
        this.aviMovieOffset = 0L;
        this.mainHdrOffset = 0L;
        this.videostarttime = 0L;
        this.audiostartime = 0L;
        this.hdrlBeginPos = 0L;
        this.hdrlEndPos = 0L;
        this.fileEndPos = 0L;
        this.strlBegPos = 0L;
        this.strlEndPos = 0L;
        this.indexlist = null;
        this.mVideoStream = false;
        this.mAudioStream = false;
        this.mTalkStream = false;
        this.mVideoIndex = -1;
        this.mAudioIndex = -1;
        this.mTalkIndex = -1;
        this.aviFile = aviFile;
    }

    public boolean addVideoStream(int height, int width) {
        this.height = height;
        this.width = width;
        this.mVideoStream = true;
        return true;
    }

    public boolean addAudioStream() {
        this.mAudioStream = true;
        return true;
    }

    public boolean addTalkStream() {
        this.mTalkStream = true;
        return true;
    }

    public boolean startAVI() throws Exception {
        this.aviOutput = new FileOutputStream(this.aviFile);
        this.aviChannel = this.aviOutput.getChannel();
        RIFFHeader rh = new RIFFHeader();
        this.aviOutput.write(rh.toBytes());
        long position = this.aviChannel.position();
        this.mainHdrOffset = position;
        this.hdrlBeginPos = position;
        this.aviOutput.write(new AVIMainHeader().toBytes());
        this.strlBegPos = this.aviChannel.position();
        int index = 0;
        if (this.mVideoStream) {
            int index2 = 0 + 1;
            this.mVideoIndex = 0;
            this.aviOutput.write(new AVIStreamVideoList().toBytes());
            this.aviOutput.write(new AVIStreamVideoHeader().toBytes());
            this.aviOutput.write(new AVIStreamVideoFormat().toBytes());
            index = index2;
        }
        if (this.mAudioStream) {
            this.mAudioIndex = index;
            this.aviOutput.write(new AVIStreamAudioList().toBytes());
            this.aviOutput.write(new AVIStreamAudioHeader().toBytes());
            this.aviOutput.write(new AVIStreamAudioFormat().toBytes());
            index++;
        }
        if (this.mTalkStream) {
            int i = index + 1;
            this.mTalkIndex = index;
            this.aviOutput.write(new AVIStreamAudioList().toBytes());
            this.aviOutput.write(new AVIStreamAudioHeader().toBytes());
            this.aviOutput.write(new AVIStreamAudioFormat().toBytes());
        }
        long position2 = this.aviChannel.position();
        this.hdrlEndPos = position2;
        this.strlEndPos = position2;
        this.aviOutput.write(new AVIJunk().toBytes());
        this.aviMovieOffset = this.aviChannel.position();
        this.aviOutput.write(new AVIMovieList().toBytes());
        this.indexlist = new AVIIndexList();
        this.indexFile = new File(String.valueOf(this.aviFile.getAbsolutePath()) + ".index");
        return this.indexFile != null;
    }

    public boolean addImage(byte[] imagedata) throws Exception {
        if (!this.mVideoStream) {
            return false;
        }
        byte[] fcc = {48, (byte) (this.mVideoIndex + 48), 100, 99};
        int useLength = imagedata.length;
        long position = this.aviChannel.position();
        int extra = (((int) position) + useLength) % 4;
        if (extra > 0) {
            useLength += extra;
        }
        this.indexlist.addAVIIndex(this.mVideoIndex, StreamType.STREAM_VIDEO_C, (int) ((position - this.aviMovieOffset) - 8), useLength);
        this.aviOutput.write(fcc);
        this.aviOutput.write(intBytes(swapInt(useLength)));
        this.aviOutput.write(imagedata);
        AppLog.d("avi", "image:insert(" + this.numFrames + ")" + System.currentTimeMillis());
        if (extra > 0) {
            for (int i = 0; i < extra; i++) {
                this.aviOutput.write(0);
            }
        }
        this.numFrames++;
        if (this.videostarttime == 0) {
            this.videostarttime = System.currentTimeMillis();
        }
        return true;
    }

    public void addAudio(byte[] audiodata, int offset, int length) throws Exception {
        if (this.mAudioStream) {
            byte[] fcc = {48, (byte) (this.mAudioIndex + 48), 119, 98};
            int useLength = length;
            long position = this.aviChannel.position();
            int extra = (((int) position) + useLength) % 4;
            if (extra > 0) {
                useLength += extra;
            }
            this.indexlist.addAVIIndex(this.mAudioIndex, StreamType.STREAM_AUDIO, (int) ((position - this.aviMovieOffset) - 8), useLength);
            this.aviOutput.write(fcc);
            this.aviOutput.write(intBytes(swapInt(useLength)));
            this.aviOutput.write(audiodata, offset, length);
            if (extra > 0) {
                for (int i = 0; i < extra; i++) {
                    this.aviOutput.write(0);
                }
            }
            if (this.audiostartime == 0) {
                this.audiostartime = Calendar.getInstance().getTimeInMillis();
            }
            this.numSamples++;
        }
    }

    public void addTalk(byte[] audiodata, int offset, int length) throws Exception {
        if (this.mTalkStream) {
            byte[] fcc = {48, (byte) (this.mTalkIndex + 48), 119, 98};
            int useLength = length;
            long position = this.aviChannel.position();
            int extra = (((int) position) + useLength) % 4;
            if (extra > 0) {
                useLength += extra;
            }
            this.indexlist.addAVIIndex(this.mTalkIndex, StreamType.STREAM_AUDIO, (int) ((position - this.aviMovieOffset) - 8), useLength);
            this.aviOutput.write(fcc);
            this.aviOutput.write(intBytes(swapInt(useLength)));
            this.aviOutput.write(audiodata, offset, length);
            if (extra > 0) {
                for (int i = 0; i < extra; i++) {
                    this.aviOutput.write(0);
                }
            }
        }
    }

    public void finishAVI(long aa) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(this.indexFile, true);
            fos.write(this.indexlist.toBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.currentTimeMillis();
        byte[] fcc_index = {105, 100, 120, 49};
        this.aviOutput.write(fcc_index);
        this.aviOutput.write(intBytes(swapInt((int) this.indexFile.length())));
        FileInputStream fis = new FileInputStream(this.indexFile);
        byte[] buffer = new byte[4096];
        while (true) {
            int nRead = fis.read(buffer);
            if (nRead <= 0) {
                break;
            }
            this.aviOutput.write(buffer, 0, nRead);
        }
        fis.close();
        this.aviOutput.close();
        long size = this.aviFile.length();
        this.fileEndPos = size;
        RandomAccessFile raf = new RandomAccessFile(this.aviFile, "rw");
        this.framerate = (this.numFrames * 1000.0d) / (this.numSamples * 40);
        this.samplerate = (this.numSamples * 1000) / aa;
        raf.write(new RIFFHeader().toBytes());
        raf.write(new AVIMainHeader().toBytes());
        if (this.mVideoStream) {
            raf.write(new AVIStreamVideoList().toBytes());
            raf.write(new AVIStreamVideoHeader().toBytes());
            raf.write(new AVIStreamVideoFormat().toBytes());
            this.mVideoStream = false;
        }
        if (this.mAudioStream) {
            raf.write(new AVIStreamAudioList().toBytes());
            raf.write(new AVIStreamAudioHeader().toBytes());
            raf.write(new AVIStreamAudioFormat().toBytes());
            this.mAudioStream = false;
        }
        if (this.mTalkStream) {
            raf.write(new AVIStreamAudioList().toBytes());
            raf.write(new AVIStreamAudioHeader().toBytes());
            raf.write(new AVIStreamAudioFormat().toBytes());
            this.mTalkStream = false;
        }
        raf.seek(this.aviMovieOffset + 4);
        raf.write(intBytes(swapInt((int) (((size - 8) - this.aviMovieOffset) - (this.indexFile.length() + 8)))));
        raf.close();
        this.indexFile.delete();
    }

    public static int swapInt(int v) {
        return (v >>> 24) | (v << 24) | ((v << 8) & 16711680) | ((v >> 8) & 65280);
    }

    public static short swapShort(short v) {
        return (short) ((v >>> 8) | (v << 8));
    }

    public static byte[] intBytes(int i) {
        byte[] b = {(byte) (i >>> 24), (byte) ((i >>> 16) & CommandEncoder.KEEP_ALIVE), (byte) ((i >>> 8) & CommandEncoder.KEEP_ALIVE), (byte) (i & CommandEncoder.KEEP_ALIVE)};
        return b;
    }

    public static byte[] shortBytes(short i) {
        byte[] b = {(byte) (i >>> 8), (byte) (i & 255)};
        return b;
    }

    /* loaded from: classes.dex */
    private class RIFFHeader {
        public byte[] fcc = {82, 73, 70, 70};
        public byte[] fcc2 = {65, 86, 73, 32};
        public byte[] fcc3 = {76, 73, 83, 84};
        public byte[] fcc4 = {104, 100, 114, 108};
        public int fileSize;
        public int listSize;

        public RIFFHeader() {
            this.fileSize = ((int) AVIGenerator.this.fileEndPos) - 8;
            this.listSize = (int) ((AVIGenerator.this.hdrlEndPos - AVIGenerator.this.hdrlBeginPos) + 4);
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.fileSize)));
            baos.write(this.fcc2);
            baos.write(this.fcc3);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.listSize)));
            baos.write(this.fcc4);
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIMainHeader {
        public int dwHeight;
        public int dwMicroSecPerFrame;
        public int[] dwReserved;
        public int dwStreams;
        public int dwSuggestedBufferSize;
        public int dwTotalFrames;
        public int dwWidth;
        public byte[] fcc = {97, 118, 105, 104};
        public int cb = 56;
        public int dwMaxBytesPerSec = 10000000;
        public int dwPaddingGranularity = 0;
        public int dwFlags = 65552;
        public int dwInitialFrames = 0;

        public AVIMainHeader() {
            this.dwMicroSecPerFrame = 0;
            this.dwTotalFrames = 0;
            this.dwStreams = (AVIGenerator.this.mVideoStream ? 1 : 0) + (AVIGenerator.this.mAudioStream ? 1 : 0) + (AVIGenerator.this.mTalkStream ? 1 : 0);
            this.dwSuggestedBufferSize = 0;
            this.dwWidth = 0;
            this.dwHeight = 0;
            this.dwReserved = new int[4];
            this.dwMicroSecPerFrame = (int) ((1.0d / AVIGenerator.this.framerate) * 1000000.0d);
            this.dwWidth = AVIGenerator.this.width;
            this.dwHeight = AVIGenerator.this.height;
            this.dwTotalFrames = AVIGenerator.this.numFrames;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.cb)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwMicroSecPerFrame)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwMaxBytesPerSec)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwPaddingGranularity)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwFlags)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwTotalFrames)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwInitialFrames)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwStreams)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwSuggestedBufferSize)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwWidth)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwHeight)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwReserved[0])));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwReserved[1])));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwReserved[2])));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwReserved[3])));
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIStreamAudioList {
        public byte[] fcc = {76, 73, 83, 84};
        public int size = 96;
        public byte[] fcc2 = {115, 116, 114, 108};

        public AVIStreamAudioList() {
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.size)));
            baos.write(this.fcc2);
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIStreamVideoList {
        public static final int size = 116;
        public byte[] fcc = {76, 73, 83, 84};
        public byte[] fcc2 = {115, 116, 114, 108};

        public AVIStreamVideoList() {
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(size)));
            baos.write(this.fcc2);
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIStreamVideoHeader {
        public static final int cb = 56;
        public int dwLength;
        public int dwScale;
        public byte[] fcc = {115, 116, 114, 104};
        public byte[] fccType = {118, 105, 100, 115};
        public byte[] fccHandler = {77, 74, 80, 71};
        public int dwFlags = 0;
        public short wPriority = 0;
        public short wLanguage = 0;
        public int dwInitialFrames = 0;
        public int dwRate = 1000000;
        public int dwStart = 0;
        public int dwSuggestedBufferSize = 0;
        public int dwQuality = -1;
        public int dwSampleSize = 0;
        public short left = 0;
        public short top = 0;
        public short right = 0;
        public short bottom = 0;

        public AVIStreamVideoHeader() {
            this.dwScale = 0;
            this.dwLength = 0;
            AppLog.v("framerateavistreamvidiohearder", "framerate=" + AVIGenerator.this.framerate);
            this.dwScale = (int) ((1.0d / AVIGenerator.this.framerate) * 1000000.0d);
            AppLog.v("dwscale", "dwscaleavistreamvideoheader=" + this.dwScale);
            this.dwLength = AVIGenerator.this.numFrames;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(56)));
            baos.write(this.fccType);
            baos.write(this.fccHandler);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwFlags)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.wPriority)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.wLanguage)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwInitialFrames)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwScale)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwRate)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwStart)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwLength)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwSuggestedBufferSize)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwQuality)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwSampleSize)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.left)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.top)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.right)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.bottom)));
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIStreamAudioHeader {
        public static final int cb = 56;
        public int dwLength;
        public byte[] fcc = {115, 116, 114, 104};
        public byte[] fccType = {97, 117, 100, 115};
        public byte[] fccHandler = new byte[4];
        public int dwFlags = 0;
        public short wPriority = 0;
        public short wLanguage = 0;
        public int dwInitialFrames = 0;
        public int dwScale = 4;
        public int dwRate = 32000;
        public int dwStart = 0;
        public int dwSuggestedBufferSize = 0;
        public int dwQuality = -1;
        public int dwSampleSize = 2;
        public short left = 0;
        public short top = 0;
        public short right = 0;
        public short bottom = 0;

        public AVIStreamAudioHeader() {
            this.dwLength = 0;
            AppLog.v("samplerate", "samplerate=" + AVIGenerator.this.samplerate);
            AVIGenerator.this.samplerate = 25.0d;
            this.dwLength = AVIGenerator.this.numSamples * 320;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(56)));
            baos.write(this.fccType);
            baos.write(this.fccHandler);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwFlags)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.wPriority)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.wLanguage)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwInitialFrames)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwScale)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwRate)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwStart)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwLength)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwSuggestedBufferSize)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwQuality)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwSampleSize)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.left)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.top)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.right)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.bottom)));
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIStreamVideoFormat {
        public static final int cb = 40;
        public int biHeight;
        public int biSizeImage;
        public int biWidth;
        public byte[] fcc = {115, 116, 114, 102};
        public int biSize = 40;
        public short biPlanes = 1;
        public short biBitCount = 24;
        public byte[] biCompression = {77, 74, 80, 71};
        public int biXPelsPerMeter = 0;
        public int biYPelsPerMeter = 0;
        public int biClrUsed = 0;
        public int biClrImportant = 0;

        public AVIStreamVideoFormat() {
            this.biWidth = 0;
            this.biHeight = 0;
            this.biSizeImage = 0;
            this.biWidth = AVIGenerator.this.width;
            this.biHeight = AVIGenerator.this.height;
            this.biSizeImage = AVIGenerator.this.width * AVIGenerator.this.height;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(40)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biSize)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biWidth)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biHeight)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.biPlanes)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.biBitCount)));
            baos.write(this.biCompression);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biSizeImage)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biXPelsPerMeter)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biYPelsPerMeter)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biClrUsed)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.biClrImportant)));
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIStreamAudioFormat {
        public static final int cb = 20;
        public byte[] fcc = {115, 116, 114, 102};
        public short wFormatTag = 1;
        public short nChannels = 1;
        public int nSamplesPerSec = 8000;
        public short wBitsPerSample = 16;
        public short nBlockAlign = 2;
        public int nAvgBytesPerSec = 16000;
        public short nSamplesPerBlock = 320;
        public short cbSize = 0;

        public AVIStreamAudioFormat() {
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(20)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.wFormatTag)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.nChannels)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.nSamplesPerSec)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.nAvgBytesPerSec)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.nBlockAlign)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.wBitsPerSample)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.cbSize)));
            baos.write(AVIGenerator.shortBytes(AVIGenerator.swapShort(this.nSamplesPerBlock)));
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIMovieList {
        public byte[] fcc = {76, 73, 83, 84};
        public int listSize = 0;
        public byte[] fcc2 = {109, 111, 118, 105};

        public AVIMovieList() {
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.listSize)));
            baos.write(this.fcc2);
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIIndexList {
        public byte[] fcc = {105, 100, 120, 49};
        public int cb = 0;
        public ArrayList ind = new ArrayList();
        long lastWriteTime = System.currentTimeMillis();

        public AVIIndexList() {
        }

        public void addAVIIndex(AVIIndex ai) {
            this.ind.add(ai);
        }

        public void addAVIIndex(int nStream, StreamType type, int dwOffset, int dwSize) {
            this.ind.add(new AVIIndex(nStream, type, dwOffset, dwSize));
            long nowTime = System.currentTimeMillis();
            if (nowTime - this.lastWriteTime > 1000) {
                this.lastWriteTime = nowTime;
                try {
                    FileOutputStream fos = new FileOutputStream(AVIGenerator.this.indexFile, true);
                    fos.write(toBytes());
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.ind.clear();
            }
        }

        public byte[] toBytes() throws Exception {
            this.cb = this.ind.size() * 16;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < this.ind.size(); i++) {
                AVIIndex in = (AVIIndex) this.ind.get(i);
                baos.write(in.toBytes());
            }
            baos.close();
            return baos.toByteArray();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AVIIndex {
        int[] $SWITCH_TABLE$com$wificar$util$StreamType;
        public int dwOffset;
        public int dwSize;
        public byte[] fcc = new byte[4];
        public int dwFlags = 16;

        int[] $SWITCH_TABLE$com$wificar$util$StreamType() {
            int[] iArr = $SWITCH_TABLE$com$wificar$util$StreamType;
            if (iArr == null) {
                iArr = new int[StreamType.valuesCustom().length];
                try {
                    iArr[StreamType.STREAM_AUDIO.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[StreamType.STREAM_VIDEO_B.ordinal()] = 3;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[StreamType.STREAM_VIDEO_C.ordinal()] = 2;
                } catch (NoSuchFieldError e3) {
                }
                $SWITCH_TABLE$com$wificar$util$StreamType = iArr;
            }
            return iArr;
        }

        public AVIIndex(int nStream, StreamType type, int dwOffset, int dwSize) {
            this.dwOffset = 0;
            this.dwSize = 0;
            this.fcc[0] = 48;
            this.fcc[1] = (byte) (nStream + 48);
            switch ($SWITCH_TABLE$com$wificar$util$StreamType()[type.ordinal()]) {
                case 1:
                    this.fcc[2] = 119;
                    this.fcc[3] = 98;
                    break;
                case 2:
                    this.fcc[2] = 100;
                    this.fcc[3] = 99;
                    break;
                case 3:
                    this.fcc[2] = 100;
                    this.fcc[3] = 98;
                    break;
            }
            this.dwOffset = dwOffset;
            this.dwSize = dwSize;
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwFlags)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwOffset)));
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.dwSize)));
            baos.close();
            return baos.toByteArray();
        }
    }

    /* loaded from: classes.dex */
    private class AVIJunk {
        public byte[] fcc = {74, 85, 78, 75};
        public int size = 1788;
        public byte[] data = new byte[this.size];

        public AVIJunk() {
            Arrays.fill(this.data, (byte) 0);
        }

        public byte[] toBytes() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(this.fcc);
            baos.write(AVIGenerator.intBytes(AVIGenerator.swapInt(this.size)));
            baos.write(this.data);
            baos.close();
            return baos.toByteArray();
        }
    }
}
