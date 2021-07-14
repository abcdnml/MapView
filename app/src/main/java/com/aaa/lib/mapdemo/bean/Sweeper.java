package com.aaa.lib.mapdemo.bean;

import com.aaa.lib.map3d.obj.Obj3DData;

public class Sweeper {
    int x;
    int y;
    float scale = 1f;
    float rotation;
    Obj3DData data;

    private Sweeper(Builder builder) {

        setPosition(builder.x, builder.y);
        setScale(builder.scale);
        setRotation(builder.rotation);
        setData(builder.data);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
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

    public void setData(Obj3DData data) {
        this.data = data;
    }

    public static final class Builder {
        private int x;
        private int y;
        private float scale;
        private float rotation;
        private Obj3DData data;

        private Builder() {
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }


        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder rotation(float rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder data(Obj3DData data) {
            this.data = data;
            return this;
        }

        public Sweeper build() {
            return new Sweeper(this);
        }
    }
}
