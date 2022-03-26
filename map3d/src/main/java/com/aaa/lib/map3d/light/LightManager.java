package com.aaa.lib.map3d.light;

import android.opengl.Matrix;

import java.util.List;

public class LightManager {
    DirectLight directLight;
    List<PointLight> pointLightList;
    private float[] tempNewLightDir = new float[4];
    private float[] tempOldLightDir = new float[4];

    public LightManager(){
        directLight = new DirectLight(new float[]{-50f, -50f, 0});
        tempOldLightDir=new float[]{-50f, -50f, 0, 1};//默认的光方向 多出的一位是为了矩阵计算
        directLight.ka = new float[]{0.2f, 0.2f, 0.2f};
        directLight.kd = new float[]{0.8f, 0.8f, 0.8f};
        directLight.ks = new float[]{0.1f, 0.1f, 0.1f};
    }

    public float[] getRotatedLight(float[] modelMatrix){
        Matrix.multiplyMV(tempNewLightDir, 0, modelMatrix, 0, tempOldLightDir, 0);
        directLight.dirction[0] = tempNewLightDir[0];
        directLight.dirction[1] = tempNewLightDir[1];
        directLight.dirction[2] = tempNewLightDir[2];
        return tempNewLightDir;
    }

    public DirectLight getDirectLight() {
        return directLight;
    }
}
