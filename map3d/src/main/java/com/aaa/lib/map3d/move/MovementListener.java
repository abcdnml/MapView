package com.aaa.lib.map3d.move;

public interface MovementListener {
    /**
     *
     * @param positionX 位置X
     * @param positionY 位置X
     * @param positionZ 位置X
     * @param directionX    面对方向X
     * @param directionY    面对方向Y
     * @param directionZ    面对方向Z
     */
    void onMove(float positionX, float positionY, float positionZ, float directionX, float directionY, float directionZ);
}
