package com.wificar.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import com.rover2.R;

/* loaded from: classes.dex */
public class Control_share_dialog {
    public static AlertDialog.Builder createcontrolsharedialog(Context context) {
        AlertDialog.Builder shareDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, (int) R.layout.share_dialog));
        shareDialog.setMessage("The Facebook, Twitter, Tumblr, and YouTube apps must already be installed on your device to share Rover 2.0 photos and videos.\nExit the app, go to Settings and access a Wi-Fi network other than Rover 2.0. Open the Rover 2.0 app and select Share.");
        shareDialog.setCancelable(false);
        shareDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.wificar.dialog.Control_share_dialog.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return shareDialog;
    }
}
