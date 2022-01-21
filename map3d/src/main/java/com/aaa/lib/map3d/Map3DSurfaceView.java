package com.aaa.lib.map3d;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.aaa.lib.map3d.model.Model;
import com.aaa.lib.map3d.obj.ModelData;
import com.aaa.lib.map3d.utils.MatrixUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Map3DSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, ModelListener {

    private static final float MAX_SCALE = 4;
    private static final float MIN_SCALE = 0.25f;
    private final float TOUCH_SCALE_AC = 5;
    protected ModelManager modelManager;
    protected float[] modelMatrix = Model.getOriginalMatrix();
    protected float[] mProjMatrix = new float[16];
    protected float[] mVMatrix = new float[16];
    protected int bgColor;
    protected float[] eye;
    protected float[] light;
    protected float rotateX = 0;
    protected float rotateY = 0;
    protected float scale = 1;
    private TouchHandler touchHandler;
    private volatile boolean isCreate = false;

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
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);



        modelManager = new ModelManager();
        modelManager.setModelListener(this);
        bgColor = Color.argb(1, 33, 162, 254);
        eye = new float[]{
                0, 9, 0 //eye x y z
        };
        light = new float[]{
                -1f, -1f, -1f,       // direction  x y z
                0.0f, 0.0f, 0.0f,   // ka
                1, 1, 1,   // kd
                1.0f, 1.0f, 1.0f,   // ks
        };

        touchHandler = new TouchHandler(this);
    }

    @Override
    public void onModelAdd(final Model model) {
        //suface创建时 会将已添加的model全部执行一遍oncreate
        //surface创建之后添加的model才需要重新执行oncreate
        if (isCreate) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    model.onCreate(getContext());
                }
            });
        }
    }

    @Override
    public void onModelRemove(final Model model) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                model.onDestroy();
            }
        });
    }

    protected void addModel(final Model model) {
        model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        model.setEye(eye);
        model.setLight(light);
        modelManager.addModel(model);
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
        isCreate = true;
        setBgColor(bgColor);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE_MODE);
        modelManager.onSurfaceCreate(getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);

        float aspectRatio = (width + 0f) / height;
        //眼睛坐标和法向量一定要算好 要不然 看到别的地方去了
        Matrix.setLookAtM(mVMatrix, 0, eye[0], eye[1], eye[2], 0, 0, 0, 0, 0, -1);
        Matrix.perspectiveM(mProjMatrix, 0, 60, aspectRatio, 1f, 100);

        modelManager.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        modelManager.onDraw();
    }

    /**
     * 更新模型数据  除了地图路径 其他的模型加载之后应该不会修改 ,只会添加和移除
     *
     * @param model
     * @param data
     */
    public void updateModelData(final Model model, final ModelData data) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                model.updateModelData(data);
            }
        });
    }

    //旋转
    public void rotate(float distanceX, float distanceY) {
        rotateX = rotateX + distanceX;
//        临时去掉旋转限制
        if (rotateY + distanceY > 90 * TOUCH_SCALE_AC || rotateY + distanceY < 0) {
            distanceY = 0;
        } else {
            rotateY = rotateY + distanceY;
        }

        Matrix.setRotateM(modelMatrix, 0, -rotateY / TOUCH_SCALE_AC, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, -rotateX / TOUCH_SCALE_AC, 0, 1, 0);
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

        float[] oldLightDir=new float[4];
        oldLightDir[0]=-1;
        oldLightDir[1]=-1;
        oldLightDir[2]=-1;
        oldLightDir[3]=-1;

        float[] newLightDir=new float[4];

        Matrix.multiplyMV(newLightDir,0, modelMatrix,0,oldLightDir,0);


        float[] newLight=new float[12];
        System.arraycopy(light,0,newLight,0,12);
        newLight[0]=newLightDir[0];
        newLight[1]=newLightDir[1];
        newLight[2]=newLightDir[2];
        modelManager.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        modelManager.setLight(newLight);
        requestRender();
    }

    public void scale(float s) {
        //这样写可以造成一个缩放回弹的效果 回弹效果要在scaleEnd时重新设置回边界大小
        Matrix.scaleM(modelMatrix, 0, s, s, s);
        scale = scale * s;

        modelManager.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
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

        modelManager.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        requestRender();
    }

    public void move(float distanceX, float distanceY, float distanceZ) {
        eye[0] = eye[0] + distanceX;
        eye[1] = eye[1] + distanceY;
        eye[2] = eye[2] + distanceZ;
        Matrix.setLookAtM(mVMatrix, 0, eye[0], eye[1], eye[2], 0, 0, 0, 0, 0, -1);

        modelManager.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        requestRender();
    }

    public void moveTo(float x, float y, float z) {
        eye[0] = x;
        eye[1] = y;
        eye[2] = z;
        Matrix.setLookAtM(mVMatrix, 0, eye[0], eye[1], eye[2], 0, 0, 0, 0, 0, -1);

        modelManager.setMatrix(modelMatrix, mVMatrix, mProjMatrix);

        requestRender();
    }

}
