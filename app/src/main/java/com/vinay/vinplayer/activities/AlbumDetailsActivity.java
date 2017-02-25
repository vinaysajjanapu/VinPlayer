package com.vinay.vinplayer.activities;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumSongsAdapter;
import com.vinay.vinplayer.helpers.BlurBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import ooo.oxo.library.widget.PullBackLayout;

public class AlbumDetailsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Bitmap blurredBitmap, blurredBitmap1;
    Bitmap input_to_blur;
    Bitmap temp_input;
    Bitmap default_bg;
    public static Drawable dr;
    static ArrayList<HashMap<String,String>> albumsongs;
    ImageView imageView;
    Toolbar toolbar;
    //import ooo.oxo.mr.databinding.ViewerActivityBinding;

    //private ViewerActivityBinding binding;

    PullBackLayout pullBackLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_album_details);
        //setContentView(R.layout.fragment_album_details);




        toolbar= (Toolbar) findViewById(R.id.toolbar);
        imageView= (ImageView) findViewById(R.id.toolbarImage);
        setSupportActionBar(toolbar);
        albumsongs= (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");
        toolbar.setTitle(albumsongs.get(0).get("album")+"");


        recyclerView = (RecyclerView) findViewById(R.id.album_details_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AlbumSongsAdapter(this,albumsongs));
    }


    @Override
    protected void onResume() {
        super.onResume();
        try{
            Log.d("albumsonsrecycler","set image");
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumsongs.get(0).get("album_id")));
            Picasso.with(this).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                    .into(imageView);
            recyclerView.setBackground(BlurBuilder.getInstance().drawable_img(albumsongs.get(0).get("album_id"),getApplicationContext()));

        }catch (Exception e){

        }
    }
}
