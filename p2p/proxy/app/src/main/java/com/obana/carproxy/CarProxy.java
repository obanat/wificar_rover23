package com.obana.carproxy;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import com.obana.carproxy.utils.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import javax.net.SocketFactory;

public class CarProxy
{
    private static final String TAG = "CarProxy";
    private static final boolean DBG = true;

    private final Main mainUI;

    //this sock for uplink & downlink command
    private static final String CAR_HOST_ADDR = "192.168.1.100";
    private static final int CAR_PORT = 80;

    private static final int USE_H264 = 1;


    private static final int CMD_BUF_LEN = 1024;
    private static final int MEDIA_BUF_LEN = (64*1024);

    private static final int CAR_STATE_INIT                     = 100;
    private static final int CAR_STATE_WIFI_CONNECTED           = 101;
    private static final int CAR_STATE_SOCKET_CONNECTED         = 103;
    private static final int CAR_STATE_RUNNING                  = 104;
    private static final int CAR_STATE_STOPPED                  = 105;

    private static final int CLOUD_STATE_INIT                   = 110;
    private static final int CLOUD_STATE_CELL_OK                = 111;
    private static final int CLOUD_STATE_IP_OK                  = 112;
    private static final int CLOUD_STATE_SERVER_OK              = 113;
    private static final int CLOUD_STATE_RUNNING                = 114;
    private static final int CLOUD_STATE_STOPPED                = 115;

    HashMap<Integer, String> stateDebugMsgs = new HashMap<Integer, String>() {
        {   put(CAR_STATE_INIT, "INIT");
            put(CAR_STATE_WIFI_CONNECTED, "WIFI");
            put(CAR_STATE_SOCKET_CONNECTED, "SOCKET");
            put(CAR_STATE_RUNNING, "RUNNING");
            put(CAR_STATE_STOPPED, "STOP");
            
            put(CLOUD_STATE_INIT, "INIT");
            put(CLOUD_STATE_CELL_OK, "CELL");
            put(CLOUD_STATE_IP_OK, "IP");

            put(CLOUD_STATE_SERVER_OK, "SOCKET");
            put(CLOUD_STATE_RUNNING, "RUNNING");
            put(CLOUD_STATE_STOPPED, "STOP");
        }
    };
    private int mCarState;
    private int mCloudState;

    private static final int CMD_PORT = 28000;
    private static final int MEDIA_PORT = 28001;
    ServerSocket cloudCmdSocket = null;
    DataInputStream cloudCmdInputStream = null;
    DataOutputStream cloudCmdOutputStream = null;

    ServerSocket cloudMediaSocket = null;
    DataInputStream cloudMediaInputStream = null;
    DataOutputStream cloudMediaOutputStream = null;

    Socket carCmdSocket = null;
    Socket carMediaSocket = null;
    DataInputStream carCmdInputStream = null;
    DataOutputStream carCmdOutputStream = null;
    DataInputStream carMediaInputStream = null;
    DataOutputStream carMediaOutputStream = null; //just for media login cmd

    private final byte[] cmdUplinkBuffer;
    private final byte[] cmdDownlinkBuffer;
    private final byte[] mediaUplinkBuffer;
    private final byte[] mediaDownlinkBuffer;

    //Thread thread_cmd_downlink = null;
    Thread thread_cmd_uplink = null;
    Thread thread_cmd_downlink = null;
    Thread thread_media_uplink = null;
    Thread thread_media_downlink = null;
    Thread thread_media_codec = null;

    public CarProxy(Activity activity)
    {
        mainUI = (Main)activity;

        cmdUplinkBuffer = new byte[CMD_BUF_LEN];
        cmdDownlinkBuffer = new byte[CMD_BUF_LEN];
        mediaUplinkBuffer = new byte[MEDIA_BUF_LEN];
        mediaDownlinkBuffer = new byte[CMD_BUF_LEN];

        mCarState = CAR_STATE_INIT;
        mCloudState = CLOUD_STATE_INIT;
        AppLog.i(TAG, "new CarProxy created successfully!");
    }
    private android.graphics.BitmapFactory.Options mBitmapOpt;
    int mWidth;//mjpg frame width
    int mHeight;
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int BIT_RATE = 240000;
    private static final int I_FRAME_INTERVAL = 2;


