package com.aaa.lib.mapdemo.imp;

import com.aaa.lib.map3d.obj.MultiObj3D;

public class Sweeper {
    int x;
    int y;
    float scale = 1f;
    float rotation;
    MultiObj3D data;

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

    public MultiObj3D getData() {
        return data;
    }

    public void setData(MultiObj3D data) {
        this.data = data;
    }

    public static final class Builder {
        private int x;
        private int y;
        private float scale;
        private float rotation;
        private MultiObj3D data;

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

        public Builder data(MultiObj3D data) {
            this.data = data;
            return this;
        }

        public Sweeper build() {
            return new Sweeper(this);
        }
    }
}
