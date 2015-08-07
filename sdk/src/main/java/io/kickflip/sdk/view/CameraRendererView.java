package io.kickflip.sdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import io.kickflip.sdk.R;
import io.kickflip.sdk.glmagic.GLFrameLayout;
import io.kickflip.sdk.view.drawing.TextureDrawer;

public class CameraRendererView extends FrameLayout {

    private Context context;
    private GLCameraEncoderView mCameraView;
    private GLFrameLayout glFrameContainer;

    public CameraRendererView(Context context) {
        super(context);
        initView(context);
    }

    public CameraRendererView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CameraRendererView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public CameraRendererView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.camera_renderer, this);
        setUi(view);
        init();
    }

    private void setUi(View view) {
        glFrameContainer = (GLFrameLayout) view.findViewById(R.id.gl_frame_container);
        mCameraView = (GLCameraEncoderView) view.findViewById(R.id.cameraPreview);
    }

    private void init() {
        mCameraView.setKeepScreenOn(true);
    }

    public void setOverlayLayout(int id) {
        View.inflate(context, id, glFrameContainer);
    }

    public void setTextureDrawer(TextureDrawer textureDrawer) {
        glFrameContainer.setTextureDrawer(textureDrawer);
    }

    public GLCameraEncoderView getCameraView() {
        return mCameraView;
    }
}
