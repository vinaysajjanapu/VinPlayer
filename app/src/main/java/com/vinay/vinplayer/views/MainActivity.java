package com.vinay.vinplayer.views;

import android.content.ContentUris;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.AlbumsFragment;
import com.vinay.vinplayer.fragments.AllSongsFragment;
import com.vinay.vinplayer.fragments.ArtistsFragment;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements
        AllSongsFragment.OnListFragmentInteractionListener,
        AlbumsFragment.OnAlbumFragmentInteractionListner,ArtistsFragment.OnArtistFragmentInteractionListner, View.OnClickListener {

    ArrayList<HashMap<String, String>> songs;

    Button start, pause, stop;
    VinMedia vm;

    ViewPager librayViewPager;
    TabLayout tabLayout;
    List<Fragment> mFragmentList = new ArrayList<>();
    List<String> titles=new ArrayList<>();
    VinMediaLists vinMediaLists ;
    ViewPagerAdapter adapter;

    SlidingUpPanelLayout slidingUpPanelLayout;

    RelativeLayout sliderPlayer;
    SeekBar sliderPlayer_seekbar;
    CircleImageView sliderPlayer_albumart;
    TextView sliderPlayer_songtitle;
    TextView sliderPlayer_songdetails;
    ImageButton sliderPlayer_playpause;

    ImageView nowPlayingAlbumArt;
    TextView nowPlayingSongDetails;
    TextView nowPlayingSongTitle;
    TextView playerCurrentDuration;
    TextView playerTotalDuration;

    SeekBar playerSeekbar;
    ImageButton playerButtonPrevious;
    ImageButton playerButtonNext;
    ImageButton playerButtonPlayPause;
    RelativeLayout slider;

    Bitmap blurredBitmap, blurredBitmap1;
    Bitmap input_to_blur;
    Bitmap temp_input;
    Bitmap default_bg;
    Drawable dr,dr1;

//    String contentURI=null;


    boolean firstback=false,secondback=false;

    Thread thread;
    Handler handler;

    Boolean isNowPlayingSeekingDone = false;
    int nowPlayingSeekBarProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        songs = new ArrayList<>();
        vm = new VinMedia(this);
        vm.VinMediaInitialize();

        vinMediaLists=new VinMediaLists(this);

        setupLibraryViewPager();
        setupSlidingPanelLayout();

    }

    private void setupLibraryViewPager(){

        librayViewPager = (ViewPager) findViewById(R.id.viewpager);
        mFragmentList.add(AllSongsFragment.newInstance(1, VinMedia.currentList));
        mFragmentList.add(AlbumsFragment.newInstance( vinMediaLists.getAlbumsList()));
        mFragmentList.add(ArtistsFragment.newInstance( vinMediaLists.getArtistsList()));
        titles.add("all songs");
        titles.add("albums");
        titles.add("artists");

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        librayViewPager.setAdapter(adapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(librayViewPager);

    }

    private void setupSlidingPanelLayout(){

        slidingUpPanelLayout = (SlidingUpPanelLayout)findViewById(R.id.slidingpanel_layout);

        sliderPlayer = (RelativeLayout)findViewById(R.id.slider_playingdetails);
        sliderPlayer_seekbar = (SeekBar)findViewById(R.id.slider_seekBar);
        sliderPlayer_albumart = (CircleImageView)findViewById(R.id.albumart_slider_playingsong);
        sliderPlayer_songtitle = (TextView)findViewById(R.id.slider_playingsong_songname);
        sliderPlayer_songdetails = (TextView)findViewById(R.id.slider_playing_songdetails);
        sliderPlayer_playpause = (ImageButton)findViewById(R.id.slider_playing_button_playpause);

        nowPlayingAlbumArt = (ImageView)findViewById(R.id.albumart_nowplaying);

        nowPlayingSongDetails = (TextView)findViewById(R.id.nowplaying_songdetails);
        nowPlayingSongTitle = (TextView)findViewById(R.id.nowplaying_songtitle);

        playerCurrentDuration = (TextView)findViewById(R.id.nowplaying_song_currentduration);
        playerTotalDuration = (TextView)findViewById(R.id.nowplaying_song_duration);
        playerSeekbar = (SeekBar)findViewById(R.id.nowplaying_seekbar);
        playerButtonPlayPause = (ImageButton)findViewById(R.id.nowplaying_button_play_pause);
        playerButtonPrevious = (ImageButton)findViewById(R.id.nowplaying_button_previous);
        playerButtonNext = (ImageButton)findViewById(R.id.nowplaying_button_next);
        slider = (RelativeLayout)findViewById(R.id.slider);

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.5f) {
                    sliderPlayer.setAlpha(1.0f-2*slideOffset);
                    sliderPlayer.setVisibility(View.VISIBLE);

                } else if (slideOffset > 0.5f && slideOffset < 1.0f) {
                    sliderPlayer.setVisibility(View.INVISIBLE);
                } else {

                }
            }
            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        sliderPlayer_playpause.setOnClickListener(this);
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
 //       sliderPlayer_playpause.setColorFilter(Color.WHITE);


        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("seekbar",progress+"");
                nowPlayingSeekBarProgress = (progress*vm.getDuration())/100;
                int duration  = nowPlayingSeekBarProgress/1000;
                playerCurrentDuration.setText( vm.getDuration()!= 0 ? String.format(Locale.ENGLISH,"%d:%02d",
                        duration / 60, duration % 60) : "-:--");


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("seekbar","tracking started");


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                vm.setAudioProgress(nowPlayingSeekBarProgress);
                Log.d("seekbar","tracking stopped");
            }
        });

        updateUI();
    }

    private void updateUI(){
        HashMap<String,String> songDetails = vm.getCurrentSongDetails();
        Log.d("songdetails","null Data");
        if (songDetails!=null) {
            Log.d("songdetails",songDetails.toString());
            sliderPlayer_songtitle.setText(songDetails.get("title"));
            sliderPlayer_songdetails.setText(songDetails.get("artist") + "\t\t" + songDetails.get("album"));

            if (vm.isPlaying())
                sliderPlayer_playpause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
            else
                sliderPlayer_playpause.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));

            nowPlayingSongTitle.setText(songDetails.get("title"));
            nowPlayingSongDetails.setText(songDetails.get("artist") + "\t\t" + songDetails.get("album"));

            if (vm.isPlaying())
                playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
            else
                playerButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));

            int duration  = vm.getDuration()/1000;
            playerTotalDuration.setText( vm.getDuration()!= 0 ? String.format(Locale.ENGLISH,"%d:%02d",
                            duration / 60, duration % 60) : "-:--");



            try {
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(songDetails.get("album_id")));

                Picasso.with(this).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                        .into(sliderPlayer_albumart);
                Picasso.with(this).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                        .into(nowPlayingAlbumArt);

                createBlurredBackground(uri.toString());

            }catch (Exception e){
e.printStackTrace();
            }
            // playerCurrentDuration.setText("");

        }
    }

    private void createBlurredBackground(String url) {
        //  Bitmap emptyBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), myBitmap.getConfig());
        //default_bg = BitmapFactory.decodeFile(url);
        if(url!=null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            default_bg = null;
            try {
                Uri uri = Uri.parse(url);
                ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    default_bg = BitmapFactory.decodeFileDescriptor(fd);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //default_bg = ((BitmapDrawable)songAlbumbg.getDrawable()).getBitmap();
            if (default_bg == null) {
                default_bg = BitmapFactory.decodeResource(getResources(), R.drawable.albumart_default);
            }
        }else {
            default_bg = BitmapFactory.decodeResource(getResources(), R.drawable.albumart_default);
        }
        temp_input = default_bg.copy(Bitmap.Config.ARGB_8888, true);
        input_to_blur = Bitmap.createScaledBitmap(temp_input, 300, 300, false);
        blurredBitmap = BlurBuilder.blur( this, input_to_blur );
        blurredBitmap1 = BlurBuilder.blur( this, input_to_blur );

        //BitmapDrawable background = new BitmapDrawable(context.getResources(),default_bg);
        dr=new BitmapDrawable(blurredBitmap);
        dr1=new BitmapDrawable(blurredBitmap1);
slider.setBackground(dr);
        dr1 = null;
        dr = null;
        temp_input = null;
        input_to_blur = null;
        blurredBitmap = null;
        blurredBitmap1 = null;
        //view.setBackgroundDrawable( new BitmapDrawable( getResources(), blurredBitmap ) );
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
                vm.nextSong();
                updateUI();
                break;

            case R.id.nowplaying_button_previous:
                vm.previousSong();
                updateUI();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        vm.VinMediaInitialize();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vm.releasePlayer();
        vm = null;
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    @Override
    public void onListFragmentInteraction(int p) {

        if(vm.isPlaying()){
            vm.resetPlayer();
        }
        playPauseAction(p);
        updateUI();
    }

    @Override
    public void OnAlbumFragmentInteraction(int pos) {

        mFragmentList.remove(1);
        vm.changeCurrentList(pos,1);
        mFragmentList.add(AllSongsFragment.newInstance(1,
                vinMediaLists.getAlbumSongsList((VinMediaLists.allAlbums.get(pos).get("album"))) ));

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        librayViewPager.setAdapter(adapter);
    }

    @Override
    public void OnArtistFragmentInteraction(int pos) {
        mFragmentList.remove(1);
        vm.changeCurrentList(pos,2);
        mFragmentList.add(AllSongsFragment.newInstance(1,
                vinMediaLists.getArtistSongsList((VinMediaLists.allArtists.get(pos).get("artist"))) ));

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        librayViewPager.setAdapter(adapter);
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }



    public void playPauseAction(){
        if (vm.isPlaying()){
            vm.pauseMusic();
        }else {
            if (VinMedia.currentList.size()!=0){
                if (!vm.isClean()){
                    vm.resumeMusic();
                }
                else {
                    vm.startMusic(vm.getPosition());
                }
            }
        }

        updateUI();
    }

    public void playPauseAction(int position){
        //this.position = position;
        vm.setPosition(position);
        if (vm.isPlaying()||!vm.isClean()){
            vm.stopMusic();
            vm.resetPlayer();
            vm.startMusic(position);
        }else {
            vm.startMusic(position);
        }

        updateUI();
    }


}
