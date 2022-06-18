package com.obana.rover;

import android.app.Activity;
import android.content.Context;

import android.net.Uri;
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


// Referenced classes of package com.wificar.component:
//            AudioComponent, VideoComponent, AudioData, VideoData, 
//            CommandEncoder, TalkData

public class WifiCar
{
    private static final String TAG = "WifiCar_T";
    private static final int CAR_MODE_LOCAL = 0x10;
    private static final int CAR_MODE_CLOUD = 0x11;

    private static final String LOCAL_HOST_ADDR = "192.168.1.100";
    private static final int LOCAL_HOST_PORT = 80;

    private static final String CLOUD_HOST_NAME = "cloud.obana.top";
    private static final int CLOUD_HOST_PORT = 19090;

    private static final int CAR_VERSION_20 = 2;
    private static final int CAR_VERSION_30 = 3;

    private static final int CMD_BUF_SIZE = 1024;
    private static final int MEDIA_BUF_SIZE = (64*1024);

    private int carStateMode;//0-local;1-cloud
    private int carVersion;//version of wificar;2.0;3.0
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    String cameraId;

    private WifiCar instance;
    Timer keepAliveTimer;
    private long lLastCmdTimeStamp;
  
    private long lastMoveCurrentTime;
    private Activity mainUI;
    DataInputStream mediaReceiverInputStream;
    DataOutputStream mediaReceiverOutputStream;
    Socket cmdSocket;
    Socket receiverMediaSocket;
    Thread mediaRevThread;
    boolean bwificarConnected = false;
    boolean bmedia_Connected = false;
    byte[] cmdBuffer = new byte[CMD_BUF_SIZE];
    byte[] mediaBuffer = new byte[MEDIA_BUF_SIZE];

    //private VideoData vData;

    public WifiCar(Activity activity)
    {


     
        carStateMode = CAR_MODE_LOCAL;
        keepAliveTimer = new Timer("keep alive");
        instance = null;
        bwificarConnected = false;
   
        mainUI = null;
    
        cmdSocket = null;
        receiverMediaSocket = null;

        dataOutputStream = null;
        dataInputStream = null;
        receiverMediaSocket = null;

        mediaReceiverOutputStream = null;
        mediaReceiverInputStream = null;
     


     
        instance = this;

        mainUI = activity;
        carVersion = CAR_VERSION_30;//3.0 by default
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

    public boolean setCmdConnect() throws IOException {
        if (bwificarConnected) {
            AppLog.i(TAG, "--->alreay connect, just return");
            return true;
        }
        if (carStateMode == CAR_MODE_LOCAL) {
            createCommandSocket(LOCAL_HOST_ADDR, LOCAL_HOST_PORT);
        } else {    
            ConnectivityManager connectivityManager = (ConnectivityManager)(mainUI.getSystemService(Context.CONNECTIVITY_SERVICE));
            Network network = connectivityManager.getActiveNetwork();
            if (network ==null) return false;

            InetAddress addr = network.getByName(CLOUD_HOST_NAME);
            AppLog.i(TAG, "--->get cloud dns addr:" + addr.getHostAddress());

            //network.bindSocket(cloudSocket);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(addr, CLOUD_HOST_PORT);
            cmdSocket = SocketFactory.getDefault().createSocket();
            cmdSocket.connect(inetSocketAddress, 5000);
        }
        AppLog.i(TAG, "wificar connect successful! addr:" + CLOUD_HOST_NAME);
        if(!cmdSocket.isConnected()){
            AppLog.i(TAG, "--->socket init failed!");
            return false;
        }

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

        Thread rev = new Thread(new Runnable() {

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
                } while(true);

            }
        });
        rev.setName("Command Thread");
        rev.start();

        bwificarConnected = true;
        return bwificarConnected;
    }
    
    private Socket createCommandSocket(String paramString, int paramInt) throws IOException {
      cmdSocket = SocketFactory.getDefault().createSocket();
      InetSocketAddress inetSocketAddress = new InetSocketAddress(paramString, paramInt);
      cmdSocket.connect(inetSocketAddress, 5000);
      //this.bSocketState = true;
      return cmdSocket;
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
                receiverMediaSocket.close();
                if (mediaRevThread!=null) mediaRevThread.stop();
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
          boolean bool = cmdSocket.isConnected();
            return 1;
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
            abyte0 = CommandEncoder.cmdCameraControlReq(i);
            AppLog.d(TAG, (new StringBuilder("cmdCameraControlReq(14):")).append(i).toString());

            dataOutputStream.write(abyte0);
            dataOutputStream.flush();
        } catch(Exception ioexception) {
            AppLog.i(TAG, "can not move camera!");
        }

        return true;
    }

    public void connectMediaReceiver(int i)throws IOException{
        if (carStateMode == CAR_MODE_CLOUD/*cloud mode use combined socket*/) {
            //byte abyte0[] = CommandEncoder.cmdMediaLoginReq(i);//this will be send by proxy, no need to send by client
            //dataOutputStream.write(abyte0);
            //dataOutputStream.flush();
            //return;
        }
        if (bmedia_Connected) {
            AppLog.i(TAG, "--->alreay connect media socket, just return");
            return;
        }
        AppLog.i(TAG, "--->media socket creating .....");
        Socket socket = createMediaReceiverSocket(LOCAL_HOST_ADDR, LOCAL_HOST_PORT);

        if(!socket.isConnected()){
            AppLog.i(TAG, "--->media socket connect failed!");
            throw new IOException();
        }

        mediaReceiverOutputStream = new DataOutputStream(socket.getOutputStream());
        mediaReceiverInputStream = new DataInputStream(socket.getInputStream());

        byte abyte0[] = CommandEncoder.cmdMediaLoginReq(i);
        mediaReceiverOutputStream.write(abyte0);
        mediaReceiverOutputStream.flush();
        
        Thread mediaRevThread = new Thread(new Runnable() {

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
                } while(true);

            }
        });
        mediaRevThread.setName("Media Thread");
        mediaRevThread.start();
        bmedia_Connected = true;
    }

    private Socket createMediaReceiverSocket(String host, int port)throws IOException {
        receiverMediaSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress addr = new InetSocketAddress(host, port);
        receiverMediaSocket.setReceiveBufferSize(MEDIA_BUF_SIZE);
        AppLog.i(TAG, "--->media socket connecting .....");
        receiverMediaSocket.connect(addr, 5000);

        AppLog.i(TAG, "--->media socket connected .....");
        return receiverMediaSocket;
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
}
