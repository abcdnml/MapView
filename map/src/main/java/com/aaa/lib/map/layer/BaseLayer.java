package com.aaa.lib.map.layer;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.aaa.lib.map.MapView;


public abstract class BaseLayer {
    public static final int LEVEL_DEFAULT = 3;
    public static final int LEVEL_MAP = 0;
    public static final int LEVEL_PATH = 1;
    public static final int LEVEL_AREA = 2;
    public static final int LEVEL_MARKER = 3;

    protected int mLayerLevel;
    protected MapView mMapView;
    protected boolean visible;
    protected Resources mResource;

    public BaseLayer(MapView mapView, int level) {
        this(mapView,level,true);
    }
    public BaseLayer(MapView mapView, int level,boolean visible) {
        this.mMapView = mapView;
        this.mLayerLevel = level;
        this.mResource=mMapView.getResources();
        this.visible=visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getLayerLevel() {
        return mLayerLevel;
    }

    public void setLayerLevel(int mLayerLevel) {
        this.mLayerLevel = mLayerLevel;
    }

    public abstract boolean onTouch(MotionEvent event);

    public abstract void draw(Canvas canvas);

    public abstract void release();
}
