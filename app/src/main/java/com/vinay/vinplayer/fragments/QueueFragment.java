package com.vinay.vinplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.QueueAdapter;
import com.vinay.vinplayer.helpers.VinMedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class QueueFragment extends Fragment {

    private OnQueueFragmentInteractionListener mListener;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private int mColumnCount = 1;
    RecyclerView recyclerView;
    QueueAdapter queueAdapter;
    ArrayList<HashMap<String,String>> songQueue;

    public QueueFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static QueueFragment newInstance(int columnCount) {
        QueueFragment fragment = new QueueFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        setupBroadCastReceiver();

        if (VinMedia.getInstance().getCurrentList()!=null)songQueue = VinMedia.getInstance().getCurrentList();
        else songQueue = null;

        queueAdapter = new QueueAdapter(getActivity(),songQueue,mListener);
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.queue_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(queueAdapter);




        recyclerView.scrollToPosition(VinMedia.getInstance().getPosition());
        final float[] start_y = new float[1];
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    start_y[0] = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                   if (songQueue!=null) {
                       if (recyclerView.getChildAt(0).getTop() == 0) {
                           if ((int) (start_y[0] - event.getY()) < 50) {
                               Log.d("scroll", "dragged");
                               getActivity().sendBroadcast(new Intent().setAction(getString(R.string.closePanel)));
                           }
                       }
                   }else {
                       if ((int) (start_y[0] - event.getY()) < 50) {
                           Log.d("scroll", "dragged");
                           getActivity().sendBroadcast(new Intent().setAction(getString(R.string.closePanel)));
                       }
                   }
                }
                return false;
            }
        });


        return view;
    }
    private void setupBroadCastReceiver(){

        Log.d("queuefrag","setup bcast listener");

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
        recyclerView.scrollToPosition(VinMedia.getInstance().getPosition());
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void queueUpdated() {
        Log.d("queuefrag","queueupdated");
        songQueue= VinMedia.getInstance().getCurrentList();
        queueAdapter = new QueueAdapter(getActivity(),songQueue,mListener);
        recyclerView.setAdapter(queueAdapter);
    }


    @Override
    public void onAttach(Context context) {
        if (context instanceof OnQueueFragmentInteractionListener) {
            mListener = (OnQueueFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
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
    public interface OnQueueFragmentInteractionListener {
        // TODO: Update argument type and name
        void OnQueueFragmentInteraction(int i);

    }

}
