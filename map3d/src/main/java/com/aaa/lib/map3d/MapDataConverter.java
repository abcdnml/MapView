package com.aaa.lib.map3d;

import android.graphics.Color;
import android.util.SparseArray;

import com.aaa.lib.map3d.obj.MtlInfo;
import com.aaa.lib.map3d.obj.Obj3D;
import com.aaa.lib.map3d.obj.Path3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
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


    //数据转换成obj格式
    public static List<Obj3D> mapDataToObj3D(int width, int height, int[] data, float unit) {
        int floorFaceCount = 0;
        int wallFaceCount = 0;
        SparseArray<boolean[]> floorFaces = new SparseArray<>();
        SparseArray<boolean[]> wallFaces = new SparseArray<>();
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
                } else if (type == 2) {
                    //2 墙外
                } else {

                }
            }
        }
        //UNIT_SIZE 现在默认是resolution * 2  resolution默认为0.05  也就是地板方块长宽高0.1m

        List<Obj3D> obj3Ds = new ArrayList<>();
        Obj3D floor = genFloorObj(width, height, data, floorFaces, floorFaceCount, unit);
        obj3Ds.add(floor);

        Obj3D wall = genWallObj(width, height, data, wallFaces, wallFaceCount, unit);
        obj3Ds.add(wall);
        return obj3Ds;
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
    public static Obj3D genFloorObj(int width, int height, int[] data, SparseArray<boolean[]> faces, int faceCount, float unit) {

//        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.mipmap.chat);
        MtlInfo mtlInfo = MtlInfo.newBuilder()
                .Ka(new float[]{0.9f, 0.9f, 0.9f})
                .Kd(new float[]{0.89300000667572f, 0.93328571319580f, 0.93999999761581f})
                .Ks(new float[]{0.09019608050585f, 0.10980392247438f, 0.13333334028721f})
                .Ke(new float[]{1f, 1f, 1f})
                .Ns(1)
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
                    float offsetX = j * unit - width * unit / 2;
                    float offsetY = rectY / 2;
                    float offsetZ = i * unit - height * unit / 2;
                    boolean[] adjucent = faces.get(i * width + j);
                    addCuboidVertex(vertex, vertexTexture, vertexNormal, rectX / 2, rectY / 2, rectZ / 2, offsetX, offsetY, offsetZ, adjucent);
                }
            }
        }
        //genFloor(vertex, vertexTexture, vertexNormal, width, height, resolution); //直接画宽高大小的地板

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
    public static Obj3D genWallObj(int width, int height, int[] data, SparseArray<boolean[]> faces, int faceCount, float unit) {
//        Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.mipmap.chat);
        MtlInfo mtlInfo = MtlInfo.newBuilder()
                .Ka(new float[]{0.9f, 0.9f, 0.9f})
                .Kd(new float[]{0.89300000667572f, 0.93328571319580f, 0.93999999761581f})
                .Ks(new float[]{0.09019608050585f, 0.10980392247438f, 0.13333334028721f})
                .Ke(new float[]{1f, 1f, 1f})
                .Ns(1)
//                .bitmap(bitmap)
                .illum(7)
                .build();
        FloatBuffer vertex = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexNormal = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer vertexTexture = ByteBuffer.allocateDirect(faceCount * 2 * 3 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();


        float rectX = unit;
        float rectY = 15 * unit;
        float rectZ = unit;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int type = data[i * width + j];
                //计算立方体6个面有无相邻
                if (type == 1) {
                    //1 墙
                    float offsetX = j * unit - width * unit / 2;    //x 轴偏移 地图中心点为(width/2, height/2) 要将地图中心移动到 (0,0) 所以减去半个地图大小
                    float offsetY = rectY / 2;                      //y 轴偏移  默认中心点在0, 要往上移 边长的一半,
                    float offsetZ = i * unit - height * unit / 2;   //z 轴偏移  地图中心点为(width/2, height/2) 要将地图中心移动到 (0,0) 所以减去半个地图大小
                    boolean[] adjucent = faces.get(i * width + j);

                    addCuboidVertex(vertex, vertexTexture, vertexNormal, rectX / 2, rectY / 2, rectZ / 2, offsetX, offsetY, offsetZ, adjucent);
                }
            }
        }
        vertex.flip();
        vertexNormal.flip();
        vertexTexture.flip();
//        genWallVetexData(vertex, vertexTexture, vertexNormal,data,  width, height, faces);
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

                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2]);
                    vt.put(originCubeTextureCoordinate[textureIndex[k] * 2 + 1]);

                }
            }
        }
    }

    /**
     * 生成路径的数据
     *
     * @param width
     * @param height
     * @param xy         路径数组
     * @param resolution 分辨率  与地图统一
     * @param pathColor  rgb值  不包含透明度  例:Color.WHITE
     * @return
     */
    public static Path3D convertPathData(int width, int height, float[] xy, float unit, int pathColor) {

        //buffer大小 = 路径点的xy个数 +  一个z轴坐标( *3/2 ) * 每个维度字节数 (float 4)
        FloatBuffer vertex = ByteBuffer.allocateDirect(xy.length / 2 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < xy.length / 2; i++) {
            float x = xy[i * 2] * unit - width * unit / 2;         //将地图制定点 缩放平移到3d地图的指定位置
            float z = xy[i * 2 + 1] * unit - height * unit / 2;
            float y = unit * 3 / 2;
            vertex.put(x);
            vertex.put(y);
            vertex.put(z);
        }
        vertex.flip();

        float bgRed = Color.red(pathColor) / 255f;
        float bgGreen = Color.green(pathColor) / 255f;
        float bgBlue = Color.blue(pathColor) / 255f;

        Path3D path3D = Path3D.newBuilder().color(new float[]{bgRed, bgGreen, bgBlue}).vert(vertex).build();

        return path3D;
    }

    /**
     * 只用一个长方体绘制地面, 不考虑清扫
     */
    private static void genFloor(FloatBuffer v, FloatBuffer vt, FloatBuffer vn, int width, int height, float unit) {
        float rectX = width * unit;
        float rectY = unit;
        float rectZ = height * unit;

        float offsetX = -width * unit / 2;
        float offsetY = unit / 2;
        float offsetZ = -height * unit / 2;
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
