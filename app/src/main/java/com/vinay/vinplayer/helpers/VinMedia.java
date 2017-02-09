package com.vinay.vinplayer.helpers;

/**
 * Created by vinaysajjanapu on 31/1/17.
 */

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VinMedia implements NotificationManager.NotificationCenterDelegate, SensorEventListener {

    public static Boolean SHUFFLE_ON = true;
    public static Boolean SHUFFLE_OFF = false;

    public static int REPEAT_NONE = 0;
    public static int REPEAT_QUEUE = 1;
    public static int REPEAT_TRACK = 2;

    private static ArrayList<HashMap<String,String>> allSongs,currentQueue,tempQueue;
    private static volatile VinMedia Instance = null;
    private Cursor cur;
    private MediaPlayer mediaPlayer;
    public static int pausePosition;
    public static int position=0;
    public HashMap<String,String> currentSongDetails;
    private static Uri uri;
    private ContentResolver contentResolver;
    private static int[] shuffle_queue;
    static int shuffle_index;

    Intent newSongLoadIntent,songPausedIntent,songResumedIntent,musicStoppedIntent;
    SharedPreferences media_settings;
    int repeatmode;
    boolean is_shuffle;



    public static VinMedia getInstance() {
        VinMedia localInstance = Instance;
        if (localInstance == null) {
            synchronized (VinMedia.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new VinMedia();
                }
            }
        }
        return localInstance;
    }

    private void iniatializeBroadcasts(Context context) {

        newSongLoadIntent = new Intent();
        songPausedIntent = new Intent();
        songResumedIntent = new Intent();
        musicStoppedIntent = new Intent();

        newSongLoadIntent.setAction(context.getString(R.string.newSongLoaded));
        songPausedIntent.setAction(context.getString(R.string.songPaused));
        songResumedIntent.setAction(context.getString(R.string.songResumed));
        musicStoppedIntent.setAction(context.getString(R.string.musicStopped));
    }

    public void updateTempQueue(int position,ArrayList<HashMap<String,String>> songList, Context context){
            tempQueue = songList;
        //VinMediaLists.getInstance().getArtistSongsList(VinMediaLists.getInstance().getArtistsList(context).get(position).get("artist"),context);
    }

    public void updateQueue(int position,Context context){
        if (position!=0){
            currentQueue = tempQueue;
        }else currentQueue = VinMediaLists.getInstance().getAllSongsList(context);
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

    void newMediaPlayer(final Context context){
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
                      nextSong(context);
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

    public void startMusic(int index,Context context){
        Log.d("Broadcast","music started");
        this.position  = index;
        if (mediaPlayer!=null){
            if(isPlaying()||!isClean()){
                resetPlayer();
            }
        }
        iniatializeBroadcasts(context);
        newMediaPlayer(context);
        setMediaSource();
        context.sendBroadcast(newSongLoadIntent);
       // mediaPlayer.start();

        NotificationManager.getInstance().postNotificationName(NotificationManager.audioDidStarted, getCurrentSongDetails());


            Intent intent = new Intent(VinPlayer.applicationContext, VinMusicService.class);
            VinPlayer.applicationContext.startService(intent);
//        } else {
//            Intent intent = new Intent(VinPlayer.applicationContext, VinMusicService.class);
//            VinPlayer.applicationContext.stopService(intent);
//        }

        NotificationManager.getInstance().notifyNewSongLoaded(NotificationManager.newaudioloaded, getCurrentSongDetails());

    }


    public void pauseMusic(Context context){
        mediaPlayer.pause();
        context.sendBroadcast(songPausedIntent);
        Log.d("Broadcast","music paused");
        pausePosition = mediaPlayer.getCurrentPosition();
    }

    public void resumeMusic(Context context){
        try {
            mediaPlayer.seekTo(pausePosition);
            mediaPlayer.start();
        }catch (Exception e){
            startMusic(getPosition(),context);
        }
        context.sendBroadcast(songResumedIntent);
        Log.d("Broadcast","music paused");
    }

    public void nextSong(Context context){
        media_settings = context.getSharedPreferences(context.getString(R.string.media_settings),Context.MODE_PRIVATE);
        repeatmode = media_settings.getInt(context.getString(R.string.repeat_mode),2);
        is_shuffle = media_settings.getBoolean(context.getString(R.string.shuffle),false);
        if (!is_shuffle){
            if (position==currentQueue.size()-1){
                if (repeatmode==1)position=0;
            }else if ((position+1)<currentQueue.size()){
                if (repeatmode!=2)position++;
            }
           // startMusic(position,context);
        }else {
            if (shuffle_index==currentQueue.size()-1){
              shuffle_index=0;
            }else if ((shuffle_index+1)<currentQueue.size()){
                    position = getShuffleQueue()[++shuffle_index];
              }
        }
        startMusic(position,context);
    }

    public void previousSong(Context context){
        media_settings = context.getSharedPreferences(context.getString(R.string.media_settings),Context.MODE_PRIVATE);
        repeatmode = media_settings.getInt(context.getString(R.string.repeat_mode),1);
        is_shuffle = media_settings.getBoolean(context.getString(R.string.shuffle),false);
        if (!is_shuffle) {
            if (position == 0) {
                if (repeatmode == 1) position = currentQueue.size() - 1;
            } else if ((position - 1) >= 0) {
                if (repeatmode != 2) position--;
            }
           // startMusic(position,context);
        }else {
            if (shuffle_index == 0) {
                shuffle_index = currentQueue.size() - 1;
            } else if ((shuffle_index - 1) >= 0) {
                    position = getShuffleQueue()[--shuffle_index];
            }
        }
        startMusic(position,context);
    }
    public int[] getShuffleQueue(){
        return shuffle_queue;
    }

    public void createShuffleQueue(){
        shuffle_index = 0;
        int[] shuffle_list = new int[currentQueue.size()];
        int ind1,k,l;
        for (ind1=0;ind1<getCurrentList().size();ind1++)shuffle_list[ind1] = ind1;
        for (ind1=0;ind1<getCurrentList().size();ind1++){
            k = shuffle_list[ind1];
            l = (int)(Math.random()*getCurrentList().size());
            shuffle_list[ind1] = shuffle_list[l];
            shuffle_list[l] = k;
        }
        shuffle_queue = shuffle_list;
        String s="";
        for (ind1=0;ind1<getCurrentList().size();ind1++)s = s + " "+shuffle_queue[ind1];
        Log.d("shuffle queue", s);

    }

    public void resetPlayer(){
        Log.d("mediaplayer status","reset player");
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }catch (Exception e){

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

    public int getCurrentQueueSize(){
        return (getCurrentList()!=null)?getCurrentList().size():0;
    }

    public int getDuration(){
        return (getCurrentSongDetails()!=null)? Integer.parseInt(getCurrentSongDetails().get("duration")):0;
    }

    public void releasePlayer(){
        if (mediaPlayer!=null){
            mediaPlayer.release();
            Intent intent = new Intent(VinPlayer.applicationContext, VinMusicService.class);
            //VinPlayer.applicationContext.stopService(intent);
            }
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
        if (currentQueue!=null) {
            currentSongDetails = currentQueue.get(getPosition());
            return currentSongDetails;
        }else return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void newSongLoaded(Object... args) {

    }



//    public int generateObserverTag() {
//        return lastTag++;
//    }
//
//
}
