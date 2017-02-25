package com.vinay.vinplayer.adapters;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.fragments.AllSongsFragment.OnListFragmentInteractionListener;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;
import com.vinay.vinplayer.activities.MetaDataEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.ViewHolder> implements SectionTitleProvider {

    private final List<HashMap<String, String>> mValues;
    private final OnListFragmentInteractionListener mListener;
    Context context;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    static int pos1 = 0, pos2 = 0;

    public AllSongsAdapter(Context context, List<HashMap<String, String>> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_allsongs_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        String title = mValues.get(position).get("title");
        holder.mItem = mValues.get(position);
        holder.songname.setText(title);
        holder.ArtisName_duration.setText(
                mValues.get(position).get("album") + "\t-\t" + mValues.get(position).get("artist") + ""
        );
        holder.songname.setTypeface(Typeface.DEFAULT);

        holder.img_playindic.setVisibility(View.GONE);
        holder.mView.setBackgroundColor(Color.TRANSPARENT);
        if (VinMedia.getInstance().getCurrentSongDetails()!=null) {
            if (VinMedia.getInstance().getCurrentSongDetails().get("title").equals(title)){
                holder.img_playindic.setVisibility(View.VISIBLE);
                holder.songname.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                holder.ArtisName_duration.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                holder.ArtisName_duration.setTextColor(Color.WHITE);
                holder.mView.setBackgroundColor(context.getResources().getColor(R.color.transparentLightBlack));
            }
        }
        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(mValues.get(position).get("album_id")));
           Picasso.with(context).load(uri).error(R.drawable.albumart_default).resize(70,70).into(holder.circleImageView);

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

        holder.more_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.more_icon);
                popup.inflate(R.menu.options_menu_allsongs);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.play:
                                ArrayList<HashMap<String, String>> song =  new ArrayList<>();
                                song.add(VinMediaLists.allSongs.get(position));
                                VinMedia.getInstance().updateTempQueue( song,context);
                                VinMedia.getInstance().updateQueue(false,context);
                                VinMedia.getInstance().startMusic(0,context);
                                break;
                            case R.id.play_next:
                                ArrayList<HashMap<String,String>> q = new ArrayList<HashMap<String, String>>();
                                q = VinMedia.getInstance().getCurrentList();

                                break;
                            case R.id.add_to_queue:
                                ArrayList<HashMap<String,String>> arrayList = VinMedia.getInstance().getCurrentList();
                                arrayList.add(VinMediaLists.allSongs.get(position));/*
                                VinMedia.getInstance().updateTempQueue(2, arrayList,context);
                                VinMedia.getInstance().updateQueue(2,context);*/
                                break;
                            case R.id.edit_info:
                                Intent intent = new Intent(context.getApplicationContext(), MetaDataEditor.class);
                                intent.putExtra("album_art_availalbe",
                                        VinMediaLists.getInstance().isAlbumArtAvailable(
                                                VinMediaLists.allSongs.get(position).get("album_id"),context));
                                intent.putExtra("data",VinMediaLists.allSongs.get(position).get("data"));
                                intent.putExtra("title",VinMediaLists.allSongs.get(position).get("title"));
                                intent.putExtra("album",VinMediaLists.allSongs.get(position).get("album"));
                                intent.putExtra("artist",VinMediaLists.allSongs.get(position).get("artist"));
                               // intent.putExtra("genre",VinMediaLists.allSongs.get(position).get("genre"));
                                intent.putExtra("album_id",VinMediaLists.allSongs.get(position).get("album_id"));

                                context.startActivity(intent);
                                break;
                            case R.id.edit:

                                String filename = VinMediaLists.allSongs.get(position).get("data");
                                try {
                                    Intent intent1 = new Intent(Intent.ACTION_EDIT, Uri.parse(filename));
                                    intent1.setClassName( "com.vinay.vinplayer", "com.vinay.vinplayer.ringdroid.RingdroidEditActivity");
                                    context.startActivity(intent1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("Ringdroid", "Couldn't start editor");
                                }/*
                                context.startActivity(new Intent(context.getApplicationContext(), RingdroidSelectActivity.class));*/
                                break;
                        }
                        return false;
                    }
                });
                popup.show();

            }
        });


    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public String getSectionTitle(int position) {

        //this String will be shown in a bubble for specified position
        return mValues.get(position).get("title").substring(0, 1);
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

            more_icon.setColorFilter(Color.argb(180,255,255,255));
            songname.setTextColor(Color.WHITE);
            img_playindic.setColorFilter(Color.GREEN);

        }

    }
}
