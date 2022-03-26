package com.aaa.lib.map3d.obj;

import android.graphics.Bitmap;

import java.nio.FloatBuffer;

public class Plane3D {
    public Bitmap bitmap;
    public FloatBuffer vertex;
    public FloatBuffer texture;

    private Plane3D(Builder builder) {
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

        public Plane3D build() {
            return new Plane3D(this);
        }
    }
}
