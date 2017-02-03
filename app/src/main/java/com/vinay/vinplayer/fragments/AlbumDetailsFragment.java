package com.vinay.vinplayer.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumSongsAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailsFragment extends DialogFragment {

    static ArrayList<HashMap<String,String>> allsongs;
    private OnAlbumListFragmentInteractionListener mListener;

    public static AlbumDetailsFragment newInstance(ArrayList<HashMap<String,String>> arrayList) {

        AlbumDetailsFragment fragment = new AlbumDetailsFragment();
        allsongs=arrayList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
          }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_album_details, container, false);

        // Set the adapter
            Context context = view.getContext();

            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.album_details_list);
            ImageView imageView= (ImageView) view.findViewById(R.id.iv_albumart);
        Toolbar toolbar= (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(allsongs.get(0).get("album"));
        toolbar.setSubtitle(allsongs.size()+"songs");
        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(allsongs.get(0).get("album_id")));
            Picasso.with(context).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                    .into(imageView);

        } catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

            recyclerView.setAdapter(new AlbumSongsAdapter(getContext(),allsongs, mListener));
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAlbumListFragmentInteractionListener) {
            mListener = (OnAlbumListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAlbumListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnAlbumListFragmentInteractionListener {

        void onAlbumListFragmentInteraction(int i);

    }
}

