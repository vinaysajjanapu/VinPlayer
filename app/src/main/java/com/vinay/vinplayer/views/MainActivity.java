package com.vinay.vinplayer.views;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinMedia;
import com.vinay.vinplayer.fragments.AlbumsFragment;
import com.vinay.vinplayer.fragments.AllSongsFragment;
import com.vinay.vinplayer.fragments.ArtistsFragment;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AllSongsFragment.OnListFragmentInteractionListener,
        AlbumsFragment.OnAlbumFragmentInteractionListner,ArtistsFragment.OnArtistFragmentInteractionListner {

    ArrayList<HashMap<String, String>> songs;

    Button start, pause, stop;
    VinMedia vm;

    ViewPager viewPager;
    TabLayout tabLayout;
    List<Fragment> mFragmentList = new ArrayList<>();
    List<String> titles=new ArrayList<>();
    VinMediaLists vinMediaLists ;
    ViewPagerAdapter adapter;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vm = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        songs = new ArrayList<>();
        start = (Button) findViewById(R.id.button1);
        pause = (Button) findViewById(R.id.button2);
        stop = (Button) findViewById(R.id.button3);

        vm = new VinMedia(this);
        vm.VinMediaInitialize();
        vinMediaLists=new VinMediaLists(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        mFragmentList.add(AllSongsFragment.newInstance(1, VinMedia.currentList));
        mFragmentList.add(AlbumsFragment.newInstance( vinMediaLists.getAlbumsList()));
        mFragmentList.add(ArtistsFragment.newInstance( vinMediaLists.getArtistsList()));
        titles.add("all songs");
        titles.add("albums");
        titles.add("artists");

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//mp.release();

               // vm.resetPlayer();

                vm.startMusic(10);

            }

        });


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vm.isPlaying()) {
                    if (pause.getText().toString().equals("pause")) {
                        vm.pauseMusic();
                        pause.setText("resume");
                    } else if (pause.getText().toString().equals("resume")) {
                        vm.resumeMusic();
                        pause.setText("pause");
                    }
                } else
                    Toast.makeText(getApplicationContext(), "no song playing", Toast.LENGTH_SHORT).show();
            }

        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vm.stopMusic();
            }
        });

    }

    @Override
    public void onListFragmentInteraction(int p) {

        if(vm.isPlaying()){
            vm.resetPlayer();
        }

        vm.startMusic(p);
    }

    @Override
    public void OnAlbumFragmentInteraction(int pos) {

        mFragmentList.remove(1);
        vm.changeCurrentList(pos,1);
        mFragmentList.add(AllSongsFragment.newInstance(1,
                vinMediaLists.getAlbumSongsList((VinMediaLists.allAlbums.get(pos).get("album"))) ));

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
    }

    @Override
    public void OnArtistFragmentInteraction(int pos) {
        mFragmentList.remove(1);
        vm.changeCurrentList(pos,2);
        mFragmentList.add(AllSongsFragment.newInstance(1,
                vinMediaLists.getArtistSongsList((VinMediaLists.allArtists.get(pos).get("artist"))) ));

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
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
}
