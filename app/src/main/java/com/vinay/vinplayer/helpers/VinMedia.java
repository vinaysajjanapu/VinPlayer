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


    private static ArrayList<HashMap<String,String>> allSongs,currentQueue;
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
        iniatializeBroadcasts();
        newMediaPlayer();
        vinMediaLists = new VinMediaLists(context);
        allSongs = vinMediaLists.getAllSongsList();
        if (currentQueue==null)currentQueue = allSongs;
    }

    private void iniatializeBroadcasts() {

        newSongLoadIntent = new Intent();
        songPausedIntent = new Intent();
        songResumedIntent = new Intent();
        musicStoppedIntent = new Intent();

        newSongLoadIntent.setAction(context.getString(R.string.newSongLoaded));
        songPausedIntent.setAction(context.getString(R.string.songPaused));
        songResumedIntent.setAction(context.getString(R.string.songResumed));
        musicStoppedIntent.setAction(context.getString(R.string.musicStopped));
    }

    public void updateQueue(int position,int i){
        if(i==1)
            currentQueue = vinMediaLists.getAlbumSongsList(vinMediaLists.getAlbumsList().get(position).get("album"));
        else if(i==2)
            currentQueue = vinMediaLists.getArtistSongsList(vinMediaLists.getArtistsList().get(position).get("artist"));
    }

    public ArrayList<HashMap<String,String>> getCurrentList(){
        return currentQueue;
    }

    private void setMediaSource(){
        try {
            mediaPlayer.setDataSource(currentQueue.get(position).get("data"));
            Log.d("mediaplayer status","starting music");
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void newMediaPlayer(){
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
                Log.d("Broadcast","song complete");
                if (!isClean()&&!isPlaying()){
                      nextSong();
                }
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
    }

    public void startMusic(int index){
        context.sendBroadcast(newSongLoadIntent);
        Log.d("Broadcast","music started");
        this.position  = index;
        if(isPlaying()||!isClean()){
            resetPlayer();
        }
        newMediaPlayer();
        setMediaSource();
    }

    public void pauseMusic(){
        mediaPlayer.pause();
        context.sendBroadcast(songPausedIntent);
        Log.d("Broadcast","music paused");
        pausePosition = mediaPlayer.getCurrentPosition();
    }
    public void resumeMusic(){
        mediaPlayer.seekTo(pausePosition);
        mediaPlayer.start();
        context.sendBroadcast(songResumedIntent);
        Log.d("Broadcast","music paused");
    }

    public void nextSong(){
        if ((position+1)<currentQueue.size()){
            position++;
            startMusic(position);
        }
    }

    public void previousSong(){
        if ((position-1)>=0){
            position--;
            startMusic(position);
        }
    }

    public void resetPlayer(){
        Log.d("mediaplayer status","reset player");
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }catch (Exception e){

        }
            mediaPlayer.release();
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

    public void releasePlayer(){
        mediaPlayer.release();
    }

    public boolean isPlaying(){
        boolean isPlaying = false;
        try {
            isPlaying = mediaPlayer.isPlaying();
        }catch (Exception e){

        }
        return isPlaying;
    }

    public boolean isClean() {
        boolean isClean = false;
        try {
            isClean = (mediaPlayer.getCurrentPosition() == 0);
        } catch (Exception e) {

        }
        return isClean;
    }

    public HashMap<String, String> getCurrentSongDetails(){
        currentSongDetails = currentQueue.get(getPosition());
        return currentSongDetails;
    }

}
