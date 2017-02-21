package com.vinay.vinplayer.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumSongsAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Bitmap blurredBitmap, blurredBitmap1;
    Bitmap input_to_blur;
    Bitmap temp_input;
    Bitmap default_bg;
    public static Drawable dr;
    static ArrayList<HashMap<String,String>> albumsongs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumsongs= (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");

        if (albumsongs!=null) {
            setTitle(albumsongs.get(0).get("album")+"");

        }
        recyclerView = (RecyclerView) findViewById(R.id.album_details_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AlbumSongsAdapter(this,albumsongs));
 /*       try{
            Log.d("albumsongs","image loaded");
            //setImage(VinMediaLists.getInstance().getAlbumart(
                    Long.parseLong(albumsongs.get(0).get("album_id")),getApplicationContext()));
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            recyclerView.setBackground(BlurBuilder.getInstance().drawable_img(albumsongs.get(0).get("album_id"),getApplicationContext()));

        }catch (Exception e){

        }*/


            new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }

                        };

                        try {
                             } catch (Exception e) {
                           // e.printStackTrace();
                        }
                   }

                    }, 500);



    }




}
