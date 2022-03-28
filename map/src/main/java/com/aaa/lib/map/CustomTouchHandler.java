package com.aaa.lib.map;

import android.graphics.PointF;
import android.view.MotionEvent;

public class CustomTouchHandler {

    private static final int MODE_NONE=0;
    private static final int MODE_DRAG=3;
    private static final int MODE_SCALE=1;

    private int mode;
    private PointF middlePoint=new PointF();
    private PointF downPoint=new PointF();
    private float lastDistance;
    private float lastRotation;
    private MapView mapView;
    public CustomTouchHandler(MapView mapView){
        this.mapView=mapView;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                downPoint.x = event.getX();
                downPoint.y = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_SCALE;
                lastDistance = spacing(event);
                lastRotation = rotation(event);
                midPoint(middlePoint, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_SCALE) {
                    float rotation = rotation(event) - lastRotation;
                    float newDistance = spacing(event);
                    float scale = newDistance / lastRotation;
                    mapView.scale(scale, middlePoint.x, middlePoint.y);// 縮放
                    mapView.rotate(rotation, middlePoint.x, middlePoint.y);// 旋轉

                    lastRotation =rotation;
                    lastDistance =newDistance;

                } else if (mode == MODE_DRAG) {
                    mapView.translate(x-downPoint.x,y-downPoint.y);
                    downPoint.x=x;
                    downPoint.y=y;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_NONE;
                break;
        }
        return true;
    }


    // 触碰两点间距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取手势中心点
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    // 取旋转角度
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}
