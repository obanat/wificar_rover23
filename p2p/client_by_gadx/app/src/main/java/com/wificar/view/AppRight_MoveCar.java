package com.wificar.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import com.rover2.R;
import com.wificar.WificarActivity;
import com.wificar.component.CommandEncoder;
import com.wificar.util.AppLog;

/* loaded from: classes.dex */
public class AppRight_MoveCar extends View {
    public static final int ACTION_ATTACK_CAMERAMOVE = 4;
    public static final int ACTION_ATTACK_DEVICEMOVE = 2;
    public static final int ACTION_RUDDER = 1;
    public static final int ACTION_STOP = 3;
    public static final String TAG = "AppRight_MoveCar";
    public static AppRight_MoveCar appRight_MoveCar = null;
    public int OutLeftCircle;
    public int OutLocalCircle;
    public Context context;
    int diff_x;
    public int flag;
    int isHide;
    public boolean isRPressed;
    public AppRight_MoveCarListener listener;
    public Paint mPaint;
    private RMovingThread rMovingThread;
    public int statkBall_halfWidth;
    public Bitmap stickBall;
    public int stickBall_width;
    public int stickBar_Local;
    public int stickBar_halfHeight;
    public int stickBar_height;
    public int stickBar_maxHeight;

    /* loaded from: classes.dex */
    public interface AppRight_MoveCarListener {
        void onSteeringWheelChanged(int i, int i2);
    }

    private AppRight_MoveCar getAppUD_MoveCar() {
        return appRight_MoveCar;
    }

    public void setAppRight_MoveCarListener(AppRight_MoveCarListener rockerListener) {
        this.listener = rockerListener;
    }

    public AppRight_MoveCar(Context context) {
        super(context);
        this.stickBar_height = 180;
        this.stickBall_width = 50;
        this.stickBar_halfHeight = 90;
        this.isHide = 0;
        this.isRPressed = false;
        this.rMovingThread = null;
        this.listener = null;
        this.flag = 0;
        this.OutLeftCircle = 1;
        this.OutLocalCircle = 1;
        this.context = context;
        appRight_MoveCar = this;
        this.stickBar_height = WificarActivity.Car_Move_Progress_Height;
        this.stickBall_width = WificarActivity.Car_Move_Progress_Width;
        int i = (this.stickBar_height - this.stickBall_width) / 2;
        this.stickBar_halfHeight = i;
        this.stickBar_Local = i;
        this.stickBar_maxHeight = this.stickBar_height - this.stickBall_width;
        this.statkBall_halfWidth = this.stickBall_width / 2;
        this.mPaint = new Paint();
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setAntiAlias(true);
        setBackgroundResource(R.drawable.control_circle_right);
        AppLog.e(TAG, "rightstickBar_height:" + this.stickBar_height + " stickBall_width:" + this.stickBall_width + " stickBar_halfHeight:" + this.stickBar_halfHeight);
        this.stickBall = BitmapFactory.decodeResource(getResources(), R.drawable.joy_stick);
        this.stickBall = Bitmap.createScaledBitmap(this.stickBall, this.stickBall_width, this.stickBall_width, true);
        this.diff_x = WificarActivity.UD_Diff_x;
    }

    public void setHided(int opt) {
        this.isHide = opt;
    }

    public int getIsHided() {
        return this.isHide;
    }

