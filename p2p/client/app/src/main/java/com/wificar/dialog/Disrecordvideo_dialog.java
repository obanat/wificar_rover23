package com.wificar.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import com.rover2.R;

/* loaded from: classes.dex */
public class Disrecordvideo_dialog {
    public static AlertDialog.Builder createdisaenablevideoDialog(Context context) {
        AlertDialog.Builder disaenablevideoDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, (int) R.layout.share_dialog));
        disaenablevideoDialog.setMessage("There is not enough power to record a video, please charge device.");
        disaenablevideoDialog.setCancelable(false);
        disaenablevideoDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.wificar.dialog.Disrecordvideo_dialog.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return disaenablevideoDialog;
    }
}
