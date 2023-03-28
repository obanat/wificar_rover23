package com.wificar.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.rover2.R;
import com.wificar.WificarActivity;
import com.wificar.component.CommandEncoder;
import com.wificar.util.AppLog;

/* loaded from: classes.dex */
public class AppCamera_UD extends View {
    public static final int ACTION_ATTACK_CAMERAMOVE = 4;
    public static final int ACTION_ATTACK_DEVICEMOVE = 2;
    public static final int ACTION_RUDDER = 1;
    public static final int ACTION_STOP = 3;
    public static final String TAG = "AppCamera_UD";
    public static AppCamera_UD appCamera_UD;
    public int OutLeftCircle;
    public int OutLocalCircle;
    public Context context;
    int diff_x;
    public int flag;
    int isHide;
    public AppCamera_UDListener listener;
    public Paint mPaint;
    public int statkBall_halfWidth;
    public Bitmap stickBall;
    public int stickBall_Local;
    public int stickBall_width;
    public Bitmap stickBar;
    public int stickBar_halfWidth;
    public int stickBar_height;
    public int stickBar_maxWidth;

    /* loaded from: classes.dex */
    public interface AppCamera_UDListener {
        void onSteeringWheelChanged(int i, int i2);
    }

    public AppCamera_UD getAppLR_Move() {
        return appCamera_UD;
    }

    public AppCamera_UD(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.stickBar_height = 180;
        this.stickBall_width = 50;
        this.stickBar_halfWidth = 90;
        this.isHide = 0;
        this.listener = null;
        this.flag = 0;
        this.OutLeftCircle = 1;
        this.OutLocalCircle = 1;
        this.context = context;
    }

    public void setAppCamera_UDListener(AppCamera_UDListener rockerListener) {
        this.listener = rockerListener;
    }

    public AppCamera_UD(Context context) {
        super(context);
        this.stickBar_height = 180;
        this.stickBall_width = 50;
        this.stickBar_halfWidth = 90;
        this.isHide = 0;
        this.listener = null;
        this.flag = 0;
        this.OutLeftCircle = 1;
        this.OutLocalCircle = 1;
        this.context = context;
        appCamera_UD = this;
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
        AppLog.e(TAG, "stickBar_height:" + this.stickBar_height + " stickBall_width:" + this.stickBall_width + " stickBar_halfWidth:" + this.stickBar_halfWidth);
        this.stickBar = BitmapFactory.decodeResource(getResources(), R.drawable.back);
        this.stickBar = Bitmap.createScaledBitmap(this.stickBar, this.stickBall_width, this.stickBar_height, true);
        this.stickBall = BitmapFactory.decodeResource(getResources(), R.drawable.stick_back);
        this.stickBall = Bitmap.createScaledBitmap(this.stickBall, this.stickBall_width, this.stickBall_width, true);
        this.diff_x = WificarActivity.UD_Diff_x;
    }

    public void initValue() {
    }

    public void setHided(int opt) {
        this.isHide = opt;
    }

    public int getIsHided() {
        return this.isHide;
    }

    public void Hided(int opt) {
        this.isHide = opt;
        AppLog.e(TAG, "isHide:" + this.isHide);
        if (opt == 1) {
            appCamera_UD.setVisibility(4);
        } else {
            appCamera_UD.setVisibility(0);
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
                int my = 0;
                if (event.getPointerCount() == 1) {
                    int mx = (int) event.getX();
                    my = (int) event.getY();
                } else if (event.getPointerCount() == 2) {
                    int mx2 = (int) event.getX(id);
                    my = (int) event.getY(id);
                }
                if (id == 0 && this.OutLocalCircle == 0) {
                    this.stickBall_Local = my - this.statkBall_halfWidth;
                    Check();
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
        this.flag = 0;
        this.stickBall_Local = this.stickBar_halfWidth;
        requestLayout();
        Canvas_OK();
        this.listener.onSteeringWheelChanged(3, this.flag);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.stickBall != null && this.isHide == 0) {
            canvas.drawBitmap(this.stickBar, 0.0f, 0.0f, this.mPaint);
            canvas.drawBitmap(this.stickBall, 0.0f, this.stickBall_Local, this.mPaint);
        }
        super.onDraw(canvas);
    }
}
