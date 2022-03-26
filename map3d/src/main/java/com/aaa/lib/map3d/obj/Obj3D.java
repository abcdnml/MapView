package com.aaa.lib.map3d.obj;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by wuwang on 2017/2/22
 */

public class Obj3D extends ModelData {
    public int vertCount;
    public FloatBuffer position;
    public FloatBuffer normal;
    public FloatBuffer texture;

    public MtlInfo mtl;
    public ArrayList<Float> tempTexture;
    private ArrayList<Float> tempPosition;
    private ArrayList<Float> tempNormal;

    public int modelVao;
    public int shadowVao;
    public int textureKd;
    public int textureKd_normal;


    private Obj3D(Builder builder) {
        vertCount = builder.vertCount;
        position = builder.vert;
        normal = builder.vertNorl;
        texture = builder.vertTexture;
        mtl = builder.mtl;
    }

    public Obj3D() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void addVert(float d) {
        if (tempPosition == null) {
            tempPosition = new ArrayList<>();
        }
        tempPosition.add(d);
    }

    public void addVertTexture(float d) {
        if (tempTexture == null) {
            tempTexture = new ArrayList<>();
        }
        tempTexture.add(d);
    }

    public void addVertNorl(float d) {
        if (tempNormal == null) {
            tempNormal = new ArrayList<>();
        }
        tempNormal.add(d);
    }

    public void dataLock() {
        if (tempPosition != null) {
            setPosition(tempPosition);
            tempPosition.clear();
            tempPosition = null;
        }
        if (tempTexture != null) {
            setTexture(tempTexture);
            tempTexture.clear();
            tempTexture = null;
        }
        if (tempNormal != null) {
            setNormal(tempNormal);
            tempNormal.clear();
            tempNormal = null;
        }
    }

    public void setPosition(ArrayList<Float> data) {
        int size = data.size();
        ByteBuffer buffer = ByteBuffer.allocateDirect(size * 4);
        buffer.order(ByteOrder.nativeOrder());
        position = buffer.asFloatBuffer();
        for (int i = 0; i < size; i++) {
            position.put(data.get(i));
        }
        position.position(0);
        vertCount = size / 3;
    }

    public void setNormal(ArrayList<Float> data) {
        int size = data.size();
        ByteBuffer buffer = ByteBuffer.allocateDirect(size * 4);
        buffer.order(ByteOrder.nativeOrder());
        normal = buffer.asFloatBuffer();
        for (int i = 0; i < size; i++) {
            normal.put(data.get(i));
        }
        normal.position(0);
    }

    public void setTexture(ArrayList<Float> data) {
        int size = data.size();
        ByteBuffer buffer = ByteBuffer.allocateDirect(size * 4);
        buffer.order(ByteOrder.nativeOrder());
        texture = buffer.asFloatBuffer();
        for (int i = 0; i < size; ) {
            texture.put(data.get(i));
            i++;
            texture.put(data.get(i));
            i++;
        }
        texture.position(0);
    }


    public static final class Builder {
        private int vertCount;
        private FloatBuffer vert;
        private FloatBuffer vertNorl;
        private FloatBuffer vertTexture;
        private MtlInfo mtl;

        private Builder() {
        }

        public Builder vertCount(int vertCount) {
            this.vertCount = vertCount;
            return this;
        }

        public Builder position(FloatBuffer vert) {
            this.vert = vert;
            return this;
        }

        public Builder normal(FloatBuffer vertNorl) {
            this.vertNorl = vertNorl;
            return this;
        }

        public Builder texture(FloatBuffer vertTexture) {
            this.vertTexture = vertTexture;
            return this;
        }

        public Builder mtl(MtlInfo mtl) {
            this.mtl = mtl;
            return this;
        }

        public Obj3D build() {
            return new Obj3D(this);
        }
    }
}
