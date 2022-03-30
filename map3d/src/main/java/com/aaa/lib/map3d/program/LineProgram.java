package com.aaa.lib.map3d.program;

import android.opengl.GLES30;
import android.util.Log;

import com.aaa.lib.map3d.model.LineModel;
import com.aaa.lib.map3d.obj.Path3D;

public class LineProgram extends GLProgram<LineModel> {

    private int LOCATION_VERTEX;
    private int LOCATION_MAT_COLOR;
    private int LOCATION_MAT_MODEL;
    private int LOCATION_MAT_VIEW;
    private int LOCATION_MAT_PROJ;

    public LineProgram() {
        createFromAsset(context, "shader/path.vert", "shader/path.frag");
    }

    @Override
    public void initLocation() {
        LOCATION_VERTEX = GLES30.glGetAttribLocation(programId, "aPos");
        LOCATION_MAT_COLOR = GLES30.glGetUniformLocation(programId, "color");
        LOCATION_MAT_MODEL = GLES30.glGetUniformLocation(programId, "model");
        LOCATION_MAT_VIEW = GLES30.glGetUniformLocation(programId, "view");
        LOCATION_MAT_PROJ = GLES30.glGetUniformLocation(programId, "projection");
    }

    public void draw(LineModel model, float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        Log.e(this.getClass().getSimpleName(), "draw path Program id " + programId);
        if (programId == 0) {
            return;
        }
        if (model == null || model.getData() == null) {
            return;
        }

        GLES30.glUseProgram(programId);
        GLES30.glLineWidth(4);

        float[] conbinedModelMatrix = conbineModelMatrix(model, mMatrix);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_MODEL, 1, false, conbinedModelMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_VIEW, 1, false, vMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_PROJ, 1, false, pMatrix, 0);

        Path3D path3D = model.getData();
        GLES30.glUniform3fv(LOCATION_MAT_COLOR, 1, path3D.color, 0);
        GLES30.glBindVertexArray(model.getVao()[0]);
        GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, path3D.vertCount);
        GLES30.glBindVertexArray(GLES30.GL_NONE);

    }

    @Override
    public void genVao(LineModel model) {

        model.clear();
        Path3D path3D = model.getData();
        if (path3D == null) {
            return;
        }
        int[] vao = new int[1];
        int[] vbo = new int[1];
        GLES30.glGenVertexArrays(vao.length, vao, 0);
        GLES30.glBindVertexArray(vao[0]);

        GLES30.glGenBuffers(vbo.length, vbo, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, path3D.vert.capacity() * 4, path3D.vert, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(LOCATION_VERTEX, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(LOCATION_VERTEX);

        GLES30.glBindVertexArray(GLES30.GL_NONE);

        model.setVao(vao);
        model.setVbo(vbo);
    }

    public void onDestroy() {
        GLES30.glDeleteProgram(programId);
    }


}
