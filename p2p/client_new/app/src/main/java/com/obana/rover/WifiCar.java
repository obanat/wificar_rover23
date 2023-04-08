package com.obana.rover;
import android.app.Activity;
import android.net.Network;
import com.obana.rover.utils.*;
import org.json.JSONObject;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.SocketFactory;


public class WifiCar
{
    private static final String TAG = "WifiCar";
    public static final int CAR_MODE_LOCAL = 0x10;
    public static final int CAR_MODE_CLOUD = 0x11;
    public static final int CAR_MODE_P2P = 0x12;

    private static final String LOCAL_HOST_ADDR = "192.168.1.100";
    private static final int LOCAL_HOST_PORT = 80;

    private static final int P2P_HOST_PORT = 28000;
    private static final String P2P_HOST_URL = "http://obana.f3322.org:38086/wificar/getClientIp";

    private static final String CLOUD_HOST_NAME = "cloud.obana.top";
    private static final int CLOUD_HOST_PORT = 19090;

    private static final int CAR_VERSION_20 = 2;
    private static final int CAR_VERSION_30 = 3;

    private static final int CMD_BUF_SIZE = 1024;
    private static final int MEDIA_BUF_SIZE = (64*1024);

    private int carStateMode;
    private int carVersion;//version of wificar;2.0;3.0
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    String cameraId;

    private WifiCar instance;
    Timer keepAliveTimer;

    private Activity mainUI;
    DataInputStream mediaReceiverInputStream;
    DataOutputStream mediaReceiverOutputStream;
    Socket cmdSocket;
    Socket mediaSocket;

    boolean bwificarConnected = false;
    boolean bmedia_Connected = false;
    byte[] cmdBuffer = new byte[CMD_BUF_SIZE];
    byte[] mediaBuffer = new byte[MEDIA_BUF_SIZE];

    private String targetHost;
    private int targetPort;

    Thread cmdThread;
    Thread mediaThread;
    boolean cmdThreadBool = false;
    boolean mediaThreadBool = false;

    public WifiCar(Activity activity)
    {
        carStateMode = CAR_MODE_P2P;
        keepAliveTimer = new Timer("keep alive");
        instance = null;
        bwificarConnected = false;
   
        mainUI = null;
    
        cmdSocket = null;

        dataOutputStream = null;
        dataInputStream = null;
        mediaSocket = null;

        mediaReceiverOutputStream = null;
        mediaReceiverInputStream = null;
        instance = this;

        mainUI = activity;
        carVersion = CAR_VERSION_30;//3.0 by default

        HttpServer hs = new HttpServer();
        try{
            hs.execute(38000);
        }catch(Exception e){
            AppLog.e(TAG,"start http server error:"+e.getMessage());
        }

        AppLog.d(TAG, "new WifiCar created successfully!");
    }

    public void reLogin()  throws IOException {
        if (!bwificarConnected) {
            AppLog.i(TAG, "--->cmd socket not connected, just return");
            return;
        }

        if(!cmdSocket.isConnected() || dataOutputStream == null){
            AppLog.i(TAG, "--->cmd socket not connected, just return");
            return;
        }

        try {
            byte abyte0[] = null;
            if (carVersion == CAR_VERSION_20) {
                abyte0 = CommandEncoder.cmdLoginReq(0, 0, 0, 0);
            } else {
                abyte0 = CommandEncoder.cmdLoginReq(1, 2, 3, 4);
            }
            dataOutputStream.write(abyte0);
            dataOutputStream.flush();
        } catch (IOException e) {
            AppLog.e(TAG, "--->cmd socket send login failed, just return");
            return;
        }
    }


