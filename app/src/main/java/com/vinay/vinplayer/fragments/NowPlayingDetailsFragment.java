package com.vinay.vinplayer.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vinaysajjanapu on 14/2/17.
 */


public class NowPlayingDetailsFragment extends Fragment {

    LinearLayout npd_container;
    RecyclerView albumsrecyclerview,artistrecyclerview;
    TextView albumlist_name,artistlistname;
    Button albumlist_button,artistlistbutton;
    View scrollview;
    ArrayList<HashMap<String,String>> albumsongslist,artistsongslist;
    public NowPlayingDetailsFragment() {
    }

    public static NowPlayingDetailsFragment newInstance() {
        NowPlayingDetailsFragment fragment = new NowPlayingDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        scrollview = inflater.inflate(R.layout.fragment_nowplaying_details, container, false);

        if (scrollview instanceof ScrollView){
            npd_container = (LinearLayout) scrollview.findViewById(R.id.npdetails_container);
            final Context context = scrollview.getContext();

            albumsrecyclerview = (RecyclerView)scrollview.findViewById(R.id.albumsongslist);
            albumlist_name = (TextView) scrollview.findViewById(R.id.list_name);
            albumlist_button = (Button) scrollview.findViewById(R.id.list_button);

            artistrecyclerview = (RecyclerView)scrollview.findViewById(R.id.artistsongslist);
            artistlistname = (TextView) scrollview.findViewById(R.id.list_name1);
            artistlistbutton = (Button) scrollview.findViewById(R.id.list_button1);

            albumlist_name.setText("More from Album");
            albumlist_name.setTextColor(Color.WHITE);

            artistlistname.setText("More from Artist");
            artistlistname.setTextColor(Color.WHITE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            LinearLayoutManager layoutManager1 = new LinearLayoutManager(context);
            layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);

            albumsrecyclerview.setLayoutManager(layoutManager);
            artistrecyclerview.setLayoutManager(layoutManager1);

            if (VinMedia.getInstance().currentSongDetails!=null) {
                albumsongslist = VinMediaLists.getInstance().getAlbumSongsList(
                        VinMedia.getInstance().currentSongDetails.get("album"), getActivity()
                );

                artistsongslist = VinMediaLists.getInstance().getArtistSongsList(
                        VinMedia.getInstance().currentSongDetails.get("artist"), getActivity()
                );
                List1Adapter albumsongsAdapter = new List1Adapter(context, albumsongslist);
                List1Adapter artistsongsAdapter = new List1Adapter(context, artistsongslist);

                albumsrecyclerview.setAdapter(albumsongsAdapter);
                artistrecyclerview.setAdapter(artistsongsAdapter);
            }
        }
        return scrollview;
    }

    private void newSongLoaded() {
        if (VinMedia.getInstance().currentSongDetails!=null) {
            albumsongslist = VinMediaLists.getInstance().getAlbumSongsList(
                    VinMedia.getInstance().currentSongDetails.get("album"), getActivity()
            );
            List1Adapter albumsongsAdapter = new List1Adapter(getActivity(), albumsongslist);
            albumsrecyclerview.setAdapter(albumsongsAdapter);

            artistsongslist = VinMediaLists.getInstance().getArtistSongsList(
                    VinMedia.getInstance().currentSongDetails.get("artist"), getActivity()
            );
            List1Adapter artistsadapter = new List1Adapter(getActivity(), artistsongslist);
            artistrecyclerview.setAdapter(artistsadapter);
        }
    }

    private void queueUpdated() {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        //Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        if (event.message.equals(getString(R.string.queueUpdated))){
            queueUpdated();
        }
        if(event.message.equals(getString(R.string.newSongLoaded))){
            newSongLoaded();
        }
    }

    private class List1Adapter extends RecyclerView.Adapter<List1Adapter.ViewHolder>{

        Context context;
        ArrayList<HashMap<String,String>> list;
        List1Adapter(Context context,ArrayList<HashMap<String,String>> list){
            this.list = list;
            this.context = context;
        }
        @Override
        public List1Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false);
            return new List1Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final List1Adapter.ViewHolder holder, final int position) {
            holder.mIdView.setText(list.get(position).get("title"));

            try {
                Uri uri;
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(list.get(position).get("album_id")));
                //ImageLoader.getInstance().displayImage(uri.toString(), holder.imageView);
                Picasso.with(context).load(uri).fit().error(R.drawable.albumart_default).into(holder.imageView);

            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VinMedia.getInstance().updateTempQueue(list,getActivity());
                    VinMedia.getInstance().updateQueue(false,getActivity());
                    VinMedia.getInstance().setPosition(position);
                    VinMedia.getInstance().startMusic(position,getActivity());
                }
            });


            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(context, holder.mView);
                    popup.inflate(R.menu.options_menu_homelist);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.play:
                                    VinMedia.getInstance().updateTempQueue(list,getActivity());
                                    VinMedia.getInstance().updateQueue(false,getActivity());
                                    VinMedia.getInstance().setPosition(position);
                                    VinMedia.getInstance().startMusic(position,getActivity());
                                    break;
                                case R.id.view_list:

                                    break;
                                case R.id.go_to_album:

                                    break;
                                case R.id.go_to_artist:

                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();

                    return true;
                }
            });


        }

        @Override
        public int getItemCount() {
            if (list!=null)
                return list.size();
            else
                return 0;
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mIdView;
            final ImageView imageView;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.tv_album_title);
                imageView = (ImageView) view.findViewById(R.id.albumart_albums);
                mIdView.setTextColor(Color.argb(120,255,255,255));
            }
        }
    }

}
