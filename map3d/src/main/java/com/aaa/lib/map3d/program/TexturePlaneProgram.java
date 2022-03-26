package com.aaa.lib.map3d.program;

import android.opengl.GLES20;
import android.opengl.GLES30;


import com.aaa.lib.map3d.model.PlaneModel;
import com.aaa.lib.map3d.obj.Plane3D;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class TexturePlaneProgram extends GLProgram<PlaneModel> {

    public TexturePlaneProgram() {
        createFromAsset(context, "shader/image.vert", "shader/image.frag");
    }

    @Override
    public void initLocation() {

    }

    @Override
    public void genVao(PlaneModel model) {
        Plane3D area3D = model.getPlane3D();

        int[] vaos = new int[1];
        int[] vbos = new int[2];
        GLES30.glGenVertexArrays(vaos.length, vaos, 0);
        GLES30.glGenBuffers(vbos.length, vbos, 0);

        GLES30.glBindVertexArray(vaos[0]);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, area3D.vertex.capacity() * 4, area3D.vertex, GLES30.GL_STATIC_DRAW);
        glVertexAttribPointer(glGetAttribLocation(programId, "position"), 3, GLES30.GL_FLOAT, false, 3 * 4, 0);
        GLES30.glEnableVertexAttribArray(glGetAttribLocation(programId, "position"));

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, area3D.texture.capacity() * 4, area3D.texture, GLES30.GL_STATIC_DRAW);
        glVertexAttribPointer(glGetAttribLocation(programId, "texCoords"), 2, GLES30.GL_FLOAT, false, 2 * 4, 0);
        GLES30.glEnableVertexAttribArray(glGetAttribLocation(programId, "texCoords"));

        GLES30.glBindVertexArray(0);

        model.setVao(vaos);
        model.setVbo(vbos);
        model.setTextureId(createTexture(area3D.bitmap));
    }

    public void draw(PlaneModel model, float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        glUseProgram(programId);

        //设置默认的帧缓冲
        glBindFramebuffer(GL_FRAMEBUFFER, GLES20.GL_NONE);
        glDisable(GLES30.GL_CULL_FACE);
        glEnable(GLES30.GL_BLEND);
        glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        //        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        float[] conbinedModelMatrix = conbineModelMatrix(model, mMatrix);
        glUniformMatrix4fv(glGetUniformLocation(programId, "mat_model"), 1, false, conbinedModelMatrix, 0);
        glUniformMatrix4fv(glGetUniformLocation(programId, "mat_view"), 1, false, vMatrix, 0);
        glUniformMatrix4fv(glGetUniformLocation(programId, "mat_proj"), 1, false, pMatrix, 0);

        Plane3D area3D = model.getPlane3D();

        //激活第 textureIndex 号纹理
        glActiveTexture(GL_TEXTURE0 + TEXTURE_INDEX_AREA);
        glBindTexture(GL_TEXTURE_2D, model.getTextureId());//绑定纹理
        glUniform1i(glGetUniformLocation(programId, "screenTexture"), TEXTURE_INDEX_AREA);

        //画
        GLES30.glBindVertexArray(model.getVao()[0]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, area3D.vertex.limit() / 3);
        GLES30.glBindVertexArray(0);

        glEnable(GLES30.GL_CULL_FACE);
        glDisable(GLES30.GL_BLEND);
    }

}
