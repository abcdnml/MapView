package com.aaa.lib.map;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;

import com.aaa.lib.map.area.CircleArea;
import com.aaa.lib.map.area.LineArea;
import com.aaa.lib.map.area.RectangleArea;

public class MapUtils {

    private static final String TAG = "MapUtils";
    private static float[] tmpPoint = new float[2];
    private static Matrix inverseMatrix = new Matrix();

    /**
     * 判断禁区是否在在充电桩保护范围内
     */
    public static boolean isLineCrossPower(LineArea line, CircleArea circle) {
        //判断圆心到线段的距离 如果小于半径 说明相交
        if (pointToLine(circle.x, circle.y, line.p1.x, line.p1.y, line.p2.x, line.p2.y) <= circle.radius) {
            return true;
        }
        return false;
    }

    /**
     * 判断禁区是否在在充电桩保护范围内
     */
    public static boolean isAreaCrossPower(RectangleArea rect, CircleArea circle) {
        return isAreaCrossPower(circle.x, circle.y, circle.radius,
                rect.lt.x, rect.lt.y,
                rect.rt.x, rect.rt.y,
                rect.rb.x, rect.rb.y,
                rect.lb.x, rect.lb.y
        );
    }

    /**
     * 判断禁区是否在在充电桩保护范围内
     */
    public static boolean isAreaCrossPower(double cx, double cy, double r, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        //1 判断圆心是否在矩形内
        //2 判断矩形的四个点是否在圆内
        //3 判断圆心与矩形四条边的距离是否小于半径
        if (isPointInRectangle(x1, y1, x2, y2, x3, y3, x4, y4,cx, cy) ||
                isRectVectexInCircle(cx, cy, r, x1, y1, x2, y2, x3, y3, x4, y4) ||
                isRectBorderCrossCircle(cx, cy, r, x1, y1, x2, y2, x3, y3, x4, y4)) {
            return true;
        }
        return false;
    }


    private static boolean isRectVectexInCircle(double cx, double cy, double r, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        // 判断矩形的四个点是否在圆内：判断圆心到4点距离是否小于半径
        if (distance(cx, cy, x1, y1) <= r
                || distance(cx, cy, x2, y2) <= r
                || distance(cx, cy, x3, y3) <= r
                || distance(cx, cy, x4, y4) <= r) {
            Log.i(TAG, "isResctrictCrossChargePile isRectVectexInCircle: true");
            return true;
        }
        Log.i(TAG, "isResctrictCrossChargePile isRectVectexInCircle: false");
        return false;
    }

    private static boolean isRectBorderCrossCircle(double cx, double cy, double r, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        //判断圆心与矩形四条边的距离是否小于半径
        if (pointToLine(cx, cy, x1, y1, x2, y2) <= r
                || pointToLine(cx, cy, x2, y2, x3, y3) <= r
                || pointToLine(cx, cy, x3, y3, x4, y4) <= r
                || pointToLine(cx, cy, x1, y1, x4, y4) <= r) {
            Log.i(TAG, "isResctrictCrossChargePile isRectBorderCrossCircle: true");
            return true;
        }
        Log.i(TAG, "isResctrictCrossChargePile isRectBorderCrossCircle: false");
        return false;
    }

    /**
     * 判断点击的点是否在端点圆形区域内
     *
     * @param x      点击的x
     * @param y      点击的y
     * @param p      圆心
     * @param radius 半径
     * @return boolean
     */
    public static boolean isPointInCircle(double x, double y, PointF p, float radius) {
        double distanceX = Math.abs(x - p.x);
        double distanceY = Math.abs(y - p.y);
        int distanceZ = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));

        if (distanceZ <= radius) {
            return true;
        } else {
            return false;
        }
    }

