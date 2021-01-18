package com.aaa.lib.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MapView<T extends LayerManager> extends View {

    private static final String TAG = MapView.class.getSimpleName();

    protected float max_zoom = 2f;
    protected float min_zoom = 0.5f;

    private TouchHandler mTouchHandler;
    protected Matrix mMatrix;
    protected float[] mMatrixValue;
    protected T mLayerManager;
    private boolean canTouch = true;
    private int bgColor;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMatrix = new Matrix();
        mMatrixValue = new float[9];
        bgColor = Color.WHITE;
        mTouchHandler = new TouchHandler(this.getContext(), this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(bgColor);
        if (mLayerManager.mLayerList != null) {
            Log.i(TAG, "draw layer size :" + mLayerManager.mLayerList.size());
            for (int i = 0; i < mLayerManager.mLayerList.size(); i++) {
                Log.i(TAG, "draw layer :" + mLayerManager.mLayerList.get(i).getClass().getSimpleName());
                mLayerManager.mLayerList.get(i).draw(canvas);
            }
        }

    }

    public void refresh() {
        postInvalidate();
    }

    /**
     * 缩放
     *
     * @param scale   缩放比例
     * @param centerX 缩放中心点x
     * @param centerY 缩放中心点y
     */
    public void scale(float scale, float centerX, float centerY) {
        float currentZoom = getCurrentZoom();
        float targetZoom = currentZoom * scale;
        if (targetZoom > max_zoom) {
            scale = max_zoom / currentZoom;
        } else if (targetZoom < min_zoom) {
            scale = min_zoom / currentZoom;
        }
        mMatrix.postScale(scale, scale, centerX, centerY);
        postInvalidate();
    }

    private float getCurrentZoom() {
        mMatrix.getValues(mMatrixValue);
        return mMatrixValue[Matrix.MSCALE_X];
    }

    public void translate(float x, float y) {
        mMatrix.postTranslate(x, y);
        postInvalidate();
    }

    public void setBackgroundColor(int color) {
        bgColor = color;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "surface onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.i(TAG, "surface onlayout");
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * 处理触摸事件
     * 先根据layer层级，交由各个layer图层处理
     * 如果layer未处理 则交由自己做缩放平移
     *
     * @param event MotionEvent
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //判断是否可以操作
        if (!canTouch) {
            return true;
        }

        //判断是子图层是否处理事件
        if (mLayerManager.dispatchToLayers(event)) {
            return true;
        }
        //子图层未处理，自己处理 做平移缩放操作
        return mTouchHandler.onTouchEvent(event);
    }


    public void setCanTouch(boolean canTouch) {
        this.canTouch = canTouch;
    }

    public Matrix getTransform() {
        return mMatrix;
    }

    public void setZoomLimit(float min, float max) {
        this.min_zoom = min;
        this.max_zoom = max;
    }


    public T getLayerManager() {
        return mLayerManager;
    }


    public void clearMap() {
        mLayerManager.clearLayer();
    }
}
