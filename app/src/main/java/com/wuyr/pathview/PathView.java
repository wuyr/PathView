package com.wuyr.pathview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Created by wuyr on 17-12-15 下午8:08.
 */

public class PathView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    @IntDef({TRAIN_MODE, AIRPLANE_MODE})
    @IntRange(from = AIRPLANE_MODE, to = TRAIN_MODE)
    @Retention(RetentionPolicy.SOURCE)
    private @interface Mode {
    }

    public static final int AIRPLANE_MODE = 0; // 一开始不显示灰色线条，粉红色线条走过后才留下灰色线条
    public static final int TRAIN_MODE = 1;// 一开始就显示灰色线条，并且一直显示，直到动画结束

    private volatile boolean isDrawing;
    private Semaphore mLightLineSemaphore, mDarkLineSemaphore;
    private SurfaceHolder mSurfaceHolder;
    private Keyframes mKeyframes;
    private int mMode;
    private float[] mLightPoints;
    private float[] mDarkPoints;
    private int mLightLineColor;
    private int mDarkLineColor;
    private ValueAnimator mProgressAnimator, mAlphaAnimator;
    private long mAnimationDuration;
    private Paint mPaint;
    private int mAlpha;

    public PathView(Context context) {
        this(context, null);
    }

    public PathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);

        //初始化画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);

        //默认动画时长
        mAnimationDuration = 1000L;

        //默认颜色
        mLightLineColor = Color.parseColor("#F17F94");
        mDarkLineColor = Color.parseColor("#D8D5D7");

        mLightLineSemaphore = new Semaphore(1);
        mDarkLineSemaphore = new Semaphore(1);

    }

    public void setMode(@Mode int mode) {
        if ((mAlphaAnimator != null && mAlphaAnimator.isRunning()) || (mAlphaAnimator != null && mAlphaAnimator.isRunning()))
            throw new IllegalStateException("animation has been started!");
        mMode = mode;
        if (mode == TRAIN_MODE)
            setDarkLineProgress(1, 0);
        else
            setDarkLineProgress(0, 0);
    }

    public void setPath(Path path) {
        mKeyframes = new Keyframes(path);
        mAlpha = 0;
    }

    public void setAnimationDuration(long duration) {
        mAnimationDuration = duration;
    }

    public void startAnimation() {
        if (mAlphaAnimator != null && mAlphaAnimator.isRunning())
            mAlphaAnimator.cancel();
        if (mProgressAnimator != null && mProgressAnimator.isRunning())
            mProgressAnimator.cancel();
        mAlphaAnimator = ValueAnimator.ofInt(0, 255).setDuration(mAnimationDuration / 10);// 时长是总时长的10%
        mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAlpha = (int) animation.getAnimatedValue();
            }
        });
        mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startUpdateProgress();
            }
        });
        mAlphaAnimator.start();
    }

    public void setLineWidth(float width) {
        mPaint.setStrokeWidth(width);
    }

    public void setLightLineColor(@ColorInt int color) {
        mLightLineColor = color;
    }

    public void setDarkLineColor(@ColorInt int color) {
        mDarkLineColor = color;
    }

    private void setLightLineProgress(float start, float end) {
        setLineProgress(start, end, true);
    }

    private void startUpdateProgress() {
        mAlphaAnimator = null;
//        底部灰色线条向后加长到原Path的60%
        mProgressAnimator = ValueAnimator.ofFloat(-.6F, 1).setDuration(mAnimationDuration);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentProgress = (float) animation.getAnimatedValue();
                float lightLineStartProgress,//粉色线头
                        lightLineEndProgress;//粉色线尾
                float darkLineStartProgress,//灰色线头
                        darkLineEndProgress;//灰色线尾

                darkLineEndProgress = currentProgress;

//                粉色线头从0开始，并且初始速度是灰色线尾的两倍
                darkLineStartProgress = lightLineStartProgress = (.6F + currentProgress) * 2;

//                粉色线尾从-0.25开始，速度跟灰色线尾速度一样
                lightLineEndProgress = .35F + currentProgress;

//                粉色线尾走到30%时，速度变为原来速度的2倍
                if (lightLineEndProgress > .3F) {
                    lightLineEndProgress = (.35F + currentProgress - .3F) * 2 + .3F;
                }

//                当粉色线头走到65%时，速度变为原来速度的0.35倍
                if (darkLineStartProgress > .65F) {
                    darkLineStartProgress = lightLineStartProgress = ((.6F + currentProgress) * 2 - .65F) * .35F + .65F;
                }
                if (lightLineEndProgress < 0) {
                    lightLineEndProgress = 0;
                }
                if (darkLineEndProgress < 0) {
                    darkLineEndProgress = 0;
                }

//                当粉色线尾走到90%时，播放透明渐变动画
                if (lightLineEndProgress > .9F) {
                    if (mAlphaAnimator == null) {
                        mAlphaAnimator = ValueAnimator.ofInt(255, 0).setDuration((long) (mAnimationDuration * .2));// 时长是总时长的20%
                        mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                mAlpha = (int) animation.getAnimatedValue();
                            }
                        });
                        mAlphaAnimator.start();
                    }
                }
                if (lightLineStartProgress > 1) {
                    darkLineStartProgress = lightLineStartProgress = 1;
                }

                setLightLineProgress(lightLineStartProgress, lightLineEndProgress);

