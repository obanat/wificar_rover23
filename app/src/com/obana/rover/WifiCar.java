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
                ByteArrayOutputStream bytearraybuffer = new ByteArrayOutputStream(0x100000);
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
                        bytearraybuffer.write(abyte1, 0, dataInputStream.read(abyte1, 0, i));
                        bytearraybuffer = CommandEncoder.parseCommand(instance, bytearraybuffer);
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
}
