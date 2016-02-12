package io.kickflip.sdk.av;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.kickflip.sdk.view.TextureDrawer;

/**
 * @hide
 */
class CameraSurfaceRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, TextureDrawer {


    private final String vss =
            "attribute vec2 vPosition;\n" +
                    "attribute vec2 vTexCoord;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  texCoord = vTexCoord;\n" +
                    "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
                    "}";

    private final String fss =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
                    "}";

    private int[] hTex;
    private FloatBuffer pVertex;
    private FloatBuffer pTexCoord;
    private int hProgram;

    private SurfaceTexture mSTexture;
    Surface surface;

    private static final String TAG = "CameraSurfaceRenderer";
    private static final boolean VERBOSE = false;

    private CameraEncoder mCameraEncoder;

    private FullFrameRect mFullScreenCamera;
    private FullFrameRect mFullScreenOverlay;     // For texture overlay

    private final float[] mSTMatrix = new float[16];
    private int mOverlayTextureId = -5;
    private int mCameraTextureId;

    private boolean mRecordingEnabled;

    private int mFrameCount;

    // Keep track of selected filters + relevant state
    private boolean mIncomingSizeUpdated;
    private int mIncomingWidth;
    private int mIncomingHeight;
    private int mCurrentFilter;
    private int mNewFilter;

    boolean showBox = false;


    /**
     * Constructs CameraSurfaceRenderer.
     * <p/>
     *
     * @param recorder video encoder object
     */
    public CameraSurfaceRenderer(CameraEncoder recorder) {
        mCameraEncoder = recorder;

        mCameraTextureId = -1;
        mFrameCount = -1;

        SessionConfig config = recorder.getConfig();
        mIncomingWidth = config.getVideoWidth();
        mIncomingHeight = config.getVideoHeight();
        mIncomingSizeUpdated = true;        // Force texture size update on next onDrawFrame

        mCurrentFilter = -1;
        mNewFilter = Filters.FILTER_NONE;

        mRecordingEnabled = false;


        float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
        float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
        pVertex = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put ( vtmp );
        pVertex.position(0);
        pTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put ( ttmp );
        pTexCoord.position(0);
    }

    public void newTexture(int texture) {
        mOverlayTextureId = texture;
    }


    /**
     * Notifies the renderer that we want to stop or start recording.
     */
    public void changeRecordingState(boolean isRecording) {
        Log.d(TAG, "changeRecordingState: was " + mRecordingEnabled + " now " + isRecording);
        mRecordingEnabled = isRecording;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
//        super.onSurfaceCreated(unused, config);
//        super.onSurfaceCreated(unused, config);
        Log.d(TAG, "onSurfaceCreated");
        // Set up the texture blitter that will be used for on-screen display.  This
        // is *not* applied to the recording, because that uses a separate shader.
        mFullScreenCamera = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));

        mCameraTextureId = mFullScreenCamera.createTextureObject();
        mCameraEncoder.onSurfaceCreated(mCameraTextureId);
        mFrameCount = 0;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // For texture overlay:
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mFullScreenOverlay = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));

        initTex();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        GlUtil.checkGlError("initTex");
        mSTexture = new SurfaceTexture ( hTex[0] );
        mSTexture.setDefaultBufferSize(width, height);
        surface = new Surface(mSTexture);

        mSTexture.setOnFrameAvailableListener(this);


        mCameraEncoder.updateOverlay(hTex[0], mSTexture);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        hProgram = loadShader ( vss, fss );

        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if (VERBOSE) {
            if (mFrameCount % 30 == 0) {
                Log.d(TAG, "onDrawFrame tex=" + mCameraTextureId);
                mCameraEncoder.logSavedEglState();
            }
        }

        if (mCurrentFilter != mNewFilter) {
            Filters.updateFilter(mFullScreenCamera, mNewFilter);
            mCurrentFilter = mNewFilter;
            mIncomingSizeUpdated = true;
        }

        if (mIncomingSizeUpdated) {
            mFullScreenCamera.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
//            mFullScreenOverlay.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
            mIncomingSizeUpdated = false;
            Log.i(TAG, "setTexSize on display Texture");
        }

        // Draw the video frame.
        if (mCameraEncoder.isSurfaceTextureReadyForDisplay()) {
            mCameraEncoder.getSurfaceTextureForDisplay().updateTexImage();
            mCameraEncoder.getSurfaceTextureForDisplay().getTransformMatrix(mSTMatrix);
            //Drawing texture overlay:
            mFullScreenCamera.drawFrame(mCameraTextureId, mSTMatrix);
        }
        if (mSTexture != null) {
            mSTexture.updateTexImage();
            mSTexture.getTransformMatrix(mSTMatrix);
            mFullScreenOverlay.drawFrame(hTex[0], mSTMatrix);
        }
        mFrameCount++;
        if (mFrameCount % 100000 == 0)
            mFrameCount = 0;
    }

    public void signalVertialVideo(FullFrameRect.SCREEN_ROTATION isVertical) {
        if (mFullScreenCamera != null) mFullScreenCamera.adjustForVerticalVideo(isVertical, false);
    }

    /**
     * Changes the filter that we're applying to the camera preview.
     */
    public void changeFilterMode(int filter) {
        mNewFilter = filter;
    }

    public void overlay(Bitmap bitmap) {
        mOverlayTextureId = GlUtil.createTextureFromImage(bitmap);
//        mCameraEncoder.updateOverlay(mOverlayTextureId);
    }

    public void handleTouchEvent(MotionEvent ev) {
        mFullScreenCamera.handleTouchEvent(ev);
    }

    private void initTex() {
        hTex = new int[1];
        GLES20.glGenTextures(1, hTex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }

    private static int loadShader ( String vss, String fss ) {
        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vshader, vss);
        GLES20.glCompileShader(vshader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile vshader");
            Log.v("Shader", "Could not compile vshader:"+GLES20.glGetShaderInfoLog(vshader));
            GLES20.glDeleteShader(vshader);
            vshader = 0;
        }

        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fshader, fss);
        GLES20.glCompileShader(fshader);
        GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile fshader");
            Log.v("Shader", "Could not compile fshader:" + GLES20.glGetShaderInfoLog(fshader));
            GLES20.glDeleteShader(fshader);
            fshader = 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        return program;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    public Canvas startDraw() {
        if (surface == null) {
            Log.w("CameraSurfaceRenderer", "NOT READY TO DRAW!");
            return null;
        }
        return surface.lockCanvas(null);
    }

    public void finishDraw(Canvas canvas) {
        if (surface == null) {
            Log.w("CameraSurfaceRenderer", "whaaaaaat?!?!?");
            return;
        }
        surface.unlockCanvasAndPost(canvas);
    }
}
