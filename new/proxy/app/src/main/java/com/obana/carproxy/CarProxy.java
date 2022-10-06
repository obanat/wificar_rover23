package com.obana.carproxy;

import android.app.Activity;
import android.net.Network;

import com.obana.carproxy.utils.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.InetAddress;
import java.util.HashMap;
import javax.net.SocketFactory;

public class CarProxy
{
    private static final String TAG = "CarProxy_T";
    private static final boolean DBG = false;

    Socket carCmdSocket;
    Socket carMediaSocket;

    Socket cloudSocket;

    Socket clientCmdSocket;
    Socket clientMediaSocket;


    DataInputStream cmdInputStreamUplink = null;//belong to carCmdSocket
    DataOutputStream cmdOutputStreamUplink = null;//belong to clientCmdSocket

    DataInputStream cmdInputStreamDownlink = null;//belong to clientCmdSocket
    DataOutputStream cmdOutputStreamDownlink = null;//belong to carCmdSocket

    DataInputStream mediaInputStreamUplink = null;//belong to carMediaSocket
    DataOutputStream mediaOutputStreamUplink = null;//belong to clientMediaSocket

    DataInputStream cloudInputStream = null;//belong to carCmdSocket
    DataOutputStream cloudOutputStream = null;//belong to clientCmdSocket

    private CarProxy instance;


    private Main mainUI;
    Network cachedNetwork = null;

    //this sock only for uplink
    private static final String CLOUD_HOST_NAME = "obana.f3322.org";
    private static final int CLOUD_PORT = 19092;

    //this sock for uplink & downlink command
    private static final String CAR_HOST_ADDR = "192.168.1.100";
    private static final int CAR_PORT = 80;

    //this sock for media uplink
    private static final String CAR_MEDIA_HOST_ADDR = "192.168.1.100";
    private static final int CAR_MEDIA_PORT = 80;

    private static final int STATE_THREAD_RUNNING = 1;
    private static final int STATE_THREAD_IDLE = 0;

    private static final int CMD_BUF_LEN = 1024;
    private static final int MEDIA_BUF_LEN = (64*1024);

    private static final int STATE_INIT                 = 100;
    private static final int STATE_WIFI_OK              = 101;
    private static final int STATE_CAR_CONNECTED        = 102;
    private static final int STATE_CELL_OK              = 103;
    private static final int STATE_CLOUD_CONNECTED      = 104;
    private static final int STATE_PROXY_REGED          = 105;
    private static final int STATE_CLIENT_CONNECTED     = 106;
    HashMap<Integer, String> stateDebugMsgs = new HashMap<Integer, String>() {
        {   put(STATE_INIT, "STATE_INIT");
            put(STATE_WIFI_OK, "STATE_WIFI_OK");
            put(STATE_CAR_CONNECTED, "STATE_CAR_CONNECTED");
            put(STATE_CELL_OK, "STATE_CELL_OK");
            put(STATE_CLOUD_CONNECTED, "STATE_CLOUD_CONNECTED");
            put(STATE_PROXY_REGED, "STATE_PROXY_REGED");
            put(STATE_CLIENT_CONNECTED, "STATE_CLIENT_CONNECTED");
        }
    };
    private int mState = STATE_INIT;

    private byte[] cmdUplinkBuffer;
    private byte[] cmdDownlinkBuffer;
    private byte[] mediaUplinkBuffer;
    private byte[] cloudBuffer;

    //private int cmdUplinkBufferLen = 0;
    private byte[] bufferedDownlinkCmd = null;

    private String cameraId = "";//camera id from cmdLoginResp
    int L1, L2, R1, R2;//for cmdVerifyReq

    Thread thread_cmd_downlink = null;
    Thread thread_cmd_uplink = null;
    Thread thread_media_uplink = null;
    Thread thread_cloud = null;

    int clientIp = 0;
    int clientPort = 0;

    public CarProxy(Activity activity)
    {
        instance = this;
        mainUI = (Main)activity;

        cmdUplinkBuffer = new byte[CMD_BUF_LEN];
        cmdDownlinkBuffer = new byte[CMD_BUF_LEN];
        mediaUplinkBuffer = new byte[MEDIA_BUF_LEN];

        cloudBuffer = new byte[CMD_BUF_LEN];
        mState = STATE_INIT;
        AppLog.d(TAG, "new CarProxy created successfully!");
    }

