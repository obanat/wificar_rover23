package com.obana.rover;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Vander on 2018/7/16.
 * Modify by obana on 2022/6/4.
 */

public class WheelView extends View implements View.OnTouchListener{

    Context mContext;
    float scale;

    private boolean isInit;
    private boolean mNeedRotate;
    private final double PI4 = Math.PI / 4;

    public WheelView(Context context) {
        super(context);
        isInit = true;
        mContext = context;
        mNeedRotate = false;
        scale = context.getResources().getDisplayMetrics().density ;
        this.setBackgroundColor(Color.TRANSPARENT);
        initWheelView();
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isInit = true;
        mContext = context;
        scale = context.getResources().getDisplayMetrics().density ;
        this.setBackgroundColor(Color.TRANSPARENT);
        mNeedRotate = attrs.getAttributeBooleanValue(null, "need_rotate",false);
        big_bg = attrs.getAttributeResourceValue(null, "big_circle_src",0);
        small_bg = attrs.getAttributeResourceValue(null, "small_circle_src", 0);
        initWheelView();

    }

    public WheelView(Context context, AttributeSet attrs, int paramInt) {
        super(context, attrs, paramInt);
        isInit = true;
        mContext = context;
        scale = context.getResources().getDisplayMetrics().density ;
        this.setBackgroundColor(Color.TRANSPARENT);
        mNeedRotate = attrs.getAttributeBooleanValue(null, "need_rotate",false);
        big_bg = attrs.getAttributeResourceValue(null, "big_circle_src",0);
        small_bg = attrs.getAttributeResourceValue(null, "small_circle_src", 0);
        initWheelView();

    }

    private int wheel_gravity;
    public void setWheelGravity(int gravity){
        wheel_gravity = gravity;
    }

    public void getWheelGravity(){
        switch (wheel_gravity){
            case Gravity.LEFT:
                iniCenter(bigcircle_radius,getHeight() / 2 - bigcircle_radius);
                break;
            case Gravity.RIGHT:
                iniCenter(getWidth() - bigcircle_radius,getHeight() / 2 - bigcircle_radius);
                break;
            case Gravity.TOP:
                iniCenter(getWidth() / 2 - bigcircle_radius,bigcircle_radius);
                break;
            case Gravity.BOTTOM:
                iniCenter(getWidth() / 2 - bigcircle_radius,getHeight() - bigcircle_radius);
                break;
            case Gravity.LEFT|Gravity.TOP:
                iniCenter(bigcircle_radius, bigcircle_radius);
                break;

            case Gravity.RIGHT|Gravity.TOP:
                iniCenter(bigcircle_radius, getWidth() - bigcircle_radius);
                break;

            case Gravity.LEFT|Gravity.BOTTOM:
                iniCenter(bigcircle_radius, getHeight() - bigcircle_radius);
                break;

            case Gravity.RIGHT|Gravity.BOTTOM:
                iniCenter(getWidth() - bigcircle_radius, getHeight() - bigcircle_radius);
                break;

            default://center
                iniCenter(getWidth() / 2 , getHeight()/2 );
                break;
        }
    }

    private float initCenterX, initCenterY;
    public void iniCenter(float x, float y){
        initCenterX = x;
        initCenterY = y;
        setCenter(initCenterX, initCenterY);
        invalidate();
    }

    private float centerX, centerY;
    public void setCenter(float x, float y){
        centerX = x;
        centerY = y;
        moveCenterX = x;
        moveCenterY = y;
        invalidate();
    }

    private float moveCenterX, moveCenterY;

    private int big_bg;
    private Bitmap bigCircle;
    private Paint bigCirclePaint;
    private int bigcircle_radius;;

    private int small_bg;
    private Bitmap smallCircle;
    private Paint smallCirclePaint;
    private int smallcircle_radius;
    //private int controllerSize;

