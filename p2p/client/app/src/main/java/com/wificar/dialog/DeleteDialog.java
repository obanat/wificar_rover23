package com.wificar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.rover2.R;
import com.wificar.ImageGalleryActivity;
import com.wificar.VideoGalleryActivity;

/* loaded from: classes.dex */
public class DeleteDialog extends Dialog {
    private int i;

    public DeleteDialog(Context context) {
        super(context);
    }

    public DeleteDialog(Context context, int theme, int inter) {
        super(context, theme);
        this.i = inter;
    }

    @Override // android.app.Dialog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_dialog);
        Button positiveBtn = (Button) findViewById(R.id.positiveButton);
        Button cancelBtn = (Button) findViewById(R.id.cancelButton);
        TextView text = (TextView) findViewById(R.id.message);
        if (this.i == 1) {
            text.setText("DELETE PHOTOS?");
        } else if (this.i == 2) {
            text.setText("DELETE VIDEOS?");
        }
        positiveBtn.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.dialog.DeleteDialog.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                DeleteDialog.this.cancel();
                if (DeleteDialog.this.i != 1) {
                    if (DeleteDialog.this.i == 2) {
                        VideoGalleryActivity.getInstance().Delete_video();
                        return;
                    }
                    return;
                }
                ImageGalleryActivity.getInstance().Delete_photo();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.dialog.DeleteDialog.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                DeleteDialog.this.dismiss();
            }
        });
    }
}
