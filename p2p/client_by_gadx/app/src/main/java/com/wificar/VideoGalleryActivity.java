package com.wificar;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.rover2.R;
import com.wificar.dialog.DeleteDialog;
import com.wificar.dialog.wifi_not_connect;
import com.wificar.mediaplayer.JNIWificarVideoPlay;
import com.wificar.mediaplayer.MediaPlayerActivity;
import com.wificar.util.AppLog;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class VideoGalleryActivity extends BaseActivity {
    private static final int VIDEO = 2;
    public static VideoAdapter imageAdapterV;
    private static VideoGalleryActivity mContext = null;
    private int currenPosition;
    private Button deleButton;
    private Dialog dlg;
    private File file;
    private PopupWindow mPopupWindow;
    private MyGallery myGallery;
    private TextView photos_count;
    private int positionV;
    private Button shareButton;
    private String videoPath;
    private List<String> video_path;
    public List<String> video_path1;
    private boolean isShowing = false;
    private boolean connectWifi = false;
    public AdapterView.OnItemSelectedListener listenerVideo = new AdapterView.OnItemSelectedListener() { // from class: com.wificar.VideoGalleryActivity.1
        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            AppLog.i("VideoGalleryActivity", "the positionV is :" + arg2);
            VideoGalleryActivity.this.positionV = arg2;
            VideoGalleryActivity.this.videoPath = VideoGalleryActivity.this.video_path1.get(VideoGalleryActivity.this.positionV).toString();
            if (!VideoGalleryActivity.this.isShowing) {
                VideoGalleryActivity.this.showPopWindow();
            }
            VideoGalleryActivity.this.photos_count.setText(String.valueOf(VideoGalleryActivity.this.positionV + 1) + " of " + VideoGalleryActivity.this.video_path1.size());
        }

        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onNothingSelected(AdapterView<?> arg0) {
            AppLog.i("VideoGalleryActivity", "onNothingSelected :" + arg0);
        }
    };
    public View.OnClickListener videoPlayListent = new View.OnClickListener() { // from class: com.wificar.VideoGalleryActivity.2
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            AppLog.i("zhang", "the video path : " + VideoGalleryActivity.this.videoPath);
            Intent intent = new Intent(VideoGalleryActivity.this, MediaPlayerActivity.class);
            intent.putExtra("file_name", VideoGalleryActivity.this.videoPath);
            intent.putExtra("file_position", VideoGalleryActivity.this.positionV);
            VideoGalleryActivity.this.startActivity(intent);
        }
    };

    public static VideoGalleryActivity getInstance() {
        return mContext;
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        AppLog.i("VideoGalleryActivity", "onPause");
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        AppLog.i("VideoGalleryActivity", "onStop");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        AppLog.e("VideoGalleryActivity", "onDrestroy");
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
        AppLog.i("VideoGalleryActivity", "onRestart");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.my_gallery);
        this.connectWifi = note_Intent(mContext);
        this.myGallery = (MyGallery) findViewById(R.id.myGallery);
        this.video_path = getInSDPhotoVideo();
        imageAdapterV = new VideoAdapter(mContext, this.video_path);
        Intent intent = getIntent();
        this.videoPath = intent.getStringExtra("videoPath");
        this.currenPosition = intent.getIntExtra("position", 0);
        AppLog.i("VideoGalleryActivity", "the currenPosition :" + this.currenPosition);
        this.myGallery.setAdapter((SpinnerAdapter) imageAdapterV);
        this.myGallery.setSelection(this.currenPosition);
        this.myGallery.setOnItemSelectedListener(this.listenerVideo);
    }

    public static List<String> getInSDPhotoVideo() {
        List<String> it_p = new ArrayList<>();
        String path = String.valueOf(Environment.getExternalStorageDirectory().toString()) + "/Brookstone/Videos";
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                String fileName = file.getName();
                if (fileName.endsWith(".avi")) {
                    it_p.add(file.getPath());
                }
            }
        }
        return it_p;
    }

    private void loadVideoGallery(int pv) {
        this.positionV = pv;
        this.videoPath = this.video_path1.get(pv).toString();
        this.video_path = getInSDPhotoVideo();
        imageAdapterV = new VideoAdapter(mContext, this.video_path);
        this.myGallery.setAdapter((SpinnerAdapter) imageAdapterV);
        this.myGallery.setSelection(pv);
        this.photos_count.setText(String.valueOf(this.positionV + 1) + " of " + this.video_path1.size());
    }

    public boolean note_Intent(Context context) {
        ConnectivityManager con = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkinfo = con.getActiveNetworkInfo();
        return networkinfo != null && networkinfo.isAvailable();
    }

    public void dismiss() {
        AppLog.d("PopWin", "dismiss");
        if (this.mPopupWindow != null) {
            this.mPopupWindow.dismiss();
            this.mPopupWindow = null;
            AppLog.d("PopWin", "dismiss ok");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPopWindow() {
        dismiss();
        this.isShowing = true;
        LayoutInflater LayoutInflater = (LayoutInflater) mContext.getSystemService("layout_inflater");
        View foot_popunwindwow = LayoutInflater.inflate(R.layout.photo_count, (ViewGroup) null);
        this.mPopupWindow = new PopupWindow(foot_popunwindwow, -1, -2);
        this.mPopupWindow.showAtLocation(findViewById(R.id.layout), 48, 0, 5);
        this.mPopupWindow.update();
        this.photos_count = (TextView) foot_popunwindwow.findViewById(R.id.photo_counts);
        this.deleButton = (Button) foot_popunwindwow.findViewById(R.id.delete_button);
        this.deleButton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.VideoGalleryActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoGalleryActivity.this.dlg = new DeleteDialog(VideoGalleryActivity.mContext, R.style.DeleteDialog, 2);
                WindowManager m = VideoGalleryActivity.this.getWindowManager();
                Display d = m.getDefaultDisplay();
                Window w = VideoGalleryActivity.this.dlg.getWindow();
                WindowManager.LayoutParams lp = w.getAttributes();
                w.setGravity(53);
                lp.x = 10;
                lp.y = 70;
                lp.height = (int) (d.getHeight() * 0.3d);
                w.setAttributes(lp);
                VideoGalleryActivity.this.dlg.show();
            }
        });
        this.shareButton = (Button) foot_popunwindwow.findViewById(R.id.share_button);
        this.shareButton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.VideoGalleryActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (!VideoGalleryActivity.this.connectWifi) {
                    wifi_not_connect.createwificonnectDialog(VideoGalleryActivity.mContext).show();
                    return;
                }
                Intent shareIntent = new Intent();
                shareIntent.setAction("android.intent.action.SEND");
                shareIntent.setType("video/*");
                VideoGalleryActivity.this.file = new File(VideoGalleryActivity.this.videoPath);
                ContentValues content = new ContentValues(5);
                content.put("title", "Share");
                content.put("_size", Long.valueOf(VideoGalleryActivity.this.file.length()));
                content.put("date_added", Long.valueOf(System.currentTimeMillis() / 1000));
                content.put("mime_type", "video/avi");
                content.put("_data", VideoGalleryActivity.this.videoPath);
                ContentResolver contentResolver = VideoGalleryActivity.this.getContentResolver();
                Uri base = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                Uri newUri = contentResolver.insert(base, content);
                AppLog.i("ShareActivity", " values:" + content);
                AppLog.i("ShareActivity", " storeLocation:" + newUri);
                if (newUri == null) {
                    shareIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(VideoGalleryActivity.this.file));
                } else {
                    shareIntent.putExtra("android.intent.extra.STREAM", newUri);
                }
                shareIntent.setFlags(268435456);
                VideoGalleryActivity.this.startActivity(Intent.createChooser(shareIntent, "Share"));
                AppLog.i("startShare", "start start");
            }
        });
    }

    public void Delete_video() {
        this.file = new File(this.videoPath);
        if (this.file.exists()) {
            this.file.delete();
        }
        if (this.positionV == this.video_path1.size() - 1) {
            this.positionV = 0;
            ShareActivity.getInstance().loadVideo();
            loadVideoGallery(this.positionV);
        } else {
            ShareActivity.getInstance().loadVideo();
            loadVideoGallery(this.positionV);
        }
        this.dlg.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class VideoAdapter extends BaseAdapter {
        LayoutInflater inflater1;
        private Context mContext;

        public VideoAdapter(Context context) {
            this.mContext = context;
        }

        public VideoAdapter(VideoGalleryActivity mContext2, List<String> path) {
            this.mContext = mContext2;
            VideoGalleryActivity.this.video_path1 = path;
            this.inflater1 = LayoutInflater.from(this.mContext);
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return VideoGalleryActivity.this.video_path1.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int position) {
            return VideoGalleryActivity.this.video_path1.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return position;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            Bitmap bitmap;
            ImageView imageview = new ImageView(this.mContext);
            View v1 = this.inflater1.inflate(R.layout.video_play_item, (ViewGroup) null);
            ImageView imgv = (ImageView) v1.findViewById(R.id.imageView_video_play);
            ImageView playBtn = (ImageView) v1.findViewById(R.id.video_play_button);
            playBtn.setOnClickListener(VideoGalleryActivity.this.videoPlayListent);
            imgv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageview.setLayoutParams(new Gallery.LayoutParams(-1, -1));
            byte[] rgb565Array = JNIWificarVideoPlay.getVideoSnapshot(VideoGalleryActivity.this.video_path1.get(position).toString());
            if (rgb565Array == null || rgb565Array.length == 0) {
                bitmap = BitmapFactory.decodeResource(VideoGalleryActivity.this.getResources(), R.drawable.video_snapshot1);
            } else {
                bitmap = VideoGalleryActivity.this.rgb565ToBitmap(rgb565Array);
            }
            imgv.setImageBitmap(bitmap);
            return v1;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap rgb565ToBitmap(byte[] data) {
        Bitmap bitmap = Bitmap.createBitmap(80, 60, Bitmap.Config.RGB_565);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }
}
