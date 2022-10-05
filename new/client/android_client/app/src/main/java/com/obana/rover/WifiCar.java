package com.obana.rover;

import android.app.Activity;
import android.content.Context;

import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.NetworkRequest.*;
import android.net.NetworkCapabilities;
import android.net.Network;

import com.obana.rover.utils.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.SocketFactory;
import java.net.Inet6Address;
import java.net.ServerSocket;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;


// Referenced classes of package com.wificar.component:
//            AudioComponent, VideoComponent, AudioData, VideoData, 
//            CommandEncoder, TalkData

public class WifiCar
{
    private static final String TAG = "WifiCar_T";
    private static final int CAR_MODE_LOCAL = 0x10;
    private static final int CAR_MODE_CLOUD = 0x11;

    private static final int HOST_CMD_PORT = 28001;

    private static final int CAR_VERSION_20 = 2;
    private static final int CAR_VERSION_30 = 3;

    private static final int CMD_BUF_SIZE = 1024;
    private static final int MEDIA_BUF_SIZE = (64*1024);

    private int carStateMode;//0-local;1-cloud
    private int carVersion;//version of wificar;2.0;3.0
    DataInputStream dataInputStream;
    OutputStream cmdOutputStream;
    InputStream mediaReceiverInputStream;
    String cameraId;

    private WifiCar instance;
    Timer keepAliveTimer;
    private long lLastCmdTimeStamp;
  
    private long lastMoveCurrentTime;
    private Activity mainUI;


    ServerSocket cmdServerSocket = null;
    ServerSocket mediaServerSocket = null;

    Thread mediaRevThread;
    boolean bCmdConnected = false;
    boolean bMediaConnected = false;
    boolean bUpnpAdded = false;

    byte[] cmdBuffer = new byte[CMD_BUF_SIZE];
    byte[] mediaBuffer = new byte[MEDIA_BUF_SIZE];

    //private VideoData vData;

    public WifiCar(Activity activity)
    {


     
        carStateMode = CAR_MODE_LOCAL;
        keepAliveTimer = new Timer("keep alive");
        instance = null;
        bCmdConnected = false;
   
        mainUI = null;

        cmdServerSocket = null;
        mediaServerSocket = null;

        cmdOutputStream = null;
        dataInputStream = null;
        mediaReceiverInputStream = null;
     
        instance = this;

        mainUI = activity;
        carVersion = CAR_VERSION_20;//3.0 by default
        AppLog.i(TAG, "new WifiCar created successfully!");
    }

    public void reLogin()  throws IOException {

    }

