package com.vinay.vinplayer.activities;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AllSongsAdapter;
import com.vinay.vinplayer.helpers.BlurBuilder;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RandomAutoPlay extends AppCompatActivity implements View.OnClickListener {

    Button play_random, stop_random;
    RecyclerView randomList;
    ArrayList<HashMap<String, String>> songList;
    String LOGTAG = "RandomPlayActivity";
    PercentRelativeLayout activity;
    CheckBox startAtBegin;
    static boolean isStartAtBegin = false;
    MaterialEditText duration_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_random_auto_play);
        getSupportActionBar().hide();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        isStartAtBegin = false;

        activity = (PercentRelativeLayout) findViewById(R.id.activity_random_auto_play);
        //activity = (PercentRelativeLayout)findViewById(R.id.activity_random_auto_play);
        play_random = (Button) findViewById(R.id.playRandom);
        stop_random = (Button) findViewById(R.id.stopRandom);
        songList = new ArrayList<>();
        startAtBegin = (CheckBox)findViewById(R.id.startAtBegin);
        duration_text = (MaterialEditText)findViewById(R.id.random_duration);

        startAtBegin.setTextColor(Color.WHITE);
        startAtBegin.setHintTextColor(Color.WHITE);

        activity.setBackground(BlurBuilder.getInstance().drawable_img("null",this));

        stop_random.setOnClickListener(this);
        play_random.setOnClickListener(this);

        randomList = (RecyclerView) findViewById(R.id.random_list);
        randomList.setLayoutManager(new LinearLayoutManager(this));
        randomList.setAdapter(new randomAdapter(this, songList));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playRandom:
                int duration = 0;
                String d = duration_text.getText().toString();
                if (!d.equals(""))
                    duration = Integer.parseInt(d);
                VinMedia.getInstance().RandomPlay(getBaseContext(), null, startAtBegin.isChecked(), duration);
                break;
            case R.id.stopRandom:
                VinMedia.getInstance().stopRandomPlay(false, this);
                break;
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

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        //Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        String action = event.message;
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

    private void onMusicStopped() {

    }

    private void onSongResumed() {

    }

    private void onSongPaused() {

    }

    private void onNewSongLoaded() {
        Log.d(LOGTAG,"newSongLoaded");
        HashMap<String,String> hashMap = VinMedia.getInstance().getCurrentSongDetails();
        songList.add(0,hashMap);
        randomList.getAdapter().notifyDataSetChanged();
    }


    private class randomAdapter extends RecyclerView.Adapter<randomAdapter.ViewHolder> {
        Context context;
        ArrayList<HashMap<String,String>> mValues;
        public randomAdapter(Context context, ArrayList<HashMap<String, String>> mValues) {
            this.context = context;
            this.mValues = mValues;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allsongs_item, parent, false);
            return new randomAdapter.ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
if (mValues!=null) {
    String title = mValues.get(position).get("title");
    holder.mItem = mValues.get(position);
    holder.songname.setText(title);
    holder.ArtisName_duration.setText(
            mValues.get(position).get("album") + "\t-\t" + mValues.get(position).get("artist") + ""
    );


    holder.img_playindic.setVisibility(View.GONE);
    holder.mView.setBackgroundColor(Color.TRANSPARENT);

    if (VinMedia.getInstance().getCurrentSongDetails() != null) {
        if (VinMedia.getInstance().getCurrentSongDetails().get("title").equals(title)) {
            holder.img_playindic.setVisibility(View.VISIBLE);/*
                holder.songname.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                holder.ArtisName_duration.setTypeface(Typeface.DEFAULT, Typeface.BOLD);*/
            holder.ArtisName_duration.setTextColor(Color.WHITE);
            holder.mView.setBackgroundColor(context.getResources().getColor(R.color.transparentLightBlack));
        }
    }
    try {
        final Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(position).get("album_id")));
        Picasso.with(context)
                .load(uri)
                .error(R.drawable.albumart_default)
                .resize(80, 80)
                .into(holder.circleImageView);

    } catch (Exception e) {
        e.printStackTrace();

    }

    holder.mView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    });

    holder.more_icon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(context, holder.more_icon);
            popup.inflate(R.menu.options_menu_allsongs);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play:
                            ArrayList<HashMap<String, String>> song = new ArrayList<>();
                            song.add(VinMediaLists.allSongs.get(position));
                            VinMedia.getInstance().updateTempQueue(song, context);
                            VinMedia.getInstance().updateQueue(false, context);
                            VinMedia.getInstance().startMusic(0, context);
                            break;
                        case R.id.play_next:
                            ArrayList<HashMap<String, String>> q = new ArrayList<HashMap<String, String>>();
                            q = VinMedia.getInstance().getCurrentList();

                            break;
                        case R.id.add_to_queue:
                            ArrayList<HashMap<String, String>> arrayList = VinMedia.getInstance().getCurrentList();
                            arrayList.add(VinMediaLists.allSongs.get(position));/*
                                VinMedia.getInstance().updateTempQueue(2, arrayList,context);
                                VinMedia.getInstance().updateQueue(2,context);*/
                            break;
                        case R.id.edit_info:
                            Intent intent = new Intent(context.getApplicationContext(), MetaDataEditor.class);
                            intent.putExtra("album_art_availalbe",
                                    VinMediaLists.getInstance().isAlbumArtAvailable(
                                            VinMediaLists.allSongs.get(position).get("album_id"), context));
                            intent.putExtra("data", VinMediaLists.allSongs.get(position).get("data"));
                            intent.putExtra("title", VinMediaLists.allSongs.get(position).get("title"));
                            intent.putExtra("album", VinMediaLists.allSongs.get(position).get("album"));
                            intent.putExtra("artist", VinMediaLists.allSongs.get(position).get("artist"));
                            // intent.putExtra("genre",VinMediaLists.allSongs.get(position).get("genre"));
                            intent.putExtra("album_id", VinMediaLists.allSongs.get(position).get("album_id"));

                            context.startActivity(intent);
                            break;
                        case R.id.edit:

                            String filename = VinMediaLists.allSongs.get(position).get("data");
                            try {
                                Intent intent1 = new Intent(Intent.ACTION_EDIT, Uri.parse(filename));
                                intent1.setClassName("com.vinay.vinplayer", "com.vinay.vinplayer.ringdroid.RingdroidEditActivity");
                                context.startActivity(intent1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("Ringdroid", "Couldn't start editor");
                            }/*
                                context.startActivity(new Intent(context.getApplicationContext(), RingdroidSelectActivity.class));*/
                            break;
                    }
                    return false;
                }
            });
            popup.show();

        }
    });
}
        }

        @Override
        public int getItemCount() {
            return (mValues!=null)?mValues.size():0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView songname;
            public final TextView ArtisName_duration;
            public final CircleImageView circleImageView;
            public final ImageView img_playindic;
            public HashMap<String, String> mItem;
            private ImageButton more_icon;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                songname = (TextView) view.findViewById(R.id.songname);
                ArtisName_duration = (TextView) view.findViewById(R.id.ArtisName_duration);
                circleImageView = (CircleImageView) view.findViewById(R.id.SongThumb);
                img_playindic = (ImageView) view.findViewById(R.id.img_playindic);
                more_icon = (ImageButton) view.findViewById(R.id.img_moreicon);

                more_icon.setColorFilter(Color.argb(180, 255, 255, 255));
                songname.setTextColor(Color.WHITE);
                img_playindic.setColorFilter(Color.GREEN);

            }

        }
    }
}
