package com.wificar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.rover2.R;
import com.wificar.mediaplayer.JNIWificarVideoPlay;
import com.wificar.util.AppLog;
import com.wificar.util.ImageAdapterPhoto;
import com.wificar.util.ImageAdapterVideo;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ShareActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private static final int DOUBLE_PRESS_INTERVAL = 2000;
    private static final int PHOTO = 1;
    private static final int VIDEO = 2;
    private static final int VIDEOPHOTO = 0;
    public static ImageAdapterPhoto imageAdapterP;
    public static ImageAdapterVideo imageAdapterV;
    private static ShareActivity instance;
    private static String path;
    private String filePath;
    private String filePathV;
    public String[] photo;
    private Button photo_button;
    private GridView photo_gridview;
    public List<String> photo_path;
    private TextView titlText;
    public String[] video;
    private Button video_button;
    private GridView video_gridview;
    public List<String> video_path;
    private long lastPressTime = 0;
    private boolean comtoShare = false;
    public View.OnClickListener bPListener = new View.OnClickListener() { // from class: com.wificar.ShareActivity.1
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            ShareActivity.this.photo_gridview.setVisibility(0);
            ShareActivity.this.video_gridview.setVisibility(8);
            ShareActivity.this.titlText.setText("Photos");
            ShareActivity.this.titlText.setTextSize(20.0f);
        }
    };
    public View.OnClickListener bVListener = new View.OnClickListener() { // from class: com.wificar.ShareActivity.2
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            ShareActivity.this.photo_gridview.setVisibility(8);
            ShareActivity.this.video_gridview.setVisibility(0);
            ShareActivity.this.titlText.setText("Videos");
            ShareActivity.this.titlText.setTextSize(20.0f);
        }
    };

    public static ShareActivity getInstance() {
        return instance;
    }

    @Override // android.app.Activity
    protected void onStop() {
        AppLog.i("shareActiviry33", "on stop:" + this.comtoShare);
        this.comtoShare = false;
        super.onStop();
    }

    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        requestWindowFeature(1);
        setContentView(R.layout.main_gallery);
        if (!SdCardIsExsit()) {
            Toast.makeText(instance, (int) R.string.wificar_no_sdcard, 1).show();
            return;
        }
        path = String.valueOf(ReadSDPath()) + "/Brookstone";
        deleIndexVideo();
        this.photo_gridview = (GridView) findViewById(R.id.photoGallery);
        this.video_gridview = (GridView) findViewById(R.id.videoGallery);
        loadPhoto();
        loadVideo();
        this.titlText = (TextView) findViewById(R.id.titl);
        this.photo_button = (Button) findViewById(R.id.photo);
        this.photo_button.setOnClickListener(this.bPListener);
        this.video_button = (Button) findViewById(R.id.video);
        this.video_button.setOnClickListener(this.bVListener);
        this.photo_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.wificar.ShareActivity.3
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShareActivity.this.comtoShare = true;
                Intent i = new Intent();
                i.setClass(ShareActivity.this, ImageGalleryActivity.class);
                i.putExtra("ImagePath", ShareActivity.this.photo_path.get(position).toString());
                i.putExtra("position", position);
                ShareActivity.this.startActivity(i);
            }
        });
        this.video_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.wificar.ShareActivity.4
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShareActivity.this.comtoShare = true;
                Intent v = new Intent();
                v.setClass(ShareActivity.this, VideoGalleryActivity.class);
                v.putExtra("videoPath", ShareActivity.this.video_path.get(position).toString());
                v.putExtra("position", position);
                ShareActivity.this.startActivity(v);
            }
        });
    }

    private boolean SdCardIsExsit() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    private String ReadSDPath() {
        boolean SDExit = Environment.getExternalStorageState().equals("mounted");
        if (SDExit) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public void loadPhoto() {
        this.photo_gridview.setClipToPadding(true);
        imageAdapterP = new ImageAdapterPhoto(getApplicationContext());
        this.photo_gridview.setAdapter((ListAdapter) imageAdapterP);
        this.photo_path = getInSDPhotoVideo(1);
        this.photo = (String[]) this.photo_path.toArray(new String[this.photo_path.size()]);
        getAsyncTaskPhoto();
        this.photo_gridview.setOnItemSelectedListener(this);
    }

    public void loadVideo() {
        this.video_gridview.setClipToPadding(true);
        imageAdapterV = new ImageAdapterVideo(getApplicationContext());
        this.video_gridview.setAdapter((ListAdapter) imageAdapterV);
        this.video_path = getInSDPhotoVideo(2);
        this.video = (String[]) this.video_path.toArray(new String[this.video_path.size()]);
        getAsyncTaskVideo();
        this.video_gridview.setOnItemSelectedListener(this);
    }

    public static List<String> getInSDPhotoVideo(int i) {
        String current_pathString = "";
        List<String> it_p = new ArrayList<>();
        if (i == 1) {
            current_pathString = String.valueOf(path) + "/Pictures";
        } else if (i == 2) {
            current_pathString = String.valueOf(path) + "/Videos";
        }
        File f = new File(current_pathString);
        File[] files = f.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                String fileName = file.getName();
                if (i == 1 && fileName.endsWith(".jpg")) {
                    it_p.add(file.getPath());
                }
                if (i == 2 && fileName.endsWith(".avi")) {
                    it_p.add(file.getPath());
                }
            }
        }
        return it_p;
    }

    private void getAsyncTaskPhoto() {
        Object data = getLastNonConfigurationInstance();
        if (data == null) {
            new AsyncTaskLoadPhoto(this, this.photo_path).execute(new Object[0]);
            return;
        }
        Bitmap[] photos = (Bitmap[]) data;
        if (photos.length == 0) {
            new AsyncTaskLoadPhoto(this, this.photo_path).execute(new Object[0]);
        }
        for (Bitmap photo : photos) {
            imageAdapterP.addPhoto(photo);
            imageAdapterP.notifyDataSetChanged();
        }
    }

    public void getAsyncTaskVideo() {
        Object data = getLastNonConfigurationInstance();
        if (data == null) {
            new AsyncTaskLoadVideo(this, this.video_path).execute(new Object[0]);
            return;
        }
        Bitmap[] videos = (Bitmap[]) data;
        if (videos.length == 0) {
            new AsyncTaskLoadVideo(this, this.video_path).execute(new Object[0]);
        }
        for (Bitmap photo : videos) {
            imageAdapterV.addPhoto(photo);
            imageAdapterV.notifyDataSetChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class AsyncTaskLoadPhoto extends AsyncTask<Object, Bitmap, Object> {
        private Context context;
        private List<String> photo_lis;

        public AsyncTaskLoadPhoto(Context mContext, List<String> path) {
            this.context = mContext;
            this.photo_lis = path;
        }

        @Override // android.os.AsyncTask
        protected Object doInBackground(Object... params) {
            Bitmap bitmap;
            Bitmap newbitmap = null;
            int sw = ShareActivity.dip2px(this.context, 110.0f);
            int sh = ShareActivity.dip2px(this.context, 82.0f);
            for (int i = 0; i < this.photo_lis.size(); i++) {
                ShareActivity.this.filePath = this.photo_lis.get(i).toString();
                if (ShareActivity.this.filePath.endsWith(".jpg") && (bitmap = ShareActivity.getImageThumbnail(this.context, ShareActivity.this.getContentResolver(), ShareActivity.this.filePath)) != null) {
                    newbitmap = Bitmap.createScaledBitmap(bitmap, sw, sh, true);
                    bitmap.recycle();
                }
                if (newbitmap != null) {
                    publishProgress(newbitmap);
                }
                AppLog.i("zhang", "i :" + i);
            }
            return null;
        }

        @Override // android.os.AsyncTask
        protected void onPostExecute(Object result) {
            AppLog.d("mybug", "异步处理 结束");
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Bitmap... values) {
            for (Bitmap bitmap : values) {
                ShareActivity.imageAdapterP.addPhoto(bitmap);
                ShareActivity.imageAdapterP.notifyDataSetChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class AsyncTaskLoadVideo extends AsyncTask<Object, Bitmap, Object> {
        private Context context;
        private List<String> video_lis;

        public AsyncTaskLoadVideo(Context mContext, List<String> path) {
            this.context = mContext;
            this.video_lis = path;
        }

        @Override // android.os.AsyncTask
        protected Object doInBackground(Object... params) {
            Bitmap newbitmap = null;
            int sw = ShareActivity.dip2px(this.context, 110.0f);
            int sh = ShareActivity.dip2px(this.context, 82.0f);
            for (int i = 0; i < this.video_lis.size(); i++) {
                ShareActivity.this.filePathV = this.video_lis.get(i).toString();
                AppLog.i("zhang", "video_lis.get(i).toString():" + this.video_lis.get(i).toString() + " " + i);
                byte[] rgb565Array = JNIWificarVideoPlay.getVideoSnapshot(ShareActivity.this.filePathV);
                if (rgb565Array == null || rgb565Array.length == 0) {
                    newbitmap = BitmapFactory.decodeResource(ShareActivity.this.getResources(), R.drawable.video_snapshot1);
                } else if (rgb565Array != null) {
                    Bitmap bitmap = ShareActivity.this.rgb565ToBitmap(rgb565Array);
                    if (bitmap != null) {
                        newbitmap = Bitmap.createScaledBitmap(bitmap, sw, sh, true);
                        bitmap.recycle();
                    }
                }
                if (newbitmap != null) {
                    publishProgress(newbitmap);
                }
                AppLog.i("zhang", "i :" + i);
            }
            return null;
        }

        @Override // android.os.AsyncTask
        protected void onPostExecute(Object result) {
            AppLog.d("mybug", "异步处理 结束");
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Bitmap... values) {
            for (Bitmap bitmap : values) {
                ShareActivity.imageAdapterV.addPhoto(bitmap);
                ShareActivity.imageAdapterV.notifyDataSetChanged();
            }
        }
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((scale / 1.0f) * dpValue);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap rgb565ToBitmap(byte[] data) {
        Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.RGB_565);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
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
            return MediaStore.Images.Thumbnails.getThumbnail(cr, _id, 3, options);
        }
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inDither = false;
        options2.inPreferredConfig = Bitmap.Config.RGB_565;
        return MediaStore.Images.Thumbnails.getThumbnail(cr, _id, 3, options2);
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        AppLog.e("MainActivity", "the position :" + position);
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        String statement = getResources().getString(R.string.click_again_to_exit_the_program);
        long pressTime = System.currentTimeMillis();
        if (pressTime - this.lastPressTime <= 2000) {
            scanSdCard();
            exiteApplication();
        } else {
            Toast.makeText(this, statement, 0).show();
        }
        this.lastPressTime = pressTime;
    }

    private void deleIndexVideo() {
        String path1 = String.valueOf(ReadSDPath()) + "/Brookstone/Videos";
        File f = new File(path1);
        if (f.exists()) {
            File[] files = f.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".index")) {
                        File dfile = new File(file.getPath());
                        dfile.delete();
                        String filePath = file.getPath().substring(0, file.getPath().length() - 6);
                        AppLog.i("DeleFile", "filePath :" + filePath);
                        File dfile1 = new File(filePath);
                        dfile1.delete();
                    }
                }
            }
        }
    }

    public void scanSdCard() {
        if (Build.VERSION.SDK_INT >= 19) {
            Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            File file = new File(path);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            return;
        }
        sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }
}
