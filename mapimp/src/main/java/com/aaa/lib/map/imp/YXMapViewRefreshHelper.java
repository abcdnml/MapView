package com.aaa.lib.map.imp;

import android.graphics.Bitmap;
import android.util.Log;

import com.aaa.lib.map.MatrixUtil;
import com.aaa.lib.map.imp.model.LDAreaBean;
import com.aaa.lib.map.imp.model.LDMapBean;
import com.aaa.lib.map.imp.model.LDPathBean;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 弃用
 */
public class YXMapViewRefreshHelper {

    private static final String TAG = "YXMapViewRefreshHelper";


    //线程和锁
    private volatile boolean refreshMap = false;
    private ThreadPoolExecutor threadPoolExecutor;
    private volatile Lock refreshMapLock;
    private volatile Condition mapDataUpdate;
    private volatile Lock refreshPathLock;
    private volatile Condition pathDataUpdate;
    private volatile Lock refreshAreaLock;
    private volatile Condition areaInfoUpdate;
    private volatile boolean drawMap = false;
    private volatile boolean drawPath = false;
    private volatile boolean drawArea = false;

    private YXMapView mapView;
    private volatile LDMapBean ldMapBean;
    private volatile LDPathBean ldPathBean;
    private volatile LDAreaBean ldAreaBean;

    public YXMapViewRefreshHelper(YXMapView mapView) {
        this.mapView = mapView;
        //初始化线程和锁
        threadPoolExecutor = new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(50));
        refreshMapLock = new ReentrantLock();
        mapDataUpdate = refreshMapLock.newCondition();
        refreshPathLock = new ReentrantLock();
        pathDataUpdate = refreshPathLock.newCondition();
        refreshAreaLock = new ReentrantLock();
        areaInfoUpdate = refreshAreaLock.newCondition();
    }

    public void updateMap(LDMapBean mapBean, LDPathBean pathBean) {
        ldMapBean = mapBean;
        ldPathBean = pathBean;
        updateMap();
    }

    public void updateMap() {
        //解析地图数据
        refreshMapLock.lock();
        drawMap = true;
        mapDataUpdate.signal();
        refreshMapLock.unlock();
    }

    public void updatePath(LDMapBean mapBean, LDPathBean pathBean) {
        ldMapBean = mapBean;
        ldPathBean = pathBean;
        updatePath();
    }

    public void updatePath() {
        refreshPathLock.lock();
        drawPath = true;
        pathDataUpdate.signal();
        refreshPathLock.unlock();
    }

    public void updateArea(LDAreaBean areaBean) {
        ldAreaBean = areaBean;
        updateArea();
    }

    public void updateArea() {
        refreshAreaLock.lock();
        //解析路径数据
        drawArea = true;
        areaInfoUpdate.signal();
        refreshAreaLock.unlock();
    }


    public void open() {
        refreshMap = true;
        // 绘制地图线程
        threadPoolExecutor.execute(runnableMap);
        //绘制路径线程
        threadPoolExecutor.execute(runnablePath);
        //绘制区域信息线程
        threadPoolExecutor.execute(runnableArea);
    }

    public void close() {
        refreshMap = false;
        updateMap();
        updatePath();
        updateArea();
    }

    /**
     * 绘制地图线程
     */
    private Runnable runnableMap = new Runnable() {
        @Override
        public void run() {
            //TODO 这里无法停止 使用handlerThread优化
            while (refreshMap) {
                Log.i(TAG, "update map  " + drawMap);
                refreshMapLock.lock();
                if (drawMap) {
                    //刷新地图
//                    Bitmap mapBitmap = Render.renderMap(ldMapBean, ldPathBean);
//                    MatrixUtil.loadMapOffsetAndScale(mapBitmap, mapView);
//                    mapView.refreshMapAndPower(mapBitmap);

                    //刷新充电桩
                    if (ldMapBean.dockerPosX != 0 && ldMapBean.dockerPosY != 0) {
//                        mapView.refreshPowerLayer(YXCoordinateConverter.getScreenPointFromOrigin(ldMapBean.dockerPosX, ldMapBean.dockerPosY), 0);
                    }

                    updatePath(ldMapBean, ldPathBean);
                    drawMap = false;
                } else {
                    try {
                        mapDataUpdate.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                refreshMapLock.unlock();
            }
        }
    };


    /**
     * 绘制路径线程
     */
    private Runnable runnablePath = new Runnable() {

        @Override
        public void run() {
            while (refreshMap) {
                refreshPathLock.lock();
                Log.i(TAG, "update path " + drawPath);
                if (drawPath) {
                    //刷新路径
//                    Bitmap pathBitmap = Render.renderPath(ldMapBean, ldPathBean);
//                    mapView.refreshPathLayer(pathBitmap);

                    //刷新当前位置
                    if (YXCoordinateConverter.devicePosX != 0 && YXCoordinateConverter.devicePosY != 0) {
//                        mapView.refreshDeviceLayer(new PointF(YXCoordinateConverter.devicePosX, YXCoordinateConverter.devicePosY), 0);
                    }

                    drawPath = false;
                } else {
                    try {
                        pathDataUpdate.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                refreshPathLock.unlock();
            }
        }
    };

    /**
     * 绘制路径线程
     */
    private Runnable runnableArea = new Runnable() {

        @Override
        public void run() {
            while (refreshMap) {
                refreshAreaLock.lock();
                Log.i(TAG, "updateArea " + drawArea);
                if (drawArea) {
                    Log.i(TAG, "updateArea 11111111111 ");
//                    mapView.refreshAreaLayer(ldAreaBean);
                    drawArea = false;
                } else {
                    try {
                        Log.i(TAG, "updateArea waite ");
                        areaInfoUpdate.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                refreshAreaLock.unlock();
            }
        }
    };

}
