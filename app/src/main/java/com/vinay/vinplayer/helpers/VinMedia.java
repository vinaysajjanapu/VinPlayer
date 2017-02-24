package com.vinay.vinplayer.helpers;

/**
 * Created by vinaysajjanapu on 31/1/17.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.activities.MainActivity;
import com.vinay.vinplayer.database.FavouriteTable;
import com.vinay.vinplayer.database.LastPlayTable;
import com.vinay.vinplayer.database.NextSongDataTable;
import com.vinay.vinplayer.database.PlaylistTable;
import com.vinay.vinplayer.database.RecentPlayTable;
import com.vinay.vinplayer.database.RecommendedTable;
import com.vinay.vinplayer.database.UsageDataTable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VinMedia extends Service implements SensorEventListener,AudioManager.OnAudioFocusChangeListener {

    AudioManager audioManager;
    RemoteControlClient remoteControlClient;
    PhoneStateListener phoneStateListener;
    private static boolean supportLockScreenControls = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    private static boolean supportBigNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private RemoteViews listeners;
    private static boolean AudioFocus = false;
    public static final String NOTIFY_PREVIOUS = "musicplayer.previous";
    public static final String NOTIFY_CLOSE = "musicplayer.close";
    public static final String NOTIFY_PAUSE = "musicplayer.pause";
    public static final String NOTIFY_PLAY = "musicplayer.play";
    public static final String NOTIFY_NEXT = "musicplayer.next";
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    Notification notification;
    static boolean sticky = true;
    static Intent intent;
    static int flags, startId;
    android.app.NotificationManager notificationManager;
    Bitmap pause,play;



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
    public static int position=-1;
    public HashMap<String,String> currentSongDetails;
    private static Uri uri;
    private ContentResolver contentResolver;
    private static int[] shuffle_queue;
    static int shuffle_index;
    public static int playPercentThreshold = 10;


    Intent newSongLoadIntent,songPausedIntent,songResumedIntent,musicStoppedIntent,queueUpdatedIntent;
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

  /*  private void iniatializeBroadcasts(Context context) {

        newSongLoadIntent = new Intent();
        songPausedIntent = new Intent();
        songResumedIntent = new Intent();
        musicStoppedIntent = new Intent();

        newSongLoadIntent.setAction(context.getString(R.string.newSongLoaded));
        songPausedIntent.setAction(context.getString(R.string.songPaused));
        songResumedIntent.setAction(context.getString(R.string.songResumed));
        musicStoppedIntent.setAction(context.getString(R.string.musicStopped));
          }*/

    public void updateTempQueue(ArrayList<HashMap<String,String>> songList, Context context){
            tempQueue = songList;
        //VinMediaLists.getInstance().getArtistSongsList(VinMediaLists.getInstance().getArtistsList(context).get(position).get("artist"),context);
    }

    public void updateQueue(boolean allsongs,Context context){
        if (!allsongs){
            currentQueue = tempQueue;
        }else currentQueue = VinMediaLists.getInstance().getAllSongsList(context);

      /*  queueUpdatedIntent = new Intent();
        queueUpdatedIntent.setAction(context.getString(R.string.queueUpdated));
        context.sendBroadcast(queueUpdatedIntent);*/
        EventBus.getDefault().post(new MessageEvent(context.getString(R.string.queueUpdated)));
    }

    public ArrayList<HashMap<String,String>> getCurrentList(){
        return currentQueue;
    }

    private void setMediaSource(){
        try {
            mediaPlayer.setDataSource(currentQueue.get(position).get("data"));
            Log.d("playing song url",currentQueue.get(position).get("data"));
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
                //context.sendBroadcast(musicStoppedIntent);
                EventBus.getDefault().post(new MessageEvent("musicStopped"));
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
        //iniatializeBroadcasts(context);
        newMediaPlayer(context);
        setMediaSource();
        //context.sendBroadcast(newSongLoadIntent);
        EventBus.getDefault().post(new MessageEvent("newSongLoaded"));

        RecentPlayTable.getInstance(context).addSongToRecentPlayList(currentSongDetails);

        //UsageDataTable.getInstance(context).updateUsageData(currentSongDetails);

        NotificationManager.getInstance().postNotificationName(NotificationManager.audioDidStarted, getCurrentSongDetails());


            Intent intent = new Intent(VinPlayer.applicationContext, VinMedia.class);
            VinPlayer.applicationContext.startService(intent);
        NotificationManager.getInstance().notifyNewSongLoaded(NotificationManager.newaudioloaded, getCurrentSongDetails());
    }

    public void pauseMusic(Context context){
        mediaPlayer.pause();
        //context.sendBroadcast(songPausedIntent);
        EventBus.getDefault().post(new MessageEvent("songPaused"));
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
       // context.sendBroadcast(songResumedIntent);
        EventBus.getDefault().post(new MessageEvent("songResumed"));
        Log.d("Broadcast","music paused");
    }

    public void nextSong(Context context){
        media_settings = context.getSharedPreferences(context.getString(R.string.media_settings),Context.MODE_PRIVATE);
        repeatmode = media_settings.getInt(context.getString(R.string.repeat_mode),REPEAT_NONE);
        is_shuffle = media_settings.getBoolean(context.getString(R.string.shuffle),SHUFFLE_OFF);
        if (!is_shuffle){
            if (position==currentQueue.size()-1){
                if (repeatmode==REPEAT_NONE)return;
                if (repeatmode==REPEAT_QUEUE)position=0;
            }else if ((position+1)<currentQueue.size()){
                if (repeatmode!=REPEAT_TRACK)position++;
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

        FavouriteTable.getInstance(context).addSongToFavourite(currentSongDetails);
        LastPlayTable.getInstance(context).storeLastPlayQueue(currentQueue,getPosition());
       // NextSongDataTable.getInstance(context).initializeNextSongData();

        PlaylistTable.getInstance(context).addSongToPlaylist(currentSongDetails, "MyPlaylist");

        UsageDataTable.getInstance(context).updateUsageData(currentSongDetails,3,30,true,false,true);

        NextSongDataTable.getInstance(context).addNextSongData(Long.parseLong(15623+""),Long.parseLong(15625+""));


    }

    public void previousSong(Context context){
        media_settings = context.getSharedPreferences(context.getString(R.string.media_settings),Context.MODE_PRIVATE);
        repeatmode = media_settings.getInt(context.getString(R.string.repeat_mode),0);
        is_shuffle = media_settings.getBoolean(context.getString(R.string.shuffle),false);
        if (!is_shuffle) {
            if (position == 0) {
                if (repeatmode == REPEAT_QUEUE) position = currentQueue.size() - 1;
            } else if ((position - 1) >= 0) {
                if (repeatmode != REPEAT_TRACK) position--;
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

        new createRecommendedList().execute("");

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
        if (mediaPlayer!=null)return mediaPlayer.getDuration();
        else return 0;
        //return /*(getCurrentSongDetails()!=null)? Integer.parseInt(getCurrentSongDetails().get("duration")):0*/0;
    }

    public void releasePlayer(){
        if (mediaPlayer!=null){
            mediaPlayer.release();
            Intent intent = new Intent(VinPlayer.applicationContext, VinMedia.class);
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

    public boolean isHeadPhonePlugged(){
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        sticky = true;
        Log.d("service","started");
        EventBus.getDefault().register(this);
        //setupBroadCastReceiver();
        play = BitmapFactory.decodeResource(getResources(),R.drawable.icon_play);
        pause = BitmapFactory.decodeResource(getResources(),R.drawable.icon_pause);
        notificationManager = (android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        try {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (VinMedia.getInstance().isPlaying()) {
                            VinMedia.getInstance().pauseMusic(getApplicationContext());
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("abcde","onstart command");
        this.intent = intent;
        this.flags = flags;
        this.startId = startId;

        try {
            HashMap<String,String> messageObject = VinMedia.getInstance().getCurrentSongDetails();
            if (messageObject == null) {
                /*VinPlayerUtility.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                });*/

                return START_NOT_STICKY;
            }

            if (supportLockScreenControls) {
                ComponentName remoteComponentName = new ComponentName(getApplicationContext(), "VinPlayer");
                try {
                    if (remoteControlClient == null) {
                        audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                        mediaButtonIntent.setComponent(remoteComponentName);
                        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                        remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                        audioManager.registerRemoteControlClient(remoteControlClient);
                    }
                    remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                            | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS | RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
                } catch (Exception e) {
                    Log.e("tmessages", e.toString());
                }
            }
            createNotification(messageObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotification(HashMap<String, String> mSongDetail) {
        try {
            String songName = mSongDetail.get("title");
            String authorName = mSongDetail.get("artist");
            String albumName = mSongDetail.get("title");
            HashMap<String, String> audioInfo = VinMedia.getInstance().getCurrentSongDetails();

            RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_small_notification);
            RemoteViews expandedView = null;
            if (supportBigNotifications) {
                expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_big_notification);
            }

            Intent intent = new Intent(VinPlayer.applicationContext, MainActivity.class);
            intent.setAction("openplayer");
            intent.setFlags(32768);
            PendingIntent contentIntent = PendingIntent.getActivity(VinPlayer.applicationContext, 0, intent, 0);
            PendingIntent deleteIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_CLOSE),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.icon_stop)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deleteIntent)
                    .setContentTitle(songName)
                    .setOngoing(sticky)
                    .build();

            notification.contentView = simpleContentView;
            if (supportBigNotifications) {
                notification.bigContentView = expandedView;
            }

            setListeners(simpleContentView);
            if (supportBigNotifications) {
                setListeners(expandedView);
            }

            Bitmap albumArt = audioInfo != null ? VinMediaLists.getInstance()
                    .getAlbumart(Long.parseLong(audioInfo.get("album_id")),
                            VinPlayer.applicationContext) : null;

            if (albumArt != null) {
                notification.contentView.setImageViewBitmap(R.id.player_album_art, albumArt);
                if (supportBigNotifications) {
                    notification.bigContentView.setImageViewBitmap(R.id.player_album_art, albumArt);
                }
            } else {
                notification.contentView.setImageViewResource(R.id.player_album_art, R.drawable.albumart_default);
                if (supportBigNotifications) {
                    notification.bigContentView.setImageViewResource(R.id.player_album_art, R.drawable.albumart_default);
                }
            }

            notification.contentView.setViewVisibility(R.id.player_progress_bar, View.GONE);
            notification.contentView.setViewVisibility(R.id.player_next, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.player_previous, View.VISIBLE);
            if (supportBigNotifications) {
                notification.bigContentView.setViewVisibility(R.id.player_next, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.player_previous, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.player_progress_bar, View.GONE);
            }

            notification.contentView.setTextViewText(R.id.player_song_name, songName);
            notification.contentView.setTextViewText(R.id.player_author_name, authorName);
            if (supportBigNotifications) {
                notification.bigContentView.setTextViewText(R.id.player_song_name, songName);
                notification.bigContentView.setTextViewText(R.id.player_author_name, authorName);
//                notification.bigContentView.setTextViewText(R.id.player_albumname, albumName);
            }


//
//            if (VinMedia.getInstance().isPlaying()) {
//                notification.contentView.setImageViewBitmap(R.id.player_play, pause);
//                if (supportBigNotifications)
//                    notification.bigContentView.setImageViewBitmap(R.id.player_play,pause);
//            }
//            else {
//                notification.contentView.setImageViewBitmap(R.id.player_play,play);
//                if (supportBigNotifications)
//                    notification.bigContentView.setImageViewBitmap(R.id.player_play,play);
//            }

            /*notification.flags |= Notification.FLAG_ONGOING_EVENT;
            startForeground(5, notification);*/

            notificationManager.notify(5,notification);

            if (remoteControlClient != null) {
                RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, authorName);
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, songName);
 /*               if (audioInfo != null && audioInfo.getCover(ApplicationVinPlayer.applicationContext) != null) {
                    metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
                            audioInfo.getCover(ApplicationVinPlayer.applicationContext));
                }*/
                metadataEditor.apply();
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*private void setupBroadCastReceiver() {

        intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.newSongLoaded));
        intentFilter.addAction(getString(R.string.songPaused));
        intentFilter.addAction(getString(R.string.songResumed));
        intentFilter.addAction(getString(R.string.musicStopped));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(getString(R.string.newSongLoaded))) {
                    onNewSongLoaded();
                } else if (action.equals(getString(R.string.songPaused))) {
                    onSongPaused();
                } else if (action.equals(getString(R.string.songResumed))) {
                    onSongResumed();
                } else if (action.equals(getString(R.string.musicStopped))) {
                    onMusicStopped();
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }*/

    private void onNewSongLoaded() {
        sticky = true;
    }

    private void onSongPaused() {
        Log.d("abcde","paused");
        sticky = false;
        onStartCommand(intent,flags,startId);
    }

    private void onSongResumed() {
        sticky = true;
        Log.d("abcde","resumed");
        onStartCommand(intent,flags,startId);
    }

    private void onMusicStopped() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
EventBus.getDefault().unregister(this);
        Log.d("vinmusicservice","destroyed");
        if (remoteControlClient != null) {
            RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
            metadataEditor.clear();
            metadataEditor.apply();
            audioManager.unregisterRemoteControlClient(remoteControlClient);
            audioManager.abandonAudioFocus(this);
        }
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioPlayStateChanged);
        unregisterReceiver(broadcastReceiver);
    }


    public void setListeners(RemoteViews view) {
        try {

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PREVIOUS),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_previous, pendingIntent);

            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_next, pendingIntent);

            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_play, pendingIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (VinMedia.getInstance().isPlaying()) {
                VinMedia.getInstance().getInstance().pauseMusic(getApplicationContext());
                setAudioFocus();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (AudioFocus){
                VinMedia.getInstance().resumeMusic(getApplicationContext());
                AudioFocus = false;
            }
        }
    }

    public static void setAudioFocus() {
        AudioFocus = true;
    }


    private class createRecommendedList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            RecommendedTable.getInstance(VinPlayer.applicationContext).createRecommendationsList();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto yo
            // u
            Log.d("async task","post execute");
        }

        @Override
        protected void onPreExecute() {
                Log.d("async task","pre execute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.d("creating list",values.toString()+"\t");
        }
    }



    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show();
        String action = event.message;
        if (action.equals(getString(R.string.newSongLoaded))) {
            onNewSongLoaded();
        } else if (action.equals(getString(R.string.songPaused))) {
            onSongPaused();
        } else if (action.equals(getString(R.string.songResumed))) {
            onSongResumed();
        } else if (action.equals(getString(R.string.musicStopped))) {
            onMusicStopped();
        }
    }

}
