package com.aaa.lib.mapdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aaa.lib.map.MapView;


public class MainActivity extends AppCompatActivity {

    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mv_main);
    }
}
