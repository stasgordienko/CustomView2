package com.stang.customview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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

import java.lang.reflect.Array;
import java.util.Arrays;

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
    RectAnimationView.Path[] mPath;

    private int mHeight = 0;
    private int mWidth = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;
    private Dot[] mCorner;
    private int mSpeed = 100;
    //private int mRepeat = 1;
    //private long mRepeatedCycles = 0;
    private int mLineColor = Color.BLACK;
    private int mLineWidth = 2;
    private int mDotFigure = FIGURE_NONE;
    private int mDotWidth = 20;
    private int mDotColor = Color.BLUE;
    private int mRadius = 20;
    private int mCenterAlpha = 255;
    private boolean isRunning = false;

    private Drawable mDotsImage;
    private Drawable mCenterImage;

    FigureX mFigureX;
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
        //mFigureX.init();
        mAnimatorSet.start();
        isRunning = true;
        invalidate();
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


        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onAnimationExploded();
                //
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationCollapsed();
                isRunning = false;
                //
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //
                isRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //
            }
        });

        mLinePaint = new Paint();
        mDotPaint = new Paint();
        mCenterPaint = new Paint();
        mCorner = new Dot[4];
        for (int i = 0; i < mCorner.length; i++) {
            mCorner[i] = new Dot();
        }
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
        Log.d(TAG, "onSizeChanged: " + mHeight + ":" + mWidth + "   center: " + mCenterX + ":" + mCenterY + "   mRadius: " + mRadius);

        Dot center = new Dot(mCenterX, mCenterY);
        Dot leftUp = new Dot(mRadius, mRadius);
        Dot leftBottom = new Dot(mRadius, mHeight-mRadius);
        Dot rightUp = new Dot(mWidth-mRadius, mRadius);
        Dot rightBottom = new Dot(mWidth - mRadius, mHeight - mRadius);

        Dot left = new Dot(mRadius, mCenterY);
        Dot bottom = new Dot(mCenterX, mHeight - mRadius);
        Dot right = new Dot(mWidth - mRadius, mCenterY);
        Dot up = new Dot(mCenterX, mRadius);


        mPath = new Path[4];
//        mPath[0] = new Path(mCorner[0], leftUp, left, center, left, leftUp);
//        mPath[1] = new Path(mCorner[1], leftBottom, bottom, center, bottom, leftBottom);
//        mPath[2] = new Path(mCorner[2], rightBottom, right, center, right, rightBottom);
//        mPath[3] = new Path(mCorner[3], rightUp, up, center, up, rightUp);

        mPath[0] = new Path(mCorner[0], leftUp, left, center);
        mPath[1] = new Path(mCorner[1], leftBottom, bottom, center);
        mPath[2] = new Path(mCorner[2], rightBottom, right, center);
        mPath[3] = new Path(mCorner[3], rightUp, up, center);

//        mPath[4] = new Path(mCorner[0], center, right, rightBottom);
//        mPath[5] = new Path(mCorner[1], center, bottom, leftBottom);
//        mPath[6] = new Path(mCorner[2], center, left, leftUp);
//        mPath[7] = new Path(mCorner[3], center, up, rightUp);

        FigureX figure1 = new FigureX(mPath[0], mPath[1], mPath[2], mPath[3]);
//        FigureX figure2 = new FigureX(mPath[4],mPath[5],mPath[6],mPath[7]);
//        mAnimatorSet.playSequentially(figure1.getAnimatorSet(),figure2.getAnimatorSet());
        mAnimatorSet.play(figure1.getAnimatorSet());

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
                //mCenterPaint.setAlpha(mCenterAlpha);
                //canvas.drawRect(mHeight /2- mRadius *2, mWidth /2- mRadius *2, mHeight /2+ mRadius *2, mWidth /2+ mRadius *2, mCenterPaint);
                for (int i = 0; i < mCorner.length; i++) {
                    canvas.drawRect(mCorner[i].getX()- mRadius, mCorner[i].getY()- mRadius, mCorner[i].getX()+ mRadius, mCorner[i].getY()+ mRadius, mDotPaint);
                }
                break;

            case FIGURE_CIRCLE:
                //mCenterPaint.setAlpha(mCenterAlpha);
                //canvas.drawCircle(mHeight /2, mWidth /2, mRadius *2, mCenterPaint);
                for (int i = 0; i < mCorner.length; i++) {
                    canvas.drawCircle(mCorner[i].getX(), mCorner[i].getY(), mRadius, mDotPaint);
                }
                break;

            case FIGURE_IMAGE:
