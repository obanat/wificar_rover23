package com.wificar;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.wificar.util.AppLog;
import com.wificar.util.GetThumb;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ImageGalleryActivity extends BaseActivity {
    protected static final int SHOW_PROGRESS = 0;
    private static ImageGalleryActivity mContext;
    private Button deleButton;
    private Dialog dlg;
    private File file;
    private PopupWindow mPopupWindow;
    private MyGallery myGallery;
    private String photoPath;
    private List<String> photo_path;
    private List<String> photo_path1;
    private TextView photos_count;
    private int position;
    private Button shareButton;
    private boolean isShowing = false;
    private boolean connectWifi = false;
    public AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() { // from class: com.wificar.ImageGalleryActivity.1
        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            AppLog.i("ImageGalleryActivity", "the position is :" + arg2);
            ImageGalleryActivity.this.position = arg2;
            ImageGalleryActivity.this.photoPath = ((String) ImageGalleryActivity.this.photo_path.get(ImageGalleryActivity.this.position)).toString();
            AppLog.i("ImageGalleryActivity", "the photoPath is :" + ImageGalleryActivity.this.photoPath);
            if (!ImageGalleryActivity.this.isShowing) {
                ImageGalleryActivity.this.showPopWindow();
            }
            ImageGalleryActivity.this.photos_count.setText(String.valueOf(ImageGalleryActivity.this.position + 1) + " of " + ImageGalleryActivity.this.photo_path.size());
        }

        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public static ImageGalleryActivity getInstance() {
        return mContext;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.wificar.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.my_gallery);
        this.connectWifi = note_Intent(mContext);
        this.myGallery = (MyGallery) findViewById(R.id.myGallery);
        this.photo_path1 = getInSDPhotoVideo();
        Intent intent = getIntent();
        this.photoPath = intent.getStringExtra("ImagePath");
        this.position = intent.getIntExtra("position", 0);
        this.myGallery.setAdapter((SpinnerAdapter) new ImageAdapter(getApplicationContext(), this.photo_path1));
        this.myGallery.setSelection(this.position);
        this.myGallery.setOnItemSelectedListener(this.listener);
    }

    private String ReadSDPath() {
        boolean SDExit = Environment.getExternalStorageState().equals("mounted");
        if (SDExit) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public static List<String> getInSDPhotoVideo() {
        List<String> it_p = new ArrayList<>();
        String path = String.valueOf(Environment.getExternalStorageDirectory().toString()) + "/Brookstone/Pictures";
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                String fileName = file.getName();
                if (fileName.endsWith(".jpg")) {
                    it_p.add(file.getPath());
                }
            }
        }
        return it_p;
    }

    private void reLoadPhoto(int p) {
        this.position = p;
        this.photoPath = this.photo_path.get(p).toString();
        this.photo_path1 = getInSDPhotoVideo();
        this.myGallery.setAdapter((SpinnerAdapter) new ImageAdapter(getApplicationContext(), this.photo_path1));
        this.myGallery.setSelection(this.position);
        this.photos_count.setText(String.valueOf(this.position + 1) + " of " + ShareActivity.getInstance().photo_path.size());
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
        this.deleButton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.ImageGalleryActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                ImageGalleryActivity.this.dlg = new DeleteDialog(ImageGalleryActivity.mContext, R.style.DeleteDialog, 1);
                WindowManager m = ImageGalleryActivity.this.getWindowManager();
                Display d = m.getDefaultDisplay();
                Window w = ImageGalleryActivity.this.dlg.getWindow();
                WindowManager.LayoutParams lp = w.getAttributes();
                w.setGravity(53);
                lp.x = 10;
                lp.y = 70;
                lp.height = (int) (d.getHeight() * 0.3d);
                w.setAttributes(lp);
                ImageGalleryActivity.this.dlg.show();
            }
        });
        this.shareButton = (Button) foot_popunwindwow.findViewById(R.id.share_button);
        this.shareButton.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.ImageGalleryActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (!ImageGalleryActivity.this.connectWifi) {
                    wifi_not_connect.createwificonnectDialog(ImageGalleryActivity.mContext).show();
                    return;
                }
                Intent shareIntent = new Intent();
                shareIntent.setAction("android.intent.action.SEND");
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra("android.intent.extra.SUBJECT", "Share");
                ImageGalleryActivity.this.file = new File(ImageGalleryActivity.this.photoPath);
                ContentValues content = new ContentValues(5);
                content.put("title", "Share");
                content.put("_size", Long.valueOf(ImageGalleryActivity.this.file.length()));
                content.put("date_added", Long.valueOf(System.currentTimeMillis() / 1000));
                content.put("mime_type", "image/jpg");
                content.put("_data", ImageGalleryActivity.this.photoPath);
                ContentResolver resolver = ImageGalleryActivity.mContext.getContentResolver();
                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content);
                if (uri == null) {
                    shareIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(ImageGalleryActivity.this.file));
                } else {
                    shareIntent.putExtra("android.intent.extra.STREAM", uri);
                }
                AppLog.i("zhang", "the pictrue path : " + ImageGalleryActivity.this.photoPath);
                ImageGalleryActivity.this.startActivity(Intent.createChooser(shareIntent, "Share"));
            }
        });
    }

    public void Delete_photo() {
        AppLog.i("ImageGalleryActivity", "delete the photoPath is :" + this.photoPath);
        this.file = new File(this.photoPath);
        if (this.file.exists()) {
            this.file.delete();
        }
        if (this.position == this.photo_path.size() - 1) {
            this.position = 0;
            ShareActivity.getInstance().loadPhoto();
            reLoadPhoto(this.position);
        } else {
            ShareActivity.getInstance().loadPhoto();
            reLoadPhoto(this.position);
        }
        this.dlg.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class ImageAdapter extends BaseAdapter {
        private Bitmap bitmap;
        private Context mContext;

        public ImageAdapter(Context applicationContext, List<String> path) {
            this.mContext = applicationContext;
            ImageGalleryActivity.this.photo_path = path;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return ImageGalleryActivity.this.photo_path.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int arg0) {
            return Integer.valueOf(arg0);
        }

        @Override // android.widget.Adapter
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override // android.widget.Adapter
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            ImageView imageview = new ImageView(this.mContext);
            imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageview.setLayoutParams(new Gallery.LayoutParams(-1, -1));
            this.bitmap = GetThumb.getImageThumbnail(this.mContext, ImageGalleryActivity.this.getContentResolver(), ((String) ImageGalleryActivity.this.photo_path.get(arg0)).toString());
            imageview.setImageBitmap(this.bitmap);
            return imageview;
        }
    }
}
