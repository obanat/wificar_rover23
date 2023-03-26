package com.wificar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.rover2.R;

/* loaded from: classes.dex */
public class DisGsensor extends Dialog {
    private static DisGsensor dialog = null;

    public DisGsensor(Context context) {
        super(context);
    }

    public DisGsensor(Context context, int theme) {
        super(context, theme);
    }

    @Override // android.app.Dialog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.disagsensor);
        Button button = (Button) findViewById(R.id.okbutton);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.dialog.DisGsensor.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                DisGsensor.this.cancel();
            }
        });
    }
}