//                mCenterImage.setBounds((int)(mHeight / 2 - mRadius *2), (int)(mWidth / 2 - mRadius *2),
//                        (int)(mHeight / 2 + mRadius *2), (int)(mWidth / 2 + mRadius *2));
//                mCenterImage.setAlpha(0);
//                mCenterImage.draw(canvas);

                mDotsImage.setAlpha(255);
                for (int i = 0; i < mCorner.length; i++) {
                    mDotsImage.setBounds(mCorner[i].getX()- mRadius, mCorner[i].getY()- mRadius,
                            mCorner[i].getX()+ mRadius, mCorner[i].getY()+ mRadius);
                    mDotsImage.draw(canvas);
                }
                break;
        }
    }

    private void drawLines(Canvas canvas) {
        canvas.drawLine(mCorner[0].getX(), mCorner[0].getY(), mCorner[1].getX(), mCorner[1].getY(), mLinePaint);
        canvas.drawLine(mCorner[0].getX(), mCorner[0].getY(), mCorner[2].getX(), mCorner[2].getY(), mLinePaint);
        canvas.drawLine(mCorner[0].getX(), mCorner[0].getY(), mCorner[3].getX(), mCorner[3].getY(), mLinePaint);

        canvas.drawLine(mCorner[1].getX(), mCorner[1].getY(), mCorner[2].getX(), mCorner[2].getY(), mLinePaint);
        canvas.drawLine(mCorner[1].getX(), mCorner[1].getY(), mCorner[3].getX(), mCorner[3].getY(), mLinePaint);

        canvas.drawLine(mCorner[2].getX(), mCorner[2].getY(), mCorner[3].getX(), mCorner[3].getY(), mLinePaint);
    }


    public class Dot {
        public int x = 0;
        public int y = 0;

        public Dot(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Dot(){
            //
        }

        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
        public void setX(int x) { this.x = x;}
        public void setY(int y) { this.y = y;}
     }


    public class Path {
        private Dot mDot;

        private int[] mX;
        private int[] mY;

        public Path(Dot dot, Dot ... dots) {
            if(dots != null) {
                mX = new int[dots.length];
                mY = new int[dots.length];
                for (int i = 0; i < dots.length; i++) {
                    mX[i] = dots[i].getX();
                    mY[i] = dots[i].getY();
                }
                mDot = dot;
                init();
            }

        }

        public Path(Dot dot, int[] x, int[] y) {
            if((x != null && y != null) && (x.length == y.length))  {
                mX = x;
                mY = y;
                mDot = dot;
                init();
            }

        }

        public Path(Dot dot, int[] coord) {
            if(coord != null && (coord.length%2 == 0)){
                for (int i = 0; i < coord.length/2; i+=2) {
                    mX[i] = coord[i];
                    mY[i] = coord[i+1];
                }
                mDot = dot;
                init();
            }
        }

        private void init(){
            mDot.setX(mX[0]);
            mDot.setY(mY[0]);
            //Log.d(TAG, "x:" + mCurrentX + " y:" + mCurrentY);
        }


        public AnimatorSet getAnimatorSet(){
            int duration = 1000;
            AnimatorSet result = new AnimatorSet();
            result.playTogether(ObjectAnimator.ofInt(this.mDot,"x",mX).setDuration(duration),
                    ObjectAnimator.ofInt(this.mDot,"y", mY).setDuration(duration));
            return result;
        }
    }

    public class FigureX {
        private Path[] mPath;

        public FigureX(Path ... path) {
            if(path != null) {
                mPath = path;
            }
        }

        public FigureX init(){
            for (int i = 0; i < mPath.length ; i++) {
                mPath[i].init();
            }
            return this;
        }


        public AnimatorSet getAnimatorSet() {
            int delay = 200;

            AnimatorSet[] animator = new AnimatorSet[mPath.length];

            for (int i = 0; i < mPath.length ; i++) {
                animator[i] = mPath[i].getAnimatorSet();
                animator[i].setStartDelay(delay * i);
            }

            AnimatorSet result = new AnimatorSet();
            result.playTogether(animator);
            return result;
        }
    }
}
