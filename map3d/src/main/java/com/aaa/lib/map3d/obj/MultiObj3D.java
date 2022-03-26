package com.aaa.lib.map3d.obj;

import java.util.List;

public class MultiObj3D extends ModelData {
    List<Obj3D> obj3DList;
    float top;
    float bottom;

    public List<Obj3D> getObj3DList() {
        return obj3DList;
    }

    public void setObj3DList(List<Obj3D> obj3DList) {
        this.obj3DList = obj3DList;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    public float getModelHeigh() {
        return top - bottom;
    }

    public void clear() {
        if (obj3DList != null) {
            obj3DList.clear();
        }
    }
}
