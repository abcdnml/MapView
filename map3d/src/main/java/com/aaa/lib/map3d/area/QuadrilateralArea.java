package com.aaa.lib.map3d.area;

import android.graphics.PointF;

public class QuadrilateralArea implements Area {
    /**
     * 区域的左上 右上 右下 左下 四个点的坐标
     */
    public PointF lt;
    public PointF rt;
    public PointF rb;
    public PointF lb;

    public QuadrilateralArea() {
        lt = new PointF();
        rt = new PointF();
        rb = new PointF();
        lb = new PointF();
    }


    public void setLt(float x, float y) {
        this.lt.x = x;
        this.lt.y = y;
    }

    public void setLb(float x, float y) {
        this.lb.x = x;
        this.lb.y = y;
    }


    public void setRt(float x, float y) {
        this.rt.x = x;
        this.rt.y = y;
    }


    public void setRb(float x, float y) {
        this.rb.x = x;
        this.rb.y = y;
    }

    public void setVertexs(PointF lt, PointF rt, PointF rb, PointF lb) {
        this.lt = lt;
        this.rt = rt;
        this.rb = rb;
        this.lb = lb;
    }

    public void setVertexs(float ltx, float lty,
                           float rtx, float rty,
                           float rbx, float rby,
                           float lbx, float lby) {
        this.lt.x = ltx;
        this.lt.y = lty;

        this.rt.x = rtx;
        this.rt.y = rty;

        this.rb.x = rbx;
        this.rb.y = rby;

        this.lb.x = lbx;
        this.lb.y = lby;

    }
}