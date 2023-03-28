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
public class AppLeft_MoveCar extends View {
    public static final int ACTION_ATTACK_CAMERAMOVE = 4;
    public static final int ACTION_ATTACK_DEVICEMOVE = 2;
    public static final int ACTION_RUDDER = 1;
    public static final int ACTION_STOP = 3;
    public static final String TAG = "AppLeft_MoveCar";
    public static AppLeft_MoveCar appLeft_MoveCar;
    public int OutLeftCircle;
    public int OutLocalCircle;
    public Context context;
    int diff_x;
    public int flag;
    int isHide;
    public boolean isLPressed;
    private LMovingThread lMovingThrad;
    public AppLeft_MoveCarListener listener;
    public Paint mPaint;
    public int statkBall_halfWidth;
    public Bitmap stickBall;
    public int stickBall_Local;
    public int stickBall_width;
    public int stickBar_halfWidth;
    public int stickBar_height;
    public int stickBar_maxWidth;

    /* loaded from: classes.dex */
    public interface AppLeft_MoveCarListener {
        void onSteeringWheelChanged(int i, int i2);
    }

    private AppLeft_MoveCar getAppLR_Move() {
        return appLeft_MoveCar;
    }

    public void setAppLeft_MoveCarListener(AppLeft_MoveCarListener rockerListener) {
        this.listener = rockerListener;
    }

    public AppLeft_MoveCar(Context context) {
        super(context);
        this.stickBar_height = 180;
        this.stickBall_width = 50;
        this.stickBar_halfWidth = 90;
        this.isHide = 0;
        this.isLPressed = false;
        this.lMovingThrad = null;
        this.listener = null;
        this.flag = 0;
        this.OutLeftCircle = 1;
        this.OutLocalCircle = 1;
        this.context = context;
        appLeft_MoveCar = this;
        this.stickBar_height = WificarActivity.Car_Move_Progress_Height;
        this.stickBall_width = WificarActivity.Car_Move_Progress_Width;
        int i = (this.stickBar_height - this.stickBall_width) / 2;
        this.stickBar_halfWidth = i;
        this.stickBall_Local = i;
        this.stickBar_maxWidth = this.stickBar_height - this.stickBall_width;
        this.statkBall_halfWidth = this.stickBall_width / 2;
        this.mPaint = new Paint();
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setAntiAlias(true);
        setBackgroundResource(R.drawable.control_circle_right);
        AppLog.e(TAG, "leftstickBar_height:" + this.stickBar_height + " stickBall_width:" + this.stickBall_width + " stickBar_halfWidth:" + this.stickBar_halfWidth);
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
                        this.isLPressed = true;
                        break;
                    case 1:
                        this.isLPressed = false;
                        if (pointerCount == 1) {
                            this.OutLocalCircle = 1;
                            init();
                            this.OutLeftCircle = 1;
                            AppRight_MoveCar.appRight_MoveCar.init();
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
                                    this.stickBall_Local = ((int) event.getY(id)) - this.statkBall_halfWidth;
                                    this.isLPressed = true;
                                    Check();
                                    this.OutLocalCircle = 0;
                                } else {
                                    this.OutLocalCircle = 1;
                                }
                            } else if (id == 1) {
                                int x12 = (x1 - this.diff_x) - this.statkBall_halfWidth;
                                if (x12 > 0 && x12 <= this.stickBall_width && y1 > 0 && y1 <= this.stickBar_height) {
                                    AppRight_MoveCar.appRight_MoveCar.stickBar_Local = y1;
                                    AppRight_MoveCar.appRight_MoveCar.isRPressed = true;
                                    AppRight_MoveCar.appRight_MoveCar.check();
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
                            AppRight_MoveCar.appRight_MoveCar.init();
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
                    this.stickBall_Local = my - this.statkBall_halfWidth;
                    Check();
                } else if (id == 1 && this.OutLeftCircle == 0) {
                    int mx2 = (mx - this.diff_x) - this.statkBall_halfWidth;
                    if (my < 0) {
                        my = 0;
                    }
                    if (my > this.stickBar_height) {
                        my = this.stickBar_height;
                    }
                    AppRight_MoveCar.appRight_MoveCar.stickBar_Local = my;
                    AppRight_MoveCar.appRight_MoveCar.isRPressed = true;
                    AppRight_MoveCar.appRight_MoveCar.check();
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

    /* loaded from: classes.dex */
    private class LMovingThread extends Thread {
        private LMovingThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            synchronized (this) {
                if (AppLeft_MoveCar.this.isLPressed) {
                    AppLog.i("", "--L-->left running");
                    if (AppLeft_MoveCar.this.stickBall_Local > AppLeft_MoveCar.this.stickBar_maxWidth) {
                        AppLeft_MoveCar.this.stickBall_Local = AppLeft_MoveCar.this.stickBar_maxWidth;
                    } else if (AppLeft_MoveCar.this.stickBall_Local < 0) {
                        AppLeft_MoveCar.this.stickBall_Local = 0;
                    }
                    if (AppLeft_MoveCar.this.stickBall_Local <= AppLeft_MoveCar.this.stickBar_halfWidth) {
                        AppLeft_MoveCar.this.flag = 1;
                        AppLeft_MoveCar.this.listener.onSteeringWheelChanged(1, AppLeft_MoveCar.this.flag);
                    } else if (AppLeft_MoveCar.this.stickBall_Local >= AppLeft_MoveCar.this.stickBar_halfWidth) {
                        AppLeft_MoveCar.this.flag = 2;
                        AppLeft_MoveCar.this.listener.onSteeringWheelChanged(1, AppLeft_MoveCar.this.flag);
                    }
                    AppLeft_MoveCar.this.Canvas_OK();
                }
            }
        }
    }

    public void Check() {
        if (this.stickBall_Local > this.stickBar_maxWidth) {
            this.stickBall_Local = this.stickBar_maxWidth;
        } else if (this.stickBall_Local < 0) {
            this.stickBall_Local = 0;
        }
        if (this.stickBall_Local <= this.stickBar_halfWidth) {
            this.flag = 1;
            this.listener.onSteeringWheelChanged(1, this.flag);
        } else if (this.stickBall_Local >= this.stickBar_halfWidth) {
            this.flag = 2;
            this.listener.onSteeringWheelChanged(1, this.flag);
        }
        Canvas_OK();
    }

    public void Canvas_OK() {
        invalidate();
    }

    public void init() {
        this.isLPressed = false;
        this.flag = 0;
        this.stickBall_Local = this.stickBar_halfWidth;
        requestLayout();
        Canvas_OK();
        this.listener.onSteeringWheelChanged(3, this.flag);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.stickBall != null && this.isHide == 0) {
            canvas.drawBitmap(this.stickBall, 0.0f, this.stickBall_Local, this.mPaint);
        }
        super.onDraw(canvas);
    }
}
