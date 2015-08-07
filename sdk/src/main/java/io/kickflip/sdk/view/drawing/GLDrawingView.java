package io.kickflip.sdk.view.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import io.kickflip.sdk.glmagic.GLRenderable;
import io.kickflip.sdk.glmagic.ViewToGLRenderer;

public class GLDrawingView extends DrawingView implements GLRenderable {

    private TextureDrawer mTextureDrawer;

    public GLDrawingView(Context c) {
        super(c);
        something();
    }

    public GLDrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        something();
    }

    public GLDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        something();
    }

    private void something() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    public void setTextureDrawer(TextureDrawer mTextureDrawer) {
        this.mTextureDrawer = mTextureDrawer;
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
