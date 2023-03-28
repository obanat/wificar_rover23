package com.wificar.mediaplayer;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.rover2.R;
import com.wificar.VideoGalleryActivity;
import com.wificar.util.AppLog;
import java.nio.ByteBuffer;

/* loaded from: classes.dex */
public class MediaPlayerActivity extends Activity {
    float density;
    private LinearLayout mDecor;
    private LinearLayout mOverlay;
    private View mSpacer;
    private SurfaceHolder mSurfaceHolder;
    private final int UPDATEUI_FOR_STOP = 256;
    private final int UPDATEUI_CURRENT_TIME = 257;
    private String mVideoFileName = null;
    private int mVideoFilePosition = 0;
    private SurfaceView mWindow = null;
    private SurfaceHolder mWindowHolder = null;
    private int mWindowWidth = 0;
    private int mWindowHeight = 0;
    private ImageButton mPreviousSongImgBtn = null;
    private ImageButton mPlayImgBtn = null;
    private ImageButton mNextSongImgBtn = null;
    private SeekBar mSeekBar = null;
    private TextView mDurationText = null;
    private TextView mCurrentTimeText = null;
    private int mDuration = 0;
    private int mCurrentTime = 0;
    private boolean mIsPlaying = false;
    private boolean mIsVideoRender = false;
    private boolean mIsAudioRender = false;
    private boolean mIsVideoEnd = false;
    private boolean mIsAudioEnd = false;
    private boolean mIsFileEnd = false;
    private boolean mIsVideoSwitch = false;
    private boolean mIsStop = true;
    private boolean calledbyother = false;
    private boolean mShowing = false;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private Handler mHandler = null;
    PowerManager.WakeLock mWakeLock = null;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() { // from class: com.wificar.mediaplayer.MediaPlayerActivity.1
        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 0) {
                if (!MediaPlayerActivity.this.mShowing) {
                    MediaPlayerActivity.this.mDecor.addView(MediaPlayerActivity.this.mOverlay);
                    MediaPlayerActivity.this.mShowing = true;
                } else {
                    MediaPlayerActivity.this.mDecor.removeView(MediaPlayerActivity.this.mOverlay);
                    MediaPlayerActivity.this.mShowing = false;
                }
            }
            return false;
        }
    };

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.player);
        this.density = getResources().getDisplayMetrics().density;
        this.mDecor = (LinearLayout) findViewById(R.id.player_overlay_decor);
        LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
        this.mOverlay = (LinearLayout) inflater.inflate(R.layout.player_overlay, (ViewGroup) null);
        this.mWindow = (SurfaceView) findViewById(R.id.window);
        this.mSurfaceHolder = this.mWindow.getHolder();
        this.mSurfaceHolder.setKeepScreenOn(true);
        this.mSurfaceHolder.setFormat(2);
        this.mPreviousSongImgBtn = (ImageButton) this.mOverlay.findViewById(R.id.previous);
        this.mPlayImgBtn = (ImageButton) this.mOverlay.findViewById(R.id.play);
        this.mNextSongImgBtn = (ImageButton) this.mOverlay.findViewById(R.id.next);
        this.mSeekBar = (SeekBar) this.mOverlay.findViewById(R.id.seek_bar);
        this.mDurationText = (TextView) this.mOverlay.findViewById(R.id.duration_text);
        this.mCurrentTimeText = (TextView) this.mOverlay.findViewById(R.id.current_time_text);
        this.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.wificar.mediaplayer.MediaPlayerActivity.2
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
                JNIWificarVideoPlay.playerSeek(seekBar.getProgress() * 1000);
            }
        });
        this.mDecor.addView(this.mOverlay);
        this.mShowing = true;
        this.mSpacer = findViewById(R.id.player_overlay_spacer);
        this.mSpacer.setOnTouchListener(this.mTouchListener);
        if (getIntent().getAction() == null || !getIntent().getAction().equals("android.intent.action.VIEW")) {
            this.mVideoFileName = getIntent().getExtras().getString("file_name");
            this.mVideoFilePosition = getIntent().getExtras().getInt("file_position");
        }
        implSurfaceHolderCallback();
        playControl();
        this.mHandler = new Handler() { // from class: com.wificar.mediaplayer.MediaPlayerActivity.3
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 256:
                        MediaPlayerActivity.this.mIsAudioRender = false;
                        MediaPlayerActivity.this.mIsVideoRender = false;
                        MediaPlayerActivity.this.mIsPlaying = false;
                        MediaPlayerActivity.this.mIsStop = true;
                        MediaPlayerActivity.this.mPlayImgBtn.setBackgroundResource(R.drawable.ic_play);
                        MediaPlayerActivity.this.mCurrentTime = 0;
                        MediaPlayerActivity.this.mCurrentTimeText.setText(MediaPlayerActivity.this.timeFormat(MediaPlayerActivity.this.mCurrentTime));
                        MediaPlayerActivity.this.mSeekBar.setProgress(MediaPlayerActivity.this.mCurrentTime);
                        JNIWificarVideoPlay.playerStop();
                        MediaPlayerActivity.this.finish();
                        break;
                    case 257:
                        if (MediaPlayerActivity.this.mCurrentTime < 0) {
                            MediaPlayerActivity.this.mCurrentTime = 0;
                        }
                        MediaPlayerActivity.this.mCurrentTimeText.setText(MediaPlayerActivity.this.timeFormat(MediaPlayerActivity.this.mCurrentTime));
                        MediaPlayerActivity.this.mSeekBar.setProgress(MediaPlayerActivity.this.mCurrentTime);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override // android.app.Activity
    protected void onResume() {
        if (this.mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService("power");
            this.mWakeLock = pm.newWakeLock(6, "media player wakelook");
            this.mWakeLock.acquire();
        }
        super.onResume();
    }

    @Override // android.app.Activity
    protected void onPause() {
        this.mIsAudioRender = false;
        this.mIsVideoRender = false;
        while (!this.mIsVideoEnd && !this.mIsAudioEnd) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        JNIWificarVideoPlay.playerStop();
        this.mIsStop = true;
        this.mIsPlaying = false;
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
        finish();
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.mIsAudioRender = false;
        this.mIsVideoRender = false;
        this.mIsPlaying = false;
        this.mIsStop = true;
        this.mIsVideoSwitch = false;
        this.mIsFileEnd = true;
        super.onDestroy();
    }

    private void implSurfaceHolderCallback() {
        this.mWindowHolder = this.mWindow.getHolder();
        this.mWindowHolder.addCallback(new SurfaceHolder.Callback() { // from class: com.wificar.mediaplayer.MediaPlayerActivity.4
            @Override // android.view.SurfaceHolder.Callback
            public void surfaceCreated(SurfaceHolder holder) {
                MediaPlayerActivity.this.mWindowWidth = MediaPlayerActivity.this.mWindow.getWidth();
                MediaPlayerActivity.this.mWindowHeight = MediaPlayerActivity.this.mWindow.getHeight();
                if (MediaPlayerActivity.this.initPlayerStart() < 0) {
                }
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceDestroyed(SurfaceHolder holder) {
                MediaPlayerActivity.this.mIsVideoSwitch = true;
            }
        });
    }

    private void playControl() {
        setPlayButtonControl();
        setPreviousButtonControl();
        setNextButtonControl();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int initPlayerStart() {
        this.mIsVideoRender = false;
        this.mIsAudioRender = false;
        this.mIsPlaying = false;
        this.mIsStop = true;
        this.mIsVideoEnd = false;
        this.mIsAudioEnd = false;
        this.mIsFileEnd = false;
        if (getIntent().getAction() != null && getIntent().getAction().equals("android.intent.action.VIEW")) {
            this.mVideoFileName = getRealPath(getIntent().getData());
            this.mPreviousSongImgBtn.setVisibility(4);
            this.mPreviousSongImgBtn.setClickable(false);
            this.mNextSongImgBtn.setVisibility(4);
            this.mNextSongImgBtn.setClickable(false);
            this.calledbyother = true;
        }
        if (JNIWificarVideoPlay.playerInit(this.mVideoFileName) < 0) {
            System.out.println("init player failed");
            return -1;
        }
        this.mVideoWidth = JNIWificarVideoPlay.getVideoWidth();
        this.mVideoHeight = JNIWificarVideoPlay.getVideoHeight();
        changeSurfaceSize();
        if (JNIWificarVideoPlay.playerStart() < 0) {
            System.out.println("start player failed");
            return -1;
        }
        this.mDuration = JNIWificarVideoPlay.getDuration();
        this.mDurationText.setText(timeFormat(this.mDuration));
        this.mSeekBar.setMax(this.mDuration);
        this.mIsVideoRender = true;
        this.mIsAudioRender = true;
        this.mIsPlaying = true;
        this.mIsStop = false;
        this.mIsVideoSwitch = false;
        new videoRenderThread().start();
        new audioRenderThread().start();
        new playStopListenThread().start();
        new UpdateCurrentTimeThread().start();
        return 0;
    }

    private void setPlayButtonControl() {
        this.mPlayImgBtn.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.mediaplayer.MediaPlayerActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MediaPlayerActivity.this.mPlayImgBtn.setClickable(false);
                switch (v.getId()) {
                    case R.id.play /* 2131361843 */:
                        if (!MediaPlayerActivity.this.mIsFileEnd || !MediaPlayerActivity.this.mIsAudioEnd || !MediaPlayerActivity.this.mIsVideoEnd || MediaPlayerActivity.this.mIsPlaying) {
                            if (!MediaPlayerActivity.this.mIsPlaying) {
                                if (!MediaPlayerActivity.this.mIsPlaying && (!MediaPlayerActivity.this.mIsAudioEnd || !MediaPlayerActivity.this.mIsVideoEnd)) {
                                    JNIWificarVideoPlay.playerResume();
                                    MediaPlayerActivity.this.mPlayImgBtn.setBackgroundResource(R.drawable.ic_pause);
                                    MediaPlayerActivity.this.mIsPlaying = true;
                                    break;
                                }
                            } else {
                                JNIWificarVideoPlay.playerPause();
                                MediaPlayerActivity.this.mPlayImgBtn.setBackgroundResource(R.drawable.ic_play);
                                MediaPlayerActivity.this.mIsPlaying = false;
                                break;
                            }
                        } else {
                            MediaPlayerActivity.this.mVideoFileName = VideoGalleryActivity.getInstance().video_path1.get(MediaPlayerActivity.this.mVideoFilePosition);
                            if (MediaPlayerActivity.this.mVideoFileName != null && MediaPlayerActivity.this.initPlayerStart() >= 0) {
                                MediaPlayerActivity.this.mPlayImgBtn.setBackgroundResource(R.drawable.ic_pause);
                                break;
                            }
                        }
                        break;
                }
                MediaPlayerActivity.this.mPlayImgBtn.setClickable(true);
            }
        });
    }

    private void setPreviousButtonControl() {
        this.mPreviousSongImgBtn.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.mediaplayer.MediaPlayerActivity.6
            /* JADX WARN: Code restructure failed: missing block: B:11:0x007b, code lost:
                if (r5.this$0.mVideoFileName != null) goto L13;
             */
            /* JADX WARN: Code restructure failed: missing block: B:13:0x0083, code lost:
                if (r5.this$0.initPlayerStart() < 0) goto L3;
             */
            /* JADX WARN: Code restructure failed: missing block: B:14:0x0085, code lost:
                r5.this$0.mPlayImgBtn.setBackgroundResource(com.rover3.R.drawable.ic_pause);
             */
            /* JADX WARN: Code restructure failed: missing block: B:18:0x00c5, code lost:
                if (r5.this$0.mVideoFileName == null) goto L3;
             */
            @Override // android.view.View.OnClickListener
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct code enable 'Show inconsistent code' option in preferences
            */
            public void onClick(android.view.View r6) {
                /*
                    r5 = this;
                    r4 = 1
                    r2 = 0
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$37(r1)
                    r1.setClickable(r2)
                    int r1 = r6.getId()
                    switch(r1) {
                        case 2131361842: goto L1c;
                        default: goto L12;
                    }
                L12:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$37(r1)
                    r1.setClickable(r4)
                    return
                L1c:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$21(r1, r2)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$20(r1, r2)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$22(r1, r2)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$7(r1, r4)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$10(r1, r4)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$33(r1, r4)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$24(r1)
                    r2 = 2130837536(0x7f020020, float:1.7280029E38)
                    r1.setBackgroundResource(r2)
                    r1 = 1000(0x3e8, double:4.94E-321)
                    java.lang.Thread.sleep(r1)     // Catch: java.lang.InterruptedException -> L92
                L4b:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r1)
                    if (r1 <= 0) goto L97
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r2 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r1)
                    int r2 = r2 + (-1)
                    com.wificar.mediaplayer.MediaPlayerActivity.access$38(r1, r2)
                    com.wificar.mediaplayer.MediaPlayerActivity r2 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.VideoGalleryActivity r1 = com.wificar.VideoGalleryActivity.getInstance()
                    java.util.List<java.lang.String> r1 = r1.video_path1
                    com.wificar.mediaplayer.MediaPlayerActivity r3 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r3 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r3)
                    java.lang.Object r1 = r1.get(r3)
                    java.lang.String r1 = (java.lang.String) r1
                    com.wificar.mediaplayer.MediaPlayerActivity.access$35(r2, r1)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    java.lang.String r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$36(r1)
                    if (r1 == 0) goto L12
                L7d:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$32(r1)
                    if (r1 < 0) goto L12
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$24(r1)
                    r2 = 2130837534(0x7f02001e, float:1.7280025E38)
                    r1.setBackgroundResource(r2)
                    goto L12
                L92:
                    r0 = move-exception
                    r0.printStackTrace()
                    goto L4b
                L97:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.VideoGalleryActivity r2 = com.wificar.VideoGalleryActivity.getInstance()
                    java.util.List<java.lang.String> r2 = r2.video_path1
                    int r2 = r2.size()
                    int r2 = r2 + (-1)
                    com.wificar.mediaplayer.MediaPlayerActivity.access$38(r1, r2)
                    com.wificar.mediaplayer.MediaPlayerActivity r2 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.VideoGalleryActivity r1 = com.wificar.VideoGalleryActivity.getInstance()
                    java.util.List<java.lang.String> r1 = r1.video_path1
                    com.wificar.mediaplayer.MediaPlayerActivity r3 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r3 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r3)
                    java.lang.Object r1 = r1.get(r3)
                    java.lang.String r1 = (java.lang.String) r1
                    com.wificar.mediaplayer.MediaPlayerActivity.access$35(r2, r1)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    java.lang.String r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$36(r1)
                    if (r1 != 0) goto L7d
                    goto L12
                */
                throw new UnsupportedOperationException("Method not decompiled: com.wificar.mediaplayer.MediaPlayerActivity.AnonymousClass6.onClick(android.view.View):void");
            }
        });
    }

    private void setNextButtonControl() {
        this.mNextSongImgBtn.setOnClickListener(new View.OnClickListener() { // from class: com.wificar.mediaplayer.MediaPlayerActivity.7
            /* JADX WARN: Code restructure failed: missing block: B:11:0x0087, code lost:
                if (r5.this$0.mVideoFileName != null) goto L13;
             */
            /* JADX WARN: Code restructure failed: missing block: B:13:0x008f, code lost:
                if (r5.this$0.initPlayerStart() < 0) goto L3;
             */
            /* JADX WARN: Code restructure failed: missing block: B:14:0x0091, code lost:
                r5.this$0.mPlayImgBtn.setBackgroundResource(com.rover3.R.drawable.ic_pause);
             */
            /* JADX WARN: Code restructure failed: missing block: B:18:0x00c6, code lost:
                if (r5.this$0.mVideoFileName == null) goto L3;
             */
            @Override // android.view.View.OnClickListener
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct code enable 'Show inconsistent code' option in preferences
            */
            public void onClick(android.view.View r6) {
                /*
                    r5 = this;
                    r4 = 1
                    r3 = 0
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$39(r1)
                    r1.setClickable(r3)
                    int r1 = r6.getId()
                    switch(r1) {
                        case 2131361844: goto L1c;
                        default: goto L12;
                    }
                L12:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$39(r1)
                    r1.setClickable(r4)
                    return
                L1c:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$21(r1, r3)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$20(r1, r3)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$22(r1, r3)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$7(r1, r4)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$10(r1, r4)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$33(r1, r4)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$24(r1)
                    r2 = 2130837536(0x7f020020, float:1.7280029E38)
                    r1.setBackgroundResource(r2)
                    r1 = 1000(0x3e8, double:4.94E-321)
                    java.lang.Thread.sleep(r1)     // Catch: java.lang.InterruptedException -> L9f
                L4b:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r1)
                    com.wificar.VideoGalleryActivity r2 = com.wificar.VideoGalleryActivity.getInstance()
                    java.util.List<java.lang.String> r2 = r2.video_path1
                    int r2 = r2.size()
                    int r2 = r2 + (-1)
                    if (r1 >= r2) goto La4
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r2 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r1)
                    int r2 = r2 + 1
                    com.wificar.mediaplayer.MediaPlayerActivity.access$38(r1, r2)
                    com.wificar.mediaplayer.MediaPlayerActivity r2 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.VideoGalleryActivity r1 = com.wificar.VideoGalleryActivity.getInstance()
                    java.util.List<java.lang.String> r1 = r1.video_path1
                    com.wificar.mediaplayer.MediaPlayerActivity r3 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r3 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r3)
                    java.lang.Object r1 = r1.get(r3)
                    java.lang.String r1 = (java.lang.String) r1
                    com.wificar.mediaplayer.MediaPlayerActivity.access$35(r2, r1)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    java.lang.String r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$36(r1)
                    if (r1 == 0) goto L12
                L89:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$32(r1)
                    if (r1 < 0) goto L12
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    android.widget.ImageButton r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$24(r1)
                    r2 = 2130837534(0x7f02001e, float:1.7280025E38)
                    r1.setBackgroundResource(r2)
                    goto L12
                L9f:
                    r0 = move-exception
                    r0.printStackTrace()
                    goto L4b
                La4:
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.mediaplayer.MediaPlayerActivity.access$38(r1, r3)
                    com.wificar.mediaplayer.MediaPlayerActivity r2 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    com.wificar.VideoGalleryActivity r1 = com.wificar.VideoGalleryActivity.getInstance()
                    java.util.List<java.lang.String> r1 = r1.video_path1
                    com.wificar.mediaplayer.MediaPlayerActivity r3 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    int r3 = com.wificar.mediaplayer.MediaPlayerActivity.access$34(r3)
                    java.lang.Object r1 = r1.get(r3)
                    java.lang.String r1 = (java.lang.String) r1
                    com.wificar.mediaplayer.MediaPlayerActivity.access$35(r2, r1)
                    com.wificar.mediaplayer.MediaPlayerActivity r1 = com.wificar.mediaplayer.MediaPlayerActivity.this
                    java.lang.String r1 = com.wificar.mediaplayer.MediaPlayerActivity.access$36(r1)
                    if (r1 != 0) goto L89
                    goto L12
                */
                throw new UnsupportedOperationException("Method not decompiled: com.wificar.mediaplayer.MediaPlayerActivity.AnonymousClass7.onClick(android.view.View):void");
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AudioTrack createPlayer(int samplerate, int channels, int format) {
        int fmt;
        int chls = 12;
        switch (channels) {
            case 1:
                chls = 4;
                break;
            case 2:
                chls = 12;
                break;
        }
        switch (format) {
            case 0:
                fmt = 3;
                break;
            case 1:
                fmt = 2;
                break;
            default:
                fmt = 0;
                break;
        }
        if (fmt == 0) {
            return null;
        }
        int minBufSize = AudioTrack.getMinBufferSize(samplerate, chls, fmt);
        AudioTrack audioPlayer = new AudioTrack(3, samplerate, chls, fmt, minBufSize, 1);
        return audioPlayer;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap rgb565ToBitmap(byte[] data) {
        Bitmap bitmap = Bitmap.createBitmap(this.mVideoWidth, this.mVideoHeight, Bitmap.Config.RGB_565);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    public void showBitMap(Bitmap bm, Canvas canvas) {
        float scaleWidth;
        float scaleHeight;
        Bitmap newbm;
        if (bm != null) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            if (this.mWindowWidth != width || this.mWindowHeight != height) {
                if ((this.mWindowWidth >= 1024) & (((double) this.density) >= 1.0d)) {
                    scaleWidth = (this.mWindowWidth / (this.density * 1.3f)) / width;
                    scaleHeight = (this.mWindowHeight / (this.density * 1.3f)) / height;
                } else if (this.density < 1.0d) {
                    scaleWidth = (this.mWindowWidth / (this.density * 2.4f)) / width;
                    scaleHeight = (this.mWindowHeight / (this.density * 2.4f)) / height;
                } else {
                    if ((this.mWindowWidth < 1024) & (((double) this.density) >= 1.0d)) {
                        scaleWidth = (this.mWindowWidth / (this.density * 1.3f)) / width;
                        scaleHeight = (this.mWindowHeight / (this.density * 1.3f)) / height;
                    } else {
                        scaleWidth = (this.mWindowWidth / (this.density * 1.3f)) / width;
                        scaleHeight = (this.mWindowHeight / (this.density * 1.3f)) / height;
                    }
                }
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
                bm.recycle();
            } else {
                newbm = bm;
            }
            try {
                synchronized (this.mWindowHolder) {
                    canvas = this.mWindowHolder.lockCanvas();
                }
                newbm.recycle();
            } finally {
                if (canvas != null) {
                    canvas.drawARGB(0, 0, 0, 0);
                    canvas.drawBitmap(newbm, 0.0f, 0.0f, (Paint) null);
                    this.mWindowHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class videoRenderThread extends Thread {
        videoRenderThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (MediaPlayerActivity.this.mIsVideoRender) {
                if (MediaPlayerActivity.this.mIsPlaying) {
                    byte[] frame = JNIWificarVideoPlay.videoRender();
                    if (frame != null && frame.length != 0) {
                        Bitmap bm = MediaPlayerActivity.this.rgb565ToBitmap(frame);
                        MediaPlayerActivity.this.showBitMap(bm, null);
                    } else {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (MediaPlayerActivity.this.mIsFileEnd) {
                            MediaPlayerActivity.this.mIsVideoEnd = true;
                        }
                    }
                } else {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            MediaPlayerActivity.this.mIsVideoEnd = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class audioRenderThread extends Thread {
        audioRenderThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            byte[] sampleRateArray = new byte[4];
            byte[] channelsArray = new byte[4];
            byte[] formatArray = new byte[4];
            AudioTrack audioPlayer = null;
            while (MediaPlayerActivity.this.mIsAudioRender) {
                if (MediaPlayerActivity.this.mIsPlaying) {
                    byte[] buf = JNIWificarVideoPlay.audioRender(sampleRateArray, sampleRateArray.length, channelsArray, channelsArray.length, formatArray, formatArray.length);
                    if (buf == null || buf.length == 0) {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (MediaPlayerActivity.this.mIsFileEnd) {
                            MediaPlayerActivity.this.mIsAudioEnd = true;
                        }
                    } else {
                        if (audioPlayer == null) {
                            int sampleRate = MediaPlayerActivity.this.byteArray2Int(sampleRateArray);
                            int channles = MediaPlayerActivity.this.byteArray2Int(channelsArray);
                            int format = MediaPlayerActivity.this.byteArray2Int(formatArray);
                            audioPlayer = MediaPlayerActivity.this.createPlayer(sampleRate, channles, format);
                            if (audioPlayer != null) {
                                audioPlayer.play();
                            }
                        }
                        audioPlayer.write(buf, 0, buf.length);
                    }
                } else {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            audioPlayer.flush();
            audioPlayer.stop();
            audioPlayer.release();
            MediaPlayerActivity.this.mIsAudioEnd = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class playStopListenThread extends Thread {
        playStopListenThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                if (JNIWificarVideoPlay.playerIsStop() != 0) {
                    MediaPlayerActivity.this.mIsFileEnd = true;
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (MediaPlayerActivity.this.mIsAudioEnd && MediaPlayerActivity.this.mIsVideoEnd) {
                        Message message = new Message();
                        message.what = 256;
                        MediaPlayerActivity.this.mHandler.sendMessage(message);
                        return;
                    }
                } else if (!MediaPlayerActivity.this.mIsStop && !MediaPlayerActivity.this.mIsVideoSwitch) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                } else {
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class UpdateCurrentTimeThread extends Thread {
        UpdateCurrentTimeThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (!MediaPlayerActivity.this.mIsStop && !MediaPlayerActivity.this.mIsVideoSwitch) {
                MediaPlayerActivity.this.mCurrentTime = JNIWificarVideoPlay.getCurrentTime();
                Message message = new Message();
                message.what = 257;
                MediaPlayerActivity.this.mHandler.sendMessage(message);
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int byteArray2Int(byte[] data) {
        int value = (data[0] & 255) | ((data[1] & 255) << 8) | ((data[2] & 255) << 16) | ((data[3] & 255) << 24);
        return value;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String timeFormat(int seconds) {
        int hour = seconds / 3600;
        int minute = (seconds - (hour * 3600)) / 60;
        int second = (seconds - (hour * 3600)) - (minute * 60);
        String fmtminute = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
        String fmtsecond = second < 10 ? "0" + String.valueOf(second) : String.valueOf(second);
        String timeStr = String.valueOf(String.valueOf(hour)) + ":" + fmtminute + ":" + fmtsecond;
        return timeStr;
    }

    private void changeSurfaceSize() {
        int dw = getWindowManager().getDefaultDisplay().getWidth();
        int dh = getWindowManager().getDefaultDisplay().getHeight();
        double ar = this.mVideoWidth / this.mVideoHeight;
        double d = dw / dh;
        int dw2 = (int) (dh * ar);
        this.mSurfaceHolder.setFixedSize(this.mVideoWidth, this.mVideoHeight);
        ViewGroup.LayoutParams lp = this.mWindow.getLayoutParams();
        lp.width = dw2;
        lp.height = dh;
        this.mWindowHeight = dh;
        this.mWindowWidth = dw2;
        this.mWindow.setLayoutParams(lp);
        this.mWindow.invalidate();
    }

    private String getRealPath(Uri fileUrl) {
        String fileName = null;
        if (fileUrl != null) {
            if (fileUrl.getScheme().toString().compareTo("content") == 0) {
                Cursor cursor = getApplicationContext().getContentResolver().query(fileUrl, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    fileName = cursor.getString(column_index);
                    AppLog.e("VideoPlayerActivity", "the content to path :" + fileName);
                    cursor.close();
                }
            } else if (fileUrl.getScheme().compareTo("file") == 0) {
                fileUrl.toString();
                fileName = fileUrl.toString().replace("file://", "");
            }
        }
        AppLog.e("videoPlayerActivty", "the realPath:" + fileName);
        return fileName;
    }
}
