package com.aaa.lib.mapdemo.imp;

import android.content.Context;
import android.util.AttributeSet;

import com.aaa.lib.map.area.RectangleArea;
import com.aaa.lib.map3d.Map3DSurfaceView;
import com.aaa.lib.map3d.model.LineModel;
import com.aaa.lib.map3d.model.Model;
import com.aaa.lib.map3d.model.ObjModel;
import com.aaa.lib.map3d.model.PlaneModel;
import com.aaa.lib.map3d.obj.MultiObj3D;
import com.aaa.lib.map3d.obj.Path3D;
import com.aaa.lib.map3d.obj.Plane3D;

import java.util.ArrayList;
import java.util.List;

public class Room3DSurfaceView extends Map3DSurfaceView {
    private MapModel mapModel;
    private LineModel pathModel;
    private SweeperModel sweeperModel;
    private PowerModel powerModer;
    private List<PlaneModel> areaModelList;

    public Room3DSurfaceView(Context context) {
        super(context);
        init();
    }

    public Room3DSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mapModel = new MapModel();
        addModel(mapModel);
        pathModel = new LineModel();
        addModel(pathModel);
        areaModelList = new ArrayList<>();
    }

    /**
     * 刷新地图
     *
     * @param width      height 地图宽高
     * @param resolution 地图分辨率
     * @param mapData    栅格地图数组
     */
    public void refreshMap(int width, int height, float resolution, int[] mapData) {

        mapModel.setUnit(resolution);

        //处理墙内的柱子, 如果 相邻的柱子少于10个, 就认为是墙内
        MapDataConverter.filterWall(width, height, mapData, 10);

        //计算地图可裁剪区域 用于居中
        mapModel.setClipArea(MapDataConverter.getClipArea(width, height, mapData));

        MultiObj3D obj3D = MapDataConverter.mapDataToObj3D(getContext(), width, height, mapData, mapModel.getUnit(), mapModel.getMapCenterX(), mapModel.getMapCenterY());

        updateModelData(mapModel, obj3D);

        requestRender();
    }

    /**
     * @param path 路径数据 格式: [x1,y1,x2,y2,x3,y3.......]
     */
    public void refreshPath(float[] path, int pathColor) {
        Path3D path3DData = MapDataConverter.convertPathData(path, pathColor, mapModel.getUnit(), mapModel.getMapCenterX(), mapModel.getMapCenterY());
        updateModelData(pathModel, path3DData);
        requestRender();
    }

    public void refreshArea(List<RectangleArea> areaList) {
        removeArea();
        for (int i = 0; i < areaList.size(); i++) {
            Plane3D areaObject = MapDataConverter.areaToObj(getContext(), mapModel.getUnit(), areaList.get(i));
            PlaneModel areaModel = new PlaneModel(areaObject);
            areaModel.setRotate(0, areaList.get(i).rotate, 0);
            addModel(areaModel);
        }
        requestRender();
    }

    public void removeArea() {
        List<PlaneModel> areaModelList = getAreaModel();
        modelManager.getAllModel().removeAll(areaModelList);
    }

    public List<PlaneModel> getAreaModel() {
        List<PlaneModel> areaModels = new ArrayList<>();
        for (int i = 0; i < modelManager.getAllModel().size(); i++) {
            Model model = modelManager.getAllModel().get(i);
            if (model instanceof PlaneModel) {
                areaModels.add((PlaneModel) model);
            }
        }
        return areaModels;
    }

    /**
     * 刷新家具列表
     * 先清空 再加载
     *
     * @param furnitures 家具列表
     */
    public void refreshFurnitures(List<Furniture> furnitures) {
        removeFurniture();
        for (Furniture furniture : furnitures) {
            FurnitureModel furnitureModel = new FurnitureModel(furniture.getData());
            furnitureModel.setId(furniture.getId());
            setModel(furnitureModel, furniture.getX(), furniture.getData().getModelHeigh() / 2, furniture.getY(), furniture.getScale(), furniture.getRotation());
            addModel(furnitureModel);
        }
        requestRender();
    }


    private void removeFurniture() {
        List<FurnitureModel> furnitureModelList = getFurnitureModel();
        modelManager.getAllModel().removeAll(furnitureModelList);
    }

    private List<FurnitureModel> getFurnitureModel() {
        List<FurnitureModel> furnitureModelList = new ArrayList<>();
        for (int i = 0; i < modelManager.getAllModel().size(); i++) {
            Model model = modelManager.getAllModel().get(i);
            if (model instanceof FurnitureModel) {
                furnitureModelList.add((FurnitureModel) model);
            }
        }
        return furnitureModelList;
    }

    /**
     * 刷新扫地机
     */
    public void refreshSweeper(Sweeper sweeper) {
        if (sweeper == null) {
            modelManager.removeModel(sweeperModel);
            requestRender();
            return;
        }

        if (sweeperModel == null) {
            sweeperModel = new SweeperModel(sweeper.getData());
            setModel(sweeperModel, sweeper.getX(), sweeper.getData().getModelHeigh() / 2, sweeper.getY(), sweeper.getScale(), sweeper.getRotation());
            addModel(sweeperModel);
        } else {
            setModel(sweeperModel, sweeper.getX(), sweeper.getData().getModelHeigh() / 2, sweeper.getY(), sweeper.getScale(), sweeper.getRotation());
        }
        requestRender();
    }

    /**
     * 刷新充电座
     */
    public void refreshPower(Power power) {
        if (power == null) {
            modelManager.removeModel(powerModer);
            requestRender();
            return;
        }

        if (powerModer == null) {
            powerModer = new PowerModel(power.getData());
            setModel(powerModer, power.getX(), power.getData().getModelHeigh() / 2, power.getY(), power.getScale(), power.getRotation());
            addModel(powerModer);
        } else {
            setModel(powerModer, power.getX(), power.getData().getModelHeigh() / 2, power.getY(), power.getScale(), power.getRotation());
        }
        requestRender();
    }

    /**
     * 刷新任意家具, 如果当前没添加加创建
     *
     * @param furniture
     */
    public void refreshFurniture(Furniture furniture) {
        if (furniture == null) {
            return;
        }

        ObjModel model = getFurniture(furniture.getId());
        if (model != null) {
            setModel(model, furniture.getX(), furniture.getData().getModelHeigh() / 2, furniture.getY(), furniture.getScale(), furniture.getRotation());
        } else {
            final FurnitureModel newModel = new FurnitureModel(furniture.getData());
            newModel.setId(furniture.getId());
            setModel(newModel, furniture.getX(), furniture.getData().getModelHeigh() / 2, furniture.getY(), furniture.getScale(), furniture.getRotation());
            addModel(newModel);
        }
        requestRender();
    }

    /**
     * 根据id查找家具模型
     *
     * @param id 家具id
     * @return FurnitureModel
     */
    private FurnitureModel getFurniture(int id) {
        List<Model> modelList = modelManager.getAllModel();
        for (int i = 0; i < modelList.size(); i++) {
            Model model = modelList.get(i);
            if (model instanceof FurnitureModel && ((FurnitureModel) model).getId() == id) {
                return ((FurnitureModel) model);
            }
        }
        return null;
    }

    /**
     * @param x        y  平移 xy 对应3d内的xz  3维的y轴偏移需要平移半个模型高度
     * @param scale    如果模型大小单位为m 且与实际大小一致 则 scale==1
     * @param rotation 家具只按y轴旋转
     */
    private void setModel(Model model, float x, float y, float z, float scale, float rotation) {
        model.setScale(scale);
        model.setOffset((x - mapModel.getMapCenterX()) * mapModel.getUnit(),
                y,
                (z - mapModel.getMapCenterY()) * mapModel.getUnit());
        model.setRotate(0, rotation, 0);
    }

}
