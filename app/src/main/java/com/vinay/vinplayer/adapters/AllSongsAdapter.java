package com.vinay.vinplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.AllSongsFragment.OnListFragmentInteractionListener;
import com.vinay.vinplayer.helpers.VinMediaLists;


import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.ViewHolder> {

    private final List<HashMap<String,String>> mValues;
    private final OnListFragmentInteractionListener mListener;
    Context context;
    ImageLoader imageLoader;
    private VinMediaLists vinMediaLists;
    DisplayImageOptions options;

    public AllSongsAdapter(Context context, List<HashMap<String,String>> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        vinMediaLists = new VinMediaLists(context);
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        options = new DisplayImageOptions.Builder().cacheInMemory(false)
                .cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.mItem = mValues.get(position);
        holder.songname.setText(mValues.get(position).get("title"));
        holder.ArtisName_duration.setText(
                mValues.get(position).get("album") + "\t\t"+ mValues.get(position).get("artist") + ""
        );

      //  imageLoader.displayImage(contentURI, holder.circleImageView);

        try {
            String contentURI = "content://media/external/audio/media/albumart";
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(position).get("album_id")));
            contentURI = uri.toString();
            imageLoader.displayImage(contentURI,holder.circleImageView,options);
        } catch (Exception e) {
            holder.circleImageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher));
            e.printStackTrace();
        }






        //holder.circleImageView.setImageBitmap(vinMediaLists.getAlbumart(Integer.parseInt(mValues.get(position).get("album_id"))));

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
       // public final ImageView imageView;
        public HashMap<String,String> mItem;
        //VinMedia v=new VinMedia(context);

        public ViewHolder(View view) {
            super(view);
            mView = view;
            songname = (TextView) view.findViewById(R.id.songname);
            ArtisName_duration = (TextView) view.findViewById(R.id.ArtisName_duration);
            //imageView = (ImageView)view.findViewById(R.id.SongThumb);
            circleImageView = (CircleImageView)view.findViewById(R.id.SongThumb);
        }


    }
}
