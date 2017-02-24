package com.vinay.vinplayer.helpers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vinay.vinplayer.activities.MainActivity;

/**
 * Created by vinaysajjanapu on 24/2/17.
 */

public class HeadPhoneDetectService extends Service {
    HeadPhoneReceiver headPhoneReceiver;
    String LOGTAG = "HeadPhoneDetectService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public HeadPhoneDetectService(){
        headPhoneReceiver = new HeadPhoneReceiver();
    }
    class HeadPhoneReceiver extends BroadcastReceiver {
        HeadPhoneReceiver() {

        }

        public void onReceive(Context arg0, Intent intent) {
            if (intent.hasExtra("state") /*&& !isInitialStickyBroadcast()*/) {
                if (intent.getIntExtra("state", 0) == 0) {
                    if (VinMedia.getInstance().isPlaying())
                        VinMedia.getInstance().pauseMusic(arg0);
                    Log.d(LOGTAG,"disconn");
                }else {
                    Log.d(LOGTAG,"connected");
                    Intent i = new Intent(arg0, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    arg0.startActivity(i);
                }
            }
        }
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.intent.action.MEDIA_BUTTON");
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(this.headPhoneReceiver, intentFilter);
        Log.d(LOGTAG,"service started");
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }
}
