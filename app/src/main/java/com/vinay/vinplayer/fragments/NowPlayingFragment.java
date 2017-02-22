package com.vinay.vinplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.anim.AccordionTransformer;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.VinMedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class NowPlayingFragment extends Fragment implements View.OnClickListener{


    ViewPager albumArtPager;
    AlbumArtPagerAdapter albumArtPagerAdapter;

    TextView nowPlayingSongDetails;
    TextView nowPlayingSongTitle;
    TextView playerCurrentDuration;
    TextView playerTotalDuration;
    SeekBar playerSeekbar;
    ImageButton playerButtonPrevious;
    ImageButton playerButtonNext;
    ImageButton playerButtonPlayPause;

    ImageButton playerButtonShuffle;
    ImageButton playerButtonRepeat;

    ArrayList<HashMap<String,String>> current_queue;
    Thread thread;
    Handler handler;
    Runnable timerRun;
    Boolean isNowPlayingSeekingDone = false;
    int nowPlayingSeekBarProgress;
    private IntentFilter intentFilter;
    private boolean seekbarDragging = false;
    private BroadcastReceiver broadcastReceiver;

    private SharedPreferences media_settings;
    private SharedPreferences.Editor editor;

    private OnNowPlayingFragmentInteractionListener mListener;

    public NowPlayingFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NowPlayingFragment newInstance() {
        NowPlayingFragment fragment = new NowPlayingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nowplaying, container, false);
        setupViews(view);
        setupBroadCastReceiver();

        return view;
    }
    private void setupViews(View view){

        media_settings = getActivity().getSharedPreferences(getString(R.string.media_settings),MODE_PRIVATE);
        editor = media_settings.edit();

        nowPlayingSongDetails = (TextView)view.findViewById(R.id.nowplaying_songdetails);
        nowPlayingSongTitle = (TextView)view.findViewById(R.id.nowplaying_songtitle);

        playerCurrentDuration = (TextView)view.findViewById(R.id.nowplaying_song_currentduration);
        playerTotalDuration = (TextView)view.findViewById(R.id.nowplaying_song_duration);

        playerSeekbar = (SeekBar)view.findViewById(R.id.nowplaying_seekbar);

        playerButtonPlayPause = (ImageButton)view.findViewById(R.id.nowplaying_button_play_pause);
        playerButtonPrevious = (ImageButton)view.findViewById(R.id.nowplaying_button_previous);
        playerButtonNext = (ImageButton)view.findViewById(R.id.nowplaying_button_next);

        playerButtonShuffle = (ImageButton)view.findViewById(R.id.nowplaying_button_shuffle);
        playerButtonRepeat = (ImageButton)view.findViewById(R.id.nowplaying_button_repeat);

        playerButtonPlayPause.setOnClickListener(this);
        playerButtonPrevious.setOnClickListener(this);
        playerButtonNext.setOnClickListener(this);
        playerButtonShuffle.setOnClickListener(this);
        playerButtonRepeat.setOnClickListener(this);

        playerCurrentDuration.setText("");

        nowPlayingSongTitle.setTextColor(Color.WHITE);
        playerCurrentDuration.setTextColor(Color.WHITE);
        playerTotalDuration.setTextColor(Color.WHITE);
        if (media_settings.getBoolean(getActivity().getString(R.string.shuffle),false))
            playerButtonShuffle.setColorFilter(Color.WHITE);
        else playerButtonShuffle.setColorFilter(Color.GREEN);

        if (media_settings.getInt(getActivity().getString(R.string.repeat_mode),0)==0)
            playerButtonShuffle.setColorFilter(Color.WHITE);
        else playerButtonShuffle.setColorFilter(Color.GREEN);

        playerButtonRepeat.setColorFilter(Color.WHITE);

        playerButtonPlayPause.setColorFilter(Color.WHITE);
        playerButtonNext.setColorFilter(Color.WHITE);
        playerButtonPrevious.setColorFilter(Color.WHITE);


        current_queue = new ArrayList<>();

        albumArtPager = (ViewPager)view.findViewById(R.id.nowplaying_albumart_pager);

        albumArtPagerAdapter = new AlbumArtPagerAdapter(getActivity().getSupportFragmentManager());

        albumArtPager.setAdapter(albumArtPagerAdapter);
        albumArtPager.setPageTransformer(true,new AccordionTransformer());

        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nowPlayingSeekBarProgress = progress;
                playerCurrentDuration.setText( VinMedia.getInstance().getDuration()!= 0 ? String.format(Locale.ENGLISH,"%d:%02d",
                        progress / 60, progress % 60) : "-:--");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                   seekbarDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarDragging = false;
                VinMedia.getInstance().setAudioProgress(nowPlayingSeekBarProgress*1000);
            }
        });


        albumArtPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int albumart_pos;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                albumart_pos = pos;
                if ((albumart_pos - VinMedia.getInstance().getPosition())>0)
                    new changeSong().execute("next");
                else if((albumart_pos - VinMedia.getInstance().getPosition())<0)
                    new changeSong().execute("prev");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state==ViewPager.SCROLL_STATE_IDLE){
                    /*if ((albumart_pos - VinMedia.getInstance().getPosition())>0)
                        new changeSong().execute("next");
                    else if((albumart_pos - VinMedia.getInstance().getPosition())<0)
                        new changeSong().execute("prev");*/
                }
            }
        });
    }

    private class changeSong extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(300);
            }catch (Exception e){

            }
            if (params[0].equals("next")){
                VinMedia.getInstance().nextSong(getActivity());
            }else {
                VinMedia.getInstance().previousSong(getActivity());
            }
            return "Executed";
        }
    }


    private void setupBroadCastReceiver(){

        intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.newSongLoaded));
        intentFilter.addAction(getString(R.string.songPaused));
        intentFilter.addAction(getString(R.string.songResumed));
        intentFilter.addAction(getString(R.string.musicStopped));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(getString(R.string.newSongLoaded))){
                    try {
                        onNewSongLoaded();
                    }catch (Exception e){

                    }
                }else if (action.equals(getString(R.string.songPaused))){
                    onSongPaused();
                }else if (action.equals(getString(R.string.songResumed))){
                    onSongResumed();
                }else if (action.equals(getString(R.string.musicStopped))){
                    onMusicStopped();
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver,intentFilter);
    }

    private void onSongPaused(){
        playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));
    }
    private void onSongResumed(){
        playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
    }
    private void onMusicStopped(){

    }

    private void onNewSongLoaded(){
        current_queue = VinMedia.getInstance().getCurrentList();
        HashMap<String,String> songDetails = VinMedia.getInstance().getCurrentSongDetails();
        Log.d("nowplaying fragment","on onew song loaded");
        if (songDetails!=null) {

            nowPlayingSongTitle.setText(songDetails.get("title"));
            nowPlayingSongDetails.setText(songDetails.get("album")+ "\t-\t" + songDetails.get("artist") );

            int duration = VinMedia.getInstance().getDuration() / 1000;
            playerTotalDuration.setText(duration != 0 ? String.format(Locale.ENGLISH, "%d:%02d",
                    duration / 60, duration % 60) : "-:--");

            playerSeekbar.setMax(duration);

            //albumArtPagerAdapter = new AlbumArtPagerAdapter(getActivity().getSupportFragmentManager());
            albumArtPagerAdapter.notifyDataSetChanged();
            albumArtPager.setAdapter(albumArtPagerAdapter);
            albumArtPager.setCurrentItem(VinMedia.getInstance().getPosition());

            playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));

            if (handler==null)
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
             //       Log.d("handler","running");
                    if (!seekbarDragging)playerSeekbar.setProgress(VinMedia.getInstance().getAudioProgress());
                }
            };
            if (timerRun==null)
            timerRun = new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(1000);
                            handler.sendEmptyMessage(0);
                        }catch (Exception e){

                        }
                    }
                }
            };
            if (thread==null)thread = new Thread(timerRun);
            if (!thread.isAlive())thread.start();
        }
    }


    class AlbumArtPagerAdapter extends FragmentStatePagerAdapter {

        AlbumArtFragment albumArtFragment;
        public AlbumArtPagerAdapter(FragmentManager fm) {super(fm);}
        @Override
        public int getCount() {
            return (current_queue == null)? 1 :current_queue.size();
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            String album_id = "def";
            if (current_queue != null)
                album_id = current_queue.get(position).get("album_id");
            albumArtFragment = AlbumArtFragment.newInstance(position, getActivity(),album_id);
            return albumArtFragment;
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (thread!=null){
            if (thread.isAlive())thread.interrupt();
        }
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
                case R.id.slider_playing_button_playpause:
                    playPauseAction();
                    break;

                case R.id.nowplaying_button_play_pause:
                    playPauseAction();
                    break;

                case R.id.nowplaying_button_next:
                    VinMedia.getInstance().nextSong(getActivity());
                    break;

                case R.id.nowplaying_button_previous:
                    VinMedia.getInstance().previousSong(getActivity());
                    break;

                case R.id.nowplaying_button_shuffle:
                    Log.d("shuffle button","clicked");
                    if (VinMedia.getInstance().getCurrentQueueSize()!=0) {
                        if (media_settings.getBoolean(getActivity().getString(R.string.shuffle), VinMedia.SHUFFLE_ON)) {
                            playerButtonShuffle.setColorFilter(Color.WHITE);
                            editor.putBoolean(getActivity().getString(R.string.shuffle), VinMedia.SHUFFLE_OFF);
                            editor.apply();
                        } else {
                            playerButtonShuffle.setColorFilter(Color.GREEN);
                            editor.putBoolean(getActivity().getString(R.string.shuffle), VinMedia.SHUFFLE_ON);
                            editor.apply();
                            VinMedia.getInstance().createShuffleQueue();
                        }
                    }else {
                        Toast.makeText(getActivity(),"First Add few songs to the Queue",Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.nowplaying_button_repeat:
                    Log.d("repeat button","clicked");
                    switch (media_settings.getInt(getActivity().getString(R.string.repeat_mode),VinMedia.REPEAT_NONE)){
                        case 0:
                            playerButtonRepeat.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.icon_repeat_music));
                            playerButtonRepeat.setColorFilter(Color.GREEN);
                            editor.putInt(getActivity().getString(R.string.repeat_mode),VinMedia.REPEAT_QUEUE);
                            editor.apply();
                            break;
                        case 1:
                            playerButtonRepeat.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.icon_repeat_track));
                            playerButtonRepeat.setColorFilter(Color.GREEN);
                            editor.putInt(getActivity().getString(R.string.repeat_mode),VinMedia.REPEAT_TRACK);
                            editor.apply();
                            break;
                        case 2:
                            playerButtonRepeat.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.icon_repeat_music));
                            playerButtonRepeat.setColorFilter(Color.WHITE);
                            editor.putInt(getActivity().getString(R.string.repeat_mode),VinMedia.REPEAT_NONE);
                            editor.apply();
                            break;
                    }

                break;

        }
    }

    private void playPauseAction(){
        if (VinMedia.getInstance().isPlaying()){
            VinMedia.getInstance().pauseMusic(getActivity());
        }else {
            if (VinMedia.getInstance().getCurrentList()!=null) {
                if (VinMedia.getInstance().getCurrentList().size() != 0) {
                    if (!VinMedia.getInstance().isClean()) {
                        VinMedia.getInstance().resumeMusic(getActivity());
                    } else {
                        VinMedia.getInstance().startMusic(VinMedia.getInstance().getPosition(),getActivity());

                    }
                }
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNowPlayingFragmentInteractionListener {
        // TODO: Update argument type and name
        void OnNowPlayingFragmentInteraction(int i);

    }
}
