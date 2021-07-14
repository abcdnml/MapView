package com.aaa.lib.mapdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.lib.map3d.obj.Obj3DData;
import com.aaa.lib.map3d.obj.ObjReader;
import com.aaa.lib.mapdemo.bean.Furniture;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Map3DActivity extends AppCompatActivity {
    Handler handler = new Handler();
    private Room3DSurfaceView map3DSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map3d);
        map3DSurfaceView = findViewById(R.id.sv_world);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                    map();
                test3D();
            }
        }, 3000);
/*        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Obj3DData sweeperObj = ObjReader.readMultiObj(Map3DActivity.this, "assets/obj/扫地机器人.obj");
                Sweeper sweeper=Sweeper.newBuilder().data(sweeperObj)
                        .position(150,0)
                        .rotation(0)
                        .scale(1f)
                        .build();
                map3DSurfaceView.refreshSweeper(sweeper);
            }
        }, 6000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Obj3DData sweeperObj = ObjReader.readMultiObj(Map3DActivity.this, "assets/obj/充电座.obj");
                Power power=Power.newBuilder().data(sweeperObj)
                        .position(0,150)
                        .rotation(0)
                        .scale(1f)
                        .build();
                map3DSurfaceView.refreshPower(power);
            }
        }, 9000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                map3DSurfaceView.refreshPower(null);
                map3DSurfaceView.refreshSweeper(null);
            }
        }, 12000);*/
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    public void test3D() {


//        Obj3DData powerObj = ObjReader.readMultiObj(this, "assets/obj/充电座.obj");
        Obj3DData obj3DList = ObjReader.readMultiObj(this, "assets/obj/单人床1.5x2.0.obj");
        Furniture furniture = Furniture.newBuilder()
                .objData(obj3DList)
                .id(1000)
                .position(200, 200)
                .rotation(45)
                .scale(1f)
                .build();
        map3DSurfaceView.refreshFurniture(furniture);
    }

    public void map() {
        //读取地图数据
        Gson gson = new Gson();
        LDMapBean ldMapBean = gson.fromJson(readAssetString("test/map.json"), LDMapBean.class);
        //转换地图数据 地图点的类型为 0-墙内 1-墙 2-墙外
        int[] mapData = new int[ldMapBean.baseMapData.length()];
        for (int i = 0; i < ldMapBean.baseMapData.length(); i++) {
            mapData[i] = ldMapBean.baseMapData.charAt(i) - '0';
        }

        //现在默认是resolution * 2  resolution默认为0.05  也就是地板方块长宽高0.1
        map3DSurfaceView.refreshMap(ldMapBean.width, ldMapBean.height, ldMapBean.resolution / 2, mapData);


        //转换路径数据  根据项目不同
        float[] pathXY = new float[ldMapBean.path.size()];
        for (int i = 0; i < ldMapBean.path.size() / 2; i++) {
            pathXY[i * 2] = ldMapBean.width - (ldMapBean.path.get(i * 2 + 1) / 5 - ldMapBean.y_min * 20); //pd的计算方式就是这样 别问为什么
            pathXY[i * 2 + 1] = ldMapBean.height - (ldMapBean.path.get(i * 2) / 5 - ldMapBean.x_min * 20);
        }
        map3DSurfaceView.refreshPath(pathXY, Color.BLUE);

    }


    public String readAssetString(String path) {
        String tmp = null;
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(path)));
            while ((tmp = br.readLine()) != null) {
                buffer.append(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tmp = buffer.toString();
//        LogUtils.ls(tmp);
        return tmp;
    }
}
