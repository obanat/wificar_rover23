package com.obana.carproxy;

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

import com.obana.carproxy.utils.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.SocketFactory;
//import com.obana.rover.WificarMain;
import java.net.Inet6Address;
import java.net.ServerSocket;

//import org.apache.http.util.ByteArrayBuffer;

// Referenced classes of package com.wificar.component:
//            AudioComponent, VideoComponent, AudioData, VideoData, 
//            CommandEncoder, TalkData

public class CarProxy
{
    private static final String TAG = "WifiCar_T";
    private static final boolean DBG = false;
    Socket carCmdSocket;
    Socket cloudCmdSocket;

    Socket carMediaSocket;
    Socket cloudMediaSocket;

    DataInputStream cmdInputStreamUplink;//belong to wificarCmdSocket
    DataOutputStream cmdOutputStreamUplink;//belong to cloudCmdSocket

    DataInputStream cmdInputStreamDownlink;//belong to cloudCmdSocket
    DataOutputStream cmdOutputStreamDownlink;//belong to wificarCmdSocket

    DataInputStream mediaInputStreamUplink;//belong to wificarMediaSocket
    DataOutputStream mediaOutputStreamUplink;//belong to cloudMediaSocket

    private CarProxy instance;


    private Main mainUI;


    public boolean b_connected_car_cmd = false;
    public boolean b_connected_cloud_cmd = false;
    public boolean b_connected_car_media = false;
    public boolean b_connected_cloud_media = false;

    //this sock only for uplink
    private static final String CLOUD_HOST_NAME = "cloud.obana.top";
    private static final int CLOUD_PORT = 19092;

    //this sock for uplink & downlink command
    private static final String CAR_HOST_ADDR = "192.168.1.100";
    private static final int CAR_PORT = 80;

    //this sock for media uplink
    private static final String CAR_MEDIA_HOST_ADDR = "192.168.1.100";
    private static final int CAR_MEDIA_PORT = 80;

    private static final int CMD_BUF_LEN = 1024;
    private static final int MEDIA_BUF_LEN = (64*1024);
    private byte[] cmdUplinkBuffer;
    private byte[] cmdDownlinkBuffer;
    private byte[] mediaUplinkBuffer;
    //private int cmdUplinkBufferLen = 0;
    private byte[] bufferedDownlinkCmd = null;

    Thread thread_cmd_downlink = null;
    Thread thread_cmd_uplink = null;
    Thread thread_media_uplink = null;

    public CarProxy(Activity activity)
    {
        instance = this;
        mainUI = (Main)activity;

        cmdUplinkBuffer = new byte[CMD_BUF_LEN];
        cmdDownlinkBuffer = new byte[CMD_BUF_LEN];
        mediaUplinkBuffer = new byte[MEDIA_BUF_LEN];
        AppLog.d(TAG, "new CarProxy created successfully!");
    }

