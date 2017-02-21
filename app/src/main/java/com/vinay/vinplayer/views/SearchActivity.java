package com.vinay.vinplayer.views;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    ListView lv_album, lv_artist;
    EditText editsearch;
    AlbumAdapter albumadapter;
    ArtistAdapter artistadapter;
    RecyclerView rv_album;
    ArrayList<HashMap<String, String>> alllist = new ArrayList<HashMap<String, String>>();
    MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().hide();
              /*
        getSupportActionBar().setTitle("search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
*/
        lv_album = (ListView) findViewById(R.id.lv_album);
        lv_artist = (ListView) findViewById(R.id.lv_artist);
        alllist.addAll(VinMediaLists.allSongs);


        artistadapter = new ArtistAdapter(getApplicationContext(), VinMediaLists.allArtists);
        albumadapter = new AlbumAdapter(getApplicationContext(), VinMediaLists.allSongs);

        lv_album.setAdapter(albumadapter);
        lv_artist.setAdapter(artistadapter);

         rv_album = (RecyclerView) findViewById(R.id.rv_album);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_album.setLayoutManager(layoutManager);

        adapter = new MyAdapter(getApplicationContext(),VinMediaLists.allAlbums);
        rv_album.setAdapter(adapter);
        editsearch = (EditText) findViewById(R.id.search);

        editsearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = editsearch.getText().toString().toLowerCase(Locale.getDefault());

                artistadapter.filter(text);
                albumadapter.filter(text);
                adapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class AlbumAdapter extends BaseAdapter {

        Context mContext;
        LayoutInflater inflater;
        private List<HashMap<String,String>> malHashMap = new ArrayList<HashMap<String,String>>();
        private ArrayList<HashMap<String,String>> arraylist;

        public AlbumAdapter(Context context,
                               List<HashMap<String,String>> mHashMap) {
            mContext = context;
            //this.malHashMap = mHashMap;
            inflater = LayoutInflater.from(mContext);
            this.arraylist = new ArrayList<HashMap<String,String>>();
            this.arraylist.addAll(mHashMap);
        }

        class ViewHolder {
            TextView title;

        }

        @Override
        public int getCount() {
            return malHashMap.size();
        }

        @Override
        public HashMap<String,String> getItem(int position) {
            return malHashMap.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View view, ViewGroup parent) {
            final ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.row_search, null);
                holder.title = (TextView) view.findViewById(R.id.tv_search);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.title.setText(malHashMap.get(position).get("title"));


            holder.title.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                }
            });

            return view;
        }

        private void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            malHashMap.clear();
            if (charText.length() ==0) {
                //mHashMap.clear();
            } else {
                for (HashMap<String,String> wp : arraylist) {

                    if (wp.get("title").toLowerCase(Locale.getDefault()).startsWith(charText)) {
                        if (!malHashMap.contains(wp))malHashMap.add(wp);
                    }
                }
                for (HashMap<String,String> wp : arraylist) {
                    if (wp.get("title").toLowerCase(Locale.getDefault()).contains(charText)) {
                        if (!malHashMap.contains(wp))malHashMap.add(wp);
                    }
                }
            }
            albumadapter.notifyDataSetChanged();
        }

    }
    public class ArtistAdapter extends BaseAdapter {

        Context mContext;
        LayoutInflater inflater;
        private List<HashMap<String,String>> mHashMap =new ArrayList<HashMap<String,String>>();
        private ArrayList<HashMap<String,String>> arraylist;


        public ArtistAdapter(Context context,
                               List<HashMap<String,String>> mHashMap) {
            mContext = context;
            //this.mHashMap = mHashMap;
            inflater = LayoutInflater.from(mContext);
            this.arraylist = new ArrayList<HashMap<String,String>>();
            this.arraylist.addAll(mHashMap);

        }

        class ViewHolder {
            TextView title;

        }

        @Override
        public int getCount() {
            return mHashMap.size();
        }

        @Override
        public HashMap<String,String> getItem(int position) {
            return mHashMap.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View view, ViewGroup parent) {
            final ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.row_search, null);
                holder.title = (TextView) view.findViewById(R.id.tv_search);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.title.setText(mHashMap.get(position).get("artist"));


            holder.title.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                }
            });

            return view;
        }

        private void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            mHashMap.clear();
            if (charText.length() ==0) {
                //mHashMap.clear();
            } else {
                for (HashMap<String,String> wp : arraylist) {

                    if (wp.get("artist").toLowerCase(Locale.getDefault()).startsWith(charText)) {
                        if (!mHashMap.contains(wp))
                            mHashMap.add(wp);
                    }}
                for (HashMap<String,String> wp : arraylist) {
                    if (wp.get("artist").toLowerCase(Locale.getDefault()).contains(charText)) {
                        if (!mHashMap.contains(wp))
                        mHashMap.add(wp);
                    }
                }
            }
            artistadapter.notifyDataSetChanged();
        }

    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.AlViewHolder>{

        Context mContext;
        LayoutInflater inflater;
        private List<HashMap<String,String>> mHashMap =new ArrayList<HashMap<String,String>>();
        private ArrayList<HashMap<String,String>> arraylist;


        MyAdapter(Context c,ArrayList<HashMap<String,String>> a){
            mContext = c;
            //this.mHashMap = mHashMap;
            inflater = LayoutInflater.from(mContext);
            this.arraylist = new ArrayList<HashMap<String,String>>();
            this.arraylist.addAll(a);
        }

        @Override
        public AlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_search, parent, false);
            return new AlViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AlViewHolder holder, int position) {

            holder.title.setText(mHashMap.get(position).get("album"));
        }

        @Override
        public int getItemCount() {
            return mHashMap.size();
        }

        public class AlViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            public AlViewHolder(View itemView) {
                super(itemView);
                title= (TextView) itemView.findViewById(R.id.tv_search);
            }
        }

        private void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            mHashMap.clear();
            if (charText.length() ==0) {
                //mHashMap.clear();
            } else {
                for (HashMap<String,String> wp : arraylist) {

                    if (wp.get("album").toLowerCase(Locale.getDefault()).startsWith(charText)) {
                        if (!mHashMap.contains(wp))
                            mHashMap.add(wp);
                    }}
                for (HashMap<String,String> wp : arraylist) {
                    if (wp.get("album").toLowerCase(Locale.getDefault()).contains(charText)) {
                        if (!mHashMap.contains(wp))
                            mHashMap.add(wp);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
}



