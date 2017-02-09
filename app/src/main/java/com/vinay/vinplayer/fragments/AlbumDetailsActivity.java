package com.vinay.vinplayer.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.klinker.android.sliding.SlidingActivity;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumSongsAdapter;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailsActivity extends SlidingActivity {
    RecyclerView recyclerView;
    Bitmap blurredBitmap, blurredBitmap1;
    Bitmap input_to_blur;
    Bitmap temp_input;
    Bitmap default_bg;
    public static Drawable dr;
    static ArrayList<HashMap<String,String>> albumsongs;

        @Override
    public void init(Bundle savedInstanceState) {

            albumsongs= (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");

            setTitle(albumsongs.get(0).get("album"));
            setContent(R.layout.fragment_album_details);

            setPrimaryColors( getResources().getColor(R.color.transparentLightBlack),
                    getResources().getColor(R.color.transparentBlack));

            expandFromPoints(0,0,0,0);

        recyclerView = (RecyclerView) findViewById(R.id.album_details_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AlbumSongsAdapter(this,albumsongs));
            try{
                Log.d("albumsongs","image loaded");
                setImage(VinMediaLists.getInstance().getAlbumart(
                        Long.parseLong(albumsongs.get(0).get("album_id")),getApplicationContext()));
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");

                recyclerView.setBackground(BlurBuilder.getInstance().drawable_img(albumsongs.get(0).get("album_id"),getApplicationContext()));

            }catch (Exception e){

            }

/*
            new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Target target = new Target() {
                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onStop() {

                            }

                            @Override
                            public void onDestroy() {

                            }

                            @Override
                            public void onLoadStarted(Drawable placeholder) {

                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {

                            }

                            @Override
                            public void onResourceReady(Object resource, GlideAnimation glideAnimation) {
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                                setImage(((BitmapDrawable)placeholder).getBitmap());
                            }

                            @Override
                            public void getSize(SizeReadyCallback cb) {

                            }

                            @Override
                            public void setRequest(Request request) {

                            }

                            @Override
                            public Request getRequest() {
                                return null;
                            }

                        };

                        try {
                             } catch (Exception e) {
                           // e.printStackTrace();
                        }
                   }

                    }, 500);*/

            enableFullscreen();

    }


}
