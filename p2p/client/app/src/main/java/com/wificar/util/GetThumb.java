package com.wificar.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

/* loaded from: classes.dex */
public class GetThumb {
    public static Bitmap getVideoThumbnail(Context context, ContentResolver cr, String testVideopath) {
        ContentResolver testcr = context.getContentResolver();
        String[] projection = {"_data", "_id"};
        String whereClause = "_data = '" + testVideopath + "'";
        Cursor cursor = testcr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, whereClause, null, null);
        int _id = 0;
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        if (cursor.moveToFirst()) {
            int _idColumn = cursor.getColumnIndex("_id");
            int _dataColumn = cursor.getColumnIndex("_data");
            do {
                _id = cursor.getInt(_idColumn);
                String videoPath = cursor.getString(_dataColumn);
                System.out.println(String.valueOf(_id) + " " + videoPath);
            } while (cursor.moveToNext());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return MediaStore.Video.Thumbnails.getThumbnail(cr, _id, 1, options);
        }
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inDither = false;
        options2.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return MediaStore.Video.Thumbnails.getThumbnail(cr, _id, 1, options2);
    }

    public static Bitmap getImageThumbnail(Context context, ContentResolver cr, String testImagepath) {
        ContentResolver testcr = context.getContentResolver();
        String[] projection = {"_data", "_id"};
        String whereClause = "_data = '" + testImagepath + "'";
        AppLog.e("getVideoThumb", "whereClause :" + whereClause);
        Cursor cursor = testcr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, whereClause, null, null);
        int _id = 0;
        AppLog.e("getVideoThumb", "cursor :" + cursor);
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        if (cursor.moveToFirst()) {
            int _idColumn = cursor.getColumnIndex("_id");
            int _dataColumn = cursor.getColumnIndex("_data");
            do {
                _id = cursor.getInt(_idColumn);
                String imagePath = cursor.getString(_dataColumn);
                System.out.println(String.valueOf(_id) + " " + imagePath);
            } while (cursor.moveToNext());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return MediaStore.Images.Thumbnails.getThumbnail(cr, _id, 1, options);
        }
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inDither = false;
        options2.inPreferredConfig = Bitmap.Config.RGB_565;
        return MediaStore.Images.Thumbnails.getThumbnail(cr, _id, 1, options2);
    }
}
