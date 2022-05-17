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

import java.util.Timer;
import java.util.TimerTask;

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
  
  public static final String TAG = "WificarMain";
  
  public RelativeLayout Parent;
	public WificarLayoutParams wificarLayoutParams;
	
  protected void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);

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

  }
  
  protected void onStop() {
    super.onStop();
  }
  
  public void onClick(View paramView) {
	  return;
  }
  
  public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
	  return true;
  }
  
  public void onChronometerTick(Chronometer paramChronometer) {
  }
}
