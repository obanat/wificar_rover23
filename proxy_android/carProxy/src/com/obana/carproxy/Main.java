package com.obana.carproxy;

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
import android.net.DhcpInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.*;
import android.net.NetworkCapabilities;
import android.net.Network;

import java.util.Timer;
import java.util.TimerTask;
import com.obana.carproxy.utils.*;
import android.view.View.OnClickListener;


public class Main extends Activity implements View.OnClickListener, View.OnTouchListener{
  public static final String TAG = "Main_Activity";
  private static final boolean SHOW_DEBUG_MESSAGE = false;
  private static final String BUNDLE_KEY_TOAST_MSG = "toast_msg";

  private static final int MESSAGE_CONNECT_TO_CAR_FAIL = 0x1001;
  private static final int MESSAGE_MAKE_TOAST = 0x1002;
  private static final int MESSAGE_THREAD_UPDATE = 0x1003;
  private static final int MESSAGE_RECONNECT_CAR = 0x1004;
  
  private CarProxy mCarProxy = null;
  private Handler mHandler = null;

  private TextView mCarCmdSocketText;
  private TextView mCloudCmdSocketText;
  private TextView mCarMediaSocketText;

  private TextView mCmdUplinkThreadText;
  private TextView mMediaUplinkThreadText;
  private TextView mCmdDownlinkThreadText;

