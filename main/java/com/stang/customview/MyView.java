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

    public static final int DIRECTION_FORWARD = 0;
    public static final int DIRECTION_BACKWARD = 1;
    public static final int DIRECTION_ROUND = 2;

    public static final int FIGURE_NONE = 0;
    public static final int FIGURE_RECTANGLE = 1;
    public static final int FIGURE_CIRCLE= 2;
    public static final int FIGURE_IMAGE = 3;

    public int TIMELINE_MIN = -150;
    public int TIMELINE_MAX = 200;

    private int mReverseTimeline = 1;
    private boolean isRunning = false;

    Paint mLinePaint;
    Paint mDotPaint;
    Paint mCenterPaint;
    Dot[] mCorners;

    private int mHeight = 0;
    private int mWidth = 0;
    private int mSpeed = 100;
    private int mRepeat = 1;
    private int mDirection = DIRECTION_ROUND;
    private int mLineColor = Color.BLACK;
    private int mLineWidth = 2;
    private int mDotFigure = FIGURE_NONE;
    private int mDotWidth = 20;
    private int mDotColor = Color.BLUE;
    private int mRadius = 20;
    private float mTimeline = TIMELINE_MIN;
    private long mRepeatedCycles = 0;
    private Drawable mCustomImage;

    OnAnimationEventListener animationListener = null;

    public void setOnAnimationEventListener(OnAnimationEventListener listener) {
        animationListener = listener;
    }

    public interface OnAnimationEventListener {
        void onAnimationStarted();
        void onAnimationStopped();
        void onAnimationCollapsed();
        void onAnimationExploded();
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

    public void startAnimation() {
        initTimeline();
        mRepeatedCycles = 0;
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


    public void setSpeed(int speed) { mSpeed = speed; setPaintProperties(); invalidate(); }

    public void setLineColor(int lineColor) { mLineColor = lineColor; setPaintProperties(); invalidate(); }

    public void setLineWidth(int lineWidth) { mLineWidth = lineWidth; setPaintProperties(); invalidate(); }

    public void setDotFigure(int dotFigure) { mDotFigure = dotFigure; setPaintProperties(); invalidate(); }

    public void setDotWidth(int dotWidth) { mDotWidth = dotWidth; setPaintProperties(); invalidate(); }

    public void setDotColor(int dotColor) { mDotColor = dotColor; setPaintProperties(); invalidate(); }

    public void setCustomImage(Drawable customImage) { mCustomImage = customImage; setPaintProperties(); invalidate(); }

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
            mLineColor = a.getColor(R.styleable.MyView_line_color, Color.BLACK);
            mDotColor = a.getColor(R.styleable.MyView_dot_color, Color.BLACK);
            mSpeed = a.getInt(R.styleable.MyView_speed_animation, 100);
            mRepeat = a.getInt(R.styleable.MyView_repeat_counts, 0);
            mDirection = a.getInt(R.styleable.MyView_direction, DIRECTION_ROUND);
            mDotFigure = a.getInt(R.styleable.MyView_dot_figure, FIGURE_CIRCLE);
            mDotWidth = a.getDimensionPixelSize(R.styleable.MyView_dot_width, 10);
            mLineWidth = a.getDimensionPixelSize(R.styleable.MyView_line_width, 1);
        } finally {
            a.recycle();
        }

        mLinePaint = new Paint();
        mDotPaint = new Paint();
        mCenterPaint = new Paint();
        mCustomImage = context.getResources().getDrawable(R.drawable.ok_);

        setPaintProperties();
        initTimeline();
    }


    private void setPaintProperties(){
        mCenterPaint.setColor(mDotColor);
        mDotPaint.setColor(mDotColor);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineWidth);
    }

    private void initTimeline(){
        switch (mDirection){
            case DIRECTION_FORWARD:
            case DIRECTION_ROUND:
                mTimeline = TIMELINE_MIN;
                mReverseTimeline = 1;
                break;
            case DIRECTION_BACKWARD:
                mTimeline = TIMELINE_MAX;
                mReverseTimeline = -1;
                break;
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;

        int centerX = mWidth /2;
        int centerY = mHeight /2;
        mRadius = mDotWidth;

        Log.d(TAG, "onSizeChanged: " + mHeight + ":" + mWidth + "   center: " + centerX + ":" + centerY + "   mRadius: " + mRadius);

        mCorners = new Dot[4];
        mCorners[0] = new Dot(mRadius, mRadius, centerX, centerY, false);
        mCorners[1] = new Dot(mWidth - mRadius, mRadius,centerX, centerY, true);
        mCorners[2] = new Dot(mWidth - mRadius, mHeight - mRadius, centerX, centerY, false);
        mCorners[3] = new Dot(mRadius, mHeight - mRadius, centerX, centerY, true);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        mCorners[0].calcPosition(mTimeline);
        mCorners[3].calcPosition(mTimeline - 30);
        mCorners[2].calcPosition(mTimeline - 60);
        mCorners[1].calcPosition(mTimeline - 90);

        drawLines(canvas);

        drawDots(canvas);
    }


    private void drawDots(Canvas canvas) {
        switch (mDotFigure){
            case FIGURE_NONE: //none
                break;

            case FIGURE_RECTANGLE:
                if(mTimeline < -30){
                    mCenterPaint.setAlpha(abs(((int) mTimeline %25))*10);
                    canvas.drawRect(mHeight /2- mRadius *2, mWidth /2- mRadius *2, mHeight /2+ mRadius *2, mWidth /2+ mRadius *2, mCenterPaint);
                }
                for (int i = 0; i < mCorners.length; i++) {
                    canvas.drawRect(mCorners[i].getX()- mRadius, mCorners[i].getY()- mRadius, mCorners[i].getX()+ mRadius, mCorners[i].getY()+ mRadius, mDotPaint);
                }
                break;

            case FIGURE_CIRCLE:
                if(mTimeline < -30){
                    mCenterPaint.setAlpha(abs(((int) mTimeline %25))*10);
                    canvas.drawCircle(mHeight /2, mWidth /2, mRadius *2, mCenterPaint);
                }
                for (int i = 0; i < mCorners.length; i++) {
                    canvas.drawCircle(mCorners[i].getX(), mCorners[i].getY(), mRadius, mDotPaint);
                }
                break;

            case FIGURE_IMAGE:
                if(mTimeline < -30){
                    mCustomImage.setAlpha(abs(((int) mTimeline %25))*10);
                    mCustomImage.setBounds((int)(mHeight /2- mRadius *2), (int)(mWidth /2- mRadius *2),
                            (int)(mHeight /2+ mRadius *2), (int)(mWidth /2+ mRadius *2));
                    mCustomImage.draw(canvas);
                }
                for (int i = 0; i < mCorners.length; i++) {
                    mCustomImage.setAlpha(255);
                    mCustomImage.setBounds(mCorners[i].getX()- mRadius, mCorners[i].getY()- mRadius,
                            mCorners[i].getX()+ mRadius, mCorners[i].getY()+ mRadius);
                    mCustomImage.draw(canvas);
                }
                break;
        }
    }

    private void drawLines(Canvas canvas) {
        canvas.drawLine(mCorners[0].getX(), mCorners[0].getY(), mCorners[1].getX(), mCorners[1].getY(), mLinePaint);
        canvas.drawLine(mCorners[0].getX(), mCorners[0].getY(), mCorners[2].getX(), mCorners[2].getY(), mLinePaint);
        canvas.drawLine(mCorners[0].getX(), mCorners[0].getY(), mCorners[3].getX(), mCorners[3].getY(), mLinePaint);

        canvas.drawLine(mCorners[1].getX(), mCorners[1].getY(), mCorners[2].getX(), mCorners[2].getY(), mLinePaint);
        canvas.drawLine(mCorners[1].getX(), mCorners[1].getY(), mCorners[3].getX(), mCorners[3].getY(), mLinePaint);

        canvas.drawLine(mCorners[2].getX(), mCorners[2].getY(), mCorners[3].getX(), mCorners[3].getY(), mLinePaint);
    }


    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            if (mTimeline > TIMELINE_MAX) {
                animationCollapsed();
                mReverseTimeline = -1;
                if(mDirection == DIRECTION_ROUND && (mRepeat==0 || mRepeatedCycles < mRepeat)){
                    mRepeatedCycles++;
                } else {
                    stopAnimation();
                };
            } else
            if (mTimeline < TIMELINE_MIN) {
                animationExploded();
                mReverseTimeline = 1;
                if(mDirection == DIRECTION_ROUND && (mRepeat==0 || mRepeatedCycles < mRepeat)){
                    mRepeatedCycles++;
                } else {
                    stopAnimation();
                };
            }

            float step = 0.03f * mSpeed;
            mTimeline += (mReverseTimeline * step);

            invalidate();

            if(isRunning) {
                postDelayed(this, 30);
            }

        }
    };



    public class Dot {
        private int currentX;
        private int currentY;

        private int startX;
        private int endX;
        private int startY;
        private int endY;

        public boolean rotated = false;


        public Dot(int sx, int sy, int ex, int ey, boolean rotated) {
            this.startX = sx;
            this.startY = sy;
            this.endX = ex;
            this.endY = ey;

            this.currentX = sx;
            this.currentY = sy;

            this.rotated = rotated;
        }


        public void calcPosition(float p) {

            int percent = (int)p;

            if(p < 1 ) {
                percent = 0;
            } else if(p > 99) {
                percent = 100;
            }

            if((percent >= 0) && (percent < 50)) {
                if(rotated) {
                    currentY = startY;
                    currentX = (int)(startX + (double)(endX - startX) * ((double)percent / 100D * 2D));
                } else {
                    currentX = startX;
                    currentY = (int)(startY + (double)(endY - startY) * ((double)percent / 100D * 2D));
                };
            } else if((percent >= 50) && (percent <= 100)) {
                if(rotated) {
                    currentX = endX;
                    currentY = (int)(startY + (double)(endY - startY) * ((double)(percent - 50) / 100D * 2D));
                } else {
                    currentY = endY;
                    currentX = (int)(startX + (double)(endX - startX) * ((double)(percent - 50) / 100D * 2D));
                };
            }

        }

        public int getX() {
            return currentX;
        }

        public int getY() {
            return currentY;
        }

        @Override
        public String toString() {
            return "start: " + startX + "," + startY + "; end: " + endX + "," + endY + "; current: " + currentX + "," + currentY;
        }
    }


}


