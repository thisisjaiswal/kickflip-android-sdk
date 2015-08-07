package io.kickflip.sdk.view.drawing;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.kickflip.sdk.KickflipApplication;
import io.kickflip.sdk.R;
import io.kickflip.sdk.av.GlUtil;
import io.kickflip.sdk.glmagic.ViewToGLRenderer;

public class GLDrawingViewRenderer extends ViewToGLRenderer {

    private static final String TAG = "GLDrawingViewRenderer";

    public GLDrawingViewRenderer() {
        super();
        float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
        float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
        pVertex = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put ( vtmp );
        pVertex.position(0);
        pTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put ( ttmp );
        pTexCoord.position(0);
    }

    private static final String vss =
            "attribute vec2 vPosition;\n" +
                    "attribute vec2 vTexCoord;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  texCoord = vTexCoord;\n" +
                    "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
                    "}";

    private static final String fss =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
                    "}";

    private FloatBuffer pVertex;
    private FloatBuffer pTexCoord;
    private int hProgram;

    private int mFrameCount;
    private int pix = 0;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);



    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        mGlSurfaceTexture = mFullScreenOverlay.createTextureObject();
//        mGlSurfaceTexture = GlUtil.createTextureFromImage(BitmapFactory.decodeResource(KickflipApplication.instance().getResources(), R.drawable.ic_camreverse));
        Log.d(TAG, "drawingView texture: " + mGlSurfaceTexture);
        super.onSurfaceChanged(gl, width, height);

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.2f);
        hProgram = loadShader ( vss, fss );
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT );
        GlUtil.checkGlError("clear");

        synchronized(this) {
            if ( VGLUpdate ) {
                Canvas canvas = onDrawViewBegin();
                Paint paint = new Paint();
                paint.setColor(Color.YELLOW);
                canvas.drawPoint(pix, pix, paint);
                onDrawViewEnd();
                mSurfaceTexture.updateTexImage();
                VGLUpdate = false;
            }
        }

        GLES20.glUseProgram(hProgram);

        int ph = GLES20.glGetAttribLocation(hProgram, "vPosition");
        int tch = GLES20.glGetAttribLocation ( hProgram, "vTexCoord" );
        int th = GLES20.glGetUniformLocation ( hProgram, "sTexture" );
        GlUtil.checkGlError("attributes");

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GlUtil.checkGlError("blend");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GlUtil.checkGlError("active");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mGlSurfaceTexture);
        Log.d("ON DRAW", "ID>" + mGlSurfaceTexture);
        GlUtil.checkGlError("bind");
        GLES20.glUniform1i(th, 0);
        GlUtil.checkGlError("uniform");
        GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, pVertex);
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, pTexCoord);
        GLES20.glEnableVertexAttribArray(ph);
        GLES20.glEnableVertexAttribArray(tch);
        GlUtil.checkGlError("vertex");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GlUtil.checkGlError("draw");
        GLES20.glFlush();
        GlUtil.checkGlError("flush");
        Log.d("RENDERER", "DRAW FRAME");

        pix++;
        if (pix > 300) {
            pix = 0;
        }
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
}
