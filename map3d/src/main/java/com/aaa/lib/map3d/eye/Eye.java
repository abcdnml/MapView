package com.aaa.lib.map3d.eye;

public class Eye {
    /**
     * 眼睛位置
     */
    public float[] position = new float[]{0, 0, 10};
    /**
     * 视线方向
     */
    public float[] direction = new float[]{0, -1, 0}; //如果以上北下南左西右东 逆时针方向为正方向 默认朝向北方
    /**
     * 欧拉角 用于计算视角旋转
     */
    public float[] euler = new float[]{0, -90, 0}; //偏航角0度是面向东边 所以与上面对应 -90度是北方

    /**
     * 法线方向 , 比如人站立时的上方
     */
    public float[] normal = new float[]{0, 1, 0};
}