//                飞机模式才更新灰色线条
                if (mMode == AIRPLANE_MODE)
                    setDarkLineProgress(darkLineStartProgress, darkLineEndProgress);
            }
        });
        mProgressAnimator.start();
    }

    private void setDarkLineProgress(float start, float end) {
        setLineProgress(start, end, false);
    }

    private void setLineProgress(float start, float end, boolean isLightPoints) {
        if (mKeyframes == null)
            throw new IllegalStateException("path not set yet!");

        if (isLightPoints) {
            try {
                mLightLineSemaphore.acquire();
            } catch (InterruptedException e) {
                return;
            }
            mLightPoints = mKeyframes.getRangeValue(start, end);
            mLightLineSemaphore.release();
        } else {
            try {
                mDarkLineSemaphore.acquire();
            } catch (InterruptedException e) {
                return;
            }
            mDarkPoints = mKeyframes.getRangeValue(start, end);
            mDarkLineSemaphore.release();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        restart();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    @Override
    public void run() {
        while (isDrawing) {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas == null) return;
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            startDraw(canvas);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void startDraw(Canvas canvas) {
        try {
            mDarkLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        if (mDarkPoints != null) {
            mPaint.setColor(mDarkLineColor);
            mPaint.setAlpha(mAlpha);
            canvas.drawPoints(mDarkPoints, mPaint);
        }
        mDarkLineSemaphore.release();
        try {
            mLightLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        if (mLightPoints != null) {
            mPaint.setColor(mLightLineColor);
            mPaint.setAlpha(mAlpha);
            canvas.drawPoints(mLightPoints, mPaint);
        }
        mLightLineSemaphore.release();
    }

    private void restart() {
        isDrawing = true;
        new Thread(this).start();
    }

    private void stop() {
        isDrawing = false;
        try {
            mDarkLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        mDarkPoints = null;
        mDarkLineSemaphore.release();
        try {
            mLightLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        mLightPoints = null;
        mLightLineSemaphore.release();
        if (mAlphaAnimator != null && mAlphaAnimator.isRunning())
            mAlphaAnimator.cancel();
        if (mProgressAnimator != null && mProgressAnimator.isRunning())
            mProgressAnimator.cancel();
    }

    private static class Keyframes {

        static final float PRECISION = 1f; //精度我们用1就够了 (数值越少 numPoints 就越大)
        int numPoints;
        float[] mData;

        Keyframes(Path path) {
            init(path);
        }

        void init(Path path) {
            final PathMeasure pathMeasure = new PathMeasure(path, false);
            final float pathLength = pathMeasure.getLength();
            numPoints = (int) (pathLength / PRECISION) + 1;
            mData = new float[numPoints * 2];
            final float[] position = new float[2];
            int index = 0;
            for (int i = 0; i < numPoints; ++i) {
                final float distance = (i * pathLength) / (numPoints - 1);
                pathMeasure.getPosTan(distance, position, null);
                mData[index] = position[0];
                mData[index + 1] = position[1];
                index += 2;
            }
            numPoints = mData.length;
        }

        /**
         * 拿到start和end之间的x,y数据
         *
         * @param start 开始百分比
         * @param end   结束百分比
         * @return 裁剪后的数据
         */
        float[] getRangeValue(float start, float end) {
            int startIndex = (int) (numPoints * start);
            int endIndex = (int) (numPoints * end);

            //必须是偶数，因为需要float[]{x,y}这样x和y要配对的
            if (startIndex % 2 != 0) {
                //直接减，不用担心 < 0  因为0是偶数，哈哈
                --startIndex;
            }
            if (endIndex % 2 != 0) {
                //不用检查越界
                ++endIndex;
            }
            //根据起止点裁剪
            return startIndex > endIndex ? Arrays.copyOfRange(mData, endIndex, startIndex) : null;
        }
    }
}