    public String realHostIp()  {
        StringBuffer sb = new StringBuffer();
        String hostUrl = P2P_HOST_URL;
        AppLog.d(TAG, hostUrl);

        if (cachedNetwork == null ) return "error";
        try {
            URL updateURL = new URL(hostUrl);
            URLConnection conn = cachedNetwork.openConnection(updateURL);
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

        if(sb.length() > 50) {
            JSONObject jsonObject= null;
            try {
                jsonObject = new JSONObject(sb.toString());
                long time = jsonObject.optLong("time");
                String ipAddr = jsonObject.optString("ipaddr");
                long now = System.currentTimeMillis();
                if (now - time > 0 && now-time < 60*1000/*1 min*/){
                    sb = new StringBuffer();
                    sb.append(ipAddr);
                    WificarMain main = (WificarMain)mainUI;
                    main.sendToastMessage("proxy onsite!");
                } else {
                    AppLog.e(TAG, "get car ip from redis error, time not match");
                }
            }catch (Exception ee){

            }
        }
        this.targetHost = sb.toString();
        return targetHost;
    }

    //use for reConnect, such as mode switch
    public boolean reConnect(Network network) throws IOException {
        bwificarConnected = false;
        releaseSocket();
        return setCmdConnect(network);
    }

    private void releaseSocket() {
        try {
            if (cmdSocket != null) {
                AppLog.i(TAG, "close previous car cmd socket...");
                cmdSocket.close();
            }

            if (mediaSocket != null) {
                AppLog.i(TAG, "close previous car media socket...");
                mediaSocket.close();
            }
        } catch (Exception e){
            AppLog.e(TAG, "clean up socket met error.");
        }

        try {
            cmdThreadBool = false;
            AppLog.i(TAG, "stop cmd & media thread...");
            if (cmdThread != null) {
                cmdThread.interrupt();
                cmdThread = null;
            }
            mediaThreadBool = false;
            if (mediaThread != null) {
                mediaThread.interrupt();
                mediaThread = null;
            }
            dataOutputStream = null;
            dataInputStream = null;

            mediaReceiverOutputStream = null;
            mediaReceiverInputStream = null;

        } catch (Exception e){
            AppLog.e(TAG, "clean up resource car cmd met error3.");
        }
    }

    Network cachedNetwork = null;
    public boolean setCmdConnect(Network network) throws IOException {
        if (bwificarConnected) {
            AppLog.i(TAG, "--->alreay connect, just return");
            return true;
        }

        if (network == null) {
            AppLog.e(TAG, "--->network null, just return");
            return true;
        }
        cachedNetwork = network;

        if (carStateMode == CAR_MODE_P2P) {
            targetHost = realHostIp();
            targetPort = P2P_HOST_PORT;
        } else {
            //local mode
            targetHost = LOCAL_HOST_ADDR;
            targetPort = LOCAL_HOST_PORT;
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(targetHost, targetPort);;
        AppLog.i(TAG, "wificar connecting ... addr:" + inetSocketAddress);
        cmdSocket = SocketFactory.getDefault().createSocket();
        network.bindSocket(cmdSocket);
        cmdSocket.connect(inetSocketAddress, 3000);

        if(!cmdSocket.isConnected()){
            AppLog.i(TAG, "--->socket init failed! addr:" + inetSocketAddress);
            return false;
        }
        AppLog.i(TAG, "wificar connect successful!");

        dataOutputStream = new DataOutputStream(cmdSocket.getOutputStream());
        dataInputStream = new DataInputStream(cmdSocket.getInputStream());
        byte abyte0[] = null;
        if (carVersion == CAR_VERSION_20) {
            abyte0 = CommandEncoder.cmdLoginReq(0, 0, 0, 0);
        } else {
            abyte0 = CommandEncoder.cmdLoginReq(1, 2, 3, 4);
        }
        dataOutputStream.write(abyte0);
        dataOutputStream.flush();

        cmdThreadBool = true;
        cmdThread = new Thread(new Runnable() {

            public void run()//this is main receive loop
            {
                ByteArrayOutputStream bytearraybuffer = new ByteArrayOutputStream(32*1024);
                AppLog.i(TAG, "--->ready to read remote socket:");

                int i;
                do {
                    try {
                        i = dataInputStream.available();
                    } catch(IOException ioexception) {
                        ioexception.printStackTrace();
                        AppLog.i(TAG, "main receive loop ioexception!, just exit thread!");
                        //TODO:reconnect
                        return;
                    }
                    if(i <= 0) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    AppLog.i(TAG, "--->read dataInputStream, len:" + i + " ready to parseCommand");
                    byte abyte1[] = new byte[i];
                    try {
                        dataInputStream.read(cmdBuffer, 0, i);
                        CommandEncoder.parseCommand(instance, cmdBuffer, i);

                    } catch(IOException ioexception) {
                        ioexception.printStackTrace();
                        AppLog.i(TAG, "main parseCommand io exception!, just throw it!");
                        bwificarConnected = false;
                    }
                } while(cmdThreadBool);

            }
        });
        cmdThread.setName("Command Thread");
        cmdThread.start();

        bwificarConnected = true;
        return bwificarConnected;
    }
    

    public String getKey()
    {
        if (isVersion20()) {
            return (new StringBuilder(String.valueOf("AC13"))).append(":").append(cameraId).append("-save-private:").append("AC13").toString();
        } else {
            return "-shanghai-hangzhou";
        }
    }

    public void setCameraId(String id) {
        cameraId = id;
    }

    public void verifyCommand(int a, int b, int c, int d)
    {
        try {
        byte abyte0[] = CommandEncoder.cmdVerifyReq(getKey(), a, b, c, d);
        dataOutputStream.write(abyte0);
        dataOutputStream.flush();
        startKeepAliveTask();
        } catch (IOException e) {
            //do nothing
        }
    }

    private void startKeepAliveTask()
    {
        try {
            AppLog.d(TAG, "startKeepAliveTask");
            keepAliveTimer.schedule(KeepAliveTask, 1000L, 30000L);
        } catch (Exception e) {
        }
    }

    private TimerTask KeepAliveTask = new TimerTask() {

        public void run()
        {
            try
            {
                AppLog.d(TAG, (new StringBuilder("startKeepAliveTask:")).append(System.currentTimeMillis()).toString());
                byte abyte0[] = CommandEncoder.cmdKeepAlive();
                dataOutputStream.write(abyte0);
                dataOutputStream.flush();
                return;
            }
            catch(IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        }
    };

    public boolean enableVideo(boolean on, boolean byUser)
        throws IOException
    {
        //WificarActivity.getInstance().sendMessage(8902);
        if(!bwificarConnected){
            return false;
        }

        if (byUser && bmedia_Connected) {
            try {
                bmedia_Connected = false;
                mediaSocket.close();
                if (mediaThread!=null) mediaThread.interrupt();
            } catch (IOException e) {
                AppLog.e(TAG, "stop thread & close media socket error!");
            }
        }
        byte [] abyte0;
        if (on) {
            abyte0 = CommandEncoder.cmdVideoStartReq();
        } else {
            abyte0 = CommandEncoder.cmdVideoEnd();
        }
        dataOutputStream.write(abyte0);
        dataOutputStream.flush();
        return true;
    }

    public void switchCamera(boolean isSecondCamera) throws IOException {
        byte [] abyte0 = CommandEncoder.cmdCamSwitchReq(isSecondCamera);
        dataOutputStream.write(abyte0);
        dataOutputStream.flush();
    }

    public boolean enableAudio()
        throws IOException
    {
        AppLog.d(TAG, "Enable audio ....");
        boolean flag;
        if(!bmedia_Connected){
            return false;
        } 

        byte abyte0[] = CommandEncoder.cmdAudioStartReq();
        dataOutputStream.write(abyte0);
        dataOutputStream.flush();
        return true;
    }

    public int isSocketConnected() {
        try {
          if (cmdSocket == null)
            return 0; 

          return cmdSocket.isConnected()?1:0;
        } catch (Exception exception) {
          exception.printStackTrace();
        } 
        return 0;
    }

    public boolean move(int i, int j) throws IOException {
        byte abyte0[];
        if (!bwificarConnected) return false;

        try {
            abyte0 = CommandEncoder.cmdDeviceControlReq(i, j);
            AppLog.d(TAG, (new StringBuilder("cmdDeviceControlReq(4):")).append(j).toString());

            dataOutputStream.write(abyte0);
            dataOutputStream.flush();
        } catch(Exception ioexception) {
            AppLog.i(TAG, "can not move wificar!");
            //TODO:reconnect
        }
        
        return true;
    }

    public boolean camMove(int i) throws IOException {
        byte abyte0[];
        if (!bwificarConnected) return false;

        try {
            abyte0 = CommandEncoder.cmdCameraControlReqV2(i);
            dataOutputStream.write(abyte0);
            dataOutputStream.flush();
        } catch(Exception ioexception) {
            AppLog.i(TAG, "can not move camera!");
        }

        return true;
    }

    public void connectMediaReceiver(int i)throws IOException{
        if (bmedia_Connected) {
            AppLog.i(TAG, "--->alreay connect media socket, just return");
            return;
        }
        AppLog.i(TAG, "--->media socket creating .....host:" + targetHost + " port:" + targetPort);

        //use cached host & port
       createMediaReceiverSocket(targetHost, targetPort);

        if(!mediaSocket.isConnected()){
            AppLog.i(TAG, "--->media socket connect failed!");
            throw new IOException();
        }

        mediaReceiverOutputStream = new DataOutputStream(mediaSocket.getOutputStream());
        mediaReceiverInputStream = new DataInputStream(mediaSocket.getInputStream());

        byte abyte0[] = CommandEncoder.cmdMediaLoginReq(i);
        mediaReceiverOutputStream.write(abyte0);
        mediaReceiverOutputStream.flush();

        mediaThreadBool = true;
        mediaThread = new Thread(new Runnable() {

            public void run()//this is media receive loop
            {

                int i;
                do {
                    try {
                        i = mediaReceiverInputStream.available();
                        AppLog.i(TAG, "Receive media socket data, length:" + i);
                    } catch(IOException ioexception) {
                        ioexception.printStackTrace();
                        AppLog.i(TAG, "media receive loop io exception2!, just exit thread!");
                        return;
                    }
                    if(i <= 0 || i > MEDIA_BUF_SIZE) {
                        continue;
                    }

                    try {
                        mediaReceiverInputStream.read(mediaBuffer, 0, i);
                        if (isVersion20()) {
                            CommandEncoder.parseMediaCommand(instance, mediaBuffer, i);
                        } else {
                            CommandEncoder.parseMediaCommandV3(instance, mediaBuffer, i);
                        }
                        //AppLog.i(TAG, "parse media socket data length:" + i);
                        Thread.sleep(5L);
                    } catch(Exception e) {
                        e.printStackTrace();
                        AppLog.i(TAG, "media parseCommand io exception!, just throw it!");
                        //throw new IOException();
                    }
                } while(mediaThreadBool);

            }
        });
        mediaThread.setName("Media Thread");
        mediaThread.start();
        bmedia_Connected = true;
    }

    private void createMediaReceiverSocket(String host, int port)throws IOException {
        mediaSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress addr = new InetSocketAddress(host, port);
        mediaSocket.setReceiveBufferSize(MEDIA_BUF_SIZE);

        if (cachedNetwork != null) cachedNetwork.bindSocket(mediaSocket);

        AppLog.i(TAG, "--->media socket connecting .....");
        mediaSocket.connect(addr, 5000);

        AppLog.i(TAG, "--->media socket connected .....");
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

    public void reInitH264So() {
        //AppDecodeH264.Uninit();
        //AppDecodeH264.Init(1);
    }

    public void refreshH264View(byte[] buf, int len, int type) {
        AppLog.i(TAG, "--->send h264 data to main activity.... len:" + len);

        WificarMain main = (WificarMain)mainUI;
        main.mH264View.decodeOneFrame(buf, len);
    }

    public void onCarLocationChanged(double lon, double lay) {
        WificarMain main = (WificarMain)mainUI;
        main.onCarLocationChanged(lon, lay);
    }

    private void createHttpServer() {
        HttpServer hs = new HttpServer();
    }

    public void setMode(int mode ){
        carStateMode = mode;
    }
    public int getMode( ){
        return carStateMode;
    }
}
