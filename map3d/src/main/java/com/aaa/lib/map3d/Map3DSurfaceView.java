package com.aaa.lib.map3d;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.aaa.lib.map3d.model.ObjModel;
import com.aaa.lib.map3d.model.PathModel;
import com.aaa.lib.map3d.obj.Obj3D;
import com.aaa.lib.map3d.obj.ObjReader;
import com.aaa.lib.map3d.obj.Path3D;

import java.util.List;

public class Map3DSurfaceView extends GLSurfaceView {

    private Map3DRender renderer;
    private TouchHandler touchHandler;

    private float unit = 0.1f; // 每个像素单元所占大小

    private int pathColor = Color.GREEN;
    private ObjModel mapModel;
    private PathModel pathModel;
    private int[] clipArea = new int[4]; //裁剪区域  左上右下

    public Map3DSurfaceView(Context context) {
        super(context);
        init();
    }

    public Map3DSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(3);
        renderer = new Map3DRender(this, Color.argb(1, 33, 162, 254));
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
//        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


        touchHandler = new TouchHandler(this);

        mapModel = new ObjModel(getContext());
        renderer.addModel(mapModel);
        pathModel = new PathModel(getContext());
        renderer.addModel(pathModel);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event);
    }


    public void refreshMap(int width, int height, float unit, int[] mapData) {
        this.unit = unit;
        //处理墙内的柱子, 如果 相邻的柱子少于30个, 就认为是墙内
        MapDataConverter.filterWall(width, height, mapData, 30);

        //计算地图可裁剪区域 用于居中
        MapDataConverter.getClipArea(clipArea, width, height, mapData);
        int offsetX=(clipArea[0] + clipArea[2]) / 2;
        int offsetY=(clipArea[1] + clipArea[3]) / 2;
        final List<Obj3D> obj3D = MapDataConverter.mapDataToObj3D(width, height, mapData, unit,offsetX,offsetY);

        queueEvent(new Runnable() {
            @Override
            public void run() {
                mapModel.setObj3D(obj3D);
                requestRender();
            }
        });


        //TODO 地图更新 需要同事更新路径 和家具
//        requestRender();
    }

    /**
     * @param width    height 地图宽高
     * @param pathData 路径数据 格式: [x1,y1,x2,y2,x3,y3.......]
     */
    public void refreshPath(float[] pathData) {
        int offsetX=(clipArea[0] + clipArea[2]) / 2;
        int offsetY=(clipArea[1] + clipArea[3]) / 2;
        final Path3D path3D = MapDataConverter.convertPathData(pathData, unit, pathColor,offsetX,offsetY);

        queueEvent(new Runnable() {
            @Override
            public void run() {
                pathModel.setPath3D(path3D);
                requestRender();
            }
        });
    }

    /**
     * @param objPath obj文件路径 如果以assets/ 开头 则从assets文件夹下读取  否则 直接读
     * @param offsetX offsetY 平面上的平移 只有xy , 对应3d内的xz  TODO y轴偏移需要平移半个模型高度
     * @param scale   如果模型大小单位为cm 且与实际大小一致 则 scale==0.01
     * @param rotate  家具只按y轴旋转
     */
    public ObjModel add3DModel(String objPath, float offsetX, float offsetY, float scale, float rotate) {
        int mapOffsetX=(clipArea[0] + clipArea[2]) / 2;
        int mapOffsetY=(clipArea[1] + clipArea[3]) / 2;

        List<Obj3D> multiObj2 = ObjReader.readMultiObj(getContext(), objPath);
        ObjModel objModel = new ObjModel(getContext(), multiObj2);
        objModel.setOffset((offsetX -mapOffsetX) * unit, 1f * unit, (offsetY-mapOffsetY) * unit );
        Log.i("add3DModel : ","offset x: "+(offsetX -(clipArea[0]+clipArea[2])/2) * unit +"  offset Y: "+(offsetY-(clipArea[1]+clipArea[3])/2) * unit);
        objModel.setRotate(0, rotate, 0);
        objModel.setScale(scale);
        renderer.addModel(objModel);
        requestRender();
        return objModel;
    }

    /**
     * 设置路径颜色
     *
     * @param pathColor 路径颜色
     */
    public void setPathColor(int pathColor) {
        this.pathColor = pathColor;
        requestRender();
    }

    /**
     * 设置背景颜色
     *
     * @param color 背景颜色
     */
    public void setBgColor(int color) {
        renderer.setBgColor(color);
        requestRender();
    }

    //移除指定模型
    public void remove() {
//        renderer.remove();
    }

    public Map3DRender getRenderer() {
        return renderer;
    }

}
