package com.wificar.surface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.rover2.R;
import com.wificar.WificarActivity;
import com.wificar.component.CommandEncoder;
import com.wificar.component.WifiCar;
import com.wificar.util.ImageUtility;
import java.io.IOException;

/* loaded from: classes.dex */
public class DoubleAxisLeftControllerSurfaceView extends SurfaceView implements SurfaceHolder.Callback, ControllerInterface {
    private static final int MAX = 10;
    static DoubleAxisLeftControllerSurfaceView instance;
    private int areaBoundHeight;
    private int areaBoundWidth;
    private boolean captureLeftBall;
    private boolean captureRightBall;
    private boolean controlEnable;
    private Handler handler;
    private int height;
    SurfaceHolder holder;
    private int iCarSpeedL;
    private int iCarSpeedR;
    private int iLastSpeedL;
    private int iLastSpeedR;
    int leftIndexId;
    private Runnable leftMovingTask;
    private int leftStartPointX;
    private int leftStartPointY;
    int maxPointIndex;
    int rightIndexId;
    private int rightStartPointX;
    private int rightStartPointY;
    private double size;
    Bitmap stickBall;
    private int stickBallHeight;
    private int stickBallMotionAreaHeight;
    private int stickBallMotionAreaHeightLarge;
    private int stickBallMotionAreaHeightSmall;
    private int stickBallMotionAreaHeightxLarge;
    private int stickBallMotionAreaHeightxxLarge;
    private int stickBallMotionAreaWidth;
    private int stickBallPointBaseLargeY;
    private int stickBallPointBaseSmallY;
    private int stickBallPointBaseX;
    private int stickBallPointBaseY;
    private int stickBallPointBasexLargeY;
    private int stickBallPointBasexxLargeY;
    private int stickBallPointLargeY;
    private int stickBallPointSmallY;
    private int stickBallPointX;
    private int stickBallPointY;
    private int stickBallPointxLargeY;
    private int stickBallPointxxLargeY;
    private int stickBallWidth;
    Bitmap stickBar;
    private int stickBarPointLargeY;
    private int stickBarPointSmallY;
    private int stickBarPointTopY;
    private int stickBarPointX;
    private int stickBarPointY;
    private int stickBarPointxLargeY;
    private int stickBarPointxxLargeY;
    Bitmap stickDown;
    Bitmap stickDownPress;
    Bitmap stickUp;
    Bitmap stickUpPress;
    private int stickValue;
    private int stopLeftSignal;
    private int stopRightSignal;
    private final int tStep;
    private int waitStopState;
    private int width;
    private WifiCar wifiCar;

    public static DoubleAxisLeftControllerSurfaceView getInstance() {
        return instance;
    }

    @Override // com.wificar.surface.ControllerInterface
    public void initial() {
        this.size = WificarActivity.getInstance().dimension;
        this.width = WificarActivity.getInstance().with;
        this.height = WificarActivity.getInstance().hight;
        this.holder = getHolder();
        this.stickBar = ImageUtility.createBitmap(getResources(), R.drawable.control_circle_right);
        this.stickBall = ImageUtility.createBitmap(getResources(), R.drawable.joy_stick);
        if (this.size < 5.8d) {
            Log.i("Double", "180X40");
            this.stickBar = Bitmap.createScaledBitmap(this.stickBar, ImageUtility.dip2px(getContext(), 40.0f), ImageUtility.dip2px(getContext(), 180.0f), true);
            this.stickBall = Bitmap.createScaledBitmap(this.stickBall, ImageUtility.dip2px(getContext(), 40.0f), ImageUtility.dip2px(getContext(), 40.0f), true);
        } else {
            Log.i("Double", "225X48");
            this.stickBall = Bitmap.createScaledBitmap(this.stickBall, ImageUtility.dip2px(getContext(), 48.0f), ImageUtility.dip2px(getContext(), 48.0f), true);
            this.stickBar = Bitmap.createScaledBitmap(this.stickBar, ImageUtility.dip2px(getContext(), 48.0f), ImageUtility.dip2px(getContext(), 225.0f), true);
        }
        this.holder.addCallback(this);
    }

