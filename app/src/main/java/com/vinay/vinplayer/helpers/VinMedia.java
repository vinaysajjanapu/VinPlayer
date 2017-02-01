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

import com.vinay.vinplayer.helpers.VinMediaLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is to control the playback of music
 */


public class VinMedia {


    public static ArrayList<HashMap<String,String>> currentList;
    private static volatile VinMedia Instance = null;
    private Context context;
    private Cursor cur;
    private MediaPlayer mediaPlayer;
    public static boolean isPlaying = false;
    int pausePosition;
    private int position;
    public HashMap<String,String> currentSongDetails;
    private static Uri uri;
    private ContentResolver contentResolver;
    VinMediaLists vinMediaLists;

    public VinMedia(Context context){
        this.context=context;
    }


    public void VinMediaInitialize(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        vinMediaLists = new VinMediaLists(context);
        currentList = vinMediaLists.getAllSongsList();
    }

    public void changeCurrentList(int position){
        currentList = vinMediaLists.getAlbumSongsList(vinMediaLists.getAlbumsList().get(position).get("album"));
    }

    private void setMediaSource(){
        try {
            mediaPlayer.setDataSource(currentList.get(position).get("data"));
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMusic(int index){
        this.position  = index;
        setMediaSource();

        if(isPlaying())resetPlayer();

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
        resetPlayer();
        isPlaying = false;
    }

    public void resetPlayer(){
        mediaPlayer.reset();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }


    public HashMap<String, String> getCurrentSongDetails(){
        currentSongDetails = currentList.get(position);
        return currentSongDetails;
    }
}
