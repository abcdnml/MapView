package com.aaa.lib.map3d.model;

import android.content.Context;

import com.aaa.lib.map3d.obj.ModelData;

public abstract class Model {

    protected int programId;
    protected String vertexShaderCode;
    protected String fragmentShaderCode;

    protected float[] scale = new float[]{1,1,1};
    protected float[] offset = new float[3];
    protected float[] rotate = new float[3];

    protected float[] mMatrix = new float[16];
    protected float[] pMatrix = new float[16];
    protected float[] vMatrix = new float[16];


    public Model() {
    }


    public static float[] getOriginalMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

    public void setScale(float scale) {
        this.scale[0] = scale;
        this.scale[1] = scale;
        this.scale[2] = scale;
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

    public abstract void setMatrix(float[] mMatrix, float[] vMatrix, float[] pMatrix);

    public void setEye(float[] eye) {

    }

    public void setLight(float[] light) {
    }

    public abstract void onCreate(Context context);

    public abstract void onDraw();

    public abstract void onDestory();

    public void updateModelData(ModelData data) {
    }

}
