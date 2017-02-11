package com.vinay.vinplayer.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.views.MainActivity;

import java.util.HashMap;

/**
 * Created by vinaysajjanapu on 5/2/17.
 */

public class VinMusicService extends Service implements AudioManager.OnAudioFocusChangeListener {

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



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        sticky = true;
        Log.d("service","started");
        setupBroadCastReceiver();
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


    private void setupBroadCastReceiver() {

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
    }

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
                /*VinMedia.getInstance().getInstance().pauseMusic(getApplicationContext());
                setAudioFocus();*/
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
             if (AudioFocus){/*
                 VinMedia.getInstance().resumeMusic(getApplicationContext());
                 AudioFocus = false;*/
             }
        }
    }

    public static void setAudioFocus() {
        AudioFocus = true;
    }

}
