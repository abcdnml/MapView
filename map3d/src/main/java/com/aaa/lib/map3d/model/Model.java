package com.aaa.lib.map3d.model;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import static android.opengl.GLES20.GL_REPEAT;


public abstract class Model<T> {

    protected static final int FLOAT_SIZE = 4;
    /**
     * 模型本身的平移缩放旋转 用于控制模型的位置大小
     */
    protected float[] scale = new float[]{1, 1, 1};
    protected float[] offset = new float[3];
    protected float[] rotate = new float[3];
    public int[] vao;
    public int[] vbo;
    protected T data;

    public static float[] getOriginalMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

    public void setOffset(float offsetX, float offsetY, float offsetZ) {
        this.offset[0] = offsetX;
        this.offset[1] = offsetY;
        this.offset[2] = offsetZ;
    }

    public void setRotate(float rotateX, float rotateY, float rotateZ) {
        this.rotate[0] = rotateX;
        this.rotate[1] = rotateY;
        this.rotate[2] = rotateZ;
    }

    public float[] getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale[0] = scale;
        this.scale[1] = scale;
        this.scale[2] = scale;
    }

    public float[] getOffset() {
        return offset;
    }

    public float[] getRotate() {
        return rotate;
    }


//    public abstract List<T> getData();

    public void clear() {
//        scale = new float[]{1, 1, 1};
//        offset = new float[3];
//        rotate = new float[3];
        if (vbo != null) {
            GLES30.glDeleteBuffers(vbo.length, vbo, 0);
        }

        if (vao != null) {
            GLES30.glDeleteVertexArrays(vao.length, vao, 0);
        }
    }

    public int[] getVao() {
        return vao;
    }

    public void setVao(int[] vao) {
        this.vao = vao;
    }

    public int[] getVbo() {
        return vbo;
    }

    public void setVbo(int[] vbo) {
        this.vbo = vbo;
    }

    protected int createTexture(Bitmap mBitmap) {
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成纹理ID
            GLES30.glGenTextures(1, texture, 0);

            //绑定纹理ID
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);

            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
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

    public T getData() {
        return data;
    }

    /**
     * 设置/更新模型数据
     * 此方法必须在GLThread中执行
     *
     * @param modelData 模型数据
     */
    public void setData(T data) {
        this.data = data;
    }
}
