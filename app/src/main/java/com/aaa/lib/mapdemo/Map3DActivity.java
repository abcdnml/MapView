package com.aaa.lib.mapdemo;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.lib.map3d.Map3DSurfaceView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Map3DActivity extends AppCompatActivity {
    private Map3DSurfaceView map3DSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map3d);
        map3DSurfaceView = findViewById(R.id.sv_world);
        map3DSurfaceView.setBgColor(Color.YELLOW);
//        test3D();
        map();
    }

    public void test3D() {
        map3DSurfaceView.add3DModel("assets/obj/扫地机器人.obj",
                0, 0,
                0.01f,   //如果模型大小单位为cm 且与实际大小一致 则 scale==0.01
                0);
    }

    public void map() {
        //读取地图数据
        Gson gson = new Gson();
        LogUtils.i(" start ");
        LDMapBean ldMapBean = gson.fromJson(readAssetString("test/map.json"), LDMapBean.class);
        LogUtils.i(" read file ");
        //转换地图数据 地图点的类型为 0-墙内 1-墙 2-墙外
        int[] mapData = new int[ldMapBean.baseMapData.length()];
        for (int i = 0; i < ldMapBean.baseMapData.length(); i++) {
            mapData[i] = ldMapBean.baseMapData.charAt(i) - '0';
        }
        LogUtils.i(" convert map value ");
        //现在默认是resolution * 2  resolution默认为0.05  也就是地板方块长宽高0.1
        map3DSurfaceView.refreshMap(ldMapBean.width, ldMapBean.height, ldMapBean.resolution * 2, mapData);


        //转换路径数据  根据项目不同
        float[] pathXY = new float[ldMapBean.path.size()];
        for (int i = 0; i < ldMapBean.path.size() / 2; i++) {
            pathXY[i * 2] = ldMapBean.width - (ldMapBean.path.get(i * 2 + 1) / 5 - ldMapBean.y_min * 20); //pd的计算方式就是这样 别问为什么
            pathXY[i * 2 + 1] = ldMapBean.height - (ldMapBean.path.get(i * 2) / 5 - ldMapBean.x_min * 20);
        }

        LogUtils.i(" conver path value ");

        map3DSurfaceView.refreshPath(ldMapBean.width, ldMapBean.height, pathXY);
        LogUtils.i(" end ");

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
