package com.vinay.vinplayer;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    //TextView textView;
    ArrayList<HashMap<String, String>> songs;

    Button start, pause, stop;
    MediaPlayer mp;
    ImageView imageView;
    EditText editText;
    VinMedia vinMedia;
    int index = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vinMedia = new VinMedia(this);

        // textView= (TextView) findViewById(R.id.tv);


        start = (Button) findViewById(R.id.button1);
        pause = (Button) findViewById(R.id.button2);
        stop = (Button) findViewById(R.id.button3);
        imageView = (ImageView) findViewById(R.id.image);
        editText = (EditText) findViewById(R.id.num);

//        mp = new MediaPlayer();


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (true) {
                    vinMedia.resetPlayer();
                }
                if (!editText.getText().toString().equals("") && editText != null && editText.getText().length() != 0) {
                    index = Integer.parseInt(editText.getText().toString());
                    setAlbumArt();
                    vinMedia.setMediaSource(index);
                    vinMedia.startMusic();

                } else
                    Toast.makeText(getApplicationContext(), "enter any no", Toast.LENGTH_SHORT).show();
            }

        });


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vinMedia.isPlaying()) {

                    setAlbumArt();
                    vinMedia.pauseMusic();
                    pause.setText("resume");

                } else {
                    vinMedia.resumeMusic();
                    pause.setText("pause");
                }
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
        songs = VinMedia.songsList;
        //myUpdateOperation(5);

    }

    private void setAlbumArt() {
        imageView.setImageBitmap(vinMedia.getAlbumart(index%songs.size())); //associated cover art in bitmap
        imageView.setAdjustViewBounds(true);
    }
}