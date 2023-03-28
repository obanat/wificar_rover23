package com.wificar;

import android.app.Application;
import com.wificar.util.CrashHandler;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class MyApplication extends Application {
    private static MyApplication instance = null;
    protected List<BaseActivity> activities = new ArrayList();

    public List<BaseActivity> getActivities() {
        return this.activities;
    }

    public void setActivities(List<BaseActivity> activities) {
        this.activities = activities;
    }

    public static synchronized MyApplication getInstance() {
        MyApplication myApplication;
        synchronized (MyApplication.class) {
            myApplication = instance;
        }
        return myApplication;
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext(), this.activities);
    }
}
