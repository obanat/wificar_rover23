package com.wificar.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import com.rover2.R;
import com.wificar.WificarActivity;

/* loaded from: classes.dex */
public class SDcardCheck {
    public static AlertDialog.Builder creatSDcardCheckDialog(Context context) {
        AlertDialog.Builder sdcardcheckDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, (int) R.layout.share_dialog));
        sdcardcheckDialog.setTitle(R.string.sdcard_tilt);
        sdcardcheckDialog.setPositiveButton(R.string.done_button, new DialogInterface.OnClickListener() { // from class: com.wificar.dialog.SDcardCheck.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                WificarActivity.getInstance().video();
            }
        });
        return sdcardcheckDialog;
    }
}
