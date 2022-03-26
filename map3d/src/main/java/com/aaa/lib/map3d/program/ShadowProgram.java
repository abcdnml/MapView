package com.aaa.lib.map3d.program;

import android.opengl.GLES20;
import android.util.Log;

import com.aaa.lib.map3d.model.Model;
import com.aaa.lib.map3d.model.ObjModel;
import com.aaa.lib.map3d.obj.MultiObj3D;
import com.aaa.lib.map3d.obj.Obj3D;

import java.util.List;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_FRONT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_NONE;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_RGBA4;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glCheckFramebufferStatus;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFramebufferRenderbuffer;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenRenderbuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glRenderbufferStorage;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glGenVertexArrays;

public class ShadowProgram extends GLProgram<ObjModel> {
    private int[] shadowVaos;
    private int[] shadowVbos;
    private int[] fbos;
    private int[] shadowTexture;
    private int LOCATION_SHADOW_POSITION;
    private int LOCATION_SHADOW_MODEL;
    private int LOCATION_SHADOW_VIEW;
    private int LOCATION_SHADOW_PROJ;

    public ShadowProgram() {
        createFromAsset(context, "shader/shadow.vert", "shader/shadow.frag");
        createDepathBuffer();
    }

    @Override
    public void initLocation() {
        LOCATION_SHADOW_POSITION = glGetAttribLocation(programId, "v_position");
        LOCATION_SHADOW_MODEL = glGetUniformLocation(programId, "mat_model");
        LOCATION_SHADOW_VIEW = glGetUniformLocation(programId, "mat_view");
        LOCATION_SHADOW_PROJ = glGetUniformLocation(programId, "mat_proj");
    }

    @Override
    public void genVao(ObjModel model) {
        MultiObj3D multiObj3D = model.getData();
        Log.i("shadowprogram", " genvao" + multiObj3D);
        model.clear();

        if (multiObj3D == null) {
            return;
        }
        List<Obj3D> data = multiObj3D.getObj3DList();
        if (data == null || data.size() == 0) {
            return;
        }

        shadowVaos = new int[data.size()];
        shadowVbos = new int[data.size()];
        glGenVertexArrays(shadowVaos.length, shadowVaos, 0);
        glGenBuffers(shadowVbos.length, shadowVbos, 0);
        for (int i = 0; i < data.size(); i++) {
            Obj3D obj3D = data.get(i);
            obj3D.shadowVao = shadowVaos[i];
            Log.i("shadowprogram", " genvao : " + obj3D.shadowVao);
            glBindVertexArray(shadowVaos[i]);
            glBindBuffer(GL_ARRAY_BUFFER, shadowVbos[i]);
            glBufferData(GL_ARRAY_BUFFER, obj3D.position.capacity() * 4, obj3D.position, GL_STATIC_DRAW);
            glVertexAttribPointer(LOCATION_SHADOW_POSITION, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(LOCATION_SHADOW_POSITION);
            glBindVertexArray(GL_NONE);
        }
    }


    public void draw(List<Model> modelList, float[] mMatrix, float[] shadowViewMatrix, float[] shadowProjeMatrix) {
        //        List<Model> modelList = ((ShadowModel) shadowModel).getModels();
        Log.i("shadowprogram", "shadowprogram draw modelList size : " + modelList.size());
        glUseProgram(programId);
        glBindFramebuffer(GL_FRAMEBUFFER, fbos[0]);
        glViewport(0, 0, 1024, 1024);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glCullFace(GL_FRONT);        //添加正面剔除 防止悬空
        for (int i = 0; i < modelList.size(); i++) {
            Model model = modelList.get(i);
            if (model instanceof ObjModel) {
                drawShadow((ObjModel) model, mMatrix, shadowViewMatrix, shadowProjeMatrix);
            }
        }
        glCullFace(GL_BACK);// 不要忘记设回原先的culling face
    }

    public void drawShadow(ObjModel model, float[] mMatrix, float[] shadowViewMatrix, float[] shadowProjeMatrix) {
        Log.i("shadowprogram", "shadowprogram drawShadow");
        MultiObj3D multiObj3D = model.getData();
        if (multiObj3D == null) {
            return;
        }
        Log.i("shadowprogram", "shadowprogram drawShadow MultiObj3D size" + multiObj3D.getObj3DList().size());
        List<Obj3D> objList = multiObj3D.getObj3DList();
        float[] shadowModelMatrix = conbineModelMatrix(model, mMatrix);

        glUniformMatrix4fv(LOCATION_SHADOW_MODEL, 1, false, shadowModelMatrix, 0);
        glUniformMatrix4fv(LOCATION_SHADOW_VIEW, 1, false, shadowViewMatrix, 0);
        glUniformMatrix4fv(LOCATION_SHADOW_PROJ, 1, false, shadowProjeMatrix, 0);
        for (int i = 0; i < objList.size(); i++) {
            //使用VAO绘制
            Obj3D obj3D = objList.get(i);
            Log.i("shadowprogram", "shadowprogram drawShadow draw vao " + obj3D.shadowVao);
            glBindVertexArray(obj3D.shadowVao);

            glDrawArrays(GL_TRIANGLES, 0, obj3D.position.limit() / 3);
            glBindVertexArray(GL_NONE);
        }
    }

    /**
     * 创建深度贴图buffer
     * @return
     */
    private int createDepathBuffer() {
        fbos = new int[1];
        glGenFramebuffers(1, fbos, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, fbos[0]);

        int[] renderBuffer = new int[1];
        glGenRenderbuffers(1, renderBuffer, 0);
        glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[0]);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA4, 1024, 1024);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBuffer[0]);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        //生成纹理ID
        shadowTexture = new int[1];
        glGenTextures(1, shadowTexture, 0);
        glBindTexture(GL_TEXTURE_2D, shadowTexture[0]);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, 1024, 1024, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTexture[0], 0);
        glBindTexture(GL_TEXTURE_2D, 0);


        //        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTexture[0], 0);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            // framebuffer生成失败
            Log.e("shadow program", " framebuffer生成失败 " + status);
        } else {
            Log.i("shadow program", " framebuffer生成成功 " + fbos[0]);
        }

        // 取消绑定纹理
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return shadowTexture[0];
    }

    public int getShadowTexture() {
        return shadowTexture[0];
    }
}
