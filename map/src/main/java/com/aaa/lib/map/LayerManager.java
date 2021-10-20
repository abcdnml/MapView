package com.aaa.lib.map;

import android.view.MotionEvent;

import com.aaa.lib.map.layer.BaseLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LayerManager {

    protected MapView mMapView;
    protected List<BaseLayer> mLayerList;
    protected BaseLayer mLastInterceptLayer;

    protected LayerListChangeListener layerListChangeListener;
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
     * 获取指定类型的图层
     *
     * @param cls class
     * @return List<BaseLayer>
     */
    public List<BaseLayer> getLayersByType(Class... cls) {
        List<BaseLayer> layers = new ArrayList<>();
        for (BaseLayer layer : mLayerList) {
            for(int i=0; i<cls.length;i++){
                if (layer.getClass() == cls[i]) {
                    layers.add(layer);
                    break;
                }
            }
        }
        return layers;
    }


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
        if (mLayerList.remove(layer) && layerListChangeListener != null) {
            layerListChangeListener.onLayerRemove(layer);
        }
    }

    public synchronized void removeLayers(List<BaseLayer> layers) {
        mLayerList.removeAll(layers);
        for (BaseLayer layer : layers) {
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
    public synchronized void removeLayersByType(Class... cls) {
        List<BaseLayer> tmpLayerList = new ArrayList<>();
        Iterator<BaseLayer> it = mLayerList.iterator();
        while (it.hasNext()) {
            BaseLayer layer = it.next();
            for (int j = 0; j < cls.length; j++) {
                if (layer.getClass() == cls[j]) {
                    tmpLayerList.add(layer);
                    break;
                }
            }
        }
        removeLayers(tmpLayerList);
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

    public void clearLayer() {
        mLayerList.clear();
    }


    public interface LayerListChangeListener {
        void onLayerAdd(BaseLayer layer);

        void onLayerRemove(BaseLayer layer);
    }


}
