package com.vinay.vinplayer.views;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.AlbumDetailsActivity;
import com.vinay.vinplayer.fragments.AlbumsFragment;
import com.vinay.vinplayer.fragments.AllSongsFragment;
import com.vinay.vinplayer.fragments.ArtistDetailsFragment;
import com.vinay.vinplayer.fragments.ArtistsFragment;
import com.vinay.vinplayer.fragments.GenreFragment;
import com.vinay.vinplayer.fragments.NowPlayingFragment;
import com.vinay.vinplayer.fragments.QueueFragment;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.NotificationManager;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import github.chenupt.springindicator.SpringIndicator;

public class MainActivity extends AppCompatActivity implements
        AllSongsFragment.OnListFragmentInteractionListener,
        AlbumsFragment.OnAlbumFragmentInteractionListner,ArtistsFragment.OnArtistFragmentInteractionListner,
        View.OnClickListener,
        QueueFragment.OnQueueFragmentInteractionListener,
        ArtistDetailsFragment.OnArtistListFragmentInteractionListener,
        GenreFragment.OnGenreFragmentInteractionListner,
        NotificationManager.NotificationCenterDelegate
{

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

    RelativeLayout sliderDragger;

    RelativeLayout slider;

    ViewPager nowPlayingPager;
    NowPlayingPagerAdapter nowPlayingPagerAdapter;
    List<Fragment> NowPlayingFragments = new ArrayList<>();

    Bitmap blurredBitmap, blurredBitmap1;
    Bitmap input_to_blur;
    Bitmap temp_input;
    Bitmap default_bg;
    Drawable dr;
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;
    SystemBarTintManager tintManager;

    RelativeLayout.LayoutParams lp_now,lp_que;
    Handler handler;

    SharedPreferences media_settings;
    SharedPreferences.Editor editor;



//    String contentURI=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setStatusBarTint();
        }


