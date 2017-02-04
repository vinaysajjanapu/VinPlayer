package com.vinay.vinplayer.adapters;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.AllSongsFragment.OnListFragmentInteractionListener;
import com.vinay.vinplayer.helpers.VinMedia;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.ViewHolder> {

    private final List<HashMap<String,String>> mValues;
    private final OnListFragmentInteractionListener mListener;
    Context context;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    static int pos1=0,pos2=0;

    public AllSongsAdapter(Context context, List<HashMap<String,String>> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allsongs_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.mItem = mValues.get(position);
        holder.songname.setText(mValues.get(position).get("title"));
        holder.ArtisName_duration.setText(
                mValues.get(position).get("album") + "\t-\t"+ mValues.get(position).get("artist") + ""
        );

        setupBroadCastReceiver(holder,position);


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
                if (null != mListener) {
                    mListener.onListFragmentInteraction(position);
                }
            }
        });
    }

    private void setupBroadCastReceiver(final ViewHolder view, final int position){

        intentFilter = new IntentFilter();
        intentFilter.addAction(context.getString(R.string.newSongLoaded));
        intentFilter.addAction(context.getString(R.string.songPaused));
        intentFilter.addAction(context.getString(R.string.songResumed));
        intentFilter.addAction(context.getString(R.string.musicStopped));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(context.getString(R.string.newSongLoaded))){
                    onNewSongLoaded(view,position);
                }else if (action.equals(context.getString(R.string.songPaused))){
                    // onSongPaused();
                }else if (action.equals(context.getString(R.string.songResumed))){
                    //onSongResumed();
                }else if (action.equals(context.getString(R.string.musicStopped))){
                    onMusicStopped(view,position);
                }
            }
        };
        context.registerReceiver(broadcastReceiver,intentFilter);
    }
    private void onNewSongLoaded(ViewHolder view, int position){
        if (position== VinMedia.getInstance().getPosition()){
            if ((pos1==-1)&&(pos2==-1))pos1 = position;
            else if ((pos1!=-1)&&(pos2==-1)) pos2 = position;

            Log.d("mediaplayer status","onNewSongLoaded");
           // view.img_playindic.setVisibility(View.VISIBLE);
        }
    }
    private void onMusicStopped(ViewHolder view, int position){
        if (position == pos1){
            pos1=pos2;
            pos2=-1;
            Log.d("mediaplayer status","onMusicStopped");
         //   view.img_playindic.setVisibility(View.INVISIBLE);
        }

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

        public ViewHolder(View view) {
            super(view);
            mView = view;
            songname = (TextView) view.findViewById(R.id.songname);
            ArtisName_duration = (TextView) view.findViewById(R.id.ArtisName_duration);
            circleImageView = (CircleImageView)view.findViewById(R.id.SongThumb);
            img_playindic = (ImageView)view.findViewById(R.id.img_playindic);

            songname.setTextColor(Color.WHITE);
            ArtisName_duration.setTextColor(Color.WHITE);

            img_playindic.setColorFilter(Color.BLACK);
        }


    }
}
