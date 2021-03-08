package com.aaa.lib.map;

import android.view.MotionEvent;

import com.aaa.lib.map.layer.BaseLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LayerManager {

    protected MapView mMapView;
    protected List<BaseLayer> mLayerList;
    private BaseLayer mLastInterceptLayer;

    private LayerListChangeListener layerListChangeListener;

    public LayerManager(MapView mapView) {
        mMapView = mapView;
        mLayerList = new ArrayList<>();
    }


    public LayerListChangeListener getLayerListChangeListener() {
        return layerListChangeListener;
    }

    public void setLayerListChangeListener(LayerListChangeListener layerListChangeListener) {
        this.layerListChangeListener = layerListChangeListener;
    }


    /**
     * 图层排序器
     */
    private Comparator<BaseLayer> mComparator = new Comparator<BaseLayer>() {
        @Override
        public int compare(BaseLayer layer1, BaseLayer layer2) {
            if (layer1.getLayerLevel() < layer2.getLayerLevel()) {
                return -1;
            } else if (layer1.getLayerLevel() > layer2.getLayerLevel()) {
                return 1;
            } else {
                return 0;
            }
        }
    };


    /**
     * 添加单个图层
     *
     * @param layer 图层
     */
    public synchronized void addLayer(BaseLayer layer) {
        if (layer == null) {
            return;
        }
        if (mLayerList.contains(layer)) {
            return;
        }
        mLayerList.add(layer);
        Collections.sort(mLayerList, mComparator);

        if (layerListChangeListener != null) {
            layerListChangeListener.onLayerAdd(layer);
        }
    }

    /**
     * 添加多个图层
     *
     * @param layers 图层
     */
    public synchronized void addLayers(List<BaseLayer> layers) {
        if (layers == null) {
            return;
        }
        for (BaseLayer layer : layers) {
            if (!mLayerList.contains(layer)) {
                mLayerList.add(layer);
                if (layerListChangeListener != null) {
                    layerListChangeListener.onLayerAdd(layer);
                }

            }
        }

        Collections.sort(mLayerList, mComparator);
    }

    /**
     * 移除单个图层
     *
     * @param layer 图层
     */
    public synchronized void removeLayer(BaseLayer layer) {
        if (mLayerList.contains(layer)) {
            mLayerList.remove(layer);

            if (layerListChangeListener != null) {
                layerListChangeListener.onLayerRemove(layer);
            }
        }
    }

    /**
     * 移除某个类型的图层
     *
     * @param cls 类型
     */
    public synchronized void removeLayersByType(Class cls) {
        List<BaseLayer> tmpLayerList = new ArrayList<>();
        for (BaseLayer layer : mLayerList) {
            if (layer.getClass().equals(cls)) {
                tmpLayerList.add(layer);
                if (layerListChangeListener != null) {
                    layerListChangeListener.onLayerRemove(layer);
                }
            }
        }
        mLayerList.removeAll(tmpLayerList);
        //默认莫一种类型的type level都是一样的 所以不进行排序
    }

    /**
     * 图层事件分发控制
     * 记录处理DOWN事件的图层 ， 之后的move和UP事件也发给此图层
     *
     * @param event
     * @return
     */
    public boolean dispatchToLayers(MotionEvent event) {
        if (mLayerList == null) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = mLayerList.size() - 1; i > -1; i--) {
                BaseLayer layer = mLayerList.get(i);
                boolean handleDown = layer.onTouch(event);
                if (handleDown) {
                    mLastInterceptLayer = layer;
                    return true;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mLastInterceptLayer != null) {
                mLastInterceptLayer.onTouch(event);
                mLastInterceptLayer = null;
                return true;
            }
        } else if (mLastInterceptLayer != null) {
            mLastInterceptLayer.onTouch(event);
            return true;
        }
        return false;
    }

    public interface LayerListChangeListener {
        void onLayerAdd(BaseLayer layer);

        void onLayerRemove(BaseLayer layer);
    }


    public void clearLayer() {
        mLayerList.clear();
    }


}