  protected void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);

    setContentView(R.layout.main);
    mCarCmdSocketText = findViewById(R.id.value_car_cmd);
    mCloudCmdSocketText = findViewById(R.id.value_cloud_cmd);
    mCarMediaSocketText = findViewById(R.id.value_car_media);
    mCmdUplinkThreadText = findViewById(R.id.value_cmd_uplink);
    mMediaUplinkThreadText = findViewById(R.id.value_media_uplink);
    mCmdDownlinkThreadText = findViewById(R.id.value_cmd_downlink);

    mCarProxy = new CarProxy(this);
    mHandler = new Handler() {
      public void handleMessage(Message param1Message) {

        super.handleMessage(param1Message);
        handleMessageinUI(param1Message);
      }
    };
  }
  
  BroadcastReceiver mScreenBroadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AppLog.i(TAG, "mScreenBroadcastReceiver:" + intent);
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            AppLog.i(TAG, "ACTION_SCREEN_ON");
            try {
                mCarProxy.b_connected_car_cmd = false;
                mCarProxy.b_connected_cloud_cmd = false;
                mCarProxy.b_connected_car_media = false;
                mCarProxy.b_connected_cloud_media = false;
            
                reConnectCarDelayed(1);
                requestMobileSocket();

            } catch (IOException e) {
                refreshUI(BUTTON_CAR_SOCKET,false,0);
            }
        }
      }
  };
  
  private void registScreen() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SCREEN_ON);
    registerReceiver(mScreenBroadcastReceiver, filter);
  }
  
  private void unUregistScreen() {
    unregisterReceiver(mScreenBroadcastReceiver);
  }

  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
    AppLog.i(TAG, "onKeyDown key:" + paramInt);
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
    registScreen();
    AppLog.i(TAG, "on Resume");
    Runnable runnable = new Runnable() {
        public void run() {
          AppLog.d(TAG, "--->onResume. connecting to wificar & cloud .....");
          WifiManager manager = (WifiManager)getSystemService("wifi");
          WifiInfo wifiInfo = manager.getConnectionInfo();
          String ssid = wifiInfo.getSSID().toString();
          int addr = wifiInfo.getIpAddress();
          AppLog.d(TAG, "---->wifi connected, ssid1:" + ssid + " local ip:" + int2ip(addr));
          DhcpInfo dhcpInfo = manager.getDhcpInfo();
          if ((ssid != null && ssid.contains("Rover")) || int2ip(dhcpInfo.gateway).equals("192.168.1.100")) {

            boolean rest = false;
            AppLog.d(TAG, "--->wificar socket connecting .....");
            try {
                rest = mCarProxy.connectToCarCmd();
                //rest = mCarProxy.ConnectToCarMedia();
            } catch (IOException e) {
                refreshUI(BUTTON_CAR_SOCKET,false,0);
            }
            AppLog.d(TAG, "--->wificar socket connect result:" + rest);
            refreshUI(BUTTON_CAR_SOCKET,true,0);

          }
        }
      };

      //cloud socket thread 
      Runnable cloudRunnable = new Runnable() {
        public void run() {
            AppLog.d(TAG, "--->cloud socket connecting .....");

            try {
                requestMobileSocket();
            } catch (IOException e) {
                AppLog.d(TAG, "--->Cloud Socket Connect failed!");
                refreshUI(BUTTON_CLOUD_SOCKET,false,0);
                return;
                //do nothing
            }
            //refreshUI(BUTTON_CLOUD_SOCKET,true,0);
        }
      };

      //start wificar socket, use wifi connection
      if (mCarProxy.isCarSocketConnected() == 0)
        (new Thread(runnable)).start(); 

      //start cloud socket, use cell connection
      if (mCarProxy.isCloudSocketConnected() == 0)
        (new Thread(cloudRunnable)).start(); 
  }
  
      private void requestMobileSocket () throws IOException {
        //create sock to cloud
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest build = builder.build();
        AppLog.d(TAG, "--->Cloud request Mobile Network");
        connectivityManager.requestNetwork(build, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                 AppLog.i(TAG, "--->cellular ready, create cloud socket....");
                try {
                    mCarProxy.ConnectToCloudCmd(network);
                    //ConnectToCloudMedia(network);
                } catch (Exception e) {
                    refreshUI(BUTTON_CLOUD_SOCKET,false,0);
                    return;
                }
                refreshUI(BUTTON_CLOUD_SOCKET,true,0);
            }
        });
    }
    
  private static final int BUTTON_CAR_SOCKET = 10;
  private static final int BUTTON_CLOUD_SOCKET = 11;
  private static final int BUTTON_THREAD_UPLINK = 12;
  private static final int BUTTON_THREAD_DOWNLINK = 13;
  
  private void refreshUI(int button, boolean on,int value) {
    switch (button) {
        case BUTTON_CAR_SOCKET:
        mCarCmdSocketText.setText(on?"ON":"OFF");
        mCarMediaSocketText.setText(on?"ON":"OFF");
        if (!on) {
            mCmdUplinkThreadText.setText("0");
            mCmdDownlinkThreadText.setText("0");
            mMediaUplinkThreadText.setText("0");
        }
        break;
        case BUTTON_CLOUD_SOCKET:
        mCloudCmdSocketText.setText(on?"ON":"OFF");
        break;
        case BUTTON_THREAD_UPLINK:
        mCmdUplinkThreadText.setText(String.valueOf(value));
        break;
        case BUTTON_THREAD_DOWNLINK:
        mCmdDownlinkThreadText.setText(String.valueOf(value));
        break;
        default:
        break;
    }
  }
  protected void onStop() {
    super.onStop();
  }

  protected void onDestroy() {
    super.onDestroy();
    unUregistScreen();
    AppLog.d(TAG, "on destory");

  }
  public void onClick(View paramView) {
      return;
  }
  
  public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
    AppLog.d(TAG, "on onTouch");

    
    return true;
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
    AppLog.d(TAG, "handleMessageinUI:" + param1Message);
    switch (param1Message.what) {
      case MESSAGE_CONNECT_TO_CAR_FAIL:
        if(SHOW_DEBUG_MESSAGE)
            Toast.makeText(this, "failed to connect!", 0).show();
        handled = true;
        break;
      case MESSAGE_MAKE_TOAST:
        if(SHOW_DEBUG_MESSAGE) {
            String msg = param1Message.getData().getString(BUNDLE_KEY_TOAST_MSG);
            Toast.makeText(this, msg, 0).show();
        }
        handled = true;
        break;
      case MESSAGE_THREAD_UPDATE:
            if (param1Message.arg1 > 0) {
                //cmd thread
                mCmdDownlinkThreadText.setText(String.valueOf(param1Message.arg2));
            } else {
                mMediaUplinkThreadText.setText(String.valueOf(param1Message.arg2));
            }
        handled = true;
        break;
      case MESSAGE_RECONNECT_CAR:
        (new Thread(reconnectTask)).start();
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

     Message msg = mHandler.obtainMessage(MESSAGE_MAKE_TOAST);
     msg.setData(bundle);
     mHandler.sendMessage(msg);
 }

 public void sendThreadUpdateMessage(boolean isCmd, int value) {
     Message msg = mHandler.obtainMessage(MESSAGE_THREAD_UPDATE, isCmd?1:0, value);
     mHandler.sendMessage(msg);
 }

 private Thread reconnectTask = new Thread() {
    public void run() {
        AppLog.i(TAG, "reconnectTask car cmd socket in 100ms");
        try {
            if (mValueReconnect == VALUE_CMD_CONNECT) {
                mCarProxy.connectToCarCmd();
            } else if(mValueReconnect == VALUE_MEDIA_CONNECT) {
                mCarProxy.ConnectToCarMedia(magicValue);
            } else {
            }
        } catch (IOException iOException) {
          iOException.printStackTrace();
        } 
    }
  };
  
  private static final int VALUE_CMD_CONNECT = 0x1000;
  private static final int VALUE_MEDIA_CONNECT = 0x1001;
  private int mValueReconnect = 0;
  private int magicValue = 0;
  public void reConnectCarDelayed(int delay) {
    //mHandler.postDelayed(reconnectTask, 100L);this is run socket in main thread, not safe!!!
    Message msg = mHandler.obtainMessage(MESSAGE_RECONNECT_CAR);
    mValueReconnect = VALUE_CMD_CONNECT;
    mHandler.sendMessageDelayed(msg, delay);
  }
  public void ConnectToCarMedia(int i) {
    Message msg = mHandler.obtainMessage(MESSAGE_RECONNECT_CAR);
    mValueReconnect = VALUE_MEDIA_CONNECT;
    magicValue = i;
    mHandler.sendMessage(msg);
  }
}
