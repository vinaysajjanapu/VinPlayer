package com.vinay.vinplayer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.AlbumsFragment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private final ArrayList<HashMap<String,String>> mValues;
    private final AlbumsFragment.OnAlbumFragmentInteractionListner mListener;
    private Context context;

    public AlbumsAdapter(Context context, ArrayList<HashMap<String,String>> items, AlbumsFragment.OnAlbumFragmentInteractionListner listener) {
        mValues = items;
        this.context = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

      holder.mIdView.setText(mValues.get(position).get("album"));
       // holder.mContentView.setText(mValues.get(position).get("no_of_songs"));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.OnAlbumFragmentInteraction(position);
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
        final TextView mContentView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.tv_album_title);
            mContentView = (TextView) view.findViewById(R.id.content);
        }


    }
}
