package com.aaa.lib.map3d.model;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import android.util.SparseArray;

import com.aaa.lib.map3d.obj.ModelData;
import com.aaa.lib.map3d.obj.Obj3D;
import com.aaa.lib.map3d.obj.Obj3DData;
import com.aaa.lib.map3d.utils.ShaderUtil;

import java.util.Arrays;

public class ObjModel extends Model {
    protected Obj3DData data;
    private int[] vaos;
    private int[] vbos;
    private int LOCATION_VERTEX;
    private int LOCATION_NORMAL;
    private int LOCATION_TEXTURE;
    private int LOCATION_MAT_MODEL;
    private int LOCATION_MAT_VIEW;
    private int LOCATION_MAT_PROJ;
    private int LOCATION_MAT_NORMAL;
    private int LOCATION_MTL_KA;
    private int LOCATION_MTL_KD;
    private int LOCATION_MTL_KS;
    private int LOCATION_MTL_NS;
    private int LOCATION_LIGHT_DIR;
    private int LOCATION_LIGHT_KA;
    private int LOCATION_LIGHT_KD;
    private int LOCATION_LIGHT_KS;
    private int LOCATION_EYE_POS;
    private float[] normalMatrix = new float[16];
    private float[] tempMatrix = new float[16];
    private float[] eye = new float[9];
    private float[] light = new float[12];
    private SparseArray<Obj3D> objVaoArray = new SparseArray<>();

    public ObjModel() {
        super();
    }

    @Override
    public void setMatrix(float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        System.arraycopy(mMatrix, 0, this.mMatrix, 0, mMatrix.length);
        System.arraycopy(vMatrix, 0, this.vMatrix, 0, vMatrix.length);
        System.arraycopy(pMatrix, 0, this.pMatrix, 0, pMatrix.length);

        //先做模型本身的 平移/旋转/缩放, 顺序不能乱
        Matrix.translateM(this.mMatrix, 0, offset[0], offset[1], offset[2]);

        Matrix.rotateM(this.mMatrix, 0, rotate[0], 1, 0, 0);
        Matrix.rotateM(this.mMatrix, 0, rotate[1], 0, 1, 0);
        Matrix.rotateM(this.mMatrix, 0, rotate[2], 0, 0, 1);

        Matrix.scaleM(this.mMatrix, 0, scale[0], scale[1], scale[2]);

        Matrix.invertM(tempMatrix, 0, this.mMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0);
    }

    @Override
    public void setEye(float[] eye) {
        this.eye = eye;
    }

    @Override
    public void setLight(float[] light) {
        this.light = light;
    }

    @Override
    public void onCreate(Context context) {
        vertexShaderCode = ShaderUtil.loadFromAssetsFile("shader/obj_mtl.vert", context.getResources());
        fragmentShaderCode = ShaderUtil.loadFromAssetsFile("shader/obj_mtl.frag", context.getResources());
        programId = ShaderUtil.createProgram(vertexShaderCode, fragmentShaderCode);
        initLocation();
        initVAO();
    }


    private void initLocation() {
        LOCATION_VERTEX = GLES30.glGetAttribLocation(programId, "aPos");
        LOCATION_NORMAL = GLES30.glGetAttribLocation(programId, "aNormal");
        LOCATION_TEXTURE = GLES30.glGetAttribLocation(programId, "aTexCoords");

        LOCATION_MAT_MODEL = GLES30.glGetUniformLocation(programId, "model");
        LOCATION_MAT_VIEW = GLES30.glGetUniformLocation(programId, "view");
        LOCATION_MAT_PROJ = GLES30.glGetUniformLocation(programId, "projection");
        LOCATION_MAT_NORMAL = GLES30.glGetUniformLocation(programId, "normal_matrix");

        LOCATION_MTL_KA = GLES30.glGetUniformLocation(programId, "material.ambient");
        LOCATION_MTL_KD = GLES30.glGetUniformLocation(programId, "material.diffuse");
        LOCATION_MTL_KS = GLES30.glGetUniformLocation(programId, "material.specular");
        LOCATION_MTL_NS = GLES30.glGetUniformLocation(programId, "material.shininess");

        LOCATION_LIGHT_DIR = GLES30.glGetUniformLocation(programId, "dirLight.direction");
        LOCATION_LIGHT_KA = GLES30.glGetUniformLocation(programId, "dirLight.ambient");
        LOCATION_LIGHT_KD = GLES30.glGetUniformLocation(programId, "dirLight.diffuse");
        LOCATION_LIGHT_KS = GLES30.glGetUniformLocation(programId, "dirLight.specular");

        LOCATION_EYE_POS = GLES30.glGetUniformLocation(programId, "viewPos");

    }

