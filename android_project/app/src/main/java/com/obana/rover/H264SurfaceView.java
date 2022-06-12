package com.obana.rover;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class H264SurfaceView extends SurfaceView implements SurfaceHolder.Callback  {
    static int mVideoHeight = 480;
    static int mVideoWidth = 640;
    public float ZOOM[] = {
        100F, 125F, 150F, 175F, 200F
    };
    public static int[] Video_WandH = new int[] { 640, 480 };
    Bitmap bitmap;
    float bx;
    float bx1;
    float by;
    float by1;
    long donw_time;
    public boolean isFirstChange;
    public int streamsize;
    private int targetZoom;
    int mCodecState = -1;
    Surface mSurface;
    MediaCodec mCodec;
    SurfaceHolder mSurfaceHolder;
    private int mFrameIndex = 0;

    public H264SurfaceView(Context context)
    {
        super(context);
        streamsize = 0;
        targetZoom = 0;
        bx1 = 0.0F;
        by1 = 0.0F;
        isFirstChange = true;
        initial();
    }

    private void initMediaCodec() {
        if (mCodecState > 0) return;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", Video_WandH[0], Video_WandH[1]);

        /*mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 1);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);*/

        try {
            mCodec = MediaCodec.createDecoderByType("video/avc");
            if (mSurface != null && mSurface.isValid()) {
                mCodec.configure(mediaFormat, mSurface, null, 0);
                mCodec.start();
                mCodecState = 1;
            }
        } catch (Exception e) {
            Log.e("CameraView", " MediaCodec == " + e.getMessage());
            return;
        }
    }

    public H264SurfaceView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        streamsize = 0;
        targetZoom = 0;
        bx1 = 0.0F;
        by1 = 0.0F;
        isFirstChange = true;
        initial();
    }

    public final int getTargetZoom()
    {
        return targetZoom;
    }

    public float getTargetZoomValue()
    {
        return ZOOM[targetZoom];
    }

    public void initial()
    {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        initMediaCodec();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }
 


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurface = holder.getSurface();
        initMediaCodec();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = null;
    }

    public final void setTargetZoom(int i)
    {
        targetZoom = i;
    }

    public void takePicture()
    {
        //AppCameraSurfaceFunction.getAppCameraSurfaceFunctionInstance().CameraTakePicture();
    }

    public int zoomIn()
        throws InterruptedException
    {
        if(targetZoom >= 0 && targetZoom < 4)
        {
            //AppDecodeH264.GlZoomIn();
            targetZoom = targetZoom + 1;
        }
        return targetZoom;
    }

    public void zoomInit()
    {
        //AppDecodeH264.GlZoomInit();
        targetZoom = 0;
    }

    public int zoomOut()throws InterruptedException{
        if(targetZoom > 0 && targetZoom <= 4)
        {
            //AppDecodeH264.GlZoomOut();
            targetZoom = targetZoom - 1;
        }
        return targetZoom;
    }

    private byte[] Bitmap2Bytes() {
        Bitmap bitmap;
        byte abyte0[];
        int i=0;
        bitmap = Bitmap.createBitmap(Video_WandH[0], Video_WandH[1], android.graphics.Bitmap.Config.ARGB_8888);
        abyte0 = new byte[bitmap.getWidth() * bitmap.getHeight() * 4];
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        while (i < height) {
            int j = 0;
            while (j < width) {
                abyte0[i * j] = (byte)0;
                j++;
            }
            i++;
        }
        return abyte0;
    }
    
    public void decodeOneFrame(byte[] data, int length) {
        if (mSurface != null && mSurface.isValid()) {
            if (mCodec != null) {
                try {

                    int inputBufferIndex = mCodec.dequeueInputBuffer(0);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferIndex);
                        long timestamp = mFrameIndex++ * 1000000 / 30;
                        inputBuffer.clear();
                        inputBuffer.put(data, 0, length);
                        mCodec.queueInputBuffer(inputBufferIndex, 0, length, timestamp, 0);
                    }
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        mCodec.releaseOutputBuffer(outputBufferIndex, true);
                        outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                } catch (Throwable t) {
                    //Log.e(TAG, "offerDecoder233 == " + t.toString() + t.getMessage());

                    release();
                }

            } 
        }
    }
    public void release() {
        if (mCodec != null) {
            mCodec.release();
            mCodec = null;
        }
    }
}
