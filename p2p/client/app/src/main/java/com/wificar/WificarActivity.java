package com.wificar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.rover2.R;
import com.wificar._R;
import com.wificar.component.AudioComponent;
import com.wificar.component.WifiCar;
import com.wificar.dialog.Connect_Dialog;
import com.wificar.dialog.DisGsensor;
import com.wificar.dialog.Disrecord_play_dialog;
import com.wificar.dialog.Disrecordvideo_dialog;
import com.wificar.dialog.SDcardCheck;
import com.wificar.dialog.VideoSaveDialog;
import com.wificar.surface.CameraSurfaceView;
import com.wificar.surface.Camera_UD_SurfaceView;
import com.wificar.util.AVIGenerator;
import com.wificar.util.AppLog;
import com.wificar.util.MessageUtility;
import com.wificar.util.WificarNewLayoutParams;
import com.wificar.util.WificarUtility;
import com.wificar.view.AppCamera_UD;
import com.wificar.view.AppLeft_MoveCar;
import com.wificar.view.AppRight_MoveCar;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class WificarActivity extends BaseActivity implements View.OnClickListener {
    public static final int DOUBLE_AXIS_CONTROLLER = 1;
    private static final int DOUBLE_PRESS_INTERVAL = 2000;
    private static final int MAX = 10;
    protected static final int MESSAGAE_BATTERY = 9502;
    public static final int MESSAGE_BATTERY_0 = 9005;
    public static final int MESSAGE_BATTERY_100 = 9001;
    public static final int MESSAGE_BATTERY_25 = 9004;
    public static final int MESSAGE_BATTERY_50 = 9003;
    public static final int MESSAGE_BATTERY_75 = 9002;
    public static final int MESSAGE_BATTERY_UNKNOWN = 9006;
    protected static final int MESSAGE_CHECK_TEST = 8915;
    public static final int MESSAGE_CONNECT_TO_CAR = 8901;
    public static final int MESSAGE_CONNECT_TO_CAR_FAIL = 8903;
    public static final int MESSAGE_CONNECT_TO_CAR_SUCCESS = 8902;
    public static final int MESSAGE_DISCONNECTED = 8904;
    public static final int MESSAGE_GET_SETTING_INFO = 8701;
    protected static final int MESSAGE_MAIN_SETTING = 8700;
    public static final int MESSAGE_PLAY_PICTRUE = 8914;
    public static final int MESSAGE_PLAY_VIDEO = 8917;
    public static final int MESSAGE_SETTING = 9000;
    public static final int MESSAGE_SOUND = 8999;
    protected static final int MESSAGE_START_APPLICATION = 8905;
    public static final int MESSAGE_START_PLAY = 8913;
    protected static final int MESSAGE_START_RECORD = 8912;
    protected static final int MESSAGE_START_RECORD_AUDIO = 7000;
    public static final int MESSAGE_STOP_PLAY = 8911;
    protected static final int MESSAGE_STOP_RECORD = 8910;
    protected static final int MESSAGE_STOP_RECORD_AUDIO = 7001;
    public static final int MESSAGE_STOP_VIDEO = 8918;
    protected static final int MESSAGE_TAKE_PHOTO = 8702;
    public static final int MESSAGE_WIFISTATE = 8900;
    public static final int SINGLE_CONTROLLER = 0;
    public static float Scale;
    public static int UD_Diff_x;
    private String DirectPath;
    private String FileName;
    private String IP;
    private int LowPower;
    private Button Okbutton;
    public RelativeLayout Parent;
    private String Port;
    private int RssiLevel;
    private String SSID;
    public int Screen_height;
    public int Screen_width;
    private int Volume;
    private Sensor aSensor;
    public AppCamera_UD appCamera_UD;
    public AppLeft_MoveCar appLeft_MoveCar;
    public AppRight_MoveCar appRight_MoveCar;
    public float density;
    private Dialog dlg;
    private String firmwareVersion;
    public ToggleButton gSensorButton;
    public int hight;
    public boolean isPad;
    private long lastTime;
    private int level;
    private Context mContext;
    private PopupWindow mPopupWindow;
    private SensorManager mSensorManager;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;
    private Sensor mfSensor;
    private long nSDFreeSize;
    public int pictrue_play1;
    private long recordTimeLength;
    private long recordTimeLength1;
    public long[] record_times;
    public long[] record_video_times;
    private long replay_time;
    private int scale;
    public double screenSize;
    private ProgressDialog settingProgressDialog;
    public Button shareButton;
    private String ssid;
    private String startSSID;
    public String stop_take_v;
    public long[] stop_time;
    public long[] stop_video_times;
    public EditText tIP;
    public EditText tPort;
    public String take_p;
    public int take_pictrue_T1;
    public String take_v;
    public int take_video_T1;
    public int take_video_T_S1;
    public TextView tdevice;
    public TextView tfirmware;
    public long[] time;
    public TextView tsoftware;
    private String version;
    public int video_play1;
    public int video_play_stop1;
    public long[] video_time;
    private int wifiRssi;
    public WificarNewLayoutParams wificarNewLayoutParams;
    public int with;
    private static int videoWidth = 320;
    private static int videoHeight = 240;
    private static AVIGenerator movUtil = null;
    public static WificarActivity instance = null;
    private static boolean videoRecordEnable = false;
    public static boolean isTop = true;
    public static int Car_Move_Progress_Width = 0;
    public static int Car_Move_Progress_Height = 0;
    public static int Car_Camera_Progress_Width = 0;
    public static int Car_Camera_Progress_Height = 0;
    public static int Car_Compont_UD_Marge_L = 20;
    public static int Car_Compont_LR_Marge_R = 20;
    public static int Car_Compont_UD_Marge_D = 80;
    public static int Car_Compont_LR_Marge_D = 80;
    private String TAG = "WificarActivity";
    public double dimension = 0.0d;
    private boolean bThreadRun = false;
    private boolean bPhotoThreadRun = false;
    private boolean sensorEnable = false;
    private AudioComponent audio = null;
    private boolean gSensorControlEnable = false;
    private int LControlFlag = 0;
    private boolean LMoving = false;
    private int RControlFlag = 0;
    private boolean RMoving = false;
    private int controllerType = 1;
    private CameraSurfaceView cameraSurfaceView = null;
    private WifiCar wifiCar = null;
    private Handler handler = null;
    private boolean isLowPower = false;
    private ProgressDialog connectionProgressDialog = null;
    private float[] fAccelerometerValues = null;
    private float[] fMagneticFieldValues = null;
    private int LR = 0;
    private boolean LRshow = false;
    public boolean isGsensor = false;
    public boolean disGsensor = false;
    public boolean isNotExit = false;
    private boolean bIsPortait = true;
    private boolean orientationLock = false;
    private boolean connect_error = false;
    private boolean No_Sdcard = false;
    private boolean stopVideo = false;
    private boolean startVideo = false;
    public boolean isconnectwifi = true;
    private boolean controlEnable = false;
    public boolean succeedConnect = false;
    public boolean isPlayModeEnable = false;
    private Timer stop_talk = new Timer(true);
    private Timer tGMove = null;
    private final int iStep = 100;
    private int iCarSpeedR = 0;
    private int iCarSpeedL = 0;
    private int iLastSpeedL = 0;
    private int GsensorCountF = 0;
    private int GsensorCountB = 0;
    private float fBaseDefault = 9999.0f;
    private float fBasePitch = this.fBaseDefault;
    private float fBaseRoll = this.fBaseDefault;
    private float stickRadiu = 40.0f;
    private float accDefaultX = 9999.0f;
    private float accDefaultY = 9999.0f;
    private int f = 0;
    private int b = 0;
    private int cameramove = 0;
    private int setting = 0;
    public int audio_play = 0;
    public int audio_stop = 0;
    private int isTalk = 0;
    private int isZero = 0;
    public int flag = 0;
    public int sdcheck = 0;
    private long lastPressTime = 0;
    private Timer checkSound = new Timer(true);
    private Timer recTimer = new Timer(true);
    private Timer playTimer = new Timer(true);
    private Timer replayTimer = new Timer(true);
    private Timer checkTimer = new Timer(true);
    private Timer checkSDcard = new Timer(true);
    private Timer take_pictrue = new Timer(true);
    private Timer take_video = new Timer(true);
    private Timer stop_take_video = new Timer(true);
    private Timer reConnectTimer = new Timer(true);
    private Timer timeout2 = null;
    public Timer ConnnectOut_timer = null;
    public int isConnectOut = 0;
    private long startRecordTimeStamp = 0;
    private long replay_start = 0;
    private int replay_flag = 0;
    private VideoSaveDialog videoSaveDialog = new VideoSaveDialog();
    public int isplay_pictrue = 0;
    public int take_pictrue_T = 0;
    public int j = 0;
    public int take_flag = 0;
    public int pictrue_play = 0;
    public int pictrue_pressed = 0;
    public int isplay_video = 0;
    public int take_video_T = 0;
    public int k = 0;
    public int video_play = 0;
    public int video_record_stop = 0;
    private int video_pressed = 0;
    public int take_video_T_S = 0;
    public int s = 0;
    public int video_play_stop = 0;
    public String FILENSME = "time_record";
    public String FILENAME_V = "time_video";
    public String FILENAME_S = "stop_video";
    private String RECORD_TIME = "record_time";
    private Bitmap batteryBitmap = null;
    private BroadcastReceiver spReceiver = new BroadcastReceiver() { // from class: com.wificar.WificarActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String str = intent.getAction();
            AppLog.d("wild0", "In myReceiver, action = " + str);
            AppLog.d("Settings", "Received action: " + str);
            if (str.equals("android.intent.action.BATTERY_CHANGED")) {
                WificarActivity.this.level = intent.getIntExtra("level", 0);
                WificarActivity.this.scale = intent.getIntExtra("scale", 100);
                int power = (WificarActivity.this.level * 100) / WificarActivity.this.scale;
                WificarActivity.this.sendMessage(WificarActivity.MESSAGAE_BATTERY);
                if (power < 20) {
                    WificarActivity.this.isLowPower = true;
                }
                if (power > 20) {
                    WificarActivity.this.isLowPower = false;
                }
                AppLog.d("wild0", "battery changed...");
            } else if (str.equals("android.intent.action.BATTERY_LOW")) {
                WificarActivity.this.isLowPower = true;
                WificarActivity.this.LowPower = (WificarActivity.this.level * 100) / WificarActivity.this.scale;
            } else if (!str.equals("android.intent.action.ACTION_POWER_CONNECTED") && !str.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                if (str.equals("WifiManager.WIFI_STATE_CHANGED_ACTION")) {
                    intent.getIntExtra("wifi_state", 4);
                } else if (!str.equals("BluetoothAdapter.STATE_ON") && !str.equals("BluetoothAdapter.STATE_TURNING_OFF") && !str.equals("android.intent.action.SCREEN_OFF")) {
                    str.equals("android.intent.action.SCREEN_ON");
                }
            }
        }
    };
    final SensorEventListener myAccelerometerListener = new SensorEventListener() { // from class: com.wificar.WificarActivity.2
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private int[] convertGravity(float dx, float dy, float gx, float gy, float range, int orientation) {
            int leftVolecity = 0;
            int rightVolecity = 0;
            float my = gy - dy;
            float mx = gx - dx;
            WificarActivity.this.LR++;
            AppLog.i("zhang", "my :" + my + ",mx :" + mx);
            if (orientation == 1) {
                if (Math.abs(my) > range && my < (-range)) {
                    leftVolecity = ((int) Math.ceil(my)) * 1;
                    rightVolecity = ((int) Math.ceil(my)) * (-1);
                    AppLog.i("acce", "left:" + leftVolecity + "," + rightVolecity);
                } else if (Math.abs(my) > range && my > range) {
                    leftVolecity = ((int) Math.ceil(my)) * 1;
                    rightVolecity = ((int) Math.ceil(my)) * (-1);
                    AppLog.i("acce", "right:" + leftVolecity + "," + rightVolecity);
                }
            } else if (Math.abs(my) > range && my < (-range)) {
                leftVolecity = ((int) Math.ceil(my)) * (-1);
                rightVolecity = ((int) Math.ceil(my)) * 1;
                AppLog.i("acce11", "right:" + leftVolecity + "," + rightVolecity);
            } else if (Math.abs(my) > range && my > range) {
                leftVolecity = ((int) Math.ceil(my)) * (-1);
                rightVolecity = ((int) Math.ceil(my)) * 1;
                AppLog.i("acce11", "left:" + leftVolecity + "," + rightVolecity);
            }
            if (mx < (-range)) {
                leftVolecity -= (int) Math.ceil(mx);
                rightVolecity -= (int) Math.ceil(mx);
                WificarActivity.this.GsensorCountF++;
                if (WificarActivity.this.GsensorCountF < 40 && WificarActivity.this.gSensorControlEnable) {
                    AppLog.i("zhang", "Gsensor :" + WificarActivity.this.GsensorCountF);
                    WificarActivity.this.setDirectionGsensor(leftVolecity, rightVolecity);
                    AppLog.i("zhang Forware", " leftVolecity :" + leftVolecity + ",rightVolecity :" + rightVolecity);
                }
            } else if (mx > range) {
                leftVolecity -= (int) Math.ceil(mx);
                rightVolecity -= (int) Math.ceil(mx);
                WificarActivity.this.GsensorCountB++;
                if (WificarActivity.this.GsensorCountB < 40 && WificarActivity.this.gSensorControlEnable) {
                    WificarActivity.this.setDirectionGsensor(leftVolecity, rightVolecity);
                    AppLog.i("zhang Back", " leftVolecity :" + leftVolecity + ",rightVolecity :" + rightVolecity);
                }
            }
            if (leftVolecity >= 2) {
                leftVolecity = 10;
            }
            if (leftVolecity <= -2) {
                leftVolecity = -10;
            }
            if (rightVolecity >= 2) {
                rightVolecity = 10;
            }
            if (rightVolecity <= -2) {
                rightVolecity = -10;
            }
            AppLog.i("zhang", " leftVolecity :" + leftVolecity + ",rightVolecity :" + rightVolecity);
            return new int[]{leftVolecity, rightVolecity};
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            int[] iCar;
            int[] iCar2;
            WificarActivity.this.fAccelerometerValues = event.values;
            if (WificarActivity.this.accDefaultX == WificarActivity.this.fBaseDefault) {
                WificarActivity.this.accDefaultX = WificarActivity.this.fAccelerometerValues[0];
            }
            if (WificarActivity.this.accDefaultY == WificarActivity.this.fBaseDefault) {
                WificarActivity.this.accDefaultY = WificarActivity.this.fAccelerometerValues[1];
            }
            WindowManager mWindowManager = (WindowManager) WificarActivity.this.getSystemService("window");
            Display mDisplay = mWindowManager.getDefaultDisplay();
            int[] iArr = new int[2];
            if (mDisplay.getOrientation() == 1) {
                if ((WificarActivity.this.fAccelerometerValues[0] < 0.1f && WificarActivity.this.fAccelerometerValues[2] < 9.8f && !WificarActivity.this.isGsensor) || (WificarActivity.this.fAccelerometerValues[0] > 7.6f && WificarActivity.this.fAccelerometerValues[2] < 5.8f && !WificarActivity.this.isGsensor)) {
                    WificarActivity.this.disGsensor = true;
                    WificarActivity.this.showDialog();
                    WificarActivity.this.gSensorControlEnable = false;
                    WificarActivity.this.mSensorManager.unregisterListener(WificarActivity.this.myAccelerometerListener);
                    return;
                }
                WificarActivity.this.handler.postDelayed(WificarActivity.this.gMovingTask, 100L);
                WificarActivity.this.isGsensor = true;
                if (WificarActivity.this.LR < 1 && WificarActivity.this.gSensorControlEnable) {
                    WificarActivity.this.appLeft_MoveCar.Hided(1);
                    WificarActivity.this.appRight_MoveCar.Hided(1);
                }
                if (mDisplay.getOrientation() == 1) {
                    iCar2 = convertGravity(WificarActivity.this.accDefaultX, WificarActivity.this.accDefaultY, WificarActivity.this.fAccelerometerValues[0], WificarActivity.this.fAccelerometerValues[1], 1.5f, mDisplay.getOrientation());
                    WificarActivity.this.iCarSpeedL = iCar2[0];
                    WificarActivity.this.iCarSpeedR = iCar2[1];
                } else {
                    iCar2 = convertGravity(WificarActivity.this.accDefaultY, WificarActivity.this.accDefaultX, WificarActivity.this.fAccelerometerValues[1], WificarActivity.this.fAccelerometerValues[0], 1.5f, mDisplay.getOrientation());
                    WificarActivity.this.iCarSpeedL = iCar2[0];
                    WificarActivity.this.iCarSpeedR = iCar2[1];
                }
                AppLog.i("acce", "cl:" + iCar2[0] + ",cr:" + iCar2[1]);
                if (WificarActivity.this.iCarSpeedL != WificarActivity.this.iCarSpeedR) {
                    try {
                        WificarActivity.this.wifiCar.enableMoveFlag();
                        WificarActivity.this.wifiCar.move(0, iCar2[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        WificarActivity.this.wifiCar.enableMoveFlag();
                        WificarActivity.this.wifiCar.move(1, iCar2[1]);
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (WificarActivity.this.GsensorCountB > 40 || WificarActivity.this.GsensorCountF > 40) {
                    WificarActivity.this.gSensorControlEnable = false;
                    AppLog.i("zhang", "zhang zhang zhang:" + WificarActivity.this.GsensorCountF);
                    if (WificarActivity.this.iCarSpeedL == WificarActivity.this.iCarSpeedR) {
                        try {
                            WificarActivity.this.wifiCar.enableMoveFlag();
                            WificarActivity.this.wifiCar.move(0, iCar2[0]);
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                        try {
                            WificarActivity.this.wifiCar.enableMoveFlag();
                            WificarActivity.this.wifiCar.move(1, iCar2[1]);
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                }
                if (WificarActivity.this.iCarSpeedL == 0 && WificarActivity.this.iCarSpeedR == 0) {
                    WificarActivity.this.GsensorCountB = 0;
                    WificarActivity.this.GsensorCountF = 0;
                    try {
                        WificarActivity.this.wifiCar.disableMoveFlag();
                        WificarActivity.this.wifiCar.move(0, WificarActivity.this.iCarSpeedL);
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                    try {
                        WificarActivity.this.wifiCar.disableMoveFlag();
                        WificarActivity.this.wifiCar.move(1, WificarActivity.this.iCarSpeedR);
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
            } else if (mDisplay.getOrientation() == 0) {
                if ((WificarActivity.this.fAccelerometerValues[1] < 0.0f && WificarActivity.this.fAccelerometerValues[2] < 9.7d && !WificarActivity.this.isGsensor) || (WificarActivity.this.fAccelerometerValues[1] > 7.8f && WificarActivity.this.fAccelerometerValues[2] < 5.5f && !WificarActivity.this.isGsensor)) {
                    AppLog.i("zhang", "想前快垂直了");
                    WificarActivity.this.showDialog();
                    WificarActivity.this.gSensorControlEnable = false;
                    WificarActivity.this.mSensorManager.unregisterListener(WificarActivity.this.myAccelerometerListener);
                    return;
                }
                WificarActivity.this.handler.postDelayed(WificarActivity.this.gMovingTask, 100L);
                WificarActivity.this.isGsensor = true;
                AppLog.e("zhangLLLLLLRRRRRRRR", new StringBuilder(String.valueOf(WificarActivity.this.LR)).toString());
                if (WificarActivity.this.LR < 1 && WificarActivity.this.gSensorControlEnable) {
                    WificarActivity.this.appLeft_MoveCar.Hided(1);
                    WificarActivity.this.appRight_MoveCar.Hided(1);
                }
                if (mDisplay.getOrientation() == 1) {
                    iCar = convertGravity(WificarActivity.this.accDefaultX, WificarActivity.this.accDefaultY, WificarActivity.this.fAccelerometerValues[0], WificarActivity.this.fAccelerometerValues[1], 1.5f, mDisplay.getOrientation());
                    WificarActivity.this.iCarSpeedL = iCar[0];
                    WificarActivity.this.iCarSpeedR = iCar[1];
                } else {
                    iCar = convertGravity(WificarActivity.this.accDefaultY, WificarActivity.this.accDefaultX, WificarActivity.this.fAccelerometerValues[1], WificarActivity.this.fAccelerometerValues[0], 1.5f, mDisplay.getOrientation());
                    WificarActivity.this.iCarSpeedL = iCar[0];
                    WificarActivity.this.iCarSpeedR = iCar[1];
                }
                AppLog.i("acce", "cl:" + iCar[0] + ",cr:" + iCar[1]);
                if (WificarActivity.this.iCarSpeedL != WificarActivity.this.iCarSpeedR) {
                    try {
                        WificarActivity.this.wifiCar.enableMoveFlag();
                        WificarActivity.this.wifiCar.move(0, iCar[0]);
                    } catch (IOException e7) {
                        e7.printStackTrace();
                    }
                    try {
                        WificarActivity.this.wifiCar.enableMoveFlag();
                        WificarActivity.this.wifiCar.move(1, iCar[1]);
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                if (WificarActivity.this.GsensorCountB > 40 || WificarActivity.this.GsensorCountF > 40) {
                    WificarActivity.this.gSensorControlEnable = false;
                    AppLog.i("zhang", "zhang zhang zhang:" + WificarActivity.this.GsensorCountF);
                    if (WificarActivity.this.iCarSpeedL == WificarActivity.this.iCarSpeedR) {
                        try {
                            WificarActivity.this.wifiCar.enableMoveFlag();
                            WificarActivity.this.wifiCar.move(0, iCar[0]);
                        } catch (IOException e9) {
                            e9.printStackTrace();
                        }
                        try {
                            WificarActivity.this.wifiCar.enableMoveFlag();
                            WificarActivity.this.wifiCar.move(1, iCar[1]);
                        } catch (IOException e10) {
                            e10.printStackTrace();
                        }
                    }
                }
                if (WificarActivity.this.iCarSpeedL == 0 && WificarActivity.this.iCarSpeedR == 0) {
                    WificarActivity.this.GsensorCountB = 0;
                    WificarActivity.this.GsensorCountF = 0;
                    try {
                        WificarActivity.this.wifiCar.disableMoveFlag();
                        WificarActivity.this.wifiCar.move(0, WificarActivity.this.iCarSpeedL);
                    } catch (IOException e11) {
                        e11.printStackTrace();
                    }
                    try {
                        WificarActivity.this.wifiCar.disableMoveFlag();
                        WificarActivity.this.wifiCar.move(1, WificarActivity.this.iCarSpeedR);
                    } catch (IOException e12) {
                        e12.printStackTrace();
                    }
                }
            }
        }
    };
    final SensorEventListener myMagneticFieldListener = new SensorEventListener() { // from class: com.wificar.WificarActivity.3
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            WificarActivity.this.fMagneticFieldValues = event.values;
        }
    };
    long recordStartTime = 0;
    long recordTime = 0;
    private Runnable LMovingTask = new Runnable() { // from class: com.wificar.WificarActivity.4
        @Override // java.lang.Runnable
        public void run() {
            if (WificarActivity.this.LMoving) {
                if (WificarActivity.this.LControlFlag == 1) {
                    AppLog.e(WificarActivity.this.TAG, "-->左边前");
                    try {
                        WificarActivity.this.wifiCar.move(0, 10);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (WificarActivity.this.LControlFlag == 2) {
                    try {
                        WificarActivity.this.wifiCar.move(0, -10);
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            WificarActivity.this.handler.postDelayed(this, 100L);
        }
    };
    private Runnable RMovingTask = new Runnable() { // from class: com.wificar.WificarActivity.5
        @Override // java.lang.Runnable
        public void run() {
            if (WificarActivity.this.RMoving) {
                if (WificarActivity.this.RControlFlag == 1) {
                    AppLog.e(WificarActivity.this.TAG, "--->右边前");
                    try {
                        WificarActivity.this.wifiCar.move(1, 10);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (WificarActivity.this.RControlFlag == 2) {
                    AppLog.e(WificarActivity.this.TAG, "--->右边后");
                    try {
                        WificarActivity.this.wifiCar.move(1, -10);
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            WificarActivity.this.handler.postDelayed(this, 100L);
        }
    };
    private Runnable gMovingTask = new Runnable() { // from class: com.wificar.WificarActivity.6
        @Override // java.lang.Runnable
        public void run() {
            if ((WificarActivity.this.iCarSpeedL == WificarActivity.this.iCarSpeedR) & WificarActivity.this.gSensorControlEnable) {
                if (WificarActivity.this.iCarSpeedL > 0 && WificarActivity.this.iCarSpeedL != 0) {
                    try {
                        WificarActivity.this.wifiCar.enableMoveFlag();
                        WificarActivity.this.wifiCar.g_move(11, WificarActivity.this.iCarSpeedL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    WificarActivity.this.iLastSpeedL = WificarActivity.this.iCarSpeedL;
                }
                if (WificarActivity.this.iCarSpeedL < 0 && WificarActivity.this.iCarSpeedL != 0) {
                    try {
                        WificarActivity.this.wifiCar.enableMoveFlag();
                        WificarActivity.this.wifiCar.g_move(12, WificarActivity.this.iCarSpeedL);
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    WificarActivity.this.iLastSpeedL = WificarActivity.this.iCarSpeedL;
                }
                if (WificarActivity.this.iCarSpeedL == 0 && WificarActivity.this.iLastSpeedL != 0) {
                    try {
                        WificarActivity.this.wifiCar.disableMoveFlag();
                        WificarActivity.this.wifiCar.move(0, WificarActivity.this.iCarSpeedL);
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                    try {
                        WificarActivity.this.wifiCar.disableMoveFlag();
                        WificarActivity.this.wifiCar.move(1, WificarActivity.this.iCarSpeedR);
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                    WificarActivity.this.iLastSpeedL = 0;
                }
            }
            WificarActivity.this.handler.postDelayed(this, 100L);
        }
    };
    private Runnable TakePhotoTask = new Runnable() { // from class: com.wificar.WificarActivity.7
        @Override // java.lang.Runnable
        public void run() {
            if (WificarActivity.this.bPhotoThreadRun) {
                if (WificarActivity.this.cameraSurfaceView != null) {
                    try {
                        AppLog.i("take photo", "start take photo");
                        WificarActivity.this.wifiCar.takePicture(WificarActivity.instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                WificarActivity.this.bPhotoThreadRun = false;
            }
            WificarActivity.this.handler.postDelayed(this, 100L);
        }
    };
    private Runnable StartVideoTask = new Runnable() { // from class: com.wificar.WificarActivity.8
        @Override // java.lang.Runnable
        public void run() {
            if (WificarActivity.this.startVideo) {
                try {
                    WificarActivity.this.openVideoStream(WificarActivity.this.FileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                WificarActivity.this.startVideo = false;
            }
            WificarActivity.this.handler.postDelayed(this, 100L);
        }
    };
    private Runnable StopVideoTask = new Runnable() { // from class: com.wificar.WificarActivity.9
        @Override // java.lang.Runnable
        public void run() {
            if (WificarActivity.this.stopVideo) {
                WificarActivity.this.stopVideo = false;
                WificarActivity.this.startVideo = false;
                try {
                    WificarActivity.this.closeVideoStream();
                    Toast toast = Toast.makeText(WificarActivity.instance, (int) R.string.wificar_activity_toast_stop_recording, 0);
                    toast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            WificarActivity.this.handler.postDelayed(this, 100L);
        }
    };
    private Camera_UD_SurfaceView camera_UD_SurfaceView = null;
    public int Left_Move_flag = 3;
    public int Right_Move_flag = 3;

    public static WificarActivity getInstance() {
        return instance;
    }

    public void sendMessage(int cmd) {
        Message msg = new Message();
        msg.what = cmd;
        getHandler().sendMessage(msg);
    }

    public void openVideoStream(String fileName) throws Exception {
        this.wifiCar.startFlim(String.valueOf(this.DirectPath) + "/Videos", fileName, videoWidth, videoHeight);
        videoRecordEnable = true;
    }

    public void closeVideoStream() throws Exception {
        AppLog.e("record", "stop recording");
        this.wifiCar.stopFlim();
        videoRecordEnable = false;
    }

    public Handler getHandler() {
        return this.handler;
    }

    public WifiCar getWifiCar() {
        return this.wifiCar;
    }

    @Override // android.app.Activity
    protected void onStop() {
        this.isNotExit = false;
        if (!this.No_Sdcard) {
            DeleVideo();
        }
        deleIndexVideo();
        exitProgrames();
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        isTop = false;
        if (this.Left_Move_flag != 3) {
            this.appLeft_MoveCar.init();
        }
        if (this.Right_Move_flag != 3) {
            this.appRight_MoveCar.init();
        }
        if (!this.No_Sdcard) {
            DeleVideo();
        }
        deleIndexVideo();
        pause();
        if (this.isPlayModeEnable) {
            try {
                this.wifiCar.stopPlayTrack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        exitProgrames();
    }

    public void exit() {
        if (this.isPlayModeEnable) {
            try {
                this.wifiCar.stopPlayTrack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        unregisterReceiver(this.spReceiver);
        exiteApplication();
    }

    public void pause() {
        if (this.wifiCar.isConnected() == 1) {
            AppLog.i("activity", "on exit 1");
            if (this.isPlayModeEnable) {
                try {
                    this.wifiCar.stopPlayTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                this.wifiCar.led_offTrack();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            try {
                this.wifiCar.disableIR();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            disableGSensorControl();
            if (this.sensorEnable) {
                this.mSensorManager.unregisterListener(this.myAccelerometerListener);
                this.mSensorManager.unregisterListener(this.myMagneticFieldListener);
            }
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e3) {
                e3.printStackTrace();
            }
            AppLog.d("activity", "on exit");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        isTop = true;
        getDisplayMetrics();
        initValue();
        reload();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.BATTERY_LOW");
        intentFilter.addAction("android.intent.action.BATTERY_OKAY");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        intentFilter.addAction("WifiManager.WIFI_STATE_CHANGED_ACTION");
        intentFilter.addAction("BluetoothAdapter.STATE_TURNING_OFF");
        intentFilter.addAction("BluetoothAdapter.STATE_ON");
        registerReceiver(this.spReceiver, intentFilter);
        AppLog.i("activity", "on Resume");
        Runnable init = new Runnable() { // from class: com.wificar.WificarActivity.10
            @Override // java.lang.Runnable
            public void run() {
                AppLog.d("wificar", "--->connecting .....");
                WifiManager wifiManager = (WifiManager) WificarActivity.this.getSystemService("wifi");
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID().toString();
                AppLog.d("SSID", "---->" + ssid);
                if (ssid.startsWith("<")) {
                    WificarActivity.this.ConnnectOut_timer = new Timer();
                    WificarActivity.this.ConnnectOut_timer.schedule(new ConnectOut(), 6000L);
                    boolean result = WificarActivity.this.wifiCar.setConnect();
                    AppLog.d("wild0", "--->connecting result:" + result + " isConnectOut:" + WificarActivity.this.isConnectOut);
                    WificarActivity.this.wifiCar.updatedChange();
                    if (result && WificarActivity.this.isConnectOut == 0) {
                        WificarActivity.this.isConnectOut = 1;
                        return;
                    } else if (!result && WificarActivity.this.isConnectOut == 0) {
                        WificarActivity.this.isConnectOut = 2;
                        Message messageConnectFail = new Message();
                        messageConnectFail.what = 34;
                        WificarActivity.this.handler.sendMessage(messageConnectFail);
                        return;
                    } else {
                        return;
                    }
                }
                Message messageConnectFail2 = new Message();
                messageConnectFail2.what = 34;
                WificarActivity.this.handler.sendMessage(messageConnectFail2);
            }
        };
        if (this.wifiCar.isConnected() == 0) {
            Thread initThread = new Thread(init);
            initThread.start();
        }
        ToggleButton gSensorTogglebutton = (ToggleButton) findViewById(R.id.g_sensor_toggle_button);
        if (gSensorTogglebutton != null && gSensorTogglebutton.isChecked()) {
            enableGSensorControl();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        AppLog.d("activity", "on destory");
        if (!this.No_Sdcard) {
            DeleVideo();
        }
        deleIndexVideo();
        this.handler.removeCallbacks(this.gMovingTask);
        this.handler.removeCallbacks(this.LMovingTask);
        this.handler.removeCallbacks(this.RMovingTask);
    }

    public void reload() {
        changeLandscape();
        this.connectionProgressDialog = new ProgressDialog(this);
        this.mSensorManager = (SensorManager) getSystemService("sensor");
        this.aSensor = this.mSensorManager.getDefaultSensor(1);
        this.mfSensor = this.mSensorManager.getDefaultSensor(2);
        int i = getResources().getConfiguration().orientation;
        ToggleButton irTogglebutton = (ToggleButton) findViewById(R.id.light_toggle_button);
        ToggleButton micTogglebutton = (ToggleButton) findViewById(R.id.mic_toggle_button);
        ToggleButton ledTogglebutton = (ToggleButton) findViewById(R.id.led_toggle_button);
        ToggleButton videTogglebutton = (ToggleButton) findViewById(R.id.video_toggle_button);
        Button takepictruebutton = (Button) findViewById(R.id.take_picture_button);
        irTogglebutton.setChecked(false);
        irTogglebutton.setBackgroundResource(R.drawable.ir);
        micTogglebutton.setChecked(false);
        micTogglebutton.setBackgroundResource(R.drawable.mic);
        ledTogglebutton.setChecked(false);
        ledTogglebutton.setBackgroundResource(R.drawable.led);
        videTogglebutton.setChecked(false);
        if (this.isPad) {
            videTogglebutton.setBackgroundResource(R.drawable.video);
            takepictruebutton.setBackgroundResource(R.drawable.camera);
            return;
        }
        videTogglebutton.setBackgroundResource(R.drawable.video1);
        takepictruebutton.setBackgroundResource(R.drawable.camera1);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        AppLog.d("activity", "WificarActivity:on Start");
    }

    public void reStartConnect() {
        AppLog.e("wificar", "restart connecting .....");
        this.reConnectTimer = new Timer(true);
        this.reConnectTimer.schedule(new TimerTask() { // from class: com.wificar.WificarActivity.11
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                WifiManager wifiManager = (WifiManager) WificarActivity.this.getSystemService("wifi");
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID().toString();
                AppLog.d("SSID", "---->restartConnect:" + ssid);
                if (ssid.startsWith("<")) {
                    boolean result = WificarActivity.this.wifiCar.setConnect();
                    AppLog.i("zhang", "reconnecting result:" + result);
                    WificarActivity.this.wifiCar.updatedChange();
                    if (!result) {
                        Message messageConnectFail = new Message();
                        messageConnectFail.what = 34;
                        WificarActivity.this.handler.sendMessage(messageConnectFail);
                        if (WificarActivity.this.reConnectTimer != null) {
                            WificarActivity.this.reConnectTimer.cancel();
                            WificarActivity.this.reConnectTimer = null;
                            return;
                        }
                        return;
                    }
                    return;
                }
                Message messageConnectFail2 = new Message();
                messageConnectFail2.what = 34;
                WificarActivity.this.handler.sendMessage(messageConnectFail2);
                if (WificarActivity.this.reConnectTimer != null) {
                    WificarActivity.this.reConnectTimer.cancel();
                    WificarActivity.this.reConnectTimer = null;
                }
            }
        }, 6000L);
    }

    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        instance = this;
        AppLog.enableLogging(true);
        requestWindowFeature(1);
        getWindow().setFlags(128, 128);
        this.isconnectwifi = note_Intent(instance);
        if (Environment.getExternalStorageState().equals("mounted")) {
            this.DirectPath = String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + "/Brookstone";
            File fileP = new File(String.valueOf(this.DirectPath) + "/Pictures");
            if (!fileP.exists()) {
                fileP.mkdirs();
            }
            File fileV = new File(String.valueOf(this.DirectPath) + "/Videos");
            if (!fileV.exists()) {
                fileV.mkdirs();
            }
        }
        this.checkTimer = new Timer(true);
        this.checkTimer.schedule(new SDCardSizeTest(), 10L);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.with = dm.widthPixels;
        this.hight = dm.heightPixels;
        AppLog.i("Main", "Width = " + dm.widthPixels);
        AppLog.i("Main", "Height = " + dm.heightPixels);
        this.dlg = new DisGsensor(instance, R.style.CustomDialog);
        new MessageUtility(instance);
        int[] rands = WificarUtility.getRandamNumber();
        this.wifiCar = new WifiCar(this, rands[0], rands[1], rands[2], rands[3]);
        try {
            this.handler = new Handler() { // from class: com.wificar.WificarActivity.12
                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    ToggleButton recordTogglebutton = (ToggleButton) WificarActivity.this.findViewById(R.id.record_toggle_button);
                    Button playTogglebutton = (Button) WificarActivity.this.findViewById(R.id.play_toggle_button);
                    ToggleButton micbutton = (ToggleButton) WificarActivity.this.findViewById(R.id.mic_toggle_button);
                    ImageView soundImg = (ImageView) WificarActivity.this.findViewById(R.id.no_sound);
                    Button button = (Button) WificarActivity.this.findViewById(R.id.take_picture_button);
                    ToggleButton toggleButton = (ToggleButton) WificarActivity.this.findViewById(R.id.video_toggle_button);
                    Button recordAudioTogglebutton = (Button) WificarActivity.this.findViewById(R.id.talk_button);
                    switch (msg.what) {
                        case WificarActivity.MESSAGE_START_RECORD_AUDIO /* 7000 */:
                            try {
                                AppLog.d("wild1", "record audio");
                                WificarActivity.this.wifiCar.enableRecordAudio(0);
                                break;
                            } catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }
                        case WificarActivity.MESSAGE_STOP_RECORD_AUDIO /* 7001 */:
                            recordAudioTogglebutton.setBackgroundResource(R.drawable.talk);
                            try {
                                WificarActivity.this.wifiCar.disableRecordAudio();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                            if (WificarActivity.this.audio_play == 1) {
                                WificarActivity.this.play_audio();
                                WificarActivity.this.setting_play();
                                WificarActivity.this.wifiCar.disableMoveFlag();
                                micbutton.setBackgroundResource(R.drawable.mic_pressed);
                                micbutton.setChecked(true);
                            }
                            if (WificarActivity.this.stop_talk != null) {
                                WificarActivity.this.stop_talk.cancel();
                                WificarActivity.this.stop_talk = null;
                                break;
                            }
                            break;
                        case WificarActivity.MESSAGE_GET_SETTING_INFO /* 8701 */:
                            new Thread(new MyThread()).start();
                            WificarActivity.this.bThreadRun = true;
                            break;
                        case WificarActivity.MESSAGE_TAKE_PHOTO /* 8702 */:
                            WificarActivity.this.bPhotoThreadRun = true;
                            WificarActivity.this.handler.postDelayed(WificarActivity.this.TakePhotoTask, 10L);
                            break;
                        case WificarActivity.MESSAGE_CONNECT_TO_CAR_SUCCESS /* 8902 */:
                            WificarActivity.this.isConnectOut = 1;
                            WificarActivity.this.succeedConnect = true;
                            WificarActivity.this.connectionProgressDialog.cancel();
                            WificarActivity.this.checkSDcard = new Timer(true);
                            WificarActivity.this.checkSDcard.schedule(new TimerTask() { // from class: com.wificar.WificarActivity.12.1
                                @Override // java.util.TimerTask, java.lang.Runnable
                                public void run() {
                                    WificarActivity.this.wifiCar.isConnected();
                                    if (!Environment.getExternalStorageState().equals("mounted")) {
                                        WificarActivity.this.No_Sdcard = true;
                                    } else {
                                        WificarActivity.this.No_Sdcard = false;
                                    }
                                    if (WificarActivity.this.No_Sdcard) {
                                        return;
                                    }
                                    WificarActivity.this.deleIndexVideo();
                                }
                            }, 100L, 1000L);
                            break;
                        case WificarActivity.MESSAGE_CONNECT_TO_CAR_FAIL /* 8903 */:
                            WificarActivity.this.connectionProgressDialog.cancel();
                            Connect_Dialog.createconnectDialog(WificarActivity.instance).show();
                            WificarActivity.this.isNotExit = true;
                            WificarActivity.this.connect_error = true;
                            break;
                        case WificarActivity.MESSAGE_STOP_RECORD /* 8910 */:
                            Toast toast = Toast.makeText(WificarActivity.instance, (int) R.string.complete_record, 0);
                            toast.show();
                            try {
                                WificarActivity.this.wifiCar.stopRecordTrack();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                            recordTogglebutton.setBackgroundResource(R.drawable.record_path);
                            playTogglebutton.setClickable(true);
                            if (WificarActivity.this.recTimer != null) {
                                WificarActivity.this.recordTimeLength = System.currentTimeMillis() - WificarActivity.this.startRecordTimeStamp;
                                SharedPreferences share_time = WificarActivity.this.getSharedPreferences(WificarActivity.this.RECORD_TIME, 0);
                                SharedPreferences.Editor edit_time = share_time.edit();
                                edit_time.putLong("record", WificarActivity.this.recordTimeLength);
                                edit_time.commit();
                                WificarActivity.this.recTimer.cancel();
                                WificarActivity.this.recTimer = null;
                            }
                            WificarActivity.this.take_flag = 0;
                            WificarActivity.this.video_record_stop = 0;
                            WificarActivity.this.take_pictrue_T = 0;
                            WificarActivity.this.take_video_T = 0;
                            WificarActivity.this.take_video_T_S = 0;
                            break;
                        case WificarActivity.MESSAGE_STOP_PLAY /* 8911 */:
                            WificarActivity.this.disablePlayMode();
                            WificarActivity.this.isPlayModeEnable = false;
                            WificarActivity.this.replay_flag = 0;
                            WificarActivity.this.isplay_pictrue = 0;
                            WificarActivity.this.j = 0;
                            WificarActivity.this.k = 0;
                            WificarActivity.this.s = 0;
                            break;
                        case WificarActivity.MESSAGE_START_RECORD /* 8912 */:
                            WificarActivity.this.take_flag = 1;
                            WificarActivity.this.pictrue_play = 0;
                            WificarActivity.this.video_play = 0;
                            WificarActivity.this.video_play_stop = 0;
                            try {
                                WificarActivity.this.wifiCar.startRecordTrack();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                            recordTogglebutton.setBackgroundResource(R.drawable.record_path_pressed);
                            recordTogglebutton.setTextColor(-16777216);
                            playTogglebutton.setClickable(false);
                            WificarActivity.this.startRecordTimeStamp = System.currentTimeMillis();
                            WificarActivity.this.recTimer = new Timer(true);
                            WificarActivity.this.recTimer.schedule(new RecordTask(), 60000L);
                            break;
                        case WificarActivity.MESSAGE_START_PLAY /* 8913 */:
                            WificarActivity.this.isPlayModeEnable = true;
                            try {
                                WificarActivity.this.wifiCar.startPlayTrack();
                            } catch (IOException e5) {
                                e5.printStackTrace();
                            }
                            playTogglebutton.setBackgroundResource(R.drawable.replay_pressed);
                            SharedPreferences share_time_play = WificarActivity.this.getSharedPreferences(WificarActivity.this.RECORD_TIME, 0);
                            WificarActivity.this.recordTimeLength1 = share_time_play.getLong("record", WificarActivity.this.recordTimeLength);
                            WificarActivity.this.replayTimer = new Timer(true);
                            WificarActivity.this.replayTimer.schedule(new rePlayTask(), WificarActivity.this.recordTimeLength1);
                            AppLog.i("aaaa", "replaystar");
                            break;
                        case WificarActivity.MESSAGE_CHECK_TEST /* 8915 */:
                            try {
                                WificarActivity.this.closeVideoStream();
                                Toast toast1 = Toast.makeText(WificarActivity.instance, (int) R.string.wificar_activity_toast_stop_recording, 0);
                                toast1.show();
                            } catch (Exception e6) {
                                e6.printStackTrace();
                            }
                            WificarActivity.this.checkSDcard();
                            break;
                        case WificarActivity.MESSAGE_SOUND /* 8999 */:
                            AudioManager mAudioManager = (AudioManager) WificarActivity.this.getSystemService("audio");
                            WificarActivity.this.Volume = mAudioManager.getStreamVolume(3);
                            AppLog.i("zhang", "currentVolume11 :" + WificarActivity.this.Volume);
                            if (WificarActivity.this.Volume == 0) {
                                WificarActivity.this.isZero = 1;
                                soundImg.setVisibility(0);
                                micbutton.setBackgroundResource(R.drawable.mic);
                                micbutton.setChecked(false);
                                micbutton.setClickable(true);
                                break;
                            } else {
                                soundImg.setVisibility(8);
                                if (WificarActivity.this.audio_play == 0 || WificarActivity.this.isTalk == 1) {
                                    soundImg.setVisibility(0);
                                }
                                if (WificarActivity.this.audio_play == 1 && WificarActivity.this.isTalk == 0) {
                                    micbutton.setBackgroundResource(R.drawable.mic_pressed);
                                    micbutton.setChecked(true);
                                    micbutton.setClickable(true);
                                    WificarActivity.this.isZero = 0;
                                    break;
                                }
                            }
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* loaded from: classes.dex */
    public class MyThread implements Runnable {
        public MyThread() {
        }

        @Override // java.lang.Runnable
        public void run() {
            while (WificarActivity.this.bThreadRun) {
                WificarActivity.this.IP = WificarActivity.this.wifiCar.getHost();
                WificarActivity.this.Port = String.valueOf(WificarActivity.this.wifiCar.getPort());
                WificarActivity.this.version = WifiCar.getVersion(WificarActivity.instance);
                WificarActivity.this.firmwareVersion = WificarActivity.this.wifiCar.getFilewareVersion();
                if (!WificarActivity.this.firmwareVersion.equals("")) {
                    WificarActivity.this.firmwareVersion = "1.0";
                } else if (WificarActivity.this.firmwareVersion.equals("")) {
                    WificarActivity.this.firmwareVersion = " ";
                }
                try {
                    WificarActivity.this.SSID = WificarActivity.this.wifiCar.getSSID();
                    AppLog.i("zhang", "获取到了SSID：" + WificarActivity.this.SSID);
                    WificarActivity.this.bThreadRun = false;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void lockOrientation() {
        this.orientationLock = true;
    }

    public void releaseOrientation() {
        this.orientationLock = false;
    }

    public boolean enableGSensorControl() {
        this.isGsensor = false;
        this.LRshow = true;
        this.gSensorControlEnable = true;
        this.LR = 0;
        this.accDefaultX = this.fBaseDefault;
        this.accDefaultY = this.fBaseDefault;
        this.mSensorManager.registerListener(this.myAccelerometerListener, this.aSensor, 2);
        this.mSensorManager.registerListener(this.myMagneticFieldListener, this.mfSensor, 2);
        this.accDefaultX = this.fBaseDefault;
        this.accDefaultY = this.fBaseDefault;
        return false;
    }

    public boolean isgSensorModeEnable() {
        ToggleButton gSensorTogglebutton = (ToggleButton) findViewById(R.id.g_sensor_toggle_button);
        return gSensorTogglebutton.isChecked();
    }

    public void disablegSensorMode() {
        ToggleButton gSensorTogglebutton = (ToggleButton) findViewById(R.id.g_sensor_toggle_button);
        gSensorTogglebutton.setBackgroundResource(R.drawable.g);
        disableGSensorControl();
    }

    public boolean isSpeaking() {
        ToggleButton speakingbutton = (ToggleButton) findViewById(R.id.mic_toggle_button);
        return speakingbutton.isChecked();
    }

    public boolean istaking() {
        Button takePictureBtn = (Button) findViewById(R.id.take_picture_button);
        return takePictureBtn.isClickable();
    }

    public void disablegSpeaking() {
        Button speakingbutton = (Button) findViewById(R.id.mic_toggle_button);
        speakingbutton.setBackgroundResource(R.drawable.mic);
        speakingbutton.setClickable(false);
        try {
            this.wifiCar.disableAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disablePlayMode() {
        Toast toast = Toast.makeText(instance, (int) R.string.complete_play, 0);
        toast.show();
        Button playTogglebutton = (Button) findViewById(R.id.play_toggle_button);
        Button button = (Button) findViewById(R.id.take_picture_button);
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.video_toggle_button);
        this.isPlayModeEnable = false;
        if (this.playTimer != null) {
            this.playTimer.cancel();
            this.playTimer = null;
        }
        if (this.replayTimer != null) {
            this.replayTimer.cancel();
            this.replayTimer = null;
        }
        try {
            this.wifiCar.stopPlayTrack();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playTogglebutton.setBackgroundResource(R.drawable.play_path);
        AppLog.i("aaa", "zdf");
        this.flag = 0;
        this.j = 0;
        this.k = 0;
        this.s = 0;
    }

    public void checkSDcard() {
        SDcardCheck.creatSDcardCheckDialog(instance).show();
        if (this.checkTimer != null) {
            this.checkTimer.cancel();
            this.checkTimer = null;
        }
    }

    public boolean disableGSensorControl() {
        this.gSensorControlEnable = false;
        this.handler.removeCallbacks(this.gMovingTask);
        releaseOrientation();
        this.accDefaultX = this.fBaseDefault;
        this.accDefaultY = this.fBaseDefault;
        try {
            this.wifiCar.move(0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.wifiCar.move(1, 0);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        this.mSensorManager.unregisterListener(this.myAccelerometerListener);
        this.fBasePitch = this.fBaseDefault;
        this.fBaseRoll = this.fBaseDefault;
        this.appLeft_MoveCar.Hided(0);
        this.appRight_MoveCar.Hided(0);
        this.accDefaultX = this.fBaseDefault;
        this.accDefaultY = this.fBaseDefault;
        return true;
    }

    /* loaded from: classes.dex */
    class GMovingTask extends TimerTask {
        public GMovingTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            if (WificarActivity.this.fMagneticFieldValues != null && WificarActivity.this.fAccelerometerValues != null) {
                float[] values = new float[3];
                float[] R = new float[9];
                float[] outR = new float[9];
                SensorManager.getRotationMatrix(R, null, WificarActivity.this.fAccelerometerValues, WificarActivity.this.fMagneticFieldValues);
                if (!WificarActivity.this.bIsPortait) {
                    SensorManager.remapCoordinateSystem(R, 2, 3, outR);
                } else {
                    outR = R;
                }
                SensorManager.getOrientation(outR, values);
                values[0] = (float) Math.toDegrees(values[0]);
                values[1] = (float) Math.toDegrees(values[1]);
                values[2] = (float) Math.toDegrees(values[2]);
                float fPitch = (float) Math.floor(values[1]);
                float fRoll = (float) Math.floor(values[2]);
                AppLog.d("gsensor", "pitch:" + fPitch + ",roll" + fRoll);
                AppLog.d("gsensor", "fBasePitch:" + WificarActivity.this.fBasePitch);
                AppLog.d("gsensor", "fBaseDefault:" + WificarActivity.this.fBaseDefault);
                if (WificarActivity.this.fBasePitch == WificarActivity.this.fBaseDefault || WificarActivity.this.fBaseRoll == WificarActivity.this.fBaseDefault) {
                    WificarActivity.this.fBasePitch = fPitch;
                    WificarActivity.this.fBaseRoll = fRoll;
                    AppLog.d("gsensor", "reset============================");
                    return;
                }
                float fVer = fPitch - WificarActivity.this.fBasePitch;
                float fHor = fRoll - WificarActivity.this.fBaseRoll;
                if (fVer > 15 + WificarActivity.this.stickRadiu) {
                    fVer = 15 + WificarActivity.this.stickRadiu;
                } else if (fVer < (-(15 + WificarActivity.this.stickRadiu))) {
                    fVer = -(15 + WificarActivity.this.stickRadiu);
                }
                if (fVer < 15 && fVer > (-15)) {
                    fVer = 0.0f;
                } else if (fVer >= 15) {
                    fVer -= 15;
                } else if (fVer <= (-15)) {
                    fVer += 15;
                }
                if (fHor > 15 + WificarActivity.this.stickRadiu) {
                    fHor = 15 + WificarActivity.this.stickRadiu;
                } else if (fHor < (-(15 + WificarActivity.this.stickRadiu))) {
                    fHor = -(15 + WificarActivity.this.stickRadiu);
                }
                if (fHor < 15 && fHor > (-15)) {
                    fHor = 0.0f;
                } else if (fHor >= 15) {
                    fHor -= 15;
                } else if (fHor <= (-15)) {
                    fHor += 15;
                }
                AppLog.d("gsensor", "fVer:" + fVer);
                WificarActivity.this.setDirection(fHor, fVer);
            }
        }
    }

    public int getControllerType() {
        return this.controllerType;
    }

    public CameraSurfaceView getCameraSurfaceView() {
        return this.cameraSurfaceView;
    }

    public void setControllerType(int controllerType) {
        this.controllerType = controllerType;
    }

    public static boolean isVideoRecord() {
        return videoRecordEnable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDirection(float x, float y) {
        double radians;
        int iSpeedR;
        int iSpeedL;
        WindowManager mWindowManager = (WindowManager) getSystemService("window");
        Display mDisplay = mWindowManager.getDefaultDisplay();
        if (mDisplay.getOrientation() == 0) {
            if (y == 0.0f) {
                radians = x;
                AppLog.i("zhang", "radians y=0 :" + radians);
            } else if (x == 0.0f) {
                radians = y;
                AppLog.i("zhang", "radians x=0 :" + radians);
            } else {
                radians = Math.atan(Math.abs(y) / Math.abs(x));
                AppLog.i("zhang", "radians else :" + radians);
            }
            double angle = radians * 57.29577951308232d;
            AppLog.i("gsensor", "angle : " + angle);
            int iSpeed = (int) Math.ceil((Math.pow(Math.pow(x, 2.0d) + Math.pow(y, 2.0d), 0.5d) / this.stickRadiu) * 10.0d);
            AppLog.i("zhang", "iSpeed :" + iSpeed);
            AppLog.i("zhang", "x :" + x + "y : " + y);
            if (x != 0.0f || y == 0.0f) {
                if (x == 0.0f || y != 0.0f) {
                    if (x > 0.0f && y > 0.0f) {
                        iSpeedR = angle >= 67.5d ? iSpeed : angle < 22.5d ? -iSpeed : iSpeed;
                        iSpeedL = 0;
                    } else if (x < 0.0f && y > 0.0f) {
                        iSpeedL = angle >= 67.5d ? iSpeed : angle < 22.5d ? -iSpeed : 0;
                        iSpeedR = iSpeed;
                    } else if (x >= 0.0f || y >= 0.0f) {
                        AppLog.i("zhang", "else x and y :" + x + " ," + y);
                        iSpeedR = -iSpeed;
                        if (angle >= 67.5d) {
                            iSpeedL = iSpeedR;
                        } else if (angle < 22.5d) {
                            iSpeedL = -iSpeedR;
                        } else {
                            iSpeedL = 0;
                            iSpeedR = -iSpeed;
                        }
                    } else {
                        iSpeedL = -iSpeed;
                        if (angle >= 67.5d) {
                            iSpeedR = iSpeedL;
                        } else if (angle < 22.5d) {
                            iSpeedR = -iSpeedL;
                        } else {
                            iSpeedL = -iSpeed;
                            iSpeedR = 0;
                        }
                    }
                } else if (x > 0.0f) {
                    iSpeedL = -iSpeed;
                    iSpeedR = iSpeed;
                } else {
                    iSpeedL = iSpeed;
                    iSpeedR = -iSpeed;
                }
            } else if (y > 0.0f) {
                iSpeedL = iSpeed;
                iSpeedR = iSpeed;
            } else {
                iSpeedL = -iSpeed;
                iSpeedR = -iSpeed;
            }
            if (iSpeedL > 10) {
                iSpeedL = 10;
            }
            if (iSpeedL < -10) {
                iSpeedL = -10;
            }
            if (iSpeedR > 10) {
                iSpeedR = 10;
            }
            if (iSpeedR < -10) {
                iSpeedR = -10;
            }
            AppLog.i("zhang", "the speed isSpeedL is :" + iSpeedL);
            AppLog.i("zhang", "the speed isSpeedR is :" + iSpeedR);
            if (iSpeedL == 0 && this.iCarSpeedL != 0) {
                try {
                    this.wifiCar.move(0, iSpeedL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (iSpeedR == 0 && this.iCarSpeedR != 0) {
                try {
                    this.wifiCar.move(1, iSpeedR);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            this.iCarSpeedL = iSpeedL;
            this.iCarSpeedR = iSpeedR;
            AppLog.i("zhang", "the speed iCarSpeedL is :" + this.iCarSpeedL);
            AppLog.i("zhang", "the speed iCarSpeedR is :" + this.iCarSpeedR);
            if (this.iCarSpeedL != 0) {
                try {
                    this.wifiCar.move(0, this.iCarSpeedL);
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (this.iCarSpeedR != 0) {
                try {
                    this.wifiCar.move(1, this.iCarSpeedR);
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        }
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        try {
            if (!this.orientationLock && !this.gSensorControlEnable) {
                this.cameraSurfaceView.destroyDrawingCache();
                setRequestedOrientation(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onConfigurationChanged(newConfig);
    }

    private void changeLandscape() {
        this.controllerType = WificarUtility.getIntVariable(instance, WificarUtility.CONTROLLER_TYPE, 1);
        if (isTablet(this)) {
            if (this.screenSize < 5.8d) {
                this.isPad = false;
                AppLog.e(this.TAG, "is Phone");
                setContentView(R.layout.double_axis_landscape);
            } else {
                this.isPad = true;
                AppLog.e(this.TAG, "is Pad");
                setContentView(R.layout.double_axis_landscape_large);
            }
        } else {
            this.isPad = false;
            AppLog.e(this.TAG, "is Phone2");
            setContentView(R.layout.double_axis_landscape);
        }
        this.wificarNewLayoutParams = WificarNewLayoutParams.getWificarNewLayoutParams(instance);
        this.Parent = (RelativeLayout) findViewById(R.id.parentLayout);
        this.appLeft_MoveCar = new AppLeft_MoveCar(this);
        this.appLeft_MoveCar.setId(_R.id.Car_Left_id);
        this.appRight_MoveCar = new AppRight_MoveCar(this);
        this.appRight_MoveCar.setId(_R.id.Car_Right_id);
        if (this.isPad) {
            this.shareButton = new Button(getApplicationContext());
            this.shareButton.setId(_R.id.share_btn_id);
            this.shareButton.setBackgroundResource(R.drawable.share_off);
            this.gSensorButton = new ToggleButton(getApplicationContext());
            this.gSensorButton.setTextOn(" ");
            this.gSensorButton.setTextOff(" ");
            this.gSensorButton.setText(" ");
            this.gSensorButton.setId(_R.id.gSensor_btn_id);
            this.gSensorButton.setBackgroundResource(R.drawable.g);
            AppLog.e(this.TAG, "shareButton:" + this.shareButton + " Parent:" + this.Parent);
            this.Parent.addView(this.shareButton, this.wificarNewLayoutParams.share_button_Params);
            this.Parent.addView(this.gSensorButton, this.wificarNewLayoutParams.gSensor_button_Params);
        }
        this.Parent.addView(this.appLeft_MoveCar, this.wificarNewLayoutParams.car_Left_Move_Params);
        this.Parent.addView(this.appRight_MoveCar, this.wificarNewLayoutParams.car_Right_Move_Params);
        this.camera_UD_SurfaceView = (Camera_UD_SurfaceView) findViewById(R.id.Car_Camera_UD_SurfaceView);
        this.camera_UD_SurfaceView.setZOrderOnTop(true);
        this.camera_UD_SurfaceView.disableControl();
        this.camera_UD_SurfaceView.setWifiCar(this.wifiCar);
        this.cameraSurfaceView = (CameraSurfaceView) findViewById(R.id.car_camera_surfaceview);
        this.wifiCar.setSurfaceView(this.cameraSurfaceView);
        refreshUIListener();
        this.bIsPortait = false;
    }

    private void refreshUIListener() {
        this.appLeft_MoveCar.setAppLeft_MoveCarListener(new AppLeft_MoveCar.AppLeft_MoveCarListener() { // from class: com.wificar.WificarActivity.13
            @Override // com.wificar.view.AppLeft_MoveCar.AppLeft_MoveCarListener
            public void onSteeringWheelChanged(int action, int flag) {
                if (action == 1) {
                    WificarActivity.this.wifiCar.enableMoveFlag();
                    WificarActivity.this.LMoving = true;
                    WificarActivity.this.handler.postDelayed(WificarActivity.this.LMovingTask, 100L);
                    if (WificarActivity.this.isPlayModeEnable) {
                        WificarActivity.this.sendMessage(WificarActivity.MESSAGE_STOP_PLAY);
                    }
                    if (flag == 1) {
                        AppLog.e(WificarActivity.this.TAG, "左边前");
                        WificarActivity.this.LControlFlag = 1;
                        try {
                            WificarActivity.this.wifiCar.move(0, 10);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (flag == 2) {
                        AppLog.e(WificarActivity.this.TAG, "左边后");
                        WificarActivity.this.LControlFlag = 2;
                        try {
                            WificarActivity.this.wifiCar.move(0, -10);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                } else if (action == 3) {
                    WificarActivity.this.LMoving = false;
                    WificarActivity.this.handler.removeCallbacks(WificarActivity.this.LMovingTask);
                    WificarActivity.this.wifiCar.disableMoveFlag();
                    try {
                        WificarActivity.this.wifiCar.move(0, 0);
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        });
        this.appRight_MoveCar.setAppRight_MoveCarListener(new AppRight_MoveCar.AppRight_MoveCarListener() { // from class: com.wificar.WificarActivity.14
            @Override // com.wificar.view.AppRight_MoveCar.AppRight_MoveCarListener
            public void onSteeringWheelChanged(int action, int flag) {
                if (action == 1) {
                    WificarActivity.this.wifiCar.enableMoveFlag();
                    WificarActivity.this.RMoving = true;
                    WificarActivity.this.handler.postDelayed(WificarActivity.this.RMovingTask, 100L);
                    if (WificarActivity.this.isPlayModeEnable) {
                        WificarActivity.this.sendMessage(WificarActivity.MESSAGE_STOP_PLAY);
                    }
                    if (flag == 1) {
                        AppLog.e(WificarActivity.this.TAG, "右边前");
                        WificarActivity.this.RControlFlag = 1;
                        try {
                            WificarActivity.this.wifiCar.move(1, 10);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (flag == 2) {
                        AppLog.e(WificarActivity.this.TAG, "右边后");
                        WificarActivity.this.RControlFlag = 2;
                        try {
                            WificarActivity.this.wifiCar.move(1, -10);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                } else if (action == 3) {
                    AppLog.e(WificarActivity.this.TAG, "右边停止");
                    WificarActivity.this.RMoving = false;
                    WificarActivity.this.handler.removeCallbacks(WificarActivity.this.RMovingTask);
                    WificarActivity.this.wifiCar.disableMoveFlag();
                    try {
                        WificarActivity.this.wifiCar.move(1, 0);
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        });
        Button zoomInBtn = (Button) findViewById(R.id.zoom_in_button);
        zoomInBtn.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.15
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    try {
                        WificarActivity.this.cameraSurfaceView.zoomIn();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int value = (int) WificarActivity.this.cameraSurfaceView.getTargetZoomValue();
                    TextView valueText = (TextView) WificarActivity.this.findViewById(R.id.screen_ratio_textview);
                    valueText.setText(String.valueOf(String.valueOf(value)) + "%");
                }
            }
        });
        Button zoomOutBtn = (Button) findViewById(R.id.zoom_out_button);
        zoomOutBtn.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.16
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    try {
                        WificarActivity.this.cameraSurfaceView.zoomOut();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int value = (int) WificarActivity.this.cameraSurfaceView.getTargetZoomValue();
                    TextView valueText = (TextView) WificarActivity.this.findViewById(R.id.screen_ratio_textview);
                    valueText.setText(String.valueOf(String.valueOf(value)) + "%");
                }
            }
        });
        final Button takePictureBtn = (Button) findViewById(R.id.take_picture_button);
        takePictureBtn.setOnTouchListener(new View.OnTouchListener() { // from class: com.wificar.WificarActivity.17
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (WificarActivity.this.succeedConnect) {
                    if (event.getAction() == 0) {
                        if (WificarActivity.this.isPad) {
                            takePictureBtn.setBackgroundResource(R.drawable.camera_pressed);
                        } else {
                            takePictureBtn.setBackgroundResource(R.drawable.camera_pressed1);
                        }
                        if (WificarActivity.this.No_Sdcard) {
                            Toast toast = Toast.makeText(WificarActivity.instance, (int) R.string.record_fail_warning, 0);
                            toast.show();
                        } else {
                            WificarActivity.this.sendMessage(WificarActivity.MESSAGE_TAKE_PHOTO);
                        }
                    } else if (event.getAction() == 1) {
                        if (WificarActivity.this.isPad) {
                            takePictureBtn.setBackgroundResource(R.drawable.camera);
                        } else {
                            takePictureBtn.setBackgroundResource(R.drawable.camera1);
                        }
                    }
                }
                return false;
            }
        });
        final ToggleButton ledTogglebutton = (ToggleButton) findViewById(R.id.led_toggle_button);
        ledTogglebutton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.18
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    if (ledTogglebutton.isChecked()) {
                        try {
                            WificarActivity.this.wifiCar.led_onTrack();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ledTogglebutton.setBackgroundResource(R.drawable.led_on);
                        return;
                    }
                    WificarActivity.instance.getSharedPreferences("WIFICAR_PREFS", 0);
                    try {
                        WificarActivity.this.wifiCar.led_offTrack();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    ledTogglebutton.setBackgroundResource(R.drawable.led);
                }
            }
        });
        final ImageView soundImg = (ImageView) findViewById(R.id.no_sound);
        final ToggleButton micToggleButton = (ToggleButton) findViewById(R.id.mic_toggle_button);
        int enableMic = WificarUtility.getIntVariable(instance, WificarUtility.WIFICAR_MIC, 0);
        if (enableMic == 1) {
            boolean result = this.wifiCar.playAudio();
            if (result) {
                micToggleButton.setBackgroundResource(R.drawable.mic_pressed);
                micToggleButton.setChecked(true);
            }
        } else {
            boolean result2 = false;
            try {
                result2 = this.wifiCar.disableAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result2) {
                micToggleButton.setBackgroundResource(R.drawable.mic);
                micToggleButton.setChecked(false);
            }
        }
        micToggleButton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.19
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    if (micToggleButton.isChecked()) {
                        boolean result3 = WificarActivity.this.wifiCar.playAudio();
                        if (result3) {
                            WificarActivity.this.checkSound = new Timer(true);
                            WificarActivity.this.checkSound.schedule(new SoundCheck(), 100L, 300L);
                            WificarActivity.this.setting = 0;
                            WificarActivity.this.audio_play = 1;
                            if (WificarActivity.this.isZero == 1) {
                                AudioManager mAudioManager = (AudioManager) WificarActivity.this.getSystemService("audio");
                                mAudioManager.setStreamVolume(3, 2, 0);
                            }
                            WificarUtility.getIntVariable(WificarActivity.instance, WificarUtility.WIFICAR_MIC, 1);
                            micToggleButton.setBackgroundResource(R.drawable.mic_pressed);
                            return;
                        }
                        return;
                    }
                    boolean result4 = false;
                    try {
                        result4 = WificarActivity.this.wifiCar.disableAudio();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    if (result4) {
                        if (WificarActivity.this.checkSound != null) {
                            WificarActivity.this.checkSound.cancel();
                            WificarActivity.this.checkSound = null;
                        }
                        soundImg.setVisibility(0);
                        WificarActivity.this.audio_play = 0;
                        WificarUtility.getIntVariable(WificarActivity.instance, WificarUtility.WIFICAR_MIC, 1);
                        micToggleButton.setBackgroundResource(R.drawable.mic);
                    }
                }
            }
        });
        final ToggleButton lightTogglebutton = (ToggleButton) findViewById(R.id.light_toggle_button);
        int enableIr = WificarUtility.getIntVariable(instance, WificarUtility.WIFICAR_IR, 0);
        if (enableIr == 1) {
            try {
                this.wifiCar.enableIR();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            lightTogglebutton.setBackgroundResource(R.drawable.ir_pressed);
            lightTogglebutton.setChecked(true);
        } else {
            try {
                this.wifiCar.disableIR();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            lightTogglebutton.setBackgroundResource(R.drawable.ir);
            lightTogglebutton.setChecked(false);
        }
        lightTogglebutton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.20
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    if (lightTogglebutton.isChecked()) {
                        WificarUtility.getIntVariable(WificarActivity.instance, WificarUtility.WIFICAR_IR, 1);
                        try {
                            WificarActivity.this.wifiCar.led_offTrack();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                        ledTogglebutton.setBackgroundResource(R.drawable.led);
                        ledTogglebutton.setClickable(false);
                        ledTogglebutton.setChecked(false);
                        try {
                            WificarActivity.this.wifiCar.enableIR();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                        lightTogglebutton.setBackgroundResource(R.drawable.ir_pressed);
                        return;
                    }
                    WificarUtility.getIntVariable(WificarActivity.instance, WificarUtility.WIFICAR_IR, 0);
                    ledTogglebutton.setBackgroundResource(R.drawable.led);
                    ledTogglebutton.setClickable(true);
                    ledTogglebutton.setChecked(false);
                    try {
                        WificarActivity.this.wifiCar.disableIR();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                    lightTogglebutton.setBackgroundResource(R.drawable.ir);
                }
            }
        });
        if (this.isPad) {
            this.gSensorButton.setOnClickListener(instance);
        } else {
            this.gSensorButton = (ToggleButton) findViewById(R.id.g_sensor_toggle_button);
            this.gSensorButton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.21
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    if (WificarActivity.this.succeedConnect) {
                        if (WificarActivity.this.gSensorButton.isChecked()) {
                            if (WificarActivity.this.isPlayModeEnable) {
                                WificarActivity.this.disablePlayMode();
                            }
                            WificarActivity.this.gSensorButton.setBackgroundResource(R.drawable.g_pressed);
                            WificarActivity.this.enableGSensorControl();
                            return;
                        }
                        WificarActivity.this.gSensorButton.setBackgroundResource(R.drawable.g);
                        WificarActivity.this.disableGSensorControl();
                    }
                }
            });
        }
        final Button playTogglebutton = (Button) findViewById(R.id.play_toggle_button);
        final ToggleButton recordTogglebutton = (ToggleButton) findViewById(R.id.record_toggle_button);
        recordTogglebutton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.22
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    if (recordTogglebutton.isChecked()) {
                        if (WificarActivity.this.isPlayModeEnable) {
                            WificarActivity.this.disablePlayMode();
                        }
                        if (WificarActivity.this.isLowPower) {
                            Disrecord_play_dialog.createdisaenableDialog(WificarActivity.instance).show();
                            recordTogglebutton.setChecked(false);
                            return;
                        }
                        Message messageStopRecord = new Message();
                        messageStopRecord.what = WificarActivity.MESSAGE_START_RECORD;
                        WificarActivity.getInstance().getHandler().sendMessage(messageStopRecord);
                        return;
                    }
                    Message messageStopRecord2 = new Message();
                    messageStopRecord2.what = WificarActivity.MESSAGE_STOP_RECORD;
                    WificarActivity.getInstance().getHandler().sendMessage(messageStopRecord2);
                }
            }
        });
        final GestureDetector detector = new GestureDetector(new GestureDetector.OnGestureListener() { // from class: com.wificar.WificarActivity.23
            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onSingleTapUp(MotionEvent e4) {
                return false;
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public void onShowPress(MotionEvent e4) {
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onScroll(MotionEvent e1, MotionEvent e22, float distanceX, float distanceY) {
                return false;
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public void onLongPress(MotionEvent e4) {
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onFling(MotionEvent e1, MotionEvent e22, float velocityX, float velocityY) {
                return false;
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onDown(MotionEvent e4) {
                return false;
            }
        });
        detector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() { // from class: com.wificar.WificarActivity.24
            @Override // android.view.GestureDetector.OnDoubleTapListener
            public boolean onSingleTapConfirmed(MotionEvent e4) {
                if (WificarActivity.this.isLowPower) {
                    Disrecord_play_dialog.createdisaenableDialog(WificarActivity.instance).show();
                } else if (WificarActivity.this.succeedConnect) {
                    if (WificarActivity.this.flag == 0) {
                        WificarActivity.this.isPlayModeEnable = true;
                        try {
                            WificarActivity.this.wifiCar.startPlayTrack();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        playTogglebutton.setBackgroundResource(R.drawable.play_path_pressed);
                        SharedPreferences share_time_play_s = WificarActivity.this.getSharedPreferences(WificarActivity.this.RECORD_TIME, 0);
                        WificarActivity.this.recordTimeLength1 = share_time_play_s.getLong("record", WificarActivity.this.recordTimeLength);
                        AppLog.i("aaa", "recordTimeLength: " + WificarActivity.this.recordTimeLength1);
                        WificarActivity.this.playTimer = new Timer(true);
                        WificarActivity.this.playTimer.schedule(new PlayTask(), WificarActivity.this.recordTimeLength1);
                        if (WificarActivity.this.isgSensorModeEnable()) {
                            WificarActivity.this.disablegSensorMode();
                        }
                        AppLog.i("aaa", "s");
                        WificarActivity.this.flag = 1;
                    } else if (WificarActivity.this.flag != 0) {
                        try {
                            WificarActivity.this.wifiCar.stopPlayTrack();
                        } catch (IOException e12) {
                            e12.printStackTrace();
                        }
                        Message messageStopPlay = new Message();
                        messageStopPlay.what = WificarActivity.MESSAGE_STOP_PLAY;
                        WificarActivity.getInstance().getHandler().sendMessage(messageStopPlay);
                        AppLog.i("aaa", "sf");
                        WificarActivity.this.flag = 0;
                    } else if (WificarActivity.this.flag == 2) {
                        Message messageStopPlay2 = new Message();
                        messageStopPlay2.what = WificarActivity.MESSAGE_STOP_PLAY;
                        WificarActivity.getInstance().getHandler().sendMessage(messageStopPlay2);
                        AppLog.i("aaa", "df");
                        WificarActivity.this.flag = 0;
                    }
                }
                return false;
            }

            @Override // android.view.GestureDetector.OnDoubleTapListener
            public boolean onDoubleTapEvent(MotionEvent e4) {
                return false;
            }

            @Override // android.view.GestureDetector.OnDoubleTapListener
            public boolean onDoubleTap(MotionEvent e4) {
                if (WificarActivity.this.isLowPower) {
                    Disrecord_play_dialog.createdisaenableDialog(WificarActivity.instance).show();
                    return false;
                }
                if (WificarActivity.this.isgSensorModeEnable()) {
                    WificarActivity.this.disablegSensorMode();
                }
                if (WificarActivity.this.succeedConnect && WificarActivity.this.flag != 2 && WificarActivity.this.flag != 1) {
                    Message messageStartRePlay = new Message();
                    messageStartRePlay.what = WificarActivity.MESSAGE_START_PLAY;
                    WificarActivity.getInstance().getHandler().sendMessage(messageStartRePlay);
                    AppLog.i("aaa", "d");
                    WificarActivity.this.flag = 2;
                    return false;
                }
                return false;
            }
        });
        playTogglebutton.setOnTouchListener(new View.OnTouchListener() { // from class: com.wificar.WificarActivity.25
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
        final Button recordAudioTogglebutton = (Button) findViewById(R.id.talk_button);
        recordAudioTogglebutton.setOnTouchListener(new View.OnTouchListener() { // from class: com.wificar.WificarActivity.26
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (WificarActivity.this.succeedConnect) {
                    if (event.getAction() == 0) {
                        soundImg.setVisibility(0);
                        recordAudioTogglebutton.setBackgroundResource(R.drawable.talk_pressed);
                        WificarActivity.instance.sendMessage(WificarActivity.MESSAGE_START_RECORD_AUDIO);
                        WificarActivity.this.isTalk = 1;
                        if (WificarActivity.this.audio_play == 1) {
                            WificarActivity.this.disablegSpeaking();
                            micToggleButton.setBackgroundResource(R.drawable.mic);
                            micToggleButton.setChecked(false);
                        }
                    } else if (event.getAction() == 1) {
                        WificarActivity.this.isTalk = 0;
                        WificarActivity.this.stop_talk = new Timer(true);
                        WificarActivity.this.stop_talk.schedule(new TimerTask() { // from class: com.wificar.WificarActivity.26.1
                            @Override // java.util.TimerTask, java.lang.Runnable
                            public void run() {
                                WificarActivity.instance.sendMessage(WificarActivity.MESSAGE_STOP_RECORD_AUDIO);
                            }
                        }, 600L);
                        if (WificarActivity.this.audio_play == 1) {
                            soundImg.setVisibility(8);
                        }
                    }
                }
                return false;
            }
        });
        if (this.isPad) {
            this.shareButton.setOnClickListener(instance);
        } else {
            this.shareButton = (Button) findViewById(R.id.share_toggle_button);
            this.shareButton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.27
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    if (WificarActivity.this.succeedConnect) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WificarActivity.this);
                        builder.setMessage("The Facebook, Twitter, Tumblr, and YouTube apps must already be installed on your device to share Rover 2.0 photos and videos.\nExit the app, go to Settings and access a Wi-Fi network other than Rover 2.0. Open the Rover 2.0 app and select Share.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.wificar.WificarActivity.27.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
                    }
                }
            });
        }
        final ToggleButton buttoncameraupon = (ToggleButton) findViewById(R.id.camup_button);
        buttoncameraupon.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.28
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    if (buttoncameraupon.isChecked()) {
                        buttoncameraupon.setBackgroundResource(R.drawable.up_on);
                        WificarActivity.this.camera_UD_SurfaceView.enableControl();
                        return;
                    }
                    buttoncameraupon.setBackgroundResource(R.drawable.up);
                    WificarActivity.this.camera_UD_SurfaceView.disableControl();
                }
            }
        });
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.mic_toggle_button);
        final ToggleButton settingbutton = (ToggleButton) findViewById(R.id.setting_button);
        settingbutton.setOnTouchListener(new View.OnTouchListener() { // from class: com.wificar.WificarActivity.29
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0) {
                    settingbutton.setBackgroundResource(R.drawable.setting_on);
                    WificarActivity.this.setDialog();
                    return false;
                } else if (event.getAction() == 1) {
                    settingbutton.setBackgroundResource(R.drawable.setting);
                    return false;
                } else {
                    return false;
                }
            }
        });
        final ToggleButton videoTogglebutton = (ToggleButton) findViewById(R.id.video_toggle_button);
        videoTogglebutton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.30
            private Object cameraSurfaceView;

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (WificarActivity.this.succeedConnect) {
                    if (!videoTogglebutton.isChecked()) {
                        if (WificarActivity.this.checkTimer != null) {
                            WificarActivity.this.checkTimer.cancel();
                            WificarActivity.this.checkTimer = null;
                        }
                        if (WificarActivity.this.isPad) {
                            videoTogglebutton.setBackgroundResource(R.drawable.video);
                        } else {
                            videoTogglebutton.setBackgroundResource(R.drawable.video1);
                        }
                        recordAudioTogglebutton.setEnabled(true);
                        recordAudioTogglebutton.setBackgroundResource(R.drawable.talk);
                        WificarActivity.this.stopVideo = true;
                        WificarActivity.this.handler.postDelayed(WificarActivity.this.StopVideoTask, 10L);
                        return;
                    }
                    WificarActivity.this.checkTimer = new Timer(true);
                    WificarActivity.this.checkTimer.schedule(new SDCardSizeTest(), 50L, 500L);
                    if (!WificarActivity.this.isLowPower) {
                        if (WificarActivity.this.connect_error) {
                            if (WificarActivity.this.isPad) {
                                videoTogglebutton.setBackgroundResource(R.drawable.video_on);
                                return;
                            } else {
                                videoTogglebutton.setBackgroundResource(R.drawable.video_on1);
                                return;
                            }
                        }
                        recordAudioTogglebutton.setEnabled(false);
                        recordAudioTogglebutton.setBackgroundResource(R.drawable.talk);
                        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                        String filename = date.format(Long.valueOf(System.currentTimeMillis()));
                        WificarActivity.this.FileName = String.valueOf(filename) + ".avi";
                        if (!WificarActivity.this.No_Sdcard) {
                            if (WificarActivity.this.nSDFreeSize < 100) {
                                WificarActivity.this.sdcheck = 2;
                                Message sdcardtest = new Message();
                                sdcardtest.what = WificarActivity.MESSAGE_CHECK_TEST;
                                WificarActivity.getInstance().getHandler().sendMessage(sdcardtest);
                                recordAudioTogglebutton.setEnabled(true);
                            } else {
                                WificarActivity.this.sdcheck = 1;
                                WificarActivity.this.startVideo = true;
                                WificarActivity.this.handler.postDelayed(WificarActivity.this.StartVideoTask, 10L);
                            }
                            if (WificarActivity.this.isPad) {
                                videoTogglebutton.setBackgroundResource(R.drawable.video_on);
                                return;
                            } else {
                                videoTogglebutton.setBackgroundResource(R.drawable.video_on1);
                                return;
                            }
                        }
                        WificarActivity.this.NoSDcard();
                        Toast toast = Toast.makeText(WificarActivity.instance, (int) R.string.record_fail_warning, 0);
                        toast.show();
                        recordAudioTogglebutton.setEnabled(true);
                        return;
                    }
                    Disrecordvideo_dialog.createdisaenablevideoDialog(WificarActivity.instance).show();
                    videoTogglebutton.setChecked(false);
                }
            }
        });
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        try {
            if (v == this.shareButton) {
                if (this.succeedConnect) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("The Facebook, Twitter, Tumblr, and YouTube apps must already be installed on your device to share Rover 2.0 photos and videos.\nExit the app, go to Settings and access a Wi-Fi network other than Rover 2.0. Open the Rover 2.0 app and select Share.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.wificar.WificarActivity.31
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
                }
            } else if (v == this.gSensorButton && this.succeedConnect) {
                if (this.gSensorButton.isChecked()) {
                    if (this.isPlayModeEnable) {
                        disablePlayMode();
                    }
                    this.gSensorButton.setBackgroundResource(R.drawable.g_pressed);
                    enableGSensorControl();
                    return;
                }
                this.gSensorButton.setBackgroundResource(R.drawable.g);
                disableGSensorControl();
            }
        } catch (Exception e) {
        }
    }

    public boolean isPortait() {
        return this.bIsPortait;
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        String statement = getResources().getString(R.string.click_again_to_exit_the_program);
        long pressTime = System.currentTimeMillis();
        if (pressTime - this.lastPressTime <= 2000) {
            if (!this.No_Sdcard) {
                DeleVideo();
            }
            deleIndexVideo();
            sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file:" + this.DirectPath)));
            pause();
            exit();
            try {
                this.wifiCar.stopPlayTrack();
            } catch (IOException e) {
                e.printStackTrace();
            }
            disableGSensorControl();
        } else {
            Toast toast = Toast.makeText(this, statement, 0);
            toast.show();
        }
        this.lastPressTime = pressTime;
    }

    public void setDialog() {
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.setting_button);
        final Dialog dialog = new Dialog(instance, R.style.my_dialog);
        dialog.setContentView(R.layout.setting_info);
        this.Okbutton = (Button) dialog.findViewById(R.id.OkButton);
        this.tIP = (EditText) dialog.findViewById(R.id.EditText_IP);
        this.tPort = (EditText) dialog.findViewById(R.id.EditText_PORT);
        this.tdevice = (TextView) dialog.findViewById(R.id.TextView_D);
        this.tfirmware = (TextView) dialog.findViewById(R.id.TextView_F);
        this.tsoftware = (TextView) dialog.findViewById(R.id.TextView_S);
        this.Okbutton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.WificarActivity.32
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                WificarActivity.this.Okbutton.setBackgroundResource(R.drawable.ok_off);
                dialog.dismiss();
            }
        });
        dialog.show();
        this.tIP.setText(this.IP);
        this.tIP.setClickable(false);
        this.tPort.setText(this.Port);
        this.tPort.setClickable(false);
        this.tdevice.setText(this.SSID);
        this.tfirmware.setText(this.firmwareVersion);
        this.tsoftware.setText(this.version);
    }

    /* loaded from: classes.dex */
    class RecordTask extends TimerTask {
        RecordTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            WificarActivity.this.recordTimeLength = WificarActivity.this.startRecordTimeStamp - System.currentTimeMillis();
            Message messageStopRecord = new Message();
            messageStopRecord.what = WificarActivity.MESSAGE_STOP_RECORD;
            WificarActivity.getInstance().getHandler().sendMessage(messageStopRecord);
        }
    }

    /* loaded from: classes.dex */
    class PlayTask extends TimerTask {
        PlayTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Message messageStopPlay = new Message();
            messageStopPlay.what = WificarActivity.MESSAGE_STOP_PLAY;
            WificarActivity.getInstance().getHandler().sendMessage(messageStopPlay);
        }
    }

    /* loaded from: classes.dex */
    class rePlayTask extends TimerTask {
        rePlayTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            WificarActivity.this.replay_flag = 1;
            WificarActivity.this.replay_start = System.currentTimeMillis();
            Message messageStartRePlay = new Message();
            messageStartRePlay.what = WificarActivity.MESSAGE_START_PLAY;
            WificarActivity.getInstance().getHandler().sendMessage(messageStartRePlay);
            WificarActivity.this.j = 0;
            WificarActivity.this.k = 0;
            WificarActivity.this.s = 0;
            AppLog.i("aaa", "replay");
        }
    }

    public void setRecordStartTime() {
        this.recordTime = 0L;
        this.recordStartTime = System.currentTimeMillis();
    }

    public void setRecordEndTime() {
        this.recordTime = System.currentTimeMillis() - this.recordStartTime;
        if (this.recordTime > 20000) {
            this.recordTime = 20000L;
        }
        this.recordStartTime = 0L;
    }

    public long getRecordTime() {
        return this.recordTime;
    }

    public void convertBitmapToJPG(Bitmap bitmap) {
        String szUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, String.valueOf(System.currentTimeMillis()) + ".jpg", String.valueOf(System.currentTimeMillis()) + ".jpg");
        AppLog.i("zhahg", "szUrl :" + szUrl);
        try {
            Uri uri = Uri.parse(szUrl);
            String[] proj = {"_data"};
            Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow("_data");
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
            Uri uri2 = Uri.parse("file://" + img_path);
            AppLog.i("zhang", "uri :" + uri2);
            AppLog.i("zhang", "img_path :" + img_path);
            sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file:" + this.DirectPath)));
            Toast pictureOK = Toast.makeText(instance, MessageUtility.MESSAGE_TAKE_PHOTO_SUCCESSFULLY, 0);
            pictureOK.show();
        } catch (Exception e) {
            Toast pictureFAIL = Toast.makeText(instance, MessageUtility.MESSAGE_TAKE_PHOTO_FAIL, 0);
            pictureFAIL.show();
            e.printStackTrace();
        }
    }

    public void saveMyBitmap(String bitName, Bitmap mBitmap) {
        String photopath = String.valueOf(this.DirectPath) + "/Pictures";
        File file = new File(photopath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(String.valueOf(this.DirectPath) + "/Pictures/" + bitName + ".jpg");
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
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        try {
            fOut.close();
            String szUrl = String.valueOf(this.DirectPath) + "/Pictures/" + bitName + ".jpg";
            AppLog.i("zhahg", "szUrl :" + szUrl);
            try {
                Uri.parse(szUrl);
                String img_path = String.valueOf(this.DirectPath) + "/Pictures/" + bitName + ".jpg";
                Uri uri = Uri.parse("file://" + img_path);
                AppLog.i("zhang", "uri :" + uri);
                AppLog.i("zhang", "img_path :" + img_path);
                sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", uri));
                Toast pictureOK = Toast.makeText(instance, MessageUtility.MESSAGE_TAKE_PHOTO_SUCCESSFULLY, 0);
                pictureOK.show();
            } catch (Exception e4) {
                Toast pictureFAIL = Toast.makeText(instance, MessageUtility.MESSAGE_TAKE_PHOTO_FAIL, 0);
                pictureFAIL.show();
                e4.printStackTrace();
            }
        } catch (IOException e5) {
            e5.printStackTrace();
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.d("wild0", "onActivityResult:controllerType:" + this.controllerType);
        int i = getResources().getConfiguration().orientation;
        ToggleButton lightTogglebutton = (ToggleButton) findViewById(R.id.light_toggle_button);
        int enableIr = WificarUtility.getIntVariable(instance, WificarUtility.WIFICAR_IR, 0);
        if (enableIr == 1) {
            try {
                this.wifiCar.enableIR();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lightTogglebutton.setBackgroundResource(R.drawable.ir_pressed);
        } else {
            try {
                this.wifiCar.disableIR();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            lightTogglebutton.setBackgroundResource(R.drawable.ir);
        }
        ToggleButton micToggleButton = (ToggleButton) findViewById(R.id.mic_toggle_button);
        int enableMic = WificarUtility.getIntVariable(instance, WificarUtility.WIFICAR_MIC, 0);
        if (enableMic == 1) {
            this.wifiCar.playAudio();
            micToggleButton.setBackgroundResource(R.drawable.mic_pressed);
        } else {
            try {
                this.wifiCar.disableAudio();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            micToggleButton.setBackgroundResource(R.drawable.mic);
        }
        this.isNotExit = false;
    }

    /* loaded from: classes.dex */
    public class SoundCheck extends TimerTask {
        public SoundCheck() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            WificarActivity.this.sendMessage(WificarActivity.MESSAGE_SOUND);
        }
    }

    /* loaded from: classes.dex */
    class ConnectOut extends TimerTask {
        ConnectOut() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            WificarActivity.this.ConnnectOut_timer.cancel();
            WificarActivity.this.ConnnectOut_timer = null;
            if (WificarActivity.this.isConnectOut == 0) {
                WificarActivity.this.isConnectOut = 3;
                Message messageConnectFail = new Message();
                messageConnectFail.what = WificarActivity.MESSAGE_CONNECT_TO_CAR_FAIL;
                WificarActivity.this.handler.sendMessage(messageConnectFail);
                AppLog.i("connect_status", "--->connect:" + WificarActivity.this.isConnectOut);
            }
        }
    }

    /* loaded from: classes.dex */
    public class SDCardSizeTest extends TimerTask {
        public SDCardSizeTest() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            String sDcString = Environment.getExternalStorageState();
            if (sDcString.equals("mounted")) {
                File pathFile = Environment.getExternalStorageDirectory();
                StatFs statfs = new StatFs(pathFile.getPath());
                long nTotalBlocks = statfs.getBlockCount();
                long nBlocSize = statfs.getBlockSize();
                long nAvailaBlock = statfs.getAvailableBlocks();
                statfs.getFreeBlocks();
                long j = ((nTotalBlocks * nBlocSize) / 1024) / 1024;
                WificarActivity.this.nSDFreeSize = ((nAvailaBlock * nBlocSize) / 1024) / 1024;
                if (WificarActivity.this.nSDFreeSize < 100 && WificarActivity.this.sdcheck == 1) {
                    WificarActivity.this.sdcheck = 0;
                    Message sdcardtest = new Message();
                    sdcardtest.what = WificarActivity.MESSAGE_CHECK_TEST;
                    WificarActivity.getInstance().getHandler().sendMessage(sdcardtest);
                }
            }
        }
    }

    public void openFile(File f) {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268435456);
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(f), "image/jpeg");
        startActivityForResult(intent, 5);
    }

    public void video() {
        ToggleButton videoTogglebutton = (ToggleButton) findViewById(R.id.video_toggle_button);
        if (this.isPad) {
            videoTogglebutton.setBackgroundResource(R.drawable.video);
        } else {
            videoTogglebutton.setBackgroundResource(R.drawable.video1);
        }
        videoTogglebutton.setChecked(false);
    }

    public void NoSDcard() {
        ToggleButton videoTogglebutton = (ToggleButton) findViewById(R.id.video_toggle_button);
        if (this.isPad) {
            videoTogglebutton.setBackgroundResource(R.drawable.video);
        } else {
            videoTogglebutton.setBackgroundResource(R.drawable.video1);
        }
        videoTogglebutton.setChecked(false);
    }

    public void play_audio() {
        ToggleButton micTogglebutton = (ToggleButton) findViewById(R.id.mic_toggle_button);
        int enableMic = WificarUtility.getIntVariable(instance, WificarUtility.WIFICAR_MIC, 0);
        AppLog.i("zhang", "enableMic :" + enableMic);
        if (enableMic == 1) {
            boolean result = this.wifiCar.playAudio();
            if (result) {
                this.audio_play = 1;
                micTogglebutton.setBackgroundResource(R.drawable.mic_pressed);
                micTogglebutton.setChecked(true);
                return;
            }
            return;
        }
        boolean result1 = false;
        try {
            result1 = this.wifiCar.disableAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result1) {
            this.audio_play = 0;
            micTogglebutton.setBackgroundResource(R.drawable.mic);
            micTogglebutton.setChecked(false);
        }
    }

    public void setting_play() {
        ToggleButton micTogglebutton = (ToggleButton) findViewById(R.id.mic_toggle_button);
        boolean result = this.wifiCar.playAudio();
        if (result) {
            this.audio_play = 1;
            micTogglebutton.setBackgroundResource(R.drawable.mic_pressed);
            micTogglebutton.setChecked(true);
        }
        this.checkSound = new Timer(true);
        this.checkSound.schedule(new SoundCheck(), 100L, 300L);
        AudioManager mAudioManager = (AudioManager) getSystemService("audio");
        int currentVolume = mAudioManager.getStreamVolume(3);
        AppLog.i("zhang", "currentVolume :" + currentVolume);
        mAudioManager.setStreamVolume(3, currentVolume, 0);
    }

    public void share() {
        Intent intent = new Intent();
        intent.setClass(instance, SettingActivity.class);
        exiteApplication();
        startActivity(intent);
    }

    public boolean note_Intent(Context context) {
        ConnectivityManager con = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkinfo = con.getActiveNetworkInfo();
        return networkinfo != null && networkinfo.isAvailable();
    }

    public Bitmap createBitMap(Bitmap src) {
        AppLog.i("zhang", "开始了，画图");
        Bitmap wmsrc = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int wmw = wmsrc.getWidth();
        int wmh = wmsrc.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(src, 0.0f, 0.0f, (Paint) null);
        cv.drawBitmap(wmsrc, (w - wmw) - 5, (h - wmh) - 5, (Paint) null);
        AppLog.i("zhang", "w , h : " + ((w - wmw) - 5) + "," + ((h - wmh) - 5));
        cv.save(8);
        cv.restore();
        return newb;
    }

    public void exitProgrames() {
        Process.killProcess(Process.myPid());
        pause();
        exit();
    }

    public void showDialog() {
        ToggleButton gsensor = (ToggleButton) findViewById(R.id.g_sensor_toggle_button);
        WindowManager m = getWindowManager();
        m.getDefaultDisplay();
        Window w = this.dlg.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        w.setBackgroundDrawableResource(R.color.vifrification);
        w.setAttributes(lp);
        this.dlg.show();
        gsensor.setBackgroundResource(R.drawable.g);
        gsensor.setChecked(false);
        try {
            this.wifiCar.move(0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.wifiCar.move(1, 0);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public void setDirectionGsensor(int left, int right) {
        if (getInstance().isPlayModeEnable) {
            getInstance().sendMessage(MESSAGE_STOP_PLAY);
        }
        AppLog.d("wild0", "direction left:" + left);
        this.iCarSpeedL = left * 10;
        this.iCarSpeedR = right * 10;
    }

    private void DeleVideo() {
        if (this.startVideo) {
            this.startVideo = false;
            String path = String.valueOf(ReadSDPath()) + "/Brookstone";
            File f = new File(path);
            File[] files = f.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    if (fileName.equals(this.FileName)) {
                        File dfile = new File(file.getPath());
                        dfile.delete();
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleIndexVideo() {
        String path = String.valueOf(ReadSDPath()) + "/Brookstone";
        File f = new File(path);
        if (f.exists()) {
            f.mkdirs();
            File[] files = f.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".index")) {
                        File dfile = new File(file.getPath());
                        dfile.delete();
                        String filePath = file.getPath().substring(0, file.getPath().length() - 6);
                        File dfile1 = new File(filePath);
                        dfile1.delete();
                    }
                }
            }
        }
    }

    public String ReadSDPath() {
        boolean SDExit = Environment.getExternalStorageState().equals("mounted");
        if (SDExit) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public static int dip2px(float dpValue) {
        return (int) ((Scale * dpValue) + 0.5f);
    }

    public void initValue() {
        if (isTablet(this) && this.screenSize > 5.8d) {
            Car_Move_Progress_Width = dip2px(50.0f);
            Car_Move_Progress_Height = dip2px(225.0f);
            Car_Compont_UD_Marge_L = dip2px(40.0f);
            Car_Compont_UD_Marge_D = dip2px(60.0f);
            Car_Compont_LR_Marge_R = dip2px(40.0f);
            Car_Camera_Progress_Width = dip2px(35.0f);
            Car_Camera_Progress_Height = dip2px(130.0f);
            UD_Diff_x = ((this.Screen_width - Car_Compont_UD_Marge_L) - Car_Move_Progress_Width) - Car_Compont_LR_Marge_R;
            return;
        }
        Car_Move_Progress_Width = dip2px(40.0f);
        Car_Move_Progress_Height = dip2px(160.0f);
        Car_Compont_UD_Marge_L = dip2px(15.0f);
        Car_Compont_LR_Marge_R = dip2px(15.0f);
        Car_Compont_UD_Marge_D = dip2px(10.0f);
        Car_Camera_Progress_Width = dip2px(30.0f);
        Car_Camera_Progress_Height = dip2px(110.0f);
        UD_Diff_x = ((this.Screen_width - Car_Compont_UD_Marge_L) - Car_Move_Progress_Width) - Car_Compont_LR_Marge_R;
    }

    public void getDisplayMetrics() {
        new DisplayMetrics();
        DisplayMetrics dm = instance.getApplicationContext().getResources().getDisplayMetrics();
        this.Screen_width = dm.widthPixels;
        this.Screen_height = dm.heightPixels;
        Scale = getResources().getDisplayMetrics().density;
        this.density = dm.density;
        double bb = Math.sqrt(Math.pow(this.Screen_width, 2.0d) + Math.pow(this.Screen_height, 2.0d));
        this.screenSize = bb / (160.0f * dm.density);
        AppLog.e(this.TAG, "Screen_width:" + this.Screen_width + "  Screen_height:" + this.Screen_height + " screenSize:" + this.screenSize);
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 3;
    }
}