    void initMediaCodec() {
        //
        mBitmapOpt = new android.graphics.BitmapFactory.Options();
        mBitmapOpt.inDither = true;
        mBitmapOpt.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565;

        mWidth = 320;
        mHeight = 240;//rover2 320x240; rover3 640x480

        MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
        if (codecInfo == null) {
            AppLog.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        AppLog.i(TAG, "found codec: " + codecInfo.getName());
        int colorFormat;
        try {
            colorFormat = selectColorFormat(codecInfo, MIME_TYPE);
        } catch (Exception e) {
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        }

        try {
            mediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
        } catch (IOException e) {
            AppLog.e(TAG, "Unable to create MediaCodec " + e.getMessage());
            return;
        }

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();

        AppLog.i(TAG, "Initialization media codec complete. waiting start encoder...");
    }

    public void stopEncoding() {
        if (mediaCodec == null) {
            AppLog.d(TAG, "Failed to stop encoding since it never started");
            return;
        }
        AppLog.d(TAG, "Stopping encoding...");

        mH264EncodeRunning = false;
        synchronized (mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
                mNewFrameLatch.countDown();
            }
        }
    }
    /*
    * thread for transact cmd from car to client
    * init state:SOCKET(103)
    * running state:running(104)
    * stop state:stop(105)
    */
    int mCmdUpLinkCount = 0;
    Runnable runnable_cmd_uplink = new Runnable() {
        //upload cmd from wificar to cloud
        public void run()
        {
            int i;
            mCmdUpLinkCount = 0;
            AppLog.i(TAG, "cmd uplink Thread ---> start running");
            do {
                try {
                    if (carCmdInputStream == null) {
                        Thread.sleep(2000);
                        continue;
                    }
                    i = carCmdInputStream.available();
                    if(i <= 0 || i >= CMD_BUF_LEN) {
                        //AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }
                    if (DBG) AppLog.i(TAG, "cmd uplink Thread ---> read data len:" + i);

                    carCmdInputStream.read(cmdUplinkBuffer, 0, i);
                } catch(Exception ioexception) {
                    AppLog.i(TAG, "cmd uplink--->read data from car ioexception!, just exit thread!");
                    break;
                }
                if (cmdUplinkBuffer[0] == 77 && cmdUplinkBuffer[1] == 79) {
                    AppLog.i(TAG, "====resp op:" + ByteUtility.byteArrayToInt(cmdUplinkBuffer, 4, 2));
                }
                try {

                    //parseCommand(cmdUplinkBuffer, i);--no need to parse,just passthrough
                    if (cloudCmdOutputStream == null) {
                        continue;
                    }

                    //forward cmd to client
                    byte tmp[] = arrayCopy(cmdUplinkBuffer, 0, i);
                    cloudCmdOutputStream.write(tmp);
                    cloudCmdOutputStream.flush();
                    AppLog.i(TAG, "transact cmd data from car to cloud, len:" + i);
                    mCmdUpLinkCount++;
                    mCarState = CAR_STATE_RUNNING;
                } catch(IOException ioexception) {
                    AppLog.i(TAG, "cmd uplink--->write data to client ioexception!, just exit thread!");
                    cloudCmdOutputStream = null;//make sure no ioexception here again when thread is re created.
                    thread_cmd_uplink = null;
                    mCarState = CAR_STATE_STOPPED;
                    break;
                }
            } while(true);
        }
    };

    /*
    * thread for transact cmd from client to car
    * init state:socket(113)
    * running state:running(114)
    * stop state:stop(115)
    */
    Runnable runnable_cmd_downlink = new Runnable() {
        public void run() {
            try {

                int len;
                mCmdDownLinkCount = 0;
                AppLog.i(TAG, "cmd downlink Thread ---> start running");

                while ((len = cloudCmdInputStream.read(cmdDownlinkBuffer)) != -1) {
                    //AppLog.i(TAG, "receive cmd data from cloud, len::::" + len);
                    if (len == 0 || len > CMD_BUF_LEN) {
                        continue;
                    }
                    AppLog.i(TAG, "receive cmd data from cloud, len:" + len);
                    if (cmdDownlinkBuffer[0] == 77 && cmdDownlinkBuffer[1] ==79) {
                        AppLog.i(TAG, "====req op:" + ByteUtility.byteArrayToInt(cmdDownlinkBuffer, 4, 2));
                    }

                    //transact cmd data to car
                    byte[] tmp = arrayCopy(cmdDownlinkBuffer, 0, len);
                    carCmdOutputStream.write(tmp);//send to car
                    carCmdOutputStream.flush();
                    mCmdDownLinkCount ++;
                    mCloudState = CLOUD_STATE_RUNNING;
                    AppLog.i(TAG, "transact cmd data from cloud to car, len:" + len);
                }
            } catch (Exception e) {
                AppLog.e(TAG, "transact cmd data from cloud to car, error!");
                mCloudState = CLOUD_STATE_STOPPED;
            } finally {
                AppLog.e(TAG, "transact cmd data from cloud to car, error!");
                mCloudState = CLOUD_STATE_STOPPED;
            }
        }
    };

    /*
    * thread for transact media from car to client
    * no ui debug state
    */
    private int mMediaCount = 0;
    Runnable runnable_media_uplink = new Runnable() {
        //upload media from wificar to cloud

        public void run()//this is main receive loop
        {
            int i;
            mMediaCount = 0;
            AppLog.i(TAG, "media uplink Thread ---> start running");
            do {
                try {
                    if (carMediaInputStream == null) {
                        /*it is important to stop media forward in case socket is not ready
                        * it also not forwarding media when carReady is false
                        * it will start media forward after mediaLoginResp is received
                        * */
                        Thread.sleep(2000);
                        continue;
                    }
                    i = carMediaInputStream.available();
                    if(i <= 0 || i >= MEDIA_BUF_LEN) {
                        if (i >= MEDIA_BUF_LEN) AppLog.i(TAG, "--->read dataInputStream loop");
                        continue;
                    }

                    carMediaInputStream.read(mediaUplinkBuffer);

                    if (USE_H264 > 0) {//uplink meida will be done until media codec finished
                        parseMedia(mediaUplinkBuffer, i);
                        continue;
                    }

                    AppLog.i(TAG, "media uplink Thread ---> read data from car len:" + i);
                } catch(Exception ioexception) {
                    AppLog.i(TAG, "media uplink--->read data from car ioexception!, just exit thread!");
                    break;
                }

                if (cloudMediaOutputStream == null /*|| block_uploadMedia*/) {
                    /*this means client is not ready * */
                    continue;
                }
                try {
                    byte tmp[] = arrayCopy(mediaUplinkBuffer, 0, i);
                    cloudMediaOutputStream.write(tmp);//use same uplink socket for both cmd & media
                    cloudMediaOutputStream.flush();
                    //AppLog.i(TAG, "media uplink Thread ---> write jpg data to cloud len:" + i);
                    mMediaCount ++;
                } catch(IOException ioexception) {
                    AppLog.i(TAG, "media uplink--->write data to cloud ioexception!, just exit thread!");
                    cloudMediaOutputStream = null;
                    carMediaInputStream = null;//make sure both uplink stream is closed to avoid cmd uplink exception when client app is closed
                    break;
                }
            } while(true);
        }
    };

    boolean mH264EncodeRunning = false;
    private MediaCodec mediaCodec;

    private Object mFrameSync = new Object();
    private CountDownLatch mNewFrameLatch;
    private int mGenerateIndex = 0;
    private static final int FRAME_RATE = 10;
    private int mTrackIndex;

    Runnable runnable_media_codec = new Runnable() {
        public void run()//this is main receive loop
        {
            mH264EncodeRunning = true;
            byte[] jpegData = null;
            int errorCounter = 0;

            while (mH264EncodeRunning) {
                if (YUVQueue.size() > 0) {
                    jpegData = YUVQueue.poll();

                    if (jpegData ==  null) {
                        synchronized (mFrameSync) {
                            mNewFrameLatch = new CountDownLatch(1);
                        }

                        try {
                            mNewFrameLatch.await();
                        } catch (InterruptedException e) {}

                        jpegData = YUVQueue.poll();
                    }

                    if (jpegData == null) continue;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, mBitmapOpt);

                    byte[] byteConvertFrame = getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);

                    //AppLog.i(TAG, "media codec Thread ---> w:" + bitmap.getWidth() + " h:" + bitmap.getHeight());

                    long TIMEOUT_USEC = 500000;
                    int inputBufIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                    long ptsUsec = computePresentationTime(mGenerateIndex, FRAME_RATE);
                    if (inputBufIndex >= 0) {
                        final ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufIndex);
                        inputBuffer.clear();
                        inputBuffer.put(byteConvertFrame);
                        mediaCodec.queueInputBuffer(inputBufIndex, 0, byteConvertFrame.length, ptsUsec, 0);
                        mGenerateIndex++;
                    }
                    MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
                    int encoderStatus = mediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                    if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        AppLog.e(TAG, "No output from encoder available");
                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        // not expected for an encoder
                        AppLog.e(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                    } else if (encoderStatus < 0) {
                        AppLog.e(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                    } else if (mBufferInfo.size != 0) {
                        ByteBuffer encodedData = mediaCodec.getOutputBuffer(encoderStatus);
                        if (encodedData == null) {
                            AppLog.e(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
                        } else {
                            encodedData.position(mBufferInfo.offset);
                            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                            //mediaMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);

                            //send data to media uplink stream
                            if (cloudMediaOutputStream == null) continue;
                            try {
                                byte[] buf = new byte[encodedData.remaining() + 36];
                                encodedData.get(buf, 36, encodedData.remaining());   //bb.get(b) is OK
                                buf[0] = 'M';buf[1] = 'O';buf[2] = '_';buf[3] = 'V';
                                buf[4] = 3;
                                cloudMediaOutputStream.write(buf);//use same uplink socket for both cmd & media
                                cloudMediaOutputStream.flush();
                                mMediaCount ++;
                                errorCounter = 0;
                                AppLog.i(TAG, "media codec Thread ---> write h264 data to cloud len:" + buf.length + " mMediaCount:" + mMediaCount);
                            } catch (IOException e) {
                                AppLog.e(TAG, "uplink output stream write excepiton! e: " + e.getMessage());
                                errorCounter++;
                                if (errorCounter > 20) break;
                            }
                            mediaCodec.releaseOutputBuffer(encoderStatus, false);
                        }
                    }
                }
            }//finish loop


            releaseCodec();
        }
    };

