package com.obana.rover;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import com.obana.rover.utils.*;

public class WificarMain extends Activity implements View.OnClickListener, View.OnTouchListener, Chronometer.OnChronometerTickListener {
    public static final String TAG = "Wificar_Activity";
    public static final int MESSAGE_CONNECT_TO_CAR_FAIL = 1002;
    public static final int MESSAGE_MAKE_TOAST = 6001;

    public static final boolean SHOW_DEBUG_MESSAGE = true;
    public static final String BUNDLE_KEY_TOAST_MSG = "Tmessage";

    public Timer ConnnectOut_timer = null;
    private WifiCar wifiCar = null;
    private Handler handler = null;
    private int LMoving = 0;

    private WheelView mControlView;
    private WheelView mCamControlView;
    private double mLastTimeCon;
    private int counter = 0;
    private static final int TIME_INTERVAL_MS = 500;
    private int mLeft = 4;
    private int mLeftSpeed = 0;
    private int mRight = 1;
    private int mRightSpeed = 0;

    private int mCameraControl = 0;

    public MjpegView mJpegView;
    public H264SurfaceView mH264View;
    private ImageButton mJpegButton;
    private boolean mJpegStart;
    private Drawable buttonJpegStart;
    private Drawable buttonJpegStop;


    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mControlView = findViewById(R.id.conntrolView);
        mControlView.setOnWheelViewMoveListener(mControllerListener);

        mCamControlView = findViewById(R.id.camConntrolView);
        mCamControlView.setOnWheelViewMoveListener(mCamControllerListener);

        mJpegView = findViewById(R.id.jpegView);
        mH264View = findViewById(R.id.h264View);
        findViewById(R.id.startWifiButton).setOnClickListener(buttonWifiClickListener);
        ;
        //button to enable/disable jpeg view
        mJpegButton = findViewById(R.id.startJpegButton);
        mJpegButton.setOnClickListener(buttonJpegClickListener);
        mJpegStart = false;
        buttonJpegStart = getResources().getDrawable(R.drawable.sym_light);
        buttonJpegStop = getResources().getDrawable(R.drawable.sym_light_off);

        this.wifiCar = new WifiCar(this);
        this.handler = new Handler() {
            public void handleMessage(Message param1Message) {
                if (!handleMessageinUI(param1Message)) {
                    super.handleMessage(param1Message);
                }
            }
        };

        SharedPreferences sp = getSharedPreferences("wificar", Context.MODE_PRIVATE);

