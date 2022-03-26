package com.aaa.lib.map3d.model;


import com.aaa.lib.map3d.obj.MultiObj3D;

public class ObjModel extends Model<MultiObj3D> {

    public ObjModel() {

    }

    public ObjModel(MultiObj3D obj3DList) {
        setData(obj3DList);
    }

}
