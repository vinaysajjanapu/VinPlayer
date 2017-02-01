package com.vinay.vinplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.AllSongsFragment.OnListFragmentInteractionListener;
import com.vinay.vinplayer.helpers.VinMediaLists;


import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.ViewHolder> {

    private final List<HashMap<String,String>> mValues;
    private final OnListFragmentInteractionListener mListener;
    Context context;
    private VinMediaLists vinMediaLists;


    public AllSongsAdapter(Context context, List<HashMap<String,String>> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        vinMediaLists = new VinMediaLists(context);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allsongs_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.mItem = mValues.get(position);
        holder.songname.setText(mValues.get(position).get("title"));
        holder.ArtisName_duration.setText(
                mValues.get(position).get("album") + "\t\t"+ mValues.get(position).get("artist") + ""
        );



        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(position).get("album_id")));
            Picasso.with(context).load(uri).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
                    .into(holder.circleImageView);

        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(position);
                }
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
        public HashMap<String,String> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            songname = (TextView) view.findViewById(R.id.songname);
            ArtisName_duration = (TextView) view.findViewById(R.id.ArtisName_duration);
            circleImageView = (CircleImageView)view.findViewById(R.id.SongThumb);
        }


    }
}