    Runnable runnable_cmd_uplink = new Runnable() {
        //upload cmd from wificar to cloud
        public void run()
        {
            int i;
            AppLog.i(TAG, "cmd uplink Thread ---> start running");
            do {
                try {
                    i = cmdInputStreamUplink.available();
                    if(i <= 0 || i >= CMD_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    if (DBG) AppLog.i(TAG, "cmd uplink Thread ---> read data len:" + i);
                    cmdInputStreamUplink.read(cmdUplinkBuffer, 0, i);
                } catch(IOException ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "cmd uplink--->read data from car ioexception!, just exit thread!");
                    break;
                }

                try {
                    //if video enable resp( op:5 ) , need to create media socket
                    int it = parseVideoStartResp(cmdUplinkBuffer);

                    if (it > 0) {
                        mainUI.ConnectToCarMedia(it);
                    } else {
                        byte tmp[] = arrayCopy(cmdUplinkBuffer, 0, i);
                        cmdOutputStreamUplink.write(tmp);
                        cmdOutputStreamUplink.flush();
                    }
                } catch(IOException ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "cmd uplink--->write data to cloud ioexception!, just exit thread!");
                    break;
                }
            } while(true);
        }
    };
    
    Runnable runnable_cmd_downlink = new Runnable() {
        //upload cmd from wificar to cloud
        public void run()//this is main receive loop
        {
            int i;
            int count = 0;
            byte tmp[] = null;
            do {
                try {
                    i = cmdInputStreamDownlink.available();//read from cloud
                    
                    if(i <= 0 || i >= CMD_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    if (DBG) AppLog.i(TAG, "cmd downlink Thread ---> read data len:" + i);
                    cmdInputStreamDownlink.read(cmdDownlinkBuffer, 0, i);
                } catch(IOException ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "cmd downlink--->read data from cloud ioexception!, just exit thread!");
                    break;
                }

                try {
                    tmp = arrayCopy(cmdDownlinkBuffer, 0, i);
                    cmdOutputStreamDownlink.write(tmp);//send to car
                    cmdOutputStreamDownlink.flush();
                    count++;
                    mainUI.sendThreadUpdateMessage(true, count);
                } catch(IOException ioexception) {
                    AppLog.i(TAG, "cmd downlink--->write data to car ioexception:" + ioexception.toString());
                    if (ioexception.toString().contains("Connection reset")) {//reconnect 
                        /*if android client started after proxy started
                         wificar will close the socket without client communication
                         hence, it need to reconnect socket & resend the buffered command
                         */
                        b_connected_car_cmd = false;
                        bufferedDownlinkCmd = tmp;
                        mainUI.reConnectCarDelayed(100);//wait 100ms
                        return;
                    } else {
                        break;
                    }
                }
            } while(true);
        }
    };

    Runnable runnable_media_uplink = new Runnable() {
        //upload media from wificar to cloud
        public void run()//this is main receive loop
        {
            int i;
            int count = 0;
            AppLog.i(TAG, "media uplink Thread ---> start running");
            do {
                try {
                    i = mediaInputStreamUplink.available();
                    if(i <= 0 || i >= MEDIA_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    AppLog.i(TAG, "media uplink Thread ---> read data len:" + i);
                    mediaInputStreamUplink.read(mediaUplinkBuffer, 0, i);
                } catch(IOException ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "media uplink--->read data from car ioexception!, just exit thread!");
                    break;
                }
                

                try {
                    byte tmp[] = arrayCopy(mediaUplinkBuffer, 0, i);
                    cmdOutputStreamUplink.write(tmp);//use same uplink socket for both cmd & media
                    cmdOutputStreamUplink.flush();
                    count ++;
                    mainUI.sendThreadUpdateMessage(false, count);
                } catch(IOException ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "media uplink--->write data to cloud ioexception!, just exit thread!");
                    break;
                }
            } while(true);
        }
    };

    public boolean connectToCarCmd() throws IOException
    {
        if (b_connected_car_cmd) {
            AppLog.i(TAG, "car cmd--->alreay connect, just return");
            return true;
        }
        if (carCmdSocket!= null) {
            carCmdSocket.close();
        }
        carCmdSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);
        carCmdSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "Car Cmd Socket ---> connected:" + CAR_HOST_ADDR);
        carCmdSocket.setSendBufferSize(MEDIA_BUF_LEN);
        if(!carCmdSocket.isConnected()){
            AppLog.i(TAG, "--->socket init failed!");
            throw new IOException();
        }

        cmdOutputStreamDownlink = new DataOutputStream(carCmdSocket.getOutputStream());//belong to downlink thread
        cmdInputStreamUplink = new DataInputStream(carCmdSocket.getInputStream());//uplink
        b_connected_car_cmd = true;

        boolean reConnect = false;
        if (bufferedDownlinkCmd != null && bufferedDownlinkCmd.length > 4) {
            cmdOutputStreamDownlink.write(bufferedDownlinkCmd);
            cmdOutputStreamDownlink.flush();
            bufferedDownlinkCmd = null;
            reConnect = true;
        }
        if (b_connected_car_cmd && b_connected_cloud_cmd) {
            thread_cmd_uplink = new Thread(runnable_cmd_uplink);
            thread_cmd_uplink.setName("cmd_uplink Thread");
            thread_cmd_uplink.start();
        }

        if (b_connected_car_cmd && b_connected_cloud_cmd) {
            thread_cmd_downlink = new Thread(runnable_cmd_downlink);
            thread_cmd_downlink.setName("cmd_downlink Thread");
            thread_cmd_downlink.start();
            
        }
        return b_connected_car_cmd;
    }

    public boolean ConnectToCarMedia(int i) throws IOException {
        if (b_connected_car_media) {
            AppLog.i(TAG, "--->alreay connect media socket, just return");
            return true;
        }
        
        if (carMediaSocket!= null) {
            carMediaSocket.close();
        }
        AppLog.i(TAG, "--->media socket connecting .....");
        carMediaSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress addr = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);
        AppLog.i(TAG, "--->media socket connecting .....");
        carMediaSocket.connect(addr, 5000);

        if(!carMediaSocket.isConnected()){
            AppLog.i(TAG, "--->media socket connect failed!");
            throw new IOException();
        }
        AppLog.i(TAG, "Car Media Socket ---> connected:" + CAR_HOST_ADDR);
        
        b_connected_car_media = true;
        mediaInputStreamUplink = new DataInputStream(carMediaSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(carMediaSocket.getOutputStream());
        byte abyte0[] = CommandEncoder.cmdMediaLoginReq(i);//this is important
        out.write(abyte0);
        out.flush();

        if (b_connected_car_cmd && b_connected_cloud_cmd) {
            //use cmd uplink socket for media uplink
            thread_media_uplink = new Thread(runnable_media_uplink);
            thread_media_uplink.setName("media_uplink Thread");
            thread_media_uplink.start();
        }

        return b_connected_car_media;
    }
    
    //not use
    public boolean ConnectToCloudMedia(Network network) throws IOException
    {
        if (b_connected_cloud_media) {
            AppLog.i(TAG, "--->alreay connect cloud media socket, just return");
            return true;
        }
        if (cloudMediaSocket!= null) {
            cloudMediaSocket.close();
        }
        if (network ==null) return false;
        
        cloudMediaSocket = SocketFactory.getDefault().createSocket();
        //cloudSocket.setSendBufferSize(128 * 1024);//important, it make sure jpg data

        AppLog.i(TAG, "--->cloud media socket connecting .....");


        InetAddress addr = network.getByName(CLOUD_HOST_NAME);
        AppLog.i(TAG, "--->get dns addr:" + addr.getHostAddress());

        network.bindSocket(cloudMediaSocket);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(addr, CLOUD_PORT);

        cloudMediaSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "Cloud Media Socket ---> connected:" + inetSocketAddress);
        if(!cloudMediaSocket.isConnected()){
            AppLog.i(TAG, "--->cloud socket init failed!");
            throw new IOException();
        }
        b_connected_cloud_media = true;
        mediaOutputStreamUplink = new DataOutputStream(cloudCmdSocket.getOutputStream());

        if (b_connected_cloud_media && b_connected_car_media && !(thread_media_uplink.getState() == Thread.State.RUNNABLE)) {
            thread_media_uplink.setName("media_uplink Thread");
            thread_media_uplink.start();
        }

        return b_connected_cloud_media;
    }

    public boolean ConnectToCloudCmd(Network network) throws IOException
    {
        if (b_connected_cloud_cmd) {
            AppLog.i(TAG, "--->alreay connect cloud socket, just return");
            return true;
        }

        cloudCmdSocket = SocketFactory.getDefault().createSocket();
        cloudCmdSocket.setSendBufferSize(64 * 1024);//important, it make sure jpg data

        AppLog.i(TAG, "--->cloud cmd socket connecting .....");
        if (network ==null) return false;

        InetAddress addr = network.getByName(CLOUD_HOST_NAME);
        AppLog.i(TAG, "--->get cloud dns addr:" + addr.getHostAddress());

        network.bindSocket(cloudCmdSocket);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(addr, CLOUD_PORT);

        cloudCmdSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "Cloud Cmd Socket ---> connected:" + inetSocketAddress);
        if(!cloudCmdSocket.isConnected()){
            AppLog.i(TAG, "--->cloud socket init failed!");
            throw new IOException();
        }
        b_connected_cloud_cmd = true;
        cmdOutputStreamUplink = new DataOutputStream(cloudCmdSocket.getOutputStream());//belong to uplink thread
        cmdInputStreamDownlink = new DataInputStream(cloudCmdSocket.getInputStream());//downlink

        if (b_connected_car_cmd && b_connected_cloud_cmd) {
            thread_cmd_uplink = new Thread(runnable_cmd_uplink);
            thread_cmd_uplink.setName("cmd_uplink Thread");
            thread_cmd_uplink.start();
        }

        if (b_connected_car_cmd && b_connected_cloud_cmd) {
            thread_cmd_downlink = new Thread(runnable_cmd_downlink);
            thread_cmd_downlink.setName("cmd_downlink Thread");
            thread_cmd_downlink.start();
        }

        return b_connected_cloud_cmd;
    }

    public int isCloudSocketConnected() {
        try {
          if (cloudCmdSocket == null)
            return 0; 
          boolean bool = cloudCmdSocket.isConnected();
            return 1;
        } catch (Exception exception) {
          exception.printStackTrace();
        } 
        return 0;
    }

    public int isCarSocketConnected() {
        try {
          if (carCmdSocket == null)
            return 0; 
          boolean bool = carCmdSocket.isConnected();
            return 1;
        } catch (Exception exception) {
          exception.printStackTrace();
        } 
        return 0;
    }

    private byte[] arrayCopy(byte[] src, int start, int len) {
        if (src == null || start < 0 || len <= 0 || start + len > src.length) {
            return null;
        }
        byte[] ret = new byte[len];
        System.arraycopy(src, start, ret, 0, len);
        return ret;
    }
    public int parseVideoStartResp(byte buf[]) {
        int k = findstr(buf, "MO_O");
        int op = byteArrayToInt(buf, 4, 2);
        if (k == 0 && op == 5) {
            return byteArrayToInt(buf, 25, 4);
        }
        return -1;
    }
    public int byteArrayToInt(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
        int i = 0;
        for (int j = 0;; j++) {
          int k;
          if (j >= paramInt2)
            return i; 
          if (j == 0 && paramArrayOfbyte[paramInt2 - 1 + paramInt1 - j] < 0) {
            k = i | paramArrayOfbyte[paramInt2 - 1 + paramInt1 - j] & 0xFFFFFFFF;
          } else {
            k = i | paramArrayOfbyte[paramInt2 - 1 + paramInt1 - j] & 0xFF;
          } 
          i = k;
          if (j < paramInt2 - 1)
            i = k << 8; 
        } 
    }
    private int findstr(byte[] buf, String str) {
        if (str.length() == 4) {
            byte[] str2byte = str.getBytes();
            int i;
            int len = buf.length;
            for (i = 0; i < len -3; i++) {
                if (buf[i] == str2byte[0] && buf[i+1] == str2byte[1]
                    && buf[i+2] == str2byte[2] && buf[i+3] == str2byte[3]) {
                    return i;
                }
            }
        } 
        return -1;
    }

}
