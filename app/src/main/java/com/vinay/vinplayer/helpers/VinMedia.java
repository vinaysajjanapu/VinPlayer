package com.vinay.vinplayer.helpers;

/**
 * Created by vinaysajjanapu on 31/1/17.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VinMedia {


    private static ArrayList<HashMap<String,String>> currentList;
    private static volatile VinMedia Instance = null;
    private Context context;
    private Cursor cur;
    private MediaPlayer mediaPlayer;
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
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                Log.d("mediaplayer","seek complete");
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("mediaplayer","song complete");
                if (!isClean()&&!isPlaying())nextSong();
            }
        });
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.d("mediaplayer", what + "  "+ extra);
                return false;
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                mediaPlayer.start();
            }
        });

        vinMediaLists = new VinMediaLists(context);
        currentList = vinMediaLists.getAllSongsList();
    }

    public void changeCurrentList(int position,int i){
        if(i==1)
        currentList = vinMediaLists.getAlbumSongsList(vinMediaLists.getAlbumsList().get(position).get("album"));
    else if(i==2)
            currentList = vinMediaLists.getArtistSongsList(vinMediaLists.getArtistsList().get(position).get("artist"));
    }

    public ArrayList<HashMap<String,String>> getCurrentList(){
        return currentList;
    }

    private void setMediaSource(){
        try {
            mediaPlayer.setDataSource(currentList.get(position).get("data"));
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMusic(int index){
        this.position  = index;
        if(isPlaying())resetPlayer();
        setMediaSource();
        //mediaPlayer.start();
    }

    public void pauseMusic(){
        mediaPlayer.pause();
        pausePosition = mediaPlayer.getCurrentPosition();
    }
    public void resumeMusic(){
        mediaPlayer.seekTo(pausePosition);
        Log.d("pausepos",pausePosition+"  ");
        mediaPlayer.start();
    }

    public void stopMusic(){
        mediaPlayer.stop();
    }

    public void nextSong(){
        if ((position+1)<currentList.size()){
            position++;
            if (isPlaying()||!isClean()){
                stopMusic();
                resetPlayer();
                startMusic(position);
            }else {
                startMusic(position);
            }
        }
        Intent local=new Intent();
        local.setAction("change.ui");
        context.sendBroadcast(local);
    }

    public void previousSong(){
        if ((position-1)>=0){
            position--;
            if (isPlaying()||!isClean()){
                stopMusic();
                resetPlayer();
                startMusic(position);
            }else {
                startMusic(position);
            }
        }
        Intent local=new Intent();
        local.setAction("change.ui");
        context.sendBroadcast(local);
    }

    public void setAudioProgress(int seekBarProgress){
        mediaPlayer.seekTo(seekBarProgress);
    }
    public int getAudioProgress(){
       // Log.d("audio progress",mediaPlayer.getCurrentPosition()/1000+"");
        return mediaPlayer.getCurrentPosition()/1000;

    }


    public void setPosition(int position){
        this.position = position;
    }
    public int getPosition(){
        return position;
    }
    public int getDuration(){
        return Integer.parseInt(getCurrentSongDetails().get("duration"));
    }




    public void resetPlayer(){
        mediaPlayer.stop();
        mediaPlayer.reset();
    }
    public void releasePlayer(){
        mediaPlayer.release();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public boolean isClean(){ return mediaPlayer.getCurrentPosition()==0;}

    public HashMap<String, String> getCurrentSongDetails(){
        currentSongDetails = currentList.get(position);
        return currentSongDetails;
    }
}
