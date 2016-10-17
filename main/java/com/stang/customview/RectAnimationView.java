package com.stang.customview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import static android.animation.ObjectAnimator.ofInt;
import static java.lang.Math.abs;

/**
 * Created by Grisha2 on 17.10.2016.
 */

public class RectAnimationView extends View {
    public static final String TAG = "app";

    public static final int FIGURE_NONE = 0;
    public static final int FIGURE_RECTANGLE = 1;
    public static final int FIGURE_CIRCLE= 2;
    public static final int FIGURE_IMAGE = 3;

    Paint mLinePaint;
    Paint mDotPaint;
    Paint mCenterPaint;
    RectAnimationView.Dot[] mDots;

    private int mHeight = 0;
    private int mWidth = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;
    private int mSpeed = 100;
    //private int mRepeat = 1;
    //private int mDirection = DIRECTION_ROUND;
    //private long mRepeatedCycles = 0;
    private int mLineColor = Color.BLACK;
    private int mLineWidth = 2;
    private int mDotFigure = FIGURE_NONE;
    private int mDotWidth = 20;
    private int mDotColor = Color.BLUE;
    private int mRadius = 20;
    private int mCenterAlpha = 0;
    private boolean isRunning = false;
    private boolean isForward = true;

    private Drawable mDotsImage;
    private Drawable mCenterImage;

    ObjectAnimator[] mDotsAnimators;
    ObjectAnimator mAlphaAnimator;
    AnimatorSet[] mAngleAnimatorSet;
    AnimatorSet mAnimatorSet;

    MyView.OnAnimationEventListener mAnimationListener = null;

    public void setOnAnimationEventListener(MyView.OnAnimationEventListener listener) {
        mAnimationListener = listener;
    }

    public interface OnAnimationEventListener {
        void onAnimationStarted();
        void onAnimationStopped();
        void onAnimationCollapsed();
        void onAnimationExploded();
    }

    private void onAnimationStarted() {
        if (mAnimationListener != null) {
            mAnimationListener.onAnimationStarted();
        }
    }

    private void onAnimationStopped() {
        if (mAnimationListener != null) {
            mAnimationListener.onAnimationStopped();
        }
    }

    private void onAnimationCollapsed() {
        if (mAnimationListener != null) {
            mAnimationListener.onAnimationCollapsed();
        }
    }

    private void onAnimationExploded() {
        if (mAnimationListener != null) {
            mAnimationListener.onAnimationExploded();
        }
    }


    public void startAnimation() {
        init();
        mAnimatorSet.start();
        isRunning = true;
        //invalidate();
        onAnimationStarted();
    }

