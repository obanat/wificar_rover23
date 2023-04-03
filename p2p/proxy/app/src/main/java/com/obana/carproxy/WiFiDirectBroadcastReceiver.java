package com.obana.carproxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.obana.carproxy.utils.AppLog;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "P2P_Test WiFiDirectBroadcastReceiver";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private CarProxy mCarProxy;
    private WifiP2pManager.PeerListListener mListener;
    private WifiP2pManager.ConnectionInfoListener mConnectionListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       CarProxy proxy, WifiP2pManager.PeerListListener listener,
                                       WifiP2pManager.ConnectionInfoListener connectionListener) {
        mManager = manager;
        mChannel = channel;
        mCarProxy = proxy;
        mListener = listener;
        mConnectionListener = connectionListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AppLog.v(TAG,"action --- > " + action);
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

            } else {
                mCarProxy.setWifiDirectState(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //5.获取搜索结果，并返回
            AppLog.v(TAG,"5.获取搜索结果");
            if (mManager != null) {
                mManager.requestPeers(mChannel, mListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                AppLog.v(TAG,"6.点击，连接设备 连接成功");
                mManager.requestConnectionInfo(mChannel, mConnectionListener);
            }else {
                mCarProxy.setWifiDirectState(false);
                AppLog.v(TAG,"6.点击，连接设备 连接失败");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {


        }
    }
}
