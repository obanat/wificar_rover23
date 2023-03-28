package com.wificar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.rover2.R;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ImageAdapterVideo extends BaseAdapter {
    LayoutInflater inflater1;
    private Context mContext;
    private ArrayList<Bitmap> videos = new ArrayList<>();

    public ImageAdapterVideo(Context context) {
        this.mContext = context;
        this.inflater1 = LayoutInflater.from(this.mContext);
    }

    public void addPhoto(Bitmap photo) {
        this.videos.add(photo);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.videos.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.videos.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return position;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v1 = this.inflater1.inflate(R.layout.video_view_item, (ViewGroup) null);
        ImageView imgv = (ImageView) v1.findViewById(R.id.imageView_video_view);
        imgv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgv.setImageBitmap(this.videos.get(position));
        return v1;
    }
}