    public void stopAnimation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnimatorSet.pause();
        } else {
            mAnimatorSet.cancel();
        }
        isRunning = false;
        onAnimationStopped();
    }


    public void setSpeed(int speed) { mSpeed = speed; setPaintProperties(); }

    public void setLineColor(int lineColor) { mLineColor = lineColor; setPaintProperties(); }

    public void setLineWidth(int lineWidth) { mLineWidth = lineWidth; setPaintProperties(); }

    public void setDotFigure(int dotFigure) { mDotFigure = dotFigure; setPaintProperties(); }

    public void setDotWidth(int dotWidth) { mDotWidth = dotWidth; setPaintProperties(); }

    public void setDotColor(int dotColor) { mDotColor = dotColor; setPaintProperties(); }

    public void setDotsImage(Drawable image) {
        mDotsImage = image;
        mCenterImage = image;
        setPaintProperties();
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

    public Drawable getDotsImage() {
        return mDotsImage;
    }

    public boolean isRunning() {
        return isRunning;
    }



    public RectAnimationView(Context context) {
        super(context);
    }

    public RectAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RectAnimationView,
                0, 0
        );

        try {
            mLineColor = a.getColor(R.styleable.RectAnimationView_rav_line_color, Color.BLACK);
            mDotColor = a.getColor(R.styleable.RectAnimationView_rav_dot_color, Color.BLACK);
            mSpeed = a.getInt(R.styleable.RectAnimationView_rav_speed_animation, 100);
            mDotFigure = a.getInt(R.styleable.RectAnimationView_rav_dot_figure, FIGURE_CIRCLE);
            mDotWidth = a.getDimensionPixelSize(R.styleable.RectAnimationView_rav_dot_width, 10);
            mLineWidth = a.getDimensionPixelSize(R.styleable.RectAnimationView_rav_line_width, 1);
        } finally {
            a.recycle();
        }

        mRadius = mDotWidth;

        mDotsAnimators = new ObjectAnimator[8];
        mAlphaAnimator = new ObjectAnimator();
        mAngleAnimatorSet = new AnimatorSet[4];
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimationExploded();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationCollapsed();
                revertDirection();
                //mAnimatorSet.start();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mLinePaint = new Paint();
        mDotPaint = new Paint();
        mCenterPaint = new Paint();
        setDotsImage(context.getResources().getDrawable(R.drawable.ok_));

        setPaintProperties();
    }


    private void setPaintProperties(){
        mCenterPaint.setColor(mDotColor);
        mCenterImage.setAlpha(mCenterAlpha);
        mCenterPaint.setAlpha(mCenterAlpha);
        mDotPaint.setColor(mDotColor);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineWidth);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
        mCenterX = mWidth /2;
        mCenterY = mHeight /2;

        init();
    }


    private void init() {
        //Log.d(TAG, "onSizeChanged: " + mHeight + ":" + mWidth + "   center: " + mCenterX + ":" + mCenterY + "   mRadius: " + mRadius);

        mDots = new Dot[4];
        mDots[0] = new Dot(mRadius, mRadius, mCenterX, mCenterY);
        mDots[3] = new Dot(mWidth - mRadius, mRadius, mCenterX, mCenterY);
        mDots[2] = new Dot(mWidth - mRadius, mHeight - mRadius, mCenterX, mCenterY);
        mDots[1] = new Dot(mRadius, mHeight - mRadius, mCenterX, mCenterY);

        mCenterImage.setBounds((int)(mHeight / 2 - mRadius *2), (int)(mWidth / 2 - mRadius *2),
                (int)(mHeight / 2 + mRadius *2), (int)(mWidth / 2 + mRadius *2));

        setForward();

     }

    private void setForward() {
        int delay = 200;
        int lineDuration = 500;
        int duration = 5000;

        mDotsAnimators[0] = ObjectAnimator.ofInt(mDots[0], "y", mDots[0].startY, mDots[0].endY);
        mDotsAnimators[1] = ObjectAnimator.ofInt(mDots[0], "x", mDots[0].startX, mDots[0].endX);
        mAngleAnimatorSet[0] = new AnimatorSet();
        mAngleAnimatorSet[0].playSequentially(mDotsAnimators[0], mDotsAnimators[1]);
        mAngleAnimatorSet[0].setStartDelay(delay * 0);

        mDotsAnimators[2] = ObjectAnimator.ofInt(mDots[1], "y", mDots[1].startY, mDots[1].endY);
        mDotsAnimators[3] = ObjectAnimator.ofInt(mDots[1], "x", mDots[1].startX, mDots[1].endX);
        mAngleAnimatorSet[1] = new AnimatorSet();
        mAngleAnimatorSet[1].playSequentially(mDotsAnimators[3], mDotsAnimators[2]);
        mAngleAnimatorSet[1].setStartDelay(delay * 1);

        mDotsAnimators[4] = ObjectAnimator.ofInt(mDots[2], "y", mDots[2].startY, mDots[2].endY);
        mDotsAnimators[5] = ObjectAnimator.ofInt(mDots[2], "x", mDots[2].startX, mDots[2].endX);
        mAngleAnimatorSet[2] = new AnimatorSet();
        mAngleAnimatorSet[2].playSequentially(mDotsAnimators[4], mDotsAnimators[5]);
        mAngleAnimatorSet[2].setStartDelay(delay * 2);

        mDotsAnimators[6] = ObjectAnimator.ofInt(mDots[3], "y", mDots[3].startY, mDots[3].endY);
        mDotsAnimators[7] = ObjectAnimator.ofInt(mDots[3], "x", mDots[3].startX, mDots[3].endX);
        mAngleAnimatorSet[3] = new AnimatorSet();
        mAngleAnimatorSet[3].playSequentially(mDotsAnimators[7], mDotsAnimators[6]);
        mAngleAnimatorSet[3].setStartDelay(delay * 3);

        for (int i = 0; i <8 ; i++) {
            mDotsAnimators[i].setDuration(lineDuration);
            mDotsAnimators[i].setRepeatMode(ObjectAnimator.REVERSE);
        }

        mAnimatorSet.playTogether(mAngleAnimatorSet);

//        mAnimatorSet.setDuration(duration);

//        mAlphaAnimator = ObjectAnimator.ofInt(this, "mCenterAlpha", 255, 0 );
//        mAlphaAnimator.setDuration(200);
//        mAlphaAnimator.setRepeatCount(5);
//        mAlphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
//
//        mAnimatorSet.play(mAlphaAnimator);
    }

    private void setBackward() {
        for (int i = 0; i < 8; i++) {
            mDotsAnimators[i].reverse();
        }

        mAngleAnimatorSet[0].playSequentially(mDotsAnimators[1], mDotsAnimators[0]);
        mAngleAnimatorSet[1].playSequentially(mDotsAnimators[2], mDotsAnimators[3]);
        mAngleAnimatorSet[2].playSequentially(mDotsAnimators[5], mDotsAnimators[4]);
        mAngleAnimatorSet[3].playSequentially(mDotsAnimators[6], mDotsAnimators[7]);

        mAnimatorSet.playTogether(mAngleAnimatorSet);
    }


    private void revertDirection() {
        if(isForward) {
            setBackward();
            isForward = false;
        } else {
            setForward();
            isForward = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawLines(canvas);
        drawDots(canvas);
        if(isRunning()) postInvalidateDelayed(50);
    }


    private void drawDots(Canvas canvas) {
        switch (mDotFigure){
            case FIGURE_NONE: //none
                break;

            case FIGURE_RECTANGLE:
                mCenterPaint.setAlpha(mCenterAlpha);
                canvas.drawRect(mHeight /2- mRadius *2, mWidth /2- mRadius *2, mHeight /2+ mRadius *2, mWidth /2+ mRadius *2, mCenterPaint);
                for (int i = 0; i < mDots.length; i++) {
                    canvas.drawRect(mDots[i].getX()- mRadius, mDots[i].getY()- mRadius, mDots[i].getX()+ mRadius, mDots[i].getY()+ mRadius, mDotPaint);
                }
                break;

            case FIGURE_CIRCLE:
                mCenterPaint.setAlpha(mCenterAlpha);
                canvas.drawCircle(mHeight /2, mWidth /2, mRadius *2, mCenterPaint);
                for (int i = 0; i < mDots.length; i++) {
                    canvas.drawCircle(mDots[i].getX(), mDots[i].getY(), mRadius, mDotPaint);
                }
                break;

            case FIGURE_IMAGE:
                mCenterImage.setAlpha(mCenterAlpha);
                mCenterImage.draw(canvas);
                for (int i = 0; i < mDots.length; i++) {
                    mDotsImage.setBounds(mDots[i].getX()- mRadius, mDots[i].getY()- mRadius,
                            mDots[i].getX()+ mRadius, mDots[i].getY()+ mRadius);
                    mDotsImage.draw(canvas);
                }
                break;
        }
    }

    private void drawLines(Canvas canvas) {
        canvas.drawLine(mDots[0].getX(), mDots[0].getY(), mDots[1].getX(), mDots[1].getY(), mLinePaint);
        canvas.drawLine(mDots[0].getX(), mDots[0].getY(), mDots[2].getX(), mDots[2].getY(), mLinePaint);
        canvas.drawLine(mDots[0].getX(), mDots[0].getY(), mDots[3].getX(), mDots[3].getY(), mLinePaint);

        canvas.drawLine(mDots[1].getX(), mDots[1].getY(), mDots[2].getX(), mDots[2].getY(), mLinePaint);
        canvas.drawLine(mDots[1].getX(), mDots[1].getY(), mDots[3].getX(), mDots[3].getY(), mLinePaint);

        canvas.drawLine(mDots[2].getX(), mDots[2].getY(), mDots[3].getX(), mDots[3].getY(), mLinePaint);
    }


    public class Dot {
        private int currentX;
        private int currentY;

        private int startX;
        private int endX;
        private int startY;
        private int endY;


        public Dot(int sx, int sy, int ex, int ey) {
            this.startX = sx;
            this.startY = sy;
            this.endX = ex;
            this.endY = ey;

            this.currentX = sx;
            this.currentY = sy;
        }


        public int getX() {
            return currentX;
        }

        public int getY() {
            return currentY;
        }

        public void setX(int x) { this.currentX = x;}

        public void setY(int y) { this.currentY = y;}


        @Override
        public String toString() {
            return "start: " + startX + "," + startY + "; end: " + endX + "," + endY + "; current: " + currentX + "," + currentY;
        }
    }
}
