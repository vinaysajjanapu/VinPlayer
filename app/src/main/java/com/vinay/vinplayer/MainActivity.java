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
    ArrayList<HashMap<String,String>> songs;

    Button start,pause,stop;
    MediaPlayer mp;
    ImageView imageView;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // textView= (TextView) findViewById(R.id.tv);
        songs= new ArrayList<>();

        start = (Button) findViewById(R.id.button1);
        pause = (Button) findViewById(R.id.button2);
        stop = (Button) findViewById(R.id.button3);
        imageView= (ImageView) findViewById(R.id.image);
        editText= (EditText) findViewById(R.id.num);

        mp = new MediaPlayer();


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true ){
                    mp.reset();
                }
                if(!editText.getText().toString().equals("") && editText!=null && editText.getText().length()!=0 ){
                    myUpdateOperation(Integer.parseInt(editText.getText().toString()));
                mp.start();
                }else Toast.makeText(getApplicationContext(),"nope",Toast.LENGTH_SHORT).show();
            }

        });


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.pause();
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //grantUriPermission("com.vinay.vinplayer",uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ContentResolver cr = getContentResolver();


        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if(cur != null)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    // Add code to get more column here
                String id=cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID));
                    // Save to your list here

                   // textView.append(data+"\n");

                    HashMap<String,String> h=new HashMap<>();
                    h.put("data",data);
                    h.put("id",id);
                    songs.add(h);
                }

            }
        }

        cur.close();

       //myUpdateOperation(5);

    }

    private void myUpdateOperation(int i) {

        try{
            mp.setDataSource(songs.get(i%songs.size()).get("data"));
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.prepare();
            getAlbumart(i%songs.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getAlbumart(int album_id)
    {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songs.get(album_id).get("data"));

        byte [] data = mmr.getEmbeddedPicture();
        //coverart is an Imageview object

        // convert the byte array to a bitmap
        if(data != null)
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imageView.setImageBitmap(bitmap); //associated cover art in bitmap
            imageView.setAdjustViewBounds(true);
            //imageView.setLayoutParams(new .LayoutParams(500, 500));
        }
        else
        {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); //any default cover resourse folder
            imageView.setAdjustViewBounds(true);
            //imageView.setLayoutParams(new LinearLayout.LayoutParams(500,500 ));
        }
    }



}
