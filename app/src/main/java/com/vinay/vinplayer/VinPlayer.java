package com.vinay.vinplayer;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

import com.nostra13.universalimageloader.core.ImageLoader;

import static com.vinay.vinplayer.helpers.ImageLoaderOptions.config;

/**
 * Created by vinaysajjanapu on 5/2/17.
 */
public class VinPlayer extends Application {

    public static Context applicationContext = null;
    public static volatile Handler applicationHandler = null;
    public static String default_art = "drawable://"+R.drawable.albumart_default;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());

        ImageLoader.getInstance().init(config); // Get singleton instance

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


}
