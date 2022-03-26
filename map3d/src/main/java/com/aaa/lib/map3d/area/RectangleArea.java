package com.aaa.lib.map3d.area;


import android.graphics.Matrix;
import android.graphics.PointF;

public class RectangleArea extends QuadrilateralArea {
    static float[] tmpPoint = new float[2];
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

    public static void getTransformedPoint(Matrix matrix, float x, float y, PointF newPoint) {
        tmpPoint[0] = x;
        tmpPoint[1] = y;
        matrix.mapPoints(tmpPoint);
        newPoint.x = tmpPoint[0];
        newPoint.y = tmpPoint[1];
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
        this.width = (float) distance(lt, rt);
        this.height = (float) distance(lt, lb);
        this.rotate = getRotateByRect(rt, lt);

        mMatrix.setRotate(rotate, center.x, center.y);
    }

    /**
     * 获取旋转角度
     *
     * @param rb     右上角的点
     * @param center 中心点
     */
    public static float getRotateByRect(PointF rb, PointF center) {
        return getRotateByRect(rb.x, rb.y, center.x, center.y);
    }

    public static float getRotateByRect(float x, float y, float centerX, float centerY) {
        float radian = (float) Math.atan2(y - centerY, x - centerX);
        return (float) (180 * radian / Math.PI);
    }


    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double distance(PointF p1, PointF p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
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

        getTransformedPoint(mMatrix, center.x - width / 2, center.y - height / 2, lt);
        getTransformedPoint(mMatrix, center.x + width / 2, center.y - height / 2, rt);
        getTransformedPoint(mMatrix, center.x + width / 2, center.y + height / 2, rb);
        getTransformedPoint(mMatrix, center.x - width / 2, center.y + height / 2, lb);
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