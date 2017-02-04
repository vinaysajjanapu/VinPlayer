package com.vinay.vinplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vinay.vinplayer.helpers.VinMedia;


public class VinMusicService extends Service {

    private VinMedia vinMedia;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public VinMusicService(VinMedia vinMedia1) {
        super();
        this.vinMedia = vinMedia1;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (vinMedia.getCurrentList()!=null)
            Log.d("service",vinMedia.getCurrentList().size()+"");



        return super.onStartCommand(intent, flags, startId);
    }

}
