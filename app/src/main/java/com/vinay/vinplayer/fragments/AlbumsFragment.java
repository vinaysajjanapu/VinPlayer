package com.vinay.vinplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AlbumsAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import ooo.oxo.library.widget.PullBackLayout;

public class AlbumsFragment extends Fragment implements PullBackLayout.Callback{

    private OnAlbumFragmentInteractionListner mListener;
    private static ArrayList<HashMap<String,String>> albumsList;

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
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

                recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            recyclerView.setAdapter(new AlbumsAdapter(getActivity(),albumsList, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mListener = null;
    }

    @Override
    public void onPullStart() {

    }

    @Override
    public void onPull(float v) {

    }

    @Override
    public void onPullCancel() {

    }

    @Override
    public void onPullComplete() {

    }

    public interface OnAlbumFragmentInteractionListner {
        // TODO: Update argument type and name
        void OnAlbumFragmentInteraction(int pos);
    }
}
