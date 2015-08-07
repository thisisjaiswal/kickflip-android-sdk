package io.kickflip.sdk.view.drawing;

import android.graphics.Bitmap;

public interface DrawingProvider {

    public boolean shouldUpdate();
    public boolean bitmapReady();
    public boolean preparingBitmap();
    public void setPreparingBitmap();
    public void prepareBitmap();

    /*
    * If called when bitmap is not ready IllegalStateException is thrown
    * */
    public Bitmap getBitmap();
}