/*    public static boolean isInRect(float x, float y, PointF center, int width, int height, double rotate) {
        isPointInRectangle(x, y, );
        return true;
    }*/

    public static boolean isInRect(PointF p, PointF lt, PointF rt, PointF rb, PointF lb) {
        return isPointInRectangle(p.x, p.y, lt.x, lt.y, rt.x, rt.y, rb.x, rb.y, lb.x, lb.y);
    }

    //判断是否在矩形内：
    // 只需要判断该点是否在上下两条边和左右两条边之间就行。
    // 判断一个点是否在两条线段之间夹着就转化成，
    // 判断一个点是否在某条线段的一边上，就可以利用叉乘的方向性
    public static boolean isPointInRectangle(double p1x, double p1y, double p2x, double p2y, double p3x, double p3y, double p4x, double p4y, double px, double py) {
        boolean result = GetCross(p1x, p1y, p2x, p2y, px, py) * GetCross(p3x, p3y, p4x, p4y, px, py) >= 0 && GetCross(p2x, p2y, p3x, p3y, px, py) * GetCross(p4x, p4y, p1x, p1y, px, py) >= 0;
        return result;
    }
    private static double GetCross(double p1x, double p1y, double p2x, double p2y, double px, double py) {
        return (p2x - p1x) * (py - p1y) - (px - p1x) * (p2y - p1y);
    }



    public static PointF getCross(float x1, float y1, float x2, float y2, float x0, float y0) {
        float x = x1;
        float y = y0;
        if (x1 != x2) {
            float k1 = (y2 - y1) / (x2 - x1);
            x = (k1 * k1 * x1 + k1 * (y0 - y1) + x0) / (k1 * k1 + 1);
            y = k1 * (x - x1) + y1;
        }
        return new PointF(x, y);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double distance(PointF p1, PointF p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    /**
     * 判断点击的点是否在端点圆形区域内
     */
    public static boolean isInCircle(float x, float y, float cx, float cy, float cr) {
        float distanceX = Math.abs(x - cx);
        float distanceY = Math.abs(y - cy);
        int distanceZ = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));

        //pointRadus*3 为了扩大点击范围
        if (distanceZ <= cr) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 计算点到线段距离
     *
     * @param cx
     * @param cy
     * @param x1 线段端点1 x
     * @param y1 线段端点1 y
     * @param x2 线段端点2 x
     * @param y2 线段端点2 y
     * @return
     */
    public static double pointToLine(double cx, double cy, double x1, double y1, double x2, double y2) {
        double space = 0;
        double a, b, c;
        a = distance(x1, y1, x2, y2);// 线段的长度
        b = distance(x1, y1, cx, cy);// (x1,y1)到点的距离
        c = distance(x2, y2, cx, cy);// (x2,y2)到点的距离
        Log.i(TAG, "pointToLine a: " + a + " b : " + b + " c : " + c);
        if (c <= 0.000001 || b <= 0.000001) {
            space = 0;
            return space;
        }
        if (a <= 0.000001) {
            space = b;
            return space;
        }
        if (c * c >= a * a + b * b) {
            space = b;
            return space;
        }
        if (b * b >= a * a + c * c) {
            space = c;
            return space;
        }
        // 海伦公式求面积
        double p = (a + b + c) / 2;// 半周长
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));
        space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
        return space;
    }


    /**
     * 获取旋转角度
     *
     * @param rb     右上角的点
     * @param center 中心点
     * @return
     */
    public static float getRotateByRect(PointF rb, PointF center) {
        return getRotateByRect(rb.x, rb.y, center.x, center.y);
    }

    public static float getRotateByRect(float x, float y, float centerX, float centerY) {
        float radian = (float) Math.atan2(y - centerY, x - centerX);
        return (float) (180 * radian / Math.PI);
    }

    /**
     * 获取旋转后的点
     * 旋转角度为当前矩形旋转角度
     *
     * @param x x
     * @param y y
     * @return 旋转后的点
     */
    public static PointF getTransformedPoint(Matrix matrix, float x, float y) {
        PointF newPoint = new PointF(x, y);
        getTransformedPoint(matrix, x, y, newPoint);
        return newPoint;
    }

    public static void getTransformedPoint(Matrix matrix, float x, float y, PointF newPoint) {
        tmpPoint[0] = x;
        tmpPoint[1] = y;
        matrix.mapPoints(tmpPoint);
        newPoint.x = tmpPoint[0];
        newPoint.y = tmpPoint[1];
    }

    public static void getTransformedPoint(Matrix matrix, PointF point) {
        tmpPoint[0] = point.x;
        tmpPoint[1] = point.y;
        matrix.mapPoints(tmpPoint);
        point.x = tmpPoint[0];
        point.y = tmpPoint[1];
    }

    /**
     * @param x x
     * @param y y
     * @return 旋转后的点
     */
    public static void getInverseRotatedPoint(Matrix matrix, float x, float y, float[] point) {
        matrix.invert(inverseMatrix);
        point[0] = x;
        point[1] = y;
        inverseMatrix.mapPoints(point);
    }
}
