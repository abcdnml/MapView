package com.aaa.lib.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void map(View view) {
        startActivity(new Intent(this,MapActivity.class));
    }

    public void map3d(View view) {
        startActivity(new Intent(this, Map3DActivity.class));
    }
}
