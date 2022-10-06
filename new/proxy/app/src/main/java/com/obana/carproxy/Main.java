package com.obana.carproxy;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.DhcpInfo;
import android.net.NetworkRequest;

import android.net.NetworkCapabilities;
import android.net.Network;

import java.util.List;

import com.obana.carproxy.utils.*;


public class Main extends Activity implements View.OnClickListener, View.OnTouchListener {
    public static final String TAG = "CarProxy_Main";
    private static final boolean SHOW_DEBUG_MESSAGE = false;
    private static final String BUNDLE_KEY_TOAST_MSG = "toast_msg";
    private static final int DBG_MSG_DELAY_MS = 1500;
    private static final int MESSAGE_CONNECT_TO_CAR_FAIL = 0x1001;
    private static final int MESSAGE_PRINT_DEBUG_MSG = 0x1002;
    private static final int MESSAGE_CONNECT_CLIENT = 0x1003;
    private static final int MESSAGE_MAKE_TOAST = 0x1004;
    private static final int MESSAGE_REFRESH_WIFI = 0x1005;
    private CarProxy mCarProxy = null;
    private Handler mHandler = null;

    private TextView mCarWifi;
    private TextView mDebugMsgText;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        setContentView(R.layout.main);
        mCarWifi = findViewById(R.id.value_car_wifi);
        mDebugMsgText = findViewById(R.id.textView_debug_msg);

        mCarProxy = new CarProxy(this);
        mHandler = new Handler() {
            public void handleMessage(Message param1Message) {

                super.handleMessage(param1Message);
                handleMessageinUI(param1Message);
            }
        };

        registScreen();
        registWifiStateChange();
        
        //start wificar socket, use wifi connection
        new Thread(wifiRunnable).start();

        //start cloud socket, use cell connection
        new Thread(cloudRunnable).start();

