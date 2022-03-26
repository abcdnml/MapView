package com.aaa.lib.map3d.obj;

import android.graphics.Bitmap;

/**
 * Created by wuwang on 2017/2/22
 */

public class MtlInfo {
    public String newmtl;
    public float[] Ka = new float[3];     //阴影色
    public float[] Kd = new float[3];     //固有色
    public float[] Ks = new float[3];     //高光色
    public float[] Ke = new float[3];     //
    public float Ns;                    //shininess
    public Bitmap kaBitmap;
    public Bitmap kdBitmap;
    public Bitmap kdNormalBitmap;
    public Bitmap ksBitmap;
    public String map_Kd;               //固有纹理贴图
    public String map_Ks;               //高光纹理贴图
    public String map_Ka;               //阴影纹理贴图
    public String map_kd_normal;               //阴影纹理贴图

    //denotes the illumination model used by the material.
    // illum = 1 indicates a flat material with no specular highlights,
    // so the value of Ks is not used.
    // illum = 2 denotes the presence of specular highlights,
    // and so a specification for Ks is required.
    public int illum;

    public MtlInfo() {

    }

    private MtlInfo(Builder builder) {
        newmtl = builder.mtl;
        Ka = builder.Ka;
        Kd = builder.Kd;
        Ks = builder.Ks;
        kaBitmap = builder.kaBitmap;
        kdBitmap = builder.kdBitmap;
        kdNormalBitmap = builder.kdNormalBitmap;
        ksBitmap = builder.ksBitmap;


        Ke = builder.Ke;
        Ns = builder.Ns;
        map_Kd = builder.map_Kd;
        map_Ks = builder.map_Ks;
        map_Ka = builder.map_Ka;
        illum = builder.illum;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        public Bitmap kaBitmap;
        public Bitmap kdBitmap;
        public Bitmap kdNormalBitmap;
        public Bitmap ksBitmap;
        private String mtl;
        private float[] Ka;
        private float[] Kd;
        private float[] Ks;
        private float[] Ke;
        private float Ns;
        private String map_Kd;
        private String map_Ks;
        private String map_Ka;
        private int illum;

        private Builder() {
        }

        public Builder mtl(String newmtl) {
            this.mtl = newmtl;
            return this;
        }

        public Builder Ka(float[] Ka) {
            this.Ka = Ka;
            return this;
        }

        public Builder Kd(float[] Kd) {
            this.Kd = Kd;
            return this;
        }

        public Builder Ks(float[] Ks) {
            this.Ks = Ks;
            return this;
        }

        public Builder Ke(float[] Ke) {
            this.Ke = Ke;
            return this;
        }

        public Builder Ns(float Ns) {
            this.Ns = Ns;
            return this;
        }

        public Builder ka(Bitmap bitmap) {
            this.kaBitmap = bitmap;
            return this;
        }

        public Builder kd(Bitmap bitmap) {
            this.kdBitmap = bitmap;
            return this;
        }
        public Builder kdNormal(Bitmap bitmap) {
            this.kdNormalBitmap = bitmap;
            return this;
        }

        public Builder ks(Bitmap bitmap) {
            this.ksBitmap = bitmap;
            return this;
        }

        public Builder map_Kd(String map_Kd) {
            this.map_Kd = map_Kd;
            return this;
        }

        public Builder map_Ks(String map_Ks) {
            this.map_Ks = map_Ks;
            return this;
        }

        public Builder map_Ka(String map_Ka) {
            this.map_Ka = map_Ka;
            return this;
        }


        public Builder illum(int illum) {
            this.illum = illum;
            return this;
        }

        public MtlInfo build() {
            return new MtlInfo(this);
        }
    }
}
