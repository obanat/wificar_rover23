package com.obana.rover;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.NetworkRequest.*;
import android.net.NetworkCapabilities;
import android.net.Network;

import com.obana.rover.utils.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.SocketFactory;
//import org.apache.http.util.ByteArrayBuffer;

// Referenced classes of package com.wificar.component:
//            AudioComponent, VideoComponent, AudioData, VideoData, 
//            CommandEncoder, TalkData

public class WifiCar
{
    private static final String TAG = "WifiCar_T";
    Timer batteryTimer;
    private int battery_value;
    private int battery_valueStop;
    String cameraId;
    private int carStateMode;
    private boolean changeFlag;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    String deviceId;
    private long endRecordTime;
    //private VideoComponent flim;
    Handler handlerGUI;
    int iTimeout;
    private byte initialAudioData[];
    private int initialParaIndex;
    private int initialParaSample;
    private WifiCar instance;
    Timer keepAliveTimer;
    private long lLastCmdTimeStamp;
    private int lastAudioDataTime;
    private long lastMoveCurrentTime;
    private Activity mainUI;
    DataInputStream mediaReceiverInputStream;
    DataOutputStream mediaReceiverOutputStream;
    DataInputStream mediaSenderInputStream;
    DataOutputStream mediaSenderOutputStream;
    private boolean move;
    private int moveFlag;
    private int moveFlagCount;
    private int moveFlagMaxCount;

    private int number1Count;
    private String photofilename;
    Timer playTimer;
    Socket receiverMediaSocket;
    long recordTimeLength;
    Timer recordTimer;
    Socket senderMediaSocket;
    //CameraSurfaceView sfhGUI;
    private Timer showP;
    Socket cmdSocket;
    private long soundDelayTime;
    private long startRecordTime;
    long startRecordTimeStamp;
    String targetCameraVer;
    String targetDevID;
    String targetHost;
    String targetId;
    String targetPassword;
    int targetPort;
    boolean bConnected = false;
    boolean bmedia_Connected = false;
    //private VideoData vData;

    public WifiCar(Activity activity)
    {
        this(activity, -1, -1, -1, -1);
    }

    public WifiCar(Activity activity, int i, int j, int k, int l)
    {


        number1Count = 0;
        showP = new Timer(true);
        //battery = 0.0F;
        //batteryStop = 0.0F;
        battery_value = 0;
        battery_valueStop = 0;
        //batteryCount = 0;
        //batteryCountStop = 0;
        //bSocketState = false;
        batteryTimer = new Timer("battery timer");
        keepAliveTimer = new Timer("keep alive");
        instance = null;
        //audio = null;
        //flim = null;
        changeFlag = true;
        lastAudioDataTime = 0;
        //audioIndex = 0;
        moveFlag = 0;
        moveFlagCount = 0;
        moveFlagMaxCount = 50;
        //audioFlag = 1;
        move = false;
        //vData = null;
        //aData = null;
        lastMoveCurrentTime = System.currentTimeMillis();
        soundDelayTime = 1500L;
        targetHost = "192.168.1.100";
        targetPort = 80;
        targetId = "AC13";
        targetPassword = "AC13";
        targetCameraVer = "";
        targetDevID = "";
        bConnected = false;
        iTimeout = 5000;
        handlerGUI = null;
        //sfhGUI = null;
        //bBufCamera = null;
        //bIr = false;
        mainUI = null;
        lLastCmdTimeStamp = 0L;
        cmdSocket = null;
        dataOutputStream = null;
        dataInputStream = null;
        receiverMediaSocket = null;
        senderMediaSocket = null;
        mediaReceiverOutputStream = null;
        mediaReceiverInputStream = null;
        mediaSenderOutputStream = null;
        mediaSenderInputStream = null;

        cameraId = "123";
        deviceId = "";
        carStateMode = 0;
        recordTimer = null;
        playTimer = null;
        startRecordTimeStamp = 0L;
        recordTimeLength = 0L;
        instance = this;
        //audio = new AudioComponent(this);
        //flim = new VideoComponent(this);
        mainUI = activity;
       
     
        AppLog.d(TAG, "new WifiCar created successfully!");
    }