    public DoubleAxisLeftControllerSurfaceView(Context context) {
        super(context);
        this.rightStartPointX = 20;
        this.rightStartPointY = 20;
        this.areaBoundWidth = 20;
        this.areaBoundHeight = 20;
        this.leftStartPointX = 20;
        this.leftStartPointY = 20;
        this.stickBallPointX = 10;
        this.stickBallPointY = 100;
        this.stickBallPointSmallY = 70;
        this.stickBallPointxLargeY = 85;
        this.stickBallPointxxLargeY = 130;
        this.stickBallPointLargeY = 65;
        this.stickBallWidth = 60;
        this.stickBallHeight = 40;
        this.stickBallMotionAreaWidth = 100;
        this.stickBallMotionAreaHeight = 230;
        this.stickBallMotionAreaHeightSmall = 160;
        this.stickBallMotionAreaHeightLarge = 150;
        this.stickBallMotionAreaHeightxLarge = 195;
        this.stickBallMotionAreaHeightxxLarge = 300;
        this.stickBarPointX = 10;
        this.stickBarPointY = 0;
        this.stickBarPointSmallY = 0;
        this.stickBarPointxLargeY = 0;
        this.stickBarPointLargeY = 0;
        this.stickBarPointxxLargeY = 0;
        this.stickBarPointTopY = 20;
        this.stickBallPointBaseX = 10;
        this.stickBallPointBaseY = 100;
        this.stickBallPointBaseSmallY = 70;
        this.stickBallPointBasexLargeY = 85;
        this.stickBallPointBasexxLargeY = 130;
        this.stickBallPointBaseLargeY = 65;
        this.captureLeftBall = false;
        this.captureRightBall = false;
        this.size = 0.0d;
        this.maxPointIndex = 2;
        this.leftIndexId = -1;
        this.rightIndexId = -1;
        this.handler = new Handler();
        this.wifiCar = null;
        this.controlEnable = true;
        this.tStep = 100;
        this.iCarSpeedR = 0;
        this.iLastSpeedR = 0;
        this.iCarSpeedL = 0;
        this.iLastSpeedL = 0;
        this.stopLeftSignal = 0;
        this.stopRightSignal = 0;
        this.waitStopState = 0;
        this.stickValue = 0;
        this.stickBar = null;
        this.stickBall = null;
        this.stickUp = null;
        this.stickDown = null;
        this.stickUpPress = null;
        this.stickDownPress = null;
        this.leftMovingTask = new Runnable() { // from class: com.wificar.surface.DoubleAxisLeftControllerSurfaceView.1
            @Override // java.lang.Runnable
            public void run() {
                if (DoubleAxisLeftControllerSurfaceView.this.controlEnable) {
                    if (DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL != 0) {
                        Log.d("move", "Run left(" + DoubleAxisLeftControllerSurfaceView.this.controlEnable + "):" + DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL);
                        try {
                            DoubleAxisLeftControllerSurfaceView.this.wifiCar.move(0, DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DoubleAxisLeftControllerSurfaceView.this.iLastSpeedL = DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL;
                    }
                    if (DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL == 0 && DoubleAxisLeftControllerSurfaceView.this.iLastSpeedL != 0) {
                        try {
                            DoubleAxisLeftControllerSurfaceView.this.wifiCar.move(0, DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        DoubleAxisLeftControllerSurfaceView.this.iLastSpeedL = 0;
                    }
                }
                DoubleAxisLeftControllerSurfaceView.this.handler.postDelayed(this, 100L);
            }
        };
        instance = this;
        initial();
    }

    public DoubleAxisLeftControllerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.rightStartPointX = 20;
        this.rightStartPointY = 20;
        this.areaBoundWidth = 20;
        this.areaBoundHeight = 20;
        this.leftStartPointX = 20;
        this.leftStartPointY = 20;
        this.stickBallPointX = 10;
        this.stickBallPointY = 100;
        this.stickBallPointSmallY = 70;
        this.stickBallPointxLargeY = 85;
        this.stickBallPointxxLargeY = 130;
        this.stickBallPointLargeY = 65;
        this.stickBallWidth = 60;
        this.stickBallHeight = 40;
        this.stickBallMotionAreaWidth = 100;
        this.stickBallMotionAreaHeight = 230;
        this.stickBallMotionAreaHeightSmall = 160;
        this.stickBallMotionAreaHeightLarge = 150;
        this.stickBallMotionAreaHeightxLarge = 195;
        this.stickBallMotionAreaHeightxxLarge = 300;
        this.stickBarPointX = 10;
        this.stickBarPointY = 0;
        this.stickBarPointSmallY = 0;
        this.stickBarPointxLargeY = 0;
        this.stickBarPointLargeY = 0;
        this.stickBarPointxxLargeY = 0;
        this.stickBarPointTopY = 20;
        this.stickBallPointBaseX = 10;
        this.stickBallPointBaseY = 100;
        this.stickBallPointBaseSmallY = 70;
        this.stickBallPointBasexLargeY = 85;
        this.stickBallPointBasexxLargeY = 130;
        this.stickBallPointBaseLargeY = 65;
        this.captureLeftBall = false;
        this.captureRightBall = false;
        this.size = 0.0d;
        this.maxPointIndex = 2;
        this.leftIndexId = -1;
        this.rightIndexId = -1;
        this.handler = new Handler();
        this.wifiCar = null;
        this.controlEnable = true;
        this.tStep = 100;
        this.iCarSpeedR = 0;
        this.iLastSpeedR = 0;
        this.iCarSpeedL = 0;
        this.iLastSpeedL = 0;
        this.stopLeftSignal = 0;
        this.stopRightSignal = 0;
        this.waitStopState = 0;
        this.stickValue = 0;
        this.stickBar = null;
        this.stickBall = null;
        this.stickUp = null;
        this.stickDown = null;
        this.stickUpPress = null;
        this.stickDownPress = null;
        this.leftMovingTask = new Runnable() { // from class: com.wificar.surface.DoubleAxisLeftControllerSurfaceView.1
            @Override // java.lang.Runnable
            public void run() {
                if (DoubleAxisLeftControllerSurfaceView.this.controlEnable) {
                    if (DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL != 0) {
                        Log.d("move", "Run left(" + DoubleAxisLeftControllerSurfaceView.this.controlEnable + "):" + DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL);
                        try {
                            DoubleAxisLeftControllerSurfaceView.this.wifiCar.move(0, DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DoubleAxisLeftControllerSurfaceView.this.iLastSpeedL = DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL;
                    }
                    if (DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL == 0 && DoubleAxisLeftControllerSurfaceView.this.iLastSpeedL != 0) {
                        try {
                            DoubleAxisLeftControllerSurfaceView.this.wifiCar.move(0, DoubleAxisLeftControllerSurfaceView.this.iCarSpeedL);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        DoubleAxisLeftControllerSurfaceView.this.iLastSpeedL = 0;
                    }
                }
                DoubleAxisLeftControllerSurfaceView.this.handler.postDelayed(this, 100L);
            }
        };
        instance = this;
        initial();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        redraw();
    }

    public synchronized void redraw() {
        synchronized (this) {
            Canvas canvas = this.holder.lockCanvas();
            if (canvas != null) {
                Paint paint = new Paint();
                paint.setAlpha(CommandEncoder.KEEP_ALIVE);
                if (this.controlEnable) {
                    canvas.drawBitmap(this.stickBar, this.stickBarPointX, this.stickBarPointY, paint);
                    if (this.size < 5.8d) {
                        if ((this.height <= 320) & (this.width <= 480)) {
                            canvas.drawBitmap(this.stickBall, this.stickBallPointX, this.stickBallPointSmallY, paint);
                        } else if (this.width > 1100 && this.height <= 800) {
                            canvas.drawBitmap(this.stickBall, this.stickBallPointX, this.stickBallPointxxLargeY, paint);
                        } else {
                            canvas.drawBitmap(this.stickBall, this.stickBallPointX, this.stickBallPointY, paint);
                        }
                    } else {
                        if ((this.height <= 480) & (this.width <= 800)) {
                            canvas.drawBitmap(this.stickBall, this.stickBallPointX, this.stickBallPointLargeY, paint);
                        } else {
                            canvas.drawBitmap(this.stickBall, this.stickBallPointX, this.stickBallPointxLargeY, paint);
                        }
                    }
                }
                this.holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(-2);
        redraw();
        this.handler.postDelayed(this.leftMovingTask, 100L);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void setDirection(int left) {
        Log.i("DoubleLeft", "left :" + left);
        if (WificarActivity.getInstance().isPlayModeEnable) {
            WificarActivity.getInstance().sendMessage(WificarActivity.MESSAGE_STOP_PLAY);
        }
        if (left > 0) {
            left = 10;
        }
        if (left < 0 && left > -4) {
            left = 0;
        }
        if (left < -4) {
            left = -10;
        }
        Log.i("wild0", "direction left:" + left);
        this.iCarSpeedL = left;
    }

    @Override // com.wificar.surface.ControllerInterface
    public void setWifiCar(WifiCar wifiCar) {
        this.wifiCar = wifiCar;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        int currentRightSpeed;
        int currentRightSpeed2;
        int currentLeftSpeed;
        int currentSpeed;
        int pointerIndex = (event.getAction() & 65280) >> 8;
        int pointerId = event.getPointerId(pointerIndex);
        int action = event.getAction() & CommandEncoder.KEEP_ALIVE;
        int pointerCount = event.getPointerCount();
        Log.d("touchinfo", "touch left count(" + pointerId + ":" + action + ":" + event.getAction() + "):" + pointerCount);
        for (int i = 0; i < pointerCount; i++) {
            int id = event.getPointerId(i);
            Log.d("moving", "id:" + id + ",action:" + action);
            switch (action) {
                case 0:
                    this.waitStopState = 0;
                    Log.d("screen_left_stick", "(" + i + ")Action down");
                    Log.e("wild0L", "id:" + id);
                    float x = event.getX();
                    float y = event.getY();
                    if (this.size < 5.8d) {
                        if ((this.width <= 480) & (this.height <= 320)) {
                            currentSpeed = (int) ((-(y - this.stickBallPointBaseSmallY)) / 7.0f);
                            if (y - this.stickBarPointSmallY > this.stickBallMotionAreaHeightSmall) {
                                y = this.stickBarPointSmallY + this.stickBallMotionAreaHeightSmall;
                            }
                        } else if (this.width > 1100) {
                            currentSpeed = (int) ((-(y - this.stickBallPointBaseY)) / 12.0f);
                            if (y - this.stickBarPointxxLargeY > this.stickBallMotionAreaHeightxxLarge) {
                                y = this.stickBarPointxxLargeY + this.stickBallMotionAreaHeightxxLarge;
                            }
                        } else {
                            currentSpeed = (int) ((-(y - this.stickBallPointBaseY)) / 9.0f);
                            if (y - this.stickBarPointY > this.stickBallMotionAreaHeight) {
                                y = this.stickBarPointY + this.stickBallMotionAreaHeight;
                            }
                        }
                    } else if ((this.width <= 800) & (this.height <= 480)) {
                        currentSpeed = (int) ((-(y - this.stickBallPointBaseY)) / 6.0f);
                        if (y - this.stickBarPointLargeY > this.stickBallMotionAreaHeightLarge) {
                            y = this.stickBarPointLargeY + this.stickBallMotionAreaHeightLarge;
                        }
                    } else {
                        currentSpeed = (int) ((-(y - this.stickBallPointBaseY)) / 9.0f);
                        if (y - this.stickBarPointxLargeY > this.stickBallMotionAreaHeightxLarge) {
                            y = this.stickBarPointxLargeY + this.stickBallMotionAreaHeightxLarge;
                        }
                    }
                    if (y < this.stickBarPointTopY) {
                        y = this.stickBarPointTopY;
                    }
                    if (isLocateAtLeftStickBallBoundary(x, y)) {
                        this.captureLeftBall = true;
                        this.stickBallPointY = ((int) y) - 20;
                        this.stickBallPointSmallY = ((int) y) - 20;
                        this.stickBallPointxLargeY = ((int) y) - 20;
                        this.stickBallPointxxLargeY = ((int) y) - 20;
                        this.stickBallPointLargeY = ((int) y) - 20;
                        setDirection(currentSpeed);
                    }
                    redraw();
                    break;
                case 1:
                    Log.i("moving", "(" + i + ")Action up");
                    this.stopLeftSignal = 1;
                    this.stopRightSignal = 1;
                    this.captureLeftBall = false;
                    this.captureRightBall = false;
                    if (pointerCount == 2) {
                        this.waitStopState = 1;
                        break;
                    } else if (pointerCount == 1) {
                        WificarActivity.getInstance().getWifiCar().disableMoveFlag();
                        setDirection(0);
                        this.stickBallPointY = this.stickBallPointBaseY;
                        this.stickBallPointSmallY = this.stickBallPointBaseSmallY;
                        this.stickBallPointxLargeY = this.stickBallPointBasexLargeY;
                        this.stickBallPointxxLargeY = this.stickBallPointBasexxLargeY;
                        this.stickBallPointLargeY = this.stickBallPointBaseLargeY;
                        DoubleAxisRightControllerSurfaceView.getInstance().setDirection(0);
                        if (this.size >= 5.8d) {
                            if ((this.width <= 800) & (this.height <= 480)) {
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointLargeY(this.stickBallPointBaseLargeY);
                            } else {
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxLargeY(this.stickBallPointBasexLargeY);
                            }
                        } else if (this.width <= 480) {
                            DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointSmallY(this.stickBallPointBaseSmallY);
                        } else if (this.width > 1100) {
                            DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxxLargeY(this.stickBallPointxxLargeY);
                        } else {
                            DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointY(this.stickBallPointBaseY);
                        }
                        DoubleAxisRightControllerSurfaceView.getInstance().redraw();
                        break;
                    } else {
                        break;
                    }
                case 2:
                    this.waitStopState = 0;
                    Log.d("moving", "(" + i + ")Action move");
                    float my = 0.0f;
                    if (event.getPointerCount() == 1) {
                        event.getX();
                        my = event.getY();
                    } else if (event.getPointerCount() == 2) {
                        event.getX(id);
                        my = event.getY(id);
                    }
                    if (this.size < 5.8d) {
                        if ((this.width <= 480) & (this.height <= 320)) {
                            if (my - this.stickBarPointSmallY > this.stickBallMotionAreaHeightSmall) {
                                my = this.stickBarPointSmallY + this.stickBallMotionAreaHeightSmall;
                            }
                        } else if (this.width > 1100) {
                            if (my - this.stickBarPointxxLargeY > this.stickBallMotionAreaHeightxxLarge) {
                                my = this.stickBarPointxxLargeY + this.stickBallMotionAreaHeightxxLarge;
                            }
                        } else if (my - this.stickBarPointY > this.stickBallMotionAreaHeight) {
                            my = this.stickBarPointY + this.stickBallMotionAreaHeight;
                        }
                    } else if ((this.width <= 800) & (this.height <= 480)) {
                        if (my - this.stickBarPointLargeY > this.stickBallMotionAreaHeightLarge) {
                            my = this.stickBarPointLargeY + this.stickBallMotionAreaHeightLarge;
                        }
                    } else if (my - this.stickBarPointxLargeY > this.stickBallMotionAreaHeightxLarge) {
                        my = this.stickBarPointxLargeY + this.stickBallMotionAreaHeightxLarge;
                    }
                    if (my < this.stickBarPointTopY) {
                        my = this.stickBarPointTopY;
                    }
                    if (id == 0) {
                        if (this.captureLeftBall) {
                            Log.i("DoubleLeft", "captureLeftBall:" + this.captureLeftBall + ",my:" + my);
                            if (this.size < 5.8d) {
                                if ((this.width <= 480) & (this.height <= 320)) {
                                    currentLeftSpeed = (int) ((-(my - this.stickBallPointBaseSmallY)) / 7.0f);
                                    this.stickBallPointSmallY = ((int) my) - 20;
                                } else if (this.width > 1100) {
                                    currentLeftSpeed = (int) ((-(my - this.stickBallPointBasexxLargeY)) / 12.0f);
                                    this.stickBallPointxxLargeY = ((int) my) - 20;
                                } else {
                                    currentLeftSpeed = (int) ((-(my - this.stickBallPointBaseY)) / 9.0f);
                                    this.stickBallPointY = ((int) my) - 20;
                                }
                            } else if ((this.width <= 800) & (this.height <= 480)) {
                                currentLeftSpeed = (int) ((-(my - this.stickBallPointBaseLargeY)) / 6.0f);
                                this.stickBallPointLargeY = ((int) my) - 20;
                            } else {
                                currentLeftSpeed = (int) ((-(my - this.stickBallPointBasexLargeY)) / 9.0f);
                                this.stickBallPointxLargeY = ((int) my) - 20;
                            }
                            Log.i("DoubleLeft", "currentLeftSpeed:" + currentLeftSpeed);
                            setDirection(currentLeftSpeed);
                            break;
                        } else {
                            break;
                        }
                    } else if (id == 1 && this.captureRightBall) {
                        if (this.size < 5.8d) {
                            if ((this.width <= 480) & (this.height <= 320)) {
                                currentRightSpeed2 = (int) ((-(my - this.stickBallPointBaseSmallY)) / 8.0f);
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointSmallY(((int) my) - 20);
                            } else if (this.width > 1100) {
                                currentRightSpeed2 = (int) ((-(my - this.stickBallPointBasexxLargeY)) / 12.0f);
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxxLargeY(((int) my) - 20);
                            } else {
                                currentRightSpeed2 = (int) ((-(my - this.stickBallPointBaseY)) / 9.0f);
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointY(((int) my) - 20);
                            }
                        } else if ((this.width <= 800) & (this.height <= 480)) {
                            currentRightSpeed2 = (int) ((-(my - this.stickBallPointBaseLargeY)) / 6.0f);
                            DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointLargeY(((int) my) - 20);
                        } else {
                            currentRightSpeed2 = (int) ((-(my - this.stickBallPointBasexLargeY)) / 9.0f);
                            DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxLargeY(((int) my) - 20);
                        }
                        Log.i("DoubleLeft", "currentRightSpeed:" + currentRightSpeed2);
                        DoubleAxisRightControllerSurfaceView.getInstance().setDirection(currentRightSpeed2);
                        DoubleAxisRightControllerSurfaceView.getInstance().redraw();
                        break;
                    }
                    break;
                case CommandEncoder.VIDEO_START_RESP /* 5 */:
                    this.waitStopState = 0;
                    if (id == 1) {
                        float x1 = event.getX(id);
                        float y1 = event.getY(id);
                        Log.i("touchinfo", "touch left point down count right(1):" + x1 + "," + y1);
                        if (isLocateAtRightStickBallBoundary(x1, y1)) {
                            this.captureRightBall = true;
                            if (this.size < 5.8d) {
                                if ((this.width <= 480) & (this.height <= 320)) {
                                    if (y1 - this.stickBarPointSmallY > this.stickBallMotionAreaHeightSmall) {
                                        y1 = this.stickBarPointSmallY + this.stickBallMotionAreaHeightSmall;
                                    }
                                    if (y1 < this.stickBarPointTopY) {
                                        y1 = this.stickBarPointTopY;
                                    }
                                    currentRightSpeed = (int) ((y1 - this.stickBallPointBaseSmallY) / 7.0f);
                                    DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointSmallY(((int) y1) - 20);
                                } else if (this.width > 1100) {
                                    if (y1 - this.stickBarPointxxLargeY > this.stickBallMotionAreaHeight) {
                                        y1 = this.stickBarPointxxLargeY + this.stickBallMotionAreaHeight;
                                    }
                                    if (y1 < this.stickBarPointTopY) {
                                        y1 = this.stickBarPointTopY;
                                    }
                                    currentRightSpeed = (int) ((y1 - this.stickBallPointBaseY) / 12.0f);
                                    DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxxLargeY(((int) y1) - 20);
                                } else {
                                    if (y1 - this.stickBarPointSmallY > this.stickBallMotionAreaHeight) {
                                        y1 = this.stickBarPointSmallY + this.stickBallMotionAreaHeight;
                                    }
                                    if (y1 < this.stickBarPointTopY) {
                                        y1 = this.stickBarPointTopY;
                                    }
                                    currentRightSpeed = (int) ((y1 - this.stickBallPointBaseY) / 9.0f);
                                    DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointY(((int) y1) - 20);
                                }
                            } else if ((this.width <= 800) & (this.height <= 480)) {
                                if (y1 - this.stickBarPointLargeY > this.stickBallMotionAreaHeightLarge) {
                                    y1 = this.stickBarPointLargeY + this.stickBallMotionAreaHeightLarge;
                                }
                                if (y1 < this.stickBarPointTopY) {
                                    y1 = this.stickBarPointTopY;
                                }
                                currentRightSpeed = (int) ((y1 - this.stickBallPointBaseLargeY) / 6.0f);
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointLargeY(((int) y1) - 20);
                            } else {
                                if (y1 - this.stickBarPointxLargeY > this.stickBallMotionAreaHeightxLarge) {
                                    y1 = this.stickBarPointxLargeY + this.stickBallMotionAreaHeightxLarge;
                                }
                                if (y1 < this.stickBarPointTopY) {
                                    y1 = this.stickBarPointTopY;
                                }
                                currentRightSpeed = (int) ((y1 - this.stickBallPointBasexLargeY) / 9.0f);
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxLargeY(((int) y1) - 20);
                            }
                            Log.i("currentLeftSpeed", "currentLeftSpeed:" + currentRightSpeed);
                            DoubleAxisRightControllerSurfaceView.getInstance().setDirection(currentRightSpeed);
                            DoubleAxisRightControllerSurfaceView.getInstance().redraw();
                            break;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                case CommandEncoder.VIDEO_END /* 6 */:
                    this.stopLeftSignal = 1;
                    this.stopRightSignal = 1;
                    this.waitStopState = 1;
                    float x2 = event.getX(id);
                    float y2 = event.getY(id);
                    Log.i("wild0", "touch left point up count(" + id + "):" + x2 + "," + y2);
                    if (pointerId != id) {
                        break;
                    } else {
                        if (id == 0) {
                            Log.i("DoubleLeft", "left is UP");
                            setDirection(0);
                            this.stickBallPointY = this.stickBallPointBaseY;
                            this.stickBallPointSmallY = this.stickBallPointBaseSmallY;
                            this.stickBallPointxLargeY = this.stickBallPointBasexLargeY;
                            this.stickBallPointxxLargeY = this.stickBallPointBasexxLargeY;
                            this.stickBallPointLargeY = this.stickBallPointBaseLargeY;
                        }
                        if (id == 1) {
                            Log.i("DoubleLeft", "right is UP");
                            if (this.size < 5.8d) {
                                if ((this.width <= 480) & (this.height <= 320)) {
                                    DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointSmallY(this.stickBallPointBaseSmallY);
                                } else if (this.width > 1100) {
                                    DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxxLargeY(this.stickBallPointBasexxLargeY);
                                } else {
                                    DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointY(this.stickBallPointBaseY);
                                }
                            } else if ((this.width <= 800) & (this.height <= 480)) {
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointLargeY(this.stickBallPointBaseLargeY);
                            } else {
                                DoubleAxisRightControllerSurfaceView.getInstance().setStickBallPointxLargeY(this.stickBallPointBasexLargeY);
                            }
                            DoubleAxisRightControllerSurfaceView.getInstance().setDirection(0);
                            DoubleAxisRightControllerSurfaceView.getInstance().redraw();
                            break;
                        } else {
                            break;
                        }
                    }
            }
        }
        if (this.stopLeftSignal == 1 && this.waitStopState == 0) {
            this.stopLeftSignal = 0;
        }
        if (this.stopRightSignal == 1 && this.waitStopState == 0) {
            this.stopRightSignal = 0;
        }
        redraw();
        return true;
    }

    public void setStickBallPointY(int y) {
        if (y > this.stickBallMotionAreaHeight) {
            y = this.stickBallMotionAreaHeight;
        }
        if (y < 0) {
            y = 0;
        }
        this.stickBallPointY = y;
    }

    public void setStickBallPointSmallY(int y) {
        if (y > this.stickBallMotionAreaHeightSmall) {
            y = this.stickBallMotionAreaHeightSmall;
        }
        if (y < 0) {
            y = 0;
        }
        this.stickBallPointSmallY = y;
    }

    public void setStickBallPointLargeY(int y) {
        if (y > this.stickBallMotionAreaHeightLarge) {
            y = this.stickBallMotionAreaHeightLarge;
        }
        if (y < 0) {
            y = 0;
        }
        this.stickBallPointLargeY = y;
    }

    public void setStickBallPointxLargeY(int y) {
        if (y > this.stickBallMotionAreaHeightxLarge) {
            y = this.stickBallMotionAreaHeightxLarge;
        }
        if (y < 0) {
            y = 0;
        }
        this.stickBallPointxLargeY = y;
    }

    public void setStickBallPointxxLargeY(int y) {
        if (y > this.stickBallMotionAreaHeightxxLarge) {
            y = this.stickBallMotionAreaHeightxxLarge;
        }
        if (y < 0) {
            y = 0;
        }
        this.stickBallPointxxLargeY = y;
    }

    public boolean isLocateAtLeftStickBallBoundary(float x, float y) {
        int edge = this.stickBallMotionAreaWidth - this.stickBallWidth;
        Log.i("isLocateAtLeft", "x:" + x + " ,y:" + y + ",edge:" + edge);
        Log.i("isLocateAtLeft", "stickBallMotionAreaWidth:" + this.stickBallMotionAreaWidth + "stickBallWidth:" + this.stickBallWidth);
        Log.i("isLocateAtLeftX", "(stickBallPointX-edge):" + (this.stickBallPointX - edge));
        Log.i("isLocateAtLeftX", "(stickBallPointX+stickBallWidth+edge):" + (this.stickBallPointX + this.stickBallWidth + edge));
        Log.i("isLocateAtLeftY", "(stickBallPointY-edge):" + (this.stickBallPointY - edge));
        Log.i("isLocateAtLeftY", "(stickBallPointY+stickBallHeight+edge):" + (this.stickBallPointY + this.stickBallHeight + edge));
        return x > ((float) (this.stickBallPointX - edge)) && x < ((float) ((this.stickBallPointX + this.stickBallWidth) + edge)) && y > 20.0f && y < 300.0f;
    }

    public boolean isLocateAtRightStickBallBoundary(float x, float y) {
        int widthPixel = ImageUtility.getWidth(getContext());
        int leftXL = widthPixel - this.stickBallWidth;
        int edge = this.stickBallMotionAreaWidth - this.stickBallWidth;
        Log.i("barnotify", "width:" + widthPixel + ", x:" + x + ",(" + leftXL + "," + widthPixel + ")");
        Log.i("isLocateAtRightStick", "(x,y):(" + x + "," + y + ")edge:" + edge);
        Log.i("isLocateAtRightStick", "leftXL-edge*4:" + (leftXL - (edge * 4)));
        Log.i("isLocateAtRightStick", "(leftXR+edge):" + (widthPixel + edge));
        Log.i("isLocateAtRightStick", "stickBallPointY:" + this.stickBallPointY + ",stickBallHeight:" + this.stickBallHeight);
        Log.i("isLocateAtRightStick", "(stickBallPointY-edge):" + (this.stickBallPointY - edge));
        Log.i("isLocateAtRightStick", "(edge-stickBallPointY):" + (edge - this.stickBallPointY));
        Log.i("isLocateAtRightStick", "(stickBallPointY+stickBallHeight+edge):" + ((this.stickBallPointY + this.stickBallHeight + edge) * 3));
        return x > ((float) (leftXL - (edge * 4))) && x < ((float) (widthPixel + edge)) && y > -40.0f && y < 300.0f;
    }

    @Override // com.wificar.surface.ControllerInterface
    public void disableControl() {
        this.controlEnable = false;
        setVisibility(4);
        redraw();
    }

    @Override // com.wificar.surface.ControllerInterface
    public void enableControl() {
        this.controlEnable = true;
        setVisibility(0);
        redraw();
    }
}
