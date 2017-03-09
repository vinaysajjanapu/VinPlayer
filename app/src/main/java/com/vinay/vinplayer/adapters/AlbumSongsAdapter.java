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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by salimatti on 2/4/2017.
 */

public class AlbumSongsAdapter extends RecyclerView.Adapter<AlbumSongsAdapter.ViewHolder> {

    private final ArrayList<HashMap<String,String>> mValues;

    Context context;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    static int pos1=0,pos2=0;
    static int gp;

    public AlbumSongsAdapter(Context context, ArrayList<HashMap<String,String>> items) {
        mValues = items;
        this.context=context;
    }

    @Override
    public AlbumSongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allsongs_item, parent, false);

        if(EventBus.getDefault().isRegistered(MessageEvent.class)){
            EventBus.getDefault().unregister(this);
        }
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

        holder.img_playindic.setVisibility(View.GONE);
        holder.mView.setBackgroundColor(Color.TRANSPARENT);

        if (VinMedia.getInstance().getCurrentSongDetails()!=null) {
            if (VinMedia.getInstance().getCurrentSongDetails().get("title").equals(title)){
                holder.img_playindic.setVisibility(View.VISIBLE);
                holder.ArtisName_duration.setTextColor(Color.WHITE);
                holder.mView.setBackgroundColor(context.getResources().getColor(R.color.transparentLightBlack));
            }
        }

        try {
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
                VinMedia.getInstance().updateTempQueue(mValues,context);
                VinMedia.getInstance().updateQueue(false,context);
                VinMedia.getInstance().startMusic(position,context);
            }
        });
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
            img_playindic.setColorFilter(Color.GREEN);
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