    int state_cmd_uplink = STATE_THREAD_IDLE;
    Runnable runnable_cmd_uplink = new Runnable() {
        //upload cmd from wificar to cloud
        public void run()
        {
            int i;
            state_cmd_uplink = STATE_THREAD_RUNNING;
            AppLog.i(TAG, "cmd uplink Thread ---> start running");
            do {
                try {
                    if (cmdInputStreamUplink == null) {
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

                    if (cmdOutputStreamUplink == null) {
                        continue;
                    }

                    //forward cmd to client
                    byte tmp[] = arrayCopy(cmdUplinkBuffer, 0, i);
                    cmdOutputStreamUplink.write(tmp);
                    cmdOutputStreamUplink.flush();

                } catch(IOException ioexception) {
                    AppLog.i(TAG, "cmd uplink--->write data to cloud ioexception!, just exit thread!");
                    break;
                }
            } while(true);
            state_cmd_uplink = STATE_THREAD_IDLE;
        }
    };

    int state_cmd_downlink = STATE_THREAD_IDLE;
    Runnable runnable_cmd_downlink = new Runnable() {
        //upload cmd from wificar to cloud
        public void run()//this is main receive loop
        {
            int i;
            int count = 0;
            state_cmd_downlink = STATE_THREAD_RUNNING;
            byte tmp[] = null;
            do {
                try {
                    if (cmdInputStreamDownlink == null) {
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

                if (cmdOutputStreamDownlink == null) {
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
                    break;
                }
            } while(true);
            state_cmd_downlink = STATE_THREAD_IDLE;
        }
    };

    int state_media_uplink = STATE_THREAD_IDLE;
    Runnable runnable_media_uplink = new Runnable() {
        //upload media from wificar to cloud
        public void run()//this is main receive loop
        {
            int i;
            int count = 0;
            state_media_uplink = STATE_THREAD_RUNNING;
            AppLog.i(TAG, "media uplink Thread ---> start running");
            do {
                try {
                    if (mediaInputStreamUplink == null) {
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
                    AppLog.i(TAG, "media uplink--->read data from car ioexception!, just exit thread!");
                    break;
                }

                if (mediaOutputStreamUplink == null) {
                    /*this means client is not ready * */
                    continue;
                }
                try {
                    byte tmp[] = arrayCopy(mediaUplinkBuffer, 0, i);
                    mediaOutputStreamUplink.write(tmp);//use same uplink socket for both cmd & media
                    mediaOutputStreamUplink.flush();
                    count ++;
                    mainUI.sendThreadUpdateMessage(false, count);
                } catch(IOException ioexception) {
                    AppLog.i(TAG, "media uplink--->write data to cloud ioexception!, just exit thread!");
                    break;
                }
            } while(true);
            state_media_uplink = STATE_THREAD_IDLE;
        }
    };

    //cloud runnable
    int state_cloud = STATE_THREAD_IDLE;
    Runnable runnable_cloud = new Runnable() {
        //thread of cloud socket
        public void run()//this is main receive loop
        {
            int i;
            byte[] tmp = null;
            state_cloud = STATE_THREAD_RUNNING;
            AppLog.i(TAG, "---> cloud recv thread running ....");
            do {
                try {
                    if (cloudInputStream == null) {
                        AppLog.e(TAG, "--->runnable_cloud input stream error, just exit thread");
                        break;
                    }
                    i = cloudInputStream.available();//read from cloud

                    if(i <= 0 || i >= CMD_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    if (DBG) AppLog.i(TAG, "cloud Thread ---> read data len:" + i);
                    cloudInputStream.read(cloudBuffer, 0, i);
                } catch(Exception ioexception) {
                    AppLog.e(TAG, "cloud thread ioexception!, just exit thread!");
                    break;
                }

                try {
                    tmp = arrayCopy(cloudBuffer, 0, i);
                    parseCloudCommand(tmp, i);
                } catch(IOException ioexception) {
                    AppLog.e(TAG, "parseCloudCommand ioexception!");
                }
            } while(true);
            state_cloud = STATE_THREAD_IDLE;
        }
    };

    public boolean connectToCarCmd() throws IOException
    {
        mState = STATE_WIFI_OK;
        if (state_cmd_downlink == STATE_THREAD_RUNNING && state_cmd_uplink == STATE_THREAD_RUNNING) {
            AppLog.i(TAG, "cmd uplink&downlink working, do nothing.");
            return true;
        }
        if (carCmdSocket!= null) {
            AppLog.i(TAG, "close previous car cmd socket...");
            carCmdSocket.close();
        }
        if (cmdOutputStreamDownlink != null ){
            cmdOutputStreamDownlink.close();
            cmdOutputStreamDownlink = null;
        }
        if (cmdInputStreamUplink != null ){
            cmdInputStreamUplink.close();
            cmdInputStreamUplink = null;
        }

        carCmdSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);
        carCmdSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "car cmd Socket ---> connected:" + CAR_HOST_ADDR + " port:" + CAR_PORT);
        carCmdSocket.setSendBufferSize(CMD_BUF_LEN);
        if(!carCmdSocket.isConnected()){
            AppLog.i(TAG, "--->car cmd socket init failed!");
            throw new IOException();
        }

        cmdOutputStreamDownlink = new DataOutputStream(carCmdSocket.getOutputStream());//belong to downlink thread
        cmdInputStreamUplink = new DataInputStream(carCmdSocket.getInputStream());//uplink

        if (thread_cmd_uplink == null) {//not use
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
        AppLog.i(TAG, "send login request via car cmd Socket");
        sendCmdLoginReq(cmdOutputStreamDownlink);

        return true;
    }

    public boolean ConnectToCarMedia(int i) throws IOException {

        if (carMediaSocket!= null) {
            carMediaSocket.close();
        }

        if (mediaInputStreamUplink != null ){
            mediaInputStreamUplink.close();
            mediaInputStreamUplink = null;
        }
        AppLog.i(TAG, "car media socket connecting .....");
        carMediaSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress addr = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);

        carMediaSocket.connect(addr, 5000);

        if(!carMediaSocket.isConnected()){
            AppLog.i(TAG, "--->media socket connect failed!");
            throw new IOException();
        }
        AppLog.i(TAG, "car media socket connected!");

        mediaInputStreamUplink = new DataInputStream(carMediaSocket.getInputStream());

        //send media login req
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
        mState = STATE_CAR_CONNECTED;
        return true;
    }

    public boolean ConnectToCloud(Network network) throws IOException
    {
        mState = STATE_CELL_OK;
        AppLog.i(TAG, "STEP 3: start connect to cloud .... ");
        if (state_cloud == STATE_THREAD_RUNNING) {
            AppLog.i(TAG, "cloud thread working, do nothing.");
            return true;
        }

        if (cloudSocket!= null) {
            cloudSocket.close();
        }

        if (cloudInputStream != null ){
            cloudInputStream.close();
            cloudInputStream = null;
        }
        if (cloudOutputStream != null ){
            cloudOutputStream.close();
            cloudOutputStream = null;
        }
        cloudSocket = SocketFactory.getDefault().createSocket();
        cloudSocket.setSendBufferSize(CMD_BUF_LEN);//important, it make sure jpg data

        AppLog.i(TAG, "--->cloud socket connecting .....");

        if (network ==null) return false;

        cachedNetwork = network;//used by client p2p connection

        InetAddress addr = network.getByName(CLOUD_HOST_NAME);
        AppLog.i(TAG, "HOST name:" +  CLOUD_HOST_NAME + " addr:" + addr.getHostAddress()
            + " port:" + CLOUD_PORT);

        network.bindSocket(cloudSocket);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(addr, CLOUD_PORT);

        cloudSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "--->cloud socket connected:" + inetSocketAddress);

        if(!cloudSocket.isConnected()){
            AppLog.e(TAG, "--->cloud socket connect failed!");
            throw new IOException();
        }
        cloudInputStream = new DataInputStream(cloudSocket.getInputStream());
        cloudOutputStream = new DataOutputStream(cloudSocket.getOutputStream());

        if (thread_cloud == null) {
            //start recv loop
            thread_cloud = new Thread(runnable_cloud);
            thread_cloud.setName("cloud Thread");
            thread_cloud.start();
        }

        AppLog.i(TAG, "STEP 4: send reg cmd to cloud .... ");
        sendCloudRegReq();//reg to cloud
        mState = STATE_CLOUD_CONNECTED;
        return true;
    }

    /*this will connect to client cmd & media socket*/
    public boolean ConnectToClientP2P() throws IOException
    {
        AppLog.i(TAG, "--->connecting to client via p2p .....");

        //clean up resource
        if (clientCmdSocket!= null) {
            clientCmdSocket.close();
        }

        if (cmdOutputStreamUplink != null ){
            cmdOutputStreamUplink.close();
            cmdOutputStreamUplink = null;
        }
        if (cmdInputStreamDownlink != null ){
            cmdInputStreamDownlink.close();
            cmdInputStreamDownlink = null;
        }
        if (mediaOutputStreamUplink != null ){
            mediaOutputStreamUplink.close();
            mediaOutputStreamUplink = null;
        }

        clientCmdSocket = SocketFactory.getDefault().createSocket();
        clientCmdSocket.setSendBufferSize(CMD_BUF_LEN);//important, it make sure jpg data

        if (clientMediaSocket!= null) {
            clientMediaSocket.close();
        }
        clientMediaSocket = SocketFactory.getDefault().createSocket();
        clientMediaSocket.setSendBufferSize(MEDIA_BUF_LEN);//important, it make sure jpg data

        AppLog.i(TAG, "--->client p2p cmd&media socket connecting .....");
        if (cachedNetwork ==null) return false;

        cachedNetwork.bindSocket(clientCmdSocket);
        InetSocketAddress inetSocketAddress;

        if (clientIp>0 && clientPort>0) {
            inetSocketAddress = new InetSocketAddress(int2ip(clientIp), clientPort);
        }  else {
            return  false;
        }

        AppLog.i(TAG, "client cmd cocket connecting ...." + inetSocketAddress);
        clientCmdSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "client cmd cocket connected!");

        if(!clientCmdSocket.isConnected()){
            AppLog.i(TAG, "--->client cmd socket connect failed!");
            throw new IOException();
        }

        cachedNetwork.bindSocket(clientMediaSocket);

        if (clientIp>0 && clientPort>0) {
            inetSocketAddress = new InetSocketAddress(int2ip(clientIp), clientPort+1);
        } else {
            return  false;
        }

        AppLog.i(TAG, "client media socket connecting ...." + inetSocketAddress);
        clientMediaSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "client media socket connected!");

        if(!clientMediaSocket.isConnected()){
            AppLog.i(TAG, "--->client media socket connect failed!");
            throw new IOException();
        }

        cmdOutputStreamUplink = new DataOutputStream(clientCmdSocket.getOutputStream());//belong to uplink thread
        cmdInputStreamDownlink = new DataInputStream(clientCmdSocket.getInputStream());//cmd downlink
        mediaOutputStreamUplink = new DataOutputStream(clientMediaSocket.getOutputStream());//media uplink

        mState = STATE_CLIENT_CONNECTED;
        return true;
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
            AppLog.i(TAG, "send login request successfully!");
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

        AppLog.i(TAG, "STEP 1.1: connect to car media, i:" + i);
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
        AppLog.i(TAG, "--->receive [" + i + "] bytes data op:" + op + " len:" + len);
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

    void sendCloudRegReq() {
        byte abyte0[] = null;

        try {
            if (cloudSocket!= null && cloudSocket.isConnected()
                && cloudOutputStream != null) {
                abyte0 = CommandEncoder.cmdCloudRegReq();
                cloudOutputStream.write(abyte0);
                cloudOutputStream.flush();
                AppLog.i(TAG, "send reg cmd to cloud successfully! waitng response.....");
            }
        } catch (IOException e){

        }
    }
    int parseCloudCommand(byte abyte0[], int i) throws IOException {

        int k = findstr(abyte0, "MO_U");//means cloud
        if (k < 0) return -1;

        if(i <= 23) return -1;

        int op = ByteUtility.byteArrayToInt(abyte0, 4, 2);
        int len = ByteUtility.byteArrayToInt(abyte0, 15, 4);
        AppLog.i(TAG, "---> cloud: receive [" + i + "] bytes data. op:" + op + " len:" + len);
        byte[] content;
        switch(op)
        {
            case 33: //cloud reg response
                content = new byte[len];
                System.arraycopy(abyte0, 23, content, 0, len);
                parseCloudRegResp(content);
                return 1;
            default:
                break;
        }
        return -1;
    }
    void parseCloudRegResp(byte abyte0[]) throws IOException {
        //get client ip & port
        if (abyte0.length == 8) {
            mState = STATE_PROXY_REGED;
            clientIp = ByteUtility.byteArrayToInt(abyte0, 0, 4);
            clientPort = ByteUtility.byteArrayToInt(abyte0, 4, 4);
            AppLog.i(TAG, "reg to cloud successfully!" );

            AppLog.i(TAG, "STEP5: connect to client ip:" + int2ip(clientIp) + " port:" + clientPort);
            mainUI.connectToClient();//use main loop to invoke ConnectToCloud
        }
    }

    private String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    public String printDebugMsg() {
        StringBuilder sb = new StringBuilder();

        sb.append("Thread CMD_UPLINK :     ").append(state_cmd_uplink == STATE_THREAD_RUNNING?"RUNNING":"IDLE").append("\r\n");
        sb.append("Thread CMD_DOWNLINK :     ").append(state_cmd_downlink == STATE_THREAD_RUNNING?"RUNNING":"IDLE").append("\r\n");
        sb.append("Thread CLOUD :     ").append(state_cloud == STATE_THREAD_RUNNING?"RUNNING":"IDLE").append("\r\n");
        sb.append("Thread MEDIA :     ").append(state_media_uplink == STATE_THREAD_RUNNING?"RUNNING":"IDLE").append("\r\n");

        sb.append("\r\n");
        sb.append("STATE :    ").append(mState).append(">>>").append(stateDebugMsgs.get(mState)).append("\r\n");
        return sb.toString();
    }

    public boolean matchWifiCarAddr(String ip) {
        return CAR_HOST_ADDR.equals(ip);
    }
}
