package com.vinay.vinplayer.views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.VinMediaDataManager;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.net.URISyntaxException;

public class MetaDataEditor extends AppCompatActivity {

    MaterialEditText title,album,artist,genre,year;
    Button submit;
    ImageButton albumart;

    String song_title,album_name,artist_name,genre_name,album_id,data;
    Boolean albumart_available;
    Bitmap albumArtBitmap;

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.mediaListChanged));
        sendBroadcast(intent);
        super.onDestroy();
    }

    RelativeLayout background;
    String f_song_title,f_album_name,f_artist_name,f_genre_name;

    private static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta_data_editor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        title = (MaterialEditText) findViewById(R.id.metaedit_title);
        album = (MaterialEditText) findViewById(R.id.metaedit_album);
        artist = (MaterialEditText) findViewById(R.id.metaedit_artist);
        genre = (MaterialEditText) findViewById(R.id.metaedit_genre);
        submit = (Button) findViewById(R.id.metadata_submit);
        albumart = (ImageButton) findViewById(R.id.metadata_albumart);
        background = (RelativeLayout) findViewById(R.id.activity_meta_data_editor);

        getIntentData(savedInstanceState);

        if (albumart_available){
            albumArtBitmap = VinMediaLists.getInstance().getAlbumart(Long.parseLong(album_id),this);
            albumart.setImageBitmap(albumArtBitmap);
            background.setBackground(BlurBuilder.getInstance().drawable_img(album_id,this));
        }else {
            albumArtBitmap = null;
        }


        if (song_title!=null)title.setText(song_title);
        if (album_name!=null)album.setText(album_name);
        if (artist_name!=null)artist.setText(artist_name);

        albumart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                f_song_title = title.getText().toString();
                f_album_name = album.getText().toString();
                f_artist_name = artist.getText().toString();
                f_genre_name = genre.getText().toString();

                if (new VinMediaDataManager().setMetaData(
                        getBaseContext(),
                        data,
                        f_song_title,
                        f_album_name,
                        f_artist_name,
                        f_genre_name,
                        albumArtBitmap
                ))Toast.makeText(getBaseContext(),"Editing Done",Toast.LENGTH_SHORT).show();
                else Toast.makeText(getBaseContext(),"Editing Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No File Manager Available",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {

                    Uri uri = data.getData();
                    String path = null;
                    try {
                        path = getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    setupAlbumartBitmap(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    private void setupAlbumartBitmap(String fileLocation) {
        if (fileLocation!=null){
            try {
                albumArtBitmap = BitmapFactory.decodeFile(fileLocation);
                albumart.setImageBitmap(albumArtBitmap);
                background.setBackground(new BitmapDrawable(
                        BlurBuilder.getInstance().blur_bitmap(this,albumArtBitmap)));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void getIntentData(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                song_title=null;
                album_name = null;
                artist_name = null;
                genre_name = null;
                album_id = null;
                data = null;
                albumart_available = false;

            } else {
                song_title = extras.getString("title");
                album_name = extras.getString("album");
                artist_name = extras.getString("artist");
                //genre_name = extras.getString("genre");
                album_id = extras.getString("album_id");
                data = extras.getString("data");
                albumart_available = extras.getBoolean("album_art_availalbe");
            }
        } else {
            data = (String) savedInstanceState.getSerializable("data");
            song_title = (String) savedInstanceState.getSerializable("title");
            album_name = (String) savedInstanceState.getSerializable("album");
            artist_name = (String) savedInstanceState.getSerializable("artist");
            //genre_name = (String) savedInstanceState.getSerializable("genre");
            album_id = (String) savedInstanceState.getSerializable("album_id");
            albumart_available = (Boolean) savedInstanceState.getSerializable("album_art_availalbe");
        }
    }

}
