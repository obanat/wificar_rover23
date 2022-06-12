package com.obana.rover;
 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.KeyguardManager;
import android.os.PowerManager;
import android.view.WindowManager;
import android.app.Activity;

import com.obana.rover.utils.*;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "Wificar_BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        AppLog.i(TAG, "onReceive：" + intent);
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Intent thisIntent = new Intent(context, WificarMain.class);
            thisIntent.setAction("android.intent.action.MAIN");
            thisIntent.addCategory("android.intent.category.LAUNCHER");
            thisIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(thisIntent);
        }
        
    }
}