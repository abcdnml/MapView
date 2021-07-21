package com.aaa.lib.map3d.model;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.aaa.lib.map3d.obj.ModelData;
import com.aaa.lib.map3d.obj.Path3DData;
import com.aaa.lib.map3d.utils.ShaderUtil;

public class LineModel extends Model {

    private int LOCATION_VERTEX;
    private int LOCATION_MAT_COLOR;
    private int LOCATION_MAT_MODEL;
    private int LOCATION_MAT_VIEW;
    private int LOCATION_MAT_PROJ;

    private int[] vao = new int[1];
    private int[] vbo = new int[1];

    private Path3DData data;

    public LineModel() {
        super();
    }

    @Override
    public void setMatrix(float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        System.arraycopy(mMatrix, 0, this.mMatrix, 0, mMatrix.length);
        System.arraycopy(vMatrix, 0, this.vMatrix, 0, vMatrix.length);
        System.arraycopy(pMatrix, 0, this.pMatrix, 0, pMatrix.length);
    }

    @Override
    public void onCreate(Context context) {
        vertexShaderCode = ShaderUtil.loadFromAssetsFile("shader/path.vert", context.getResources());
        fragmentShaderCode = ShaderUtil.loadFromAssetsFile("shader/path.frag", context.getResources());
        programId = ShaderUtil.createProgram(vertexShaderCode, fragmentShaderCode);

        initLocation();

        updateModelData(data);
    }

    private void initLocation() {
        LOCATION_VERTEX = GLES30.glGetAttribLocation(programId, "aPos");
        LOCATION_MAT_COLOR = GLES30.glGetUniformLocation(programId, "color");
        LOCATION_MAT_MODEL = GLES30.glGetUniformLocation(programId, "model");
        LOCATION_MAT_VIEW = GLES30.glGetUniformLocation(programId, "view");
        LOCATION_MAT_PROJ = GLES30.glGetUniformLocation(programId, "projection");
    }

    @Override
    public void onDraw() {
        Log.e(this.getClass().getSimpleName(), "draw path Program id " + programId);
        if (programId == 0) {
            return;
        }
        if (data == null || data.vertCount < 1) {
            return;
        }
        GLES30.glUseProgram(programId);
        GLES30.glLineWidth(4);
        GLES30.glUniform3fv(LOCATION_MAT_COLOR, 1, data.color, 0);

        GLES30.glUniformMatrix4fv(LOCATION_MAT_MODEL, 1, false, mMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_VIEW, 1, false, vMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_PROJ, 1, false, pMatrix, 0);

        drawWithVAO();
    }

    @Override
    public void updateModelData(ModelData path3D) {
        clearVAO();
        this.data = (Path3DData) path3D;
        if (this.data == null || this.data.vertCount < 1) {
            return;
        }

        initVAO();
    }
    @Override
    public void onDestroy() {
        data.clear();
        clearVAO();
        GLES30.glDeleteProgram(programId);
    }


    private void clearVAO() {
        GLES30.glDeleteBuffers(vbo.length, vbo, 0);
        GLES30.glDeleteVertexArrays(vao.length, vao, 0);
    }

    private void initVAO() {
        GLES30.glGenVertexArrays(1, vao, 0);
        GLES30.glBindVertexArray(vao[0]);

        GLES30.glGenBuffers(1, vbo, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.vert.capacity() * 4, data.vert, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(LOCATION_VERTEX, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(LOCATION_VERTEX);

        GLES30.glBindVertexArray(GLES30.GL_NONE);
    }

    private void drawWithVAO() {
        GLES30.glBindVertexArray(vao[0]);
        GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, data.vertCount);
        GLES30.glBindVertexArray(GLES30.GL_NONE);
    }


}