    private void releaseCodec() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
            AppLog.i(TAG,"RELEASE CODEC");
        }
    }

    static byte mediaRecvBuf[] = new byte[0];
    void parseMedia(byte[] buf, int i) {
        int k = findstr(buf, i, "MO_V");

        if (k  >= 0) {
            // Already have media bytes?
            if (mediaRecvBuf != null && mediaRecvBuf.length > 0) {

                //add to media bytes up through start of new
                //mediaRecvBuf += buf[0:k];
                mediaRecvBuf = arrayCopy2(mediaRecvBuf, 0, mediaRecvBuf.length,
                        buf, 0, k);

                // Both video and audio messages are time-stamped in 10ms units
                //timestamp = bytes_to_uint(media bytes, 23)

                if ((int)mediaRecvBuf[4] == 1) {//1-vedio;2-audio
                    //video bytes: call h264 codec processing routine
                    addToH264VideoQueue(arrayCopy(mediaRecvBuf, 36, mediaRecvBuf.length - 36), mediaRecvBuf.length - 36);
                    // Audio bytes: call processing routine
                } else if ((int)mediaRecvBuf[4] == 2){
                    addToH264AudioQueue(arrayCopy(mediaRecvBuf, 36, mediaRecvBuf.length - 36), mediaRecvBuf.length - 36);
                }
                //mediaRecvBuf = buf[k:]
                mediaRecvBuf = arrayCopy(buf, k, i/*buf.length*/ - k);

                // No media bytes yet: start with new bytes
            }else{
                //mediaRecvBuf = buf[k:]
                mediaRecvBuf = arrayCopy(buf, k, i/*buf.length*/ - k);
            }
        } else{
            //mediaRecvBuf += buf;
            //mediaRecvBuf should start with MO_V
            mediaRecvBuf = arrayCopy2(mediaRecvBuf, 0, mediaRecvBuf.length,
                    buf, 0, i/*buf.length*/);

        }
    }

    private static final int YUV_QUEUE_SIZE = 50;
    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(YUV_QUEUE_SIZE);

    //add mjpg data to h264 codec queue
    void addToH264VideoQueue(byte[] buf, int len) {

        if (YUVQueue.size() >= YUV_QUEUE_SIZE) {
            YUVQueue.poll();
        }
        YUVQueue.add(buf);
        synchronized (mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
                mNewFrameLatch.countDown();
            }
        }
    }

    //add audio data to h264 codec queue
    void addToH264AudioQueue(byte[] buf, int len) {

    }


    private static int findstr(byte[] buf, int bufLen, String str) {
        int strLen = str.length();
        if (strLen == 4) {
            byte[] str2byte = str.getBytes();

            for (int i = 0; i < bufLen -3; i++) {
                if (buf[i] == str2byte[0] && buf[i+1] == str2byte[1]
                        && buf[i+2] == str2byte[2] && buf[i+3] == str2byte[3]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static byte[] arrayCopy(byte[] src, int start, int len) {
        if (src == null || start < 0 || len <= 0 || start + len > src.length) {
            return null;
        }
        byte[] ret = new byte[len];
        System.arraycopy(src, start, ret, 0, len);
        return ret;
    }

    private static byte[] arrayCopy2(byte[] src1, int start1, int len1, byte[] src2, int start2, int len2) {
        if (src1 == null || start1 < 0 ) {
            return null;
        }

        if (src2 == null || start2 < 0 ) {
            return null;
        }
        if (len1 + len2 <= 0) {
            return null;
        }
        byte ret[] = new byte[len1 + len2];
        System.arraycopy(src1, start1, ret, 0, len1);
        System.arraycopy(src2, start2, ret, len1, len2);
        return ret;
    }


    /*
    * thread for transact media cmd(loginin )from client to car
    * no ui debug state
    */
    Runnable runnable_media_downlink = new Runnable() {
        public void run() {
            try {
                int len;
                AppLog.i(TAG, "media downlink Thread ---> start running");
                while ((len = cloudMediaInputStream.read(mediaDownlinkBuffer)) != -1) {

                    if (len == 0 || len > CMD_BUF_LEN) {//mediaDownlinkBuffer is short
                        continue;
                    }
                    //transact cmd data to car
                    byte tmp[] = arrayCopy(mediaDownlinkBuffer, 0, len);
                    carMediaOutputStream.write(tmp);//send to car
                    carMediaOutputStream.flush();
                    AppLog.i(TAG, "transact media data from cloud to car, len:" + len);
                }
            } catch (Exception e) {
                AppLog.e(TAG, "transact media cmd data from cloud to car, error!");
            } finally {
                AppLog.e(TAG, "transact media cmd data from cloud to car, error!");
            }
        }
    };

    public void requestWIFINetwork() {
        if (conMgr == null) return;
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        NetworkRequest build = builder.build();
        AppLog.i(TAG, "---> start request wifi network");
        conMgr.requestNetwork(build, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                AppLog.i(TAG, "---> request wifi OK! connectToCarCmd...");
                try {
                    connectToCarCmd(network);
                } catch (IOException e){
                    AppLog.e(TAG, "connectToCarCmd failed, e:" + e.getMessage());
                }
            }
        });
    }

    private Network mWifiNetwork;
    public int connectToCarCmd(Network network) throws IOException
    {
        AppLog.i(TAG, "connectToCarCmd, car state:" + stateDebugMsgs.get(mCarState));
        if (mCarState != CAR_STATE_WIFI_CONNECTED) {
            //AppLog.e(TAG, "car wifi state error, just return!");
        }
        if (carCmdSocket != null &&  carCmdSocket.isConnected()) {
            AppLog.i(TAG, "cmd uplink&downlink working, close it!");
        }
        try {
            if (carCmdSocket != null) {
                AppLog.i(TAG, "close previous car cmd socket...");
                carCmdSocket.close();
            }
        } catch (Exception e){
            AppLog.e(TAG, "clean up resource car cmd met error1.");
        }

        try {
            AppLog.i(TAG, "stop cmd uplink & downlink thread...");
            if (thread_cmd_uplink != null) {
                thread_cmd_uplink.interrupt();
                thread_cmd_uplink = null;
            }
            if (thread_cmd_downlink != null) {
                thread_cmd_downlink.interrupt();
                thread_cmd_downlink = null;
            }
            carCmdInputStream = null;
            carCmdOutputStream = null;
        } catch (Exception e){
            AppLog.e(TAG, "clean up resource car cmd met error3.");
        }

        carCmdSocket = SocketFactory.getDefault().createSocket();
        if (network != null) {
            mWifiNetwork = network;
            network.bindSocket(carCmdSocket);
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);

        carCmdSocket.connect(inetSocketAddress, 5000);
        AppLog.i(TAG, "car cmd Socket ---> connect success:" + CAR_HOST_ADDR + " port:" + CAR_PORT);
        carCmdSocket.setSendBufferSize(CMD_BUF_LEN);
        if(!carCmdSocket.isConnected()){
            AppLog.e(TAG, "--->car cmd socket init failed!");
            throw new IOException();
        }

        carCmdOutputStream = new DataOutputStream(carCmdSocket.getOutputStream());//downlink
        carCmdInputStream = new DataInputStream(carCmdSocket.getInputStream());//uplink

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

        AppLog.i(TAG, "car cmd Socket create successful!");
        mCarState = CAR_STATE_SOCKET_CONNECTED;

        return 0;
    }


    public void disConnectCarCmd() {//not use
        if (thread_cmd_uplink != null) {
            thread_cmd_uplink.interrupt();
            thread_cmd_uplink = null;
        }

        if (thread_media_uplink != null) {
            thread_media_uplink.interrupt();
            thread_media_uplink= null;
        }
        mCarState = CAR_STATE_INIT;
    }

    //invoke after cloud media socket accepted
    public boolean ConnectToCarMedia( ) throws IOException {
        AppLog.i(TAG, "ConnectToCarMedia, car state:" + stateDebugMsgs.get(mCarState));
        try {
            AppLog.e(TAG, "clean up previous car media socket.");
            if (carMediaSocket!= null) {
                carMediaSocket.close();
            }
        } catch (Exception e){
            AppLog.e(TAG, "clean up resource car media met error1.");
        }

        try {
            if (thread_media_uplink != null ){
                thread_media_uplink.interrupt();
                thread_media_uplink = null;
            }
            if (thread_media_downlink != null ){
                thread_media_downlink.interrupt();
                thread_media_downlink = null;
            }

            if (thread_media_codec != null ){
                thread_media_codec.interrupt();
                thread_media_codec = null;
            }

            carMediaInputStream = null;
            carMediaOutputStream = null;
        } catch (Exception e){
            AppLog.e(TAG, "clean up resource car media met error2.");
        }

        AppLog.i(TAG, "car media socket connecting .....");
        carMediaSocket = SocketFactory.getDefault().createSocket();
        InetSocketAddress addr = new InetSocketAddress(CAR_HOST_ADDR, CAR_PORT);

        //use previous cached wifi network
        if (mWifiNetwork != null) mWifiNetwork.bindSocket(carMediaSocket);
        carMediaSocket.connect(addr, 5000);

        if(!carMediaSocket.isConnected()){
            AppLog.i(TAG, "--->media socket connect failed!");
            throw new IOException();
        }
        AppLog.i(TAG, "car media socket connected!");

        carMediaInputStream = new DataInputStream(carMediaSocket.getInputStream());

        //just for send media login req, this is important, as we ignor LoginReq from client
        carMediaOutputStream = new DataOutputStream(carMediaSocket.getOutputStream());

        if (thread_media_uplink == null) {
            thread_media_uplink = new Thread(runnable_media_uplink);
            thread_media_uplink.setName("media_uplink Thread");
            thread_media_uplink.start();
        }

        if (thread_media_downlink == null) {
            thread_media_downlink = new Thread(runnable_media_downlink);
            thread_media_downlink.setName("media_downlink Thread");
            thread_media_downlink.start();
        }

        initMediaCodec();
        if (thread_media_codec == null) {
            thread_media_codec = new Thread(runnable_media_codec);
            thread_media_codec.setName("media_codec Thread");
            thread_media_codec.start();
        }
        return true;
    }

    //invoke after ipv6 check OK
    int mCmdDownLinkCount = 0;
    private static int CLIENT_EXIT = 100;
    private Socket clientSocket;
    public boolean createServerSocket() {

        try {
            cloudCmdSocket = new ServerSocket(CMD_PORT);
            cloudMediaSocket = new ServerSocket(MEDIA_PORT);
            mCloudState = CLOUD_STATE_SERVER_OK;
            AppLog.i(TAG, "server socket created! start Listening thread ....");
            Thread cmdThread = new Thread(new Runnable() {
                public void run()//cmd main receive loop
                {
                    int ret;
                    try {
                        while (true) {
                            //just waiting for client cmd socket connect
                            AppLog.i(TAG, "waiting for client cmd connection ....");
                            final Socket socket = cloudCmdSocket.accept();

                            final String address = socket.getRemoteSocketAddress().toString();
                            AppLog.i(TAG, "one client cmd connected, address:" + address);

                            cloudCmdInputStream = new DataInputStream(socket.getInputStream());
                            cloudCmdOutputStream = new DataOutputStream(socket.getOutputStream());

                            requestWIFINetwork();
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
                    //just waiting for proxy cmd socket connect
                    try {
                        while (true) {
                            AppLog.i(TAG, "waiting for client media connection ....");
                            final Socket socket = cloudMediaSocket.accept();
                            //print proxy media socket addr & port
                            final String address = socket.getRemoteSocketAddress().toString();
                            AppLog.i(TAG, "one client media connected, address:" + address);

                            cloudMediaOutputStream = new DataOutputStream(socket.getOutputStream());
                            cloudMediaInputStream = new DataInputStream(socket.getInputStream());

                            //no need to request wifi network, just use cached wifi network;
                            ConnectToCarMedia();
                        }
                    } catch (IOException e) {
                        AppLog.e(TAG, "Listen cloud media server socket Exception! e:" + e.getMessage());
                    }
                }
            });
            mediaThread.setName("mediaThread");
            mediaThread.start();
            mCloudState = CLOUD_STATE_SERVER_OK;
        } catch (IOException e) {
            if (e.getMessage().contains("EADDRINUSE")) {
                AppLog.i(TAG, "server socket existed, just use them");
                mCloudState = CLOUD_STATE_SERVER_OK;
                return true;
            }
            AppLog.e(TAG, "create server socket failed!");
            return false;
        }
        mCloudState = CLOUD_STATE_SERVER_OK;
        return true;
    }

    //no use
    public boolean disConnectCar() {

        //cmd
        if (carCmdSocket != null) {
            AppLog.i(TAG, "close previous car cmd socket...");
            try {
                carCmdSocket.close();
            } catch (Exception e) {
                AppLog.e(TAG, "close socket error!");
            }
        }
        try {
            if (carCmdInputStream != null) carCmdInputStream = null;
            if (carCmdOutputStream != null) carCmdOutputStream = null;
        }catch(Exception e){
            AppLog.e(TAG, "close socket error!");
        }

        try {
            thread_cmd_uplink.interrupt();
        } catch (Exception e){
            AppLog.e(TAG, "close socket error!");
        }


        //media
        if (carMediaSocket != null) {
            AppLog.i(TAG, "close previous car cmd socket...");
            try {
                carMediaSocket.close();
            } catch (Exception e) {
                AppLog.e(TAG, "close socket error!");
            }
        }
        try {
            if (carMediaInputStream != null) carMediaInputStream = null;
            if (carMediaOutputStream != null) carMediaOutputStream = null;
        }catch(Exception e){
            AppLog.e(TAG, "close socket error!");
        }
        try {
            thread_media_uplink.interrupt();
        } catch (Exception e){
            AppLog.e(TAG, "close socket error!");
        }
        return true;
    }

    public String printDebugMsg() {
        StringBuilder sb = new StringBuilder();
        sb.append("CAR STATUS :").append(stateDebugMsgs.get(mCarState)).append("-").append(mCarState).append("\r\n");
        sb.append("\r\n");
        
        sb.append("CLOUD STATUS :").append(stateDebugMsgs.get(mCloudState)).append("-").append(mCloudState).append("\r\n");
        sb.append("\r\n");

        sb.append("CMD:[UP]").append(mCmdUpLinkCount).append("   [DOWN]").append(mCmdDownLinkCount)
                .append("   MEDIA:").append(mMediaCount).append("\r\n");
        sb.append("\r\n");
        return sb.toString();
    }

    public String printIpInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("LOCAL IP:").append(mLocalIpV6Address).append("\r\n");
        sb.append("PUBLIC IP:").append(mExternalIpV6Address).append("\r\n");

        return sb.toString();
    }
    public boolean matchWifiCarAddr(String ip) {
        return CAR_HOST_ADDR.equals(ip);
    }

    public void setCarReady (boolean ready) {
        mCarState = ready?CAR_STATE_WIFI_CONNECTED:CAR_STATE_INIT;
    }

    ConnectivityManager conMgr = null;
    public void makeWifiReady(Network network, ConnectivityManager cm) {
        conMgr = cm;
    }
    private String mLocalIpV6Address;
    private String mExternalIpV6Address;
    public void makeCloudReady(Network network, ConnectivityManager cm) {
        //1. get local ip from Network
        //2. get external ip from dyndns
        //3. if same; upload ip to redis
        //4. create server socket
        mCloudState = CLOUD_STATE_CELL_OK;
        try {
            LinkProperties prop = cm.getLinkProperties(network);
            if (prop == null) {
                return;
            }

            String interfaceName = prop.getInterfaceName();
            NetworkInterface networkInterface = null;
            mLocalIpV6Address = "";
            try {
                networkInterface = NetworkInterface.getByName(interfaceName);
            } catch (Exception e) {

            }
            if (networkInterface == null) {
                return;
            }
            Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
            while (enumIpAddr.hasMoreElements()) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (inetAddress instanceof Inet6Address && !inetAddress.isLoopbackAddress()
                        && !inetAddress.isLinkLocalAddress()) {

                    mLocalIpV6Address = inetAddress.getHostAddress();
                    AppLog.i(TAG, "getLocalIPv6, IP Address:" + mLocalIpV6Address);
                }
            }

            //start to get cloud ipv6
            byte[] buf = new byte[64];
            URL url = null;
            try {

                url = new URL("http://6.ipw.cn") ;
                HttpURLConnection conn = (HttpURLConnection) network.openConnection(url);
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() != 200) {
                    AppLog.e(TAG, "getLocalIPv6, 6.ipw.cn error! ResponseCode:" + conn.getResponseCode());
                }
                InputStream inStream = conn.getInputStream();

                inStream.read(buf);
            } catch (IOException e) {
                AppLog.e(TAG, "getExternalIPv6, 6.ipw.cn error! e:" + e.getMessage());
            }

            if( buf.length > 12 ){
                mExternalIpV6Address = new String(buf);
                //if (mExternalIpV6Address.equals(mLocalIpV6Address)) {
                    //this means ip match, now create server socket

                uploadIp(network);
                mCloudState = CLOUD_STATE_IP_OK;
                createServerSocket();
                //}
            }
            mainUI.updateUI(mainUI.VIEW_ID_MOBILE_NETWORK, false);
        } catch (Exception e) {
            AppLog.e(TAG, "check local & external ip, error!");
            mainUI.updateUI(mainUI.VIEW_ID_MOBILE_NETWORK, false);
            mCloudState = CLOUD_STATE_CELL_OK;
        }
    }

    public void uploadIp(Network network) {
        AppLog.i(TAG, "upload ip to cloud ...");
        try {
            String data = "mac=112233&time=" + System.currentTimeMillis()
                    + "&ipaddr=" + mLocalIpV6Address;

            InetAddress addr = network.getByName("i4free.top");
            String sAddr = addr.toString();
            sAddr = sAddr.substring(sAddr.indexOf("/")+1);
            URL url =new URL("http://i4free.top:38086/wificar/regClient");
            HttpURLConnection connection = (HttpURLConnection)network.openConnection(url);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            connection.setUseCaches(false);
            connection.connect();

            connection.getOutputStream().write(data.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            if (connection.getResponseCode() == 200) {
                mExternalIpV6Address = "OK------->";
                InputStream input = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
                String result = bufferedReader.readLine();
                input.close();
                AppLog.i(TAG, "upload local IpV6 to i4free, success!");
            }
            mainUI.updateUI(mainUI.VIEW_ID_MOBILE_NETWORK, false);
        } catch (IOException iOException) {
            AppLog.e(TAG, "uploadIp, failed! e:" + iOException.getMessage());
        }
    }

    private byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
        int[] argb = new int[inputWidth * inputHeight];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        scaled.recycle();
        return yuv;
    }

    private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }
                index++;
            }
        }
    }

    private long computePresentationTime(long frameIndex, int framerate) {
        return 132 + frameIndex * 1000000 / framerate;
    }

    private MediaCodecInfo selectCodec(String mimeType) {
        MediaCodecList list = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] infos = list.getCodecInfos();

        for (MediaCodecInfo codecInfo :infos) {
            if (codecInfo == null || !codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private static int selectColorFormat(MediaCodecInfo codecInfo,
                                         String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo
                .getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        return 0; // not reached
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }
}
