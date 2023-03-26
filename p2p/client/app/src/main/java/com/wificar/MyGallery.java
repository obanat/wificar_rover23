package com.wificar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

/* loaded from: classes.dex */
public class MyGallery extends Gallery {
    public MyGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
        return e2.getX() > e1.getX();
    }

    @Override // android.widget.Gallery, android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int keyCode;
        if (isScrollingLeft(e1, e2)) {
            keyCode = 21;
        } else {
            keyCode = 22;
        }
        onKeyDown(keyCode, null);
        return true;
    }
}
