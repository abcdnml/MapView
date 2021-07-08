package com.aaa.lib.map3d;

import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.aaa.lib.map3d.model.Model;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Map3DRender implements GLSurfaceView.Renderer {
    private static final String TAG = Map3DRender.class.getSimpleName();
    private static final float MAX_SCALE = 4;
    private static final float MIN_SCALE = 0.25f;
    final float TOUCH_SCALE_AC = 5;
    private int bgColor;
    private GLSurfaceView surfaceView;
    private volatile List<Model> modelList;
    private float[] modelMatrix = Model.getOriginalMatrix();
    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] eye;
    private float[] light ;
    private float rotateX = 0;
    private float rotateY = 0;
    private float scale = 1;

    public Map3DRender(GLSurfaceView surfaceView, int bgColor) {
        this.bgColor = bgColor;
        this.surfaceView = surfaceView;
        init();
    }
    private void init(){
        modelList = new ArrayList<>();
        eye=new float[]{
                0, 9, 0 //eye x y z
        };
        light =new float[]{
                -1f, -8f, 0f,       // direction  x y z
                0.8f, 0.8f, 0.8f,   // ka
                0.2f, 0.2f, 0.2f,   // kd
                1.0f, 1.0f, 1.0f,   // ks
        };
    }

    public void addModel(final Model model) {
        Log.i(TAG, "addModel");
        modelList.add(model);
        model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        model.setEye(eye);
        model.setLight(light);
    }

    public void remove(Model shape) {
        modelList.remove(shape);
    }

    public void clear() {
        modelList.clear();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated: ");
        setBgColor(bgColor);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE_MODE);

        for (Model shape : modelList) {
            shape.onSurfaceCreate(surfaceView.getContext());
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged width: " + width + " height : " + height);
        GLES30.glViewport(0, 0, width, height);

        float aspectRatio = (width + 0f) / height;
        //眼睛坐标和法向量一定要算好 要不然 看到别的地方去了
        Matrix.setLookAtM(mVMatrix, 0, eye[0],eye[1],eye[2],0,0,0,0,0,-1);
        Matrix.perspectiveM(mProjMatrix, 0, 72, aspectRatio, 0.1f, 100);
        for (Model model : modelList) {
            model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
            model.onSurfaceChange(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        Log.i(TAG, "onDrawFrame width: " + modelList.size());
        for (Model model : modelList) {
            model.onDraw();
        }
    }

    //平移  平移某个模型 还是平移视角  双指平移视角?
    public void translate() {

    }

    //旋转
    public void rotate(float distanceX, float distanceY) {
        rotateX = rotateX + distanceX;
        Log.i(TAG, "onScroll rotateX : " + rotateX + "  rotateY: " + rotateY);
        if (rotateY + distanceY > 90 * TOUCH_SCALE_AC || rotateY + distanceY < 0) {
            distanceY = 0;
        } else {
            rotateY = rotateY + distanceY;
        }

        Matrix.setRotateM(modelMatrix, 0, -rotateY / TOUCH_SCALE_AC, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, -rotateX / TOUCH_SCALE_AC, 0, 1, 0);
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

        for (Model model : modelList) {
            model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        }
        surfaceView.requestRender();
    }

    public void scale(float s) {
        //这样写可以造成一个缩放回弹的效果 回弹效果要在scaleEnd时重新设置回边界大小
        Matrix.scaleM(modelMatrix, 0, s, s, s);
        scale = scale * s;
        for (Model model : modelList) {
            model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        }
        surfaceView.requestRender();
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

        for (Model model : modelList) {
            model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        }
        surfaceView.requestRender();
    }

    public void setBgColor(int color){
        this.bgColor=color;
        float bgRed = Color.red(bgColor) / 255f;
        float bgGreen = Color.green(bgColor) / 255f;
        float bgBlue = Color.blue(bgColor) / 255f;
        float bgAlpha = Color.alpha(bgColor) / 255f;
        GLES30.glClearColor(bgRed, bgGreen, bgBlue, bgAlpha);
    }

    public void move(float distanceX ,float distanceY ,float distanceZ){
        eye[0]=eye[0]+distanceX;
        eye[1]=eye[1]+distanceY;
        eye[2]=eye[2]+distanceZ;
        Matrix.setLookAtM(mVMatrix, 0, eye[0],eye[1],eye[2],0,0,0,0,0,-1);
        for (Model model : modelList) {
            model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        }
        surfaceView.requestRender();
    }
    public void moveTo(float x ,float y ,float z){
        eye[0]=x;
        eye[1]=y;
        eye[2]=z;
        Matrix.setLookAtM(mVMatrix, 0, eye[0],eye[1],eye[2],0,0,0,0,0,-1);
        for (Model model : modelList) {
            model.setMatrix(modelMatrix, mVMatrix, mProjMatrix);
        }
        surfaceView.requestRender();
    }


}
