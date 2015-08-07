package io.kickflip.sdk.glmagic;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import io.kickflip.sdk.view.drawing.TextureDrawer;

public class GLFrameLayout extends FrameLayout implements GLRenderable {

    private TextureDrawer mTextureDrawer;

    public GLFrameLayout(Context context) {
        super(context);
    }

    public GLFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GLFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTextureDrawer(TextureDrawer textureDrawer) {
        mTextureDrawer = textureDrawer;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTextureDrawer == null) return;
        Canvas glAttachedCanvas = mTextureDrawer.startDraw();

        if (glAttachedCanvas != null) {
            float xScale = glAttachedCanvas.getWidth() / (float)canvas.getWidth();
            glAttachedCanvas.scale(xScale, xScale);
            super.onDraw(glAttachedCanvas);
        }
        mTextureDrawer.finishDraw(glAttachedCanvas);
    }

}
