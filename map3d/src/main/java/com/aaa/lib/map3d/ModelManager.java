package com.aaa.lib.map3d;

import android.content.Context;

import com.aaa.lib.map3d.model.Model;

import java.util.ArrayList;
import java.util.List;

public class ModelManager {
    private volatile List<Model> modelList = new ArrayList<>();
    private ModelListener modelListener;

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

    public void setMatrix(float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        for (int i = 0; i < modelList.size(); i++) {
            modelList.get(i).setMatrix(mMatrix, vMatrix, pMatrix);
        }
    }

    public void onSurfaceCreate(Context context) {
        for (int i = 0; i < modelList.size(); i++) {
            modelList.get(i).onCreate(context);
        }
    }

    public void onDraw() {
        for (int i = 0; i < modelList.size(); i++) {
            modelList.get(i).onDraw();
        }
    }

}
