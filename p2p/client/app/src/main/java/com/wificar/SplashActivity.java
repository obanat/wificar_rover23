package com.wificar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.rover2.R;

/* loaded from: classes.dex */
public class SplashActivity extends BaseActivity {
    protected static final int MESSAGE_MAIN_PROCEDURE = 0;
    private static SplashActivity instance = null;
    private Handler handler = null;
    private String TAG = "SplashActivity";
    public boolean isExit = false;

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static SplashActivity getInstance() {
        return instance;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        finish();
        MyApplication.getInstance().getActivities().remove(this);
    }

    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.splash);
        instance = this;
        this.handler = new Handler() { // from class: com.wificar.SplashActivity.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Intent intent = new Intent(SplashActivity.instance, WificarActivity.class);
                        SplashActivity.instance.startActivityForResult(intent, 1);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        if (this.isExit) {
            this.isExit = false;
        }
        Runnable init = new Runnable() { // from class: com.wificar.SplashActivity.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Thread.sleep(2300L);
                    Message messageLoadingSuccess = new Message();
                    messageLoadingSuccess.what = 0;
                    SplashActivity.this.handler.sendMessage(messageLoadingSuccess);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread initThread = new Thread(init);
        initThread.start();
    }
}
