package com.vinay.vinplayer.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.activities.MetaDataEditor;
import com.vinay.vinplayer.fragments.ArtistsFragment;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder> implements SectionTitleProvider {

    private final ArrayList<HashMap<String,String>> mValues;
    private final ArtistsFragment.OnArtistFragmentInteractionListner mListener;
    private Context context;

    public ArtistsAdapter(Context context, ArrayList<HashMap<String,String>> items,
                          ArtistsFragment.OnArtistFragmentInteractionListner listener) {
        //Set<String> set = new HashSet<String>(list);
        mValues = items;
        this.context = context;
        mListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        String artist = mValues.get(position).get("artist");
        holder.mIdView.setText(artist);
        // holder.mContentView.setText(mValues.get(position).get("no_of_songs"));

        try {
            String subdir;
            String externalRootDir = Environment.getExternalStorageDirectory().getPath();
            if (!externalRootDir.endsWith("/")) {
                externalRootDir += "/";
            }
            subdir = "VinPlayer/.thumb/.artistcache/";
            String path = externalRootDir + subdir+artist+".jpg";

            Picasso.with(context)
                    .load("file://"+path)
                    .placeholder(R.drawable.albumart_default)
                    .error(R.drawable.albumart_default)
                    //.onlyScaleDown()
                    .resize(100,100)
                    .centerCrop()
                    .into(holder.imageView);


        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.OnArtistFragmentInteraction(position);
                }
            }
        });


        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.options);
                popup.inflate(R.menu.options_menu_albums);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.play:
                                ArrayList<HashMap<String, String>> artistsongs =  new ArrayList<>();
                                artistsongs = VinMediaLists.getInstance().getArtistSongsList(
                                        VinMediaLists.getInstance().getArtistsList(context).get(position).get("artist"),context);
                                VinMedia.getInstance().updateTempQueue( artistsongs,context);
                                VinMedia.getInstance().updateQueue(false,context);
                                VinMedia.getInstance().startMusic(0,context);
                                break;
                            case R.id.play_next:
                                artistsongs = new ArrayList<HashMap<String, String>>();
                                artistsongs = VinMediaLists.getInstance().getArtistSongsList(
                                        VinMediaLists.getInstance().getArtistsList(context).get(position).get("artist"),context);
                                int cur = VinMedia.getInstance().getPosition();
                                for (int i=0;i<artistsongs.size();i++){
                                    VinMedia.getInstance().getCurrentList().add(cur+i+1,artistsongs.get(i));
                                }
                                EventBus.getDefault().post(new MessageEvent(context.getString(R.string.queueUpdated)));
                                break;
                            case R.id.add_to_queue:
                                artistsongs = VinMediaLists.getInstance().getArtistSongsList(
                                        VinMediaLists.getInstance().getArtistsList(context).get(position).get("artist"),context);
                                VinMedia.getInstance().getCurrentList().addAll(artistsongs);
                                EventBus.getDefault().post(new MessageEvent(context.getString(R.string.queueUpdated)));
                                break;
                            case R.id.edit_art:/*
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
                                */
                                break;

                            case R.id.delete:

                                final ArrayList<HashMap<String,String>> deletelist = VinMediaLists.getInstance().getArtistSongsList(
                                        VinMediaLists.getInstance().getArtistsList(context).get(position).get("artist"),context);

                                final ProgressDialog pdialog = new ProgressDialog(context);

                                new AlertDialog.Builder(context)
                                        .setTitle("Confirm Delete")
                                        .setMessage("Are you sure you want to delete this Artist?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, int which) {
                                                try {
                                                    new AsyncTask<ArrayList<HashMap<String,String>>,Void,String>(){
                                                        @Override
                                                        protected String doInBackground(ArrayList<HashMap<String,String>>... params) {
                                                            try {
                                                                for (HashMap<String,String> h1 : params[0]) {
                                                                    File file = new File(h1.get("data"));
                                                                    file.delete();
                                                                }
                                                                return "success";
                                                            }catch (Exception e){
                                                                return "failed";
                                                            }
                                                        }

                                                        @Override
                                                        protected void onPreExecute() {
                                                            pdialog.setTitle("Deleting Artist");
                                                            pdialog.show();
                                                            super.onPreExecute();
                                                        }

                                                        @Override
                                                        protected void onPostExecute(String s) {
                                                            VinMediaLists.allArtists.remove(position);
                                                            for (HashMap<String,String> h: deletelist) {
                                                                context.sendBroadcast(new Intent(
                                                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                                                        Uri.fromFile(new File(h.get("data")))));
                                                                if(s.equals("success")) {
                                                                    try {
                                                                        VinMediaLists.allSongs.remove(h);
                                                                        Log.d("Delete","file deleted from allsongs");
                                                                    }catch (Exception e){

                                                                    }
                                                                }
                                                            }
                                                            EventBus.getDefault().post(
                                                                    new MessageEvent(
                                                                            context.getResources().getString(R.string.fileDeleted)));
                                                            pdialog.dismiss();
                                                        }
                                                    }.execute(deletelist);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Log.e("All songs adapter", "Cannot Delete");
                                                }
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                                dialog.dismiss();
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                /*context.startActivity(new Intent(context.getApplicationContext(), RingdroidSelectActivity.class));*/
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
        return mValues.get(position).get("artist").substring(0, 1);
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
  //      final TextView mContentView;
        final ImageView imageView;
        final ImageButton options;
        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.tv_album_title);
         //   mContentView = (TextView) view.findViewById(R.id.content);
            imageView = (ImageView)view.findViewById(R.id.albumart_albums);
            options = (ImageButton)view.findViewById(R.id.options);
        }


    }
}
