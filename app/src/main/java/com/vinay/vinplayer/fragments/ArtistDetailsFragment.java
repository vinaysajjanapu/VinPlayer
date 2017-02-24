package com.vinay.vinplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.ArtistSongAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by salimatti on 2/4/2017.
 */

public class ArtistDetailsFragment extends DialogFragment {

    static ArrayList<HashMap<String,String>> allsongs;
    private ArtistDetailsFragment.OnArtistListFragmentInteractionListener mListener;

    public static ArtistDetailsFragment newInstance(ArrayList<HashMap<String,String>> arrayList) {

        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        allsongs=arrayList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            recyclerView.setAdapter(new ArtistSongAdapter(getContext(),allsongs, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ArtistDetailsFragment.OnArtistListFragmentInteractionListener) {
            mListener = (ArtistDetailsFragment.OnArtistListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnArtistListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnArtistListFragmentInteractionListener {

        void onArtistListFragmentInteraction(int i);

    }
}

