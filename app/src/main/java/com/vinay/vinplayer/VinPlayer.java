package com.vinay.vinplayer;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;


import com.vinay.vinplayer.database.VinDBHelper;


/**
 * Created by vinaysajjanapu on 5/2/17.
 */
public class VinPlayer extends Application {

    public static Context applicationContext = null;
    public static volatile Handler applicationHandler = null;
    public static String default_art = "drawable://"+R.drawable.albumart_default;

    public static String playpath = "";

    @Override
    public void onCreate() {
        super.onCreate();
        if(applicationContext==null){

        applicationContext = getApplicationContext();
        }
        applicationHandler = new Handler(applicationContext.getMainLooper());

        initilizeDB();

        //ImageLoader.getInstance().init(config); // Get singleton instance

    }

    public static Context getGlobalAppContext() {
        return VinPlayer.applicationContext;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    public VinDBHelper DB_HELPER;

    @Override
    public void onTerminate() {
        closeDB();
        super.onTerminate();
    }

    private void initilizeDB() {
        if (DB_HELPER == null) {
            DB_HELPER = new VinDBHelper(VinPlayer.this);
        }
        try {
            DB_HELPER.getWritableDatabase();
            DB_HELPER.openDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeDB() {
        try {
            DB_HELPER.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
