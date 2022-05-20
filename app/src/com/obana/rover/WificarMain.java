package com.obana.rover;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.*;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Timer;
import java.util.TimerTask;
import com.obana.rover.utils.*;
public class WificarMain extends Activity implements View.OnClickListener, View.OnTouchListener, Chronometer.OnChronometerTickListener {
  private static final int DOUBLE_PRESS_INTERVAL = 2000;
  
  public static final int MESSAGE_BATTERY_0 = 1010;
  
  public static final int MESSAGE_BATTERY_100 = 1005;
  
  public static final int MESSAGE_BATTERY_20 = 1007;
  
  public static final int MESSAGE_BATTERY_25 = 1009;
  
  public static final int MESSAGE_BATTERY_50 = 1008;
  
  public static final int MESSAGE_BATTERY_75 = 1006;
  
  public static final int MESSAGE_BATTERY_UNKNOWN = 1011;
  
  public static final int MESSAGE_CAMERACHANGE_END = 2007;
  
  public static final int MESSAGE_CAMERA_DEGREE = 5010;
  
  public static final int MESSAGE_CAMERA_RESET_END = 5011;
  
  public static final int MESSAGE_CONNECT_TO_CAR = 1004;
  
  public static final int MESSAGE_CONNECT_TO_CAR_FAIL = 1002;
  
  public static final int MESSAGE_CONNECT_TO_CAR_SUCCESS = 1003;
  public static final int MESSAGE_GSENSER_ENABLE = 2012;
  
  public static final int MESSAGE_GSENSER_START = 5005;
  
  public static final int MESSAGE_GSENSOR_END = 2008;
  
  public static final int MESSAGE_HEARTBEAT_WARNING = 5003;
  
  public static final int MESSAGE_HOME_PRESS = 5008;
  
  public static final int MESSAGE_HORZONTAL_HIDE = 5004;
  
  public static final int MESSAGE_LISTENING_END = 2010;
  
  public static final int MESSAGE_NIGHT_LIGNTH_END = 2009;
  
  public static final int MESSAGE_NO_RECORD = 5002;
  
  public static final int MESSAGE_NO_SHOOTING = 5001;
  
  public static final int MESSAGE_PHONE_CAPACITY_LOW = 1013;
  
  public static final int MESSAGE_PHOTOGRAPH_END = 2005;
  
  public static final int MESSAGE_PING_FAIL = 3001;
  
  public static final int MESSAGE_PLAYPATH_END = 2006;
  
  public static final int MESSAGE_RECORDPATH_COMPLETE = 2003;
  
  public static final int MESSAGE_SCALE_END = 1012;
  
  public static final int MESSAGE_SCROLL_LR_FLAG = 5009;
  
  public static final int MESSAGE_SHOOTING_COMPLETE = 2001;
  
  public static final int MESSAGE_SHOOTING_START = 2000;
  
  public static final int MESSAGE_SHOWINFORMATION_END = 1001;
  
  public static final int MESSAGE_SINGLRESETCLICK = 5012;
  
  public static final int MESSAGE_SOUNDSLIDER_CANCEL = 999;
  
  public static final int MESSAGE_SPK_END_SUCCESS = 3002;
  
  public static final int MESSAGE_STOP_PLAYPATH = 2002;
  
  public static final int MESSAGE_TALK_PRESS = 5007;
  public static final int MESSAGE_MAKE_TOAST = 6001;
  public static final boolean SHOW_DEBUG_MESSAGE = true;
  public static final String BUNDLE_KEY_TOAST_MSG = "Tmessage";
  
  public static final String TAG = "Wificar_Activity";
  
