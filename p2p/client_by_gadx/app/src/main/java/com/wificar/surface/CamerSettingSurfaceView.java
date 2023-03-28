package com.wificar.surface;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.rover2.R;
import com.wificar.WificarActivity;
import com.wificar.component.CommandEncoder;
import com.wificar.component.WifiCar;
import com.wificar.util.AppLog;
import com.wificar.util.ImageUtility;
import java.io.IOException;

/* loaded from: classes.dex */
public class CamerSettingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    static CamerSettingSurfaceView instance;
    public float RSmallRockerCircleX;
    public float RSmallRockerCircleY;
    public float RSmallRockerCircleY_1;
    public float RSmallRockerCircleY_2;
    private int b;
    Bitmap background;
    private boolean cameraPressed;
    private int cameramove;
    private boolean controlEnable;
    private boolean enableRun;
    private int f;
    private Handler handler;
    private Handler handler_camer;
    SurfaceHolder holder;
    private Runnable rightMovingTask;
    Bitmap stickBall;
    private final int tStep;
    private WifiCar wifiCar;

    public static CamerSettingSurfaceView getInstance() {
        return instance;
    }

    CamerSettingSurfaceView(Context context) {
        super(context);
        this.handler = new Handler();
        this.handler_camer = new Handler();
        this.RSmallRockerCircleX = 0.0f;
        this.RSmallRockerCircleY = 40.0f;
        this.RSmallRockerCircleY_1 = 65.0f;
        this.RSmallRockerCircleY_2 = 32.0f;
        this.wifiCar = null;
        this.background = null;
        this.stickBall = null;
        this.tStep = 1;
        this.controlEnable = false;
        this.enableRun = false;
        this.cameraPressed = false;
        this.cameramove = 0;
        this.f = 0;
        this.b = 0;
        this.rightMovingTask = new Runnable() { // from class: com.wificar.surface.CamerSettingSurfaceView.1
            @Override // java.lang.Runnable
            public void run() {
                if (CamerSettingSurfaceView.this.controlEnable) {
                    if (CamerSettingSurfaceView.this.cameramove == 0) {
                        AppLog.i("aaaa_sp", "camera stop:" + CamerSettingSurfaceView.this.cameramove);
                        try {
                            CamerSettingSurfaceView.this.wifiCar.disableMoveFlag();
                            CamerSettingSurfaceView.this.wifiCar.camerastop();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (CamerSettingSurfaceView.this.cameramove == 1 && CamerSettingSurfaceView.this.f == 1) {
                        AppLog.i("aaaa_up", "camera up:" + CamerSettingSurfaceView.this.cameramove);
                        try {
                            CamerSettingSurfaceView.this.wifiCar.enableMoveFlag();
                            CamerSettingSurfaceView.this.wifiCar.cameraup();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (CamerSettingSurfaceView.this.cameramove == 2 && CamerSettingSurfaceView.this.b == 1) {
                        AppLog.i("aaaa_down", "camera down:" + CamerSettingSurfaceView.this.cameramove);
                        try {
                            CamerSettingSurfaceView.this.wifiCar.enableMoveFlag();
                            CamerSettingSurfaceView.this.wifiCar.cameradown();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                }
                CamerSettingSurfaceView.this.handler.postDelayed(this, 1L);
            }
        };
        init();
    }

    public CamerSettingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.handler = new Handler();
        this.handler_camer = new Handler();
        this.RSmallRockerCircleX = 0.0f;
        this.RSmallRockerCircleY = 40.0f;
        this.RSmallRockerCircleY_1 = 65.0f;
        this.RSmallRockerCircleY_2 = 32.0f;
        this.wifiCar = null;
        this.background = null;
        this.stickBall = null;
        this.tStep = 1;
        this.controlEnable = false;
        this.enableRun = false;
        this.cameraPressed = false;
        this.cameramove = 0;
        this.f = 0;
        this.b = 0;
        this.rightMovingTask = new Runnable() { // from class: com.wificar.surface.CamerSettingSurfaceView.1
            @Override // java.lang.Runnable
            public void run() {
                if (CamerSettingSurfaceView.this.controlEnable) {
                    if (CamerSettingSurfaceView.this.cameramove == 0) {
                        AppLog.i("aaaa_sp", "camera stop:" + CamerSettingSurfaceView.this.cameramove);
                        try {
                            CamerSettingSurfaceView.this.wifiCar.disableMoveFlag();
                            CamerSettingSurfaceView.this.wifiCar.camerastop();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (CamerSettingSurfaceView.this.cameramove == 1 && CamerSettingSurfaceView.this.f == 1) {
                        AppLog.i("aaaa_up", "camera up:" + CamerSettingSurfaceView.this.cameramove);
                        try {
                            CamerSettingSurfaceView.this.wifiCar.enableMoveFlag();
                            CamerSettingSurfaceView.this.wifiCar.cameraup();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (CamerSettingSurfaceView.this.cameramove == 2 && CamerSettingSurfaceView.this.b == 1) {
                        AppLog.i("aaaa_down", "camera down:" + CamerSettingSurfaceView.this.cameramove);
                        try {
                            CamerSettingSurfaceView.this.wifiCar.enableMoveFlag();
                            CamerSettingSurfaceView.this.wifiCar.cameradown();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                }
                CamerSettingSurfaceView.this.handler.postDelayed(this, 1L);
            }
        };
        instance = this;
        init();
    }

    private void init() {
        this.holder = getHolder();
        this.background = ImageUtility.createBitmap(getResources(), R.drawable.back);
        this.stickBall = ImageUtility.createBitmap(getResources(), R.drawable.stick_back);
        this.holder.addCallback(this);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        AppLog.i("zhangzhangzhang", "surfavechange");
        redraw();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(-2);
        redraw();
        this.handler.postDelayed(this.rightMovingTask, 1L);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void setWifiCar(WifiCar wifiCar) {
        this.wifiCar = wifiCar;
    }

    private void clear(Canvas canvas) {
        if (canvas != null) {
            canvas.drawColor(0);
        }
    }

    public synchronized void redraw() {
        Canvas canvas = this.holder.lockCanvas();
        if (canvas != null) {
            clear(canvas);
            Paint paint = new Paint();
            paint.setAlpha(CommandEncoder.KEEP_ALIVE);
            if (this.controlEnable) {
                if (WificarActivity.getInstance().dimension > 5.8d) {
                    if (WificarActivity.getInstance().with > 850) {
                        if (this.RSmallRockerCircleY != 40.0f) {
                            this.RSmallRockerCircleY -= 18.0f;
                        }
                        canvas.drawBitmap(this.background, 0.0f, 0.0f, paint);
                        canvas.drawBitmap(this.stickBall, 1.0f, this.RSmallRockerCircleY, paint);
                        AppLog.i("1212121", "Y   :" + this.RSmallRockerCircleY);
                    } else {
                        if (this.RSmallRockerCircleY_2 != 32.0f) {
                            this.RSmallRockerCircleY_2 -= 18.0f;
                        }
                        canvas.drawBitmap(this.background, 0.0f, 0.0f, paint);
                        canvas.drawBitmap(this.stickBall, 1.0f, this.RSmallRockerCircleY_2, paint);
                        AppLog.i("1212121", "Y   :" + this.RSmallRockerCircleY_2);
                    }
                } else {
                    if (this.RSmallRockerCircleY_1 != 65.0f) {
                        this.RSmallRockerCircleY_1 -= 18.0f;
                    }
                    canvas.drawBitmap(this.background, 0.0f, 0.0f, paint);
                    canvas.drawBitmap(this.stickBall, 1.0f, this.RSmallRockerCircleY_1, paint);
                    AppLog.i("1212121", "Y111   :" + this.RSmallRockerCircleY_1);
                }
            }
            this.holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (!this.controlEnable) {
            return true;
        }
        int action = event.getAction() & CommandEncoder.KEEP_ALIVE;
        switch (action) {
            case 0:
                AppLog.i("zhangdddd", " the f is : " + this.f);
                AppLog.i("zhangddd11", " the b is : " + this.b);
                float x = event.getX();
                float y = event.getY();
                this.RSmallRockerCircleX = 0.0f;
                this.RSmallRockerCircleY = y;
                this.RSmallRockerCircleY_1 = y;
                this.RSmallRockerCircleY_2 = y;
                if (y > 90.0f) {
                    this.RSmallRockerCircleY = 90.0f;
                }
                if (y < 20.0f) {
                    this.RSmallRockerCircleY = 20.0f;
                }
                if (y > 125.0f) {
                    this.RSmallRockerCircleY_1 = 125.0f;
                }
                if (y < 20.0f) {
                    this.RSmallRockerCircleY_1 = 20.0f;
                }
                if (y > 70.0f) {
                    this.RSmallRockerCircleY_2 = 70.0f;
                }
                if (y < 17.0f) {
                    this.RSmallRockerCircleY_2 = 17.0f;
                }
                if (isLocateAtCameraForwardBoundary(x, y)) {
                    AppLog.i("zhangzhangzhn", "up_up");
                    WificarActivity.getInstance().getWifiCar().enableMoveFlag();
                    this.f++;
                    AppLog.i("zhangdddd__11111", " the f is : " + this.f);
                    if (this.f == 1) {
                        this.cameramove = 1;
                        break;
                    }
                } else if (isLocateAtCameraBackwardBoundary(x, y)) {
                    AppLog.i("zhangzhangzhn", "down_down");
                    WificarActivity.getInstance().getWifiCar().enableMoveFlag();
                    this.b++;
                    AppLog.i("zhangdddd__11111", " the b is : " + this.b);
                    if (this.b == 1) {
                        this.cameramove = 2;
                        break;
                    }
                } else if (isLocateAtCamera(x, y)) {
                    this.b = 0;
                    this.f = 0;
                    this.cameramove = 0;
                    AppLog.i("zhang33333333 ", " the f is : " + this.f);
                    AppLog.i("zhang33333333 ", " the b is : " + this.b);
                    break;
                }
                break;
            case 1:
                this.f = 0;
                this.b = 0;
                this.cameramove = 0;
                AppLog.i("zhangupup", " the f is : " + this.f);
                AppLog.i("zhangupup11", " the b is : " + this.b);
                event.getX();
                event.getY();
                this.RSmallRockerCircleX = 0.0f;
                this.RSmallRockerCircleY = 40.0f;
                this.RSmallRockerCircleY_1 = 65.0f;
                this.RSmallRockerCircleY_2 = 32.0f;
                WificarActivity.getInstance().getWifiCar().disableMoveFlag();
                break;
            case 2:
                AppLog.i("zhangmv", " the f is : " + this.f);
                AppLog.i("zhangmv11", " the b is : " + this.b);
                float mx = event.getX();
                float my = event.getY();
                this.RSmallRockerCircleX = 0.0f;
                this.RSmallRockerCircleY = my;
                this.RSmallRockerCircleY_1 = my;
                this.RSmallRockerCircleY_2 = my;
                if (my > 90.0f) {
                    this.RSmallRockerCircleY = 90.0f;
                }
                if (my < 20.0f) {
                    this.RSmallRockerCircleY = 20.0f;
                }
                if (my > 125.0f) {
                    this.RSmallRockerCircleY_1 = 125.0f;
                }
                if (my < 20.0f) {
                    this.RSmallRockerCircleY_1 = 20.0f;
                }
                if (my > 70.0f) {
                    this.RSmallRockerCircleY_2 = 70.0f;
                }
                if (my < 17.0f) {
                    this.RSmallRockerCircleY_2 = 17.0f;
                }
                if (isLocateAtCameraForwardBoundary(mx, my)) {
                    this.f++;
                    AppLog.i("zhangdddd__22222", " the f is : " + this.f);
                    AppLog.i("zhangzhangzhn", "moveup_up");
                    WificarActivity.getInstance().getWifiCar().enableMoveFlag();
                    if (this.f == 1) {
                        this.cameramove = 1;
                        break;
                    }
                } else if (isLocateAtCameraBackwardBoundary(mx, my)) {
                    AppLog.i("zhangzhangzhn", "movedown_down");
                    this.b++;
                    AppLog.i("zhangdddd__2222222", " the b is : " + this.b);
                    WificarActivity.getInstance().getWifiCar().enableMoveFlag();
                    if (this.b == 1) {
                        this.cameramove = 2;
                        break;
                    }
                } else if (isLocateAtCamera(mx, my)) {
                    this.b = 0;
                    this.f = 0;
                    AppLog.i("zhang2323 ", " the f is : " + this.f);
                    AppLog.i("zhang23232 ", " the b is : " + this.b);
                    break;
                }
                break;
            case CommandEncoder.VIDEO_END /* 6 */:
                this.f = 0;
                this.b = 0;
                this.cameramove = 0;
                AppLog.i("zhangup", " the f is : " + this.f);
                AppLog.i("zhangup11", " the b is : " + this.b);
                event.getX();
                event.getY();
                this.RSmallRockerCircleX = 0.0f;
                this.RSmallRockerCircleY = 40.0f;
                this.RSmallRockerCircleY_1 = 65.0f;
                this.RSmallRockerCircleY_2 = 32.0f;
                WificarActivity.getInstance().getWifiCar().disableMoveFlag();
                try {
                    this.wifiCar.camerastop();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
        }
        redraw();
        return true;
    }

    private boolean isLocateAtCameraForwardBoundary(float x, float y) {
        AppLog.i("aaaa222222222222F", "x  , y " + x + " ," + y);
        return x > -10.0f && x < 80.0f && y < 45.0f && y > -80.0f;
    }

    private boolean isLocateAtCameraBackwardBoundary(float x, float y) {
        AppLog.i("aaaa222222222222222B", "x  , y " + x + " ," + y);
        return x > -50.0f && x < 80.0f && y > 70.0f && y < 240.0f;
    }

    private boolean isLocateAtCamera(float x, float y) {
        AppLog.i("aaaa33333333333", "x  , y " + x + " ," + y);
        return x > -50.0f && x < 80.0f && y > 45.0f && y < 70.0f;
    }

    public static Bitmap createBitmap(Resources res, int srcId) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, srcId);
        return bitmap;
    }

    public void disableControl() {
        this.controlEnable = false;
        setVisibility(4);
        AppLog.i("zhang11", "setting controldisable");
        redraw();
    }

    public void enableControl() {
        this.controlEnable = true;
        setVisibility(0);
        AppLog.i("zhang", "setting controlenable");
        redraw();
    }
}
