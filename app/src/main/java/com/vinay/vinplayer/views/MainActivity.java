package com.vinay.vinplayer.views;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinMedia;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    ArrayList<HashMap<String,String>> songs;

    Button start,pause,stop;
    VinMedia vinMedia;
    static int i = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songs= new ArrayList<>();

        vinMedia = new VinMedia(getApplicationContext());

        start = (Button) findViewById(R.id.button1);
        pause = (Button) findViewById(R.id.button2);
        stop = (Button) findViewById(R.id.button3);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                vinMedia.startMusic(i);
            }

        });


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }

        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vinMedia.stopMusic();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        vinMedia.VinMediaInitialize();

    }



}
