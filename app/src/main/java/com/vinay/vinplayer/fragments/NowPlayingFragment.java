package com.vinay.vinplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AllSongsAdapter;
import com.vinay.vinplayer.helpers.VinMedia;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class NowPlayingFragment extends Fragment implements View.OnClickListener {


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

    Thread thread;
    Handler handler;

    Boolean isNowPlayingSeekingDone = false;
    int nowPlayingSeekBarProgress;
    static VinMedia vinMedia;
    private IntentFilter intentFilter;
    private boolean seekbarDragging = false;
    private BroadcastReceiver broadcastReceiver;


    private OnNowPlayingFragmentInteractionListener mListener;

    public NowPlayingFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NowPlayingFragment newInstance(VinMedia vinMedia1) {
        NowPlayingFragment fragment = new NowPlayingFragment();
        vinMedia = vinMedia1;
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

        nowPlayingSongDetails = (TextView)view.findViewById(R.id.nowplaying_songdetails);
        nowPlayingSongTitle = (TextView)view.findViewById(R.id.nowplaying_songtitle);

        playerCurrentDuration = (TextView)view.findViewById(R.id.nowplaying_song_currentduration);
        playerTotalDuration = (TextView)view.findViewById(R.id.nowplaying_song_duration);

        playerSeekbar = (SeekBar)view.findViewById(R.id.nowplaying_seekbar);

        playerButtonPlayPause = (ImageButton)view.findViewById(R.id.nowplaying_button_play_pause);
        playerButtonPrevious = (ImageButton)view.findViewById(R.id.nowplaying_button_previous);
        playerButtonNext = (ImageButton)view.findViewById(R.id.nowplaying_button_next);

        playerButtonPlayPause.setOnClickListener(this);
        playerButtonPrevious.setOnClickListener(this);
        playerButtonNext.setOnClickListener(this);

        playerCurrentDuration.setText("");

        nowPlayingSongTitle.setTextColor(Color.WHITE);
        playerCurrentDuration.setTextColor(Color.WHITE);
        playerTotalDuration.setTextColor(Color.WHITE);


        playerButtonPlayPause.setColorFilter(Color.WHITE);
        playerButtonNext.setColorFilter(Color.WHITE);
        playerButtonPrevious.setColorFilter(Color.WHITE);

        albumArtPager = (ViewPager)view.findViewById(R.id.nowplaying_albumart_pager);
        albumArtPagerAdapter = new AlbumArtPagerAdapter(getActivity().getSupportFragmentManager());
        albumArtPager.setAdapter(albumArtPagerAdapter);


        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nowPlayingSeekBarProgress = progress;
                playerCurrentDuration.setText( vinMedia.getDuration()!= 0 ? String.format(Locale.ENGLISH,"%d:%02d",
                        progress / 60, progress % 60) : "-:--");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                   seekbarDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarDragging = false;
                vinMedia.setAudioProgress(nowPlayingSeekBarProgress*1000);
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
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state==ViewPager.SCROLL_STATE_IDLE){
                    if ((albumart_pos - vinMedia.getPosition())>0)
                        vinMedia.nextSong();
                    else if((albumart_pos - vinMedia.getPosition())<0)
                        vinMedia.previousSong();
                }
            }
        });
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

    private void onNewSongLoaded(){

        HashMap<String,String> songDetails = vinMedia.getCurrentSongDetails();
        Log.d("songdetails","null Data");
        if (songDetails!=null) {

            nowPlayingSongTitle.setText(songDetails.get("title"));
            nowPlayingSongDetails.setText(songDetails.get("album")+ "\t-\t" + songDetails.get("artist") );

            int duration = vinMedia.getDuration() / 1000;
            playerTotalDuration.setText(vinMedia.getDuration() != 0 ? String.format(Locale.ENGLISH, "%d:%02d",
                    duration / 60, duration % 60) : "-:--");

            playerSeekbar.setMax(duration);

            //albumArtPagerAdapter = new AlbumArtPagerAdapter(getActivity().getSupportFragmentManager());
            albumArtPagerAdapter.notifyDataSetChanged();
            albumArtPager.setAdapter(albumArtPagerAdapter);
            albumArtPager.setCurrentItem(vinMedia.getPosition());

            playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));

            handler = new Handler();
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //Log.d("handler","running");
                    try {
                        if (!seekbarDragging)playerSeekbar.setProgress(vinMedia.getAudioProgress());
                    } catch (Exception e) {

                    }
                    handler.postDelayed(this, 1000);
                }
            });

        }
    }
    private void onSongPaused(){
        playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));
    }
    private void onSongResumed(){
        playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
    }
    private void onMusicStopped(){

    }

    class AlbumArtPagerAdapter extends FragmentPagerAdapter {

        AlbumArtFragment albumArtFragment;
        public AlbumArtPagerAdapter(FragmentManager fm) {super(fm);}
        @Override
        public int getCount() {
            return ((vinMedia.getCurrentList().size()==0)?1:vinMedia.getCurrentList().size());
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
            albumArtFragment = AlbumArtFragment.newInstance(position, getActivity());
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
                    vinMedia.nextSong();
                    break;

                case R.id.nowplaying_button_previous:
                    vinMedia.previousSong();
                    break;
            }
          //  updatePlayPauseButton();
    }

    private void playPauseAction(){
        if (vinMedia.isPlaying()){
            vinMedia.pauseMusic();
        }else {
            if (vinMedia.getCurrentList().size()!=0){
                if (!vinMedia.isClean()){
                    vinMedia.resumeMusic();
                }
                else {
                    vinMedia.startMusic(vinMedia.getPosition());
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
