package com.aaa.lib.map3d.light;


public class DirectLight extends Light {
    public float[] dirction=new float[3];
    public DirectLight(float[] direction){
        this.dirction=direction;
    }
}
