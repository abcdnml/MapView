package com.aaa.lib.map.imp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import com.aaa.lib.map.MapView;
import com.aaa.lib.map.MatrixUtil;
import com.aaa.lib.map.imp.layer.YXAreaDivideLineLayer;
import com.aaa.lib.map.imp.layer.YXAreaLayer;
import com.aaa.lib.map.imp.layer.YXCleanAreaLayer;
import com.aaa.lib.map.imp.layer.YXForbiddenAreaLayer;
import com.aaa.lib.map.imp.layer.YXForbiddenLineLayer;
import com.aaa.lib.map.imp.layer.YXForbiddenMopAreaLayer;
import com.aaa.lib.map.imp.layer.YXImageMarkerLayer;
import com.aaa.lib.map.imp.layer.YXPathLayer;
import com.aaa.lib.map.imp.layer.YXPointAroundAreaLayer;
import com.aaa.lib.map.imp.layer.YXPowerLayer;
import com.aaa.lib.map.imp.layer.YXRoomTagLayer;
import com.aaa.lib.map.imp.model.AreaBean;
import com.aaa.lib.map.imp.model.LDAreaBean;
import com.aaa.lib.map.imp.model.LDMapBean;
import com.aaa.lib.map.imp.model.LDPathBean;
import com.aaa.lib.map.layer.BaseLayer;
import com.aaa.lib.map.layer.MapLayer;

import java.util.ArrayList;
import java.util.List;

public class YXMapView extends MapView<YXLayerManager> {
    private static final String TAG = "YXMapView";


    //各种图层
    private MapLayer mapLayer;
    private YXPathLayer pathLayer;
    private YXImageMarkerLayer deviceLayer;
    private YXPowerLayer powerLayer;
    private List<YXForbiddenAreaLayer> forbiddenAreaLayers;
    private List<YXForbiddenLineLayer> forbiddenLineLayers;
    private List<YXForbiddenMopAreaLayer> forbiddenMopAreaLayers;
    private List<YXCleanAreaLayer> cleanAreaLayers;
    private List<YXRoomTagLayer> roomTagLayers;
    private List<YXPointAroundAreaLayer> locationLayers;
    private volatile YXAreaDivideLineLayer areaDivideLineLayer;
    private boolean hasLoadMap = false;

    public YXMapView(Context context) {
        this(context, null);
    }

