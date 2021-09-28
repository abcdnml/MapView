package com.aaa.lib.map.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.aaa.lib.map.MapView;

public class MapLayer extends BaseLayer {
    private Bitmap mMapBitmap;
    private int mMapWidth;
    private int mMapHeight;

    public MapLayer(MapView mapView) {
        this(mapView, null);
    }

    public MapLayer(MapView mapView, Bitmap bitmap) {
        super(mapView, LEVEL_MAP);
        this.mMapView = mapView;
        this.mMapBitmap = bitmap;
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mMapBitmap == null || mMapBitmap.isRecycled()) {
            return;
        }
        canvas.drawBitmap(mMapBitmap, mMapView.getTransform(), null);
    }

    @Override
    public void release() {
        if (mMapBitmap != null && !mMapBitmap.isRecycled()) {
            mMapBitmap.recycle();
        }
    }

    public Bitmap getMapBitmap() {
        return mMapBitmap;
    }

    public void setMapBitmap(Bitmap mapBitmap) {
        this.mMapBitmap = mapBitmap;
    }
}
