package com.vinay.vinplayer.adapters;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.L;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.QueueFragment;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by vinaysajjanapu on 3/2/17.
 */

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder>  {

    private  List<HashMap<String,String>> mValues;
    private  QueueFragment.OnQueueFragmentInteractionListener mListener;
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
        holder.img_playindic.setVisibility(View.GONE);
        holder.mView.setBackgroundColor(Color.TRANSPARENT);

        if (position == VinMedia.getInstance().getPosition()){
            Log.d("queue position", position+"");
            holder.img_playindic.setVisibility(View.VISIBLE);
            holder.songname.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            holder.ArtisName_duration.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            holder.ArtisName_duration.setTextColor(Color.WHITE);
            holder.mView.setBackgroundColor(context.getResources().getColor(R.color.transparentLightBlack));

        }
       // setupBroadCastReceiver(holder,position);



        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(position).get("album_id")));
            /*Picasso.with(context).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                    .into(holder.circleImageView);
            */
            ImageLoader.getInstance().displayImage(uri.toString(), holder.circleImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("queue","interacted");
                if (null != mListener) {
                    Log.d("queue","not null");
                 //   VinMedia.getInstance().startMusic(position,context);
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
        private ImageButton more_icon;
        public HashMap<String,String> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            songname = (TextView) view.findViewById(R.id.songname);
            ArtisName_duration = (TextView) view.findViewById(R.id.ArtisName_duration);
            circleImageView = (CircleImageView)view.findViewById(R.id.SongThumb);
            img_playindic = (ImageView)view.findViewById(R.id.img_playindic);
            img_playindic.setVisibility(View.GONE);
            more_icon = (ImageButton)view.findViewById(R.id.img_moreicon);

            more_icon.setColorFilter(Color.argb(180,255,255,255));

            img_playindic.setColorFilter(Color.GREEN);
            songname.setTextColor(Color.WHITE);

        }


    }



}
