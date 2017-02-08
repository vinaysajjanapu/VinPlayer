package com.vinay.vinplayer.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.GenreFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by salimatti on 2/4/2017.
 */

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {

    private final ArrayList<HashMap<String,String>> mValues;
    private final GenreFragment.OnGenreFragmentInteractionListner mListener;
    private Context context;

    public GenreAdapter(Context context, ArrayList<HashMap<String,String>> items,
                        GenreFragment.OnGenreFragmentInteractionListner listener) {
        //Set<String> set = new HashSet<String>(list);
        mValues = items;
        this.context = context;
        mListener = listener;
    }


    @Override
    public GenreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album_item, parent, false);
        return new GenreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GenreAdapter.ViewHolder holder, final int position) {

        holder.mIdView.setText(mValues.get(position).get("genre"));
        // holder.mContentView.setText(mValues.get(position).get("no_of_songs"));

        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart/0");
            //Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(0).get("album_id")));
            /*Picasso.with(context).load(sArtworkUri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                    .into(holder.imageView);
*/
            Glide.with(context)
                    .load(sArtworkUri)
                    .centerCrop()/*
                    .thumbnail(context.getResources().getDimension(R.dimen.albumspage_thumb_scale))*/
                    .override(context.getResources().getInteger(R.integer.albums_page_albumart_size),
                            context.getResources().getInteger(R.integer.albums_page_albumart_size))
                    .placeholder(R.drawable.albumart_default)
                    .crossFade()
                    .into(holder.imageView);

        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.OnGenreFragmentInteraction(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        //      final TextView mContentView;
        final ImageView imageView;
        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.tv_album_title);
            //   mContentView = (TextView) view.findViewById(R.id.content);
            imageView = (ImageView)view.findViewById(R.id.albumart_albums);
        }


    }
}

