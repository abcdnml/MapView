package com.aaa.lib.map3d.imp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;

import com.aaa.lib.map3d.R;
import com.aaa.lib.map3d.area.RectangleArea;
import com.aaa.lib.map3d.obj.Area3D;
import com.aaa.lib.map3d.obj.MtlInfo;
import com.aaa.lib.map3d.obj.MultiObj3D;
import com.aaa.lib.map3d.obj.Obj3D;
import com.aaa.lib.map3d.obj.Path3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapDataConverter {
    //每块地板立方体顶点坐标  需要根据位置来计算
    private static final float[] currentCubeVertex = new float[24];
    //法线坐标
    private static final float[] originCubeNormal = new float[]{
            0, 1, 0,//上
            0, -1, 0,//下
            -1, 0, 0,//左
            1, 0, 0,//右
            0, 0, 1,//前
            0, 0, -1//后
    };
    private static final float[] originCubeTextureCoordinate = new float[]{
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };
    //绘制一个立方体的36个顶点的索引
    private static final int[] vertexIndex = new int[]{
            3, 1, 0, 3, 2, 1,//正面两个三角形
            6, 4, 5, 6, 7, 4,//背面
            7, 0, 4, 7, 3, 0,//左侧
            2, 5, 1, 2, 6, 5, //右侧
            0, 5, 4, 0, 1, 5, //上
            7, 2, 3, 7, 6, 2
    };
    //立方体的36个顶点法向量的索引
    private static final int[] normalIndex = new int[]{
            4, 4, 4, 4, 4, 4,//正面两个三角形
            5, 5, 5, 5, 5, 5,//背面
            2, 2, 2, 2, 2, 2,//左侧
            3, 3, 3, 3, 3, 3, //右侧
            0, 0, 0, 0, 0, 0,//上
            1, 1, 1, 1, 1, 1//下
    };
    //立方体的36个顶点纹理的索引
    private static final int[] textureIndex = new int[]{
            1, 3, 0, 1, 2, 3,//正面两个三角形
            1, 3, 0, 1, 2, 3,//背面
            1, 3, 0, 1, 2, 3,//左侧
            1, 3, 0, 1, 2, 3, //右侧
            1, 3, 0, 1, 2, 3, //上
            1, 3, 0, 1, 2, 3//下
    };
    //原始立方体顶点坐标
    static float[] originCubeVertex = new float[]{
            -0.5f, 0.5f, 0.5f,//前 左 上
            0.5f, 0.5f, 0.5f,//前 右 上
            0.5f, -0.5f, 0.5f,//前 右 下
            -0.5f, -0.5f, 0.5f,//前 左 下
            -0.5f, 0.5f, -0.5f,//后 左 上
            0.5f, 0.5f, -0.5f,//后 右 上
            0.5f, -0.5f, -0.5f,//后 右 下
            -0.5f, -0.5f, -0.5f,//后 左 下
    };
    //纹理坐标
    //纹理坐标
    static float[] currentCubeTexture = new float[8];
    static float[] currentFloorDownTexture = new float[8];
    static float[] currentWallBackTexture = new float[8];
    static float[] currentWallFrontTexture = new float[8];
    static float[] currentWallLeftTexture = new float[8];
    static float[] currentWallRightTexture = new float[8];

    /**
     * 计算地图实际探测到的边界 用于居中
     */
    public static int[] getClipArea(int width, int height, int[] mapData) {
        int[] clipArea = new int[4];
        int minX = width;
        int minY = height;
        int maxX = 0;
        int maxY = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int type = mapData[i * width + j];
                if (type < 2) {
                    if (minX > j) {
                        minX = j;
                    }
                    if (minY > i) {
                        minY = i;
                    }
                    if (maxX < j) {
                        maxX = j;
                    }
                    if (maxY < i) {
                        maxY = i;
                    }
                }
            }
        }

        /**
         * 如果有数据 设置可裁剪的范围
         * 如果没有数据 设置为整个地图大小
         */
        if (minX <= maxX && minY <= maxY) {
            clipArea[0] = minX;
            clipArea[1] = minY;
            clipArea[2] = maxX;
            clipArea[3] = maxY;
        } else {
            clipArea[0] = 0;
            clipArea[1] = 0;
            clipArea[2] = width;
            clipArea[3] = height;
        }
        Log.i("getClipArea", Arrays.toString(clipArea));

        return clipArea;
    }

    /**
     * 过滤墙内的柱子
     *
     * @param width        长
     * @param height       宽
     * @param mapData      数据
     * @param minWallCount 最小相邻墙的数目 ,  小于这个值就当做地板
     */
    public static void filterWall(int width, int height, int[] mapData, int minWallCount) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (mapData[i * width + j] == 1) {
                    list.clear();
                    int count = getNeighborWall(mapData, width, height, i, j, list);
                    if (count > minWallCount) {
                        for (int index : list) {
                            //相邻数超过minWallCount时  就认为是墙 置为101
                            mapData[index] = 101;
                        }
                    }
                }
            }
        }

        //将100 和 101 转换成 0(地板)  和 1(墙)
        for (int i = 0; i < mapData.length; i++) {
            mapData[i] = mapData[i] % 100;
        }
    }

    /**
     * 计算相邻的墙的数量
     *
     * @param mapData
     * @param w       地图宽
     * @param h       地图高
     * @param i       坐标y
     * @param j       坐标x
     * @param path    已经遍历过得墙的列表
     * @return 相邻墙的数量
     */
    private static int getNeighborWall(int[] mapData, int w, int h, int i, int j, List<Integer> path) {
        if (mapData[i * w + j] != 1) {
            return 0;
        }
        path.add(i * w + j);
        //已经统计过得格子 设置为100   后面 如果计算出相邻数量大于某个值 就设置成101 ,  再转化成 0 和 1
        mapData[i * w + j] = 100;
        int increase = 1;

        //前面有格子
        if (i + 1 < h) {
            increase += getNeighborWall(mapData, w, h, i + 1, j, path);
        }

        //后面有格子
        if (i - 1 >= 0) {
            increase += getNeighborWall(mapData, w, h, i - 1, j, path);
        }

        //左面有格子
        if (j - 1 >= 0) {
            increase += getNeighborWall(mapData, w, h, i, j - 1, path);
        }

        //右面有格子
        if (j + 1 < w) {
            increase += getNeighborWall(mapData, w, h, i, j + 1, path);
        }

        return increase;
    }


    //数据转换成obj格式
    public static MultiObj3D mapDataToObj3D(Context context, int width, int height, int[] data, float unit, int offsetX, int offsetY) {
        int floorFaceCount = 0;
        int wallFaceCount = 0;
        SparseArray<boolean[]> floorFaces = new SparseArray<>();
        SparseArray<boolean[]> wallFaces = new SparseArray<>();

        /**
         * 这里提前计算面相邻 是因为需要提前算要绘制的面的个数
         */
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int type = data[i * width + j];
                //计算立方体6个面有无相邻
                if (type == 0) {
                    //0 墙内
                    boolean[] adjucent = new boolean[]{false, false, false, false, false, false};
                    int faceCount = isAdjucency(data, width, height, i, j, adjucent);
                    floorFaceCount += faceCount;
                    floorFaces.put(i * width + j, adjucent);
                } else if (type == 1) {
                    //1 墙
                    boolean[] adjucent = new boolean[]{false, false, false, false, false, false};
                    int faceCount = isWallAdjucency(data, width, height, i, j, adjucent);
                    wallFaceCount += faceCount;
                    wallFaces.put(i * width + j, adjucent);
                }
            }
        }
        //因为地图边缘有很多空白, 直接将坐标系移到地图中心点不合适 , 应该移到裁剪区域的中心点
        List<Obj3D> objList = new ArrayList<>();
        Obj3D floor = genFloorObj(context, width, height, data, floorFaces, floorFaceCount, unit, offsetX, offsetY);
        objList.add(floor);

        Obj3D wall = genWallObj(context, width, height, data, wallFaces, wallFaceCount, unit, offsetX, offsetY);
        objList.add(wall);

        MultiObj3D multiObj3D = new MultiObj3D();
        multiObj3D.setObj3DList(objList);
        multiObj3D.setTop(0);
        multiObj3D.setBottom(-unit);
        return multiObj3D;
    }

    public static Area3D areaToObj(Context context, float resolution, RectangleArea rectangleArea) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.forbid_area);
        float wallTileSize = 1f;
        float wallTileUnit = resolution;  //地图每一格栅格 在现实中为0.05  在opengl中也是0.05
        float wallHeight = 10;
        float wallTextureUnit = resolution / wallTileSize;

        FloatBuffer vertex = ByteBuffer.allocateDirect(4 * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexTexture = ByteBuffer.allocateDirect(4 * 2 * 3 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        Log.i("mapdataconverter", "rectangleArea x  " + rectangleArea.toString());
        float offsetX = -rectangleArea.center.x * wallTileUnit;
        float offsetY = wallTileUnit * wallHeight;
        float offsetZ = -rectangleArea.center.y * wallTileUnit;

        Log.i("mapdataconverter", "offset x  " + offsetX + " y " + offsetZ);

        //虚拟墙上下两个面不画
        boolean[] adjust = new boolean[]{false, false, false, false, true, true};
        addAreaCuboidVertex(vertex, vertexTexture,
                rectangleArea.width * wallTileUnit, wallHeight*wallTileUnit, rectangleArea.height * wallTileUnit,
                rectangleArea.width * wallTileUnit, wallHeight, rectangleArea.height * wallTileUnit,
                offsetX, offsetY, offsetZ,
                adjust);
        vertex.flip();
        vertexTexture.flip();
        Area3D area3D = Area3D.newBuilder().bitmap(bitmap).vertex(vertex).texture(vertexTexture).build();

        return area3D;
    }


    /**
     * 生成地板的Obj3D 对象
     *
     * @param width     地图宽
     * @param height    地图高
     * @param data      地图数据
     * @param faces     需要绘制的面
     * @param faceCount 绘制面的个数
     * @return Obj3D
     */
    public static Obj3D genFloorObj(Context context, int width, int height, int[] data, SparseArray<boolean[]> faces, int faceCount, float unit, int mapOffsetX, int mapOffsetY) {

        //        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.mipmap.chat);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.woodfloor);
        Bitmap normalBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.woodfloor_normal);

        MtlInfo mtlInfo = MtlInfo.newBuilder()
                .Ka(new float[]{1, 1, 1})
                .Kd(new float[]{245 / 255f, 245 / 255f, 245 / 255f})
                .Ks(new float[]{18 / 255f, 18 / 255f, 18 / 255f})
                .Ke(new float[]{1f, 1f, 1f})
                .kd(bitmap)
                .kdNormal(normalBitmap)
                .Ns(1)
                .illum(7)
                .build();

        //Buffer大小等于: 面数* 每个面2个三角形* 每个三角形3个顶点* 每个顶点3个维度(xyz) * 每个维度四字节(float) : faceCount* 2*3*3*4
        FloatBuffer vertex = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexNormal = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexTexture = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        genFloorVetextData(vertex, vertexTexture, vertexNormal, data, width, height, faces, unit);

        Obj3D obj3D = Obj3D.newBuilder()
                .mtl(mtlInfo)
                .position(vertex)
                .normal(vertexNormal)
                .texture(vertexTexture)
                .vertCount(vertex.limit() / 3)
                .build();
        return obj3D;
    }

    /**
     * 生成墙的Obj3D 对象
     *
     * @param width     地图宽
     * @param height    地图高
     * @param data      地图数据
     * @param faces     需要绘制的面
     * @param faceCount 绘制面的个数
     * @return Obj3D
     * 当前墙和地板只有高度不同,  后期可能会有其他变化
     */
    public static Obj3D genWallObj(Context context, int width, int height, int[] data, SparseArray<boolean[]> faces, int faceCount, float unit, int mapOffsetX, int mapOffsetY) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.brickwall);
        Bitmap normalBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.brickwall_normal);

        MtlInfo mtlInfo = MtlInfo.newBuilder()
                .Ka(new float[]{1, 1, 1})
                .Kd(new float[]{1, 1, 1})
                .Ks(new float[]{1f, 1f, 1f})
                .Ke(new float[]{1f, 1f, 1f})
                .Ns(50)
                .kd(bitmap)
                .kdNormal(normalBitmap)
                .illum(7)
                .build();
        FloatBuffer vertex = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexNormal = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexTexture = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        genWallVetexData(vertex, vertexTexture, vertexNormal, data, width, height, faces, unit);

        Obj3D obj3D = Obj3D.newBuilder()
                .mtl(mtlInfo)
                .position(vertex)
                .normal(vertexNormal)
                .texture(vertexTexture)
                .vertCount(vertex.limit() / 3)
                .build();
        return obj3D;
    }

    /**
     * 把顶点数据添加到Buffer中
     */
    public static void genFloorVetextData(FloatBuffer vertex, FloatBuffer vertexTexture, FloatBuffer vertexNormal, int[] data, int width, int height, SparseArray<boolean[]> faces, float unit) {

        /*        float rectX = unit;
        float rectY = unit;
        float rectZ = unit;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int type = data[i * width + j];
                if (type == 0) {
                    //0 墙内
                    float offsetX = j * unit - mapOffsetX * unit;
                    float offsetY = -rectY / 2;
                    float offsetZ = i * unit - mapOffsetY * unit;
                    boolean[] adjucent = faces.get(i * width + j);
                    addCuboidVertex(vertex, vertexTexture, vertexNormal, rectX / 2, rectY / 2, rectZ / 2, offsetX, offsetY, offsetZ, adjucent);
                }
            }
        }
//        genFloor(vertex, vertexTexture, vertexNormal, width, height, unit, mapOffsetX, mapOffsetY); //直接画宽高大小的地板

        vertex.flip();
        vertexNormal.flip();
        vertexTexture.flip();*/
        float floorTileSize = 1f;
        float floorTileUnit = unit;
        float floorTileTextureUnit = unit / floorTileSize;

        float offsetX = -width / 2 * floorTileUnit;
        float offsetY = -floorTileUnit / 2;
        float offsetZ = -height / 2 * floorTileUnit;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int type = data[i * width + j];
                if (type == 0) {
                    //0 墙内
                    boolean[] adjucent = faces.get(i * width + j);
                    addfloorCuboidVertex(vertex, vertexTexture, vertexNormal,
                            floorTileUnit, floorTileUnit, floorTileUnit,
                            floorTileTextureUnit, floorTileTextureUnit, floorTileTextureUnit, //每一格纹理大小,一格是0.05 代表现实的5厘米, , 地砖如果是0.8m, 那么16格就是一块地砖  1格的纹理大小就是1/16
                            j, 0, i,
                            offsetX, offsetY, offsetZ,
                            adjucent);
                }
            }
        }
        //genFloor(vertex, vertexTexture, vertexNormal, width, height, resolution); //直接画宽高大小的地板

        vertex.flip();
        vertexNormal.flip();
        vertexTexture.flip();


    }

    public static void genWallVetexData(FloatBuffer vertex, FloatBuffer vertexTexture, FloatBuffer vertexNormal, int[] data, int width, int height, SparseArray<boolean[]> faces, float unit) {
        /*        float rectX = unit;
        float rectY = 20 * unit;
        float rectZ = unit;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int type = data[i * width + j];
                //计算立方体6个面有无相邻
                if (type == 1) {
                    //1 墙
                    float offsetX = j * unit - mapOffsetX * unit;    //x 轴偏移 去除空数据后 地图中心点为(mapOffsetX, mapOffsetY) 要将地图中心移动到 (0,0) 所以减去这个向量
                    float offsetY = rectY / 2;                      //y 轴偏移  默认中心点在0, 要往上移 边长的一半,
                    float offsetZ = i * unit - mapOffsetY * unit;   //z 轴偏移  地图中心点为(width/2, height/2) 要将地图中心移动到 (0,0) 所以减去半个地图大小
                    boolean[] adjucent = faces.get(i * width + j);

                    addCuboidVertex(vertex, vertexTexture, vertexNormal, rectX / 2, rectY / 2, rectZ / 2, offsetX, offsetY, offsetZ, adjucent);
                }
            }
        }
        vertex.flip();
        vertexNormal.flip();
        vertexTexture.flip();*/

        float wallTileSize = 0.78f;
        float wallTileUnit = unit;
        float wallTextureUnit = unit / wallTileSize;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int type = data[i * width + j];
                //计算立方体6个面有无相邻
                if (type == 1) {
                    //1 墙
                    boolean[] adjucent = faces.get(i * width + j);

                    float offsetX = -width / 2 * wallTileUnit;
                    float offsetY = wallTileUnit *10;
                    float offsetZ = -height / 2 * wallTileUnit;

                    addWallCuboidVertex(vertex, vertexTexture, vertexNormal,
                            wallTileUnit, wallTileUnit * 20, wallTileUnit,
                            wallTextureUnit, wallTextureUnit * 20, wallTextureUnit, //纹理大小,  一格是0.05 代表现实的5厘米, 20格就是纹理坐标的1 , 地砖如果是0.8, 那么16格就是一块地砖  1格就是1/16
                            j, 0, i,
                            offsetX, offsetY, offsetZ,
                            adjucent);
                }
            }
        }
        vertex.flip();
        vertexNormal.flip();
        vertexTexture.flip();
    }

    /**
     * 计算每个方块有无相邻,并计算需要绘制少个面
     * 相邻的结果存在boolean数组中  顺序为: 前 后 左 右 上 下
     * 有相邻 为true  不用画这个面
     * 无相邻 为false
     */
    public static int isAdjucency(int[] mapData, int w, int h, int i, int j, boolean[] adjucent) {

        int faceCount = 6; //总共6个面

        int positionFront = (i + 1) * w + j;
        int positionBack = (i - 1) * w + j;
        int positionLeft = i * w + j - 1;
        int positionRight = i * w + j + 1;
        //前面有格子
        if (i + 1 < h && mapData[positionFront] < 2) {
            adjucent[0] = true;
            faceCount--; //有相邻的面不画
        }

        //后面有格子
        if (i - 1 >= 0 && mapData[positionBack] < 2) {
            adjucent[1] = true;
            faceCount--;
        }

        //左面有格子
        if (j - 1 >= 0 && mapData[positionLeft] < 2) {
            adjucent[2] = true;
            faceCount--;
        }

        //右面有格子
        if (j + 1 < w && mapData[positionRight] < 2) {
            adjucent[3] = true;
            faceCount--;
        }

        //上下两个面暂时都画
        adjucent[4] = false;
        adjucent[5] = false;
        return faceCount;
    }

    /**
     * 判断墙方块是否相邻墙方块 并计算需要绘制少个面
     * 两面墙相邻才可以不绘制共有的那面墙
     *
     * @return
     */
    public static int isWallAdjucency(int[] mapData, int w, int h, int i, int j, boolean[] adjucent) {

        int faceCount = 6;

        int positionFront = (i + 1) * w + j;
        int positionBack = (i - 1) * w + j;
        int positionLeft = i * w + j - 1;
        int positionRight = i * w + j + 1;
        //前面有格子
        if (i + 1 < h && mapData[positionFront] == 1) {
            adjucent[0] = true;
            faceCount--;
        }

        //后面有格子
        if (i - 1 >= 0 && mapData[positionBack] == 1) {
            adjucent[1] = true;
            faceCount--;
        }

        //左面有格子
        if (j - 1 >= 0 && mapData[positionLeft] == 1) {
            adjucent[2] = true;
            faceCount--;
        }

        //右面有格子
        if (j + 1 < w && mapData[positionRight] == 1) {
            adjucent[3] = true;
            faceCount--;
        }

        //上下两个面暂时都画
        adjucent[4] = false;
        adjucent[5] = false;
        return faceCount;
    }

    public static void addfloorCuboidVertex(FloatBuffer v, FloatBuffer vt, FloatBuffer vn,
                                            float rectX, float rectY, float rectZ,
                                            float textureX, float textureY, float textureZ,
                                            float positionX, float positionY, float positionZ,
                                            float offsetX, float offsetY, float offsetZ,
                                            boolean[] adjucent) {
        //每个矩形根据位置不同 重新设置长宽高和偏移
        for (int i = 0; i < currentCubeVertex.length; i++) {
            if (i % 3 == 0) {
                currentCubeVertex[i] = (originCubeVertex[i] + positionX) * rectX + offsetX;
            } else if (i % 3 == 1) {
                currentCubeVertex[i] = (originCubeVertex[i] + positionY) * rectY + offsetY;
            } else if (i % 3 == 2) {
                currentCubeVertex[i] = (originCubeVertex[i] + positionZ) * rectZ + offsetZ;
            }
        }

        for (int i = 0; i < originCubeTextureCoordinate.length; i++) {
            if (i % 2 == 0) {
                currentCubeTexture[i] = (originCubeTextureCoordinate[i] + positionX) * textureX;

                currentFloorDownTexture[i] = (originCubeTextureCoordinate[i] + positionX) * textureX;
            } else if (i % 2 == 1) {
                currentCubeTexture[i] = (originCubeTextureCoordinate[i] + positionZ) * textureZ;

                currentFloorDownTexture[i] = (originCubeTextureCoordinate[i] - positionZ) * textureZ;
            }
        }


        for (int i = 0; i < adjucent.length; i++) {
            //根据adjucent 判断需要添加几个面 有相邻的面不用添加
            //每个面六个点 每个点有 坐标xyz 法向量xyz 纹理坐标xy
            if (!adjucent[i]) {
                for (int j = 0; j < 6; j++) {
                    int k = i * 6 + j;
                    v.put(currentCubeVertex[vertexIndex[k] * 3]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 1]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 2]);

                    vn.put(originCubeNormal[normalIndex[k] * 3]);
                    vn.put(originCubeNormal[normalIndex[k] * 3 + 1]);
                    vn.put(originCubeNormal[normalIndex[k] * 3 + 2]);

                    //                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2]);
                    //                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2 + 1]);

                    if (i == 4) {
                        vt.put(currentCubeTexture[textureIndex[k] * 2]);
                        vt.put(currentCubeTexture[textureIndex[k] * 2 + 1]);
                    } else if (i == 5) {
                        vt.put(currentFloorDownTexture[textureIndex[k] * 2]);
                        vt.put(currentFloorDownTexture[textureIndex[k] * 2 + 1]);
                    } else {
                        vt.put(0.5f);
                        vt.put(0.5f);
                    }
                }
            }

        }
    }

    public static void addAreaCuboidVertex(FloatBuffer v, FloatBuffer vt,
                                           float rectX, float rectY, float rectZ,
                                           float textureX, float textureY, float textureZ,
                                           float offsetX, float offsetY, float offsetZ,
                                           boolean[] adjucent) {
        //每个矩形根据位置不同 重新设置长宽高和偏移
        for (int i = 0; i < currentCubeVertex.length; i++) {
            if (i % 3 == 0) {
                currentCubeVertex[i] = (originCubeVertex[i]) * rectX + offsetX;
            } else if (i % 3 == 1) {
                currentCubeVertex[i] = (originCubeVertex[i]) * rectY + offsetY;
            } else if (i % 3 == 2) {
                currentCubeVertex[i] = (originCubeVertex[i]) * rectZ + offsetZ;
            }
        }


        for (int i = 0; i < originCubeTextureCoordinate.length; i++) {
            if (i % 2 == 0) {
                currentWallFrontTexture[i] = (originCubeTextureCoordinate[i]) * textureX;
                currentWallBackTexture[i] = (originCubeTextureCoordinate[i]) * textureX;

                currentWallLeftTexture[i] = (originCubeTextureCoordinate[i]) * textureZ;
                currentWallRightTexture[i] = (originCubeTextureCoordinate[i]) * textureZ;
            } else if (i % 2 == 1) {
                currentWallFrontTexture[i] = originCubeTextureCoordinate[i] * textureY;
                currentWallBackTexture[i] = originCubeTextureCoordinate[i] * textureY;

                currentWallLeftTexture[i] = originCubeTextureCoordinate[i] * textureY;
                currentWallRightTexture[i] = originCubeTextureCoordinate[i] * textureY;
            }
        }


        for (int i = 0; i < adjucent.length; i++) {
            //根据adjucent 判断需要添加几个面 有相邻的面不用添加
            //每个面六个点 每个点有 坐标xyz 法向量xyz 纹理坐标xy
            if (!adjucent[i]) {
                for (int j = 0; j < 6; j++) {
                    int k = i * 6 + j;
                    v.put(currentCubeVertex[vertexIndex[k] * 3]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 1]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 2]);

                    if (i == 0) {
                        vt.put(currentWallFrontTexture[textureIndex[k] * 2]);
                        vt.put(currentWallFrontTexture[textureIndex[k] * 2 + 1]);
                    } else if (i == 1) {
                        vt.put(currentWallBackTexture[textureIndex[k] * 2]);
                        vt.put(currentWallBackTexture[textureIndex[k] * 2 + 1]);
                    } else if (i == 2) {
                        vt.put(currentWallLeftTexture[textureIndex[k] * 2]);
                        vt.put(currentWallLeftTexture[textureIndex[k] * 2 + 1]);
                    } else if (i == 3) {
                        vt.put(currentWallRightTexture[textureIndex[k] * 2]);
                        vt.put(currentWallRightTexture[textureIndex[k] * 2 + 1]);
                    } else {
                        //没有上下两个面
                        vt.put(0.5f);
                        vt.put(0.5f);
                    }


                }
            }

        }
    }

    public static void addWallCuboidVertex(FloatBuffer v, FloatBuffer vt, FloatBuffer vn,
                                           float rectX, float rectY, float rectZ,
                                           float textureX, float textureY, float textureZ,
                                           float positionX, float positionY, float positionZ,
                                           float offsetX, float offsetY, float offsetZ,

                                           boolean[] adjucent) {
        //每个矩形根据位置不同 重新设置长宽高和偏移
        for (int i = 0; i < currentCubeVertex.length; i++) {
            if (i % 3 == 0) {
                currentCubeVertex[i] = (originCubeVertex[i] + positionX) * rectX + offsetX;
            } else if (i % 3 == 1) {
                currentCubeVertex[i] = (originCubeVertex[i] + positionY) * rectY + offsetY;
            } else if (i % 3 == 2) {
                currentCubeVertex[i] = (originCubeVertex[i] + positionZ) * rectZ + offsetZ;
            }
        }


        for (int i = 0; i < originCubeTextureCoordinate.length; i++) {
            if (i % 2 == 0) {
                currentWallFrontTexture[i] = (originCubeTextureCoordinate[i] + positionX) * textureX;
                currentWallBackTexture[i] = (originCubeTextureCoordinate[i] - positionX) * textureX;

                currentWallLeftTexture[i] = (originCubeTextureCoordinate[i] + positionZ) * textureZ;
                currentWallRightTexture[i] = (originCubeTextureCoordinate[i] - positionZ) * textureZ;
            } else if (i % 2 == 1) {
                currentWallFrontTexture[i] = originCubeTextureCoordinate[i] * textureY;
                currentWallBackTexture[i] = originCubeTextureCoordinate[i] * textureY;

                currentWallLeftTexture[i] = originCubeTextureCoordinate[i] * textureY;
                currentWallRightTexture[i] = originCubeTextureCoordinate[i] * textureY;
            }
        }


        for (int i = 0; i < adjucent.length; i++) {
            //根据adjucent 判断需要添加几个面 有相邻的面不用添加
            //每个面六个点 每个点有 坐标xyz 法向量xyz 纹理坐标xy
            if (!adjucent[i]) {
                for (int j = 0; j < 6; j++) {
                    int k = i * 6 + j;
                    v.put(currentCubeVertex[vertexIndex[k] * 3]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 1]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 2]);

                    vn.put(originCubeNormal[normalIndex[k] * 3]);
                    vn.put(originCubeNormal[normalIndex[k] * 3 + 1]);
                    vn.put(originCubeNormal[normalIndex[k] * 3 + 2]);

                    //                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2]);
                    //                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2 + 1]);

                    if (i == 0) {
                        vt.put(currentWallFrontTexture[textureIndex[k] * 2]);
                        vt.put(currentWallFrontTexture[textureIndex[k] * 2 + 1]);
                    } else if (i == 1) {
                        vt.put(currentWallBackTexture[textureIndex[k] * 2]);
                        vt.put(currentWallBackTexture[textureIndex[k] * 2 + 1]);
                    } else if (i == 2) {
                        vt.put(currentWallLeftTexture[textureIndex[k] * 2]);
                        vt.put(currentWallLeftTexture[textureIndex[k] * 2 + 1]);
                    } else if (i == 3) {
                        vt.put(currentWallRightTexture[textureIndex[k] * 2]);
                        vt.put(currentWallRightTexture[textureIndex[k] * 2 + 1]);
                    } else {
                        //上下两个面
                        vt.put(0.5f);
                        vt.put(0.5f);
                    }


                }
            }

        }
    }

    public static void addCuboidVertex(FloatBuffer v, FloatBuffer vt, FloatBuffer vn, float rectX, float rectY, float rectZ, float offsetX, float offsetY, float offsetZ, boolean[] adjucent) {
        //每个矩形根据位置不同 重新设置长宽高和偏移
        for (int i = 0; i < currentCubeVertex.length; i++) {
            if (i % 3 == 0) {
                currentCubeVertex[i] = originCubeVertex[i] * rectX + offsetX;
            } else if (i % 3 == 1) {
                currentCubeVertex[i] = originCubeVertex[i] * rectY + offsetY;
            } else if (i % 3 == 2) {
                currentCubeVertex[i] = originCubeVertex[i] * rectZ + offsetZ;
            }
        }

        //每个面六个点 每个点有 坐标xyz 法向量xyz 纹理坐标xy
        for (int i = 0; i < adjucent.length; i++) {
            //根据adjucent 判断需要添加几个面 有相邻的面不用添加
            if (!adjucent[i]) {
                //依次添加每个面的六个点  六个点从根据是第几个面 从索引数组中取
                for (int j = 0; j < 6; j++) {
                    int k = i * 6 + j;
                    //添加
                    v.put(currentCubeVertex[vertexIndex[k] * 3]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 1]);
                    v.put(currentCubeVertex[vertexIndex[k] * 3 + 2]);

                    vn.put(originCubeNormal[normalIndex[k] * 3]);
                    vn.put(originCubeNormal[normalIndex[k] * 3 + 1]);
                    vn.put(originCubeNormal[normalIndex[k] * 3 + 2]);

                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2]);
                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2 + 1]);

                }
            }
        }
    }

    /**
     * 生成路径的数据
     *
     * @param xy        路径数组
     * @param pathColor rgb值  不包含透明度  例:Color.WHITE
     * @return
     */
    public static Path3D convertPathData(float[] xy, int pathColor, float unit, int offsetX, int offsetY) {
        if (xy == null) {
            return null;
        }

        //buffer大小 = 路径点的xy个数 +  一个z轴坐标( *3/2 ) * 每个维度字节数 (float 4)
        FloatBuffer vertex = ByteBuffer.allocateDirect(xy.length / 2 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < xy.length / 2; i++) {
            float x = xy[i * 2] * unit - offsetX * unit;         //将地图制定点 缩放平移到3d地图的指定位置
            float z = xy[i * 2 + 1] * unit - offsetY * unit;
            float y = unit / 2;
            vertex.put(x);
            vertex.put(y);
            vertex.put(z);
        }
        vertex.flip();

        float bgRed = Color.red(pathColor) / 255f;
        float bgGreen = Color.green(pathColor) / 255f;
        float bgBlue = Color.blue(pathColor) / 255f;

        Path3D path3DData = Path3D.newBuilder().color(new float[]{bgRed, bgGreen, bgBlue}).vert(vertex).build();

        return path3DData;
    }

    /**
     * 只用一个长方体绘制地面, 不考虑清扫
     */
    private static void genFloor(FloatBuffer v, FloatBuffer vt, FloatBuffer vn, int width, int height, float unit, int mapOffsetX, int mapOffsetY) {
        float rectX = width * unit / 2;
        float rectY = unit / 2;
        float rectZ = height * unit / 2;

        float offsetX = width * unit / 2 - mapOffsetX * unit;
        float offsetY = unit / 2;
        float offsetZ = height * unit / 2 - mapOffsetY * unit;
        boolean[] adjucent = new boolean[]{false, false, false, false, false, false};
        addCuboidVertex(v, vt, vn, rectX, rectY, rectZ, offsetX, offsetY, offsetZ, adjucent);
    }


    /**
     * 计算模型缩放倍数
     *
     * @param realSize   模型真实大小  单位 m
     * @param modelScale 模型取值范围
     * @return
     */
    public static float calculateModelScale(float realSize, float modelScale) {
        return realSize / modelScale;
    }
}
