package com.vinay.vinplayer.adapters;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.QueueFragment;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by vinaysajjanapu on 3/2/17.
 */

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder>  {

    private final List<HashMap<String,String>> mValues;
    private final QueueFragment.OnQueueFragmentInteractionListener mListener;
    Context context;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    public QueueAdapter(Context context, List<HashMap<String,String>> items, QueueFragment.OnQueueFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
  //      vinMediaLists = new VinMediaLists(context);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allsongs_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final QueueAdapter.ViewHolder holder, final int position) {

        //Log.d("queue",mValues.size()+"");
        if (mValues!=null) {
            holder.mItem = mValues.get(position);
            holder.songname.setText(mValues.get(position).get("title"));
            holder.ArtisName_duration.setText(
                    mValues.get(position).get("album") + "\t-\t" + mValues.get(position).get("artist") + ""
            );
        }


       // setupBroadCastReceiver(holder,position);



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
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.OnQueueFragmentInteraction(position);
                }
            }
        });
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
        public HashMap<String,String> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            songname = (TextView) view.findViewById(R.id.songname);
            ArtisName_duration = (TextView) view.findViewById(R.id.ArtisName_duration);
            circleImageView = (CircleImageView)view.findViewById(R.id.SongThumb);
            img_playindic = (ImageView)view.findViewById(R.id.img_playindic);

            img_playindic.setColorFilter(Color.WHITE);
            songname.setTextColor(Color.WHITE);
            ArtisName_duration.setTextColor(Color.WHITE);
        }


    }



}
