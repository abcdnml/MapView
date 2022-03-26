package com.aaa.lib.map3d.program;

import android.opengl.Matrix;
import android.util.Log;

import com.aaa.lib.map3d.eye.Eye;
import com.aaa.lib.map3d.light.DirectLight;
import com.aaa.lib.map3d.light.Light;
import com.aaa.lib.map3d.model.ObjModel;
import com.aaa.lib.map3d.obj.MultiObj3D;
import com.aaa.lib.map3d.obj.Obj3D;

import java.util.List;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_NONE;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glGenVertexArrays;

public class ModelProgram extends GLProgram<ObjModel> {
    private int LOCATION_VERTEX;
    private int LOCATION_NORMAL;
    private int LOCATION_TEXTURE;
    private int LOCATION_MAT_MODEL;
    private int LOCATION_MAT_VIEW;
    private int LOCATION_MAT_PROJ;
    private int LOCATION_MAT_NORMAL;
    private int LOCATION_MAT_SHADOW_PROJ;
    private int LOCATION_MAT_SHADOW_VIEW;
    private int LOCATION_MAT_SHADOW_MAP;
    private int LOCATION_MTL_KA;
    private int LOCATION_MTL_KD;
    private int LOCATION_MTL_KS;
    private int LOCATION_MTL_NS;
    private int LOCATION_MTL_MAP_KD;
    private int LOCATION_MTL_MAP_KD_NORMAL;
    private int LOCATION_LIGHT_DIR;
    private int LOCATION_LIGHT_KA;
    private int LOCATION_LIGHT_KD;
    private int LOCATION_LIGHT_KS;
    private int LOCATION_EYE_POS;

    private int shadowMap = -1;

    private float[] normalMatrix = new float[16];
    private float[] tempMatrix = new float[16];


    public ModelProgram() {
        createFromAsset(context, "shader/shadow_test.vert", "shader/shadow_test.frag");
    }

    public void setShadowMap(int shadowMap) {
        this.shadowMap = shadowMap;
    }


    @Override
    public void initLocation() {
        LOCATION_VERTEX = glGetAttribLocation(programId, "v_position");
        LOCATION_TEXTURE = glGetAttribLocation(programId, "v_texCoords");
        LOCATION_NORMAL = glGetAttribLocation(programId, "v_normal");

        LOCATION_MAT_MODEL = glGetUniformLocation(programId, "mat_model");
        LOCATION_MAT_VIEW = glGetUniformLocation(programId, "mat_view");
        LOCATION_MAT_PROJ = glGetUniformLocation(programId, "mat_proj");
        LOCATION_MAT_NORMAL = glGetUniformLocation(programId, "mat_normal");

        //计算阴影需要使用的参数
        LOCATION_MAT_SHADOW_PROJ = glGetUniformLocation(programId, "mat_shadow_proj");
        LOCATION_MAT_SHADOW_VIEW = glGetUniformLocation(programId, "mat_shadow_view");
        LOCATION_MAT_SHADOW_MAP = glGetUniformLocation(programId, "shadowMap");

        LOCATION_MTL_KA = glGetUniformLocation(programId, "material.ambient");
        LOCATION_MTL_KD = glGetUniformLocation(programId, "material.diffuse");
        LOCATION_MTL_KS = glGetUniformLocation(programId, "material.specular");
        LOCATION_MTL_NS = glGetUniformLocation(programId, "material.shininess");
        LOCATION_MTL_MAP_KD = glGetUniformLocation(programId, "material.map_kd");
        LOCATION_MTL_MAP_KD_NORMAL = glGetUniformLocation(programId, "material.map_kd_normal");

        LOCATION_LIGHT_DIR = glGetUniformLocation(programId, "dirLight.direction");
        LOCATION_LIGHT_KA = glGetUniformLocation(programId, "dirLight.ambient");
        LOCATION_LIGHT_KD = glGetUniformLocation(programId, "dirLight.diffuse");
        LOCATION_LIGHT_KS = glGetUniformLocation(programId, "dirLight.specular");
        LOCATION_EYE_POS = glGetUniformLocation(programId, "eye");
    }

