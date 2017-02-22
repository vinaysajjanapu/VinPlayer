package com.vinay.vinplayer.helpers;

import org.greenrobot.eventbus.Subscribe;

/**
 * Created by salimatti on 2/22/2017.
 */

public class MessageEvent {

    public final String message;

    public MessageEvent(String message) {
        this.message = message;
    }
}