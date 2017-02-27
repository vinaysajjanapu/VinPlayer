package com.vinay.vinplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.ArtistsAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistsFragment extends Fragment {



    FastScroller fastScroller;
    private OnArtistFragmentInteractionListner mListener;
    private static ArrayList<HashMap<String,String>> artistsList;

    public ArtistsFragment() {
    }

    public static ArtistsFragment newInstance( ArrayList<HashMap<String,String>> arrayList) {
        artistsList = arrayList;
        return new ArtistsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        // Set the adapter
        if (view instanceof RelativeLayout) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
            FastScroller fastScroller = (FastScroller) view.findViewById(R.id.fastscroll);
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            recyclerView.setAdapter(new ArtistsAdapter(getActivity(), artistsList, mListener));
            fastScroller.setRecyclerView(recyclerView);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnArtistFragmentInteractionListner) {
            mListener = (OnArtistFragmentInteractionListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnArtistFragmentInteractionListner {
        void OnArtistFragmentInteraction(int pos);
    }
}
