package com.aaa.lib.map3d.program;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;


import com.aaa.lib.map3d.model.Model;
import com.aaa.lib.map3d.utils.ShaderUtil;

import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.glDeleteProgram;

public abstract class GLProgram<T> {

    public static final int TEXTURE_INDEX_SHADOW = 0;
    public static final int TEXTURE_INDEX_KD = 1;
    public static final int TEXTURE_INDEX_KD_NORMAL = 2;
    public static final int TEXTURE_INDEX_AREA = 3;

    protected static Context context;
    protected int programId;

    public GLProgram() {
    }

    public static void init(Context ctx) {
        context = ctx;
    }

    public void use() {
        if (programId == 0) {
            throw new IllegalStateException("program not init");
        }
        GLES30.glUseProgram(programId);
    }

    public abstract void initLocation();

    public abstract void genVao(T model);

    public void destroy() {
        glDeleteProgram(programId);
        programId = 0;
    }


    public boolean createFromAsset(Context context, String vertexResource, String fragmentResource) {
        String vertexShaderCode = ShaderUtil.loadFromAssetsFile(vertexResource, context.getResources());
        String fragmentShaderCode = ShaderUtil.loadFromAssetsFile(fragmentResource, context.getResources());
        return create(vertexShaderCode, fragmentShaderCode);
    }

    public boolean create(String vertexShaderCode, String fragmentShaderCode) {
        programId = ShaderUtil.createProgram(vertexShaderCode, fragmentShaderCode);
        Log.i("GLProgram", "program: "+ programId);
        initLocation();
        return programId == 0;
    }

    public float[] conbineModelMatrix(Model model, float[] worldMMatrix) {
        float[] matrix = new float[16];
        System.arraycopy(worldMMatrix, 0, matrix, 0, worldMMatrix.length);

        //先做模型本身的 平移/旋转/缩放, 顺序不能乱
        float[] offset = model.getOffset();
        Matrix.translateM(matrix, 0, offset[0], offset[1], offset[2]);

        float[] rotate = model.getRotate();
        Matrix.rotateM(matrix, 0, rotate[0], 1, 0, 0);
        Matrix.rotateM(matrix, 0, rotate[1], 0, 1, 0);
        Matrix.rotateM(matrix, 0, rotate[2], 0, 0, 1);

        float[] scale = model.getScale();
        Matrix.scaleM(matrix, 0, scale[0], scale[1], scale[2]);
        return matrix;
    }


    protected int createTexture(Bitmap mBitmap) {
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成纹理ID
            GLES30.glGenTextures(1, texture, 0);

            //绑定纹理ID
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);

            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR); //使用nearest缩小时会糊
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GL_REPEAT);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GL_REPEAT);


            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0);
            // 生成MIP贴图
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

            // 数据如果已经被加载进OpenGL,则可以回收该bitmap
            //            bitmap.recycle();

            // 取消绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            return texture[0];
        }
        return -1;
    }
}
