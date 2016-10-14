package com.stang.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import static java.lang.Math.abs;


/**
 * Created by StanG on 13.10.2016.
 */

public class MyView extends View {
    public static final String TAG = "app";
    public int TIMELINE_MIN = -150;
    public int TIMELINE_MAX = 200;

    private int reverseTimeline = 1;
    private boolean isRunning = false;

    Paint linePaint;
    Paint dotPaint;
    Paint centerPaint;
    Dot[] corners;

    private int height = 0;
    private int width = 0;
    private int mSpeed = 50;
    private int mRepeat = 0;
    private int mDirection = 2;
    private int mLineColor = Color.BLACK;
    private int mLineWidth = 2;
    private int mDotFigure = 0;
    private int mDotWidth = 20;
    private int mDotColor = Color.BLUE;
    private int radius = 20;
    private float timeline = TIMELINE_MIN;
    private long repeatedCycles = 0;
    private Drawable mCustomImage;

    OnAnimationEventListener animationListener = null;


    public MyView(Context context) {
        super(context);
    }


    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyView,
                0, 0
        );

        try {
            mLineColor = a.getColor(R.styleable.MyView_line_color, 0xff000000);
            mDotColor = a.getColor(R.styleable.MyView_dot_color, 0xff000000);
            mSpeed = a.getInt(R.styleable.MyView_speed_animation, 100);
            mRepeat = a.getInt(R.styleable.MyView_repeat_counts, 0);
            mDirection = a.getInt(R.styleable.MyView_direction, 2);
            mDotFigure = a.getInt(R.styleable.MyView_dot_figure, 1);
            mDotWidth = a.getDimensionPixelSize(R.styleable.MyView_dot_width, 10);
            mLineWidth = a.getDimensionPixelSize(R.styleable.MyView_line_width, 1);

        } finally {
            a.recycle();
        }

        linePaint = new Paint();
        linePaint.setColor(mLineColor);
        linePaint.setStrokeWidth(mLineWidth);

        dotPaint = new Paint();
        dotPaint.setColor(mDotColor);

        centerPaint = new Paint();
        centerPaint.setColor(mDotColor);

        mCustomImage = context.getResources().getDrawable(R.drawable.ok_);

        init();

    }


    private void init(){
        switch (mDirection){
            case 0:
            case 2:
                timeline = TIMELINE_MIN;
                reverseTimeline = 1;
                break;
            case 1:
                timeline = TIMELINE_MAX;
                reverseTimeline = -1;
                break;
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;

        int centerX = width/2;
        int centerY = height/2;
        radius = mDotWidth;

        Log.d(TAG, "onSizeChanged: " + height + ":" + width + "   center: " + centerX + ":" + centerY + "   radius: " + radius);

        corners = new Dot[4];
        corners[0] = new Dot(radius, radius, centerX, centerY, false);
        corners[1] = new Dot(width-radius, radius,centerX, centerY, true);
        corners[2] = new Dot(width-radius, height-radius, centerX, centerY, false);
        corners[3] = new Dot(radius, height-radius, centerX, centerY, true);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        corners[0].calcPosition(timeline);
        corners[3].calcPosition(timeline - 30);
        corners[2].calcPosition(timeline - 60);
        corners[1].calcPosition(timeline - 90);

        canvas.drawLine(corners[0].getX(), corners[0].getY(), corners[1].getX(), corners[1].getY(), linePaint);
        canvas.drawLine(corners[0].getX(), corners[0].getY(), corners[2].getX(), corners[2].getY(), linePaint);
        canvas.drawLine(corners[0].getX(), corners[0].getY(), corners[3].getX(), corners[3].getY(), linePaint);

        canvas.drawLine(corners[1].getX(), corners[1].getY(), corners[2].getX(), corners[2].getY(), linePaint);
        canvas.drawLine(corners[1].getX(), corners[1].getY(), corners[3].getX(), corners[3].getY(), linePaint);

        canvas.drawLine(corners[2].getX(), corners[2].getY(), corners[3].getX(), corners[3].getY(), linePaint);


        switch (mDotFigure){
            case 0: //none
                break;

            case 1:
                if(timeline < -30){
                    centerPaint.setAlpha(abs(((int)timeline%25))*10);
                    canvas.drawRect(height/2-radius*2, width/2-radius*2, height/2+radius*2, width/2+radius*2, centerPaint);
                }
                for (int i = 0; i < corners.length; i++) {
                    canvas.drawRect(corners[i].getX()- radius, corners[i].getY()- radius, corners[i].getX()+ radius, corners[i].getY()+ radius, dotPaint);
                }
                break;

            case 2:
                if(timeline < -30){
                    centerPaint.setAlpha(abs(((int)timeline%25))*10);
                    canvas.drawCircle(height/2, width/2, radius*2, centerPaint);
                }
                for (int i = 0; i < corners.length; i++) {
                    canvas.drawCircle(corners[i].getX(), corners[i].getY(), radius, dotPaint);
                }
                break;

            case 3:
                if(timeline < -30){
                    mCustomImage.setAlpha(abs(((int)timeline%25))*10);
                    mCustomImage.setBounds((int)(height/2-radius*2), (int)(width/2-radius*2), (int)(height/2+radius*2), (int)(width/2+radius*2));
                    mCustomImage.draw(canvas);
                }
                for (int i = 0; i < corners.length; i++) {
                    mCustomImage.setAlpha(255);
                    mCustomImage.setBounds(corners[i].getX()- radius, corners[i].getY()- radius, corners[i].getX()+ radius, corners[i].getY()+ radius);
                    mCustomImage.draw(canvas);
                }
                break;
        }

    }


    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            if (timeline > TIMELINE_MAX) {
                animationCollapsed();
                reverseTimeline = -1;
                if(mDirection == 2 && (mRepeat==0 || repeatedCycles < mRepeat)){
                    repeatedCycles++;
                } else {
                    stopAnimation();
                };
            } else
            if (timeline < TIMELINE_MIN) {
                animationExploded();
                reverseTimeline = 1;
                if(mDirection == 2 && (mRepeat==0 || repeatedCycles < mRepeat)){
                    repeatedCycles++;
                } else {
                    stopAnimation();
                };
            }

            float step = 0.03f * mSpeed;
            timeline += (reverseTimeline * step);

            invalidate();

            if(isRunning) {
                postDelayed(this, 30);
            }

        }
    };


    public void setSpeed(int mSpeed) {
        this.mSpeed = mSpeed;
    }

    public void setLineColor(int mLineColor) {
        this.mLineColor = mLineColor;
    }

    public void setLineWidth(int mLineWidth) {
        this.mLineWidth = mLineWidth;
    }

    public void setDotFigure(int mDotFigure) {
        this.mDotFigure = mDotFigure;
    }

    public void setDotWidth(int mDotWidth) {
        this.mDotWidth = mDotWidth;
    }

    public void setDotColor(int mDotColor) {
        this.mDotColor = mDotColor;
    }

    public void setCustomImage(Drawable mCustomImage) {
        this.mCustomImage = mCustomImage;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public int getLineWidth() {
        return mLineWidth;
    }

    public int getDotFigure() {
        return mDotFigure;
    }

    public int getDotWidth() {
        return mDotWidth;
    }

    public int getDotColor() {
        return mDotColor;
    }

    public Drawable getCustomImage() {
        return mCustomImage;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void startAnimation() {
        init();
        repeatedCycles = 0;
        removeCallbacks(animator);
        post(animator);
        isRunning = true;
        animationStarted();
    }

    public void stopAnimation() {
        isRunning = false;
        removeCallbacks(animator);
        animationStopped();
    }

    public void setOnAnimationEventListener(OnAnimationEventListener listener) {
        animationListener = listener;
    }

    public interface OnAnimationEventListener {
        public abstract void onAnimationStarted();
        public abstract void onAnimationStopped();
        public abstract void onAnimationCollapsed();
        public abstract void onAnimationExploded();
    }

    private void animationStarted() {
        if (animationListener != null) {
            animationListener.onAnimationStarted();
        }
    }

    private void animationStopped() {
        if (animationListener != null) {
            animationListener.onAnimationStopped();
        }
    }

    private void animationCollapsed() {
        if (animationListener != null) {
            animationListener.onAnimationCollapsed();
        }
    }

    private void animationExploded() {
        if (animationListener != null) {
            animationListener.onAnimationExploded();
        }
    }


}


