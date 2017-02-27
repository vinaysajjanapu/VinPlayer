package com.vinay.vinplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
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
                .inflate(R.layout.list_item_genre, parent, false);
        return new GenreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GenreAdapter.ViewHolder holder, final int position) {

        if (mValues != null) {
            holder.mIdView.setText(mValues.get(position).get("genre"));
            int num_songs = Integer.parseInt(mValues.get(position).get("num_songs"));
            if (num_songs>3)num_songs=3;
            try {
                for (int k=0;k<3;k++) {
                    if (k < num_songs) {
                        final Uri sArtworkUri = Uri
                                .parse("content://media/external/audio/albumart/");
                        Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(position).get("album_id" + (k + 1))));
                        Picasso.with(context)
                                .load(uri)
                                .resize(200-50*(k+1), 200-50*(k+1))
                                .centerCrop()
                                .into(holder.imageView[k]);
                    }else {
                        Picasso.with(context)
                                .load("abc")
                                .error(R.drawable.albumart_default)
                                .resize(200-50*(k+1), 200-50*(k+1))
                                .centerCrop()
                                .into(holder.imageView[k]);
                    }
                }

                //ImageLoader.getInstance().displayImage(sArtworkUri.toString(), holder.imageView);

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
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        //      final TextView mContentView;
        final ImageView[] imageView = new ImageView[3];
        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.tv_album_title);
            //   mContentView = (TextView) view.findViewById(R.id.content);
            imageView[0] = (ImageView)view.findViewById(R.id.img1);
            imageView[1] = (ImageView)view.findViewById(R.id.img2);
            imageView[2] = (ImageView)view.findViewById(R.id.img3);
        }


    }
}

