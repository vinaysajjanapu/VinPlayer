package com.vinay.vinplayer.fragments;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.activities.MetaDataEditor;
import com.vinay.vinplayer.database.FavouriteTable;
import com.vinay.vinplayer.database.PlaylistTable;
import com.vinay.vinplayer.database.RecentAddedTable;
import com.vinay.vinplayer.database.RecentPlayTable;
import com.vinay.vinplayer.database.RecommendedTable;
import com.vinay.vinplayer.database.UsageDataTable;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vinaysajjanapu on 9/2/17.
 */

public class HomeFragment extends Fragment {

    private View view1[];
    private RecyclerView list1[];
    private TextView list_name[];
    private Button list_button;
    private LinearLayout home_container;
    private RecyclerView.LayoutParams layoutParams;
    private int MAX_LIST_SIZE = 5;
    private int NUM_OF_LISTS = 6;
    private ArrayList<HashMap<String,String>> songsList;
    static LayoutInflater inflater1;
    static ViewGroup container1;
    static Bundle savedInstanceState1;

    public HomeFragment() {
    }
    public static HomeFragment newInstance( ) {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void newSongLoaded(){
            songsList = RecentPlayTable.getInstance(getActivity()).getRecentPlayList();
            List1Adapter list1Adapter = new List1Adapter(getActivity(), songsList);
            list1[1].setAdapter(list1Adapter);
        onCreateView(inflater1,container1,savedInstanceState1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflater1 = inflater;
        container1 = container;
        savedInstanceState1 = savedInstanceState;
        View scrollview = inflater.inflate(R.layout.fragment_home, container, false);

        if (scrollview instanceof ScrollView) {
            home_container = (LinearLayout)scrollview.findViewById(R.id.home_container);
            final Context context = scrollview.getContext();

            if (NUM_OF_LISTS>6)NUM_OF_LISTS=6;

            view1 = new View[NUM_OF_LISTS];
            list1 = new RecyclerView[NUM_OF_LISTS];
            list_name = new TextView[NUM_OF_LISTS];
/*
            new createRecommendedList1(){
                @Override
                protected void onPostExecute(String s) {
                    if (list1[0]!=null){
                        songsList = RecommendedTable.getInstance(context).getRecommendedList();
                        List1Adapter list1Adapter = new List1Adapter(context, songsList);
                        list1[0].setAdapter(list1Adapter);
                        list_name[0].setText("Recommended");
                    }
                }
            }.execute();*/

            for (int i=0;i<NUM_OF_LISTS;i++) {
                view1[i] = inflater.inflate(R.layout.home_item, container, false);
                if (view1[i] instanceof RelativeLayout) {
                    list1[i] = (RecyclerView) view1[i].findViewById(R.id.list);
                    list_name[i] = (TextView) view1[i].findViewById(R.id.list_name);
                    list_button = (Button) view1[i].findViewById(R.id.list_button);
                    list_button.setText("");
                    list_button.setTextColor(Color.WHITE);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    list1[i].setLayoutManager(layoutManager);

                    switch (i) {
                        case 0:
                            songsList = RecommendedTable.getInstance(context).getRecommendedList();
                            if (songsList.size()!=0)
                                list_name[i].setText("Recommended");
                            break;
                        case 1:
                            songsList =  RecentPlayTable.getInstance(context).getRecentPlayList();
                            if (songsList.size()!=0)
                                list_name[i].setText("Recently Played");
                            break;

                        case 2:
                            songsList = UsageDataTable.getInstance(context).getMostPlayedList();
                            if (songsList.size()!=0)
                                list_name[i].setText("MostPlayed");
                            break;

                        case 3:
                            songsList = FavouriteTable.getInstance(context).getFavouriteList();
                            if (songsList.size()!=0)
                                list_name[i].setText("Favourites");
                            break;
                        case 4:
                            songsList = RecentAddedTable.getInstance(context).getRecentAddedList(15);
                            if (songsList.size()!=0)
                                list_name[i].setText("Recently Added");
                            break;
                        case 5:
                            songsList = PlaylistTable.getInstance(context).getPlaylistList("MyPlaylist");
                            if (songsList.size()!=0)
                                list_name[i].setText("PlayList");
                            break;
                    }
                    final List1Adapter list1Adapter = new List1Adapter(context, songsList);
                    list1[i].setAdapter(list1Adapter);
                    list_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            VinMedia.getInstance().updateTempQueue(songsList,getActivity());
                            VinMedia.getInstance().updateQueue(false,getActivity());
                            VinMedia.getInstance().setPosition(0);
                            VinMedia.getInstance().startMusic(0,getActivity());
                        }
                    });
                }
                home_container.addView(view1[i]);
            }
        }

        return scrollview;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class List1Adapter extends RecyclerView.Adapter<List1Adapter.ViewHolder>{

        Context context;
        ArrayList<HashMap<String,String>> list;
        List1Adapter(Context context,ArrayList<HashMap<String,String>> list){
            this.list = list;
            this.context = context;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (position!=MAX_LIST_SIZE)
                holder.mIdView.setText(list.get(position).get("title"));
            else
                holder.mIdView.setText("View All");
            try {
                Uri uri;
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                if (position!= MAX_LIST_SIZE){
                    uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(list.get(position).get("album_id")));
                }else uri = sArtworkUri;
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
                return (list.size()> MAX_LIST_SIZE) ? MAX_LIST_SIZE+1 : list.size();
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
    private class createRecommendedList1 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            RecommendedTable.getInstance(VinPlayer.applicationContext).createRecommendationsList();
            return "Executed";
        }

        @Override
        protected void onPreExecute() {
            Log.d("async task","pre execute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.d("creating list",values.toString()+"\t");
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        String action = event.message;
        if (action.equals(getString(R.string.newSongLoaded))){
            newSongLoaded();
        }
    }
}
