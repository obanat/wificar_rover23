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
public class DoubleAxisRightControllerSurfaceView extends SurfaceView implements SurfaceHolder.Callback, ControllerInterface {
    private static final int MAX = 10;
    static DoubleAxisRightControllerSurfaceView instance;
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
    private int leftStartPointX;
    private int leftStartPointY;
    int maxPointIndex;
    int rightIndexId;
    private Runnable rightMovingTask;
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
    private int stickBarWidth;
    Bitmap stickDown;
    Bitmap stickDownPress;
    Bitmap stickUp;
    Bitmap stickUpPress;
    private int stopLeftSignal;
    private int stopRightSignal;
    private final int tStep;
    private int waitStopState;
    private int width;
    private WifiCar wifiCar;

    public static DoubleAxisRightControllerSurfaceView getInstance() {
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
        }
        if (this.size > 5.8d) {
            Log.i("Double", "225X48");
            this.stickBall = Bitmap.createScaledBitmap(this.stickBall, ImageUtility.dip2px(getContext(), 48.0f), ImageUtility.dip2px(getContext(), 48.0f), true);
            this.stickBar = Bitmap.createScaledBitmap(this.stickBar, ImageUtility.dip2px(getContext(), 48.0f), ImageUtility.dip2px(getContext(), 225.0f), true);
        }
        this.holder.addCallback(this);
    }

    public DoubleAxisRightControllerSurfaceView(Context context) {
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
        this.stickBallPointLargeY = 65;
        this.stickBallPointxLargeY = 85;
        this.stickBallPointxxLargeY = 130;
        this.stickBallWidth = 40;
        this.stickBallHeight = 40;
        this.stickBarPointX = 10;
        this.stickBarPointY = 0;
        this.stickBarPointSmallY = 0;
        this.stickBarPointLargeY = 0;
        this.stickBarPointxLargeY = 0;
        this.stickBarPointxxLargeY = 0;
        this.stickBarPointTopY = 20;
        this.stickBarWidth = 60;
        this.stickBallPointBaseY = 100;
        this.stickBallPointBaseSmallY = 70;
        this.stickBallPointBaseLargeY = 65;
        this.stickBallPointBasexLargeY = 85;
        this.stickBallPointBasexxLargeY = 130;
        this.stickBallMotionAreaWidth = 80;
        this.stickBallMotionAreaHeight = 230;
        this.stickBallMotionAreaHeightSmall = 160;
        this.stickBallMotionAreaHeightLarge = 150;
        this.stickBallMotionAreaHeightxLarge = 195;
        this.stickBallMotionAreaHeightxxLarge = 300;
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
        this.stickBar = null;
        this.stickBall = null;
        this.stickUp = null;
        this.stickDown = null;
        this.stickUpPress = null;
        this.stickDownPress = null;
        this.rightMovingTask = new Runnable() { // from class: com.wificar.surface.DoubleAxisRightControllerSurfaceView.1
            @Override // java.lang.Runnable
            public void run() {
                if (DoubleAxisRightControllerSurfaceView.this.controlEnable) {
                    if (DoubleAxisRightControllerSurfaceView.this.iCarSpeedR != 0) {
                        Log.d("wild0", "Run r(" + DoubleAxisRightControllerSurfaceView.this.controlEnable + "):" + DoubleAxisRightControllerSurfaceView.this.iCarSpeedR);
                        try {
                            DoubleAxisRightControllerSurfaceView.this.wifiCar.move(1, DoubleAxisRightControllerSurfaceView.this.iCarSpeedR);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DoubleAxisRightControllerSurfaceView.this.iLastSpeedR = DoubleAxisRightControllerSurfaceView.this.iCarSpeedR;
                    }
                    if (DoubleAxisRightControllerSurfaceView.this.iCarSpeedR == 0 && DoubleAxisRightControllerSurfaceView.this.iLastSpeedR != 0) {
                        try {
                            DoubleAxisRightControllerSurfaceView.this.wifiCar.move(1, DoubleAxisRightControllerSurfaceView.this.iCarSpeedR);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        DoubleAxisRightControllerSurfaceView.this.iLastSpeedR = 0;
                    }
                }
                DoubleAxisRightControllerSurfaceView.this.handler.postDelayed(this, 100L);
            }
        };
        instance = this;
        initial();
    }

    public DoubleAxisRightControllerSurfaceView(Context context, AttributeSet attrs) {
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
        this.stickBallPointLargeY = 65;
        this.stickBallPointxLargeY = 85;
        this.stickBallPointxxLargeY = 130;
        this.stickBallWidth = 40;
        this.stickBallHeight = 40;
        this.stickBarPointX = 10;
        this.stickBarPointY = 0;
        this.stickBarPointSmallY = 0;
        this.stickBarPointLargeY = 0;
        this.stickBarPointxLargeY = 0;
        this.stickBarPointxxLargeY = 0;
        this.stickBarPointTopY = 20;
        this.stickBarWidth = 60;
        this.stickBallPointBaseY = 100;
        this.stickBallPointBaseSmallY = 70;
        this.stickBallPointBaseLargeY = 65;
        this.stickBallPointBasexLargeY = 85;
        this.stickBallPointBasexxLargeY = 130;
        this.stickBallMotionAreaWidth = 80;
        this.stickBallMotionAreaHeight = 230;
        this.stickBallMotionAreaHeightSmall = 160;
        this.stickBallMotionAreaHeightLarge = 150;
        this.stickBallMotionAreaHeightxLarge = 195;
        this.stickBallMotionAreaHeightxxLarge = 300;
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
        this.stickBar = null;
        this.stickBall = null;
        this.stickUp = null;
        this.stickDown = null;
        this.stickUpPress = null;
        this.stickDownPress = null;
        this.rightMovingTask = new Runnable() { // from class: com.wificar.surface.DoubleAxisRightControllerSurfaceView.1
            @Override // java.lang.Runnable
            public void run() {
                if (DoubleAxisRightControllerSurfaceView.this.controlEnable) {
                    if (DoubleAxisRightControllerSurfaceView.this.iCarSpeedR != 0) {
                        Log.d("wild0", "Run r(" + DoubleAxisRightControllerSurfaceView.this.controlEnable + "):" + DoubleAxisRightControllerSurfaceView.this.iCarSpeedR);
                        try {
                            DoubleAxisRightControllerSurfaceView.this.wifiCar.move(1, DoubleAxisRightControllerSurfaceView.this.iCarSpeedR);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DoubleAxisRightControllerSurfaceView.this.iLastSpeedR = DoubleAxisRightControllerSurfaceView.this.iCarSpeedR;
                    }
                    if (DoubleAxisRightControllerSurfaceView.this.iCarSpeedR == 0 && DoubleAxisRightControllerSurfaceView.this.iLastSpeedR != 0) {
                        try {
                            DoubleAxisRightControllerSurfaceView.this.wifiCar.move(1, DoubleAxisRightControllerSurfaceView.this.iCarSpeedR);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        DoubleAxisRightControllerSurfaceView.this.iLastSpeedR = 0;
                    }
                }
                DoubleAxisRightControllerSurfaceView.this.handler.postDelayed(this, 100L);
            }
        };
        instance = this;
        initial();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        redraw();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(-2);
        redraw();
        this.handler.postDelayed(this.rightMovingTask, 100L);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override // com.wificar.surface.ControllerInterface
    public void setWifiCar(WifiCar wifiCar) {
        this.wifiCar = wifiCar;
    }

    public void setDirection(int right) {
        if (WificarActivity.getInstance().isPlayModeEnable) {
            WificarActivity.getInstance().sendMessage(WificarActivity.MESSAGE_STOP_PLAY);
        }
        if (right > 0) {
            right = 10;
        }
        if (right > 0 && right < -4) {
            right = 0;
        }
        if (right < -4) {
            right = -10;
        }
        this.iCarSpeedR = right;
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
                        } else if (this.width > 1100) {
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

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        int currentLeftSpeed;
        int currentLeftSpeed2;
        int currentRightSpeed;
        int currentSpeed;
        if (!this.controlEnable) {
            return true;
        }
        int pointerIndex = (event.getAction() & 65280) >> 8;
        int pointerId = event.getPointerId(pointerIndex);
        int action = event.getAction() & CommandEncoder.KEEP_ALIVE;
        int pointerCount = event.getPointerCount();
        Log.d("touchinfo", "touch right count(" + pointerId + ":" + action + ":" + event.getAction() + "):" + pointerCount);
        for (int i = 0; i < pointerCount; i++) {
            int id = event.getPointerId(i);
            Log.d("wild0", "id:" + id + ",action:" + action);
            switch (action) {
                case 0:
                    try {
                        this.waitStopState = 0;
                        Log.e("wild0R", "id:" + id);
                        float x = event.getX(id);
                        float y = event.getY(id);
                        if (this.size < 5.8d) {
                            if ((this.height <= 320) & (this.width <= 480)) {
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
                        } else {
                            if ((this.height <= 480) & (this.width <= 800)) {
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
                        }
                        if (y < this.stickBarPointTopY) {
                            y = this.stickBarPointTopY;
                        }
                        if (isLocateAtRightStickBallBoundary(x, y)) {
                            this.captureRightBall = true;
                            this.stickBallPointY = ((int) y) - 20;
                            this.stickBallPointSmallY = ((int) y) - 20;
                            this.stickBallPointxLargeY = ((int) y) - 20;
                            this.stickBallPointxxLargeY = ((int) y) - 20;
                            this.stickBallPointLargeY = ((int) y) - 20;
                            setDirection(currentSpeed);
                        }
                        redraw();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                case 1:
                    try {
                        this.stopLeftSignal = 1;
                        this.stopRightSignal = 1;
                        this.waitStopState = 1;
                        Log.d("barnotify", "ACTION_UP(" + i + ")Action up");
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
                            DoubleAxisLeftControllerSurfaceView.getInstance().setDirection(0);
                            if (this.size < 5.8d) {
                                if ((this.height <= 320) & (this.width <= 480)) {
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointSmallY(this.stickBallPointBaseSmallY);
                                } else if (this.width > 1100) {
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxxLargeY(this.stickBallPointBasexxLargeY);
                                } else {
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointY(this.stickBallPointBaseY);
                                }
                            } else {
                                if ((this.height <= 480) & (this.width <= 800)) {
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointLargeY(this.stickBallPointBaseLargeY);
                                } else {
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxLargeY(this.stickBallPointBasexLargeY);
                                }
                            }
                            DoubleAxisLeftControllerSurfaceView.getInstance().redraw();
                            this.captureRightBall = false;
                            this.captureLeftBall = false;
                            break;
                        } else {
                            break;
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        break;
                    }
                case 2:
                    try {
                        this.waitStopState = 0;
                        int id2 = event.getPointerId(i);
                        Log.d("rightbar", String.valueOf(i) + ":(" + id2 + ")Action move=>" + event.getPointerCount());
                        float my = 0.0f;
                        if (event.getPointerCount() == 1) {
                            event.getX();
                            my = event.getY();
                        } else if (event.getPointerCount() == 2) {
                            event.getX(id2);
                            my = event.getY(id2);
                        }
                        Log.d("rightbar", "(" + id2 + ")Action move");
                        if (this.size < 5.8d) {
                            if ((this.height <= 320) & (this.width <= 480)) {
                                if (my - this.stickBarPointY > this.stickBallMotionAreaHeightSmall) {
                                    my = this.stickBarPointY + this.stickBallMotionAreaHeightSmall;
                                }
                            } else if (this.width > 1100) {
                                if (my - this.stickBarPointxxLargeY > this.stickBallMotionAreaHeightxxLarge) {
                                    my = this.stickBarPointxxLargeY + this.stickBallMotionAreaHeightxxLarge;
                                }
                            } else if (my - this.stickBarPointY > this.stickBallMotionAreaHeight) {
                                my = this.stickBarPointY + this.stickBallMotionAreaHeight;
                            }
                        } else {
                            if ((this.height <= 480) & (this.width <= 800)) {
                                if (my - this.stickBarPointLargeY > this.stickBallMotionAreaHeightLarge) {
                                    my = this.stickBarPointLargeY + this.stickBallMotionAreaHeightLarge;
                                }
                            } else if (my - this.stickBarPointxLargeY > this.stickBallMotionAreaHeightxLarge) {
                                my = this.stickBarPointxLargeY + this.stickBallMotionAreaHeightxLarge;
                            }
                        }
                        if (my < this.stickBarPointTopY) {
                            my = this.stickBarPointTopY;
                        }
                        if (id2 == 0) {
                            if (this.captureRightBall) {
                                Log.i("DoubleRight", "captureRightBall:" + this.captureRightBall + ",my:" + my);
                                if (this.size < 5.8d) {
                                    if ((this.height <= 320) & (this.width <= 480)) {
                                        currentRightSpeed = (int) ((-(my - this.stickBallPointBaseSmallY)) / 7.0f);
                                        this.stickBallPointSmallY = ((int) my) - 20;
                                    } else if (this.width > 1100) {
                                        currentRightSpeed = (int) ((-(my - this.stickBallPointBasexxLargeY)) / 12.0f);
                                        this.stickBallPointxxLargeY = ((int) my) - 20;
                                    } else {
                                        currentRightSpeed = (int) ((-(my - this.stickBallPointBaseY)) / 9.0f);
                                        this.stickBallPointY = ((int) my) - 20;
                                    }
                                } else {
                                    if ((this.height <= 480) & (this.width <= 800)) {
                                        currentRightSpeed = (int) ((-(my - this.stickBallPointBaseLargeY)) / 6.0f);
                                        this.stickBallPointLargeY = ((int) my) - 20;
                                    } else {
                                        currentRightSpeed = (int) ((-(my - this.stickBallPointBasexLargeY)) / 9.0f);
                                        this.stickBallPointxLargeY = ((int) my) - 20;
                                    }
                                }
                                Log.i("DoubleRight", "currentLeftSpeed:" + currentRightSpeed);
                                setDirection(currentRightSpeed);
                                break;
                            } else {
                                break;
                            }
                        } else if (id2 == 1 && this.captureLeftBall) {
                            if (this.size < 5.8d) {
                                if ((this.height <= 320) & (this.width <= 480)) {
                                    currentLeftSpeed2 = (int) ((-(my - this.stickBallPointBaseSmallY)) / 9.0f);
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointSmallY(((int) my) - 20);
                                } else if (this.width > 1100) {
                                    currentLeftSpeed2 = (int) ((-(my - this.stickBallPointBasexxLargeY)) / 12.0f);
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxxLargeY(((int) my) - 20);
                                } else {
                                    currentLeftSpeed2 = (int) ((-(my - this.stickBallPointBaseY)) / 9.0f);
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointY(((int) my) - 20);
                                }
                            } else {
                                if ((this.height <= 480) & (this.width <= 800)) {
                                    currentLeftSpeed2 = (int) ((-(my - this.stickBallPointBaseLargeY)) / 6.0f);
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointLargeY(((int) my) - 20);
                                } else {
                                    currentLeftSpeed2 = (int) ((-(my - this.stickBallPointBasexLargeY)) / 9.0f);
                                    DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxLargeY(((int) my) - 20);
                                }
                            }
                            Log.i("DoubleRight", "currentRightSpeed:" + currentLeftSpeed2);
                            DoubleAxisLeftControllerSurfaceView.getInstance().setDirection(currentLeftSpeed2);
                            DoubleAxisLeftControllerSurfaceView.getInstance().redraw();
                            break;
                        }
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        Log.d("rightbar", "excep (" + event.getX(0) + ")Action move");
                        break;
                    }
                    break;
                case CommandEncoder.VIDEO_START_RESP /* 5 */:
                    try {
                        this.waitStopState = 0;
                        if (id == 1) {
                            float x1 = event.getX(id);
                            float y1 = event.getY(id);
                            Log.d("wild0", "touch left point down count(1):" + x1 + "," + y1);
                            if (isLocateAtLeftStickBallBoundary(x1, y1)) {
                                this.captureLeftBall = true;
                                if (this.size < 5.8d) {
                                    if ((this.height <= 320) & (this.width <= 480)) {
                                        currentLeftSpeed = (int) ((-(y1 - this.stickBallPointBaseSmallY)) / 7.0f);
                                        if (y1 - this.stickBarPointSmallY > this.stickBallMotionAreaHeightSmall) {
                                            y1 = this.stickBarPointSmallY + this.stickBallMotionAreaHeightSmall;
                                        }
                                        if (y1 < this.stickBarPointTopY) {
                                            y1 = this.stickBarPointTopY;
                                        }
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointSmallY(((int) y1) - 20);
                                    } else if (this.width > 1100) {
                                        currentLeftSpeed = (int) ((-(y1 - this.stickBallPointBaseY)) / 9.0f);
                                        if (y1 - this.stickBarPointxxLargeY > this.stickBallMotionAreaHeightxxLarge) {
                                            y1 = this.stickBarPointxxLargeY + this.stickBallMotionAreaHeightxxLarge;
                                        }
                                        if (y1 < this.stickBarPointTopY) {
                                            y1 = this.stickBarPointTopY;
                                        }
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxxLargeY(((int) y1) - 20);
                                    } else {
                                        currentLeftSpeed = (int) ((-(y1 - this.stickBallPointBaseY)) / 9.0f);
                                        if (y1 - this.stickBarPointY > this.stickBallMotionAreaHeight) {
                                            y1 = this.stickBarPointY + this.stickBallMotionAreaHeight;
                                        }
                                        if (y1 < this.stickBarPointTopY) {
                                            y1 = this.stickBarPointTopY;
                                        }
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointY(((int) y1) - 20);
                                    }
                                } else {
                                    if ((this.height <= 480) & (this.width <= 800)) {
                                        currentLeftSpeed = (int) ((-(y1 - this.stickBallPointBaseY)) / 6.0f);
                                        if (y1 - this.stickBarPointLargeY > this.stickBallMotionAreaHeightLarge) {
                                            y1 = this.stickBarPointLargeY + this.stickBallMotionAreaHeightLarge;
                                        }
                                        if (y1 < this.stickBarPointTopY) {
                                            y1 = this.stickBarPointTopY;
                                        }
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointLargeY(((int) y1) - 20);
                                    } else {
                                        currentLeftSpeed = (int) ((-(y1 - this.stickBallPointBaseY)) / 9.0f);
                                        if (y1 - this.stickBarPointxLargeY > this.stickBallMotionAreaHeightxLarge) {
                                            y1 = this.stickBarPointxLargeY + this.stickBallMotionAreaHeightxLarge;
                                        }
                                        if (y1 < this.stickBarPointTopY) {
                                            y1 = this.stickBarPointTopY;
                                        }
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxLargeY(((int) y1) - 20);
                                    }
                                }
                                DoubleAxisLeftControllerSurfaceView.getInstance().setDirection(currentLeftSpeed);
                                DoubleAxisLeftControllerSurfaceView.getInstance().redraw();
                                break;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } catch (Exception e4) {
                        e4.printStackTrace();
                        break;
                    }
                case CommandEncoder.VIDEO_END /* 6 */:
                    try {
                        this.stopLeftSignal = 1;
                        this.stopRightSignal = 1;
                        this.waitStopState = 1;
                        float x2 = event.getX(id);
                        float y2 = event.getY(id);
                        Log.d("barnotify", "ACTION_POINTER_1_UP(" + pointerId + "," + id + "):" + x2 + "," + y2);
                        if (pointerId != id) {
                            break;
                        } else {
                            if (id == 0) {
                                this.captureRightBall = false;
                                setDirection(0);
                                this.stickBallPointY = this.stickBallPointBaseY;
                                this.stickBallPointSmallY = this.stickBallPointBaseSmallY;
                                this.stickBallPointxLargeY = this.stickBallPointBasexLargeY;
                                this.stickBallPointxxLargeY = this.stickBallPointBasexxLargeY;
                                this.stickBallPointLargeY = this.stickBallPointBaseLargeY;
                            }
                            if (id == 1) {
                                this.captureLeftBall = false;
                                if (this.size < 5.8d) {
                                    if ((this.height <= 320) & (this.width <= 480)) {
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointSmallY(this.stickBallPointBaseSmallY);
                                    } else if (this.width > 1100) {
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxxLargeY(this.stickBallPointBasexxLargeY);
                                    } else {
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointY(this.stickBallPointBaseY);
                                    }
                                } else {
                                    if ((this.height <= 480) & (this.width <= 800)) {
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointLargeY(this.stickBallPointBaseLargeY);
                                    } else {
                                        DoubleAxisLeftControllerSurfaceView.getInstance().setStickBallPointxLargeY(this.stickBallPointBasexLargeY);
                                    }
                                }
                                DoubleAxisLeftControllerSurfaceView.getInstance().setDirection(0);
                                DoubleAxisLeftControllerSurfaceView.getInstance().redraw();
                                break;
                            } else {
                                break;
                            }
                        }
                    } catch (Exception e5) {
                        e5.printStackTrace();
                        break;
                    }
            }
        }
        if (this.stopRightSignal == 1 && this.waitStopState == 0) {
            this.stopRightSignal = 0;
        } else if (this.stopLeftSignal == 1 && this.waitStopState == 0) {
            this.stopLeftSignal = 0;
        }
        redraw();
        DoubleAxisLeftControllerSurfaceView.getInstance().redraw();
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
        int widthPixel = ImageUtility.getWidth(getContext());
        int leftXL = -(widthPixel - this.stickBarWidth);
        int leftXR = (-(widthPixel - this.stickBarWidth)) + (this.stickBarWidth * 2);
        int edge = this.stickBallMotionAreaWidth - this.stickBallWidth;
        Log.i("barnotify", "width:" + widthPixel + ", x:" + x + ",(" + leftXL + "," + leftXR + ")");
        Log.i("isLocateAtRightStick", "(x,y):(" + x + "," + y + ")edge:" + edge);
        Log.i("isLocateAtRightStick", "leftXL-edge*4:" + (leftXL - (edge * 4)));
        Log.i("isLocateAtRightStick", "(leftXR+edge):" + (leftXR + edge));
        Log.i("isLocateAtRightStick", "stickBallPointY:" + this.stickBallPointY + ",stickBallHeight:" + this.stickBallHeight);
        Log.i("isLocateAtRightStick", "(stickBallPointY-edge):" + (this.stickBallPointY - edge));
        Log.i("isLocateAtRightStick", "(edge-stickBallPointY):" + (edge - this.stickBallPointY));
        Log.i("isLocateAtRightStick", "(stickBallPointY+stickBallHeight+edge):" + (this.stickBallPointY + this.stickBallHeight + edge));
        Log.d("barnotify", "width:" + widthPixel + ", x:" + x + ",(" + leftXL + "," + leftXR + ")");
        return x > ((float) (leftXL - (edge * 4))) && x < ((float) (leftXR + edge)) && y > -40.0f && y < 300.0f;
    }

    public boolean isLocateAtRightStickBallBoundary(float x, float y) {
        ImageUtility.getWidth(getContext());
        int edge = this.stickBallMotionAreaWidth - this.stickBallWidth;
        if (x <= this.stickBallPointX - edge || x >= this.stickBallPointX + this.stickBallWidth + edge || y <= 20.0f || y >= 300.0f) {
            return false;
        }
        Log.d("boundary", "r:true");
        return true;
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
