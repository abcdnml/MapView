package com.aaa.lib.map3d.model;


import com.aaa.lib.map3d.obj.Area3D;

public class AreaModel extends Model<Area3D> {

    private Area3D area3d;
    private int textureId;

    public AreaModel(Area3D area3d){
        setData(area3d);
    }

    public Area3D getData() {
        return area3d;
    }

    @Override
    public void clear() {
        super.clear();
    }

    public Area3D getArea3d() {
        return area3d;
    }

    public void setData(Area3D area3d) {
        this.area3d = area3d;
    }

    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
}
