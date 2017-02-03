package com.vinay.vinplayer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.AllSongsAdapter;
import com.vinay.vinplayer.adapters.QueueAdapter;
import com.vinay.vinplayer.helpers.VinMedia;


import java.util.ArrayList;
import java.util.HashMap;

public class QueueFragment extends Fragment {

    private OnQueueFragmentInteractionListener mListener;

    private static VinMedia vinMedia;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    static ArrayList<HashMap<String,String>> songQueue;

    public QueueFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static QueueFragment newInstance(int columnCount, VinMedia vinMedia1) {
        vinMedia = vinMedia1;
        QueueFragment fragment = new QueueFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        songQueue=vinMedia1.getCurrentList();
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

        // Set the adapter
   //     if (view instanceof RecyclerView) {
            Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.queue_list);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            recyclerView.setAdapter(new QueueAdapter(getContext(),songQueue, mListener));

        final float[] start_y = new float[1];
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    start_y[0] = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (recyclerView.getChildAt(0).getTop()==0){
                        if((int)(start_y[0]-event.getY())<50){
                            Log.d("scroll","dragged");
                            getActivity().sendBroadcast(new Intent().setAction(getString(R.string.closePanel)));
                        }
                    }
                }
                return false;
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        if (context instanceof AllSongsFragment.OnListFragmentInteractionListener) {
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
