package com.aaa.lib.map3d.imp;


import com.aaa.lib.map3d.model.ObjModel;
import com.aaa.lib.map3d.obj.Obj3DData;

public class FurnitureModel extends ObjModel {
    private int id;

    public FurnitureModel(Obj3DData obj3DData) {
        super();
        this.data = obj3DData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
