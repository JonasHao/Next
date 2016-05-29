package com.jonashao.musicplayerview;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;

import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mertsimsek on 14/08/15.
 * Reduced and Modified by Jonas on /12/15
 */
public class MusicPlayerView extends View {
    private static final String TAG = "IPV";
    /**
     * Handler will post runnable object every @ROTATE_DELAY seconds.
     */
    private static int ROTATE_DELAY = 20;

    /**
     * mRotateDegrees count increase 1 by 1 default.
     * I used that parameter as velocity.
     */
    static float VELOCITY = 0.4f;
    /**
     * empty progress paint
     */
    private Paint mPaintEmptyProgress;

    /**
     * loaded progress paint
     */
    private Paint mPaintLoadedProgress;

    /**
     * empty progress color
     */
    private int mEmptyProgressColor;

    /**
     * loaded progress color
     */
    private int mLoadedProgressColor;

    /**
     * Progress toggle
     */
    private Paint mPaintProgressToggle;

    /**
     * Progress toggle radius
     */
    private float mRadiusToggle;

    /**
     * Default empty progress color
     */
    private static final int COLOR_EMPTY_PROGRESS_DEFAULT = 0xAAFFFFFF;

    /**
     * Default empty progress color
     */
    private static final int COLOR_LOADED_PROGRESS_DEFAULT = 0xFFF44336;

    /**
     * progress rectf
     */
    private RectF mProgressRectF;

    /**
     * Cover image paint
     */
    private Paint mCoverPaint;

    /**
     * Cover bitmap
     */
    private Bitmap mBitmapCover;

    /**
     * Cover shader to make it circle
     */
    private BitmapShader mBitmapShader;

    /**
     * Scale image to existing size
     */
    private float mCoverScale;

    /**
     * Image height
     */
    private int mHeight;

    /**
     * Image width
     */
    private int mWidth;

    /**
     * Image center X
     */
    private float mCenterX;

    /**
     * Image center Y
     */
    private float mCenterY;

    /**
     * Cover Image radius
     */
    private float mCoverRadius;

    /**
     * %15 transparency black color for making
     * cover image little bit darker
     */
    private final static int COLOR_BLACK_TRANSPARENT = 0x26000000;

    /**
     * Default color code for cover
     */
    private int mCoverColor = Color.GRAY;

    /**
     * Music duration in milliseconds
     */
    private int maxProgress = 0;

    /**
     * Music current duration in milliseconds
     */
    private int currentProgress = 0;

    /**
     * count duration handler
     */
    private Handler mHandlerProgress;

    /**
     * count duration runnable
     */
    private Runnable mRunnableDuration;

    /**
     * cover rotate handler
     */
    private Handler mHandlerRotate;


    /**
     * Cover image is rotating. That is why we hold that value.
     */
    private float mRotateDegrees;

    /**
     * Detect Double click action and fling action
     */
    private GestureDetectorCompat mDetector;

    /**
     * Runnable for turning image (default velocity is 10)
     */
    private final Runnable mRunnableRotate = new Runnable() {
        @Override
        public void run() {
            if (isRotating) {
                if (currentProgress > maxProgress) {
                    currentProgress = 0;
                    setProgress(currentProgress);
                    stop();
                }
                updateCoverRotate();
                mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
            }
        }
    };


    /**
     * Is music playing
     */
    private boolean isPlaying = false;

    /***
     * Is rotating
     */
    private boolean isRotating;

    /**
     * Is auto progress draw
     */
    private boolean isAutoProgress = true;

    /**
     * Time unit to update progress
     */
    private static final int TIME_UNIT = 10;
    private OnTouchEventListener mTouchEventListener;
    private OnColorListener mColorListener;

    private int LightVibrantColor = Color.GRAY;   //empty
    private int DarkVibrantColor = Color.WHITE;       //loaded
    private int DarkMutedColor = Color.BLACK;        //background

    public MusicPlayerView(Context context) {
        super(context);
        init(context, null);
    }

    public MusicPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MusicPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MusicPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    /**
     * Initializes all paint, drawable, bitmap objects.
     * Object initializations must be defined here because this method
     * is called only once.(If you want smooth view, Stay away from onDraw() :)
     */
    public void init(Context context, AttributeSet attrs) {
        //Get Image resource from xml
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ipv);
        Drawable mDrawableCover = a.getDrawable(R.styleable.ipv_imageCover);
        if (mDrawableCover != null) {
            mBitmapCover = drawableToBitmap(mDrawableCover);

            Palette palette = Palette.from(mBitmapCover).generate();
            LightVibrantColor = palette.getLightVibrantColor(Color.GREEN);
            DarkVibrantColor = palette.getDarkVibrantColor(Color.WHITE);
            DarkMutedColor = palette.getDarkMutedColor(Color.BLACK);

            mEmptyProgressColor = LightVibrantColor;
            mLoadedProgressColor = DarkVibrantColor;
            if (mColorListener != null) {
                mColorListener.applyColor(DarkMutedColor);
            }

        } else {
            mEmptyProgressColor = a.getColor(R.styleable.ipv_emptyColor, COLOR_EMPTY_PROGRESS_DEFAULT);
            mLoadedProgressColor = a.getColor(R.styleable.ipv_loadedColor, COLOR_LOADED_PROGRESS_DEFAULT);
        }