        final Switch mVersionSwitch = (Switch) findViewById(R.id.versionSwitch);
        int version = sp.getInt("version", 2);
        wifiCar.setVersion(version);
        mVersionSwitch.setChecked(version == 3);
        mVersionSwitch.setShowText(true);
        //mVersionSwitch.setText(version == 3 ? "Ver3.0" : "Ver2.0");
        refreshGLView();
        mVersionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.i(TAG, "car version user switch changed:" + b);
                int ver = b ? 3 : 2;//destination version;
                WificarMain.this.wifiCar.setVersion(ver);
                SharedPreferences sp = getSharedPreferences("wificar", Context.MODE_PRIVATE);
                Editor editor = sp.edit();
                editor.putInt("version", ver);
                editor.commit();
                refreshGLView();
                //mVersionSwitch.setText(version == 3 ? "Ver3.0" : "Ver2.0");
                (new Thread(reLoginTask)).start();
            }
        });
    }

    private void refreshGLView() {
        if (wifiCar.isVersion20()) {
            mJpegView.setVisibility(View.VISIBLE);
            mH264View.setVisibility(View.GONE);
        } else {
            mJpegView.setVisibility(View.GONE);
            mH264View.setVisibility(View.VISIBLE);
        }
    }

    public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
        Log.i(TAG, "onKeyDown key=" + paramInt + " event=" + paramKeyEvent);
        Toast.makeText(this, "k:" + paramInt + " k:" + paramKeyEvent.getKeyCode(), Toast.LENGTH_SHORT).show();
        return super.onKeyDown(paramInt, paramKeyEvent);
    }

    public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent) {
        return super.onKeyUp(paramInt, paramKeyEvent);
    }

    protected void onPause() {

        super.onPause();
    }

    Runnable connectRunnable = new Runnable() {
        public void run() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetworkInfo.isConnected()) {

                WificarMain.this.ConnnectOut_timer = new Timer();

                AppLog.d(TAG, "--->connectRunnable. connecting to wificar.....");
                boolean result = false;
                try {
                    result = WificarMain.this.wifiCar.setCmdConnect();
                } catch (IOException e) {

                }
                if (result) {
                    AppLog.d(TAG, "--->wificar socket connect Succuess!");
                    sendToastMessage("Socket Connect Succuess!");
                } else {
                    AppLog.d(TAG, "--->wificar socket connect failed!");
                    sendToastMessage("Socket Connect Failed!");
                }
            }
        }
    };

    protected void onResume() {
        super.onResume();
        AppLog.i(TAG, "on Resume");


        //start wificar socket, use wifi connection
        if (this.wifiCar.isSocketConnected() == 0) (new Thread(connectRunnable)).start();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        AppLog.d(TAG, "on destory");

        this.handler.removeCallbacks(this.LMovingTask2s);
        this.handler.removeCallbacks(this.LMovingTask3s);

    }

    public void onClick(View paramView) {
        return;
    }

    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
        AppLog.d(TAG, "on onTouch");
        //super.onTouch(paramView, paramMotionEvent);
        if (WificarMain.this.LMoving > 0) {
            AppLog.d(TAG, "on onTouch, already moving ,return");
            sendToastMessage("already moving!");
            return true;
        }
        WificarMain.this.LMoving = 0;
        (new Thread(LMovingTask2s)).start();
        //WificarMain.this.handler.postDelayed(LMovingTask2s, 100L);
        return true;
    }

    public void onChronometerTick(Chronometer paramChronometer) {
    }


    class ConnectOut extends TimerTask {
        public void run() {
            WificarMain.this.ConnnectOut_timer.cancel();
            WificarMain.this.ConnnectOut_timer = null;

            Message message = new Message();
            message.what = MESSAGE_CONNECT_TO_CAR_FAIL;
            WificarMain.this.handler.sendMessage(message);
            AppLog.i(TAG, "connect_status --->connect out!");
        }
    }

    private String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    public boolean handleMessageinUI(Message param1Message) {
        boolean handled = false;
        switch (param1Message.what) {
            case MESSAGE_CONNECT_TO_CAR_FAIL:
                if (SHOW_DEBUG_MESSAGE)
                    Toast.makeText(WificarMain.this, "failed to connect!", 0).show();
                handled = true;
                break;
            case MESSAGE_MAKE_TOAST:
                if (SHOW_DEBUG_MESSAGE) {
                    String msg = param1Message.getData().getString(BUNDLE_KEY_TOAST_MSG);
                    Toast.makeText(WificarMain.this, msg, 0).show();
                }
                handled = true;
                break;
            default:
                return false;
        }
        return handled;
    }

    public void sendToastMessage(String str) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_TOAST_MSG, str);

        Message msg = handler.obtainMessage(MESSAGE_MAKE_TOAST);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private Thread LMovingTask2s = new Thread() {
        public void run() {
            AppLog.i(TAG, "run move in 100ms");
            try {
                WificarMain.this.wifiCar.move(WificarMain.this.mLeft, WificarMain.this.mLeftSpeed);
                WificarMain.this.wifiCar.move(WificarMain.this.mRight, WificarMain.this.mRightSpeed);
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    };

    private Thread CamMovingTask = new Thread() {
        public void run() {
            AppLog.i(TAG, "run camera move in 100ms");
            try {
                wifiCar.camMove(mCameraControl);
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    };

    private Thread videoEnableThread = new Thread() {
        public void run() {
            AppLog.i(TAG, "videoEnable:" + mJpegStart);
            try {
                WificarMain.this.wifiCar.enableVideo(mJpegStart, true);
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    };

    WheelView.OnWheelViewMoveListener mControllerListener = new WheelView.OnWheelViewMoveListener() {
        @Override
        public void onValueChanged(int status, float angle, float distance) {
            //

            if (status == 100) {
                mLastTimeCon = System.currentTimeMillis();
                counter = 0;
            } else if (status == 200) {
                double now = System.currentTimeMillis();
                if (now - mLastTimeCon > TIME_INTERVAL_MS) {
                    //report & counter

                    //Log.i("WHEELVIEW","move, status:" + status + " counter:" + counter);
                    //caculate left,leftspeed, right,right speed;
                    if (WificarMain.this.wifiCar.isVersion20()) {
                        sendMoveCommand20(status, angle, distance);
                    } else {
                        sendMoveCommand30(status, angle, distance);
                    }
                    counter = 0;
                    mLastTimeCon = now;
                } else {
                    //ignore value change
                    counter++;
                }
            } else {
                //stop
                if (WificarMain.this.wifiCar.isVersion20()) {
                    sendMoveCommand20(status, angle, distance);
                } else {
                    sendMoveCommand30(status, angle, distance);
                }
            }
        }
    };

    private int camContrlCount = 0;
    private double camContrlLastTimeCon = 0;
    WheelView.OnWheelViewMoveListener mCamControllerListener = new WheelView.OnWheelViewMoveListener() {
        @Override
        public void onValueChanged(int status, float angle, float distance) {
            if (status == 100) {//down
                camContrlLastTimeCon = System.currentTimeMillis();
            } else if (status == 200) {//move
                double now = System.currentTimeMillis();
                if (now - camContrlLastTimeCon > TIME_INTERVAL_MS) {
                    if (wifiCar.isVersion20()) {
                        sendCamMoveCommand20(status, angle, distance);
                    } else {
                        sendCamMoveCommand30(status, angle, distance);
                    }
                    camContrlLastTimeCon = now;
                }
            } else {
                //stop
                if (wifiCar.isVersion20()) {
                    sendCamMoveCommand20(status, angle, distance);
                } else {
                    sendCamMoveCommand30(status, angle, distance);
                }
            }
        }
    };

    void sendCamMoveCommand20(int status, float angle, float distance) {

    }

    void sendCamMoveCommand30(int status, float angle, float distance) {
        if ((angle >= 0 && angle <= 45) || (angle >= 315 && angle <= 360)) {
            mCameraControl = 0;//up
        } else if (angle >= 225 && angle <= 315) {
            mCameraControl = 4;//left
        } else if (angle >= 135 && angle <= 225) {
            mCameraControl = 2;//down
        } else if (angle >= 45 && angle <= 135) {
            mCameraControl = 6;//right
        } else {
        }

        if (status == 300) {
            //move finish
            if (mCameraControl%2 == 0)  mCameraControl++;//motor stop
        }

        (new Thread(CamMovingTask)).start();
    }

    void sendMoveCommand20(int status, float angle, float distance) {
        if (status == 200) {
            int left = 4;
            int right = 1;
            int leftspeed = 0;
            int rightspeed = 0;

            if (distance <= 30) {
                //stop
                leftspeed = rightspeed = 0;
            } else if ((angle >= 0 && angle <= 10) || (angle >= 350 && angle <= 360)) {
                //forward
                left = 4;
                right = 1;
                leftspeed = rightspeed = Math.round(distance / 10);
            } else if (angle >= 280 && angle <= 350) {
                //left & forward
                left = 4;
                right = 1;
                leftspeed = Math.round(distance * ((angle - 270) / 90) / 20);
                rightspeed = Math.round(distance / 10);
            } else if (angle > 10 && angle < 80) {
                //right & forward
                left = 4;
                right = 1;
                leftspeed = Math.round(distance / 10);
                rightspeed = Math.round(distance * ((90 - angle) / 90) / 20);
            } else if (angle >= 260 && angle <= 280) {
                //treat as left & forward
                left = 5;
                right = 1;
                leftspeed = Math.round(distance / 10);
                rightspeed = Math.round(distance / 10);
            } else if (angle >= 80 && angle <= 100) {
                //reat as right & forward
                left = 4;
                right = 2;
                leftspeed = Math.round(distance / 10);
                rightspeed = Math.round(distance / 10);
            } else {
                //backward
                left = 5;
                right = 2;
                leftspeed = rightspeed = Math.round(distance / 10);
            }

            WificarMain.this.mLeft = left;
            WificarMain.this.mLeftSpeed = leftspeed;
            WificarMain.this.mRight = right;
            WificarMain.this.mRightSpeed = rightspeed;
            (new Thread(LMovingTask2s)).start();
        } else {
            WificarMain.this.mLeft = 4;
            WificarMain.this.mLeftSpeed = 0;
            WificarMain.this.mRight = 1;
            WificarMain.this.mRightSpeed = 0;
            (new Thread(LMovingTask2s)).start();
        }
    }

    int mForward = 0;
    int mDirect = 3;
    boolean mHalfspeed = false;
    boolean mHalfRLSpeed = false;

    void sendMoveCommand30(int status, float angle, float distance) {
        if (status == 200) {//moving
            int forward = 0; //0-stop;1-forward;2-backward
            int direct = 3;//5-left;3-middle;4-right
            boolean halfspeed = false;
            boolean halfRLSpeed = false;

            if (distance <= 30) {
                //stop
                forward = 0;
                direct = 3;
            } else if ((angle >= 0 && angle <= 10) || (angle >= 350 && angle <= 360)) {
                //forward
                forward = 1;
                direct = 3;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = false;
            } else if (angle >= 280 && angle <= 350) {
                //left & forward
                forward = 1;
                direct = 5;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = true;
            } else if (angle > 10 && angle < 80) {
                //right & forward
                forward = 1;
                direct = 4;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = true;
            } else if (angle >= 260 && angle <= 280) {
                //treat as left & forward, with high RLSpeed
                forward = 1;
                direct = 5;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = false;
            } else if (angle >= 80 && angle <= 100) {
                //treat as right & forward, with high RLSpeed
                forward = 1;
                direct = 4;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = false;
            } else if (angle >= 100 && angle <= 170) {
                //left & backward
                forward = 2;
                direct = 5;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = false;//TODO:
            } else if (angle >= 190 && angle <= 260) {
                //right & backward
                forward = 2;
                direct = 4;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = false;//TODO:
            } else {
                //backward
                forward = 2;
                direct = 3;
                halfspeed = distance <= 80 ? true : false;
                halfRLSpeed = false;
            }

            WificarMain.this.mForward = forward;
            WificarMain.this.mDirect = direct;
            WificarMain.this.mHalfspeed = halfspeed;
            WificarMain.this.mHalfRLSpeed = halfRLSpeed;
            (new Thread(LMovingTask3s)).start();
        } else {//up
            WificarMain.this.mForward = 0;
            WificarMain.this.mDirect = 3;
            WificarMain.this.mHalfspeed = false;
            WificarMain.this.mHalfRLSpeed = false;
            (new Thread(LMovingTask3s)).start();
        }
    }

    private Thread LMovingTask3s = new Thread() {
        public void run() {
            AppLog.i(TAG, "run move for version 3.0");
            try {
                WificarMain.this.wifiCar.move(WificarMain.this.mForward, WificarMain.this.mHalfspeed ? 1 : 0);
                WificarMain.this.wifiCar.move(WificarMain.this.mDirect, WificarMain.this.mHalfRLSpeed ? 1 : 0);
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    };

    private Thread reLoginTask = new Thread() {
        public void run() {
            AppLog.i(TAG, "run reLoginTask");
            try {
                WificarMain.this.wifiCar.reLogin();
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    };
    private OnClickListener buttonJpegClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            if (mJpegStart) {
                mJpegStart = false;
                mJpegView.stopPlayback();
                mJpegButton.setImageDrawable(buttonJpegStop);
                mJpegButton.invalidateDrawable(buttonJpegStop);
            } else {
                mJpegStart = true;
                mJpegView.startPlayback();
                mJpegButton.setImageDrawable(buttonJpegStart);
                mJpegButton.invalidateDrawable(buttonJpegStart);
            }
            (new Thread(videoEnableThread)).start();
        }
    };

    private OnClickListener buttonWifiClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
            startActivity(wifiSettingsIntent);
        }
    };
}
