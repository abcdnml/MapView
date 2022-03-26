package com.aaa.lib.map3d.obj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Path3D extends ModelData {
    public FloatBuffer vert;
    public float[] color;
    public int vertCount;

    private Path3D(Builder builder) {
        setVert(builder.vert);
        setColor(builder.color);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void clear() {
        vert.clear();
        vertCount = 0;
    }

    public void setVert(ArrayList<Float> data) {
        int size = data.size();
        vert = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < size; i++) {
            vert.put(data.get(i));
        }
        vert.flip();
        vertCount = size / 3;
    }

    public void setVert(FloatBuffer buffer) {
        vert = buffer;
        vertCount = vert.capacity() / 3;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public static final class Builder {
        private FloatBuffer vert;
        private float[] color;

        private Builder() {
        }

        public Builder vert(FloatBuffer vert) {
            this.vert = vert;
            return this;
        }

        public Builder color(float[] color) {
            this.color = color;
            return this;
        }

        public Path3D build() {
            return new Path3D(this);
        }
    }
}
