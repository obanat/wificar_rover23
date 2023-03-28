package com.wificar.surface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.AbsoluteLayout;
import com.wificar.WificarActivity;
import com.wificar.component.CommandEncoder;
import com.wificar.component.VideoData;
import com.wificar.util.AppLog;
import com.wificar.util.ImageUtility;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public static final int MESSAGE_SHIFT_VIEWPORT = 11;
    private Runnable DrawingTask;
    private int[] HEIGHT_LAND;
    private int[] HEIGHT_PORTAIT;
    private int[] WIDTH_LAND;
    private int[] WIDTH_PORTAIT;
    private float[] ZOOM;
    private float actualheight;
    private final Semaphore available;
    private Bitmap bMap;
    private boolean bMoveImage;
    private float bx;
    private float by;
    VideoData cameraData;
    private byte[] copyBytes;
    private Bitmap currentBitmap;
    private int currentHeight;
    long currentTime;
    private int currentWidth;
    private float cx;
    private float cy;
    private Handler handler;
    private HandlerThread handlerThread;
    SurfaceHolder holder;
    long intervalTime;
    private boolean isConnect;
    private Handler messageHandler;
    private BitmapFactory.Options opt;
    private int originalHeight;
    private int originalWidth;
    private Paint p;
    private AbsoluteLayout.LayoutParams params;
    private int recordingTimeOut;
    private int sx;
    private int sy;
    private int targetZoom;
    private int timeOut;

    public boolean isRecordAvailable() {
        try {
            if (this.currentBitmap.getWidth() == 320) {
                return this.currentBitmap.getHeight() == 240;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setCameraBytes(VideoData vdata) {
        try {
            if (this.available.tryAcquire(this.timeOut, TimeUnit.MILLISECONDS)) {
                this.cameraData = vdata;
                this.available.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int getScaledWidth(int zoom) {
        return WificarActivity.getInstance().isPortait() ? this.WIDTH_PORTAIT[zoom] : this.WIDTH_LAND[zoom];
    }

    private int getScaledHeight(int zoom) {
        return WificarActivity.getInstance().isPortait() ? this.HEIGHT_PORTAIT[zoom] : this.HEIGHT_LAND[zoom];
    }

    public int zoomIn() throws InterruptedException {
        if (this.targetZoom >= 0 && this.targetZoom < 4) {
            this.targetZoom++;
            int previousWidth = ImageUtility.dip2px(getContext(), getScaledWidth(this.targetZoom - 1));
            int previousHeight = ImageUtility.dip2px(getContext(), getScaledHeight(this.targetZoom - 1));
            int currentWidth = ImageUtility.dip2px(getContext(), getScaledWidth(this.targetZoom));
            int currentHeight = ImageUtility.dip2px(getContext(), getScaledHeight(this.targetZoom));
            int previousCenterX = previousWidth / 2;
            int previousCenterY = previousHeight / 2;
            int currentCenterX = currentWidth / 2;
            int currentCenterY = currentHeight / 2;
            this.sx = (this.sx + previousCenterX) - currentCenterX;
            this.sy = (this.sy + previousCenterY) - currentCenterY;
            setVisibility(8);
            setVisibility(0);
        }
        return this.targetZoom;
    }

    public int zoomOut() throws InterruptedException {
        int newX;
        int newY;
        if (this.targetZoom > 0 && this.targetZoom <= 4) {
            this.targetZoom--;
            int originalWidth = ImageUtility.dip2px(getContext(), getScaledWidth(0));
            int originalHeight = ImageUtility.dip2px(getContext(), getScaledHeight(0));
            int previousWidth = ImageUtility.dip2px(getContext(), getScaledWidth(this.targetZoom + 1));
            int previousHeight = ImageUtility.dip2px(getContext(), getScaledHeight(this.targetZoom + 1));
            int currentWidth = ImageUtility.dip2px(getContext(), getScaledWidth(this.targetZoom));
            int currentHeight = ImageUtility.dip2px(getContext(), getScaledHeight(this.targetZoom));
            int previousCenterX = previousWidth / 2;
            int previousCenterY = previousHeight / 2;
            int currentCenterX = currentWidth / 2;
            int currentCenterY = currentHeight / 2;
            int shiftX = previousCenterX - currentCenterX;
            int shiftY = previousCenterY - currentCenterY;
            if (this.sx + shiftX > 0) {
                newX = 0;
            } else {
                newX = shiftX + this.sx;
            }
            if (this.sy + shiftY > 0) {
                newY = 0;
            } else {
                newY = shiftY + this.sy;
            }
            if (this.sx + shiftX + currentWidth < originalWidth) {
                newX = originalWidth - currentWidth;
            }
            if (this.sy + shiftY + currentHeight < originalHeight) {
                newY = originalHeight - currentHeight;
            }
            this.sx = newX;
            this.sy = newY;
            setVisibility(8);
            setVisibility(0);
        }
        return this.targetZoom;
    }

    public float getTargetZoomValue() {
        return this.ZOOM[this.targetZoom];
    }

    public void initial() {
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper());
        this.holder = getHolder();
        this.holder.addCallback(this);
    }

    public CameraSurfaceView(Context context) {
        super(context);
        this.p = new Paint();
        this.actualheight = ImageUtility.getHeight(getContext()) / ImageUtility.getDensity(getContext());
        this.opt = new BitmapFactory.Options();
        this.currentBitmap = null;
        this.ZOOM = new float[]{100.0f, 125.0f, 150.0f, 175.0f, 200.0f};
        this.WIDTH_PORTAIT = new int[]{320, 400, 480, 560, 640};
        this.HEIGHT_PORTAIT = new int[]{240, 300, 360, 420, 480};
        this.timeOut = 20;
        this.recordingTimeOut = 100;
        this.currentTime = System.currentTimeMillis();
        this.intervalTime = 0L;
        this.targetZoom = 0;
        this.cx = 0.0f;
        this.cy = 0.0f;
        this.available = new Semaphore(1, true);
        this.bMoveImage = false;
        this.cameraData = null;
        this.copyBytes = null;
        this.params = null;
        this.handlerThread = new HandlerThread("camera surface");
        this.handler = null;
        this.isConnect = false;
        this.messageHandler = new Handler() { // from class: com.wificar.surface.CameraSurfaceView.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 11:
                        AppLog.d("shift", "shift viewport");
                        if (WificarActivity.getInstance().succeedConnect) {
                            CameraSurfaceView.this.redraw(CameraSurfaceView.this.cameraData.getData());
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.bx = 0.0f;
        this.by = 0.0f;
        this.DrawingTask = new Runnable() { // from class: com.wificar.surface.CameraSurfaceView.2
            @Override // java.lang.Runnable
            public void run() {
                if (!CameraSurfaceView.this.bMoveImage) {
                    try {
                        long preTime = System.currentTimeMillis();
                        if (CameraSurfaceView.this.available.tryAcquire(CameraSurfaceView.this.timeOut, TimeUnit.MILLISECONDS)) {
                            if (CameraSurfaceView.this.cameraData != null) {
                                CameraSurfaceView.this.redraw(CameraSurfaceView.this.cameraData.getData());
                                long postTime = System.currentTimeMillis();
                                AppLog.d("redraw", "redraw time(" + CameraSurfaceView.this.cameraData.getTimestamp() + "):" + (postTime - preTime));
                            }
                            CameraSurfaceView.this.available.release();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CameraSurfaceView.this.handler.postDelayed(this, CameraSurfaceView.this.timeOut);
            }
        };
        initial();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.p = new Paint();
        this.actualheight = ImageUtility.getHeight(getContext()) / ImageUtility.getDensity(getContext());
        this.opt = new BitmapFactory.Options();
        this.currentBitmap = null;
        this.ZOOM = new float[]{100.0f, 125.0f, 150.0f, 175.0f, 200.0f};
        this.WIDTH_PORTAIT = new int[]{320, 400, 480, 560, 640};
        this.HEIGHT_PORTAIT = new int[]{240, 300, 360, 420, 480};
        this.timeOut = 20;
        this.recordingTimeOut = 100;
        this.currentTime = System.currentTimeMillis();
        this.intervalTime = 0L;
        this.targetZoom = 0;
        this.cx = 0.0f;
        this.cy = 0.0f;
        this.available = new Semaphore(1, true);
        this.bMoveImage = false;
        this.cameraData = null;
        this.copyBytes = null;
        this.params = null;
        this.handlerThread = new HandlerThread("camera surface");
        this.handler = null;
        this.isConnect = false;
        this.messageHandler = new Handler() { // from class: com.wificar.surface.CameraSurfaceView.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 11:
                        AppLog.d("shift", "shift viewport");
                        if (WificarActivity.getInstance().succeedConnect) {
                            CameraSurfaceView.this.redraw(CameraSurfaceView.this.cameraData.getData());
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.bx = 0.0f;
        this.by = 0.0f;
        this.DrawingTask = new Runnable() { // from class: com.wificar.surface.CameraSurfaceView.2
            @Override // java.lang.Runnable
            public void run() {
                if (!CameraSurfaceView.this.bMoveImage) {
                    try {
                        long preTime = System.currentTimeMillis();
                        if (CameraSurfaceView.this.available.tryAcquire(CameraSurfaceView.this.timeOut, TimeUnit.MILLISECONDS)) {
                            if (CameraSurfaceView.this.cameraData != null) {
                                CameraSurfaceView.this.redraw(CameraSurfaceView.this.cameraData.getData());
                                long postTime = System.currentTimeMillis();
                                AppLog.d("redraw", "redraw time(" + CameraSurfaceView.this.cameraData.getTimestamp() + "):" + (postTime - preTime));
                            }
                            CameraSurfaceView.this.available.release();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CameraSurfaceView.this.handler.postDelayed(this, CameraSurfaceView.this.timeOut);
            }
        };
        initial();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.p = new Paint();
        this.actualheight = ImageUtility.getHeight(getContext()) / ImageUtility.getDensity(getContext());
        this.opt = new BitmapFactory.Options();
        this.currentBitmap = null;
        this.ZOOM = new float[]{100.0f, 125.0f, 150.0f, 175.0f, 200.0f};
        this.WIDTH_PORTAIT = new int[]{320, 400, 480, 560, 640};
        this.HEIGHT_PORTAIT = new int[]{240, 300, 360, 420, 480};
        this.timeOut = 20;
        this.recordingTimeOut = 100;
        this.currentTime = System.currentTimeMillis();
        this.intervalTime = 0L;
        this.targetZoom = 0;
        this.cx = 0.0f;
        this.cy = 0.0f;
        this.available = new Semaphore(1, true);
        this.bMoveImage = false;
        this.cameraData = null;
        this.copyBytes = null;
        this.params = null;
        this.handlerThread = new HandlerThread("camera surface");
        this.handler = null;
        this.isConnect = false;
        this.messageHandler = new Handler() { // from class: com.wificar.surface.CameraSurfaceView.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 11:
                        AppLog.d("shift", "shift viewport");
                        if (WificarActivity.getInstance().succeedConnect) {
                            CameraSurfaceView.this.redraw(CameraSurfaceView.this.cameraData.getData());
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.bx = 0.0f;
        this.by = 0.0f;
        this.DrawingTask = new Runnable() { // from class: com.wificar.surface.CameraSurfaceView.2
            @Override // java.lang.Runnable
            public void run() {
                if (!CameraSurfaceView.this.bMoveImage) {
                    try {
                        long preTime = System.currentTimeMillis();
                        if (CameraSurfaceView.this.available.tryAcquire(CameraSurfaceView.this.timeOut, TimeUnit.MILLISECONDS)) {
                            if (CameraSurfaceView.this.cameraData != null) {
                                CameraSurfaceView.this.redraw(CameraSurfaceView.this.cameraData.getData());
                                long postTime = System.currentTimeMillis();
                                AppLog.d("redraw", "redraw time(" + CameraSurfaceView.this.cameraData.getTimestamp() + "):" + (postTime - preTime));
                            }
                            CameraSurfaceView.this.available.release();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CameraSurfaceView.this.handler.postDelayed(this, CameraSurfaceView.this.timeOut);
            }
        };
        initial();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                if (WificarActivity.getInstance().succeedConnect) {
                    this.bMoveImage = true;
                    this.bx = event.getX();
                    this.by = event.getY();
                    this.currentWidth = ImageUtility.dip2px(getContext(), getScaledWidth(this.targetZoom));
                    this.currentHeight = ImageUtility.dip2px(getContext(), getScaledHeight(this.targetZoom));
                    this.originalWidth = ImageUtility.dip2px(getContext(), getScaledWidth(0));
                    this.originalHeight = ImageUtility.dip2px(getContext(), getScaledHeight(0));
                    break;
                }
                break;
            case 1:
                this.bMoveImage = false;
                break;
            case 2:
                if (WificarActivity.getInstance().succeedConnect) {
                    float nx = event.getX() - this.bx;
                    float ny = event.getY() - this.by;
                    if (this.sx + nx < 0.0f && this.sx + nx + this.currentWidth > this.originalWidth && this.sy + ny < 0.0f && this.sy + ny + this.currentHeight > this.originalHeight) {
                        this.sx = (int) (this.sx + nx);
                        this.sy = (int) (this.sy + ny);
                    }
                    Message msg = new Message();
                    msg.what = 11;
                    this.messageHandler.sendMessage(msg);
                    this.bx = event.getX();
                    this.by = event.getY();
                    break;
                }
                break;
        }
        return true;
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.cx = getWidth() / 2.0f;
        this.cy = getHeight() / 2.0f;
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        this.cx = getWidth() / 2.0f;
        this.cy = getHeight() / 2.0f;
        start();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.handler.removeCallbacks(this.DrawingTask);
    }

    public void start() {
        this.handler.postDelayed(this.DrawingTask, this.timeOut);
        if (WificarActivity.getInstance().isPad) {
            this.WIDTH_LAND = new int[]{720, 840, 960, 1080, 1200};
            this.HEIGHT_LAND = new int[]{540, 630, 720, 810, 900};
            return;
        }
        this.WIDTH_LAND = new int[]{480, 600, 720, 840, 960};
        this.HEIGHT_LAND = new int[]{360, 450, 540, 630, 720};
    }

    public synchronized void redraw(byte[] bArrayImage) {
        Canvas canvas = this.holder.lockCanvas();
        if (canvas != null) {
            this.p.setARGB(CommandEncoder.KEEP_ALIVE, 0, 0, 0);
            Rect rect = new Rect(0, 0, getWidth(), getHeight());
            this.opt.inPurgeable = true;
            this.opt.inInputShareable = true;
            this.opt.inDither = true;
            this.opt.inPreferredConfig = Bitmap.Config.RGB_565;
            if (bArrayImage == null) {
                canvas.drawRect(rect, this.p);
            } else {
                this.bMap = BitmapFactory.decodeByteArray(bArrayImage, 0, bArrayImage.length, this.opt);
                if (this.bMap == null) {
                    this.holder.unlockCanvasAndPost(canvas);
                } else {
                    int currentWidth = ImageUtility.dip2px(getContext(), getScaledWidth(this.targetZoom));
                    int currentHeight = ImageUtility.dip2px(getContext(), getScaledHeight(this.targetZoom));
                    if (currentWidth != this.bMap.getWidth() || currentHeight != this.bMap.getHeight()) {
                        float scaleWidth = currentWidth / this.bMap.getWidth();
                        float scaleHeight = currentHeight / this.bMap.getHeight();
                        Matrix mRescale = new Matrix();
                        mRescale.reset();
                        mRescale.postScale(scaleWidth, scaleHeight);
                        float scalesize = (this.targetZoom + 1) * this.actualheight;
                        if (scalesize > 500.0f) {
                            Matrix matrix = new Matrix();
                            matrix.reset();
                            matrix.postScale(1.0f, 1.0f);
                            matrix.postTranslate(this.sx, this.sy);
                            this.bMap = Bitmap.createBitmap(this.bMap, 0, 0, this.bMap.getWidth(), this.bMap.getHeight(), mRescale, true);
                            canvas.drawBitmap(this.bMap, matrix, this.p);
                        } else {
                            mRescale.postTranslate(this.sx, this.sy);
                            canvas.drawBitmap(this.bMap, mRescale, this.p);
                        }
                    } else {
                        canvas.drawBitmap(this.bMap, 0.0f, 0.0f, this.p);
                    }
                }
            }
            this.holder.unlockCanvasAndPost(canvas);
            if (this.bMap != null) {
                this.bMap.recycle();
            }
            if (this.p != null) {
                this.p.reset();
            }
        }
    }

    public synchronized void redraw1(byte[] bArrayImage) {
        Canvas canvas = this.holder.lockCanvas();
        if (canvas != null) {
            Paint p = new Paint();
            p.setARGB(CommandEncoder.KEEP_ALIVE, 0, 0, 0);
            new Rect(0, 0, getWidth(), getHeight());
            if (bArrayImage == null) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inDither = true;
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
            } else {
                BitmapFactory.Options opt2 = new BitmapFactory.Options();
                opt2.inDither = true;
                opt2.inPreferredConfig = Bitmap.Config.RGB_565;
                this.currentBitmap = BitmapFactory.decodeByteArray(bArrayImage, 0, bArrayImage.length, opt2);
                if (this.currentBitmap != null) {
                    int currentWidth = ImageUtility.dip2px(getContext(), getScaledWidth(0));
                    int currentHeight = ImageUtility.dip2px(getContext(), getScaledHeight(0));
                    Bitmap adjustBitmap = Bitmap.createBitmap(this.currentBitmap);
                    AppLog.d("shift", "shift viewport:currentWidth:(" + currentWidth + ")");
                    if (currentWidth != this.currentBitmap.getWidth() || currentHeight != this.currentBitmap.getHeight()) {
                        float scaleWidth = currentWidth / this.currentBitmap.getWidth();
                        float scaleHeight = currentHeight / this.currentBitmap.getHeight();
                        Matrix mRescale = new Matrix();
                        mRescale.reset();
                        AppLog.d("shift", "shift viewport:scaleWidth:(" + scaleWidth + ")");
                        AppLog.d("shift", "shift viewport:scaleHeight:(" + scaleHeight + ")");
                        mRescale.postScale(scaleWidth, scaleHeight);
                        adjustBitmap = Bitmap.createBitmap(adjustBitmap, 0, 0, adjustBitmap.getWidth(), adjustBitmap.getHeight(), mRescale, true);
                    }
                    float scale = this.ZOOM[this.targetZoom] / 100.0f;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);
                    matrix.postTranslate(this.sx, this.sy);
                    AppLog.d("shift", "shift viewport:scale:(" + scale + "),t(" + this.sx + "," + this.sy + ")");
                    if (getWidth() <= adjustBitmap.getWidth() && getHeight() <= adjustBitmap.getHeight()) {
                        canvas.drawBitmap(adjustBitmap, matrix, p);
                    } else {
                        canvas.drawBitmap(adjustBitmap, 0.0f, 0.0f, p);
                    }
                }
            }
            this.holder.unlockCanvasAndPost(canvas);
        }
    }
}
