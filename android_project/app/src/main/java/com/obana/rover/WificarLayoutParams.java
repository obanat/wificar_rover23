package com.obana.rover;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class WificarLayoutParams {
  public static int Car_Camera_Move_Height;
  
  public static int Car_Camera_Move_Width;
  
  public static int Car_Compont_LR_Marge_D;
  
  public static int Car_Compont_LR_Marge_R;
  
  public static int Car_Compont_UD_Marge_D;
  
  public static int Car_Compont_UD_Marge_L;
  
  public static int Car_Move_Progress_Height;
  
  public static int Car_Move_Progress_Height1;
  
  public static int Car_Move_Progress_Width = 180;
  
  public static int UD_Diff_x;
  
  public static int UD_Diff_y;
  
  public static WificarLayoutParams instance;
  
  public static float scale;
  
  public int Screen_height;
  
  public int Screen_width;
  
  public Activity activity;
  
  public RelativeLayout.LayoutParams bottomLayoutParams;
  
  public RelativeLayout.LayoutParams bottomParams;
  
  public LinearLayout.LayoutParams bottom_Btn_Params;
  
  public int bottom_btn_height = 50;
  
  public int bottom_btn_marge = 10;
  
  public int bottom_btn_phone_height = 34;
  
  public int bottom_btn_phone_width = 66;
  
  public int bottom_btn_width = 80;
  
  public int bottom_height = 108;
  
  public int bottom_height_hint = 81;
  
  public int bottom_in_parent_height = 120;
  
  public LinearLayout.LayoutParams btn_in_bottomParams;
  
  public int btn_in_bottom_height = 80;
  
  public int btn_in_bottom_num = 10;
  
  public int btn_in_bottom_width = 120;
  
  public int btn_in_bottom_width_center = 60;
  
  public RelativeLayout.LayoutParams cameraChangeParams;
  
  public RelativeLayout.LayoutParams cameraChangeTextParams;
  
  public RelativeLayout.LayoutParams cameraMoveParams;
  
  public RelativeLayout.LayoutParams camera_Move_Params;
  
  public int camera_change_btn_height = 50;
  
  public int camera_change_btn_width = 50;
  
  public RelativeLayout.LayoutParams camera_lr_Params;
  
  public RelativeLayout.LayoutParams car_LR_Move_Params;
  
  public RelativeLayout.LayoutParams car_UD_Move_Params;
  
  public RelativeLayout.LayoutParams chronParams;
  
  public RelativeLayout.LayoutParams connectParams;
  
  public float density;
  
  public RelativeLayout.LayoutParams gsensorParams;
  
  public RelativeLayout.LayoutParams inforParams;
  
  public LinearLayout.LayoutParams last_Bottom_Btn_Params;
  
  public RelativeLayout.LayoutParams listenVolume_Change_Params;
  
  public RelativeLayout.LayoutParams p_v_Params;
  
  public RelativeLayout.LayoutParams parentParams;
  
  public RelativeLayout.LayoutParams photoParams;
  
  public LinearLayout.LayoutParams photoParams2;
  
  public int photo_bottom_magin = 60;
  
  public int same_distance_height = 10;
  
  public int same_distance_width = 10;
  
  public RelativeLayout.LayoutParams scaleTViewParams;
  
  public double screenSize;
  
  public RelativeLayout.LayoutParams showInfoTViewParams;
  
  public RelativeLayout.LayoutParams speedParams;
  
  public int speed_width = 10;
  
  public RelativeLayout.LayoutParams surfaceParams;
  
  public int surfaceParams_heigth = this.Screen_height - this.up_center_surface_marge_bottom;
  
  public int surfaceParams_width = (this.Screen_height - this.up_center_surface_marge_bottom) * 4 / 3;
  
  public int up_bottom_info_width = 17;
  
  public int up_center_surface_marge_bottom = 50;
  
  public int up_center_surface_marge_lr = 2;
  
  public int up_center_volum_change_height = 20;
  
  public int up_center_volum_change_width = 200;
  
  public int up_center_zoom_height = 82;
  
  public int up_center_zoom_top_marge = 10;
  
  public int up_center_zoom_width = 20;
  
  public int up_edge_botm_info_width = 25;
  
  public int up_edge_botm_joystick_width = 230;
  
  public int up_edge_botm_joystick_width_center = 115;
  
  public int up_edge_botm_right_joystick_width = 100;
  
  public int up_edge_ctn_btn_height = 120;
  
  public int up_edge_ctn_btn_height_center = 60;
  
  public int up_edge_ctn_btn_width = 50;
  
  public int up_edge_marge_lr_width = 5;
  
  public int up_edge_marge_ud_height = 10;
  
  public int up_edge_top_btn_height = 50;
  
  public int up_edge_top_btn_width = 50;
  
  public int up_edge_top_scalebtn_width = 25;
  
  public int up_edge_video_red_width = 25;
  
  public RelativeLayout.LayoutParams videoParams;
  
  public LinearLayout.LayoutParams videoParams2;
  
  public RelativeLayout.LayoutParams videoRedParams;
  
  int x;
  
  public RelativeLayout.LayoutParams zoomInParams;
  
  public RelativeLayout.LayoutParams zoomOutParams;
  
  public int zoom_width = 10;
  
  static {
    Car_Move_Progress_Height1 = 183;
    Car_Move_Progress_Height = 50;
    Car_Camera_Move_Width = 180;
    Car_Camera_Move_Height = 190;
    Car_Compont_UD_Marge_L = 20;
    Car_Compont_LR_Marge_R = 20;
    Car_Compont_UD_Marge_D = 80;
    Car_Compont_LR_Marge_D = 80;
  }
  
  public WificarLayoutParams(Activity paramActivity) {
    this.activity = paramActivity;
    getDisplayMetrics();
    initVar();
  }
  
  public static int dip2px(float paramFloat) {
    return (int)(scale * paramFloat + 0.5F);
  }
  
  public static WificarLayoutParams getWificarLayoutParams() {
    return instance;
  }
  
  public static WificarLayoutParams getWificarLayoutParams(Activity paramActivity) {
    if (instance == null)
      instance = new WificarLayoutParams(paramActivity); 
    return instance;
  }
  
  public void getDisplayMetrics() {

    DisplayMetrics displayMetrics = this.activity.getApplicationContext().getResources().getDisplayMetrics();
    this.Screen_width = displayMetrics.widthPixels;
    this.Screen_height = displayMetrics.heightPixels;
    scale = (this.activity.getResources().getDisplayMetrics()).density;
    this.density = displayMetrics.density;
    this.screenSize = Math.sqrt(Math.pow(this.Screen_width, 2.0D) + Math.pow(this.Screen_height, 2.0D)) / (160.0F * displayMetrics.density);
    Log.e("getWificarLayoutParams", "Screen_width:" + this.Screen_width + "----" + "Screen_height:" + this.Screen_height);
    Log.e("getDisplayMetrics", "Screen_width = " + this.Screen_width + "  Screen_height=" + this.Screen_height);
  }
  
  public void initHParams() {
    this.parentParams = new RelativeLayout.LayoutParams(-1, -1);

  }
  
  public void initVar() {

    initHParams();
  }
}
