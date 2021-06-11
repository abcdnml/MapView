package com.aaa.lib.mapdemo;

import android.graphics.Bitmap;

import java.util.List;

/**
 * 地图数据
 * <p>
 * width 宽
 * height 高
 * resolution 分辨率
 * x_min 世界坐标x偏移
 * y_min 世界坐标y偏移
 * dockerPosX 充电座x
 * dockerPosY 充电座y
 * frameNumber当前地图帧数
 * mapId 地图id
 * pathId 路径ID
 * fullMapData 地图数据
 */
public class LDMapBean {
    public int width;
    public int height;
    public float resolution;
    public float x_min;
    public float y_min;
    public double dockerPosX;
    public double dockerPosY;
    public double dockertheta;
    public int frameNumber;
    public long mapId;
    public long pathId;
    public int[] fullMapData;
    public List<Integer> path;
    public String baseMapData;
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap newBitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        this.bitmap = newBitmap;
    }

    public void clear() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        baseMapData = null;
        fullMapData = null;
        pathId = -1;
        mapId = -1;
        frameNumber = -1;
        width = 0;
        height = 0;
        resolution = 1;
        x_min = 0;
        y_min = 0;
        dockerPosX = 0;
        dockerPosY = 0;
    }

    @Override
    public String toString() {
        return "LDMapBean{" +
                "width=" + width +
                ", height=" + height +
                ", resolution=" + resolution +
                ", x_min=" + x_min +
                ", y_min=" + y_min +
                ", dockerPosX=" + dockerPosX +
                ", dockerPosY=" + dockerPosY +
                ", dockertheta=" + dockertheta +
                ", frameNumber=" + frameNumber +
                ", mapId=" + mapId +
                ", pathId=" + pathId +
                ", bitmap=" + bitmap +
                '}';
    }
}
