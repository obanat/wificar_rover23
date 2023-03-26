package com.wificar.surface;

import com.wificar.component.WifiCar;

/* loaded from: classes.dex */
public interface ControllerInterface {
    void destroyDrawingCache();

    void disableControl();

    void enableControl();

    void initial();

    void setWifiCar(WifiCar wifiCar);

    void setZOrderOnTop(boolean z);
}
