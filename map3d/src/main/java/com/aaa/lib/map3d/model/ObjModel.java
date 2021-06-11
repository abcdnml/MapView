package com.aaa.lib.map3d.model;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.aaa.lib.map3d.obj.Obj3D;
import com.aaa.lib.map3d.utils.ShaderUtil;

import java.util.ArrayList;
import java.util.List;

public class ObjModel extends Model {
    static int programId;
    //模型放置到地图上时 本身需要做平移缩放旋转
    float scale = 1f;
    float offsetX = 0f;
    float offsetY = 0f;
    float offsetZ = 0f;
    float rotateX = 0f;
    float rotateY = 0f;
    float rotateZ = 0f;

    private int LOCATION_VETEX;
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

    private String vertexShaderCode;
    private String fragmentShaderCode;

    private float[] modelMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] normalMatrix = new float[16];
    private float[] tempmatrix = new float[16];
    private float[] eye = new float[9];
    private float[] light = new float[12];

    private List<Obj3D> obj3Ds;

    public ObjModel(Context context) {
        super(context);
        this.obj3Ds = new ArrayList<>();
    }

    public ObjModel(Context context, List<Obj3D> obj3Ds) {
        super(context);
        this.obj3Ds = obj3Ds;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setOffset(float offsetX, float offsetY, float offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public void setRotate(float rotateX, float rotateY, float rotateZ) {
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
    }

    @Override
    public void setMatrix(float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        System.arraycopy(mMatrix, 0, modelMatrix, 0, mMatrix.length);
        System.arraycopy(vMatrix, 0, mVMatrix, 0, vMatrix.length);
        System.arraycopy(pMatrix, 0, mProjMatrix, 0, pMatrix.length);

        Matrix.translateM(modelMatrix, 0, offsetX, offsetY, offsetZ);

        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

        //旋转要放到平移后面  旋转的中心点就在000  否则 ....
        Matrix.rotateM(modelMatrix, 0, rotateX, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, rotateY, 0, 1, 0);
        Matrix.rotateM(modelMatrix, 0, rotateZ, 0, 0, 1);


        Matrix.invertM(tempmatrix, 0, modelMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, tempmatrix, 0);
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
    public void onSurfaceCreate(Context context) {
        vertexShaderCode = ShaderUtil.loadFromAssetsFile("shader/obj_mtl.vert", context.getResources());
        fragmentShaderCode = ShaderUtil.loadFromAssetsFile("shader/obj_mtl.frag", context.getResources());
        programId = createGLProgram(vertexShaderCode, fragmentShaderCode);

        LOCATION_VETEX = GLES30.glGetAttribLocation(programId, "aPos");
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
        GLES30.glUseProgram(programId);
        //设置平行光源方向
        GLES30.glUniform3fv(LOCATION_LIGHT_DIR, 1, light, 0);
        GLES30.glUniform3fv(LOCATION_LIGHT_KA, 1, light, 3);
        GLES30.glUniform3fv(LOCATION_LIGHT_KD, 1, light, 6);
        GLES30.glUniform3fv(LOCATION_LIGHT_KS, 1, light, 9);
        //设置眼睛位置 用于计算镜面反射
        GLES30.glUniform3fv(LOCATION_EYE_POS, 1, eye, 0);

        for (Obj3D obj3D : obj3Ds) {
            draw3DObj(obj3D);
        }
    }

    private void draw3DObj(Obj3D obj3D) {

        //设置 顶点/纹理/法向量
        GLES30.glEnableVertexAttribArray(LOCATION_VETEX);
        GLES30.glVertexAttribPointer(LOCATION_VETEX, 3, GLES30.GL_FLOAT, false, 0, obj3D.vert);
        GLES30.glEnableVertexAttribArray(LOCATION_NORMAL);
        GLES30.glVertexAttribPointer(LOCATION_NORMAL, 3, GLES30.GL_FLOAT, false, 0, obj3D.vertNorl);
        if (obj3D.vertTexture != null) {
            GLES30.glEnableVertexAttribArray(LOCATION_TEXTURE);
            GLES30.glVertexAttribPointer(LOCATION_TEXTURE, 2, GLES30.GL_FLOAT, false, 0, obj3D.vertTexture);
        }

        //设置 模型矩阵/视图矩阵/投影矩阵/法向量变换矩阵
        GLES30.glUniformMatrix4fv(LOCATION_MAT_MODEL, 1, false, modelMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_VIEW, 1, false, mVMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_PROJ, 1, false, mProjMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_NORMAL, 1, false, normalMatrix, 0);

        //设置材质 环境光/漫反射/镜面反射/锋锐值
        GLES30.glUniform3fv(LOCATION_MTL_KA, 1, obj3D.mtl.Ka, 0);
        GLES30.glUniform3fv(LOCATION_MTL_KD, 1, obj3D.mtl.Kd, 0);
        GLES30.glUniform3fv(LOCATION_MTL_KS, 1, obj3D.mtl.Ks, 0);
        GLES30.glUniform1f(LOCATION_MTL_NS, obj3D.mtl.Ns);

        //绘制顶点
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, obj3D.vertCount);

        GLES30.glDisableVertexAttribArray(LOCATION_VETEX);
        GLES30.glDisableVertexAttribArray(LOCATION_NORMAL);
        GLES30.glDisableVertexAttribArray(LOCATION_TEXTURE);
    }

    @Override
    public void onSurfaceChange(int width, int height) {
        Matrix.invertM(tempmatrix, 0, modelMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, tempmatrix, 0);

    }

    public void setObj3D(List<Obj3D> obj3Ds) {
        if(obj3Ds==null){
            this.obj3Ds=new ArrayList<>();
        }else{
            this.obj3Ds = obj3Ds;
        }
    }
}
