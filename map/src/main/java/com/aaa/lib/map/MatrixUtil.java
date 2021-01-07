package com.aaa.lib.map;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class MatrixUtil {
    private static final String TAG = DrawHelper.class.getSimpleName();

    public static final float MIN_ZOOM = 0.25f;
    public static final float MAX_ZOOM = 4.0f;
    private static float[] sMatrixValue = new float[9];
    private static float[] sPoint = new float[2];

    public static synchronized boolean loadMapOffsetAndScale(Bitmap bitmap, MapView mMapView) {
        Log.i(TAG, "loadMapOffsetAndScale start: ");
        if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            return false;
        }

        if (mMapView.getWidth() == 0 || mMapView.getHeight() == 0) {
            return false;
        }
        mMapView.getTransform().reset();

        //计算缩放比例， 选取缩放比小的  保证控件能完全显示图片
        float mBitmapScale = calculateScaleFactor(bitmap.getWidth(), bitmap.getHeight(), mMapView.getWidth(), mMapView.getHeight());
        if (mBitmapScale > MAX_ZOOM) {
            mBitmapScale = MAX_ZOOM;
        }
        if (mBitmapScale < MIN_ZOOM) {
            mBitmapScale = MIN_ZOOM;
        }
        Log.i(TAG, "map scale : " + mBitmapScale);
        mMapView.getTransform().setScale(mBitmapScale, mBitmapScale);

        //图片会放到控件中心 ， 计算偏移量
        float offsetX = mMapView.getWidth() - bitmap.getWidth() * mBitmapScale;
        float offsetY = mMapView.getHeight() - bitmap.getHeight() * mBitmapScale;
        Log.i(TAG, "map offset x  : " + offsetX + " y " + offsetY);

        mMapView.getTransform().postTranslate(offsetX / 2, offsetY / 2);
        return true;
    }

    private static float calculateScaleFactor(int bitmapWidth, int bitmapHeight, int viewWidth, int viewHeight) {
        float scaleWidth = viewWidth / ((float) bitmapWidth);
        float scaleHeight = viewHeight / ((float) bitmapHeight);
        return scaleWidth < scaleHeight ? scaleWidth : scaleHeight;
    }

    /**
     * 将view中心移动到点（x,y）
     */
    public static void mapCenterWithPoint(float x, float y, float width, float height, Matrix matrix) {
        sPoint[0] = x;
        sPoint[1] = y;
        matrix.mapPoints(sPoint);
        float deltaX = width * 0.5f - sPoint[0];
        float deltaY = height * 0.5f - sPoint[1];

        Log.d(TAG, "mapCenterWithPoint deltaX:" + deltaX + ", deltaY:" + deltaY);
        matrix.postTranslate(deltaX, deltaY);

    }

    /**
     * 缩放
     *
     * @param scale   缩放比例
     * @param centerX 缩放中心点x
     * @param centerY 缩放中心点y
     */
    public static synchronized void scale(float scale, float centerX, float centerY, Matrix matrix) {
        float currentZoom = getCurrentZoom(matrix);
        float targetZoom = currentZoom * scale;
        if (targetZoom > MAX_ZOOM) {
            scale = MAX_ZOOM / currentZoom;
        } else if (targetZoom < MIN_ZOOM) {
            scale = MIN_ZOOM / currentZoom;
        }
        matrix.postScale(scale, scale, centerX, centerY);
    }

    /**
     * 基于当前比例 在指定点缩放
     */
    public synchronized void setCurrentZoom(float zoom, float x, float y, Matrix matrix) {
        scale(zoom / getCurrentZoom(matrix), x, y, matrix);
    }

    public static float getCurrentZoom(Matrix matrix) {
        matrix.getValues(sMatrixValue);
        return sMatrixValue[Matrix.MSCALE_X];
    }
}