        isAutoProgress = a.getBoolean(R.styleable.ipv_isAutoProgress, false);

        a.recycle();

        mPaintEmptyProgress = new Paint();
        mPaintEmptyProgress.setAntiAlias(true);
        mPaintEmptyProgress.setColor(mEmptyProgressColor);
        mPaintEmptyProgress.setStyle(Paint.Style.STROKE);
        mPaintEmptyProgress.setStrokeWidth(9.0f);

        mPaintLoadedProgress = new Paint();
        mPaintLoadedProgress.setAntiAlias(true);
        mPaintLoadedProgress.setColor(mLoadedProgressColor);
        mPaintLoadedProgress.setStyle(Paint.Style.STROKE);
        mPaintLoadedProgress.setStrokeWidth(9.0f);

        mPaintProgressToggle = new Paint();
        mPaintProgressToggle.setAntiAlias(true);
        mPaintProgressToggle.setColor(mLoadedProgressColor);
        mPaintProgressToggle.setStyle(Paint.Style.FILL);

        mProgressRectF = new RectF();

        //Handler and Runnable object for progressing.
        mHandlerProgress = new Handler();
        //Handler and Runnable object for turn cover image by updating rotation degrees
        mHandlerRotate = new Handler();

        mDetector = new GestureDetectorCompat(getContext(), new GestureListener());

    }

    /**
     * duration seconds and minutes will be counting while isPlaying value
     * is true and current seconds is less than max.
     */
    private void startDurationRunnable() {
        mHandlerProgress = new Handler();

        mRunnableDuration = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && maxProgress > currentProgress) {
                    currentProgress += TIME_UNIT;
                    mHandlerProgress.postDelayed(mRunnableDuration, TIME_UNIT);
                    postInvalidate();
                } else if (currentProgress >= maxProgress) {
                    currentProgress = 0;
                    isPlaying = false;
                    postInvalidate();
                }
            }
        };
    }

    /**
     * Calculate image width, height, center, radius etc. values.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        int minSide = Math.min(mWidth, mHeight);
        mWidth = minSide;
        mHeight = minSide;

        this.setMeasuredDimension(mWidth, mHeight);

        mCenterX = mWidth / 2f;
        mCenterY = mHeight / 2f;

        mCoverRadius = minSide / 2.3f;

        mRadiusToggle = mWidth / 40.0f;

        createShader();

        mProgressRectF.set(20.0f, 20.0f, mWidth - 20.0f, mHeight - 20.0f);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * What magic happens.
     * 1- draw image cover in circle
     * 2- draw black shadow on it to make seconds and icons more visible
     * 3- draw duration center of image.
     * 4- put icon/s on it
     * 5- draw progress
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmapShader == null)
            return;

        /**
         * Draw cover image cover
         */
        canvas.rotate(mRotateDegrees, mCenterX, mCenterY);
        canvas.drawCircle(mCenterX, mCenterY, mCoverRadius, mCoverPaint);
        //Rotate back to make play/pause button stable(No turn)
        canvas.rotate(-mRotateDegrees, mCenterX, mCenterY);


        /**
         * Draw progress
         */
        canvas.drawArc(mProgressRectF, 0, 360, false, mPaintEmptyProgress);
        canvas.drawArc(mProgressRectF, 270, calculatePastProgress(), false, mPaintLoadedProgress);
        canvas.drawCircle(
                (float) (mCenterX + ((mCenterX - 20.0f) * Math.cos(Math.toRadians(calculatePastProgress() - 90)))),
                (float) (mCenterY + ((mCenterX - 20.0f) * Math.sin(Math.toRadians(calculatePastProgress() - 90)))),
                mRadiusToggle,
                mPaintProgressToggle);
    }

    /**
     * We need to convert drawable (which we get from attributes) to bitmap
     * to prepare if for BitmapShader
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int mWidth, int mHeight) {
        return Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, false);
    }

    /**
     * Create shader and set shader to mPaintCover
     */
    private void createShader() {
        if (mWidth == 0)
            return;

        //if mBitmapCover is null then create default colored cover
        if (mBitmapCover == null) {
            mBitmapCover = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mBitmapCover.eraseColor(mCoverColor);
        }

        mCoverScale = ((float) mWidth) / (float) mBitmapCover.getWidth();

        mBitmapCover = Bitmap.createScaledBitmap(mBitmapCover,
                (int) (mBitmapCover.getWidth() * mCoverScale),
                (int) (mBitmapCover.getHeight() * mCoverScale),
                true);

        mBitmapShader = new BitmapShader(mBitmapCover, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mCoverPaint = new Paint();
        mCoverPaint.setAntiAlias(true);
        mCoverPaint.setShader(mBitmapShader);

        Palette palette = Palette.from(mBitmapCover).generate();
        DarkVibrantColor = palette.getDarkVibrantColor(Color.GRAY);
        DarkMutedColor = palette.getDarkMutedColor(Color.BLACK);
        LightVibrantColor = palette.getLightVibrantColor(Color.WHITE);

        setProgressEmptyColor(LightVibrantColor);
        setProgressLoadedColor(DarkVibrantColor);
        if (mColorListener != null) {
            mColorListener.applyColor(DarkMutedColor);
        }

    }

    /**
     * Update rotate degree of cover and invalidate onDraw();
     */
    public void updateCoverRotate() {
        mRotateDegrees += VELOCITY;
        mRotateDegrees = mRotateDegrees % 360;
        postInvalidate();
    }

    /**
     * set cover image resource
     */
    public void setCoverDrawable(int coverDrawable) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), coverDrawable);
        mBitmapCover = drawableToBitmap(drawable);
        createShader();
        postInvalidate();
    }


    /**
     * gets image URL and load it to cover image.It uses Picasso Library.
     */
    public void setCoverUrl(long album_id) {
        try {
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            InputStream input = getContext().getContentResolver().openInputStream(uri);
            mBitmapCover = BitmapFactory.decodeStream(input);
            if (input == null) {
                Log.e(TAG, "INPUT IS NULL");
                return;
            }
            createShader();
            postInvalidate();
        } catch (IOException e) {
            Log.e(TAG, "No file found with the selected image url");
        } catch (NullPointerException e2) {
            Log.e(TAG, "INPUT IS NULL", e2);
        }
    }


    public void setMax(int durationMax) {
        this.maxProgress = durationMax;
        postInvalidate();
    }


    public void start() {
        isPlaying = true;
        if (isAutoProgress && mHandlerProgress != null && mRunnableDuration != null) {
            mHandlerProgress.postDelayed(mRunnableDuration, TIME_UNIT);
        }
        isRotating = true;
        mHandlerRotate.removeCallbacksAndMessages(null);
        mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
        if (isAutoProgress) {
            startDurationRunnable();
        }

    }

    public void stop() {
        isPlaying = false;
        if (mHandlerProgress != null) {
            mHandlerProgress.removeCallbacks(mRunnableDuration);
        }
        isRotating = false;
        postInvalidate();
    }

    public boolean isPlaying() {
        return isPlaying;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDetector != null) {
            return mDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void setTouchEventListener(OnTouchEventListener listener) {
        this.mTouchEventListener = listener;
    }

    public void setColorListener(OnColorListener listener) {
        mColorListener = listener;
    }


    private int calculatePastProgress() {
        return maxProgress > 0 ? (360 * currentProgress) / maxProgress : 0;
    }

    public void setProgress(int progress) {
        if (currentProgress > maxProgress) {
            currentProgress = 0;
            stop();
            return;
        }
        currentProgress = progress;
        if (!isPlaying) {
            start();
        }
        postInvalidate();
    }


    public int getProgress() {
        return currentProgress;
    }

    public void setProgressLoadedColor(int mLoadedProgressColor) {
        this.mLoadedProgressColor = mLoadedProgressColor;
        mPaintLoadedProgress.setColor(mLoadedProgressColor);
        mPaintProgressToggle.setColor(mLoadedProgressColor);
        postInvalidate();
    }

    public void setProgressEmptyColor(int mEmptyProgressColor) {
        this.mEmptyProgressColor = mEmptyProgressColor;
        mPaintEmptyProgress.setColor(mEmptyProgressColor);
        postInvalidate();
    }

    public int getDarkMutedColor() {
        return DarkMutedColor;
    }

    public int getDarkVibrantColor() {
        return DarkVibrantColor;
    }


    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mTouchEventListener != null) {
                mTouchEventListener.onDoubleClicked();
            }
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null)
                return false;
            float x_from = e1.getX();
            float x_to = e2.getX();
            float y_from = e1.getY();
            float y_to = e2.getY();
            // 左右滑动的X轴幅度大于100，并且X轴方向的速度大于100
            if (Math.abs(x_from - x_to) > 200.0f && Math.abs(velocityX) > 100.0f) {
                // X轴幅度大于Y轴的幅度
                if (Math.abs(x_from - x_to) >= Math.abs(y_from - y_to)) {
                    if (x_from > x_to) {
                        // 下一个
                        if (mTouchEventListener != null) {
                            mTouchEventListener.onFling();
                        }
                    }
                }
            } else {
                return false;
            }
            return true;
        }
    }
}
