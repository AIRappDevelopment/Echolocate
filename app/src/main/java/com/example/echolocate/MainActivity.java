package com.example.echolocate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
             startCamera();
            }
        }, 0000);
    }

    //starts the camera activity
    public void startCamera(){
        Intent intent = new Intent(this, CameraActivity.class);
        Intent intent2 = new Intent(this, CameraPreview.class);
        startActivity(intent);
        startActivity(intent2);
    }

}
