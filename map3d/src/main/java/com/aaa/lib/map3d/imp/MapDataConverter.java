package com.aaa.lib.map3d.imp;

import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;

import com.aaa.lib.map3d.obj.MtlInfo;
import com.aaa.lib.map3d.obj.Obj3D;
import com.aaa.lib.map3d.obj.Obj3DData;
import com.aaa.lib.map3d.obj.Path3DData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapDataConverter {
    //原始立方体顶点坐标
    private static final float[] originCubeVertex = new float[]{
            -1, 1, 1,//前 左 上
            1, 1, 1,//前 右 上
            1, -1, 1,//前 右 下
            -1, -1, 1,//前 左 下
            -1, 1, -1,//后 左 上
            1, 1, -1,//后 右 上
            1, -1, -1,//后 右 下
            -1, -1, -1,//后 左 下
    };
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
    //纹理坐标
    private static final float[] originCubeTextureCoordinate = new float[]{
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };
    //绘制一个立方体的36个顶点的索引
    private static final int[] vertexIndex = new int[]{
            0, 1, 2, 0, 2, 3,//正面两个三角形
            4, 5, 6, 4, 6, 7,//背面
            0, 4, 7, 0, 7, 3,//左侧
            1, 5, 6, 1, 6, 2, //右侧
            0, 1, 5, 0, 5, 4,//上
            3, 2, 6, 3, 6, 7
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
            0, 1, 2, 0, 2, 3,//正面两个三角形
            1, 0, 3, 1, 3, 2,//背面
            1, 0, 3, 1, 3, 2,//左侧
            0, 1, 2, 0, 2, 3, //右侧
            3, 2, 1, 3, 1, 0,//上
            0, 1, 2, 0, 2, 3//下
    };

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
    public static Obj3DData mapDataToObj3D(int width, int height, int[] data, float unit, int offsetX, int offsetY) {
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
        Obj3D floor = genFloorObj(width, height, data, floorFaces, floorFaceCount, unit, offsetX, offsetY);
        objList.add(floor);

        Obj3D wall = genWallObj(width, height, data, wallFaces, wallFaceCount, unit, offsetX, offsetY);
        objList.add(wall);

        Obj3DData obj3DData = new Obj3DData();
        obj3DData.setObj3DList(objList);
        obj3DData.setTop(0);
        obj3DData.setBottom(-unit);
        return obj3DData;
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
    public static Obj3D genFloorObj(int width, int height, int[] data, SparseArray<boolean[]> faces, int faceCount, float unit, int mapOffsetX, int mapOffsetY) {

//        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.mipmap.chat);
        MtlInfo mtlInfo = MtlInfo.newBuilder()
                .Ka(new float[]{1, 1, 1})
                .Kd(new float[]{1, 1, 1})
                .Ks(new float[]{1f, 1f, 1f})
                .Ke(new float[]{1f, 1f, 1f})
                .Ns(50)
//                .bitmap(bitmap)
                .illum(7)
                .build();

        //Buffer大小等于: 面数* 每个面2个三角形* 每个三角形3个顶点* 每个顶点3个维度(xyz) * 每个维度四字节(float) : faceCount* 2*3*3*4
        FloatBuffer vertex = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexNormal = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexTexture = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        float rectX = unit;
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
        vertexTexture.flip();

        Obj3D obj3D = Obj3D.newBuilder()
                .mtl(mtlInfo)
                .vert(vertex)
                .vertNorl(vertexNormal)
                .vertTexture(vertexTexture)
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
    public static Obj3D genWallObj(int width, int height, int[] data, SparseArray<boolean[]> faces, int faceCount, float unit, int mapOffsetX, int mapOffsetY) {
//        Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.mipmap.chat);
        MtlInfo mtlInfo = MtlInfo.newBuilder()
                .Ka(new float[]{1, 1, 1})
                .Kd(new float[]{1, 1, 1})
                .Ks(new float[]{1f, 1f, 1f})
                .Ke(new float[]{1f, 1f, 1f})
                .Ns(50)
//                .bitmap(bitmap)
                .illum(7)
                .build();
        FloatBuffer vertex = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexNormal = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexTexture = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();


        float rectX = unit;
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
        vertexTexture.flip();
        Obj3D obj3D = Obj3D.newBuilder()
                .mtl(mtlInfo)
                .vert(vertex)
                .vertNorl(vertexNormal)
                .vertTexture(vertexTexture)
                .vertCount(vertex.limit() / 3)
                .build();
        return obj3D;
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
    public static Path3DData convertPathData(float[] xy, int pathColor, float unit, int offsetX, int offsetY) {
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

        Path3DData path3DData = Path3DData.newBuilder().color(new float[]{bgRed, bgGreen, bgBlue}).vert(vertex).build();

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
