package com.obana.carproxy;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import com.obana.carproxy.utils.*;

/**
 * Created by obana on 2023.03.30.
 */
public class WatchingAccessibilityService extends AccessibilityService {
    private static WatchingAccessibilityService sInstance;
    private static final String TAG  = "CarProxy_Accessibility";
    @SuppressLint("NewApi")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        AppLog.i(TAG, "AccessibilityEvent event:" + event);

    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        sInstance = this;
        Context ctx = getApplicationContext();
        Intent thisIntent = new Intent(ctx, Main.class);
        thisIntent.setAction("android.intent.action.MAIN");
        thisIntent.addCategory("android.intent.category.LAUNCHER");
        thisIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(thisIntent);
        AppLog.i(TAG, "startActivity finished....");

        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        AppLog.i(TAG, "onUnbind finished....");
        return super.onUnbind(intent);
    }

    public static WatchingAccessibilityService getInstance(){
        return sInstance;
    }

}