package io.kickflip.sdk.view.testrender;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import io.kickflip.sdk.view.drawing.TextureDrawer;

public class MainView extends GLSurfaceView {
    public MainRenderer mRenderer;

    public MainView ( Context context ) {
        super(context);
        init();
    }

    public MainView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public MainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        init();
    }

    private void init() {
        Log.w("MainView", "STARTED");
        mRenderer = new MainRenderer(this);
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setRenderer ( mRenderer );
        setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
    }


    public void surfaceCreated ( SurfaceHolder holder ) {
        super.surfaceCreated ( holder );
    }

    public void surfaceDestroyed ( SurfaceHolder holder ) {
        mRenderer.close();
        super.surfaceDestroyed ( holder );
    }

    public void surfaceChanged ( SurfaceHolder holder, int format, int w, int h ) {
        super.surfaceChanged ( holder, format, w, h );
    }

    public TextureDrawer getTextureDrawer() {
        return mRenderer;
    }
}