    private void initWheelView(){
        bigCirclePaint = new Paint(1);
        smallCirclePaint = new Paint(1);
        //int controlsize = controllerSize
        bigcircle_radius = dip2px(100);
        smallcircle_radius = bigcircle_radius / 2;
        setWheelGravity(0);//center

        InputStream is = getResources().openRawResource(big_bg);
        Bitmap tempMap = BitmapFactory.decodeStream(is);
        bigCircle = Bitmap.createScaledBitmap(tempMap, bigcircle_radius * 2, bigcircle_radius * 2, true);

        is = getResources().openRawResource(small_bg);
        tempMap = BitmapFactory.decodeStream(is);
        smallCircle = Bitmap.createScaledBitmap(tempMap, smallcircle_radius * 2, smallcircle_radius * 2, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = measure(widthMeasureSpec);
        int h = measure(heightMeasureSpec);
        setMeasuredDimension(w, h);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measure(int paramInt) {
        int i = MeasureSpec.getMode(paramInt);
        int j = MeasureSpec.getSize(paramInt);
        if (i == 0)// android:layout_width="wrap_content"
            return dip2px(200);
        return j;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInit) {
            getWheelGravity();
            isInit = false;
        }
        if (mNeedRotate) {
            Matrix matrix = new Matrix();

            matrix.postRotate(this.lastAngle, (float) bigcircle_radius, (float) bigcircle_radius);
            matrix.postTranslate((float) centerX - bigcircle_radius, (float) centerY - bigcircle_radius);

            //new add feature: add direction indication
            //Log.e("WHEELVIEW","move, centerX:" + centerX+ ":" + centerY + ":" + bigcircle_radius);
            canvas.drawBitmap(bigCircle, matrix, bigCirclePaint);
            canvas.drawBitmap(smallCircle, moveCenterX - smallcircle_radius, moveCenterY - smallcircle_radius, smallCirclePaint);

        } else {
            canvas.drawBitmap(bigCircle, centerX-bigcircle_radius, centerY-bigcircle_radius, bigCirclePaint);
            canvas.drawBitmap(smallCircle, moveCenterX - smallcircle_radius, moveCenterY - smallcircle_radius, smallCirclePaint);
        }
    }

    public int dip2px(float dipValue)
    {
        return (int)(dipValue * scale + 0.5f) ;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    private float position_X,position_Y;
    public float lastAngle,lastDistance;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            setCenter(event.getX(), event.getY());
            if(centerX < bigcircle_radius){
                centerX = bigcircle_radius;
            } else if (centerX > (getWidth() - bigcircle_radius)){
                centerX = getWidth() - bigcircle_radius;
            }
            if(centerY < bigcircle_radius){
                centerY = bigcircle_radius;
            } else if (centerY > (getHeight() - bigcircle_radius)){
                centerY = getWidth() - bigcircle_radius;
            }

            this.position_X = (int) event.getX();
            this.position_Y = (int) event.getY();

            if(wheelViewMoveListener != null){
                if ((this.thread != null) && (this.thread.isAlive()))
                    this.thread.interrupt();
                //this.thread = new Thread(runnable);
                //this.thread.start();//remove useless code

                this.lastAngle = 1;//getAngle();

                if (this.wheelViewMoveListener != null)
                    this.wheelViewMoveListener.onValueChanged(100, this.lastAngle, getDistance());
            }


        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            this.position_X = (int) event.getX();
            this.position_Y = (int) event.getY();
            double d = Math.sqrt(
                    Math.pow(this.position_X - this.centerX, 2) +
                            Math.pow(this.position_Y - this.centerY, 2));

            if (d > this.bigcircle_radius) {
                this.position_X = (int) ((this.position_X - this.centerX)
                        * this.bigcircle_radius / d + this.centerX);
                this.position_Y = (int) ((this.position_Y - this.centerY)
                        * this.bigcircle_radius / d + this.centerY);
            }
            moveCenterX = this.position_X;
            moveCenterY = this.position_Y;

            this.lastAngle = getAngle();
            this.lastDistance = getDistance();

            invalidate();

            if (this.wheelViewMoveListener != null)
                this.wheelViewMoveListener.onValueChanged(200, this.lastAngle, getDistance());


        } else if(event.getAction() == MotionEvent.ACTION_UP){
            setCenter(initCenterX,initCenterY);
            this.position_X = (int) this.centerX;
            this.position_Y = (int) this.centerY;
            if ((this.thread != null) && (this.thread.isAlive()))
                this.thread.interrupt();

            this.lastAngle = getAngle();


            if (this.wheelViewMoveListener != null)
                this.wheelViewMoveListener.onValueChanged(300, this.lastAngle, getDistance());
        }
        invalidate();

        return true;
    }



    private float getAngle() {
        if (this.position_X > this.centerX) {
            if (this.position_Y < this.centerY) {
                float m = (float) (90.0D + 57.295779500000002D * Math
                        .atan((position_Y - this.centerY)
                                / (this.position_X - this.centerX)));

                this.lastAngle = m;
                return m;
            }
            if (this.position_Y > this.centerY) {
                float k = 90 + (float) (57.295779500000002D * Math
                        .atan((this.position_Y - this.centerY)
                                / (this.position_X - this.centerX)));

                this.lastAngle = k;
                return k;
            }
            this.lastAngle = 90;
            return 90;
        }
        if (this.position_X < this.centerX) {
            if (this.position_Y < this.centerY) {
                float j = (float) (57.295779500000002D * Math
                        .atan((this.position_Y - this.centerY)
                                / (this.position_X - this.centerX)) - 90.0D);
                j = (j + 360) % 360;
                this.lastAngle = j;
                return j;
            }
            if (this.position_Y > this.centerY) {
                float i = -90	+ (float) (57.295779500000002D * Math
                        .atan((this.position_Y - this.centerY)
                                / (this.position_X - this.centerX)));
                i = (i + 360) % 360;
                this.lastAngle = i;
                return i;
            }
            this.lastAngle = 270;
            return 270;
        }
        if (this.position_Y == this.centerY) {
            this.lastAngle = 0;
            return 0;
        }
        if (this.lastAngle < 0) {
            this.lastAngle = 0;
            return 0;
        }
        this.lastAngle = 180;
        return 180;
    }

    private float getDistance() {
        this.lastDistance= (float)(100.0D * Math.sqrt(
                Math.pow(this.position_X - this.centerX, 2) +
                        Math.pow(this.position_Y - this.centerY, 2)
        ) / this.bigcircle_radius);
        return lastDistance;
    }


    private long loopInterval = 100L;
    private Thread thread;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (Thread.interrupted())
                    return;
                post(new Runnable() {
                    public void run() {
                        if (WheelView.this.wheelViewMoveListener != null)
                            WheelView.this.wheelViewMoveListener.onValueChanged(200,
                                    WheelView.this.lastAngle,
                                    WheelView.this.getDistance());
                    }
                });
                try {
                    Thread.sleep(WheelView.this.loopInterval);
                } catch (InterruptedException localInterruptedException) {
                }
            }
        }
    };


    OnWheelViewMoveListener wheelViewMoveListener;
    public void setOnWheelViewMoveListener(OnWheelViewMoveListener listener){
        wheelViewMoveListener = listener;
    }

    public void setOnWheelViewMoveListener(OnWheelViewMoveListener listener, long paramLong) {
        this.wheelViewMoveListener = listener;
        loopInterval = paramLong;
    }

    public abstract interface OnWheelViewMoveListener {
        public abstract void onValueChanged(int status, float angle, float distance);
    }

}
