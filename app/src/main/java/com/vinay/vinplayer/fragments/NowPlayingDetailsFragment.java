package com.vinay.vinplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinay.vinplayer.R;

/**
 * Created by vinaysajjanapu on 14/2/17.
 */


public class NowPlayingDetailsFragment extends Fragment {

    private OnNowPlayingDetailsFragmentInteractionListener mListener;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    public NowPlayingDetailsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
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

        View view = inflater.inflate(R.layout.fragment_nowplaying_details, container, false);
        setupBroadCastReceiver();

        return view;
    }
    private void setupBroadCastReceiver(){

        intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.queueUpdated));
        intentFilter.addAction(getString(R.string.newSongLoaded));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("queuefrag","received");
                String action = intent.getAction();
                if (action.equals(getString(R.string.queueUpdated))){
                    queueUpdated();
                }else {
                    newSongLoaded();
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver,intentFilter);
    }

    private void newSongLoaded() {

    }

    private void queueUpdated() {

    }


    @Override
    public void onAttach(Context context) {
/*
        if (context instanceof OnNowPlayingDetailsFragmentInteractionListener) {
            mListener = (OnNowPlayingDetailsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
*/
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener=null;
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNowPlayingDetailsFragmentInteractionListener {
        // TODO: Update argument type and name
        void OnNowPlayingDetailsFragmentInteraction(int i);

    }

}
