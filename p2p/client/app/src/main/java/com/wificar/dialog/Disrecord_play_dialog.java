package com.wificar.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import com.rover2.R;

/* loaded from: classes.dex */
public class Disrecord_play_dialog {
    public static AlertDialog.Builder createdisaenableDialog(Context context) {
        AlertDialog.Builder disaenableDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, (int) R.layout.share_dialog));
        disaenableDialog.setMessage("There is not enough power to play or record a path, please charge device.");
        disaenableDialog.setCancelable(false);
        disaenableDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.wificar.dialog.Disrecord_play_dialog.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return disaenableDialog;
    }
}
