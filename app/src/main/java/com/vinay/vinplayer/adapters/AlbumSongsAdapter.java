package com.vinay.vinplayer.adapters;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMedia;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by salimatti on 2/4/2017.
 */

public class AlbumSongsAdapter extends RecyclerView.Adapter<AlbumSongsAdapter.ViewHolder> {

    private final List<HashMap<String,String>> mValues;

    Context context;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    static int pos1=0,pos2=0;
    static int gp;

    public AlbumSongsAdapter(Context context, List<HashMap<String,String>> items) {
        mValues = items;
        this.context=context;
    }

    @Override
    public AlbumSongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allsongs_item, parent, false);

        if(EventBus.getDefault().isRegistered(MessageEvent.class)){
            EventBus.getDefault().unregister(this);
        }
        //EventBus.getDefault().register(this);
        return new AlbumSongsAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final AlbumSongsAdapter.ViewHolder holder, final int position) {

        String title = mValues.get(position).get("title");

        holder.mItem = mValues.get(position);
        holder.songname.setText(title);
        holder.ArtisName_duration.setText(
                mValues.get(position).get("album") + "\t-\t"+ mValues.get(position).get("artist") + ""
        );

        if (VinMedia.getInstance().getCurrentSongDetails()!=null) {
            if (VinMedia.getInstance().getCurrentSongDetails().get("title").equals(title)){
                holder.img_playindic.setVisibility(View.VISIBLE);/*
                holder.songname.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                holder.ArtisName_duration.setTypeface(Typeface.DEFAULT, Typeface.BOLD);*/
                holder.ArtisName_duration.setTextColor(Color.WHITE);
                holder.mView.setBackgroundColor(context.getResources().getColor(R.color.transparentLightBlack));
            }
        }

        holder.img_playindic.setVisibility(View.GONE);
        holder.mView.setBackgroundColor(Color.TRANSPARENT);
        try {
            Log.d("albumsonsrecycler","set image");
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(position).get("album_id")));
            Picasso.with(context).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                    .into(holder.circleImageView);

        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VinMedia.getInstance().isPlaying()) {
                    VinMedia.getInstance().resetPlayer(VinMedia.getInstance().getMediaPlayer());
                }
                VinMedia.getInstance().updateQueue(false,context);
                EventBus.getDefault().post(new MessageEvent(context.getString(R.string.queueUpdated)));
                playPauseAction(position);
            }
        });
    }
    private void playPauseAction(int position) {

        VinMedia.getInstance().setPosition(position);
        if (VinMedia.getInstance().isPlaying() || !VinMedia.getInstance().isClean()) {
            VinMedia.getInstance().resetPlayer(VinMedia.getInstance().getMediaPlayer());
            VinMedia.getInstance().startMusic(position,context);
        } else {
            VinMedia.getInstance().startMusic(position,context);
        }
    }


    private void onNewSongLoaded( ){

    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView songname;
        public final TextView ArtisName_duration;
        public final CircleImageView circleImageView;
        public final ImageView img_playindic;
        public HashMap<String,String> mItem;
        private ImageButton more_icon;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            songname = (TextView) view.findViewById(R.id.songname);
            ArtisName_duration = (TextView) view.findViewById(R.id.ArtisName_duration);
            circleImageView = (CircleImageView)view.findViewById(R.id.SongThumb);
            img_playindic = (ImageView)view.findViewById(R.id.img_playindic);
            more_icon = (ImageButton)view.findViewById(R.id.img_moreicon);

            view.setBackgroundColor(Color.argb(50,0,0,0));
            more_icon.setVisibility(View.INVISIBLE);
            songname.setTextColor(Color.WHITE);
            ArtisName_duration.setTextColor(Color.WHITE);
            img_playindic.setColorFilter(Color.BLACK);
        }


    }

    @Subscribe
    public void onEvent(MessageEvent event) {
        Log.e("Test event 2 ", event.message);
        String action = event.message;
        if(action.equals(context.getString(R.string.newSongLoaded))){
            onNewSongLoaded();
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {

        if(EventBus.getDefault().isRegistered(MessageEvent.class)){
            EventBus.getDefault().unregister(this);
        }
        super.onViewDetachedFromWindow(holder);
    }
}
