package com.aaa.lib.mapdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.aaa.lib.map.MapView;

public class MapActivity extends AppCompatActivity {
    MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mv_main);
    }
}
