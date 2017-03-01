package com.vinay.vinplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumsAdapter;
import com.vinay.vinplayer.helpers.MessageEvent;
import com.vinay.vinplayer.helpers.VinMediaLists;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumsFragment extends Fragment {


    FastScroller fastScroller;
    private OnAlbumFragmentInteractionListner mListener;
    private static ArrayList<HashMap<String,String>> albumsList;
    private RecyclerView recyclerView;

    public AlbumsFragment() {
    }

    public static AlbumsFragment newInstance( ArrayList<HashMap<String,String>> arrayList) {
        albumsList = arrayList;
        return new AlbumsFragment();
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
            fastScroller = (FastScroller) view.findViewById(R.id.fastscroll);
            recyclerView = (RecyclerView) view.findViewById(R.id.list);
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            recyclerView.setAdapter(new AlbumsAdapter(getActivity(),albumsList, mListener));
            fastScroller.setRecyclerView(recyclerView);

        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
        if (context instanceof OnAlbumFragmentInteractionListner) {
            mListener = (OnAlbumFragmentInteractionListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
        mListener = null;
    }
    public interface OnAlbumFragmentInteractionListner {
        // TODO: Update argument type and name
        void OnAlbumFragmentInteraction(int pos);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        //Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        String action = event.message;
        if (action.equals(getString(R.string.fileDeleted))){
            Log.d("eventbus", " album deleted");
            recyclerView.getAdapter().notifyDataSetChanged();
            //recyclerView.setAdapter(new AlbumsAdapter(getActivity(), VinMediaLists.allAlbums, mListener));
            //  recyclerView.setAdapter(new AllSongsAdapter(getContext(),allsongs, mListener));
        }
    }
}
