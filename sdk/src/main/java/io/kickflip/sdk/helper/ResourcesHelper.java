package io.kickflip.sdk.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.KickflipApplication;

public class ResourcesHelper {

    static DisplayMetrics metrics = new DisplayMetrics();
    static Point screenSize = new Point();

    static {
        setScreenSize();
    }

    public static Point setScreenSize() {
        WindowManager manager = (WindowManager) KickflipApplication.instance().getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        manager.getDefaultDisplay().getSize(screenSize);
        return screenSize;
    }

    public static int scale(float value) {
        return (int) (metrics.density * value);
    }

    public static Point getScreenSize() {
        return screenSize;
    }

    public static String getString(int resId) {
        return KickflipApplication.instance().getString(resId);
    }

    public static String getString(int resId, int qty) {
        return KickflipApplication.instance().getResources().getQuantityString(resId, qty);
    }

    public static Resources getResources() {
        return KickflipApplication.instance().getResources();
    }

    public static AssetManager getAssets() {
        return KickflipApplication.instance().getResources().getAssets();
    }

    public static String[] getStringArray(int resource) {
        return KickflipApplication.instance().getResources().getStringArray(resource);
    }

    public static int getDimensionPixelSize(int resource) {
        return KickflipApplication.instance().getResources().getDimensionPixelSize(resource);
    }

    public static Drawable getDrawable(int resource) {
        return KickflipApplication.instance().getResources().getDrawable(resource);
    }

    public static String getNotNullString(String text) {
        if (TextUtils.isEmpty(text) || "null".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }

    public static Bitmap roundImage(Bitmap originalImage, int width, int height) {
        return roundImage(originalImage, width, height, 0);
    }

    public static Bitmap roundImage(Bitmap originalImage, int width, int height, int color) {
        if (originalImage == null) {
            return null;
        }
        final Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final float originalWidth = originalImage.getWidth();
        final float originalHeight = originalImage.getHeight();
        Canvas canvas = new Canvas(background);
        final float scale = height / originalHeight;
        final float yTranslation = 0.0f;
        final float xTranslation = (width - originalWidth * scale) / 2.0f;
        final Matrix transformation = new Matrix();

        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        BitmapShader shader = new BitmapShader(originalImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setShader(shader);
        if (color != 0) {
            canvas.drawColor(color);
        }
        canvas.drawBitmap(originalImage, transformation, paint);

        Bitmap circleBitmap = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Bitmap.Config.ARGB_8888);
        shader = new BitmapShader(background, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas = new Canvas(circleBitmap);
        canvas.drawCircle(background.getWidth() / 2, background.getHeight() / 2, background.getWidth() / 2, paint);
        background.recycle();
        originalImage.recycle();
        return circleBitmap;
    }

}