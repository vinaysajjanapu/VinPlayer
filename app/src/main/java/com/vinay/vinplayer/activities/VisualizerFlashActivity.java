package com.vinay.vinplayer.activities;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vinay.vinplayer.R;

public class VisualizerFlashActivity extends AppCompatActivity {

    Button start,stop;
    static Camera cam = null;
    Thread thread;

    @Override
    protected void onDestroy() {
        try {
            cam.release();
        }catch (Exception e){

        }
        cam = null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizer_flash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        start = (Button)findViewById(R.id.start);
        stop = (Button)findViewById(R.id.stop);

        try{

            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.stopPreview();
            }
        }catch (Exception e){

        }


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //      startFlash();
                if (thread!=null)thread.start();
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    stopFlash();
                if (thread!=null && thread.isAlive())
                    thread.interrupt();
            }
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    startFlash();
                    try {
                        Thread.sleep(70/*(long)(Math.random()*20)*/);
                    } catch (Exception e) {
                        //
                    }
                    stopFlash();
                    try {
                        Thread.sleep(70/*(long)(Math.random()*500)*/);
                    } catch (Exception e) {
                        //
                    }
                }
            }
        };
        thread = new Thread(runnable);
        //thread.start();

    }

    private void stopFlash() {

        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startFlash() {

        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
