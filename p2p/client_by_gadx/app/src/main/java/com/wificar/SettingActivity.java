package com.wificar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.rover2.R;
import com.wificar.component.WifiCar;
import com.wificar.util.AppLog;

/* loaded from: classes.dex */
public class SettingActivity extends BaseActivity {
    private static SettingActivity instance = null;
    public EditText IP;
    private Button Okbutton;
    public EditText Port;
    public int audio_play = WificarActivity.getInstance().audio_play;
    public TextView device;
    public TextView firmware;
    public TextView software;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.setting_info);
        if (this.audio_play == 1) {
            WificarActivity.getInstance().setting_play();
        }
        this.IP = (EditText) findViewById(R.id.EditText_IP);
        this.Port = (EditText) findViewById(R.id.EditText_PORT);
        this.device = (TextView) findViewById(R.id.TextView_D);
        this.firmware = (TextView) findViewById(R.id.TextView_F);
        this.software = (TextView) findViewById(R.id.TextView_S);
        this.Okbutton = (Button) findViewById(R.id.OkButton);
        this.Okbutton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.SettingActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                SettingActivity.this.Okbutton.setBackgroundResource(R.drawable.ok_off);
                WificarActivity.getInstance().onResume();
                SettingActivity.instance.finish();
            }
        });
        String host = WificarActivity.getInstance().getWifiCar().getHost();
        this.IP.setText(host);
        this.IP.setClickable(false);
        String port = String.valueOf(WificarActivity.getInstance().getWifiCar().getPort());
        this.Port.setText(port);
        this.IP.setClickable(false);
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
        String version = WifiCar.getVersion(instance);
        this.software.setText(version);
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        onBackPressed();
        if (isFinishing()) {
            AppLog.d("activity", "setting on Pause:finish");
        }
    }
}
