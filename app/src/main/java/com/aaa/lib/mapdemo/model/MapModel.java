package com.aaa.lib.mapdemo.model;

import com.aaa.lib.map3d.model.ObjModel;

public class MapModel extends ObjModel {
    private int[] clipArea = new int[4];
    private float unit = 0.01f; // 每个像素单元所占大小

    public MapModel() {
        super();
    }

    public int[] getClipArea() {
        return clipArea;
    }

    public void setClipArea(int[] clipArea) {
        this.clipArea = clipArea;
    }

    public float getUnit() {
        return unit;
    }

    public void setUnit(float unit) {
        this.unit = unit;
    }

    public int getMapCenterX() {
        return (clipArea[0] + clipArea[2]) / 2;
    }

    public int getMapCenterY() {
        return (clipArea[1] + clipArea[3]) / 2;
    }

}
