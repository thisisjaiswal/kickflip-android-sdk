package io.kickflip.sdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import io.kickflip.sdk.gl_interfaces.GLRenderable;

/**
 * Created by framundo on 8/24/15.
 */
public class GLFrameView extends FrameLayout implements GLRenderable {

    private TextureDrawer mTextureDrawer;
    private boolean shouldDrawOnTexture;
    private boolean shouldDrawOnScreen;

    public GLFrameView(Context c) {
        super(c);
        something();
    }

    public GLFrameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        something();
    }

    public GLFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    public void dispatchDraw(Canvas canvas) {
        if (mTextureDrawer == null) return;
        Canvas glAttachedCanvas = mTextureDrawer.startDraw();

        if (glAttachedCanvas != null) {
            float xScale = glAttachedCanvas.getWidth() / (float)canvas.getWidth();
            glAttachedCanvas.scale(xScale, xScale);
            glAttachedCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

            if (shouldDrawOnTexture) {
                beforeTextureDraw();
                super.dispatchDraw(glAttachedCanvas);
                afterTextureDraw();
            }

            if (shouldDrawOnScreen) {
                beforeScreenDraw();
                super.dispatchDraw(canvas);
                afterScreenDraw();
            }

            mTextureDrawer.finishDraw(glAttachedCanvas);
        }
    }

    public void shouldDrawOnTexture(boolean shouldDrawOnTexture) {
        this.shouldDrawOnTexture = shouldDrawOnTexture;
    }

    public void shouldDrawOnScreen(boolean shouldDrawOnScreen) {
        this.shouldDrawOnScreen = shouldDrawOnScreen;
    }

    protected void beforeScreenDraw() {
    }
    protected void afterScreenDraw() {
    }
    protected void beforeTextureDraw() {
    }
    protected void afterTextureDraw() {
    }
}
