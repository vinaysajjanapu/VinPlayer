package com.vinay.vinplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinay.vinplayer.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vinaysajjanapu on 9/2/17.
 */

public class HomeFragment extends Fragment {

    public HomeFragment() {
    }
    public static HomeFragment newInstance( ) {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
      /*  if (context instanceof GenreFragment.OnGenreFragmentInteractionListner) {
   //         mListener = (GenreFragment.OnGenreFragmentInteractionListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }*/
    }


    @Override
    public void onDetach() {
        super.onDetach();
    //    mListener = null;
    }

    public interface OnGenreFragmentInteractionListner {
        void OnGenreFragmentInteraction(int pos);
    }
}
