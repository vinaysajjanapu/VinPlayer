package com.vinay.vinplayer;

/**
 * Created by vinaysajjanapu on 31/1/17.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is to control the playback of music
 */


public class VinMedia {


    public static ArrayList<HashMap<String,String>> songsList;
    private static volatile VinMedia Instance = null;
    private Context context;
    private String selection;
    private String sortOrder;
    private Cursor cur;
    private MediaPlayer mediaPlayer;
    public static boolean isPlaying = false;
    int pausePosition;


    public VinMedia(Context context){
        this.context=context;
    }

    public static VinMedia getInstance() {
        VinMedia localInstance = Instance;
        if (localInstance == null) {
            synchronized (VinMedia.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new VinMedia(getInstance().context);
                }
            }
        }
        return localInstance;
    }

    public void VinMediaInitialize(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        songsList = new ArrayList<>();

        //grantUriPermission("com.vinay.vinplayer",uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ContentResolver cr = context.getContentResolver();

        selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        cur = cr.query(uri, null, selection, null, sortOrder);
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
                    String id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID));
                    String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String album_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String artist_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                    String date_added = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                    String duration = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String size = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String track = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TRACK));
                    String year = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.YEAR));

                    // Save to your list here

                    HashMap<String,String> h=new HashMap<>();
                    h.put("data",data);
                    h.put("id",id);
                    h.put("album",album);
                    h.put("album_id",album_id);
                    h.put("artist",artist);
                    h.put("artist_id",artist_id);
                    h.put("date_added",date_added);
                    h.put("duration",duration);
                    h.put("size",size);
                    h.put("title",title);
                    h.put("track",track);
                    h.put("year",year);

                    songsList.add(h);
                }
            }
        }

        cur.close();
    }
    public void setMediaSource(int i){
        try {
            mediaPlayer.setDataSource(songsList.get(i%songsList.size()).get("data"));
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMusic(){
        mediaPlayer.start();
        isPlaying = true;
    }

    public void pauseMusic(){
        mediaPlayer.pause();
        pausePosition = mediaPlayer.getCurrentPosition();
        isPlaying = false;
    }
    public void resumeMusic(){
        mediaPlayer.seekTo(pausePosition);
        mediaPlayer.start();
        isPlaying = true;
    }

    public void stopMusic(){
        mediaPlayer.stop();
        isPlaying = false;
    }
    public void resetPlayer(){
        mediaPlayer.reset();
    }
    public boolean isPlaying(){

        return mediaPlayer.isPlaying();


    }




    public Bitmap getAlbumart(int album_id)
    {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songsList.get(album_id).get("data"));

        byte [] data = mmr.getEmbeddedPicture();
        if(data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        }else {
            return BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);
        }
    }


}
