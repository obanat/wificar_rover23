package com.wificar.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.wificar._R;

/* loaded from: classes.dex */
public class WificarNewLayoutParams {
    public static int UD_Diff_x;
    public static int UD_Diff_y;
    public static WificarNewLayoutParams instance;
    public static float scale;
    public int Screen_height;
    public int Screen_width;
    public Activity activity;
    public RelativeLayout.LayoutParams bottomLayoutParams;
    public LinearLayout.LayoutParams btn_in_bottomParams;
    public RelativeLayout.LayoutParams car_Left_Move_Params;
    public RelativeLayout.LayoutParams car_Right_Move_Params;
    public float density;
    public RelativeLayout.LayoutParams gSensor_button_Params;
    private boolean isPad;
    public double screenSize;
    public RelativeLayout.LayoutParams share_button_Params;
    int x;
    public static int Car_Move_Progress_Width = 50;
    public static int Car_Move_Progress_Height1 = 183;
    public static int Car_Move_Progress_Height = 180;
    public static int Car_Camera_Move_Width = 180;
    public static int Car_Camera_Move_Height = 190;
    public static int Car_Camera_Progress_Width = 30;
    public static int Car_Camera_Progress_Height = 118;
    public static int Car_Compont_UD_Marge_L = 20;
    public static int Car_Compont_LR_Marge_R = 20;
    public static int Car_Compont_UD_Marge_D = 20;
    public static int Car_Compont_LR_Marge_D = 20;
    public static int Car_Camera_Marge_L = 80;
    public static int Car_Camera_Marge_D = 80;
    public static int Car_Setting_Width = 35;
    public int share_btn_width = 50;
    public int bottom_in_parent_height = 120;
    public int btn_in_bottom_width = 120;
    public int btn_in_bottom_width_center = 60;
    public int btn_in_bottom_height = 80;
    public int btn_in_bottom_num = 10;

    public WificarNewLayoutParams(Activity activity) {
        this.isPad = false;
        this.activity = activity;
        getDisplayMetrics();
        if (isTablet(activity)) {
            if (this.screenSize < 5.8d) {
                this.isPad = false;
            } else {
                this.isPad = true;
            }
        } else {
            this.isPad = false;
        }
        initVar();
    }

    public static WificarNewLayoutParams getWificarNewLayoutParams() {
        return instance;
    }

    public static WificarNewLayoutParams getWificarNewLayoutParams(Activity activity) {
        if (instance == null) {
            instance = new WificarNewLayoutParams(activity);
        }
        return instance;
    }

    public void initVar() {
        if (this.isPad) {
            Car_Move_Progress_Width = dip2px(50.0f);
            Car_Move_Progress_Height = dip2px(240.0f);
            Car_Compont_UD_Marge_L = dip2px(20.0f);
            Car_Compont_UD_Marge_D = dip2px(25.0f);
            Car_Compont_LR_Marge_R = dip2px(20.0f);
            this.share_btn_width = dip2px(50.0f);
            initHParams();
        } else {
            Car_Move_Progress_Width = dip2px(40.0f);
            Car_Move_Progress_Height = dip2px(160.0f);
            Car_Camera_Marge_L = dip2px(80.0f);
            Car_Camera_Marge_D = dip2px(80.0f);
            Car_Compont_UD_Marge_L = dip2px(13.0f);
            Car_Compont_LR_Marge_R = dip2px(13.0f);
            Car_Compont_UD_Marge_D = dip2px(10.0f);
            initParmas();
        }
        Car_Setting_Width = dip2px(35.0f);
    }

    public static int dip2px(float dpValue) {
        return (int) ((scale * dpValue) + 0.5f);
    }

    public void getDisplayMetrics() {
        new DisplayMetrics();
        DisplayMetrics dm = this.activity.getApplicationContext().getResources().getDisplayMetrics();
        this.Screen_width = dm.widthPixels;
        this.Screen_height = dm.heightPixels;
        scale = this.activity.getResources().getDisplayMetrics().density;
        this.density = dm.density;
        double bb = Math.sqrt(Math.pow(this.Screen_width, 2.0d) + Math.pow(this.Screen_height, 2.0d));
        this.screenSize = bb / (160.0f * dm.density);
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 3;
    }

    public void initParmas() {
        AppLog.e("TAG", "Car_Move_Progress_Width:" + Car_Move_Progress_Width + " Car_Move_Progress_Height:" + Car_Move_Progress_Height);
        AppLog.e("TAG", "Car_Compont_UD_Marge_L:" + Car_Compont_UD_Marge_L);
        this.car_Left_Move_Params = new RelativeLayout.LayoutParams(Car_Move_Progress_Width, Car_Move_Progress_Height);
        this.car_Left_Move_Params.addRule(9);
        this.car_Left_Move_Params.addRule(15);
        this.car_Left_Move_Params.leftMargin = Car_Compont_UD_Marge_L;
        this.car_Right_Move_Params = new RelativeLayout.LayoutParams(Car_Move_Progress_Width, Car_Move_Progress_Height);
        this.car_Right_Move_Params.addRule(11);
        this.car_Right_Move_Params.addRule(15);
        this.car_Right_Move_Params.rightMargin = Car_Compont_LR_Marge_R;
    }

    public void initHParams() {
        this.car_Left_Move_Params = new RelativeLayout.LayoutParams(Car_Move_Progress_Width, Car_Move_Progress_Height);
        this.car_Left_Move_Params.addRule(9);
        this.car_Left_Move_Params.addRule(15);
        this.car_Left_Move_Params.leftMargin = Car_Compont_UD_Marge_L;
        this.car_Right_Move_Params = new RelativeLayout.LayoutParams(Car_Move_Progress_Width, Car_Move_Progress_Height);
        this.car_Right_Move_Params.addRule(11);
        this.car_Right_Move_Params.addRule(15);
        this.car_Right_Move_Params.rightMargin = Car_Compont_UD_Marge_L;
        this.share_button_Params = new RelativeLayout.LayoutParams(this.share_btn_width, this.share_btn_width);
        this.share_button_Params.addRule(9);
        this.share_button_Params.addRule(2, _R.id.Car_Left_id);
        this.share_button_Params.bottomMargin = Car_Compont_UD_Marge_D;
        this.share_button_Params.leftMargin = Car_Compont_UD_Marge_L;
        this.gSensor_button_Params = new RelativeLayout.LayoutParams(this.share_btn_width, this.share_btn_width);
        this.gSensor_button_Params.addRule(11);
        this.gSensor_button_Params.addRule(2, _R.id.Car_Right_id);
        this.gSensor_button_Params.bottomMargin = Car_Compont_UD_Marge_D;
        this.gSensor_button_Params.rightMargin = Car_Compont_UD_Marge_L;
    }
}
