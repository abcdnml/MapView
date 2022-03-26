package com.aaa.lib.map3d.imp;


import com.aaa.lib.map3d.model.ObjModel;
import com.aaa.lib.map3d.obj.MultiObj3D;

public class FurnitureModel extends ObjModel {
    private int id;

    public FurnitureModel(MultiObj3D multiObj3D) {
        super(multiObj3D);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