  public RelativeLayout Parent;
  public WificarLayoutParams wificarLayoutParams;
  public Timer ConnnectOut_timer = null;
  private WifiCar wifiCar = null;
  private Handler handler = null;
  private int LMoving = 0;
  protected void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);

    this.wifiCar = new WifiCar(this);
    this.handler = new Handler() {
      public void handleMessage(Message param1Message) {
        if (!handleMessageinUI(param1Message)) {
            super.handleMessage(param1Message);
        }    
      }
    };
    this.wificarLayoutParams = WificarLayoutParams.getWificarLayoutParams(this);
    setHLayoutparams();
  }
  
  public void setHLayoutparams()
  {
        this.Parent = new RelativeLayout(getApplicationContext());
        setContentView(this.Parent, wificarLayoutParams.parentParams);

        //refreshUIListener();
  }

  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
	Log.e(TAG, "onKeyDown key=" + paramInt + " event=" + paramKeyEvent);
    Toast.makeText(this, "k:" + paramInt + " k:" + paramKeyEvent.getKeyCode(), Toast.LENGTH_SHORT).show(); 
    return super.onKeyDown(paramInt, paramKeyEvent);
  }
  
  public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent) {
    return super.onKeyUp(paramInt, paramKeyEvent);
  }
  
  protected void onPause() {
    
    super.onPause();
  }
  
  protected void onResume() {
    super.onResume();
    AppLog.i(TAG, "on Resume");
    Runnable runnable = new Runnable() {
        public void run() {
          AppLog.d(TAG, "--->socket connecting .....");

          WifiInfo wifiInfo = ((WifiManager)WificarMain.this.getSystemService("wifi")).getConnectionInfo();
          String ssid1 = wifiInfo.getSSID().toString();
          int addr = wifiInfo.getIpAddress();
          AppLog.d(TAG, "---->wifi connected, ssid1:" + ssid1);

          ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
          String ssid2 = networkInfo.getExtraInfo();
          AppLog.d(TAG, "---->wifi connected, ssid2:" + ssid2 + " ip:" + int2ip(addr));
          
          if ((ssid1 != null && ssid1.contains("Rover")) || (ssid2 != null && ssid2.contains("Rover"))) {
            WificarMain.this.ConnnectOut_timer = new Timer();
            WificarMain.this.ConnnectOut_timer.schedule(new WificarMain.ConnectOut(), 6000L);
            boolean rest = false;
            try {
                rest = WificarMain.this.wifiCar.setConnect();
            } catch (IOException e) {
                //do nothing
            }
            AppLog.d(TAG, "--->connect socket & main loop result:" + rest);
            //WificarMain.this.wifiCar.updatedChange();
             sendToastMessage("Socket Connect Succuess!");

            return;
          }
          sendToastMessage("wifi not match, just exit!");
        }
      };
      if (this.wifiCar.isSocketConnected() == 0)
        (new Thread(runnable)).start(); 
  }
  
  protected void onStop() {
    super.onStop();
  }

  protected void onDestroy() {
    super.onDestroy();
    AppLog.d(TAG, "on destory");

    this.handler.removeCallbacks(this.LMovingTask2s);

  }
  public void onClick(View paramView) {
      return;
  }
  
  public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
    AppLog.d(TAG, "on onTouch");
    if (WificarMain.this.LMoving > 0) {
        AppLog.d(TAG, "on onTouch, already moving ,return");
        sendToastMessage("already moving!");
        return true;
    }
    WificarMain.this.LMoving = 0;
    WificarMain.this.handler.postDelayed(LMovingTask2s, 100L);
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
        if(SHOW_DEBUG_MESSAGE)
            Toast.makeText(WificarMain.this, "failed to connect!", 0).show();
        handled = true;
        break;
      case MESSAGE_MAKE_TOAST:
        if(SHOW_DEBUG_MESSAGE) {
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

 private Runnable LMovingTask2s = new Runnable() {
    public void run() {
        AppLog.e(WificarMain.this.TAG, "run move in 100ms");
        try {
          WificarMain.this.wifiCar.move(0, 10);
        } catch (IOException iOException) {
          iOException.printStackTrace();
        } 

        if (WificarMain.this.LMoving < 20) {
            WificarMain.this.handler.postDelayed(this, 100L);
            WificarMain.this.LMoving++;
        } else {
            WificarMain.this.LMoving = 0;
            WificarMain.this.handler.removeCallbacks(this);
        }
    }
  };
}
