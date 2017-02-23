
package com.vinay.vinplayer.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.vinay.vinplayer.VinPlayer;

public class VinPlayerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			if (intent.getExtras() == null) {
				return;
			}
			KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
			if (keyEvent == null) {
				return;
			}
			if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
				return;

			switch (keyEvent.getKeyCode()) {
			case KeyEvent.KEYCODE_HEADSETHOOK:
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				if (!VinMedia.getInstance().isPlaying()&&!VinMedia.getInstance().isClean()) {
					VinMedia.getInstance().resumeMusic(context);
				} else {
					VinMedia.getInstance().pauseMusic(context);
				}
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				VinMedia.getInstance().resumeMusic(context);
				break;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				VinMedia.getInstance().pauseMusic(context);
				break;
			case KeyEvent.KEYCODE_MEDIA_STOP:
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				VinMedia.getInstance().nextSong(context);
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				VinMedia.getInstance().previousSong(context);
				break;
			}
		} else {
			if (intent.getAction().equals(VinMedia.NOTIFY_PLAY)) {
					if (!VinMedia.getInstance().isPlaying()){
						VinMedia.getInstance().resumeMusic(context);
					}else {
						VinMedia.getInstance().pauseMusic(context);
					}
			}
			else if (intent.getAction().equals(VinMedia.NOTIFY_NEXT)) {
				VinMedia.getInstance().nextSong(context);
			}
			else if (intent.getAction().equals(VinMedia.NOTIFY_CLOSE)) {
				Log.d("notification","close it");
				Intent intent1 = new Intent(VinPlayer.applicationContext, VinMedia.class);
				VinPlayer.applicationContext.stopService(intent1);
				VinMedia.getInstance().resetPlayer(VinMedia.getInstance().getMediaPlayer());
			}
			else if (intent.getAction().equals(VinMedia.NOTIFY_PREVIOUS)) {
				VinMedia.getInstance().previousSong(context);
			}
		}
	}
}
