package com.vinay.vinplayer.fragments;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.klinker.android.sliding.SlidingActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumSongsAdapter;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.views.MainActivity;


import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailsActivity extends SlidingActivity {
    RecyclerView recyclerView;
    Bitmap blurredBitmap, blurredBitmap1;
    Bitmap input_to_blur;
    Bitmap temp_input;
    Bitmap default_bg;
    public static Drawable dr;
    static ArrayList<HashMap<String,String>> allsongs;
        @Override
    public void init(Bundle savedInstanceState) {

            setTitle("jj");
            setContent(R.layout.fragment_album_details);

            setPrimaryColors( getResources().getColor(R.color.transparentLightBlack),
                    getResources().getColor(R.color.transparentBlack));

            expandFromPoints(0,0,0,0);
           // addContentView();


            allsongs= (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");


        recyclerView = (RecyclerView) findViewById(R.id.album_details_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AlbumSongsAdapter(this,allsongs));

            new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                setImage(bitmap);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        };

                        try {
                            final Uri sArtworkUri = Uri
                                    .parse("content://media/external/audio/albumart");
                            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(allsongs.get(0).get("album_id")));

                            Picasso.with(getApplicationContext()).load(uri)
                                    .placeholder(R.drawable.albumart_default)
                                    .error(R.drawable.albumart_default)
                                    .into(target);

                            recyclerView.setBackground(BlurBuilder.getInstance().drawable_img(allsongs.get(0).get("album_id"),getApplicationContext()));
                        } catch (Exception e) {
                           // e.printStackTrace();
                        }
                   }

                    }, 500);

            enableFullscreen();

    }
}
