package com.aaa.lib.map3d;

import android.opengl.GLES20;


import com.aaa.lib.map3d.model.AreaModel;
import com.aaa.lib.map3d.model.LineModel;
import com.aaa.lib.map3d.model.Model;
import com.aaa.lib.map3d.model.ObjModel;
import com.aaa.lib.map3d.obj.ModelData;
import com.aaa.lib.map3d.program.AreaProgram;
import com.aaa.lib.map3d.program.GLProgram;
import com.aaa.lib.map3d.program.LineProgram;
import com.aaa.lib.map3d.program.ModelProgram;
import com.aaa.lib.map3d.program.ShadowProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;

public class ModelManager {
    public ShadowProgram shadowProgram;
    public ModelProgram modelProgram;
    public LineProgram lineProgram;
    public AreaProgram areaProgram;

    private volatile List<Model> modelList = new ArrayList<>();

    private ModelListener modelListener;

    private Map3DSurfaceView worldSurfaceView;

    public ModelManager(Map3DSurfaceView view) {
        worldSurfaceView = view;
    }


    public void setModelListener(ModelListener listener) {
        modelListener = listener;
    }

    public void addModel(Model model) {
        if (model == null) {
            return;
        }

        modelList.add(model);
        if (modelListener != null) {
            modelListener.onModelAdd(model);
        }
    }

    public List<Model> getAllModel() {
        return modelList;
    }

    public List<Model> getModelByType(Class cls) {
        List<Model> models = new ArrayList<>();
        for (int i = 0; i < modelList.size(); i++) {
            Model model = modelList.get(i);
            if (model.getClass() == cls) {
                models.add(model);
            }
        }
        return models;
    }

    public List<Model> getObjModel() {
        List<Model> models = new ArrayList<>();
        for (int i = 0; i < modelList.size(); i++) {
            Model model = modelList.get(i);
            if (model instanceof ObjModel) {
                models.add(model);
            }
        }
        return models;
    }

    public void removeModels(List<Model> models) {
        if (models == null || models.size() == 0) {
            return;
        }
        for (int i = 0; i < models.size(); i++) {
            //TODO 这里遍历删除会有异常
            if (modelList.remove(models.get(i))) {
                if (modelListener != null) {
                    modelListener.onModelRemove(models.get(i));
                }
            }
        }
    }

    public void removeModel(Model model) {
        if (model == null) {
            return;
        }
        modelList.remove(model);
        if (modelListener != null) {
            modelListener.onModelRemove(model);
        }
    }

    public void clear() {
        //TODO 暂未实现
    }

    public void initModel() {
        modelProgram = new ModelProgram();
        shadowProgram = new ShadowProgram();
        lineProgram = new LineProgram();
        areaProgram = new AreaProgram();

        for (int i = 0; i < modelList.size(); i++) {
            Model model = modelList.get(i);
            initData(model);
        }
    }

    public void initData(Model model) {
        if (model instanceof ObjModel) {
            shadowProgram.genVao((ObjModel) model);
        }

        GLProgram glProgram = getProgram(model);
        glProgram.genVao(model);
    }

    public void updateData(Model model) {
        if (model instanceof ObjModel) {
            shadowProgram.genVao((ObjModel) model);
        }

        GLProgram glProgram = getProgram(model);
        glProgram.genVao(model);
    }

    public GLProgram getProgram(Model model) {
        if (model instanceof ObjModel) {
            return modelProgram;
        } else if (model instanceof LineModel) {
            return lineProgram;
        } else if (model instanceof AreaModel) {
            return areaProgram;
        }
        return null;
    }


    public void draw(float[] mMatrix, float[] vMatrix, float[] pMatrix, float[] shadowViewMatrix, float[] shadowProjMatrix) {
        //绘制阴影贴图
        List<Model> objModelList = getObjModel();
        shadowProgram.draw(objModelList, mMatrix, shadowViewMatrix, shadowProjMatrix);
        modelProgram.setShadowMap(shadowProgram.getShadowTexture());

        glBindFramebuffer(GL_FRAMEBUFFER, GLES20.GL_NONE);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, worldSurfaceView.getWidth(), worldSurfaceView.getHeight());
        //绘制模型
        for (int i = 0; i < modelList.size(); i++) {
            Model model = modelList.get(i);
            if (model instanceof ObjModel) {
                modelProgram.draw((ObjModel) model, mMatrix, vMatrix, pMatrix,
                        shadowViewMatrix, shadowProjMatrix,
                        worldSurfaceView.getLight(), worldSurfaceView.getEye());
            } else if (model instanceof LineModel) {
                lineProgram.draw((LineModel) model, mMatrix, vMatrix, pMatrix);
            } else if (model instanceof AreaModel) {
                areaProgram.draw((AreaModel) model, mMatrix, vMatrix, pMatrix);
            }
        }
    }

}