*/      getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        isStoragePermissionGranted();

        media_settings = getSharedPreferences(getString(R.string.media_settings),MODE_PRIVATE);
        editor = media_settings.edit();
        editor.putInt(getString(R.string.repeat_mode), VinMedia.REPEAT_NONE);
        editor.putBoolean(getString(R.string.shuffle), VinMedia.SHUFFLE_OFF);
        editor.apply();

        songs = new ArrayList<>();

        setupLibraryViewPager();
        setupSlidingPanelLayout();
        setupNowPlayingPager();
        setupBroadCastReceiver();

        createBlurredBackground(null);
    }

    private void setupBroadCastReceiver() {

        intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.newSongLoaded));
        intentFilter.addAction(getString(R.string.songPaused));
        intentFilter.addAction(getString(R.string.songResumed));
        intentFilter.addAction(getString(R.string.musicStopped));
        intentFilter.addAction(getString(R.string.closePanel));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(getString(R.string.newSongLoaded))) {
                       onNewSongLoaded();
                } else if (action.equals(getString(R.string.songPaused))) {
                    onSongPaused();
                } else if (action.equals(getString(R.string.songResumed))) {
                    onSongResumed();
                } else if (action.equals(getString(R.string.musicStopped))) {
                    onMusicStopped();
                } else if(action.equals(getString(R.string.closePanel))){
                    Log.d("slider","closepanel");
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void onNewSongLoaded() {
        HashMap<String, String> songDetails = VinMedia.getInstance().getCurrentSongDetails();

        if (songDetails != null) {
            sliderPlayer_playpause.setImageDrawable(getResources().getDrawable(R.drawable.icon_pause));
            sliderPlayer_songtitle.setText(songDetails.get("title"));
            sliderPlayer_songdetails.setText(songDetails.get("artist") + "\t\t" + songDetails.get("album"));

            try {
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(songDetails.get("album_id")));

                Picasso.with(this).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                        .into(sliderPlayer_albumart);

                createBlurredBackground(uri.toString());

            } catch (Exception e) {
             //   e.printStackTrace();
            }

            sliderPlayer_progressBar.setMax(VinMedia.getInstance().getDuration()/1000);
            handler = new Handler();
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //Log.d("handler","running");
                    try {
                        sliderPlayer_progressBar.setProgress(VinMedia.getInstance().getAudioProgress());
                    } catch (Exception e) {

                    }
                    handler.postDelayed(this, 1000);
                }
            });
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
        mFragmentList.add(AllSongsFragment.newInstance(1, VinMediaLists.getInstance().getAllSongsList(this)));
        mFragmentList.add(AlbumsFragment.newInstance(VinMediaLists.getInstance().getAlbumsList(this)));
        mFragmentList.add(ArtistsFragment.newInstance(VinMediaLists.getInstance().getArtistsList(this)));
        mFragmentList.add(GenreFragment.newInstance(VinMediaLists.getInstance().getGenresList(this)));
        titles.add("all");
        titles.add("album");
        titles.add("artist");
        titles.add("genre");

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        librayViewPager.setAdapter(adapter);
        tabLayout = (SpringIndicator) findViewById(R.id.tabs);
        tabLayout.setViewPager(librayViewPager);


    }

    private void setupSlidingPanelLayout() {

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingpanel_layout);
        sliderPlayer = (RelativeLayout) findViewById(R.id.slider_playingdetails);
        sliderPlayer_progressBar = (ProgressBar) findViewById(R.id.slider_progressBar);
        sliderPlayer_albumart = (CircleImageView) findViewById(R.id.albumart_slider_playingsong);
        sliderPlayer_songtitle = (TextView) findViewById(R.id.slider_playingsong_songname);
        sliderPlayer_songdetails = (TextView) findViewById(R.id.slider_playing_songdetails);
        sliderPlayer_playpause = (ImageButton) findViewById(R.id.slider_playing_button_playpause);

        slider = (RelativeLayout) findViewById(R.id.slider);
        sliderDragger = (RelativeLayout)findViewById(R.id.slider_dragger);

        sliderPlayer_progressBar.getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

        lp_now = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        TypedValue tv = new TypedValue();
        int actionBarHeight=0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        lp_que = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                actionBarHeight);



        Log.d("actionBar height",actionBarHeight+"");
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.5f) {
                    sliderPlayer.setAlpha(1.0f - 2 * slideOffset);
                    sliderPlayer.setVisibility(View.VISIBLE);

                } else if (slideOffset > 0.5f && slideOffset < 1.0f) {
                    sliderPlayer.setVisibility(View.INVISIBLE);
                } else {

                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (previousState== SlidingUpPanelLayout.PanelState.DRAGGING){
                    if (newState== SlidingUpPanelLayout.PanelState.COLLAPSED){
                        Log.d("panel","collapsed");
                        nowPlayingPager.setCurrentItem(0);
                    }
                }
            }
        });

        sliderPlayer_playpause.setOnClickListener(this);
        sliderPlayer_playpause.setColorFilter(Color.WHITE);

    }

    private void setupNowPlayingPager() {

        nowPlayingPager = (ViewPager) findViewById(R.id.nowplaying_pager);
        nowPlayingPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                    if (position==1){
                        sliderDragger.setLayoutParams(lp_que);
                    }else {
                        sliderDragger.setLayoutParams(lp_now);
                    }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        NowPlayingFragments.add(NowPlayingFragment.newInstance());
        NowPlayingFragments.add(QueueFragment.newInstance(1));
        nowPlayingPagerAdapter = new NowPlayingPagerAdapter(getSupportFragmentManager());
        nowPlayingPager.setAdapter(nowPlayingPagerAdapter);

    }

    private void createBlurredBackground(String url) {
        //  Bitmap emptyBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), myBitmap.getConfig());
        //default_bg = BitmapFactory.decodeFile(url);
        if (url != null) {
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

            }
            //default_bg = ((BitmapDrawable)songAlbumbg.getDrawable()).getBitmap();
            if (default_bg == null) {
                default_bg = BitmapFactory.decodeResource(getResources(), R.drawable.albumart_default);
            }
        } else {
            default_bg = BitmapFactory.decodeResource(getResources(), R.drawable.albumart_default);
        }
        temp_input = default_bg.copy(Bitmap.Config.ARGB_8888, true);
        input_to_blur = Bitmap.createScaledBitmap(temp_input, 300, 300, false);
        blurredBitmap = BlurBuilder.blur(this, input_to_blur);
        blurredBitmap1 = BlurBuilder.blur(this, input_to_blur);

        //BitmapDrawable background = new BitmapDrawable(context.getResources(),default_bg);
        dr = new BitmapDrawable(blurredBitmap);
        slider.setBackground(dr);
        dr = null;
        temp_input = null;
        input_to_blur = null;
        blurredBitmap = null;
        blurredBitmap1 = null;
        //view.setBackgroundDrawable( new BitmapDrawable( getResources(), blurredBitmap ) );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slider_playing_button_playpause:
                playPauseAction();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addObserver();
        //vm.VinMediaInitialize();
        sendBroadcast(new Intent().setAction(getString(R.string.newSongLoaded)));
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void loadAlreadyPlaying() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        removeObserver();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // VinMedia.getInstance().releasePlayer();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    @Override
    public void onListFragmentInteraction(int p) {

        if (VinMedia.getInstance().isPlaying()) {
            VinMedia.getInstance().resetPlayer();
        }
        VinMedia.getInstance().updateQueue(0,this);
        sendBroadcast(new Intent().setAction(getString(R.string.queueUpdated)));
        playPauseAction(p);
    }

    @Override
    public void OnQueueFragmentInteraction(int p) {

        Log.d("queue","fragment interation  "+p);
        if (VinMedia.getInstance().isPlaying()) {
            VinMedia.getInstance().resetPlayer();
        }
        playPauseAction(p);
    }
    @Override
    public void OnAlbumFragmentInteraction(int pos) {

        VinMedia.getInstance().updateTempQueue(pos,VinMediaLists.getInstance()
                .getAlbumSongsList(VinMediaLists.getInstance().getAlbumsList(this)
                 .get(pos).get("album"),this),this);

      startActivity(new Intent(getApplicationContext(),AlbumDetailsActivity.class).putExtra("list",
              VinMediaLists.getInstance().getAlbumSongsList((VinMediaLists.allAlbums.get(pos).get("album")),this)));
    }

    @Override
    public void OnArtistFragmentInteraction(int pos) {

        VinMedia.getInstance().updateTempQueue(pos,VinMediaLists.getInstance()
                .getArtistSongsList(VinMediaLists.getInstance().getArtistsList(this)
                        .get(pos).get("artist"),this),this);

        FragmentManager fm = getSupportFragmentManager();

        ArtistDetailsFragment  dFragment = ArtistDetailsFragment.newInstance(
                VinMediaLists.getInstance().getArtistSongsList((VinMediaLists.allArtists.get(pos).get("artist")),this));
        // Show DialogFragment
        dFragment.show(fm, "Dialog Fragment");
    }

    @Override
    public void onArtistListFragmentInteraction(int i) {
        if (VinMedia.getInstance().isPlaying()) {
            VinMedia.getInstance().resetPlayer();
        }
        VinMedia.getInstance().updateQueue(1,this);
        sendBroadcast(new Intent().setAction(getString(R.string.queueUpdated)));
        playPauseAction(i);
    }

    @Override
    public void OnGenreFragmentInteraction(int pos) {

    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.audioDidStarted || id == NotificationManager.audioPlayStateChanged || id == NotificationManager.audioDidReset) {
           // updateTitle(id == NotificationManager.audioDidReset && (Boolean) args[1]);
        } else if (id == NotificationManager.audioProgressDidChanged) {
            HashMap<String,String> mSongDetail = VinMedia.getInstance().getCurrentSongDetails();
           // updateProgress(mSongDetail);
        }
    }

    @Override
    public void newSongLoaded(Object... args) {

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
            return 2;
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
            if (VinMedia.getInstance().getCurrentList()!=null) {
                if (VinMedia.getInstance().getCurrentList().size() != 0) {
                    if (!VinMedia.getInstance().isClean()) {
                        VinMedia.getInstance().resumeMusic(this);
                    } else {
                        VinMedia.getInstance().startMusic(VinMedia.getInstance().getPosition(),this);
                    }
                }
            }
        }
    }

    private void playPauseAction(int position) {

        VinMedia.getInstance().setPosition(position);
        if (VinMedia.getInstance().isPlaying() || !VinMedia.getInstance().isClean()) {
            VinMedia.getInstance().resetPlayer();
            VinMedia.getInstance().startMusic(position,this);
        } else {
            VinMedia.getInstance().startMusic(position,this);
        }
    }

    public void addObserver() {
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioDidReset);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioPlayStateChanged);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioDidStarted);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().addObserver(this, NotificationManager.newaudioloaded);
    }

    public void removeObserver() {
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioDidReset);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioPlayStateChanged);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioDidStarted);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.newaudioloaded);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Storage Permisson","granted");
                return true;
            } else {

                Log.v("Storage Permisson","revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Storage Permisson","granted");
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("Storage Permisson","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    /*private void setStatusBarTint() {
        try {
            setTranslucentStatus(true);
            TypedValue typedValueStatusBarColor = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValueStatusBarColor, true);
            final int colorStatusBar = typedValueStatusBarColor.data;

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(colorStatusBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }*/

}

