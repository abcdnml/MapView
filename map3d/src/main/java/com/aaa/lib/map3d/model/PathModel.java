package com.aaa.lib.map3d.model;

import android.content.Context;
import android.opengl.GLES30;

import com.aaa.lib.map3d.obj.Path3D;
import com.aaa.lib.map3d.utils.ShaderUtil;

public class PathModel extends Model {

    private float[] modelMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];

    private Path3D path3D;

    public PathModel(Context context) {
        super(context);
        path3D = null;
    }

    public PathModel(Context context, Path3D path3D) {
        super(context);
        this.path3D = path3D;
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
    }

    @Override
    public void onDraw() {
        if(path3D==null){
            return;
        }

        GLES30.glUseProgram(programId);
        int location = GLES30.glGetAttribLocation(programId, "aPos");
        GLES30.glEnableVertexAttribArray(location);
        GLES30.glVertexAttribPointer(location, 3, GLES30.GL_FLOAT, false, 0, path3D.vert);
        GLES30.glLineWidth(4);

        location = GLES30.glGetUniformLocation(programId, "color");
        GLES30.glUniform3fv(location, 1, path3D.color, 0);

        location = GLES30.glGetUniformLocation(programId, "model");
        GLES30.glUniformMatrix4fv(location, 1, false, modelMatrix, 0);
        location = GLES30.glGetUniformLocation(programId, "view");
        GLES30.glUniformMatrix4fv(location, 1, false, mVMatrix, 0);
        location = GLES30.glGetUniformLocation(programId, "projection");
        GLES30.glUniformMatrix4fv(location, 1, false, mProjMatrix, 0);

        GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, path3D.vertCount);

        GLES30.glDisableVertexAttribArray(GLES30.glGetAttribLocation(programId, "aPos"));
    }

    @Override
    public void onSurfaceChange(int width, int height) {

    }

    public void setPath3D(Path3D path3D) {
        this.path3D = path3D;
    }
}
