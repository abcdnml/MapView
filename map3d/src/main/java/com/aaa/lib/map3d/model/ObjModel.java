package com.aaa.lib.map3d.model;


import com.aaa.lib.map3d.obj.MultiObj3D;

public class ObjModel extends Model<MultiObj3D> {

    private String name;

    public ObjModel() {

    }

    public ObjModel(MultiObj3D obj3DList) {
        setData(obj3DList);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
