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
    private static final String TAG = "CarProxy";
    private static final boolean DBG = false;

    Socket carCmdSocket;
    Socket carMediaSocket;

    Socket cloudCmdSocket;

    Socket clientCmdSocket;
    Socket clientMediaSocket;


    DataInputStream cmdInputStreamUplink = null;//belong to carCmdSocket
    DataOutputStream cmdOutputStreamUplink = null;//belong to clientCmdSocket

    DataInputStream cmdInputStreamDownlink = null;//belong to clientCmdSocket
    DataOutputStream cmdOutputStreamDownlink = null;//belong to carCmdSocket

    DataInputStream mediaInputStreamUplink = null;//belong to carMediaSocket
    DataOutputStream mediaOutputStreamUplink = null;//belong to clientMediaSocket

    private CarProxy instance;


    private Main mainUI;


    public boolean b_connected_car_cmd = false;
    public boolean b_connected_cloud_cmd = false;
    public boolean b_connected_car_media = false;
    public boolean b_connected_client = false;

    //this sock only for uplink
    private static final String CLOUD_HOST_NAME = "obana.f3322.org";
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

    private boolean carReady = false;//set carReady to TRUE after mediaLoginResp received
    private String cameraId = "";//camera id from cmdLoginResp
    int L1, L2, R1, R2;//for cmdVerifyReq

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
                    if (cmdInputStreamUplink == null || cmdOutputStreamUplink == null) {
                        Thread.sleep(2000);
                        continue;
                    }
                    i = cmdInputStreamUplink.available();
                    if(i <= 0 || i >= CMD_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    if (DBG) AppLog.i(TAG, "cmd uplink Thread ---> read data len:" + i);
                    cmdInputStreamUplink.read(cmdUplinkBuffer, 0, i);
                } catch(Exception ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "cmd uplink--->read data from car ioexception!, just exit thread!");
                    break;
                }

                try {

                    parseCommand(cmdUplinkBuffer, i);

                    if (carReady == false) {
                        /*this means car is not ready, not forward cmd to client
                         * it will start cmd forwarding after mediaLoginResp is received
                         * */
                        continue;
                    }

                    //forward cmd to client
                    byte tmp[] = arrayCopy(cmdUplinkBuffer, 0, i);
                    cmdOutputStreamUplink.write(tmp);
                    cmdOutputStreamUplink.flush();

                } catch(IOException ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "cmd uplink--->write data to cloud ioexception!, just exit thread!");
                    break;
                }
            } while(true);
            thread_cmd_uplink = null;
            b_connected_car_cmd = false;
            carReady = false;//this means car is disconnected
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
                    if (cmdInputStreamDownlink == null || cmdOutputStreamDownlink == null) {
                        Thread.sleep(2000);
                        continue;
                    }
                    i = cmdInputStreamDownlink.available();//read from cloud
                    
                    if(i <= 0 || i >= CMD_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    if (DBG) AppLog.i(TAG, "cmd downlink Thread ---> read data len:" + i);
                    cmdInputStreamDownlink.read(cmdDownlinkBuffer, 0, i);
                } catch(Exception ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "cmd downlink--->read data from cloud ioexception!, just exit thread!");
                    break;
                }

                if (carReady == false) {
                    /*this means car is not ready, not forward cmd to car
                    * it will start cmd forwarding after mediaLoginResp is received
                    * */
                    continue;
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
                        //bufferedDownlinkCmd = tmp;
                        mainUI.reConnectCarDelayed(100);//wait 100ms
                        return;
                    } else {
                        break;
                    }
                }
            } while(true);
            thread_cmd_downlink = null;
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
                    if (mediaInputStreamUplink == null || mediaOutputStreamUplink == null || carReady == false) {
                        /*it is important to stop media forward in case socket is not ready
                        * it alse not forwarding media when carReady is false
                        * it will start media forward after mediaLoginResp is received
                        * */
                        Thread.sleep(2000);
                        continue;
                    }
                    i = mediaInputStreamUplink.available();
                    if(i <= 0 || i >= MEDIA_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    //AppLog.i(TAG, "media uplink Thread ---> read data len:" + i);
                    mediaInputStreamUplink.read(mediaUplinkBuffer, 0, i);
                } catch(Exception ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "media uplink--->read data from car ioexception!, just exit thread!");
                    break;
                }
                

                try {
                    byte tmp[] = arrayCopy(mediaUplinkBuffer, 0, i);
                    mediaOutputStreamUplink.write(tmp);//use same uplink socket for both cmd & media
                    mediaOutputStreamUplink.flush();
                    count ++;
                    mainUI.sendThreadUpdateMessage(false, count);
                } catch(IOException ioexception) {
                    ioexception.printStackTrace();
                    AppLog.i(TAG, "media uplink--->write data to cloud ioexception!, just exit thread!");
                    break;
                }
            } while(true);
            thread_media_uplink = null;
        }
    };

    public boolean connectToCarCmd() throws IOException
    {
        if (b_connected_car_cmd) {
            AppLog.i(TAG, "car cmd--->alreay connect, just return");
            return true;
        }
        if (carCmdSocket!= null) {
            b_connected_car_cmd = false;
            carCmdSocket.close();
        }
        carReady = false;

        carCmdSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);
        carCmdSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "Car Cmd Socket ---> connected:" + CAR_HOST_ADDR);
        carCmdSocket.setSendBufferSize(CMD_BUF_LEN);
        if(!carCmdSocket.isConnected()){
            AppLog.i(TAG, "--->socket init failed!");
            throw new IOException();
        }

        cmdOutputStreamDownlink = new DataOutputStream(carCmdSocket.getOutputStream());//belong to downlink thread
        cmdInputStreamUplink = new DataInputStream(carCmdSocket.getInputStream());//uplink
        b_connected_car_cmd = true;

        if (thread_cmd_uplink == null) {
            thread_cmd_uplink = new Thread(runnable_cmd_uplink);
            thread_cmd_uplink.setName("cmd_uplink Thread");
            thread_cmd_uplink.start();
        }

        if (thread_cmd_downlink == null) {
            thread_cmd_downlink = new Thread(runnable_cmd_downlink);
            thread_cmd_downlink.setName("cmd_downlink Thread");
            thread_cmd_downlink.start();
            
        }

        //TODO:this should be replaced by init CMD from client
        sendCmdLoginReq(cmdOutputStreamDownlink);

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
        AppLog.i(TAG, "Car Media Socket connecting .....");
        carMediaSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress addr = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);

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
        AppLog.i(TAG, "cmdMediaLoginReq send. value:" + i);

        if (thread_media_uplink == null) {
            //use cmd uplink socket for media uplink
            thread_media_uplink = new Thread(runnable_media_uplink);
            thread_media_uplink.setName("media_uplink Thread");
            thread_media_uplink.start();
        }
        carReady = true;//media input & output uplink stream is OK
        return b_connected_car_media;
    }

    public boolean ConnectToCloud(Network network) throws IOException
    {
        if (b_connected_cloud_cmd) {
            AppLog.i(TAG, "--->alreay connect cloud socket, just return");
            return true;
        }

        cloudCmdSocket = SocketFactory.getDefault().createSocket();
        cloudCmdSocket.setSendBufferSize(CMD_BUF_LEN);//important, it make sure jpg data

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

        return b_connected_cloud_cmd;
    }

    /*this will connect to client cmd & media socket*/
    public boolean ConnectToClientP2P(Network network) throws IOException
    {
        if (b_connected_client) {
            AppLog.i(TAG, "--->alreay connect client via p2p, just return");
            return true;
        }

        clientCmdSocket = SocketFactory.getDefault().createSocket();
        clientCmdSocket.setSendBufferSize(CMD_BUF_LEN);//important, it make sure jpg data

        clientMediaSocket = SocketFactory.getDefault().createSocket();
        clientMediaSocket.setSendBufferSize(MEDIA_BUF_LEN);//important, it make sure jpg data

        AppLog.i(TAG, "--->client p2p cmd&media socket connecting .....");
        if (network ==null) return false;

        String CLIENT_HOST_NAME = "obana.f3322.org";
        int CLIENT_HOST_PORT = 28001;
        InetAddress addr = network.getByName(CLIENT_HOST_NAME);

        network.bindSocket(clientCmdSocket);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(addr, CLIENT_HOST_PORT);

        clientCmdSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "Client Cmd Socket ---> connected:" + inetSocketAddress);
        if(!clientCmdSocket.isConnected()){
            AppLog.i(TAG, "--->client cmd socket connect failed!");
            throw new IOException();
        }

        network.bindSocket(clientMediaSocket);
        inetSocketAddress = new InetSocketAddress(addr, CLIENT_HOST_PORT+1);

        clientMediaSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "Client Meida Socket ---> connected:" + inetSocketAddress);
        if(!clientMediaSocket.isConnected()){
            AppLog.i(TAG, "--->client media socket connect failed!");
            throw new IOException();
        }

        b_connected_client = true;
        cmdOutputStreamUplink = new DataOutputStream(clientCmdSocket.getOutputStream());//belong to uplink thread
        cmdInputStreamDownlink = new DataInputStream(clientCmdSocket.getInputStream());//cmd downlink
        mediaOutputStreamUplink = new DataOutputStream(clientMediaSocket.getOutputStream());//media uplink

        /*cmd uplink*/
        if (thread_cmd_uplink == null) {
            thread_cmd_uplink = new Thread(runnable_cmd_uplink);
            thread_cmd_uplink.setName("cmd_uplink Thread");
            thread_cmd_uplink.start();
        }

        /*cmd downlink*/
        if (thread_cmd_downlink == null) {
            thread_cmd_downlink = new Thread(runnable_cmd_downlink);
            thread_cmd_downlink.setName("cmd_downlink Thread");
            thread_cmd_downlink.start();
        }

        /*media uplink*/
        if (thread_media_uplink == null) {
            thread_media_uplink = new Thread(runnable_media_uplink);
            thread_media_uplink.setName("media_uplink Thread");
            thread_media_uplink.start();//only media output stream is OK
        }
        return b_connected_client;
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

    void sendCmdLoginReq(DataOutputStream stream) {
        byte abyte0[] = null;
        try {
            abyte0 = CommandEncoder.cmdLoginReq(0, 0, 0, 0);

            stream.write(abyte0);
            stream.flush();
        } catch (IOException e) {

        }
    }

    void sendCmdVerifyReq(DataOutputStream stream) {
        byte abyte0[] = null;
        try {
            String key = getKey();
            abyte0 = CommandEncoder.cmdVerifyReq(key, L1, R1, L2, R2);

            stream.write(abyte0);
            stream.flush();
        } catch (IOException e) {

        }
    }

    void sendCmdVideoStartReq(DataOutputStream stream) {
        byte abyte0[] = null;
        try {
            abyte0 = CommandEncoder.cmdVideoStartReq();

            stream.write(abyte0);
            stream.flush();
        } catch (IOException e) {

        }
    }

    //no use
    void sendMediaLoginReq(DataOutputStream stream) {
        byte abyte0[] = null;
        try {
            abyte0 = CommandEncoder.cmdLoginReq(0, 0, 0, 0);

            stream.write(abyte0);
            stream.flush();
        } catch (IOException e) {

        }
    }

    String getKey() {
        String key = new StringBuilder(String.valueOf("AC13")).append(":").append(cameraId).append("-save-private:").append("AC13").toString();
        return key;
    }

    boolean parseLoginResp(byte abyte0[])
    {
        AppLog.d(TAG, "--->parseLoginResp start");

        //if(ByteUtility.byteArrayToInt(abyte0, 0, 2) != 0) return false;

        if (abyte0.length < 0x3B) {/*0x3B for 2.0,0x3E for 3.0*/
            AppLog.d(TAG, "UNKNOW LoginResp,JUST RETURN!");
            return false;
        }

        cameraId = ByteUtility.byteArrayToString(abyte0, 2, 13);
        L1 = ByteUtility.byteArrayToInt(abyte0, 43, 4);
        R1 = ByteUtility.byteArrayToInt(abyte0, 47, 4);
        L2 = ByteUtility.byteArrayToInt(abyte0, 51, 4);
        R2 = ByteUtility.byteArrayToInt(abyte0, 55, 4);

        AppLog.d(TAG, "--->camera id:" + cameraId);

        BlowFish bf = new BlowFish();

        bf.InitBlowfish(getKey().getBytes(), getKey().length());
        int[] ret = bf.Blowfish_encipher(L1, R1);
        L1 = ret[0]; R1 = ret[1];
        ret = bf.Blowfish_encipher(L2, R2);
        L2 = ret[0]; R2 = ret[1];

        sendCmdVerifyReq(cmdOutputStreamDownlink);
        return true;
    }

    void parseVerifyResp(byte abyte0[]) throws IOException {
        //just send video start
        if (cmdOutputStreamDownlink != null) sendCmdVideoStartReq(cmdOutputStreamDownlink);
    }

    void parseVideoStartResp(byte abyte0[])  throws IOException {
        int i = ByteUtility.byteArrayToInt(abyte0, 2, 4);
        if (i >= 0) ConnectToCarMedia(i);//it will send mediaLoginreq after socket connected
    }

    //no use
    void parseMediaLoginResp(byte abyte0[]) throws IOException {
        //just send video start
        if (cmdOutputStreamDownlink != null) sendCmdVideoStartReq(cmdOutputStreamDownlink);
    }


    int parseCommand(byte abyte0[], int i) throws IOException {

        int k = findstr(abyte0, "MO_O");
        if (k < 0) return -1;

        if(i <= 23) return -1;

        int op = ByteUtility.byteArrayToInt(abyte0, 4, 2);
        int len = ByteUtility.byteArrayToInt(abyte0, 15, 4);
        AppLog.d(TAG, "--->receive [" + i + "] bytes data op:" + op + " len:" + len);
        byte[] content;
        switch(op)
        {
            default:
                return -1;

            case 1: // '\001'
                content = new byte[len];
                System.arraycopy(abyte0, 23, content, 0, len);
                parseLoginResp(content);
                return 1;

            case 3: // '\003'
                content = new byte[len];
                System.arraycopy(abyte0, 23, content, 0, len);
                parseVerifyResp(content);
                return 1;

            case 5: // '\005'
                content = new byte[len];
                System.arraycopy(abyte0, 23, content, 0, len);
                parseVideoStartResp(content);
                return 1;

        }
    }
}
