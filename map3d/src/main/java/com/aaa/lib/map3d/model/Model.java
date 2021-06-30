package com.aaa.lib.map3d.model;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.nio.FloatBuffer;

public abstract class Model {

    protected int programId;
    protected String vertexShaderCode;
    protected String fragmentShaderCode;

    protected static final int FLOAT_SIZE = 4;
    private static final String TAG = Model.class.getSimpleName();
    protected FloatBuffer vertexBuffer;
    protected FloatBuffer colorBuffer;
    protected Context context;

    public Model(Context context) {
        this.context = context;
    }

    /**
     * 编译
     *
     * @param type       顶点着色器:GLES30.GL_VERTEX_SHADER
     *                   片段着色器:GLES30.GL_FRAGMENT_SHADER
     * @param shaderCode
     * @return
     */
    protected static int compileShader(int type, String shaderCode) {
        //创建一个着色器
        final int shaderId = GLES30.glCreateShader(type);
        Log.e(TAG, "glCreateShader : " + shaderId);
        if (shaderId != 0) {
            //加载到着色器
            GLES30.glShaderSource(shaderId, shaderCode);
            //编译着色器
            GLES30.glCompileShader(shaderId);
            //检测状态
            final int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                String logInfo = GLES30.glGetShaderInfoLog(shaderId);
                System.err.println(logInfo);
                Log.e(TAG, "compileShader : " + type + " " + logInfo);
                //创建失败
                GLES30.glDeleteShader(shaderId);
                return 0;
            }
            return shaderId;
        } else {
            //创建失败
            return 0;
        }
    }

    /**
     * 链接小程序
     *
     * @param vertexShaderId   顶点着色器
     * @param fragmentShaderId 片段着色器
     * @return
     */
    protected static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programId = GLES30.glCreateProgram();
        Log.e(TAG, "linkProgram programId: " + programId);
        if (programId != 0) {
            //将顶点着色器加入到程序
            GLES30.glAttachShader(programId, vertexShaderId);
            //将片元着色器加入到程序中
            GLES30.glAttachShader(programId, fragmentShaderId);
            //链接着色器程序
            GLES30.glLinkProgram(programId);
            final int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                String logInfo = GLES30.glGetProgramInfoLog(programId);
                Log.e(TAG, "linkProgram err: " + logInfo);
                GLES30.glDeleteProgram(programId);
                return 0;
            }
            return programId;
        } else {
            //创建失败
            return 0;
        }
    }

    protected static int createGLProgram(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShaderId = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderId = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            Log.e(TAG, " shader id is 0  vertex :" + vertexShaderId + " color: " + fragmentShaderId);
            return 0;
        }

        int programId = linkProgram(vertexShaderId, fragmentShaderId);
        if (programId == 0) {
            Log.e(TAG, " program id is 0");
            return 0;
        }
        return programId;
    }


    public static float[] getOriginalMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }


    public abstract void setMatrix(float[] mMatrix, float[] vMatrix, float[] pMatrix);

    public void setEye(float[] eye) {

    }

    public void setLight(float[] light) {
    }

    public abstract void onSurfaceCreate(Context context);

    public abstract void onDraw();

    public abstract void onSurfaceChange(int width, int height);
}