    @Override
    public void onDraw() {
        Log.e(this.getClass().getSimpleName(), "draw obj Program id " + programId);
        GLES30.glUseProgram(programId);
        //设置平行光源方向
        GLES30.glUniform3fv(LOCATION_LIGHT_DIR, 1, light, 0);
        GLES30.glUniform3fv(LOCATION_LIGHT_KA, 1, light, 3);
        GLES30.glUniform3fv(LOCATION_LIGHT_KD, 1, light, 6);
        GLES30.glUniform3fv(LOCATION_LIGHT_KS, 1, light, 9);
        //设置眼睛位置 用于计算镜面反射
        GLES30.glUniform3fv(LOCATION_EYE_POS, 1, eye, 0);

        //设置 模型矩阵/视图矩阵/投影矩阵/法向量变换矩阵
        GLES30.glUniformMatrix4fv(LOCATION_MAT_MODEL, 1, false, mMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_VIEW, 1, false, vMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_PROJ, 1, false, pMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_NORMAL, 1, false, normalMatrix, 0);

        for (int i = 0; i < objVaoArray.size(); i++) {
            int vao = objVaoArray.keyAt(i);
            Obj3D obj3D = objVaoArray.get(vao);
            //设置每个obj的材质 环境光/漫反射/镜面反射/锋锐值
            GLES30.glUniform3fv(LOCATION_MTL_KA, 1, obj3D.mtl.Ka, 0);
            GLES30.glUniform3fv(LOCATION_MTL_KD, 1, obj3D.mtl.Kd, 0);
            GLES30.glUniform3fv(LOCATION_MTL_KS, 1, obj3D.mtl.Ks, 0);
            GLES30.glUniform1f(LOCATION_MTL_NS, obj3D.mtl.Ns);

            //使用VAO绘制
            drawVAO(vao, obj3D.vertCount);
        }
    }

    /**
     * 此方法必须在GLThread中执行
     *
     * @param modelData
     */
    @Override
    public void updateModelData(ModelData modelData) {
        clearVAO();
        objVaoArray.clear();
        data = (Obj3DData) modelData;

        initVAO();
    }

    public void clearVAO() {
        if (vbos != null) {
            GLES30.glDeleteBuffers(vbos.length, vbos, 0);
        }

        if (vaos != null) {
            GLES30.glDeleteVertexArrays(objVaoArray.size(), vaos, 0);
        }
    }

    private void initVAO() {
        if (data == null || data.getObj3DList() == null || data.getObj3DList().size() == 0) {
            return;
        }

        if (programId != 0) {
            vaos = new int[data.getObj3DList().size()];
            vbos = new int[vaos.length * 3];
            GLES30.glGenVertexArrays(vaos.length, vaos, 0);
            GLES30.glGenBuffers(vbos.length, vbos, 0);
            for (int i = 0; i < data.getObj3DList().size(); i++) {
                objVaoArray.put(vaos[i], data.getObj3DList().get(i));
                fillVAO(i, vaos[i], data.getObj3DList().get(i));
            }
            Log.i("set obj  ", "vao :" + Arrays.toString(vaos));
            Log.i("set obj  ", "vbo :" + Arrays.toString(vbos));
        }
    }

    private void fillVAO(int i, int vao, Obj3D obj3D) {
        GLES30.glBindVertexArray(vao);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[i * 3]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, obj3D.vert.capacity() * 4, obj3D.vert, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(LOCATION_VERTEX, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(LOCATION_VERTEX);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[i * 3 + 1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, obj3D.vertNorl.capacity() * 4, obj3D.vertNorl, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(LOCATION_NORMAL, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(LOCATION_NORMAL);

        if (obj3D.vertTexture != null) {
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vbos[i * 3 + 2]);
            GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, obj3D.vertTexture.capacity() * 4, obj3D.vertTexture, GLES30.GL_STATIC_DRAW);
            GLES30.glEnableVertexAttribArray(LOCATION_TEXTURE);
            GLES30.glVertexAttribPointer(LOCATION_TEXTURE, 2, GLES30.GL_FLOAT, false, 0, 0);
        }
        GLES30.glBindVertexArray(GLES30.GL_NONE);
    }

    private void drawVAO(int vao, int vertCount) {
        GLES30.glBindVertexArray(vao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertCount);
        GLES30.glBindVertexArray(GLES30.GL_NONE);
    }


    public Obj3DData getData() {
        return data;
    }

    public void onDestory() {
        objVaoArray.clear();
        data.clear();
        clearVAO();
        GLES30.glDeleteProgram(programId);
        programId = 0;
    }


}
