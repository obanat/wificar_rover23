package com.wificar;

import android.app.Activity;
import android.os.Bundle;

/* loaded from: classes.dex */
public class BaseActivity extends Activity {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().getActivities().add(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().getActivities().remove(this);
    }

    public void exiteApplication() {
        for (BaseActivity activity : MyApplication.getInstance().getActivities()) {
            activity.finish();
        }
        MyApplication.getInstance().getActivities().clear();
    }
}
