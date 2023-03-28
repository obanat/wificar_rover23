package com.wificar.surface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.rover2.R;
import com.wificar.WificarActivity;
import com.wificar.component.CommandEncoder;
import com.wificar.component.WifiCar;
import com.wificar.util.AppLog;
import java.io.IOException;

/* loaded from: classes.dex */
public class Camera_UD_SurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = "Camera_UD_SurfaceView";
    public int OutLeftCircle;
    public int OutLocalCircle;
    public Bitmap cameraBall;
    public Bitmap cameraBar;
    public int flag;
    SurfaceHolder holder;
    int isHide;
    public Paint mPaint;
    public int statkBall_halfWidth;
    public int stickBall_Local;
    public int stickBall_width;
    public int stickBar_halfWidth;
    public int stickBar_height;
    public int stickBar_maxWidth;
    private WifiCar wifiCar;

    public Camera_UD_SurfaceView(Context context) {
        super(context);
        this.wifiCar = null;
        this.isHide = 0;
        this.stickBar_height = 180;
        this.stickBall_width = 50;
        this.stickBar_halfWidth = 90;
        this.flag = 0;
        this.OutLeftCircle = 1;
        this.OutLocalCircle = 1;
        initial();
    }

    public Camera_UD_SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.wifiCar = null;
        this.isHide = 0;
        this.stickBar_height = 180;
        this.stickBall_width = 50;
        this.stickBar_halfWidth = 90;
        this.flag = 0;
        this.OutLeftCircle = 1;
        this.OutLocalCircle = 1;
        initial();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        redraw();
    }

    public void initial() {
        this.holder = getHolder();
        this.stickBar_height = WificarActivity.Car_Camera_Progress_Height;
        this.stickBall_width = WificarActivity.Car_Camera_Progress_Width;
        int i = (this.stickBar_height - this.stickBall_width) / 2;
        this.stickBar_halfWidth = i;
        this.stickBall_Local = i;
        this.stickBar_maxWidth = this.stickBar_height - this.stickBall_width;
        this.statkBall_halfWidth = this.stickBall_width / 2;
        this.mPaint = new Paint();
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setAntiAlias(true);
        this.cameraBar = BitmapFactory.decodeResource(getResources(), R.drawable.back);
        this.cameraBar = Bitmap.createScaledBitmap(this.cameraBar, this.stickBall_width, this.stickBar_height, true);
        this.cameraBall = BitmapFactory.decodeResource(getResources(), R.drawable.stick_back);
        this.cameraBall = Bitmap.createScaledBitmap(this.cameraBall, this.stickBall_width, this.stickBall_width, true);
        this.holder.addCallback(this);
    }

    public synchronized void redraw() {
        Canvas canvas = this.holder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            this.mPaint.setAlpha(CommandEncoder.KEEP_ALIVE);
            if (this.cameraBall != null && this.isHide == 0) {
                canvas.drawBitmap(this.cameraBar, 0.0f, 0.0f, this.mPaint);
                canvas.drawBitmap(this.cameraBall, 0.0f, this.stickBall_Local, this.mPaint);
            }
            this.holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(-2);
        redraw();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void setWifiCar(WifiCar wifiCar) {
        this.wifiCar = wifiCar;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.isHide == 0 && WificarActivity.isTop) {
            int pointerIndex = (event.getAction() & 65280) >> 8;
            int pointerId = event.getPointerId(pointerIndex);
            int action = event.getActionMasked();
            int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                int id = event.getPointerId(i);
                switch (action) {
                    case 0:
                        this.OutLocalCircle = 0;
                        break;
                    case 1:
                        if (pointerCount == 1) {
                            this.OutLocalCircle = 1;
                            init();
                            this.OutLeftCircle = 1;
                        } else {
                            continue;
                        }
                    case 2:
                        break;
                    case 3:
                    case 4:
                    case CommandEncoder.VIDEO_START_RESP /* 5 */:
                    default:
                    case CommandEncoder.VIDEO_END /* 6 */:
                        if (pointerId != id) {
                            continue;
                        } else if (id == 0) {
                            this.OutLocalCircle = 1;
                            init();
                        }
                }
                AppLog.e(TAG, "ACTION_MOVE");
                int mx = (int) event.getX();
                int my = (int) event.getY();
                if (id == 0 && this.OutLocalCircle == 0) {
                    this.stickBall_Local = my - this.statkBall_halfWidth;
                    Check();
                }
            }
            try {
                Thread.sleep(30L);
            } catch (Exception e){

            }
            redraw();
            return true;
        }
        try {
            Thread.sleep(200L);
        } catch (Exception e){

        }
        return false;
    }

    public void Check() {
        AppLog.e(TAG, "Check()");
        if (this.stickBall_Local > this.stickBar_maxWidth) {
            this.stickBall_Local = this.stickBar_maxWidth;
        } else if (this.stickBall_Local < 0) {
            this.stickBall_Local = 0;
        }
        if (this.stickBall_Local <= this.stickBar_halfWidth) {
            this.flag = 1;
            AppLog.e(TAG, "镜头上");
            try {
                this.wifiCar.cameraup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this.stickBall_Local >= this.stickBar_halfWidth) {
            this.flag = 2;
            AppLog.e(TAG, "镜头下");
            try {
                this.wifiCar.cameradown();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        Canvas_OK();
    }

    public void Canvas_OK() {
        invalidate();
        redraw();
    }

    public void init() {
        this.flag = 0;
        this.stickBall_Local = this.stickBar_halfWidth;
        requestLayout();
        Canvas_OK();
        AppLog.e(TAG, "镜头停止");
        try {
            this.wifiCar.camerastop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disableControl() {
        setVisibility(4);
        redraw();
    }

    public void enableControl() {
        setVisibility(0);
        redraw();
    }
}
