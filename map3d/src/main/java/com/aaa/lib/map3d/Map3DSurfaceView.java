package com.aaa.lib.map3d;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.aaa.lib.map3d.eye.Eye;
import com.aaa.lib.map3d.light.DirectLight;
import com.aaa.lib.map3d.light.Light;
import com.aaa.lib.map3d.light.LightManager;
import com.aaa.lib.map3d.model.Model;
import com.aaa.lib.map3d.move.MoveManager;
import com.aaa.lib.map3d.move.MovementListener;
import com.aaa.lib.map3d.obj.ModelData;
import com.aaa.lib.map3d.program.GLProgram;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Map3DSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, ModelListener, MovementListener {

    private static final float MAX_SCALE = 4;
    private static final float MIN_SCALE = 0.25f;
    private final float TOUCH_SCALE_AC = 5;

    protected int bgColor;
    protected float rotateX = 0;
    protected float rotateY = 0;
    protected float scale = 1;
    protected float aspect = 1;
    protected float fovy = 45;
    protected float near = 1;
    protected float far = 50;
    protected float[] modelMatrix = Model.getOriginalMatrix();
    protected float[] mProjMatrix = new float[16];
    protected float[] mVMatrix = new float[16];
    protected ModelManager modelManager;
    protected MoveManager moveManager;
    protected LightManager lightManager;
    private volatile boolean isCreate = false;
    private float[] mShadowViewMatrix = new float[16];
    private float[] mShadowProjMatrix = new float[16];
    private TouchHandler touchHandler;


    public Map3DSurfaceView(Context context) {
        super(context);
        init();
    }

    public Map3DSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgColor = Color.argb(1, 33, 162, 254);

        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);


        GLProgram.init(getContext());

        modelManager = new ModelManager(this);
        modelManager.setModelListener(this);
        touchHandler = new TouchHandler(this);
        moveManager = new MoveManager(this);
        lightManager=new LightManager();

        //眼睛放到平行光的位置 以光的视角来看物体 形成阴影贴图
        float[] shadowEye = lightManager.getDirectLight().dirction;
        Matrix.orthoM(mShadowProjMatrix, 0, -20, 20, -20, 20, 1f, 100);
        Matrix.setLookAtM(mShadowViewMatrix, 0, -shadowEye[0], -shadowEye[1], -shadowEye[2], 0, 0, 0, 0, 1, 0);

    }

    @Override
    public void onModelAdd(final Model model) {
        //suface创建时 会将已添加的model全部执行一遍oncreate
        //surface创建之后添加的model才需要重新执行oncreate
        if (isCreate) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    modelManager.initData(model);
                }
            });
        }
    }

    @Override
    public void onModelRemove(final Model model) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                model.clear();
            }
        });
    }

    protected void addModel(final Model model) {
        modelManager.addModel(model);
    }

    public void remove(Model model) {
        modelManager.removeModel(model);
    }

    public void clear() {
        modelManager.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event);
    }

    /**
     * 设置背景颜色
     *
     * @param color 背景颜色
     */
    public void setBgColor(int color) {
        this.bgColor = color;
        float bgRed = Color.red(bgColor) / 255f;
        float bgGreen = Color.green(bgColor) / 255f;
        float bgBlue = Color.blue(bgColor) / 255f;
        float bgAlpha = Color.alpha(bgColor) / 255f;
        GLES30.glClearColor(bgRed, bgGreen, bgBlue, bgAlpha);
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        setBgColor(bgColor);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);

        modelManager.initModel();
        isCreate = true;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mVMatrix = moveManager.genViewMatrix();
        aspect=(width + 0f) / height;
        Matrix.perspectiveM(mProjMatrix, 0, fovy, aspect, near, far);
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        modelManager.draw(modelMatrix, mVMatrix, mProjMatrix, mShadowViewMatrix, mShadowProjMatrix);
    }

    /**
     * 更新模型数据  除了地图路径 其他的模型加载之后应该不会修改 ,只会添加和移除
     *
     * @param model
     * @param data
     */
    public void updateModelData(final Model model, final ModelData data) {
        model.setData(data);
        if(isCreate){
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    modelManager.updateData(model);
                }
            });
        }
    }

    //旋转整个世界 当只有一个眼睛的时候等同 其实就相当于自己转....
    public synchronized void rotateWorld(float distanceX, float distanceY) {
        rotateX = rotateX + distanceX;
//        rotateY = rotateY + distanceY;        //去掉旋转限制

        //y轴旋转限制 0~90度之间
        if (rotateY + distanceY > 90 * TOUCH_SCALE_AC || rotateY + distanceY < 0) {
            distanceY = 0;
        } else {
            rotateY = rotateY + distanceY;
        }

        Matrix.setRotateM(modelMatrix, 0, -rotateY / TOUCH_SCALE_AC, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, -rotateX / TOUCH_SCALE_AC, 0, 1, 0);
        float[] shadowEye=lightManager.getRotatedLight(modelMatrix);
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);        //scale 必须放在设置光线后  因为光线不需要缩放 否则 地图扩大后看不到阴影
        Matrix.setLookAtM(mShadowViewMatrix, 0, -shadowEye[0], -shadowEye[1], -shadowEye[2], 0, 0, 0, 0, 1, 0);
        requestRender();
    }

    public synchronized void scale(float s) {
        //这样写可以造成一个缩放回弹的效果 回弹效果要在scaleEnd时重新设置回边界大小
        Matrix.scaleM(modelMatrix, 0, s, s, s);
        scale = scale * s;
        requestRender();
    }

    public void onScaleEnd(float s) {
        float tempScale = scale * s;
        if (tempScale > MAX_SCALE) {
            s = MAX_SCALE / scale;
            scale = MAX_SCALE;
        } else if (tempScale < MIN_SCALE) {
            s = MIN_SCALE / scale;
            scale = MIN_SCALE;
        } else {
            scale = tempScale;
        }
        Matrix.scaleM(modelMatrix, 0, s, s, s);

        requestRender();
    }

    public void move(float distanceX, float distanceY, float distanceZ) {
        mVMatrix = moveManager.move(distanceX, distanceY, distanceZ);
        requestRender();
    }

    public void moveTo(float x, float y, float z) {
        mVMatrix = moveManager.moveTo(x, y, z);
        requestRender();
    }

    public Light getLight() {
        return lightManager.getDirectLight();
    }

    public Eye getEye() {
        return moveManager.getEye();
    }

    public void setSight(float near ,float far){
        this.setSight(45,near,far);
    }
    public void setSight(float fovy,float near ,float far){
        this.setSight(fovy,aspect,near,far);
    }
    public void setSight(float fovy,float aspect,float near ,float far){
        this.aspect=aspect;
        this.fovy=fovy;
        this.near=near;
        this.far=far;
        Matrix.perspectiveM(mProjMatrix, 0, fovy, aspect, near, far);
        requestRender();
    }

    @Override
    public void onMove(float positionX, float positionY, float positionZ, float directionX, float directionY, float directionZ) {
        Log.i("onMove", "position: (" + positionX + "," + positionY + "," + positionZ + ")");
        Log.i("onMove", "direction: (" + directionX + "," + directionY + "," + directionZ + ")");
    }

    /**
     * 俯仰角(Pitch)、偏航角(Yaw)和滚转角(Roll)
     *
     */
    public void rotateSelf(float pitch, float yaw, float roll) {
        mVMatrix = moveManager.rotate(pitch, yaw, roll);
        requestRender();
    }
}
