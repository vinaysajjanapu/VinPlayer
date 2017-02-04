package com.vinay.vinplayer.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;


public class VinMusicService extends IntentService {

    private static final String ACTION_FOO = "com.vinay.vinplayer.services.action.FOO";

    MediaPlayer mediaPlayer;

    public VinMusicService() {
        super("VinMusicService");
    }


    public static void startmusicPlayer(Context context) {
        Intent intent = new Intent(context, VinMusicService.class);
        intent.setAction(ACTION_FOO);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {

                handleActionFoo();
            }
        }
    }



    private void handleActionFoo() {
       mediaPlayer=new MediaPlayer();

        throw new UnsupportedOperationException("Not yet implemented");
    }


}
