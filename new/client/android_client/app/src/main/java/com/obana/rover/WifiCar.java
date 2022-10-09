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
import java.util.HashMap;
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

    private static final String CLOUD_HOST_ADDR = "obana.f3322.org";
    private static final int CLOUD_HOST_PORT = 19092;
    private static final int HOST_CMD_PORT = 28001;

    private static final int CAR_VERSION_20 = 2;
    private static final int CAR_VERSION_30 = 3;

    private static final int CMD_BUF_SIZE = 1024;
    private static final int MEDIA_BUF_SIZE = (64*1024);

    private static final int STATE_UNKNOW = 0;
    private static final int STATE_CONNECT_CLOUD    = 100;
    private static final int STATE_ADD_UPNP         = 101;
    private static final int STATE_REG_CLOUD        = 102;
    private static final int STATE_CREATE_SERVER    = 103;
    private static final int STATE_CLOUD_REGED      = 104;
    private static final int STATE_PROXY_CONNECTED  = 105;
    private static final int STATE_MEDIA_RECVED     = 106;
    HashMap<Integer, String> stateDebugMsgs = new HashMap<Integer, String>() {
        {   put(STATE_UNKNOW, "STATE_UNKNOW");
            put(STATE_CONNECT_CLOUD, "STATE_CONNECT_CLOUD");
            put(STATE_ADD_UPNP, "STATE_ADD_UPNP");
            put(STATE_REG_CLOUD, "STATE_REG_CLOUD");
            put(STATE_CREATE_SERVER, "STATE_CREATE_SERVER");
            put(STATE_CLOUD_REGED, "STATE_CLOUD_REGED");
            put(STATE_PROXY_CONNECTED, "STATE_PROXY_CONNECTED");
            put(STATE_MEDIA_RECVED, "STATE_MEDIA_RECVED");
        }
    };
    private int mState = STATE_UNKNOW;
    private int carVersion;//version of wificar;2.0;3.0

    DataInputStream cloudInputStream = null;
    DataOutputStream cloudOutputStream = null;

    DataInputStream cmdInputStream = null;
    DataOutputStream cmdOutputStream = null;
    DataInputStream mediaReceiverInputStream = null;
    String cameraId;

    private WifiCar instance;
    Timer keepAliveTimer;
    private long lLastCmdTimeStamp;
  
    private long lastMoveCurrentTime;
    private Activity mainUI;

    Socket cloudSocket = null;
    ServerSocket cmdServerSocket = null;
    ServerSocket mediaServerSocket = null;

    Thread mediaRevThread;
    boolean bInited = false;
    boolean bServerListened = false;
    boolean bUpnpAdded = false;
    boolean bClientReged = false;
    boolean bProxyConnected = false;

    byte[] cloudBuffer = new byte[CMD_BUF_SIZE];
    byte[] cmdBuffer = new byte[CMD_BUF_SIZE];
    byte[] mediaBuffer = new byte[MEDIA_BUF_SIZE];

    //private VideoData vData;

    public WifiCar(Activity activity)
    {

        cameraId = "1100000";//for test,

        mState = STATE_UNKNOW;

        keepAliveTimer = new Timer("keep alive");
        instance = null;
   
        mainUI = null;

        cloudSocket = null;
        cmdServerSocket = null;
        mediaServerSocket = null;

        cloudInputStream = null;
        cloudOutputStream = null;
        cmdOutputStream = null;
        cmdInputStream = null;
        mediaReceiverInputStream = null;

        instance = this;

        mainUI = activity;
        carVersion = CAR_VERSION_20;//3.0 by default
        showStateDebugMsg(mState);
        AppLog.i(TAG, "new WifiCar created successfully!");
    }

    public void reLogin()  throws IOException {

    }

    public boolean initCmdConnection() {
        //setup client cocket to cloud
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) (mainUI.getSystemService(Context.CONNECTIVITY_SERVICE));
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            //step 1:connect to cloud
            InetAddress addr = network.getByName(CLOUD_HOST_ADDR);
            AppLog.i(TAG, "connecting cloud socket:" +  CLOUD_HOST_ADDR + "..... ip:" + addr.getHostAddress());

            InetSocketAddress inetSocketAddress = new InetSocketAddress(addr, CLOUD_HOST_PORT);
            cloudSocket = SocketFactory.getDefault().createSocket();
            cloudSocket.connect(inetSocketAddress, 5000);
            if(!cloudSocket.isConnected()){
                AppLog.e(TAG, "connecting cloud socket failed!");
                bInited = false;
                return false;
            }

            cloudInputStream = new DataInputStream(cloudSocket.getInputStream());
            cloudOutputStream = new DataOutputStream(cloudSocket.getOutputStream());
            new Thread(cloudSocketRecv).start();
            mState = STATE_CONNECT_CLOUD;
            showStateDebugMsg(mState);

            //step 2:get p2p ipv4 addr
            Thread getIpThread = new Thread(runnableGetExternalIP);
            getIpThread.setName("getIpThread");
            getIpThread.start();
        } catch (IOException e) {
            AppLog.e(TAG, "connect to cloud socket failed!");
            bInited = false;
            return false;
        }

        return true;
    }

    public boolean createServerConnection() {
        //create cmd & media server socket
        try {
            bServerListened = false;

            cmdServerSocket = new ServerSocket(HOST_CMD_PORT);
            mediaServerSocket = new ServerSocket(HOST_CMD_PORT + 1);

            cmdOutputStream = null;
            cmdInputStream = null;

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

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        cmdInputStream = new DataInputStream(socket.getInputStream());
                                        cmdOutputStream = new DataOutputStream(socket.getOutputStream());

                                        int len;
                                        while ((len = cmdInputStream.available()) != -1) {
                                            cmdInputStream.read(mediaBuffer);
                                            if (len == 0 || len > CMD_BUF_SIZE) {
                                                continue;
                                            }


                                            //process cmd data
                                            AppLog.i(TAG, "received cmd data, len:" + len);
                                            //CommandEncoder.parseMediaCommand(instance, mediaBuffer, len);
                                        }
                                    } catch (Exception e) {

                                    } finally {
                                        synchronized (this) {
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

                            mState = STATE_PROXY_CONNECTED;
                            showStateDebugMsg(mState);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    AppLog.i(TAG, "media thread running.....");
                                    try {
                                        //socket.setKeepAlive(true);
                                        mediaReceiverInputStream = new DataInputStream(socket.getInputStream());
                                        int len;
                                        while ((len = mediaReceiverInputStream.available()) != -1) {
                                            len = mediaReceiverInputStream.read(mediaBuffer);
                                            if (len == 0 || len > MEDIA_BUF_SIZE) {
                                                continue;
                                            }

                                            if (len > 100) {
                                                mState = STATE_MEDIA_RECVED;
                                                showStateDebugMsg(mState);
                                            }
                                            //process mjpg data
                                            //AppLog.i(TAG, "received media data, len:" + len);
                                            CommandEncoder.parseMediaCommand(instance, mediaBuffer, len);
                                        }
                                    } catch (Exception e) {
                                        AppLog.e(TAG, "media loop error :" + e.getMessage());
                                    } finally {
                                        synchronized (this) {
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
        } catch (IOException e) {
            AppLog.e(TAG, "create server socket failed!");
            bServerListened = false;
            return false;
        }
        bServerListened = true;
        mState = STATE_CREATE_SERVER;
        showStateDebugMsg(mState);
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
        if (mState < STATE_PROXY_CONNECTED || cmdOutputStream == null) return false;

        try {
            abyte0 = CommandEncoder.cmdDeviceControlReq(i, j);
            AppLog.d(TAG, (new StringBuilder("MOVE cmdDeviceControlReq(4):")).append(j).toString());

            cmdOutputStream.write(abyte0);
            cmdOutputStream.flush();
        } catch(IOException ioexception) {
            AppLog.e(TAG, "IOException when move wificar!");
            cmdOutputStream = null;
        }
        
        return true;
    }

    public void refreshView(byte[] jpgData) {
        //AppLog.i(TAG, "--->send jpeg data to main activity.... len:" + jpgData.length);

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
                String externalIp;
                InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices(discoveryTiemout);
                if (IGDs != null) {
                    for (int i = 0; i < IGDs.length; i++) {
                        boolean mapped = false;
                        int portNum = HOST_CMD_PORT;
                        String localHostIP = getLocalIpAddr();

                        InternetGatewayDevice testIGD = IGDs[i];
                        AppLog.i(TAG, "Found device, Name:" + testIGD.getIGDRootDevice().getModelName());
                        externalIp = "";
                        try {
                            externalIp = testIGD.getExternalIPAddress();
                            AppLog.i(TAG, "Found device, IP address: " + externalIp);

                            // now let's open the port

                            AppLog.i(TAG, "Trying to add upnp mapping" + portNum + "...");


                            AppLog.i(TAG, "localHostIP: " + localHostIP);

                            //remove port mapping already set
                            mapped = testIGD.deletePortMapping(null, portNum, "TCP");
                            mapped = testIGD.addPortMapping("RoverCmd", null, portNum, portNum, localHostIP, 0, "TCP");

                            mapped = testIGD.deletePortMapping(null, portNum + 1, "TCP");
                            mapped = testIGD.addPortMapping("RoverMedia", null, portNum + 1, portNum + 1, localHostIP, 0, "TCP");
                        } catch (UPNPResponseException respEx) {
                            AppLog.e(TAG, "UPNP device unhappy " + respEx.getDetailErrorCode() + " " + respEx.getDetailErrorDescription());
                        }
                        AppLog.i(TAG, "AddPortState: " + mapped);
                        if (mapped) {
                            AppLog.i(TAG, "Port " + portNum + " mapped to " + localHostIP);
                            //AppLog.i(TAG, "Current mappings count is " + testIGD.getNatMappingsCount());
                            // checking on the device
                            //ActionResponse resp = testIGD.getSpecificPortMappingEntry(null, portNum, "TCP");
                            //if (resp != null) {
                                AppLog.i(TAG, "Port: " + portNum + " mapping confirmation received from device");
                                bUpnpAdded = true;

                                //step 3:register to cloud
                                startClientReg(cameraId, externalIp, portNum);
                                mState = STATE_REG_CLOUD;
                                showStateDebugMsg(mState);
                            //}
                        }
                    }
                    AppLog.i(TAG, "Add upnp mapping Done!");
                } else {
                    AppLog.e(TAG, "Unable to find IGD on your network, just exit");
                }
            } catch (IOException ex) {
                AppLog.e(TAG, "IOException occured during discovery or ports mapping " + ex.getMessage());
            }

            if (bUpnpAdded) {
                //step 4: create cmd & media socket
                mState = STATE_ADD_UPNP;
                showStateDebugMsg(mState);
                createServerConnection();
            }
        }
    };

    Runnable cloudSocketRecv = new Runnable() {
        /*
         * start this thread for get router external ip addr
         * it depends on the route has a external ip & support upnp
         * then client will send ipaddr to signal server
         * signal server will check whether this ipaddr is same with client's external ipaddr
         * this ipaddr will send to proxy if same
         * proxy use this ipaddr to establish p2p connection
         * */
        @Override
        public void run()//this is main receive loop
        {
            AppLog.i(TAG, "--->ready to recv cloud data");
            int i;
            do {
                try {
                    i = cloudInputStream.available();
                } catch(IOException ioexception) {
                    AppLog.e(TAG, "cloud receive loop ioexception!, just exit thread!");
                    //TODO:reconnect
                    break;
                }


                try {
                    cloudInputStream.read(cloudBuffer, 0, i);

                    if(i <= 0) {
                        //AppLog.e(TAG, "cloud receive loop read error! i:" + i);
                        continue;
                    }
                    CommandEncoder.parseCloudCommand(instance, cloudBuffer, i);

                } catch(IOException ioexception) {
                    AppLog.e(TAG, "main parseCommand io exception!, just throw it!");
                }
            } while(true);
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

    void startClientReg(String devId, String ipv4Addr, int port) {
        try {
            AppLog.i(TAG, "startClientReg,devId:" + devId + " ip:" + ipv4Addr + " port:" + port);
            if (devId != null && devId.length() > 0
                && ipv4Addr != null && !ipv4Addr.equals("0.0.0.0")
                && port > 0 && port < 65000) {
                byte abyte0[] = CommandEncoder.cmdClientRegReq(devId, ip2int(ipv4Addr), port);
                cloudOutputStream.write(abyte0);
                cloudOutputStream.flush();
            }
        } catch (Exception e) {

        }
    }
    public void processClientRegResult(int result) {
        if (result == 100) {
            mState = STATE_CLOUD_REGED;
            showStateDebugMsg(mState);
            AppLog.i(TAG, "client register to cloud successfully!");
        } else {
            AppLog.e(TAG, "client register to cloud failed!");
        }
    }

    long ip2int(String ipv4) {
        String[] ipArr = ipv4.split("\\.");
        if (ipArr!= null && ipArr.length == 4) {
            int i0 = Integer.parseInt(ipArr[0]) & 0xFF;
            int i1 = Integer.parseInt(ipArr[1]) & 0xFF;
            int i2 = Integer.parseInt(ipArr[2]) & 0xFF;
            int i3 = Integer.parseInt(ipArr[3]) & 0xFF;
            int m = i0 << 24;
            m += i1<<16;
            m += i2<<8;
            m += i3;
            return m;
        }
        return  0;
    }

    void showStateDebugMsg(int state) {
        String debugMsg = stateDebugMsgs.get(state);
        ((WificarMain)mainUI).sendDebugMessage(state + ":" + debugMsg);
    }

    public boolean enableCamera(boolean on) {
        //enable/disable  media socket for total close media upload stream
        AppLog.i(TAG, on ? "enable camera ...." : "disable camera ....");
        
        byte abyte0[];
        if (mState < STATE_PROXY_CONNECTED || cmdOutputStream == null) return false;

        try {
            abyte0 = CommandEncoder.cmdCameraEnable(on);
            AppLog.i(TAG, "create cmdCameraEnable(34): and send");

            cmdOutputStream.write(abyte0);
            cmdOutputStream.flush();
        } catch(IOException ioexception) {
            AppLog.e(TAG, "IOException when enable/disable camera!");
            //cmdOutputStream = null;
            return false;
        }
        
        return true;
    }
}
