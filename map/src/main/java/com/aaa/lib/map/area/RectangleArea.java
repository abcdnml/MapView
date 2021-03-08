package com.aaa.lib.map.area;

import android.graphics.Matrix;
import android.graphics.PointF;

import com.aaa.lib.map.MapUtils;


public class RectangleArea extends QuadrilateralArea {
    public PointF center;
    public float width;
    public float height;
    public float rotate;

    private Matrix mMatrix;

    public RectangleArea() {
        super();
        mMatrix = new Matrix();
        center = new PointF();
    }

    //设置矩形， 只修改rotate
    public void setRotate(float rotate) {
        this.setRect(center, width, height, rotate);
    }

    //设置矩形， 只修改center
    public void setCenter(float centerX, float centerY) {
        this.setRect(centerX, centerY, width, height, rotate);
    }

    /**
     * 通过四顶点的方式设置矩形
     */
    public void setRect(PointF lt, PointF rt, PointF rb, PointF lb) {
        setRect(lt.x, lt.y, rt.x, rt.y, rb.x, rb.y, lb.x, lb.y);
    }

    public void setRect(
            float ltx, float lty,
            float rtx, float rty,
            float rbx, float rby,
            float lbx, float lby) {
        super.setVertexs(ltx, lty, rtx, rty, rbx, rby, lbx, lby);

        this.center.x = (lt.x + rb.x) / 2;
        this.center.y = (lt.y + rb.y) / 2;
        this.width = (float) MapUtils.distance(lt, rt);
        this.height = (float) MapUtils.distance(lt, lb);
        this.rotate = MapUtils.getRotateByRect(rt, lt);

        mMatrix.setRotate(rotate, center.x, center.y);
    }


    /**
     * 通过中心点的方式设置矩形
     *
     * @param center 中心点
     * @param width  宽
     * @param height 高
     * @param rotate 旋转角度
     */
    public void setRect(PointF center, float width, float height, float rotate) {
        setRect(center.x, center.y, width, height, rotate);
    }

    public void setRect(float centerX, float centerY, float width, float height, float rotate) {
        this.center.x = centerX;
        this.center.y = centerY;
        this.width = width;
        this.height = height;
        this.rotate = rotate;
        this.mMatrix.setRotate(rotate, center.x, center.y);

        MapUtils.getTransformedPoint(mMatrix, center.x - width / 2, center.y - height / 2, lt);
        MapUtils.getTransformedPoint(mMatrix, center.x + width / 2, center.y - height / 2, rt);
        MapUtils.getTransformedPoint(mMatrix, center.x + width / 2, center.y + height / 2, rb);
        MapUtils.getTransformedPoint(mMatrix, center.x - width / 2, center.y + height / 2, lb);
        super.setVertexs(lt, rt, rb, lb);
    }

    @Override
    public String toString() {
        return "{" +
                "\"center\":" + center +
                ", \"width\":" + width +
                ", \"height\":" + height +
                ", \"rotate\":" + rotate +
                ", \"mMatrix\":" + mMatrix +
                ", \"lt\":" + lt +
                ", \"rt\":" + rt +
                ", \"rb\":" + rb +
                ", \"lb\":" + lb +
                '}';
    }
}
