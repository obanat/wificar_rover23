package com.obana.carproxy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final int MESSAGE_REFRESH_CLOUD = 0x1006;
    private CarProxy mCarProxy = null;
    private Handler mHandler = null;

    private TextView mCarWifi;
    private TextView mCellgular;
    private TextView mDebugMsgText;

    PowerManager.WakeLock mWakeLock;
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        setContentView(R.layout.main);
        mCarWifi = findViewById(R.id.value_car_wifi);
        mDebugMsgText = findViewById(R.id.textView_debug_msg);
        mCellgular = findViewById(R.id.textView_ipaddr);
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

        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
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
                            AppLog.i(TAG, "car wifi connected & ip match, waiting client ....");

                            //refresh UI
                            Message newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_WIFI,1,0);
                            mHandler.sendMessage(newMsg);
                            mCarProxy.setCarReady(true);
                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            mCarProxy.makeWifiReady(null,connectivityManager);
                            //requestWIFINetwork();
                        } else {
                            //refresh UI
                            Message newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_WIFI,0,0);
                            mHandler.sendMessage(newMsg);
                            mCarProxy.setCarReady(false);
                        }
                    } else if (state == NetworkInfo.State.DISCONNECTED) {
                        Message newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_WIFI,0,0);
                        mHandler.sendMessage(newMsg);
                        mCarProxy.setCarReady(false);
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
        keepScreenOff();
    }

    protected void onResume() {
        super.onResume();
        keepScreenOn();
        toggleLight(true);
        AppLog.i(TAG, "on Resume");
    }

    Runnable wifiRunnable = new Runnable() {
        public void run() {
            AppLog.d(TAG, "STEP1: connecting to car wifi .....");
            updateUI(VIEW_ID_CAR_WIFI, false);
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

                //force update UI
                mCarProxy.setCarReady(true);
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                mCarProxy.makeWifiReady(null,connectivityManager);
                //requestWIFINetwork();

                updateUI(VIEW_ID_CAR_WIFI, true);
            } else if (!mCarProxy.matchWifiCarAddr(dhcpAddr)) {
                //connect to spcified ssid
                mCarProxy.setCarReady(false);
                updateUI(VIEW_ID_CAR_WIFI, false);

                WifiConfiguration tempConfig = findSpecifiedSsid(manager, "Rover_");
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
                requestMobileNetwork();
            } catch (Exception e) {
                AppLog.e(TAG, "--->request cell network failed!");
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


    public static final int VIEW_ID_DEBUG_MSG = 10;
    public static final int VIEW_ID_MOBILE_NETWORK = 11;
    public static final int VIEW_ID_CAR_WIFI = 12;
    private void refreshUI(int viewId, boolean wifiOn, int value) {
        switch (viewId) {

            case VIEW_ID_DEBUG_MSG:
                mDebugMsgText.setText(mCarProxy.printDebugMsg());
                break;
            case VIEW_ID_MOBILE_NETWORK:
                mCellgular.setText(mCarProxy.printIpInfo());
                break;
            case VIEW_ID_CAR_WIFI:
                mCarWifi.setText(wifiOn?"connected":"disconnected");
                break;
            default:
                break;
        }
    }

    //for external use
    public void updateUI(int viewId, boolean wifion) {
        Message newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_CLOUD);;

        if (viewId == VIEW_ID_CAR_WIFI) {
            newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_WIFI);
            newMsg.arg1 = wifion?1:0;
        } else if (viewId == VIEW_ID_DEBUG_MSG) {
            newMsg = mHandler.obtainMessage(MESSAGE_PRINT_DEBUG_MSG);
        } else if (viewId == VIEW_ID_MOBILE_NETWORK) {
            newMsg = mHandler.obtainMessage(MESSAGE_REFRESH_CLOUD);
        }
        mHandler.sendMessage(newMsg);
        //refreshUI(viewId, false, 0);
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
            case MESSAGE_REFRESH_CLOUD:
                refreshUI(VIEW_ID_MOBILE_NETWORK, false,0);
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
            mCarProxy.createServerSocket();

        }
    };

    public void requestMobileNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest build = builder.build();
        AppLog.i(TAG, "---> start request cell network");
        //mCarProxy.makeCloudReady(connectivityManager);
        connectivityManager.requestNetwork(build, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                mCarProxy.makeCloudReady(network,cm);
            }
        });
    }

    private void keepScreenOn() {
        PowerManager powerManager = null;
        PowerManager.WakeLock wakeLock = null;
        powerManager = (PowerManager)this.getSystemService(this.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wificar:My Lock");
        mWakeLock.acquire();
    }

    private void keepScreenOff() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }
    CameraManager mCameraManager;
    public void toggleLight(boolean OPEN) {
        try {
            //get all camera id
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String[] ids = mCameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                //get CameraCharacteristics
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

                /*
                 * get camer facing
                 * CameraCharacteristics.LENS_FACING_FRONT
                 * CameraCharacteristics.LENS_FACING_BACK
                 * CameraCharacteristics.LENS_FACING_EXTERNAL
                 */
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                //if (flashAvailable != null && flashAvailable
                //        && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    //
                    mCameraManager.setTorchMode("0", OPEN);
                    break;
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onCarLocationChanged(double lon, double lay) {
        //mainUI.onCarLocationChanged(mLocationCount);
    }
}
