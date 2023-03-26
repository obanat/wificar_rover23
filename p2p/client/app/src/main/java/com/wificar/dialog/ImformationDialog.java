package com.wificar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.rover2.R;
import com.wificar.WificarActivity;
import com.wificar.component.WifiCar;
import com.wificar.util.AppLog;

/* loaded from: classes.dex */
public class ImformationDialog extends Dialog {
    public EditText IP;
    private Button Okbutton;
    public EditText Port;
    public TextView device;
    public TextView firmware;
    public TextView software;

    public ImformationDialog(Context context, int deletedialog) {
        super(context);
    }

    @Override // android.app.Dialog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(128, 128);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.setting_info);
        this.IP = (EditText) findViewById(R.id.EditText_IP);
        this.Port = (EditText) findViewById(R.id.EditText_PORT);
        this.device = (TextView) findViewById(R.id.TextView_D);
        this.firmware = (TextView) findViewById(R.id.TextView_F);
        this.software = (TextView) findViewById(R.id.TextView_S);
        this.Okbutton = (Button) findViewById(R.id.OkButton);
        this.Okbutton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.dialog.ImformationDialog.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                ImformationDialog.this.cancel();
            }
        });
        String host = WificarActivity.getInstance().getWifiCar().getHost();
        this.IP.setText(host);
        this.IP.setClickable(false);
        String port = String.valueOf(WificarActivity.getInstance().getWifiCar().getPort());
        this.Port.setText(port);
        this.Port.setClickable(false);
        String ssid = "";
        try {
            ssid = WificarActivity.getInstance().getWifiCar().getSSID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppLog.i("zhang", "ssid :" + ssid);
        AppLog.i("zhang", "deivce :" + this.device);
        this.device.setText(ssid);
        String firmwareVersion = WificarActivity.getInstance().getWifiCar().getFilewareVersion();
        if (!firmwareVersion.equals("")) {
            firmwareVersion = "1.0";
        } else if (firmwareVersion.equals("")) {
            firmwareVersion = " ";
        }
        AppLog.i("zhang", "firmwareVersion11 :" + firmwareVersion);
        this.firmware.setText(firmwareVersion);
        String version = WifiCar.getVersion(getContext());
        this.software.setText(version);
    }
}
