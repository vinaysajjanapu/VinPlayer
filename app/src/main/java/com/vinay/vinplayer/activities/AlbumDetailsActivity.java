package com.vinay.vinplayer.activities;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumSongsAdapter;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMedia;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailsActivity extends SwipeBackActivity {
    RecyclerView recyclerView;
    static ArrayList<HashMap<String,String>> albumsongs;
    ImageView imageView;
    Toolbar toolbar;
    ImageButton close;
    FloatingActionButton fab;
    String type = "",title = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_album_details);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        imageView= (ImageView) findViewById(R.id.toolbarImage);
        setSupportActionBar(toolbar);
        albumsongs= (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("list");
        type = (String)getIntent().getSerializableExtra("type");
        title = (String)getIntent().getSerializableExtra("title");

        getSupportActionBar().setTitle(title);
        Log.d("albumdetailsactivity",title);
        //toolbar.setTitle(title);


        setDragEdge(SwipeBackLayout.DragEdge.LEFT);

        close = (ImageButton)findViewById(R.id.btn_close);
        close.setColorFilter(Color.WHITE);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VinMedia.getInstance().updateTempQueue(albumsongs,getBaseContext());
                VinMedia.getInstance().updateQueue(false,getBaseContext());
                VinMedia.getInstance().startMusic(0,getBaseContext());
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.album_details_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AlbumSongsAdapter(this,albumsongs));
    }


    @Override
    protected void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
        if (!type.equals("artist")) {
            try {
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumsongs.get(0).get("album_id")));
                Picasso.with(this)
                        .load(uri)
                        .resize(200,200)
                        .centerCrop()
                        .placeholder(R.drawable.albumart_default)
                        .error(R.drawable.albumart_default)
                        .into(imageView);
                recyclerView.setBackground(BlurBuilder.getInstance().drawable_img(albumsongs.get(0).get("album_id"), getApplicationContext()));

            } catch (Exception e) {

            }
        }else {
            try {
                String subdir;
                String externalRootDir = Environment.getExternalStorageDirectory().getPath();
                if (!externalRootDir.endsWith("/")) {
                    externalRootDir += "/";
                }
                subdir = "VinPlayer/.thumb/.artistcache/";
                String path = externalRootDir + subdir+albumsongs.get(0).get("artist")+".jpg";
                Picasso.with(this)
                        .load("file://"+path)
                        .placeholder(R.drawable.albumart_default)
                        .error(R.drawable.albumart_default)
                        //.onlyScaleDown()
                        .resize(170,170)
                        .centerCrop()
                        .into(imageView);
                recyclerView.setBackground(BlurBuilder.getInstance().drawable_usingPath(path,this));

            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onMessage(MessageEvent event){
        if (event.message.equals(getString(R.string.newSongLoaded))){
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

}
