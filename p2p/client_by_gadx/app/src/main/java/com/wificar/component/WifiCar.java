package com.wificar.component;

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
import android.util.Log;
import android.widget.Toast;
import com.wificar.WificarActivity;
import com.wificar.component.CommandEncoder;
import com.wificar.surface.CameraSurfaceView;
import com.wificar.util.AppLog;
import com.wificar.util.ByteUtility;
import com.wificar.util.MessageUtility;
import com.wificar.util.NetworkUtility;
import com.wificar.util.TimeUtility;
import com.wificar.util.WificarUtility;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.SocketFactory;

/* loaded from: classes.dex */
public class WifiCar {
    public static final int LEFT_WHEEL = 0;
    public static final int MAX_SPEED = 10;
    public static final int MESSAGE_DISCONNECTED = 8904;
    public static final int RIGHT_WHEEL = 1;
    private static final int SAMPLE_RATE = 8000;
    private static final int iHeaderLen = 23;
    private boolean First;
    private boolean First3;
    private boolean First4;
    private boolean First5;
    private boolean First6;
    private boolean First7;
    private TimerTask KeepAliveTask;
    private int PTimes;
    private int[] PowerCount;
    private int PowerTimes;
    private AudioData aData;
    private AudioComponent audio;
    private int audioFlag;
    private int audioIndex;
    byte[] bBufCamera;
    private boolean bConnected;
    public boolean bIr;
    private boolean bSocketState;
    private float battery;
    private int batteryCount;
    private int batteryCountStop;
    private float batteryStop;
    private TimerTask batteryTask;
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
    private VideoComponent flim;
    Handler handlerGUI;
    int iTimeout;
    private byte[] initialAudioData;
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
    int nc1;
    int nc2;
    int nc3;
    int nc4;
    private int number1Count;
    private String photofilename;
    Timer playTimer;
    Socket receiverMediaSocket;
    long recordTimeLength;
    Timer recordTimer;
    Socket senderMediaSocket;
    CameraSurfaceView sfhGUI;
    private Timer showP;
    Socket socket;
    private long soundDelayTime;
    private long startRecordTime;
    long startRecordTimeStamp;
    String targetCameraVer;
    String targetDevID;
    String targetHost;
    String targetId;
    String targetPassword;
    int targetPort;
    int v1;
    int v2;
    int v3;
    int v4;
    private VideoData vData;
    public static int videoWidth = 320;
    public static int videoHeight = 240;
    private static int[] step_table = {7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};
    private static int[] index_adjust = {-1, -1, -1, -1, 2, 4, 6, 8};