    public boolean setConnect() throws IOException
    {
        if (bConnected) {
            AppLog.i(TAG, "--->alreay connect, just return");
            return true;
        }
        createCommandSocket(targetHost, targetPort);
        AppLog.i(TAG, (new StringBuilder("--->Socket:")).append(cmdSocket).toString());
        if(!cmdSocket.isConnected()){
            AppLog.i(TAG, "--->socket init failed!");
            throw new IOException();
        }

        dataOutputStream = new DataOutputStream(cmdSocket.getOutputStream());
        dataInputStream = new DataInputStream(cmdSocket.getInputStream());
        byte abyte0[] = CommandEncoder.cmdLoginReq(0, 0, 0, 0);
        dataOutputStream.write(abyte0);
        dataOutputStream.flush();
        Thread rev = new Thread(new Runnable() {

            public void run()//this is main receive loop
            {
                ByteArrayOutputStream bytearraybuffer = new ByteArrayOutputStream(1024);
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
                        bytearraybuffer.reset();
                        bytearraybuffer.write(abyte1, 0, dataInputStream.read(abyte1, 0, i));
                        CommandEncoder.parseCommand(instance, bytearraybuffer);
                    } catch(IOException ioexception) {
                        ioexception.printStackTrace();
                        AppLog.i(TAG, "main parseCommand io exception!, just throw it!");
                        //throw new IOException();
                    }
                } while(true);

            }
        });
        rev.setName("Command Thread");
        rev.start();

        bConnected = true;
        return bConnected;
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
        return (new StringBuilder(String.valueOf(targetId))).append(":").append(cameraId).append("-save-private:").append(targetPassword).toString();
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

    public void startKeepAliveTask()
    {
        AppLog.d(TAG, "startKeepAliveTask");
        keepAliveTimer.schedule(KeepAliveTask, 1000L, 30000L);
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

    public boolean enableVideo()
        throws IOException
    {
        //WificarActivity.getInstance().sendMessage(8902);
        byte abyte0[] = CommandEncoder.cmdVideoStartReq();
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

        abyte0 = CommandEncoder.cmdDeviceControlReq(4, j);
        AppLog.d(TAG, (new StringBuilder("cmdDeviceControlReq(4):")).append(j).toString());

        dataOutputStream.write(abyte0);
        dataOutputStream.flush();
        return true;
    }

    static boolean bcloud_Connected = false;
    Socket cloudSocket = null;
    DataOutputStream cloudDataOutputStream;
    DataInputStream cloudDataInputStream;

    public boolean requestMobileSocket () throws IOException {
        ConnectivityManager connectivityManager = (ConnectivityManager)(mainUI.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        NetworkRequest build = builder.build();
        AppLog.d(TAG, "--->Cloud request Mobile Network");
        connectivityManager.requestNetwork(build, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                 AppLog.i(TAG, "--->cellular ready, creating socket....");
                try {
                    setCloudConnect(network);
                } catch (Exception e) {
                    return;
                }
            }
        });
        return true;
    }
    public boolean setCloudConnect(Network network) throws IOException
    {
        if (bcloud_Connected) {
            AppLog.i(TAG, "--->alreay connect cloud socket, just return");
            return true;
        }

        cloudSocket = SocketFactory.getDefault().createSocket();
        cloudSocket.setSendBufferSize(1024 * 1024);//important, it make sure jpg data
        InetSocketAddress inetSocketAddress = new InetSocketAddress("123.113.244.38", 19090);
        AppLog.i(TAG, "--->cloud socket connecting .....");
        network.bindSocket(cloudSocket);
        cloudSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, (new StringBuilder("--->Cloud Socket connected:")).append(cloudSocket).toString());
        if(!cloudSocket.isConnected()){
            AppLog.i(TAG, "--->cloud socket init failed!");
            throw new IOException();
        }

        cloudDataOutputStream = new DataOutputStream(cloudSocket.getOutputStream());
        cloudDataInputStream = new DataInputStream(cloudSocket.getInputStream());
        byte abyte0[] = CommandEncoder.cmdLoginReq(0, 0, 0, 0);
        cloudDataOutputStream.write(abyte0);
        cloudDataOutputStream.flush();
        Thread cloudRev = new Thread(new Runnable() {

            public void run()//this is main receive loop
            {
                ByteArrayOutputStream bytearraybuffer = new ByteArrayOutputStream(1024);
                AppLog.i(TAG, "--->ready to read cloud socket:");

                int i;
                do {
                    try {
                        i = cloudDataInputStream.available();
                    } catch(IOException ioexception) {
                        ioexception.printStackTrace();
                        AppLog.i(TAG, "main receive loop io exception2!, just exit thread!");
                        //TODO:reconnect
                        return;
                    }
                    if(i <= 0) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    AppLog.i(TAG, "--->read cloud InputStream, len:" + i + " ready to parseCommand");
                    byte abyte1[] = new byte[i];
                    try {
                        bytearraybuffer.reset();
                        bytearraybuffer.write(abyte1, 0, cloudDataInputStream.read(abyte1, 0, i));
                        //CommandEncoder.parseCommand(instance, bytearraybuffer);
                        AppLog.i(TAG, "receive cloud socket data:" + new String(abyte1));
                    } catch(IOException ioexception) {
                        ioexception.printStackTrace();
                        AppLog.i(TAG, "main parseCommand io exception!, just throw it!");
                        //throw new IOException();
                    }
                } while(true);

            }
        });
        cloudRev.setName("Cloud Command Thread");
        cloudRev.start();

        bcloud_Connected = true;
        return bcloud_Connected;
    }
     public int isCloudSocketConnected() {
        try {
          if (cloudSocket == null)
            return 0; 
          boolean bool = cloudSocket.isConnected();
            return 1;
        } catch (Exception exception) {
          exception.printStackTrace();
        } 
        return 0;
    }

    public void connectMediaReceiver(int i)throws IOException{

        if (bmedia_Connected) {
            AppLog.i(TAG, "--->alreay connect media socket, just return");
            return;
        }
        AppLog.i(TAG, "--->media socket creating .....");
        Socket socket = createMediaReceiverSocket(targetHost, targetPort);

        if(!socket.isConnected()){
            AppLog.i(TAG, "--->media socket connect failed!");
            throw new IOException();
        }

        mediaReceiverOutputStream = new DataOutputStream(socket.getOutputStream());
        mediaReceiverInputStream = new DataInputStream(socket.getInputStream());

        byte abyte0[] = CommandEncoder.cmdMediaLoginReq(i);
        mediaReceiverOutputStream.write(abyte0);
        mediaReceiverOutputStream.flush();
        
        Thread mediaRev = new Thread(new Runnable() {

            public void run()//this is media receive loop
            {
                ByteArrayOutputStream bytearraybuffer = new ByteArrayOutputStream(16*1024);
                AppLog.i(TAG, "--->ready to read media socket:");

                int i;
                do {
                    try {
                        i = mediaReceiverInputStream.available();
                    } catch(IOException ioexception) {
                        ioexception.printStackTrace();
                        AppLog.i(TAG, "media receive loop io exception2!, just exit thread!");
                        //TODO:reconnect
                        return;
                    }
                    if(i <= 0) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    AppLog.i(TAG, "--->read media InputStream, len:" + i + " ready to parseCommand");
                    byte buf[] = new byte[i];
                    try {
                        //bytearraybuffer.reset();
                        //bytearraybuffer.write(abyte1, 0, );
                        mediaReceiverInputStream.read(buf, 0, i);
                        CommandEncoder.parseMediaCommand(instance, buf, i);
                        AppLog.i(TAG, "parse media socket data length:" + i);
                        Thread.sleep(5L);
                    } catch(Exception e) {
                        e.printStackTrace();
                        AppLog.i(TAG, "media parseCommand io exception!, just throw it!");
                        //throw new IOException();
                    }
                } while(true);

            }
        });
        mediaRev.setName("Media Thread");
        mediaRev.start();
        bmedia_Connected = true;
    }

    private Socket createMediaReceiverSocket(String host, int port)throws IOException {
        receiverMediaSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress addr = new InetSocketAddress(host, port);
        AppLog.i(TAG, "--->media socket connecting .....");
        receiverMediaSocket.connect(addr, 5000);

        AppLog.i(TAG, "--->media socket connected .....");
        return receiverMediaSocket;
    }

    public void onVideoReceived(byte[] jpgData) {
        AppLog.i(TAG, "--->media socket sending .... len:" + jpgData.length);
        try {
            cloudDataOutputStream.write(jpgData);
            cloudDataOutputStream.flush();
            AppLog.i(TAG, "--->media socket sended!");
        } catch(IOException ioexception) {
            ioexception.printStackTrace();
            AppLog.i(TAG, "media socket sending exception!");
        }
    }
}
