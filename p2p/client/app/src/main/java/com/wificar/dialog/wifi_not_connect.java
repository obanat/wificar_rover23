package com.wificar.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import com.rover2.R;

/* loaded from: classes.dex */
public class wifi_not_connect {
    public static AlertDialog.Builder createwificonnectDialog(Context context) {
        AlertDialog.Builder connectDialogwifi = new AlertDialog.Builder(new ContextThemeWrapper(context, (int) R.layout.share_dialog));
        connectDialogwifi.setTitle("Connection Error");
        connectDialogwifi.setMessage("Please check your Wi-Fi connection");
        connectDialogwifi.setCancelable(false);
        connectDialogwifi.setPositiveButton("Done", new DialogInterface.OnClickListener() { // from class: com.wificar.dialog.wifi_not_connect.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return connectDialogwifi;
    }
}
