package io.kickflip.sdk.view.drawing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import java.io.File;
import java.util.UUID;

import io.kickflip.sdk.helper.FilesHelper;
import io.kickflip.sdk.helper.ResourcesHelper;


public class DrawingView extends View implements DrawingProvider {
    private static final String DRAWING = "drawing_%1$s";
    private Paint mPaint;
    private Paint mBitmapPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap overlayBitmap;
    private Bitmap overlayRotatedBitmap;
    private Canvas overlayCanvas;
    private Path mPath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private boolean hasDrawing = false;
    private OnDrawingFinished listener;
    private String file;

    private volatile boolean shouldUpdate;
    private boolean preparingBitmap;
    private boolean bitmapReady;

    public boolean shouldUpdate() {
        return shouldUpdate;
    }

    public boolean preparingBitmap() {
        return preparingBitmap;
    }

    public void setPreparingBitmap() {
        preparingBitmap = true;
    }

    public boolean bitmapReady() {
        return bitmapReady;
    }

    public void prepareBitmap() {
//        draw(overlayCanvas);
//        if (overlayRotatedBitmap != null && !overlayRotatedBitmap.isRecycled())
//            overlayRotatedBitmap.recycle();
//        overlayRotatedBitmap = BitmapUtils.RotateBitmap(overlayBitmap, -90);
        preparingBitmap = false;
        bitmapReady = true;
    }

    public Bitmap getBitmap() {
//        if (!bitmapReady) throw new IllegalStateException("bitmap is not ready!");
//        bitmapReady = false;
        shouldUpdate = false;
//        return overlayRotatedBitmap;
        return mBitmap;
    }

    public DrawingView(Context c) {
        super(c);
    }

    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize() {
        mBitmap = Bitmap.createBitmap(ResourcesHelper.getScreenSize().x, ResourcesHelper.getScreenSize().y, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        overlayBitmap = Bitmap.createBitmap(ResourcesHelper.getScreenSize().x, ResourcesHelper.getScreenSize().y, Bitmap.Config.ARGB_8888);
        overlayRotatedBitmap = Bitmap.createBitmap(ResourcesHelper.getScreenSize().y, ResourcesHelper.getScreenSize().x, Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayBitmap);
        overlayCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(20);
        mPaint.setColor(0xFFFF0000);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        if (isInEditMode()) {
//            super.onDraw(canvas);
//        } else {
//            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
//            Log.w("DrawingView", "" + mBitmap);
//            if (mPaint.getXfermode() == null) {
//                canvas.drawPath(mPath, mPaint);
//            }
//        }
////        TODO: quizas esta asignacion tiene que ir adentro del else. Ver cuando caigo adentro del else.
//        shouldUpdate = true;
//    }

//    private void drawOnTexture() {
//        Canvas canvas = lockCanvas();
//        if (isInEditMode()) {
//            return;
//        } else {
//            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
////            Log.w("DrawingView", "" + mBitmap);
//            if (mPaint.getXfermode() == null) {
//                canvas.drawPath(mPath, mPaint);
//            }
//        }
////        TODO: quizas esta asignacion tiene que ir adentro del else. Ver cuando caigo adentro del else.
//        shouldUpdate = true;
//        unlockCanvasAndPost(canvas);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        } else {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
//            Log.w("DrawingView", "" + mBitmap);
            if (mPaint.getXfermode() == null) {
                canvas.drawPath(mPath, mPaint);
            }
        }
//        TODO: quizas esta asignacion tiene que ir adentro del else. Ver cuando caigo adentro del else.
        shouldUpdate = true;
    }

    public void clear() {
        Bitmap aux = mBitmap;
        mBitmap = Bitmap.createBitmap(ResourcesHelper.getScreenSize().x, ResourcesHelper.getScreenSize().y, Bitmap.Config.ARGB_8888);
        aux.recycle();
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        hasDrawing = false;
        invalidate();
    }

    public String saveDrawing() {
        if (file == null) {
            file = String.format(DRAWING, UUID.randomUUID().toString());
        }
        saveBitmap();
        return file;
    }

    public void loadDrawing(String drawing) {
        file = drawing;
        clear();
        if (file != null) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inMutable = true;
            mBitmap = BitmapFactory.decodeFile(FilesHelper.getDestinationVideoFolder() + "/" + file, opt);
            mCanvas = new Canvas(mBitmap);
            invalidate();
        }
    }

    public String duplicate() {
        if (!TextUtils.isEmpty(file)) {
            file = String.format(DRAWING, UUID.randomUUID().toString());
            saveBitmapBlocking();
        }
        return file;
    }

    private void saveBitmap() {
        Thread saveFile = new Thread(new Runnable() {

            @Override
            public void run() {
                saveBitmapBlocking();
            }
        });
        saveFile.start();
        ;
    }

    private void saveBitmapBlocking() {
        File save = new File(FilesHelper.getDestinationVideoFolder(), file);
        FilesHelper.saveBitmap(mBitmap, save.getAbsolutePath(), true);
        Log.w("SAVING_BITMAP", "bitmap saved");
    }

    public void setColor(int color) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);
        if (color == Color.TRANSPARENT) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            mPaint.setColor(color);
        }
    }

    public void setSize(int size) {
        mPaint.setStrokeWidth(size);
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        if (listener != null) {
            listener.onDrawingStarted();
        }
    }

    private void touch_move(float x, float y) {
        if (x < getX() || x >= getX() + getWidth()) return;
        if (y < getY() || y >= getY() + getHeight()) return;
//        Rect r = new Rect();
//        getGlobalVisibleRect(r);
//        r.contains((int)x, (int)y);
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
        if (mPaint.getXfermode() != null) {
            mCanvas.drawPath(mPath, mPaint);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        mPath = new Path();
        hasDrawing = true;
        if (listener != null) {
            listener.onDrawingFinished();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public boolean hasDrawing() {
        return hasDrawing;
    }

    public interface OnDrawingFinished {
        void onDrawingFinished();

        void onDrawingStarted();
    }

    public void setOnDrawingFinished(OnDrawingFinished listener) {
        this.listener = listener;
    }
}