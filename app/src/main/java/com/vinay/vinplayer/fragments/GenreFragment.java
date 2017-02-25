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
import com.vinay.vinplayer.adapters.GenreAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class GenreFragment extends Fragment {




    private GenreFragment.OnGenreFragmentInteractionListner mListener;
    private static ArrayList<HashMap<String,String>> albumsList;

    public GenreFragment() {
    }

    public static GenreFragment newInstance( ArrayList<HashMap<String,String>> arrayList) {
        albumsList = arrayList;
        return new GenreFragment();
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
            recyclerView.setAdapter(new GenreAdapter(getActivity(),albumsList, mListener));
            fastScroller.setRecyclerView(recyclerView);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GenreFragment.OnGenreFragmentInteractionListner) {
            mListener = (GenreFragment.OnGenreFragmentInteractionListner) context;
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

    public interface OnGenreFragmentInteractionListner {
        void OnGenreFragmentInteraction(int pos);
    }
}
