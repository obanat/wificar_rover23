package com.obana.rover;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.graphics.BitmapFactory;
import java.util.concurrent.TimeUnit;
import com.obana.rover.utils.AppLog;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
	private final static String TAG = "WifiCar_JPEG";


    public final static int SIZE_STANDARD = 1;
    public final static int SIZE_FULL_RECT = 4;

    private final static String SAVE_TO_DIR = "mjpegview";
    private final static int REDRAW_DELAY_MS = 20;


    private boolean mRun = false;
    private boolean surfaceDone = false;
    private SurfaceHolder mSurfaceHolder;
    private int displayMode;

    private boolean resume = false;
    private boolean mtakePic = false;//flag for take a picture
    //private String mFileName = null;//file name to save picture
    private final Semaphore available = new Semaphore(1, true);
    private byte[] cameraData;
    private Context context;
    private int timeOut = REDRAW_DELAY_MS;
    private Handler handler = null;
    private HandlerThread handlerThread = new HandlerThread("camera surface");
    private int mRectWidth = 0;
    private int mRectHeight = 0;
    private android.graphics.BitmapFactory.Options opt;

    private void init(Context context) {

        this.context = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper());
    
        displayMode = MjpegView.SIZE_FULL_RECT;
        opt = opt = new android.graphics.BitmapFactory.Options();
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inDither = true;
        opt.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565;

        //thread = new MjpegViewThread(holder, context);
        setFocusable(true);

        setKeepScreenOn(true);
    }

  
    public void startPlayback() {
        mRun = true;
        this.handler.postDelayed(this.DrawingTask, this.timeOut);
    }

    public void resumePlayback() {
        mRun = true;
        this.handler.postDelayed(this.DrawingTask, this.timeOut);
    }

    public void stopPlayback() {
        mRun = false;
        this.handler.removeCallbacks(this.DrawingTask);
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        mRectWidth = w;
        mRectHeight = h;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
    }

    public MjpegView(Context context) {
        super(context);
        init(context);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    }

    public void showFps(boolean b) {
        //showFps = b;
    }

    public void setDisplayMode(int s) {
        displayMode = s;
    }

    public void saveBitmap () {

    }

    private String generateFileName() {
        return null;
    }

    private void BroardCastResult (int res, String fName) {

    }

    private int saveBitmapToFile(Bitmap mBitmap, String bitName) {

        return 0;
    }

    public void setCameraBytes(byte[] paramVideoData) {
        try {
          if (this.available.tryAcquire(this.timeOut, TimeUnit.MILLISECONDS)) {
            this.cameraData = paramVideoData;
            this.available.release();
          } 
        } catch (InterruptedException interruptedException) {
          interruptedException.printStackTrace();
        } 
    }
  
    private Runnable DrawingTask = new Runnable() {
      public void run() {
        if (MjpegView.this.mRun)
          try {
            long l = System.currentTimeMillis();
            if (MjpegView.this.available.tryAcquire(MjpegView.this.timeOut, TimeUnit.MILLISECONDS)) {
              if (MjpegView.this.cameraData != null) {
                MjpegView.this.redraw(MjpegView.this.cameraData);
                //long l1 = System.currentTimeMillis();
                //StringBuilder stringBuilder = new StringBuilder();
                //this("redraw time(");
                //AppLog.d("redraw", stringBuilder.append(CameraSurfaceView.this.cameraData.getTimestamp()).append("):").append(l1 - l).toString());
              } 
              MjpegView.this.available.release();
            } 
          } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
          }  
        MjpegView.this.handler.postDelayed(this, MjpegView.this.timeOut);
      }
    };
    
    private Rect destRect(int bmw, int bmh) {
        int tempx;
        int tempy;
        int x = this.getLeft();
        int y = this.getTop();
        int w = this.getRight() - this.getLeft();
        int h = this.getBottom() - this.getTop();
            
        if (displayMode == MjpegView.SIZE_STANDARD) {
            tempx = (w / 2) - (bmw / 2);
            tempy = (h / 2) - (bmh / 2);
            return new Rect(0, 0, bmw, bmh);
        } else if (displayMode == MjpegView.SIZE_FULL_RECT) {
            return new Rect(0, 0, w, h);
        } else {
            return null;
        }
    }
    
    void redraw(byte[] jpegData) {
        //AppLog.d(TAG, "redraw once....");
        Rect destRect;
        Canvas c = null;
        Paint p = new Paint();
        if (surfaceDone && mRun) {
            try {
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    try {
                        Bitmap bm = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, opt);
                        destRect = destRect(bm.getWidth(), bm.getHeight());
                        if (destRect != null) {
                            //AppLog.d(TAG, "redraw rect:" + destRect);
                            c.drawColor(Color.BLACK);
                            c.drawBitmap(bm, null, destRect, p);
                        }
                    } catch (Exception e) {
                    }
                }
            } finally {
                if (c != null)
                    mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }
}