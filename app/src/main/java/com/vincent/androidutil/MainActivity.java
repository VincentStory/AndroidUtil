package com.vincent.androidutil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.vincent.monitorcrashlib.util.CrashToolUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        findViewById(R.id.tv_1).setOnClickListener(view -> {
            CrashToolUtils.startCrashTestActivity(this);
        });
        findViewById(R.id.tv_3).setOnClickListener(view -> {
           startActivity(new Intent(this, NetworkActivity.class));
        });


    }
}