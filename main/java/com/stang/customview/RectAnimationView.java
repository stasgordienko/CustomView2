package com.stang.customview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
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
    RectAnimationView.Path[] mPath;

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
    //private boolean isForward = true;

    private Drawable mDotsImage;
    private Drawable mCenterImage;

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
        //init();
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
        mPath[0] = new Path(leftUp, left, center);
        mPath[1] = new Path(leftBottom, bottom, center);
        mPath[2] = new Path(rightBottom, right, center);
        mPath[3] = new Path(rightUp, up, center);

        mCenterImage.setBounds((int)(mHeight / 2 - mRadius *2), (int)(mWidth / 2 - mRadius *2),
                (int)(mHeight / 2 + mRadius *2), (int)(mWidth / 2 + mRadius *2));


        FigureX figure = new FigureX(mPath);

        mAnimatorSet.play(figure.getAnimatorSet()).before(figure.reverse().getAnimatorSet());


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
                for (int i = 0; i < mPath.length; i++) {
                    canvas.drawRect(mPath[i].getX()- mRadius, mPath[i].getY()- mRadius, mPath[i].getX()+ mRadius, mPath[i].getY()+ mRadius, mDotPaint);
                }
                break;

            case FIGURE_CIRCLE:
                mCenterPaint.setAlpha(mCenterAlpha);
                canvas.drawCircle(mHeight /2, mWidth /2, mRadius *2, mCenterPaint);
                for (int i = 0; i < mPath.length; i++) {
                    canvas.drawCircle(mPath[i].getX(), mPath[i].getY(), mRadius, mDotPaint);
                }
                break;

            case FIGURE_IMAGE:
                mCenterImage.setAlpha(mCenterAlpha);
                mCenterImage.draw(canvas);
                for (int i = 0; i < mPath.length; i++) {
                    mDotsImage.setBounds(mPath[i].getX()- mRadius, mPath[i].getY()- mRadius,
                            mPath[i].getX()+ mRadius, mPath[i].getY()+ mRadius);
                    mDotsImage.draw(canvas);
                }
                break;
        }
    }

    private void drawLines(Canvas canvas) {
        canvas.drawLine(mPath[0].getX(), mPath[0].getY(), mPath[1].getX(), mPath[1].getY(), mLinePaint);
        canvas.drawLine(mPath[0].getX(), mPath[0].getY(), mPath[2].getX(), mPath[2].getY(), mLinePaint);
        canvas.drawLine(mPath[0].getX(), mPath[0].getY(), mPath[3].getX(), mPath[3].getY(), mLinePaint);

        canvas.drawLine(mPath[1].getX(), mPath[1].getY(), mPath[2].getX(), mPath[2].getY(), mLinePaint);
        canvas.drawLine(mPath[1].getX(), mPath[1].getY(), mPath[3].getX(), mPath[3].getY(), mLinePaint);

        canvas.drawLine(mPath[2].getX(), mPath[2].getY(), mPath[3].getX(), mPath[3].getY(), mLinePaint);
    }


    public class Dot {
        public int x;
        public int y;

        public Dot(int x, int y) {
            this.x = x;
            this.y = y;
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
        public static final int DIRECTION_FORWARD = 1;
        public static final int DIRECTION_BACKWARD = -1;

        private int mCurrentX;
        private int mCurrentY;

        private int[] mX;
        private int[] mY;
        private int mDirection = DIRECTION_FORWARD;

        private ValueAnimator mAnimatorX;
        private ValueAnimator mAnimatorY;

        public int getX(){
            return (int) mAnimatorX.getAnimatedValue();
        }
        public int getY(){
            return (int) mAnimatorY.getAnimatedValue();
        }

        public Path(Dot ... dots) {
            if(dots != null) {
                mX = new int[dots.length];
                mY = new int[dots.length];
                for (int i = 0; i < dots.length; i++) {
                    mX[i] = dots[i].getX();
                    mY[i] = dots[i].getY();
                }
                init();
            }

        }

        public Path(int[] x, int[] y) {
            if((x != null && y != null) && (x.length == y.length))  {
                this.mX = x;
                this.mY = y;
                init();
            }

        }

        public Path(int[] coord) {
            if(coord != null && (coord.length%2 == 0)){
                for (int i = 0; i < coord.length/2; i+=2) {
                    mX[i] = coord[i];
                    mY[i] = coord[i+1];
                }
                init();
            }
        }

        private void init(){
            mCurrentX = mX[0];
            mCurrentY = mY[0];
        }

        public Path reverse(){
            mDirection *= -1;
            return this;
        }

        public Path setDirection(int direction) {
            if(direction < 0) {
                if(mDirection == DIRECTION_FORWARD){
                    reverse();
                }
            } else {
                if(mDirection == DIRECTION_BACKWARD){
                    reverse();
                }
            }
            return this;
        }

        public AnimatorSet getAnimatorSet(){
            mAnimatorX = ValueAnimator.ofInt(mX).setDuration(1000);
            mAnimatorY = ValueAnimator.ofInt(mY).setDuration(1000);
            AnimatorSet result = new AnimatorSet();
            result.playTogether(mAnimatorX, mAnimatorY);
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

        public FigureX reverse(){
            for (int i = 0; i < mPath.length ; i++) {
                mPath[i].reverse();
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
