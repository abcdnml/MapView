package com.aaa.lib.map3d.model;


import com.aaa.lib.map3d.obj.Path3D;


public class LineModel extends Model<Path3D> {

    private Path3D data;

    public LineModel() {
        super();
    }

    public LineModel(Path3D path) {
        this();
        data = path;
    }

    public Path3D getData() {
        return data;
    }


    public void setData(Path3D path3D) {
        clear();
        this.data = path3D;
    }

    @Override
    public void clear() {

    }
}