    public void draw(ObjModel model,
                     float[] mMatrix, float[] vMatrix, float[] pMatrix,
                     float[] shadowViewMatrix, float[] shadowProjMatrix,
                     Light light, Eye eye) {

        Log.i("model program ","draw model");

        MultiObj3D data = model.getData();
        if (data == null || data.getObj3DList() == null|| data.getObj3DList().size() == 0) {
            return;
        }
        glUseProgram(programId);

        //设置平行光源方向
        DirectLight directLight = (DirectLight) light;
        glUniform3fv(LOCATION_LIGHT_DIR, 1, directLight.dirction, 0);
        glUniform3fv(LOCATION_LIGHT_KA, 1, directLight.ka, 0);
        glUniform3fv(LOCATION_LIGHT_KD, 1, directLight.kd, 0);
        glUniform3fv(LOCATION_LIGHT_KS, 1, directLight.ks, 0);

        //设置眼睛位置 用于计算镜面反射
        glUniform3fv(LOCATION_EYE_POS, 1, eye.position, 0);

        float[] conbinedModelMatrix = conbineModelMatrix(model, mMatrix);
        //设置 模型矩阵/视图矩阵/投影矩阵/法向量变换矩阵
        glUniformMatrix4fv(LOCATION_MAT_MODEL, 1, false, conbinedModelMatrix, 0);
        glUniformMatrix4fv(LOCATION_MAT_VIEW, 1, false, vMatrix, 0);
        glUniformMatrix4fv(LOCATION_MAT_PROJ, 1, false, pMatrix, 0);
        glUniformMatrix4fv(LOCATION_MAT_SHADOW_PROJ, 1, false, shadowProjMatrix, 0);
        glUniformMatrix4fv(LOCATION_MAT_SHADOW_VIEW, 1, false, shadowViewMatrix, 0);

        Matrix.invertM(tempMatrix, 0, conbinedModelMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0);
        glUniformMatrix4fv(LOCATION_MAT_NORMAL, 1, false, normalMatrix, 0);

        //激活阴影贴图
        glActiveTexture(GL_TEXTURE0 + TEXTURE_INDEX_SHADOW);
        glBindTexture(GL_TEXTURE_2D, shadowMap);//绑定纹理
        glUniform1i(LOCATION_MAT_SHADOW_MAP, TEXTURE_INDEX_SHADOW);


        List<Obj3D> obj3DList = data.getObj3DList();
        for (int i = 0; i < obj3DList.size(); i++) {
            Obj3D obj3D = obj3DList.get(i);
            //设置每个obj的材质 环境光/漫反射/镜面反射/锋锐值
            glUniform3fv(LOCATION_MTL_KA, 1, obj3D.mtl.Ka, 0);
            glUniform3fv(LOCATION_MTL_KD, 1, obj3D.mtl.Kd, 0);
            glUniform3fv(LOCATION_MTL_KS, 1, obj3D.mtl.Ks, 0);
            glUniform1f(LOCATION_MTL_NS, obj3D.mtl.Ns);
            if (obj3D.mtl.kdBitmap != null) {
                glUniform1i(LOCATION_MTL_MAP_KD, TEXTURE_INDEX_KD);
                glActiveTexture(GL_TEXTURE0 + TEXTURE_INDEX_KD);//激活纹理
                glBindTexture(GL_TEXTURE_2D, obj3D.textureKd);//绑定纹理
            }
            if (obj3D.mtl.kdNormalBitmap != null) {
                glUniform1i(LOCATION_MTL_MAP_KD_NORMAL, TEXTURE_INDEX_KD_NORMAL);
                glActiveTexture(GL_TEXTURE0 + TEXTURE_INDEX_KD_NORMAL);//激活纹理
                glBindTexture(GL_TEXTURE_2D, obj3D.textureKd_normal);//绑定纹理
            }

            //使用VAO绘制
            drawVAO(obj3D.modelVao, obj3D);
        }
    }

    private void drawVAO(int vao, Obj3D obj3D) {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, obj3D.position.limit() / 3);
        glBindVertexArray(GL_NONE);
    }

    @Override
    public void genVao(ObjModel model) {
        model.clear();

        MultiObj3D data = model.getData();
        if (data == null) {
            return;
        }
        List<Obj3D> obj3DList = data.getObj3DList();
        if (obj3DList == null || obj3DList.size() == 0) {
            return;
        }

        model.vao = new int[obj3DList.size()];
        model.vbo = new int[obj3DList.size() * 3];

        glGenVertexArrays(model.vao.length, model.vao, 0);
        glGenBuffers(model.vbo.length, model.vbo, 0);
        for (int i = 0; i < obj3DList.size(); i++) {
            Obj3D obj3D = obj3DList.get(i);
            obj3D.modelVao = model.vao[i];
            obj3D.textureKd = createTexture(obj3DList.get(i).mtl.kdBitmap);
            obj3D.textureKd_normal = createTexture(obj3DList.get(i).mtl.kdNormalBitmap);

            glBindVertexArray(model.vao[i]);

            glBindBuffer(GL_ARRAY_BUFFER, model.vbo[i * 3]);
            glBufferData(GL_ARRAY_BUFFER, obj3D.position.capacity() * 4, obj3D.position, GL_STATIC_DRAW);
            glVertexAttribPointer(LOCATION_VERTEX, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(LOCATION_VERTEX);

            glBindBuffer(GL_ARRAY_BUFFER, model.vbo[i * 3 + 1]);
            glBufferData(GL_ARRAY_BUFFER, obj3D.normal.capacity() * 4, obj3D.normal, GL_STATIC_DRAW);
            glVertexAttribPointer(LOCATION_NORMAL, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(LOCATION_NORMAL);

            if (obj3D.texture != null) {
                glBindBuffer(GL_ARRAY_BUFFER, model.vbo[i * 3 + 2]);
                glBufferData(GL_ARRAY_BUFFER, obj3D.texture.capacity() * 4, obj3D.texture, GL_STATIC_DRAW);
                glVertexAttribPointer(LOCATION_TEXTURE, 2, GL_FLOAT, false, 0, 0);
                glEnableVertexAttribArray(LOCATION_TEXTURE);
            }
            glBindVertexArray(GL_NONE);
        }
    }

}
