package com.vinay.vinplayer.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.database.FavouriteTable;
import com.vinay.vinplayer.database.PlaylistTable;
import com.vinay.vinplayer.database.RecentAddedTable;
import com.vinay.vinplayer.database.RecentPlayTable;
import com.vinay.vinplayer.database.RecommendedTable;
import com.vinay.vinplayer.database.UsageDataTable;
import com.vinay.vinplayer.helpers.VinMedia;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vinaysajjanapu on 9/2/17.
 */

public class HomeFragment extends Fragment {

    private RecyclerView list1;
    private TextView list_name;
    private Button list_button;
    private LinearLayout home_container;
    private RecyclerView.LayoutParams layoutParams;
    private int MAX_LIST_SIZE = 5;
    private int NUM_OF_LISTS = 6;
    private static ArrayList<HashMap<String,String>> songsList;

    public HomeFragment() {
    }
    public static HomeFragment newInstance( ) {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View scrollview = inflater.inflate(R.layout.fragment_home, container, false);

        if (scrollview instanceof ScrollView) {
            home_container = (LinearLayout)scrollview.findViewById(R.id.home_container);
            Context context = scrollview.getContext();

            if (NUM_OF_LISTS>6)NUM_OF_LISTS=6;
            for (int i=0;i<NUM_OF_LISTS;i++) {
                View view1 = inflater.inflate(R.layout.home_item, container, false);
                if (view1 instanceof RelativeLayout) {
                    list1 = (RecyclerView) view1.findViewById(R.id.list);
                    list_name = (TextView) view1.findViewById(R.id.list_name);
                    list_button = (Button) view1.findViewById(R.id.list_button);
                    list_button.setText("");
                    list_button.setTextColor(Color.WHITE);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    list1.setLayoutManager(layoutManager);

                    switch (i) {
                        case 0:
                            songsList = RecommendedTable.getInstance(context).getRecommendedList();
                            list_name.setText("Recommended");
                            break;
                        case 1:
                            songsList =  RecentPlayTable.getInstance(context).getRecentPlayList();
                            list_name.setText("Recently Played");
                            break;

                        case 2:
                            songsList = UsageDataTable.getInstance(context).getMostPlayedList();
                            list_name.setText("MostPlayed");
                            break;

                        case 3:
                            songsList = FavouriteTable.getInstance(context).getFavouriteList();
                            list_name.setText("Favourites");
                            break;
                        case 4:
                            songsList = RecentAddedTable.getInstance(context).getRecentAddedList(15);
                            list_name.setText("Recently Added");
                            break;
                        case 5:
                            songsList = PlaylistTable.getInstance(context).getPlaylistList("MyPlaylist");
                            list_name.setText("PlayList");
                            break;
                    }
                    final List1Adapter list1Adapter = new List1Adapter(context, songsList);
                    list1.setAdapter(list1Adapter);
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
                home_container.addView(view1);
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
        public void onBindViewHolder(ViewHolder holder, final int position) {
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

        }

        @Override
        public int getItemCount() {
            return (list.size()> MAX_LIST_SIZE) ? MAX_LIST_SIZE+1 : list.size();
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


}
