package com.aaa.lib.map3d.imp;

import com.aaa.lib.map3d.obj.Obj3DData;

public class Furniture {
    int id;
    int type;
    int x;
    int y;
    float scale = 1f;
    float rotation;
    Obj3DData data;

    public Furniture() {

    }

    private Furniture(Builder builder) {
        id = builder.id;
        type = builder.type;
        scale = builder.scale;
        x = builder.x;
        y = builder.y;
        rotation = builder.rotation;
        data = builder.multiObj;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public Obj3DData getData() {
        return data;
    }

    public void setData(Obj3DData multiObj) {
        this.data = multiObj;
    }

    public static final class Builder {
        private int id;
        private int type;
        private int x;
        private int y;
        private float scale = 1f;
        private float rotation;
        private Obj3DData multiObj;

        private Builder() {
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder rotation(int rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder objData(Obj3DData multiObj) {
            this.multiObj = multiObj;
            return this;
        }

        public Furniture build() {

            return new Furniture(this);
        }
    }
}
