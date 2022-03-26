package com.aaa.lib.map3d.obj;

import android.graphics.Bitmap;

import java.nio.FloatBuffer;

public class Area3D extends ModelData {
    public Bitmap bitmap;
    public FloatBuffer vertex;
    public FloatBuffer texture;

    private Area3D(){

    }

    private Area3D(Builder builder) {
        bitmap = builder.bitmap;
        vertex = builder.vertex;
        texture = builder.texture;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Bitmap bitmap;
        private FloatBuffer vertex;
        private FloatBuffer texture;

        private Builder() {
        }

        public Builder bitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public Builder vertex(FloatBuffer vertex) {
            this.vertex = vertex;
            return this;
        }

        public Builder texture(FloatBuffer texture) {
            this.texture = texture;
            return this;
        }

        public Area3D build() {
            return new Area3D(this);
        }
    }
}