    public void takePicture(Activity activity) throws ParseException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        byte[] bArrayImage = this.vData.getData();
        Bitmap currentBitmap = BitmapFactory.decodeByteArray(bArrayImage, 0, bArrayImage.length, opt);
        Bitmap currentBitmap2 = WificarActivity.getInstance().createBitMap(currentBitmap);
        this.photofilename = TimeUtility.getCurrentTimeStr();
        String photopath = String.valueOf(WificarActivity.getInstance().ReadSDPath()) + "/Brookstone/Pictures";
        File file = new File(photopath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(String.valueOf(WificarActivity.getInstance().ReadSDPath()) + "/Brookstone/Pictures/" + this.photofilename + ".jpg");
        try {
            f.createNewFile();
        } catch (IOException e) {
        }
        FileOutputStream fOut = null;
        try {
            FileOutputStream fOut2 = new FileOutputStream(f);
            fOut = fOut2;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
        currentBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        try {
            fOut.close();
            String szUrl = String.valueOf(WificarActivity.getInstance().ReadSDPath()) + "/Brookstone/Pictures/" + this.photofilename + ".jpg";
            try {
                Uri.parse(szUrl);
                String img_path = String.valueOf(WificarActivity.getInstance().ReadSDPath()) + "/Brookstone/Pictures/" + this.photofilename + ".jpg";
                Uri uri = Uri.parse("file://" + img_path);
                activity.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", uri));
                if (WificarActivity.getInstance().isplay_pictrue != 1) {
                    Toast pictureOK = Toast.makeText(activity, MessageUtility.MESSAGE_TAKE_PHOTO_SUCCESSFULLY, Toast.LENGTH_SHORT);
                    pictureOK.show();
                }
            } catch (Exception e4) {
                Toast pictureFAIL = Toast.makeText(activity, MessageUtility.MESSAGE_TAKE_PHOTO_FAIL, Toast.LENGTH_SHORT);
                pictureFAIL.show();
                e4.printStackTrace();
            }
        } catch (IOException e5) {
            e5.printStackTrace();
        }
        if (this.carStateMode == 1) {
            AppLog.d("recordaction", "stopAudio:" + System.currentTimeMillis());
        }
    }

    public void startBatteryTask() {
        AppLog.d("wild1", "--->startBatteryTask");
        this.batteryTimer.schedule(this.batteryTask, 1000L, 5000L);
    }

    public void startKeepAliveTask() {
        AppLog.d("keep", "startKeepAliveTask");
        this.keepAliveTimer.schedule(this.KeepAliveTask, 1000L, 30000L);
    }

    public int getMoveFlagCount() {
        return this.moveFlagCount;
    }

    public void decreaseMoveFlagCount() {
        AppLog.d("move", "decreaseMoveFlagCount:" + this.moveFlagCount);
        this.moveFlagCount--;
    }

    public void enableMoveFlag() {
        this.move = true;
        this.moveFlagCount = this.moveFlagMaxCount;
        this.moveFlag = 1;
        disableAudioFlag();
    }

    public void disableAudioFlag() {
        this.audioFlag = 0;
    }

    public void enableAudioFlag() {
        this.audioFlag = 1;
    }

    public int getAudioFlag() {
        return this.audioFlag;
    }

    public void disableMoveFlag() {
        this.move = false;
        this.batteryStop = 0.0f;
        this.battery_valueStop = 0;
        this.batteryCountStop = 0;
        this.moveFlag = 0;
    }

    public boolean isRecording() {
        return this.carStateMode == 1;
    }

    public boolean isPlaying() {
        return this.carStateMode == 2;
    }

    public boolean isChange() {
        return this.changeFlag;
    }

    public void change() {
        this.changeFlag = true;
    }

    public void updatedChange() {
        this.changeFlag = false;
    }

    public boolean setConnect() {
        try {
            connectCommand();
            this.bConnected = true;
            return true;
        } catch (IOException e) {
            AppLog.d("wificar", "--->setConnect IOException:" + e);
            e.printStackTrace();
            return false;
        }
    }

    public void setDisconnect() {
        if (this.bConnected) {
            disconnect();
            this.bConnected = false;
        }
    }

    public void checkConnection() {
        if (this.bConnected) {
            try {
                connectCommand();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean disconnect() {
        try {
            AppLog.d("wild0", "disconnect");
            this.bSocketState = false;
            this.socket.close();
            this.receiverMediaSocket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int isConnected() {
        try {
            if (this.socket == null) {
                return 0;
            }
            return this.socket.isConnected() ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getChallenge(int i) {
        if (i == 0) {
            return this.v1;
        }
        if (i == 1) {
            return this.v2;
        }
        if (i == 2) {
            return this.v3;
        }
        if (i == 3) {
            return this.v4;
        }
        return 0;
    }

    public void setChallengeReverse(int i, int value) {
        if (i == 0) {
            this.nc1 = value;
        } else if (i == 1) {
            this.nc2 = value;
        } else if (i == 2) {
            this.nc3 = value;
        } else if (i == 3) {
            this.nc4 = value;
        }
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFilewareVersion() {
        return this.deviceId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public int getMoveFlag() {
        return this.moveFlag;
    }

    public String getCameraId() {
        return this.cameraId;
    }

    public String getKey() {
        return String.valueOf(this.targetId) + ":" + this.cameraId + "-save-private:" + this.targetPassword;
    }

    public WifiCar(Activity inActivity) {
        this(inActivity, -1, -1, -1, -1);
    }

    public WifiCar(Activity inActivity, int v1, int v2, int v3, int v4) {
        this.PowerCount = new int[10];
        this.PowerTimes = 0;
        this.PTimes = 0;
        this.First = true;
        this.First3 = false;
        this.First4 = false;
        this.First5 = false;
        this.First6 = false;
        this.First7 = false;
        this.number1Count = 0;
        this.showP = new Timer(true);
        this.battery = 0.0f;
        this.batteryStop = 0.0f;
        this.battery_value = 0;
        this.battery_valueStop = 0;
        this.batteryCount = 0;
        this.batteryCountStop = 0;
        this.bSocketState = false;
        this.batteryTimer = new Timer("battery timer");
        this.batteryTask = new TimerTask() { // from class: com.wificar.component.WifiCar.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                try {
                    byte[] data = CommandEncoder.cmdFetchBatteryPowerReq();
                    WifiCar.this.dataOutputStream.write(data);
                    WifiCar.this.dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        this.keepAliveTimer = new Timer("keep alive");
        this.KeepAliveTask = new TimerTask() { // from class: com.wificar.component.WifiCar.2
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                try {
                    AppLog.d("keep", "startKeepAliveTask:" + System.currentTimeMillis());
                    byte[] data = CommandEncoder.cmdKeepAlive();
                    WifiCar.this.dataOutputStream.write(data);
                    WifiCar.this.dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        this.instance = null;
        this.audio = null;
        this.flim = null;
        this.changeFlag = true;
        this.lastAudioDataTime = 0;
        this.audioIndex = 0;
        this.moveFlag = 0;
        this.moveFlagCount = 0;
        this.moveFlagMaxCount = 50;
        this.audioFlag = 1;
        this.move = false;
        this.vData = null;
        this.aData = null;
        this.lastMoveCurrentTime = System.currentTimeMillis();
        this.soundDelayTime = 1500L;
        this.targetHost = "192.168.1.100";
        this.targetPort = 28000;
        this.targetId = "AC13";
        this.targetPassword = "AC13";
        this.targetCameraVer = "";
        this.targetDevID = "";
        this.bConnected = false;
        this.iTimeout = 5000;
        this.handlerGUI = null;
        this.sfhGUI = null;
        this.bBufCamera = null;
        this.bIr = false;
        this.mainUI = null;
        this.lLastCmdTimeStamp = 0L;
        this.socket = null;
        this.dataOutputStream = null;
        this.dataInputStream = null;
        this.receiverMediaSocket = null;
        this.senderMediaSocket = null;
        this.mediaReceiverOutputStream = null;
        this.mediaReceiverInputStream = null;
        this.mediaSenderOutputStream = null;
        this.mediaSenderInputStream = null;
        this.v1 = 0;
        this.v2 = 0;
        this.v3 = 0;
        this.v4 = 0;
        this.nc1 = 0;
        this.nc2 = 0;
        this.nc3 = 0;
        this.nc4 = 0;
        this.cameraId = "";
        this.deviceId = "";
        this.carStateMode = 0;
        this.recordTimer = null;
        this.playTimer = null;
        this.startRecordTimeStamp = 0L;
        this.recordTimeLength = 0L;
        this.instance = this;
        this.audio = new AudioComponent(this);
        this.flim = new VideoComponent(this);
        this.mainUI = inActivity;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.targetHost = WificarUtility.getStringVariable(inActivity, WificarUtility.ACCOUNT_HOST, this.targetHost);
        this.targetPort = WificarUtility.getIntVariable(inActivity, WificarUtility.ACCOUNT_PORT, this.targetPort);
        this.targetId = WificarUtility.getStringVariable(inActivity, WificarUtility.ACCOUNT_ID, this.targetId);
        this.targetPassword = WificarUtility.getStringVariable(inActivity, WificarUtility.ACCOUNT_PASSWORD, this.targetPassword);
        AppLog.d("wificar", "new WifiCar");
    }

    public void setGUIHandler(Handler inHandler) {
        this.handlerGUI = inHandler;
    }

    public void setSurfaceView(CameraSurfaceView surfaceView) {
        this.sfhGUI = surfaceView;
    }

    public void setVideoBitmapBytes(VideoData vData) {
        this.sfhGUI.setCameraBytes(vData);
    }

    public static void setVideoFolder(Context context, String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        WificarUtility.putStringVariable(context, WificarUtility.VIDEO_FOLDER, path);
    }

    public static String getVideoFolder(Context context) {
        return WificarUtility.getStringVariable(context, WificarUtility.VIDEO_FOLDER, Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public Boolean setHost(String host) {
        this.targetHost = host;
        WificarUtility.putStringVariable(this.mainUI, WificarUtility.ACCOUNT_HOST, host);
        return true;
    }

    public String getHost() {
        return WificarUtility.getStringVariable(this.mainUI, WificarUtility.ACCOUNT_HOST, this.targetHost);
    }

    public Boolean setPort(int port) {
        this.targetPort = port;
        WificarUtility.putIntVariable(this.mainUI, WificarUtility.ACCOUNT_PORT, port);
        return true;
    }

    public int getPort() {
        return WificarUtility.getIntVariable(this.mainUI, WificarUtility.ACCOUNT_PORT, this.targetPort);
    }

    public Boolean setId(String id) {
        this.targetId = id;
        WificarUtility.putStringVariable(this.mainUI, WificarUtility.ACCOUNT_ID, id);
        return true;
    }

    public String getId() {
        return WificarUtility.getStringVariable(this.mainUI, WificarUtility.ACCOUNT_ID, this.targetId);
    }

    public Boolean setPassword(String pw) {
        this.targetPassword = pw;
        WificarUtility.putStringVariable(this.mainUI, WificarUtility.ACCOUNT_PASSWORD, pw);
        return true;
    }

    public String getPassword() {
        return WificarUtility.getStringVariable(this.mainUI, WificarUtility.ACCOUNT_PASSWORD, this.targetPassword);
    }

    public static String getVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public String getDevID() {
        return this.targetDevID;
    }

    public String getSSID() throws IOException {
        String ssid = NetworkUtility.getURLContent("http://i4free.top:38086/wificar/getClientIp");
        this.targetHost = ssid;
        return ssid;
    }

    public byte[] getBufCamera() {
        return this.bBufCamera;
    }

    private void updateData() {
    }

    public synchronized void connectMediaReceiver(int linkId) throws IOException {
        Socket s = createMediaReceiverSocket(this.targetHost, 28001);
        this.mediaReceiverOutputStream = new DataOutputStream(s.getOutputStream());
        this.mediaReceiverInputStream = new DataInputStream(s.getInputStream());
        byte[] login = CommandEncoder.cmdMediaLoginReq(linkId);
        this.mediaReceiverOutputStream.write(login);
        this.mediaReceiverOutputStream.flush();
        Runnable thread = new Runnable() { // from class: com.wificar.component.WifiCar.3
            @Override // java.lang.Runnable
            public void run() {
                ByteArrayOutputStream bufInput = new ByteArrayOutputStream();
                bufInput.reset();

                while (WifiCar.this.bSocketState) {
                    try {
                        if (8192 > 0) {
                            int iReadLen = mediaReceiverInputStream.available();
                            if(iReadLen <= 0 || iReadLen > 8192) {
                                continue;
                            }

                            byte[] b = new byte[8192];
                            WifiCar.this.mediaReceiverInputStream.read(b, 0, iReadLen);

                            //bufInput.write(b, 0, iReadLen);
                            Log.e("media", "receive media data len:" + iReadLen);
                            CommandEncoder.parseMediaCommand(WifiCar.this.instance, b, iReadLen);
                        }
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e2) {
                        WificarActivity.getInstance().reStartConnect();
                        e2.printStackTrace();
                        return;
                    }
                }
            }
        };
        Thread rec = new Thread(thread);
        rec.setName("Media Thread");
        rec.start();
    }

    //no use
    public synchronized void connectMediaSender(int linkId) throws IOException {
        Socket s = createMediaSenderSocket(this.targetHost, this.targetPort);
        this.mediaSenderOutputStream = new DataOutputStream(s.getOutputStream());
        this.mediaSenderInputStream = new DataInputStream(s.getInputStream());
        byte[] login = CommandEncoder.cmdMediaLoginReq(linkId);
        this.mediaSenderOutputStream.write(login);

        Runnable thread = new Runnable() { // from class: com.wificar.component.WifiCar.4
            @Override // java.lang.Runnable
            public void run() {
                ByteArrayOutputStream bufInput = new ByteArrayOutputStream();
                bufInput.reset();
                while (WifiCar.this.bSocketState) {
                    try {
                        if (8192 > 0) {
                            byte[] b = new byte[8192];
                            int iReadLen = WifiCar.this.mediaSenderInputStream.read(b, 0, 8192);
                            bufInput.write(b, 0, iReadLen);
                            CommandEncoder.parseMediaCommand(WifiCar.this.instance, b, WifiCar.this.mediaSenderInputStream.available());
                        }
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        return;
                    }
                }
            }
        };
        Thread send = new Thread(thread);
        send.setName("Media Send Thread");
        send.start();
    }

    public synchronized void startRecordTrack() throws IOException {
        byte[] cmd = CommandEncoder.cmdDeviceControlReq(6, 1);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
    }

    public synchronized void stopRecordTrack() throws IOException {
        byte[] cmd = CommandEncoder.cmdDeviceControlReq(6, 0);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
    }

    public synchronized void startPlayTrack() throws IOException {
        byte[] cmd = CommandEncoder.cmdDeviceControlReq(7, 1);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
    }

    public synchronized void stopPlayTrack() throws IOException {
        byte[] cmd = CommandEncoder.cmdDeviceControlReq(7, 0);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
    }

    public synchronized void verifyCommand() throws IOException {
        byte[] cmd = CommandEncoder.cmdVerifyReq(getKey(), this.nc1, this.nc2, this.nc3, this.nc4);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        startBatteryTask();
        startKeepAliveTask();
    }

    public synchronized void connectCommand() throws IOException {
        WificarActivity.getInstance().sendMessage(WificarActivity.MESSAGE_CONNECT_TO_CAR);
        Socket s = createCommandSocket(getSSID(), this.targetPort);
        AppLog.i("zwt", "--->Socket:" + s);
        if (!s.isConnected()) {
            AppLog.i("", "--->s.isConnected()");
            throw new IOException();
        }
        this.dataOutputStream = new DataOutputStream(s.getOutputStream());
        this.dataInputStream = new DataInputStream(s.getInputStream());
        byte[] login = CommandEncoder.cmdLoginReq(this.v1, this.v2, this.v3, this.v4);
        this.dataOutputStream.write(login);
        this.dataOutputStream.flush();
        Runnable thread = new Runnable() { // from class: com.wificar.component.WifiCar.5
            @Override // java.lang.Runnable
            public void run() {
                ByteArrayOutputStream bufInput = new ByteArrayOutputStream();
                try {
                    AppLog.i("", "--->bSocketState:" + WifiCar.this.bSocketState);
                    while (WifiCar.this.bSocketState) {
                        int iLimit = WifiCar.this.dataInputStream.available();
                        if (iLimit > 0) {
                            byte[] b = new byte[iLimit];
                            int iReadLen = WifiCar.this.dataInputStream.read(b, 0, iLimit);
                            bufInput.reset();
                            bufInput.write(b, 0, iReadLen);
                            bufInput = CommandEncoder.parseCommand(WifiCar.this.instance, bufInput);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    AppLog.i("", "--->IOException:" + e + " bConnected:" + WifiCar.this.bConnected);
                    if (WifiCar.this.bConnected) {
                        try {
                            WifiCar.this.instance.connectCommand();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        };
        Thread rec = new Thread(thread);
        rec.setName("Command Thread");
        rec.start();
    }

    private void enableCommandInputStream(DataInputStream in) {
        new Runnable() { // from class: com.wificar.component.WifiCar.6
            @Override // java.lang.Runnable
            public void run() {
                ByteArrayOutputStream bufInput = new ByteArrayOutputStream();
                if (System.currentTimeMillis() - WifiCar.this.lLastCmdTimeStamp > 60000) {
                    try {
                        byte[] keepAlive = CommandEncoder.cmdKeepAlive();
                        WifiCar.this.dataOutputStream.write(keepAlive);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                while (true) {
                    try {
                        int iLimit = WifiCar.this.dataInputStream.available();
                        if (iLimit > 0) {
                            byte[] b = new byte[iLimit];
                            int iReadLen = WifiCar.this.dataInputStream.read(b, 0, iLimit);
                            bufInput.write(b, 0, iReadLen);
                            CommandEncoder.parseCommand(WifiCar.this.instance, bufInput);
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        return;
                    }
                }
            }
        };
    }

    private Socket createCommandSocket(String targetHost, int targetPort) throws IOException {
        this.socket = SocketFactory.getDefault().createSocket();
        SocketAddress remoteAddr = new InetSocketAddress(getSSID(), targetPort);
        this.socket.connect(remoteAddr, 5000);
        this.bSocketState = true;
        return this.socket;
    }

    private Socket createMediaReceiverSocket(String targetHost, int targetPort) throws IOException {
        this.receiverMediaSocket = SocketFactory.getDefault().createSocket();
        SocketAddress remoteAddr = new InetSocketAddress(targetHost, targetPort);
        this.receiverMediaSocket.connect(remoteAddr, 5000);
        this.bSocketState = true;
        return this.receiverMediaSocket;
    }

    private Socket createMediaSenderSocket(String targetHost, int targetPort) throws IOException {
        this.senderMediaSocket = SocketFactory.getDefault().createSocket();
        SocketAddress remoteAddr = new InetSocketAddress(targetHost, targetPort);
        this.senderMediaSocket.connect(remoteAddr, 5000);
        this.bSocketState = true;
        return this.senderMediaSocket;
    }

    public boolean enableVideo() throws IOException {
        WificarActivity.getInstance().sendMessage(WificarActivity.MESSAGE_CONNECT_TO_CAR_SUCCESS);
        byte[] cmd = CommandEncoder.cmdVideoStartReq();
        return true;
        /*this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        return true;*/
    }

    public boolean disableVideo() throws IOException {
        if (!this.bConnected) {
            return false;
        }
        byte[] cmd = CommandEncoder.cmdVideoEnd();
        this.dataOutputStream.write(cmd);
        return true;
    }

    public synchronized void led_onTrack() throws IOException {
        byte[] cmd = CommandEncoder.cmdDeviceControlReq(8, 0);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
    }

    public synchronized void led_offTrack() throws IOException {
        byte[] cmd = CommandEncoder.cmdDeviceControlReq(9, 0);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
    }

    public boolean cameraup() throws IOException {
        if (this.bConnected) {
            this.moveFlag = 1;
            byte[] cmd = CommandEncoder.cmdDecoderControlReq(0);
            this.dataOutputStream.write(cmd);
            this.dataOutputStream.flush();
            return true;
        }
        return false;
    }

    public boolean cameradown() throws IOException {
        if (!this.bConnected) {
            return false;
        }
        this.moveFlag = 1;
        byte[] cmd = CommandEncoder.cmdDecoderControlReq(2);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        return true;
    }

    public boolean camerastop() throws IOException {
        if (this.bConnected) {
            this.moveFlag = 0;
            byte[] cmd = CommandEncoder.cmdDecoderControlReq(1);
            this.dataOutputStream.write(cmd);
            this.dataOutputStream.flush();
            return true;
        }
        return false;
    }

    public boolean enableRecordAudio(int time) throws IOException {
        AppLog.d("audio", "audioEnable");
        this.audio.startRecord();
        if (!this.bConnected) {
            return false;
        }
        byte[] cmd = CommandEncoder.cmdTalkStartReq(time);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        return true;
    }

    public boolean enableAudio() throws IOException {
        AppLog.d("wild0", "audioEnable");
        if (!this.bConnected) {
            return false;
        }
        byte[] cmd = CommandEncoder.cmdAudioStartReq();
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        return true;
    }

    public boolean disableRecordAudio() throws IOException {
        stopRecordAudio();
        if (!this.bConnected) {
            return false;
        }
        byte[] cmd = CommandEncoder.cmdTalkEnd();
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        return true;
    }

    public boolean disableAudio() throws IOException {
        if (!this.bConnected) {
            return false;
        }
        stopAudio();
        return true;
    }

    public boolean enableIR() throws IOException {
        if (!this.bConnected) {
            return false;
        }
        byte[] cmd = CommandEncoder.cmdDecoderControlReq(94);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        return true;
    }

    public boolean disableIR() throws IOException {
        if (!this.bConnected) {
            return false;
        }
        byte[] cmd = CommandEncoder.cmdDecoderControlReq(95);
        this.dataOutputStream.write(cmd);
        this.dataOutputStream.flush();
        return true;
    }

    public boolean g_move(int cmd, int speeds) throws IOException {
        if (!this.bConnected) {
            return false;
        }
        byte[] cmdg = null;
        if (speeds > 0) {
            this.moveFlag = 1;
            cmdg = CommandEncoder.cmdDeviceControlReq(11, CommandEncoder.KEEP_ALIVE);
        }
        if (speeds < 0) {
            this.moveFlag = 1;
            cmdg = CommandEncoder.cmdDeviceControlReq(12, CommandEncoder.KEEP_ALIVE);
        }
        this.dataOutputStream.write(cmdg);
        this.dataOutputStream.flush();
        return true;
    }

    private int mDir= 0;
    private int mSpeed= 0;

    public void move(int dir, int speed) throws IOException {
        if (speed != 0 && dir == mDir && speed == mSpeed) return;

        mDir = dir;
        mSpeed = speed;

        (new Thread(LMovingTask2s)).start();
    }

    private Thread LMovingTask2s = new Thread() {
        public void run() {
            try {
                sendMoveCommand(mDir, mSpeed);
            } catch (Exception iOException) {
                iOException.printStackTrace();
            }
        }
    };
    private void sendMoveCommand(int dir, int speed) throws IOException {
        if (this.bConnected) {
            AppLog.d("run move", "dir:" + dir + " speed:" + speed);
            byte[] cmd = null;
            if (dir == 0) {
                if (speed > 0) {
                    enableMoveFlag();
                    this.moveFlag = 1;
                    cmd = CommandEncoder.cmdDeviceControlReq(4, speed);
                    AppLog.d("move", "cmdDeviceControlReq(1):" + speed);
                } else if (speed == 0) {
                    disableMoveFlag();
                    this.moveFlag = 0;
                    cmd = CommandEncoder.cmdDeviceControlReq(3, 0);
                } else if (speed < 0) {
                    enableMoveFlag();
                    this.moveFlag = 1;
                    cmd = CommandEncoder.cmdDeviceControlReq(5, Math.abs(speed));
                }
            } else if (dir == 1) {
                if (speed > 0) {
                    enableMoveFlag();
                    this.moveFlag = 1;
                    cmd = CommandEncoder.cmdDeviceControlReq(1, speed);
                } else if (speed == 0) {
                    disableMoveFlag();
                    this.moveFlag = 0;
                    cmd = CommandEncoder.cmdDeviceControlReq(0, speed);
                } else if (speed < 0) {
                    enableMoveFlag();
                    this.moveFlag = 1;
                    cmd = CommandEncoder.cmdDeviceControlReq(2, Math.abs(speed));
                }
            }
            this.dataOutputStream.write(cmd);
            this.dataOutputStream.flush();
            //return true;
        }
        //return false;
    }

    public void sendTalkData(TalkData data, int type) throws IOException {
        if (type == 0) {
            CommandEncoder.Protocol cmd = CommandEncoder.createTalkData(data);
            AppLog.d("mic", "send talk(" + this.receiverMediaSocket.isConnected() + "," + this.mediaReceiverOutputStream.size() + "):" + data.getTicktime() + "," + data.getSerial() + "," + data.getTimestamp());
            AppLog.d("mic", "hex:" + ByteUtility.bytesToHex(cmd.output()));
            this.mediaReceiverOutputStream.write(cmd.output());
            this.mediaReceiverOutputStream.flush();
        }
        if (type == 1) {
            CommandEncoder.Protocol cmd2 = CommandEncoder.createTalkData(data);
            AppLog.d("mic", "send talk(" + this.senderMediaSocket.isConnected() + "," + this.mediaSenderOutputStream.size() + "):" + data.getTicktime() + "," + data.getSerial() + "," + data.getTimestamp());
            AppLog.d("mic", "hex:" + ByteUtility.bytesToHex(cmd2.output()));
            this.mediaSenderOutputStream.write(cmd2.output());
            this.mediaSenderOutputStream.flush();
        }
    }

    public static byte[] int32ToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & CommandEncoder.KEEP_ALIVE);
        }
        return b;
    }

    private static byte[] int16ToByteArray(int value) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & CommandEncoder.KEEP_ALIVE);
        }
        return b;
    }

    private static byte[] int8ToByteArray(int value) {
        byte[] b = new byte[1];
        for (int i = 0; i < 1; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & CommandEncoder.KEEP_ALIVE);
        }
        return b;
    }

    private static int byteArrayToInt(byte[] inByteArray, int iOffset, int iLen) {
        int iResult = 0;
        for (int x = 0; x < iLen; x++) {
            if (x == 0 && inByteArray[((iLen - 1) + iOffset) - x] < 0) {
                iResult |= inByteArray[((iLen - 1) + iOffset) - x] & (-1);
            } else {
                iResult |= inByteArray[((iLen - 1) + iOffset) - x] & 255;
            }
            if (x < iLen - 1) {
                iResult <<= 8;
            }
        }
        return iResult;
    }

    private static byte[] adpcm_decode(byte[] raw, int len, int pre_sample, int index) {
        int code;
        int sb;
        ByteBuffer bDecoded = ByteBuffer.allocate(len * 4);
        int len2 = len << 1;
        for (int i = 0; i < len2; i++) {
            if ((i & 1) != 0) {
                code = raw[i >> 1] & 15;
            } else {
                code = raw[i >> 1] >> 4;
            }
            if ((code & 8) != 0) {
                sb = 1;
            } else {
                sb = 0;
            }
            int code2 = code & 7;
            int delta = ((step_table[index] * code2) / 4) + (step_table[index] / 8);
            if (sb != 0) {
                delta = -delta;
            }
            pre_sample += delta;
            if (pre_sample > 32767) {
                pre_sample = 32767;
            } else if (pre_sample < -32768) {
                pre_sample = -32768;
            }
            bDecoded.put(int16ToByteArray(pre_sample));
            index += index_adjust[code2];
            if (index < 0) {
                index = 0;
            }
            if (index > 88) {
                index = 88;
            }
        }
        return bDecoded.array();
    }

    public AudioComponent getAudioComponent() {
        return this.audio;
    }

    public boolean playAudio() {
        try {
            AppLog.d("audio", "play audio");
            this.audio.play();
            if (this.carStateMode == 1) {
                AppLog.d("recordaction", "playAudio:" + System.currentTimeMillis());
                return true;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopAudio() {
        this.audio.stopPlayer();
        if (this.carStateMode == 1) {
            AppLog.d("recordaction", "stopAudio:" + System.currentTimeMillis());
        }
    }

    public void stopRecordAudio() {
        this.audio.stopRecord();
    }

    public void startMic() {
    }

    public void stopMic() {
    }

    public String startFlim(Context context, int width, int height) throws ParseException, Exception {
        return startFlim(getVideoFolder(context), String.valueOf(TimeUtility.getCurrentTimeStr()) + ".avi", width, height);
    }

    public String startFlim(String path, String fileName, int width, int height) throws Exception {
        this.flim.start(path, fileName, width, height);
        if (this.carStateMode == 1) {
            AppLog.d("recordaction", "stopAudio:" + System.currentTimeMillis());
        }
        return String.valueOf(path) + fileName;
    }

    public void stopFlim() throws Exception {
        this.flim.stop();
        if (this.carStateMode == 1) {
            AppLog.d("recordaction", "stopAudio:" + System.currentTimeMillis());
        }
    }

    public void appendVideoDataToFlim(VideoData data) throws Exception {
        this.vData = data;
        if (this.flim.state == 1 && getAudioFlag() == 0) {
            long timeInterval = data.getCustomTimestamp() - this.flim.getLastVideoFrameCustomTimestamp();
            int count = ((int) (timeInterval / 40)) + 1;
            for (int i = 0; i < count; i++) {
                AudioData audioData = AudioData.createEmptyPCMData(640, 0, data.getTimestamp());
                this.flim.pushAudioData(audioData, 1);
            }
        }
        this.flim.pushVideoData(data, 0);
        setVideoBitmapBytes(data);
    }

    public void appendAudioDataToFlim(AudioData data) throws Exception {
        this.aData = data;
        if (getAudioFlag() == 1) {
            if (getMoveFlagCount() > 0) {
                long audioTimeInterval = data.getTimestamp() - this.flim.getLastAudioFrameTimestamp();
                if (audioTimeInterval > 1000) {
                }
                AudioData audioData = AudioData.createEmptyPCMData(640, 0, data.getTimestamp());
                this.flim.pushAudioData(audioData, getMoveFlagCount());
                getAudioComponent().writeAudioData(audioData);
                decreaseMoveFlagCount();
                return;
            }
            this.flim.pushAudioData(data, 2);
            getAudioComponent().writeAudioData(data);
        }
    }
}
