package com.aaa.lib.map3d.model;


import com.aaa.lib.map3d.obj.Plane3D;

public class PlaneModel extends Model<Plane3D> {

    private Plane3D plane3D;
    private int textureId;

    public PlaneModel(Plane3D area3d){
        setData(area3d);
    }

    public Plane3D getData() {
        return plane3D;
    }

    @Override
    public void clear() {
        super.clear();
    }

    public Plane3D getPlane3D() {
        return plane3D;
    }

    public void setData(Plane3D plane3D) {
        this.plane3D = plane3D;
    }

    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
}
