package com.aaa.lib.map3d.model;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.aaa.lib.map3d.obj.Path3D;
import com.aaa.lib.map3d.utils.ShaderUtil;

public class PathModel extends Model {

    private int LOCATION_VETEX;
    private int LOCATION_MAT_COLOR;
    private int LOCATION_MAT_MODEL;
    private int LOCATION_MAT_VIEW;
    private int LOCATION_MAT_PROJ;

    private int[] vao = new int[1];

    private float[] modelMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];

    private Path3D path3D;

    public PathModel(Context context) {
        super(context);
        setPath3D(null);
    }

    public PathModel(Context context, Path3D path3D) {
        super(context);
        setPath3D(path3D);
    }

    @Override
    public void setMatrix(float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        System.arraycopy(mMatrix, 0, modelMatrix, 0, mMatrix.length);
        System.arraycopy(vMatrix, 0, mVMatrix, 0, vMatrix.length);
        System.arraycopy(pMatrix, 0, mProjMatrix, 0, pMatrix.length);
    }

    @Override
    public void onSurfaceCreate(Context context) {
        vertexShaderCode = ShaderUtil.loadFromAssetsFile("shader/path.vert", context.getResources());
        fragmentShaderCode = ShaderUtil.loadFromAssetsFile("shader/path.frag", context.getResources());
        programId = createGLProgram(vertexShaderCode, fragmentShaderCode);

        initLocation();
    }

    private void initLocation() {
        LOCATION_VETEX = GLES30.glGetAttribLocation(programId, "aPos");
        LOCATION_MAT_COLOR = GLES30.glGetUniformLocation(programId, "color");
        LOCATION_MAT_MODEL = GLES30.glGetUniformLocation(programId, "model");
        LOCATION_MAT_VIEW = GLES30.glGetUniformLocation(programId, "view");
        LOCATION_MAT_PROJ = GLES30.glGetUniformLocation(programId, "projection");
    }

    @Override
    public void onDraw() {
        Log.e(this.getClass().getSimpleName(), "draw Program id " + programId);
        if (programId == 0) {
            Log.e(this.getClass().getSimpleName(), "Program id is 0 ,may not init");
            return;
        }
        if (path3D == null || path3D.vertCount < 1) {
            return;
        }
        GLES30.glUseProgram(programId);
        GLES30.glLineWidth(4);
        GLES30.glUniform3fv(LOCATION_MAT_COLOR, 1, path3D.color, 0);

        GLES30.glUniformMatrix4fv(LOCATION_MAT_MODEL, 1, false, modelMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_VIEW, 1, false, mVMatrix, 0);
        GLES30.glUniformMatrix4fv(LOCATION_MAT_PROJ, 1, false, mProjMatrix, 0);

        drawWithVAO();
    }

    @Override
    public void onSurfaceChange(int width, int height) {

    }

    public void setPath3D(Path3D path3D) {
        this.path3D = path3D;
        initVAO();
    }

    private void initVAO() {
        if (path3D == null || path3D.vertCount < 1) {
            return;
        }

        GLES30.glGenVertexArrays(1, vao, 0);

        GLES30.glBindVertexArray(vao[0]);
        int[] vbo = new int[1];
        GLES30.glGenBuffers(1, vbo, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, path3D.vert.capacity() * 4, path3D.vert, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(LOCATION_VETEX, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(LOCATION_VETEX);

        GLES30.glBindVertexArray(GLES30.GL_NONE);
    }

    private void drawWithVAO() {
        GLES30.glBindVertexArray(vao[0]);
        GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, path3D.vertCount);
        GLES30.glBindVertexArray(GLES30.GL_NONE);
    }
}
