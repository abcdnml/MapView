package com.aaa.lib.map3d.move;

import android.opengl.Matrix;
import android.util.Log;

import com.aaa.lib.map3d.eye.Eye;


public class MoveManager {
    Eye eye;

    MovementListener movementListener = new MovementListener() {
        @Override
        public void onMove(float positionX, float positionY, float positionZ, float directionX, float directionY, float directionZ) {
            Log.i("onMove", "position: (" + positionX + "," + positionY + "," + positionZ + ")");
            Log.i("onMove", "direction: (" + directionX + "," + directionY + "," + directionZ + ")");
        }
    };

    public MoveManager(MovementListener listener) {
        this();
        setMovementListener(listener);
    }

    public MoveManager() {
        eye = new Eye();
        eye.position = new float[]{0, 10, 0};
        eye.direction = new float[]{0, 0, -1}; //如果以上北下南左西右东 逆时针方向为正方向 默认朝向北方
        eye.euler = new float[]{0, 0, 0}; //如果是0 0 0 是面向东边  所以与上面对应 -90度是北方
        eye.normal = new float[]{0, 0, -1};
        rotate(0, 0, 0);
    }

    public Eye getEye() {
        return eye;
    }

    public float[] getPosition() {
        return eye.position;
    }

    public float[] getNormal() {
        return eye.normal;
    }

    /**
     * 获取视图矩阵
     *
     * @return
     */
    public float[] genViewMatrix() {
        float[] vMatrix = new float[16];
        // 眼睛的目标的位置等于 位置+方向
        float centerX = eye.direction[0] + eye.position[0];
        float centerY = eye.direction[0] + eye.position[0];
        float centerZ = eye.direction[0] + eye.position[0];
        Matrix.setLookAtM(vMatrix, 0, eye.position[0], eye.position[1], eye.position[2], centerX, centerY, centerZ, eye.normal[0], eye.normal[1], eye.normal[2]);

        //回调
        if (movementListener != null) {
            movementListener.onMove(eye.position[0], eye.position[1], eye.position[2], eye.direction[0], eye.direction[1], eye.direction[2]);
        }

        return vMatrix;
    }

    /**
     * 环绕某点移动
     *
     * @param centerX centerY 中心点
     * @param normal 法线方向
     * @param distance 环绕距离
     */
    public void surround(float centerX, float centerY, float[] normal, float distance) {

    }

    /**
     * 沿着当前朝向移动
     *
     * @param distance 距离 或者说 速度
     * @return 视图矩阵
     */
    public float[] moveDirection(float distance) {
        return moveDirection(distance, eye.direction);
    }

    /**
     * 朝着某个方向移动指定距离
     * (比如我可以看着右边但是往前走 这时可以额外指定)
     *
     * @param distance 移动距离
     * @param direct   移动方向
     * @return 视图矩阵
     */
    public float[] moveDirection(float distance, float[] direct) {
        return move(distance * direct[0], distance * direct[1], distance * direct[2]);
    }

    /**
     * 根据向量移动
     *
     * @param distanceX distanceY distanceZ
     * @return 视图矩阵
     */
    public float[] move(float distanceX, float distanceY, float distanceZ) {
        return moveTo(eye.position[0] + distanceX, eye.position[1] + distanceY, eye.position[2] + distanceZ);
    }

    /**
     * 移动到某个坐标
     *
     * @param x y z
     * @return 视图矩阵
     */
    public float[] moveTo(float x, float y, float z) {
        eye.position[0] = x;
        eye.position[1] = y;
        eye.position[2] = z;
        return genViewMatrix();
    }

    /**
     * 旋转视角
     *
     * @param x y z
     * @return
     */
    public float[] rotate(float pitch, float yaw, float roll) {
        eye.euler[0] += pitch;
        eye.euler[1] += yaw;
        eye.euler[2] += roll;

        return rotateTo(eye.euler);
    }


    /**
     * 旋转方向限制
     *
     * @param eulerAngle
     */
    private void rotateLimit(float[] eulerAngle) {
        //限制俯仰角角度  否则超过90度就会翻转
        if (eulerAngle[0] > 89) {
            eulerAngle[0] = 89;
        }
        if (eulerAngle[0] < -89) {
            eulerAngle[0] = -89;
        }
    }

    /**
     * 旋转到指定角度
     *
     * @param eulerAngle 欧拉角
     * @return
     */
    public float[] rotateTo(float[] eulerAngle) {

        rotateLimit(eulerAngle);

        eye.direction[0] = (float) Math.cos(Math.toRadians(eulerAngle[0])) * (float) Math.cos(Math.toRadians(eulerAngle[1]));
        eye.direction[1] = (float) Math.sin(Math.toRadians(eulerAngle[0]));
        eye.direction[2] = (float) Math.cos(Math.toRadians(eulerAngle[0])) * (float) Math.sin(Math.toRadians(eulerAngle[1]));
        eye.direction = normalize(eye.direction); //转换成单位向量  在运动的时候好计算运动距离  速度

        return genViewMatrix();
    }

    private float[] normalize(float[] in) {
        float[] out = new float[3];
        float length = (float) Math.sqrt(in[0] * in[0] + in[1] * in[1] + in[2] * in[2]);
        out[0] = in[0] / length;
        out[1] = in[1] / length;
        out[2] = in[2] / length;
        return out;
    }

    public void setMovementListener(MovementListener listener) {
        this.movementListener = listener;
    }
}
