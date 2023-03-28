package com.wificar.util;

import com.wificar.WificarActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/* loaded from: classes.dex */
public class NetworkUtility {
    public static String getURLContent(String url) {
        StringBuffer sb = new StringBuffer();
        try {
            WificarActivity.getInstance().getWifiCar().isConnected();
        } catch (Exception e) {
            AppLog.d("network", "error");
            e.printStackTrace();
        }
        if (1 == 0) {
            return "";
        }
        AppLog.d("network", url);
        try {
            URL updateURL = new URL(url);
            URLConnection conn = updateURL.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
            while (true) {
                String s = rd.readLine();
                if (s == null) {
                    break;
                }
                sb.append(s);
            }
        } catch (Exception e){

        }
        return sb.toString();
    }
}