    public boolean initCmdConnection() throws IOException {
        ConnectivityManager connectivityManager = (ConnectivityManager)(mainUI.getSystemService(Context.CONNECTIVITY_SERVICE));
        Network network = connectivityManager.getActiveNetwork();
        if (network ==null) return false;

        cmdServerSocket = new ServerSocket(HOST_CMD_PORT);
        mediaServerSocket = new ServerSocket(HOST_CMD_PORT + 1);

        cmdOutputStream = null;
        dataInputStream = null;

        bCmdConnected = false;
        Thread cmdThread = new Thread(new Runnable() {
            public void run()//this is main receive loop
            {
                try {
                    while (true) {
                        //just waiting for porxy cmd socket connect
                        AppLog.i(TAG, "waiting for client cmd connection ....");
                        final Socket socket = cmdServerSocket.accept();
                        //print proxy cmd socket addr&port
                        final String address = socket.getRemoteSocketAddress().toString();
                        AppLog.i(TAG, "one client cmd connected, address:" + address);

                        bCmdConnected = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InputStream inputStream = socket.getInputStream();

                                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());

                                    int len;
                                    while ((len = inputStream.read(cmdBuffer)) != -1){

                                        socket.shutdownInput();
                                        //do something
                                        //sendGps();
                                    }
                                }catch (Exception e){

                                }finally {
                                    synchronized (this){
                                        //
                                    }
                                }
                            }
                        }).start();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        cmdThread.setName("cmdThread");
        cmdThread.start();

        bMediaConnected = false;
        Thread mediaThread = new Thread(new Runnable() {
            public void run()//this is main receive loop
            {
                try {
                    while (true) {
                        //just waiting for porxy cmd socket connect
                        AppLog.i(TAG, "waiting for client media connection ....");
                        final Socket socket = mediaServerSocket.accept();
                        //print proxy media socket addr&port
                        final String address = socket.getRemoteSocketAddress().toString();
                        AppLog.i(TAG, "one client media connected, address:" + address);

                        bMediaConnected = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mediaReceiverInputStream = socket.getInputStream();

                                    InputStreamReader isr = new InputStreamReader(mediaReceiverInputStream);

                                    int len;
                                    while ((len = mediaReceiverInputStream.read(mediaBuffer)) != -1){
                                        socket.shutdownInput();

                                        //process mjpg data
                                        CommandEncoder.parseMediaCommand(instance, mediaBuffer, len);
                                    }
                                }catch (Exception e){

                                }finally {
                                    synchronized (this){
                                        //
                                    }
                                }
                            }
                        }).start();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mediaThread.setName("mediaThread");
        mediaThread.start();


        Thread getIpThread = new Thread(runnableGetExternalIP);
        getIpThread.setName("getIpThread");
        getIpThread.start();

        return true;
    }



    public void switchCamera(boolean isSecondCamera)  {

    }

    public boolean enableAudio() throws IOException
    {

        return true;
    }

    public boolean move(int i, int j){
        byte abyte0[];
        if (!bCmdConnected || cmdOutputStream == null) return false;

        try {
            abyte0 = CommandEncoder.cmdDeviceControlReq(i, j);
            AppLog.d(TAG, (new StringBuilder("MOVE cmdDeviceControlReq(4):")).append(j).toString());

            cmdOutputStream.write(abyte0);
            cmdOutputStream.flush();
        } catch(IOException ioexception) {
            AppLog.e(TAG, "IOException when move wificar!");
            bCmdConnected = false;
            cmdOutputStream = null;
        }
        
        return true;
    }

    public void refreshView(byte[] jpgData) {
        AppLog.i(TAG, "--->send jpeg data to main activity.... len:" + jpgData.length);

        WificarMain main = (WificarMain)mainUI;
        main.mJpegView.setCameraBytes(jpgData);
    }

    public boolean isVersion20() {
        return carVersion==CAR_VERSION_20;
    }
    
    public void setVersion(int version) {
        carVersion = (version == 3)?CAR_VERSION_30:CAR_VERSION_20;
    }

    public boolean isSocketInited() {
        return cmdServerSocket != null && mediaServerSocket != null;
    }

    Runnable runnableGetExternalIP = new Runnable() {
        /*
        * start this thread for get router external ip addr
        * it depends on the route has a external ip & support upnp
        * then client will send ipaddr to signal server
        * signal server will check whether this ipaddr is same with client's external ipaddr
        * this ipaddr will send to proxy if same
        * proxy use this ipaddr to establish p2p connection
        * */
        @Override
        public void run() {
            int discoveryTiemout = 5000; // 5 secs
            bUpnpAdded = false;
            try {
                AppLog.i(TAG, "Therad start getExternal IP... Looking for Internet Gateway Device...");
                InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices(discoveryTiemout);
                if (IGDs != null) {
                    for (int i = 0; i < IGDs.length; i++) {
                        InternetGatewayDevice testIGD = IGDs[i];
                        AppLog.i(TAG,"Found device, Name:" + testIGD.getIGDRootDevice().getModelName());
                        AppLog.i(TAG, "Found device, IP address: " + testIGD.getExternalIPAddress());

                        // now let's open the port
                        int portNum = HOST_CMD_PORT;
                        AppLog.i(TAG, "Trying to add upnp mapping" + portNum + "...");

                        String localHostIP = getLocalIpAddr();
                        AppLog.i(TAG, "localHostIP: " + localHostIP);
                        boolean mapped = testIGD.addPortMapping("RoverCmd", null, portNum, portNum, localHostIP, 0, "TCP");
                        mapped = testIGD.addPortMapping("RoverMedia", null, portNum+1, portNum+1, localHostIP, 0, "TCP");
                        AppLog.i(TAG, "AddPortState: " + mapped);
                        if (mapped) {
                            AppLog.i(TAG, "Port " + portNum + " mapped to " + localHostIP);
                            AppLog.i(TAG, "Current mappings count is " + testIGD.getNatMappingsCount());
                            // checking on the device
                            ActionResponse resp = testIGD.getSpecificPortMappingEntry(null, portNum, "TCP");
                            if (resp != null) {
                                AppLog.i(TAG, "Port: " + portNum + " mapping confirmation received from device");
                                bUpnpAdded = true;
                            }
                        }
                    }
                    AppLog.i(TAG, "Add upnp mapping Done!");
                } else {
                    AppLog.e(TAG, "Unable to find IGD on your network, just exit");
                }
            } catch (IOException ex) {
                AppLog.e(TAG, "IOException occured during discovery or ports mapping " + ex.getMessage());
            } catch (UPNPResponseException respEx) {
                AppLog.e(TAG, "UPNP device unhappy " + respEx.getDetailErrorCode() + " " + respEx.getDetailErrorDescription());
            }

        }
    };

    String getLocalIpAddr() {
        WifiManager manager = (WifiManager)mainUI.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().toString();
        int ipInt = wifiInfo.getIpAddress();
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }
}
