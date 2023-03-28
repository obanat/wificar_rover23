package com.wificar.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import com.wificar.BaseActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler INSTANCE = new CrashHandler();
    public static final String TAG = "CrashHandler";
    private List<BaseActivity> activities;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Map<String, String> infos = new HashMap();
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context context, List<BaseActivity> activities) {
        this.mContext = context;
        this.activities = activities;
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && this.mDefaultHandler != null) {
            this.mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            for (Activity activity : this.activities) {
                activity.finish();
            }
            this.activities.clear();
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
        collectDeviceInfo(this.mContext);
        saveCrashInfo2File(ex);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.wificar.util.CrashHandler$1] */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread() { // from class: com.wificar.util.CrashHandler.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Looper.prepare();
                Looper.loop();
            }
        }.start();
        collectDeviceInfo(this.mContext);
        saveCrashInfo2File(ex);
        return true;
    }

    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 1);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = new StringBuilder(String.valueOf(pi.versionCode)).toString();
                this.infos.put("versionName", versionName);
                this.infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                this.infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, String.valueOf(field.getName()) + " : " + field.get(null));
            } catch (Exception e2) {
                Log.e(TAG, "an error occured when collect crash info", e2);
            }
        }
    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : this.infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(String.valueOf(key) + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            cause.printStackTrace(printWriter);
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = this.formatter.format(new Date());
            String fileName = String.valueOf(time) + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals("mounted")) {
                String path = String.valueOf(Environment.getExternalStorageDirectory().toString()) + File.separator + "Rover2Log" + File.separator + "log" + File.separator;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(String.valueOf(path) + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
                return fileName;
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
            return null;
        }
    }
}
