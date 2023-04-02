package com.obana.carproxy;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.obana.carproxy.utils.AppLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HTTPS";
    private static final String KEY_IPADDR = "ipaddr";
    private static final String KEY_MAC = "mac";

    private static String ipAddress = "";
    private static Context appContext;
    //开启监听
    public void execute(int port, Context ctx) throws Exception{

        appContext = ctx;
    }

    public HttpServer(int port, Context ctx) {
        super(port);
        appContext = ctx;
    }
    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String answer = "Success!";


        if (method.equals(Method.GET)) {

            byte[] buf = new byte[64 * 1024];
            try {
                InputStream inStream = appContext.getResources().getAssets().open("index.html");

                inStream.read(buf);
            } catch (IOException e) {

            }
            return newFixedLengthResponse(new String(buf));
        } else if(method.equals(Method.POST)) {
            if (uri.contains("postvalue")) {
                try {
                    InputStream input = session.getInputStream();
                    byte[] buf = new byte[1024];
                    input.read(buf);
                    String content = new String(buf);
                    String values[] = content.split("&");
                    for (String oneValue : values) {
                        if (oneValue.contains(KEY_IPADDR)) {

                        } else if (oneValue.contains(KEY_MAC)) {
                            String  mac = oneValue.substring(oneValue.indexOf("=") + 1);
                            if (mac.length() > 8) {
                                SharedPreferences sharedPreferences = appContext.getSharedPreferences("wificar_para", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_MAC,mac);
                                editor.apply();
                                AppLog.i(TAG, "write new mac to sp:" + mac);
                            }
                        }
                    }
                } catch (IOException e){

                }
                answer = "modify para successfully!";
            }
        }
        return newFixedLengthResponse(answer);

    }


}