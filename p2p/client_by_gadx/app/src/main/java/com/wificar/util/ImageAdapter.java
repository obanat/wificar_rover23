package com.wificar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Bitmap> photos = new ArrayList<>();

    public ImageAdapter(Context context) {
        this.mContext = context;
    }

    public void addPhoto(Bitmap photo) {
        this.photos.add(photo);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.photos.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.photos.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return position;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(this.mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(this.photos.get(position));
        return imageView;
    }
}