    public void Hided(int opt) {
        AppLog.e(TAG, "isHide:" + this.isHide);
        this.isHide = opt;
        if (opt == 1) {
            getBackground().setAlpha(0);
        } else {
            getBackground().setAlpha(CommandEncoder.KEEP_ALIVE);
        }
        Canvas_OK();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
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
                        this.isRPressed = true;
                        break;
                    case 1:
                        this.isRPressed = false;
                        if (pointerCount == 1) {
                            this.OutLocalCircle = 0;
                            init();
                            this.OutLeftCircle = 1;
                            AppLeft_MoveCar.appLeft_MoveCar.init();
                        } else {
                            continue;
                        }
                    case 2:
                        break;
                    case 3:
                    case 4:
                    default:
                    case CommandEncoder.VIDEO_START_RESP /* 5 */:
                        if (pointerId == id) {
                            int x1 = (int) event.getX(id);
                            int y1 = (int) event.getY(id);
                            if (id == 0) {
                                if (x1 >= 0 && x1 <= this.stickBall_width && y1 > 0 && y1 <= this.stickBar_height) {
                                    this.stickBar_Local = ((int) event.getY(id)) - this.statkBall_halfWidth;
                                    this.isRPressed = true;
                                    check();
                                    this.OutLocalCircle = 0;
                                } else {
                                    this.OutLocalCircle = 1;
                                }
                            } else if (id == 1) {
                                int x12 = this.diff_x + x1 + this.statkBall_halfWidth;
                                if (x12 > 0 && x12 <= this.stickBall_width && y1 > 0 && y1 <= this.stickBar_height) {
                                    AppLeft_MoveCar.appLeft_MoveCar.stickBall_Local = y1;
                                    AppLeft_MoveCar.appLeft_MoveCar.isLPressed = true;
                                    AppLeft_MoveCar.appLeft_MoveCar.Check();
                                    this.OutLeftCircle = 0;
                                } else {
                                    this.OutLeftCircle = 1;
                                }
                            }
                        } else {
                            continue;
                        }
                        break;
                    case CommandEncoder.VIDEO_END /* 6 */:
                        if (pointerId != id) {
                            continue;
                        } else if (id == 0) {
                            this.OutLocalCircle = 1;
                            init();
                        } else if (id == 1) {
                            this.OutLeftCircle = 1;
                            AppLeft_MoveCar.appLeft_MoveCar.init();
                        }
                }
                int mx = 0;
                int my = 0;
                if (event.getPointerCount() == 1) {
                    mx = (int) event.getX();
                    my = (int) event.getY();
                } else if (event.getPointerCount() == 2) {
                    mx = (int) event.getX(id);
                    my = (int) event.getY(id);
                }
                if (id == 0 && this.OutLocalCircle == 0) {
                    this.stickBar_Local = my - this.statkBall_halfWidth;
                    check();
                } else if (id == 1 && this.OutLeftCircle == 0) {
                    int i2 = mx + this.diff_x;
                    if (my < 0) {
                        my = 0;
                    }
                    if (my > this.stickBar_height) {
                        my = this.stickBar_height;
                    }
                    AppLeft_MoveCar.appLeft_MoveCar.stickBall_Local = my;
                    AppLeft_MoveCar.appLeft_MoveCar.isLPressed = true;
                    AppLeft_MoveCar.appLeft_MoveCar.Check();
                }
            }
            try {
                Thread.sleep(30L);
            } catch (Exception e){

            }
            return true;
        }
        try {
            Thread.sleep(200L);
        } catch (Exception e){

        }
        return false;
    }

    public void Canvas_OK() {
        invalidate();
    }

    public void init() {
        this.isRPressed = false;
        this.flag = 0;
        this.stickBar_Local = this.stickBar_halfHeight;
        requestLayout();
        Canvas_OK();
        this.listener.onSteeringWheelChanged(3, this.flag);
    }

    /* loaded from: classes.dex */
    private class RMovingThread extends Thread {
        private RMovingThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            synchronized (this) {
                if (AppRight_MoveCar.this.isRPressed) {
                    AppLog.i("", "--R-->right running");
                    if (AppRight_MoveCar.this.stickBar_Local > AppRight_MoveCar.this.stickBar_maxHeight) {
                        AppRight_MoveCar.this.stickBar_Local = AppRight_MoveCar.this.stickBar_maxHeight;
                    } else if (AppRight_MoveCar.this.stickBar_Local < 0) {
                        AppRight_MoveCar.this.stickBar_Local = 0;
                    }
                    if (AppRight_MoveCar.this.stickBar_Local <= AppRight_MoveCar.this.stickBar_halfHeight) {
                        AppRight_MoveCar.this.flag = 1;
                        AppRight_MoveCar.this.listener.onSteeringWheelChanged(1, AppRight_MoveCar.this.flag);
                    } else if (AppRight_MoveCar.this.stickBar_Local > AppRight_MoveCar.this.stickBar_halfHeight) {
                        AppRight_MoveCar.this.flag = 2;
                        AppRight_MoveCar.this.listener.onSteeringWheelChanged(1, AppRight_MoveCar.this.flag);
                    }
                    AppRight_MoveCar.this.Canvas_OK();
                }
            }
        }
    }

    public void check() {
        if (this.stickBar_Local > this.stickBar_maxHeight) {
            this.stickBar_Local = this.stickBar_maxHeight;
        } else if (this.stickBar_Local < 0) {
            this.stickBar_Local = 0;
        }
        if (this.stickBar_Local <= this.stickBar_halfHeight) {
            this.flag = 1;
            this.listener.onSteeringWheelChanged(1, this.flag);
        } else if (this.stickBar_Local > this.stickBar_halfHeight) {
            this.flag = 2;
            this.listener.onSteeringWheelChanged(1, this.flag);
        }
        Canvas_OK();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.stickBall != null && this.isHide == 0) {
            canvas.drawBitmap(this.stickBall, 0.0f, this.stickBar_Local, this.mPaint);
        }
        super.onDraw(canvas);
    }
}
