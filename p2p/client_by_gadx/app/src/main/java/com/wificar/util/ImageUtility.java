package com.wificar.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import com.wificar.WificarActivity;

/* loaded from: classes.dex */
public class ImageUtility {
    public static Bitmap createBitmap(Resources res, int srcId) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, srcId);
        return bitmap;
    }

    public static Bitmap createBitmap(Resources res, int srcId, BitmapFactory.Options opt) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, srcId, opt);
        return bitmap;
    }

    public static void createJPEGFile(byte[] buf, ContentResolver cr) {
        try {
            Bitmap snap = BitmapFactory.decodeByteArray(buf, 0, buf.length);
            MediaStore.Images.Media.insertImage(cr, snap, String.valueOf(System.currentTimeMillis()) + ".jpg", String.valueOf(System.currentTimeMillis()) + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getWidth(Context context) {
        int x = context.getResources().getDisplayMetrics().widthPixels;
        return x;
    }

    public static int getHeight(Context context) {
        int y = context.getResources().getDisplayMetrics().heightPixels;
        return y;
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        int i = context.getResources().getDisplayMetrics().heightPixels;
        float f = context.getResources().getDisplayMetrics().ydpi;
        return (int) ((scale / 1.0f) * dpValue);
    }

    public static float getDensity(Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return scale;
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / (1.5f / scale));
    }

    public static int getBatterySection(int value) {
        WificarActivity.getInstance().getResources();
        if (value < 2) {
            return 0;
        }
        if (value < 3) {
            return 1;
        }
        if (value >= 4) {
            return value < 6 ? 3 : 4;
        }
        return 2;
    }
}
