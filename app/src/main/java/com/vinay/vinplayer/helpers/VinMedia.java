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

import com.vinay.vinplayer.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VinMedia {


    private static ArrayList<HashMap<String,String>> currentList;
    private static volatile VinMedia Instance = null;
    private Context context;
    private Cursor cur;
    private MediaPlayer mediaPlayer;
    public static int pausePosition;
    public static int position=0;
    public HashMap<String,String> currentSongDetails;
    private static Uri uri;
    private ContentResolver contentResolver;
    VinMediaLists vinMediaLists;
    Intent newSongLoadIntent,songPausedIntent,songResumedIntent,musicStoppedIntent;


    public VinMedia(Context context){
        this.context=context;
    }


    public void VinMediaInitialize(){
        newSongLoadIntent = new Intent();
        songPausedIntent = new Intent();
        songResumedIntent = new Intent();
        musicStoppedIntent = new Intent();

        newSongLoadIntent.setAction(context.getString(R.string.newSongLoaded));
        songPausedIntent.setAction(context.getString(R.string.songPaused));
        songResumedIntent.setAction(context.getString(R.string.songResumed));
        musicStoppedIntent.setAction(context.getString(R.string.musicStopped));

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                Log.d("mediaplayer","seek complete");
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                context.sendBroadcast(musicStoppedIntent);
                Log.d("mediaplayer","song complete");
                if (!isClean()&&!isPlaying())nextSong();
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
        if(isPlaying()||!isClean())resetPlayer();
        setMediaSource();
        context.sendBroadcast(newSongLoadIntent);
    }

    public void pauseMusic(){
        mediaPlayer.pause();
        context.sendBroadcast(songPausedIntent);
        pausePosition = mediaPlayer.getCurrentPosition();
    }
    public void resumeMusic(){
        mediaPlayer.seekTo(pausePosition);
        mediaPlayer.start();
        context.sendBroadcast(songResumedIntent);
    }

    public void stopMusic(){
        mediaPlayer.stop();
    }

    public void nextSong(){
        if ((position+1)<currentList.size()){
            position++;
            startMusic(position);
        }
    }

    public void previousSong(){
        if ((position-1)>=0){
            position--;
           /* if (isPlaying()||!isClean()){
                stopMusic();
                resetPlayer();
                startMusic(position);
            }else {
                startMusic(position);
            }*/
            startMusic(position);
        }
    }

    public void setAudioProgress(int seekBarProgress){
        if(!isPlaying())pausePosition = seekBarProgress;
        mediaPlayer.seekTo(seekBarProgress);
    }
    public int getAudioProgress(){
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
