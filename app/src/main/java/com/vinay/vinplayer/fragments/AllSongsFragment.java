package com.vinay.vinplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.activities.MainActivity;
import com.vinay.vinplayer.adapters.AllSongsAdapter;
import com.vinay.vinplayer.helpers.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;




public class AllSongsFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    static ArrayList<HashMap<String,String>> allsongs;
    private OnListFragmentInteractionListener mListener;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private RecyclerView recyclerView;

    FastScroller fastScroller;
    public AllSongsFragment() {
    }

    public static AllSongsFragment newInstance(int columnCount, ArrayList<HashMap<String,String>> arrayList) {
        AllSongsFragment fragment = new AllSongsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        allsongs=arrayList;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
          //  NextSongDataTable.getInstance(getActivity()).initializeNextSongData();
           // RecentAddedTable.getInstance(getActivity()).updateRecentAddedList();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_allsongs, container, false);

           Context context = view.getContext();
            recyclerView = (RecyclerView) view.findViewById(R.id.list);
            fastScroller = (FastScroller) view.findViewById(R.id.fastscroll);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new AllSongsAdapter(getContext(),allsongs, mListener));

        //has to be called AFTER RecyclerView.setAdapter()
        fastScroller.setRecyclerView(recyclerView);
        return view;
    }


    private void newSongLoaded() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
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

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(int i);

    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        //Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        String action = event.message;
        if (action.equals(getString(R.string.newSongLoaded))){
            newSongLoaded();
        }
        if (action.equals(getString(R.string.fileDeleted))){
            Log.d("eventbus", " file deleted");
            recyclerView.getAdapter().notifyDataSetChanged();
            //  recyclerView.setAdapter(new AllSongsAdapter(getContext(),allsongs, mListener));
        }
    }
}