    public YXMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YXMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLayerManager = new YXLayerManager(this);
        initAllArea();
        Log.i(TAG, "YXMapView init");
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (!hasLoadMap && mapLayer != null && mapLayer.getMapBitmap() != null) {
                hasLoadMap = MatrixUtil.loadMapOffsetAndScale(mapLayer.getMapBitmap(), this);
                refresh();
            }
        }
    }

    public void initLayer(LDMapBean ldMapBean, LDPathBean ldPathBean, LDAreaBean ldAreaBean) {
        refreshMapAndPower(ldMapBean, ldPathBean);
        refreshPathAndDevice(ldMapBean, ldPathBean);
        refreshAreaLayer(ldAreaBean);
    }

    public void refreshMapAndPower(LDMapBean ldMapBean, LDPathBean ldPathBean) {
        //刷新地图
        refreshMapLayer(ldMapBean, ldPathBean);
        //刷新充电桩
        if (ldMapBean.dockerPosX != 0 && ldMapBean.dockerPosY != 0) {
            refreshPowerLayer(YXCoordinateConverter.getScreenPointFromOrigin(ldMapBean.dockerPosX, ldMapBean.dockerPosY), 0);
        }

        refresh();
    }

    public void refreshPathAndDevice(LDMapBean ldMapBean, LDPathBean ldPathBean) {
        //刷新路径
        refreshPathLayer(ldMapBean, ldPathBean);
        Log.i(TAG, "device x " + YXCoordinateConverter.devicePosX + " y " + YXCoordinateConverter.devicePosY);
        //刷新设备图标
        if (YXCoordinateConverter.devicePosX != 0 && YXCoordinateConverter.devicePosY != 0) {
            refreshDeviceLayer(new PointF(YXCoordinateConverter.devicePosX, YXCoordinateConverter.devicePosY), 0);
        }
        refresh();
    }

    float[] matrixValue = new float[9];

    public void translate(float distanceX, float distanceY) {
        //限制滑动范围
        if (mapLayer != null && mapLayer.getMapBitmap() != null) {
            float bitmapWidth = mapLayer.getMapBitmap().getWidth();
            float bitmapHeight = mapLayer.getMapBitmap().getHeight();
            float mapWidth = getWidth();
            float mapHeight = getHeight();
            if (bitmapWidth != 0 && bitmapHeight != 0) {
                mMatrix.getValues(matrixValue);

                if (matrixValue[2] - distanceX < -bitmapWidth * matrixValue[0]) {
                    matrixValue[2] = -bitmapWidth * matrixValue[0];
                } else if (matrixValue[2] - distanceX > mapWidth) {
                    matrixValue[2] = mapWidth;
                } else {
                    matrixValue[2] = matrixValue[2] - distanceX;
                }

                if (matrixValue[5] - distanceY < -bitmapHeight * matrixValue[0]) {
                    matrixValue[5] = -bitmapHeight * matrixValue[0];
                } else if (matrixValue[5] - distanceY > mapHeight) {
                    matrixValue[5] = mapHeight;
                } else {
                    matrixValue[5] = matrixValue[5] - distanceY;
                }

                mMatrix.setValues(matrixValue);
                refresh();
            } else {
                super.translate(distanceX, distanceY);
            }
        } else {
            super.translate(distanceX, distanceY);
        }
    }


    private void refreshMapLayer(LDMapBean ldMapBean, LDPathBean ldPathBean) {
        if (mapLayer == null) {
            //添加地图
            mapLayer = new MapLayer(this);
            mLayerManager.addLayer(mapLayer);
        }
//        Bitmap mapBitmap = Render.renderMap(ldMapBean, ldPathBean);
        if (!hasLoadMap) {
//            hasLoadMap = MatrixUtil.loadMapOffsetAndScale(mapBitmap, this);
        }

//        mapLayer.setMapBitmap(mapBitmap);
    }

    public void refreshPathLayer(LDMapBean ldMapBean, LDPathBean ldPathBean) {
        if (pathLayer == null) {
            //添加路径
            pathLayer = new YXPathLayer(this);
            mLayerManager.addLayer(pathLayer);
        }

//        Bitmap pathBitmap = Render.renderPath(ldMapBean, ldPathBean);
//        pathLayer.setPathBitmap(pathBitmap);
    }

    public void refreshAreaLayer(LDAreaBean ldAreaBean) {
        //移除所有区域图层
        clearAllArea();
        List<YXAreaLayer> newAreaLayerList = new ArrayList<>();
        for (AreaBean areaBean : ldAreaBean.getAreaList()) {
//            YXAreaLayer layer = isLayerExist(areaBean);  判断图层是否存在 如果图层不存在就新建
            if (areaBean.getType() == YXAreaLayer.TYPE_CLEAN_AREA) {
                YXCleanAreaLayer layer = new YXCleanAreaLayer(this);
                layer.initAreaLayer(areaBean);
                cleanAreaLayers.add(layer);
            } else if (areaBean.getType() == YXAreaLayer.TYPE_FORBIDDEN_AREA) {
                YXForbiddenAreaLayer layer = new YXForbiddenAreaLayer(this);
                layer.initAreaLayer(areaBean);
                forbiddenAreaLayers.add(layer);
            } else if (areaBean.getType() == YXAreaLayer.TYPE_FORBIDDEN_LINE) {
                YXForbiddenLineLayer layer = new YXForbiddenLineLayer(this);
                layer.initAreaLayer(areaBean);
                forbiddenLineLayers.add(layer);
            } else if (areaBean.getType() == YXAreaLayer.TYPE_LOCATION) {
                YXPointAroundAreaLayer layer = new YXPointAroundAreaLayer(this);
                layer.initAreaLayer(areaBean);
                locationLayers.add(layer);
            } else if (areaBean.getType() == YXAreaLayer.TYPE_ROOM) {
                YXRoomTagLayer layer = new YXRoomTagLayer(this);
                layer.initAreaLayer(areaBean);
                roomTagLayers.add(layer);
            } else if (areaBean.getType() == YXAreaLayer.TYPE_FORBIDDEN_MOP) {
                YXForbiddenMopAreaLayer layer = new YXForbiddenMopAreaLayer(this);
                layer.initAreaLayer(areaBean);
                forbiddenMopAreaLayers.add(layer);
            } else {
                YXAreaLayer layer = new YXAreaLayer(this);
                layer.initAreaLayer(areaBean);
            }
        }
        newAreaLayerList.addAll(cleanAreaLayers);
        newAreaLayerList.addAll(forbiddenAreaLayers);
        newAreaLayerList.addAll(forbiddenLineLayers);
        newAreaLayerList.addAll(forbiddenMopAreaLayers);
        newAreaLayerList.addAll(locationLayers);
        newAreaLayerList.addAll(roomTagLayers);

        mLayerManager.setAreaLayer(newAreaLayerList);
        refresh();
    }


    private void refreshDeviceLayer(PointF position, float direction) {
        refreshDeviceLayer(position.x, position.y, direction);
    }

    private void refreshDeviceLayer(float centerX, float centerY, float direction) {
        if (deviceLayer == null) {
            //添加扫地机
            Bitmap sweeperBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.robot_inmap);
            deviceLayer = new YXImageMarkerLayer(this, sweeperBitmap);
            mLayerManager.addLayer(deviceLayer);
        }
        deviceLayer.setMarker(centerX, centerY, direction);
    }

    /**
     * 刷新电源
     *
     * @param position  电源位置
     * @param direction 电源朝向
     */
    private void refreshPowerLayer(PointF position, float direction) {
        refreshPowerLayer(position.x, position.y, direction);
    }

    private void refreshPowerLayer(float centerX, float centerY, float rotation) {
        Log.i(TAG, "refreshPowerLayer : " + centerX + " , " + centerY);
        if (powerLayer == null) {
            //添加电源图层
            powerLayer = new YXPowerLayer(this);
            powerLayer.setRadius(0.5f);
            mLayerManager.addLayer(powerLayer);
        }
        powerLayer.setCenter(centerX, centerY);
        powerLayer.setRotation(rotation);
    }

    /**
     * 添加区域分割线
     * 限制只能添加一个
     */
    public void addAreaDivideLineLayer() {
        if (areaDivideLineLayer == null) {
            areaDivideLineLayer = new YXAreaDivideLineLayer(this);
            areaDivideLineLayer.setLine(new PointF(200, getHeight() / 2), new PointF(getWidth() / 2, getHeight() / 2));
            mLayerManager.addLayer(areaDivideLineLayer);
            refresh();
        }
    }

    /**
     * 移除区域分割线
     */
    public void removeAreaDivideLineLayer() {
        mLayerManager.removeLayer(areaDivideLineLayer);
        areaDivideLineLayer = null;
        refresh();
    }

    /**
     * 设置房间标签是否可见
     *
     * @param visible 可见性
     */
    public void setRoomTagMode(boolean visible, boolean canSelect, boolean showOrder) {
        YXRoomTagLayer.setMode(visible, canSelect, showOrder);
        refresh();
    }

    /**
     * 设置禁区是否可编辑
     *
     * @param editable 可编辑
     */
    public void setForbiddenAreaEditable(boolean editable) {
        for (YXAreaLayer areaLayer : forbiddenAreaLayers) {
            if (areaLayer instanceof YXForbiddenAreaLayer) {
                areaLayer.setEditMode(editable);
            }
        }
        refresh();
    }

    /**
     * 设置禁过线是否可编辑
     *
     * @param editable 可编辑
     */
    public void setForbiddenLineEditable(boolean editable) {
        for (YXAreaLayer areaLayer : forbiddenAreaLayers) {
            if (areaLayer instanceof YXForbiddenLineLayer) {
                areaLayer.setEditMode(editable);
            }
        }
        refresh();
    }

    /**
     * 设置自定义区域清扫是否可编辑
     *
     * @param editable 可编辑
     */
    public void setCleanAreaEditable(boolean editable) {
        for (BaseLayer areaLayer : cleanAreaLayers) {
            ((YXCleanAreaLayer) areaLayer).setEditMode(editable);
        }
        refresh();
    }

    /**
     * 是否显示电源保护区域
     *
     * @param show 显示/不显示
     */
    public void showPowerProtectArea(boolean show) {
        if (powerLayer == null) {
            //添加电源图层
            powerLayer = new YXPowerLayer(this);
            powerLayer.setRadius(0.5f);
            mLayerManager.addLayer(powerLayer);
            powerLayer.setShowProtectArea(show);
            refresh();
        }
    }


    private void initAllArea() {
        forbiddenAreaLayers = new ArrayList<>();
        forbiddenLineLayers = new ArrayList<>();
        forbiddenMopAreaLayers = new ArrayList<>();
        cleanAreaLayers = new ArrayList<>();
        locationLayers = new ArrayList<>();
        roomTagLayers = new ArrayList<>();
    }

    private void clearAllArea() {
        cleanAreaLayers.clear();
        forbiddenAreaLayers.clear();
        forbiddenLineLayers.clear();
        forbiddenMopAreaLayers.clear();
        roomTagLayers.clear();
        locationLayers.clear();
    }

    public List getAreaLayerListByType(Class cls) {
        if (cls == YXForbiddenAreaLayer.class) {
            return forbiddenAreaLayers;
        } else if (cls == YXForbiddenLineLayer.class) {
            return forbiddenLineLayers;
        } else if (cls == YXForbiddenMopAreaLayer.class) {
            return forbiddenMopAreaLayers;
        } else if (cls == YXPointAroundAreaLayer.class) {
            return locationLayers;
        } else if (cls == YXRoomTagLayer.class) {
            return roomTagLayers;
        } else {
            return null;
        }
    }

    public void addAreaLayer(YXAreaLayer layer) {
        if (layer instanceof YXForbiddenMopAreaLayer) {
            forbiddenMopAreaLayers.add((YXForbiddenMopAreaLayer) layer);
        } else if (layer instanceof YXForbiddenAreaLayer) {
            forbiddenAreaLayers.add((YXForbiddenAreaLayer) layer);
        } else if (layer instanceof YXForbiddenLineLayer) {
            forbiddenLineLayers.add((YXForbiddenLineLayer) layer);
        } else if (layer instanceof YXPointAroundAreaLayer) {
            locationLayers.add((YXPointAroundAreaLayer) layer);
        } else if (layer instanceof YXRoomTagLayer) {
            roomTagLayers.add((YXRoomTagLayer) layer);
        } else if (layer instanceof YXCleanAreaLayer) {
            cleanAreaLayers.add((YXCleanAreaLayer) layer);
        }
        mLayerManager.addLayer(layer);
        refresh();
    }


    public int getUniqueAreaId(int type) {
        int startID = 0;
        if (type == YXAreaLayer.TYPE_CLEAN_AREA) {
            startID = 200;
            return loopToGetUniqueAreaId(cleanAreaLayers, startID);
        } else if (type == YXAreaLayer.TYPE_FORBIDDEN_AREA) {
            startID = 300;
            return loopToGetUniqueAreaId(forbiddenAreaLayers, startID);
        } else if (type == YXAreaLayer.TYPE_FORBIDDEN_LINE) {
            startID = 400;
            return loopToGetUniqueAreaId(forbiddenLineLayers, startID);
        }
        return 0;
    }

    private int loopToGetUniqueAreaId(List<? extends YXAreaLayer> layers, int startId) {
        while (isIdExist(layers, startId)) {
            startId++;
        }
        return startId;
    }

    private boolean isIdExist(List<? extends YXAreaLayer> layers, int id) {
        for (YXAreaLayer areaLayer : layers) {
            if (id == areaLayer.getAreaInfo().getAreaID()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPowerCrossForbidArea() {
        return mLayerManager.isPowerCrossForbidArea(powerLayer);
    }

    public YXPowerLayer getPowerLayer() {
        return powerLayer;
    }

    public List<YXForbiddenAreaLayer> getForbiddenAreaLayers() {
        return forbiddenAreaLayers;
    }

    public List<YXForbiddenLineLayer> getForbiddenLineLayers() {
        return forbiddenLineLayers;
    }

    public List<YXForbiddenMopAreaLayer> getForbiddenMopAreaLayers() {
        return forbiddenMopAreaLayers;
    }

    public List<YXCleanAreaLayer> getCleanAreaLayers() {
        return cleanAreaLayers;
    }

    public List<YXRoomTagLayer> getRoomTagLayers() {
        return roomTagLayers;
    }

    public YXAreaDivideLineLayer getAreaDivideLineLayer() {
        return areaDivideLineLayer;
    }

    public List<YXRoomTagLayer> getSelectedRoomTag() {
        List<YXRoomTagLayer> selectedRoom = new ArrayList<>();
        for (YXRoomTagLayer layer : roomTagLayers) {
            if (layer.isSelected()) {
                selectedRoom.add(layer);
            }
        }
        return selectedRoom;
    }

    @Override
    public void clearMap() {
        super.clearMap();
        clearAllArea();
    }
}
