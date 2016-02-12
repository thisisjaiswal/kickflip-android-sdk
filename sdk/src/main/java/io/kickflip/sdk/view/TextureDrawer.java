package io.kickflip.sdk.view;

import android.graphics.Canvas;

public interface TextureDrawer {

    public Canvas startDraw();

    public void finishDraw(Canvas canvas);
}
