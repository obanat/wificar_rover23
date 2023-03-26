package com.wificar.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import com.rover2.R;
import com.wificar.WificarActivity;

/* loaded from: classes.dex */
public class Connect_Dialog {
    public static AlertDialog.Builder createconnectDialog(Context context) {
        AlertDialog.Builder connectDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, (int) R.layout.share_dialog));
        connectDialog.setTitle("Connection Status ");
        connectDialog.setMessage("Rover 2.0 is not connected, press exit to check your Wi-Fi connection or press Share to share Rover 2.0 photos and videos");
        connectDialog.setCancelable(false);
        connectDialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() { // from class: com.wificar.dialog.Connect_Dialog.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                WificarActivity.getInstance().exit();
                dialog.cancel();
            }
        }).setNegativeButton("Share", new DialogInterface.OnClickListener() { // from class: com.wificar.dialog.Connect_Dialog.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                WificarActivity.getInstance().share();
            }
        });
        return connectDialog;
    }
}
