package com.vinay.vinplayer.helpers;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;

/**
 * Created by vinaysajjanapu on 9/2/17.
 */

public class ImageLoaderOptions {

    public static DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            .cacheOnDisc(true)
            .showImageOnFail(R.drawable.albumart_default)
            .showImageForEmptyUri(R.drawable.albumart_default)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();

    public static ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
            VinPlayer.applicationContext)
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCache(new WeakMemoryCache())
            .discCacheSize(100 * 1024 * 1024).build();
}
