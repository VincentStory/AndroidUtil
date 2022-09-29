package com.vincent.androidutil;

import androidx.appcompat.app.AppCompatActivity;

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


    }
}