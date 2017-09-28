package com.vinay.vinplayer.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.mxn.soul.flowingdrawer_core.FlowingMenuLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.activities.settings.MainSettings;
import com.vinay.vinplayer.anim.AccordionTransformer;
import com.vinay.vinplayer.database.LastPlayTable;
import com.vinay.vinplayer.database.RecommendedTable;
import com.vinay.vinplayer.fragments.AlbumsFragment;
import com.vinay.vinplayer.fragments.AllSongsFragment;
import com.vinay.vinplayer.fragments.ArtistDetailsFragment;
import com.vinay.vinplayer.fragments.ArtistsFragment;
import com.vinay.vinplayer.fragments.FoldersFragment;
import com.vinay.vinplayer.fragments.GenreFragment;
import com.vinay.vinplayer.fragments.HomeFragment;
import com.vinay.vinplayer.fragments.NowPlayingDetailsFragment;
import com.vinay.vinplayer.fragments.NowPlayingFragment;
import com.vinay.vinplayer.fragments.QueueFragment;
import com.vinay.vinplayer.helpers.ArtistImageCacheService;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.HeadPhoneDetectService;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;
import com.vinay.vinplayer.helpers.VinPlayerReceiver;
import com.vinay.vinplayer.springtablayout.SpringIndicator;
import com.vinay.vinplayer.ui_elemets.Fab;
import com.vinay.vinplayer.wifi.WiFiDirectActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements
        AllSongsFragment.OnListFragmentInteractionListener,
        AlbumsFragment.OnAlbumFragmentInteractionListner, ArtistsFragment.OnArtistFragmentInteractionListner,
        View.OnClickListener,
        QueueFragment.OnQueueFragmentInteractionListener,
        ArtistDetailsFragment.OnArtistListFragmentInteractionListener,
        GenreFragment.OnGenreFragmentInteractionListner,
        FoldersFragment.OnFoldersFragmentInteractionListener {

    ArrayList<HashMap<String, String>> songs;


    ViewPager librayViewPager;
    SpringIndicator tabLayout;
    List<Fragment> mFragmentList = new ArrayList<>();
    List<String> titles = new ArrayList<>();
    ViewPagerAdapter adapter;

    SlidingUpPanelLayout slidingUpPanelLayout;

    RelativeLayout sliderPlayer;
    ProgressBar sliderPlayer_progressBar;
    CircleImageView sliderPlayer_albumart;
    TextView sliderPlayer_songtitle;
    TextView sliderPlayer_songdetails;
    ImageButton sliderPlayer_playpause;
    FlowingMenuLayout menuLayout;

    RelativeLayout sliderDragger, nowPlaying_statusBar;

    RelativeLayout slider;

    CardView fab_listitem1,fab_listitem2,fab_listitem3;
    MaterialSheetFab materialSheetFab;
    Thread thread;
    ViewPager nowPlayingPager;
    NowPlayingPagerAdapter nowPlayingPagerAdapter;
    List<Fragment> NowPlayingFragments = new ArrayList<>();

    Button wifi;

    RelativeLayout.LayoutParams lp_now, lp_que;
    Handler handler;
    Runnable timerRun;

    SharedPreferences media_settings;
    SharedPreferences.Editor editor;

    Button drawer_random_play_button,drawer_flash_button,drawer_settings_button;


    String LOGTAG = "MainActivity";

    boolean firstback = false;
    long firstback_t;


    ImageView iv_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VinPlayerReceiver mMediaButtonReceiver = new VinPlayerReceiver();
        IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        mediaFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
//        registerReceiver(mMediaButtonReceiver, mediaFilter);

        startService(new Intent(this, HeadPhoneDetectService.class));

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        media_settings = getSharedPreferences(getString(R.string.media_settings), MODE_PRIVATE);
        editor = media_settings.edit();
        editor.putInt(getString(R.string.repeat_mode), VinMedia.REPEAT_NONE);
        editor.putBoolean(getString(R.string.shuffle), VinMedia.SHUFFLE_OFF);
        editor.apply();

        songs = new ArrayList<>();

        setupLibraryViewPager();
        setupSlidingPanelLayout();
        setupNowPlayingPager();
        slider.setBackground(BlurBuilder.getInstance().drawable_img("null", this));


        iv_search = (ImageView) findViewById(R.id.iv_search);
        iv_search.setColorFilter(Color.WHITE);
        iv_search.setOnClickListener(this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        VinPlayerReceiver r = new VinPlayerReceiver();
        filter.setPriority(1000);

        startService(new Intent(this, ArtistImageCacheService.class));

    }

    private Drawable background;
    private class loadBackground extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            background = BlurBuilder.getInstance().drawable_img(params[0],getApplicationContext());
            return "Executed";
        }
    }

    private void onNewSongLoaded() {
        HashMap<String, String> songDetails = VinMedia.getInstance().getCurrentSongDetails();

        if (songDetails != null) {
            sliderPlayer_playpause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
            sliderPlayer_songtitle.setText(songDetails.get("title"));
            sliderPlayer_songdetails.setText(songDetails.get("artist") + "\t\t" + songDetails.get("album"));
            sliderPlayer_progressBar.setMax(VinMedia.getInstance().getDuration() / 1000);

            if (handler==null)
            handler = new Handler(){

                @Override
                public void handleMessage(Message msg) {
             //       Log.d("handler","running");
                    sliderPlayer_progressBar.setProgress(VinMedia.getInstance().getAudioProgress());
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

            try {
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(songDetails.get("album_id")));

                Picasso.with(this).load(uri).fit().placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                        .into(sliderPlayer_albumart);

                new loadBackground(){
                    @Override
                    protected void onCancelled() {
                        slider.setBackground(background);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        slider.setBackground(background);
                    }
                }.execute(songDetails.get("album_id"));

            } catch (Exception e) {

            }
        }
    }

    private void onSongPaused() {
        sliderPlayer_playpause.setImageDrawable(getResources().getDrawable(R.drawable.icon_play));
    }

    private void onSongResumed() {
        sliderPlayer_playpause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
    }

    private void onMusicStopped() {

    }

    private void setupLibraryViewPager() {

        librayViewPager = (ViewPager) findViewById(R.id.viewpager);
        mFragmentList.add(HomeFragment.newInstance());
        mFragmentList.add(AllSongsFragment.newInstance(1, VinMediaLists.getInstance().getAllSongsList(this)));
        mFragmentList.add(AlbumsFragment.newInstance(VinMediaLists.getInstance().getAlbumsList(this)));
        mFragmentList.add(FoldersFragment.newInstance());
        mFragmentList.add(ArtistsFragment.newInstance(VinMediaLists.getInstance().getArtistsList(this)));
        mFragmentList.add(GenreFragment.newInstance(VinMediaLists.getInstance().getGenresList(this)));
        titles.add("home");
        titles.add("all");
        titles.add("album");
        titles.add("folders");
        titles.add("artist");
        titles.add("genre");

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        librayViewPager.setAdapter(adapter);
        tabLayout = (SpringIndicator) findViewById(R.id.tabs);
        tabLayout.setViewPager(librayViewPager);
        // set transitions
        librayViewPager.setPageTransformer(true, new AccordionTransformer());
    }


    private void getIntentData() {
        try {
            Uri data = getIntent().getData();
            if (data != null) {
                if (data.getScheme().equalsIgnoreCase("file")) {
                    String path = data.getPath();
                    path = path.replace("file:/","");
                    if (!TextUtils.isEmpty(path)){
                        ArrayList<HashMap<String,String>> arrayList;
                        arrayList = VinMediaLists.getInstance().getListByKey("data",path,this);
                        if (!arrayList.isEmpty()) {
                            VinMedia.getInstance().updateTempQueue(arrayList, this);
                            VinMedia.getInstance().updateQueue(false, this);
                            VinMedia.getInstance().setPosition(0);
                            playPauseAction(0);
                            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupSlidingPanelLayout() {

        menuLayout = (FlowingMenuLayout)findViewById(R.id.menulayout);
        drawer_random_play_button = (Button) findViewById(R.id.randomPlay);
        wifi= (Button) findViewById(R.id.wifi);
        drawer_flash_button = (Button)findViewById(R.id.flashVisualizer);
        drawer_settings_button = (Button)findViewById(R.id.settings);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingpanel_layout);
        sliderPlayer = (RelativeLayout) findViewById(R.id.slider_playingdetails);
        sliderPlayer_progressBar = (ProgressBar) findViewById(R.id.slider_progressBar);
        sliderPlayer_albumart = (CircleImageView) findViewById(R.id.albumart_slider_playingsong);
        sliderPlayer_songtitle = (TextView) findViewById(R.id.slider_playingsong_songname);
        sliderPlayer_songtitle.setSelected(true);
        sliderPlayer_songdetails = (TextView) findViewById(R.id.slider_playing_songdetails);
        sliderPlayer_songdetails.setSelected(true);
        sliderPlayer_playpause = (ImageButton) findViewById(R.id.slider_playing_button_playpause);
        nowPlaying_statusBar = (RelativeLayout) findViewById(R.id.nowplaying_status_bar);
        nowPlaying_statusBar.setVisibility(View.INVISIBLE);
        slider = (RelativeLayout) findViewById(R.id.slider);
        sliderDragger = (RelativeLayout) findViewById(R.id.slider_dragger);

        sliderPlayer_progressBar.getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

        lp_now = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        lp_que = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                actionBarHeight);


        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.5f) {
                    sliderPlayer.setAlpha(1.0f - 2 * slideOffset);
                    sliderPlayer.setVisibility(View.VISIBLE);

                    nowPlaying_statusBar.setVisibility(View.INVISIBLE);

                } else if (slideOffset > 0.5f && slideOffset < 1.0f) {
                    sliderPlayer.setVisibility(View.INVISIBLE);
                    nowPlaying_statusBar.setAlpha(slideOffset);
                    nowPlaying_statusBar.setVisibility(View.VISIBLE);
                } else {

                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (previousState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        Log.d("panel", "collapsed");
                        nowPlayingPager.setCurrentItem(1);
                    }
                }
            }
        });

        sliderPlayer_playpause.setOnClickListener(this);
        sliderPlayer_playpause.setColorFilter(Color.WHITE);


        Fab fab = (Fab) findViewById(R.id.fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = Color.WHITE;
        int fabColor = Color.BLACK;

        // Initialize material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay,
                sheetColor, fabColor);
        materialSheetFab.showFab(50, 50);

        fab_listitem1 = (CardView)findViewById(R.id.fab_listitem1);
        fab_listitem2 = (CardView)findViewById(R.id.fab_listitem2);
        fab_listitem3 = (CardView)findViewById(R.id.fab_listitem3);
        fab_listitem1.setOnClickListener(this);
        fab_listitem2.setOnClickListener(this);
        fab_listitem3.setOnClickListener(this);


        drawer_random_play_button.setOnClickListener(this);
        drawer_flash_button.setOnClickListener(this);
        wifi.setOnClickListener(this);
        drawer_settings_button.setOnClickListener(this);

    }

    private void setupNowPlayingPager() {

        nowPlayingPager = (ViewPager) findViewById(R.id.nowplaying_pager);
        nowPlayingPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    sliderDragger.setLayoutParams(lp_que);
                } else {
                    sliderDragger.setLayoutParams(lp_now);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        NowPlayingFragments.add(NowPlayingDetailsFragment.newInstance());
        NowPlayingFragments.add(NowPlayingFragment.newInstance());
        NowPlayingFragments.add(QueueFragment.newInstance(1));
        nowPlayingPagerAdapter = new NowPlayingPagerAdapter(getSupportFragmentManager());
        nowPlayingPager.setAdapter(nowPlayingPagerAdapter);
        nowPlayingPager.setCurrentItem(1);
        nowPlayingPager.setPageTransformer(true, new AccordionTransformer());

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slider_playing_button_playpause:
                playPauseAction();
                break;
            case R.id.iv_search:
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                break;
            case R.id.fab_listitem1:
                VinMedia.getInstance().updateQueue(true,getApplicationContext());
                VinMedia.getInstance().startMusic(0,getApplicationContext());
                materialSheetFab.hideSheet();
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.fab_listitem2:
                materialSheetFab.hideSheet();
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.fab_listitem3:
                VinMedia.getInstance().updateTempQueue(RecommendedTable.getInstance(this).getRecommendedList(),this);
                VinMedia.getInstance().updateQueue(false,getApplicationContext());
                VinMedia.getInstance().startMusic(0,getApplicationContext());
                materialSheetFab.hideSheet();
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.randomPlay:
                startActivity(new Intent(this,RapidPlay.class));
                overridePendingTransition(0,0);
                break;
            case R.id.wifi:
                startActivity(new Intent(this,WiFiDirectActivity.class));
                overridePendingTransition(0,0);
                break;
            case R.id.flashVisualizer:
                startActivity(new Intent(this,VisualizerFlashActivity.class));
                overridePendingTransition(0,0);
                break;
            case R.id.settings:
                startActivity(new Intent(this,MainSettings.class));
                overridePendingTransition(0,0);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getIntentData();
        loadAlreadyPlaying();
    }

    private void loadAlreadyPlaying() {
        ArrayList<HashMap<String,String>> lastplay = new ArrayList<>();
        lastplay = LastPlayTable.getInstance(this).getLastPlayQueue();
        if (lastplay!=null && lastplay.size()>0){
            if (VinMedia.getInstance().getCurrentList()==null) {
                VinMedia.getInstance().updateTempQueue(lastplay, this);
                VinMedia.getInstance().updateQueue(false, this);
                VinMedia.getInstance().setPosition(Integer.parseInt(lastplay.get(0).get("position")));
            }else {
                EventBus.getDefault().post(new MessageEvent(getString(R.string.queueUpdated)));
            }
                onNewSongLoaded();
                onSongPaused();

        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (VinMedia.getInstance().getCurrentList()!=null)
        LastPlayTable.getInstance(this).storeLastPlayQueue(VinMedia.getInstance().getCurrentList(),
                VinMedia.getInstance().getPosition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!VinMedia.getInstance().isPlaying()){
            stopService(new Intent(this,VinMedia.class));
        }
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            if (librayViewPager.getCurrentItem()!=0)
                librayViewPager.setCurrentItem(0);
            else {
                if (firstback) {
                    if (System.currentTimeMillis() - firstback_t < 1000) {
                        super.onBackPressed();
                    }
                    firstback = false;
                }
                firstback = true;
                firstback_t = System.currentTimeMillis();
                Toast.makeText(this, "Press again to quit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onListFragmentInteraction(int p) {

        if (VinMedia.getInstance().isPlaying()) {
            VinMedia.getInstance().resetPlayer(VinMedia.getInstance().getMediaPlayer());
        }
        VinMedia.getInstance().updateQueue(true, this);
        EventBus.getDefault().post(new MessageEvent(getString(R.string.queueUpdated)));
        playPauseAction(p);
    }


    @Override
    public void OnFoldersFragmentInteraction(int p) {

    }

    @Override
    public void OnQueueFragmentInteraction(int p) {

        Log.d("queue", "fragment interation  " + p);
        if (VinMedia.getInstance().isPlaying()) {
            VinMedia.getInstance().resetPlayer(VinMedia.getInstance().getMediaPlayer());
        }
        playPauseAction(p);
    }

    @Override
    public void OnAlbumFragmentInteraction(int pos) {
        String album = VinMediaLists.getInstance().getAlbumsList(this).get(pos).get("album");
        VinMedia.getInstance().updateTempQueue(VinMediaLists.getInstance()
                .getAlbumSongsList(album, this), this);
        Intent intent = new Intent(getApplicationContext(), AlbumDetailsActivity.class);
        intent.putExtra("list",VinMediaLists.getInstance().getAlbumSongsList(album, this));
        intent.putExtra("type","album");
        intent.putExtra("title",album);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    public void OnArtistFragmentInteraction(int pos) {

        String artist = VinMediaLists.getInstance().getArtistsList(this).get(pos).get("artist") ;
        VinMedia.getInstance().updateTempQueue(VinMediaLists.getInstance()
                .getArtistSongsList(artist, this), this);
        Intent intent = new Intent(getApplicationContext(), AlbumDetailsActivity.class);
        intent.putExtra("list",VinMediaLists.getInstance().getArtistSongsList(artist, this));
        intent.putExtra("type","artist");
        intent.putExtra("title",artist);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    public void onArtistListFragmentInteraction(int i) {
        if (VinMedia.getInstance().isPlaying()) {
            VinMedia.getInstance().resetPlayer(VinMedia.getInstance().getMediaPlayer());
        }
        VinMedia.getInstance().updateQueue(false, this);
        EventBus.getDefault().post(new MessageEvent(getString(R.string.queueUpdated)));
        playPauseAction(i);
    }

    @Override
    public void OnGenreFragmentInteraction(int pos) {
        VinMedia.getInstance().updateTempQueue(VinMediaLists.getInstance()
                .getGenreSongsList(pos, this), this);
        Intent intent = new Intent(getApplicationContext(), AlbumDetailsActivity.class);
        intent.putExtra("list",VinMediaLists.getInstance().getGenreSongsList(pos, this));
        intent.putExtra("type","genre");
        intent.putExtra("title",VinMediaLists.allgenres.get(pos).get("genre"));
        startActivity(intent);
        overridePendingTransition(0,0);
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

    public class NowPlayingPagerAdapter extends FragmentPagerAdapter {

        public NowPlayingPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return NowPlayingFragments.get(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return " ";
        }
    }

    private void playPauseAction() {
        if (VinMedia.getInstance().isPlaying()) {
            VinMedia.getInstance().pauseMusic(this);
        } else {
            if (VinMedia.getInstance().getCurrentList() != null) {
                if (VinMedia.getInstance().getCurrentList().size() != 0) {
                    if (!VinMedia.getInstance().isClean()) {
                        VinMedia.getInstance().resumeMusic(this);
                    } else {
                        VinMedia.getInstance().startMusic(VinMedia.getInstance().getPosition(), this);
                    }
                }
            }
        }
    }

    private void playPauseAction(int position) {

        VinMedia.getInstance().setPosition(position);
        if (VinMedia.getInstance().isPlaying() || !VinMedia.getInstance().isClean()) {
            VinMedia.getInstance().resetPlayer(VinMedia.getInstance().getMediaPlayer());
            VinMedia.getInstance().startMusic(position, this);
        } else {
            VinMedia.getInstance().startMusic(position, this);
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for //Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        //Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show();
        String action = event.message;
        if (action.equals(getString(R.string.newSongLoaded))) {
            onNewSongLoaded();
        } else if (action.equals(getString(R.string.songPaused))) {
            onSongPaused();
        } else if (action.equals(getString(R.string.songResumed))) {
            onSongResumed();
        } else if (action.equals(getString(R.string.musicStopped))) {
            onMusicStopped();
        } else if (action.equals(getString(R.string.closePanel))) {
            Log.d("slider", "closepanel");
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else if(action.equals(getString(R.string.mediaListChanged))){
            finish();
            startActivity(new Intent(MainActivity.this,MainActivity.class));
        }
    }

}