        //start debug msg ui
        Message msg = mHandler.obtainMessage(MESSAGE_PRINT_DEBUG_MSG);
        mHandler.sendMessageDelayed(msg,DBG_MSG_DELAY_MS);
    }

    BroadcastReceiver mScreenBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                AppLog.i(TAG, "ACTION_SCREEN_ON, reconnect all......");

                //start wificar socket, use wifi connection
                new Thread(wifiRunnable).start();

                //start cloud socket, use cell connection
                new Thread(cloudRunnable).start();
            }
        }
    };

    BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            AppLog.i(TAG, "mWifiBroadcastReceiver:" + intent);
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    if (state == NetworkInfo.State.CONNECTED) {
                        //reconnect car wifi

                        WifiManager manager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        DhcpInfo dhcpInfo = manager.getDhcpInfo();
                        AppLog.i(TAG, "wifi state change received, dhcp ip addr:" + int2ip(dhcpInfo.gateway));
                        if (mCarProxy.matchWifiCarAddr(int2ip(dhcpInfo.gateway))) {
                            AppLog.i(TAG, "car wifi connected, ready to connect cmd socket....." + intent);

                            //refresh UI
                            Message newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_WIFI,1,0);
                            mHandler.sendMessage(newMsg);
                            try {
                                mCarProxy.connectToCarCmd();
                            } catch (Exception e) {

                            }
                        } else {
                            //refresh UI
                            Message newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_WIFI,0,0);
                            mHandler.sendMessage(newMsg);
                        }
                    }
                }
            }
        }
    };

    private void registScreen() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenBroadcastReceiver, filter);
    }

    private void registWifiStateChange() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiBroadcastReceiver, filter);

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

        AppLog.i(TAG, "on Resume");
    }

    Runnable wifiRunnable = new Runnable() {
        public void run() {
            AppLog.d(TAG, "STEP1: connecting to car wifi .....");
            refreshUI(VIEW_ID_CAR_WIFI, false, 0);
            WifiManager manager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = manager.getConnectionInfo();
            String ssid = wifiInfo.getSSID().toString();
            int addr = wifiInfo.getIpAddress();

            //ssid is not available after Android6.0
            AppLog.d(TAG, "---->wifi info, ssid:" + ssid + " local ip:" + int2ip(addr));
            DhcpInfo dhcpInfo = manager.getDhcpInfo();
            String dhcpAddr = int2ip(dhcpInfo.gateway);
            AppLog.d(TAG, "---->wifi info, dhcp ip addr:" + dhcpAddr);
            if ((ssid != null && ssid.contains("Rover")) || mCarProxy.matchWifiCarAddr(dhcpAddr)) {

                boolean rest = false;
                //AppLog.d(TAG, "--->wificar socket connecting .....");
                try {
                    rest = mCarProxy.connectToCarCmd();
                    //TODO:this should be replaced by cmd:101
                    //rest = mCarProxy.ConnectToCarMedia();
                } catch (Exception e) {

                }
                AppLog.d(TAG, "--->wificar socket connect result:" + rest);
                refreshUI(VIEW_ID_CAR_WIFI, true, 0);

            } else if (!mCarProxy.matchWifiCarAddr(dhcpAddr)) {
                //connect to spcified ssid
                WifiConfiguration tempConfig = findSpecifiedSsid(manager, "Pixel");
                if (tempConfig != null) {
                    AppLog.i(TAG, "---->wifi not connected, enable it ....");
                    boolean enabled = manager.enableNetwork(tempConfig.networkId, true);
                    AppLog.i(TAG, "enable wifi status enable=" + enabled);
                }
            }
        }
    };

    //cloud socket thread
    Runnable cloudRunnable = new Runnable() {
        public void run() {
            AppLog.i(TAG, "STEP2: request cell network .....");

            try {
                requestMobileSocket();
            } catch (IOException e) {
                AppLog.d(TAG, "--->Cloud Socket Connect failed!");
                //do nothing
            }
        }
    };

    WifiConfiguration findSpecifiedSsid(WifiManager wifiManager, String ssid) {
        if (wifiManager != null) {

            List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
            AppLog.i(TAG, "---> 1111");
            for (WifiConfiguration existingConfig : existingConfigs) {
                AppLog.i(TAG, "---> 22222 " + existingConfig.SSID);
                if (existingConfig.SSID.contains(ssid)) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    private void requestMobileSocket() throws IOException {
        //create sock to cloud
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest build = builder.build();
        AppLog.i(TAG, "---> start request cell network");
        connectivityManager.requestNetwork(build, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                AppLog.i(TAG, "--->cell network ready, connecting to cloud ....");

                boolean ret = false;
                try {
                    ret = mCarProxy.ConnectToCloud(network);
                } catch (Exception e) {
                    return;
                }

            }
        });
    }

    private static final int VIEW_ID_DEBUG_MSG = 10;
    private static final int VIEW_ID_MOBILE_NETWORK = 11;
    private static final int VIEW_ID_CAR_WIFI = 12;
    private void refreshUI(int viewId, boolean on, int value) {
        switch (viewId) {

            case VIEW_ID_DEBUG_MSG:
                mDebugMsgText.setText(mCarProxy.printDebugMsg());
                break;
            case VIEW_ID_MOBILE_NETWORK:
                //mDebugMsgText.setText(mCarProxy.printDebugMsg());
                break;
            case VIEW_ID_CAR_WIFI:
                mCarWifi.setText(on?"connected":"disconnected");
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

    public boolean handleMessageinUI(Message msg) {
        boolean handled = false;
        //AppLog.d(TAG, "handleMessageinUI:" + param1Message);
        switch (msg.what) {
            case MESSAGE_CONNECT_TO_CAR_FAIL:
                if (SHOW_DEBUG_MESSAGE)
                    Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
                handled = true;
                break;
            case MESSAGE_MAKE_TOAST:
                if(SHOW_DEBUG_MESSAGE) {
                    String msgStr = msg.getData().getString(BUNDLE_KEY_TOAST_MSG);
                    Toast.makeText(this, msgStr, Toast.LENGTH_SHORT).show();
                }
                handled = true;
                break;
            case MESSAGE_PRINT_DEBUG_MSG:

                refreshUI(VIEW_ID_DEBUG_MSG, false,0);
                Message newMsg = mHandler.obtainMessage(MESSAGE_PRINT_DEBUG_MSG);
                mHandler.sendMessageDelayed(newMsg, DBG_MSG_DELAY_MS);
                break;
            case MESSAGE_REFRESH_WIFI:
                refreshUI(VIEW_ID_CAR_WIFI, msg.arg1 > 0,0);
                handled = true;
                break;
            case MESSAGE_CONNECT_CLIENT:
                (new Thread(connectClientTask)).start();
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

    }

    private Thread connectClientTask = new Thread() {
        public void run() {
            AppLog.i(TAG, "connect to client task starting ...");
            try {
                mCarProxy.ConnectToClientP2P();
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    };

    public void connectToClient() {
        Message msg = mHandler.obtainMessage(MESSAGE_CONNECT_CLIENT);
        mHandler.sendMessage(msg);
    }
}
