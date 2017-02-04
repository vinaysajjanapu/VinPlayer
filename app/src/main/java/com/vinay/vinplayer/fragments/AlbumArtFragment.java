/**
 * Created by vinaysajjanapu on 2/2/17.
 */

package com.vinay.vinplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.QueueAdapter;
import com.vinay.vinplayer.helpers.VinMedia;

import java.util.HashMap;


public class AlbumArtFragment extends Fragment {

    private String album_id = null;
    ImageView albumArt;

    HashMap<String,String> songDetails;
    static VinMedia vinMedia;

    public AlbumArtFragment() {    }

    public static AlbumArtFragment newInstance(int position, Context mContext,VinMedia vinMedia1) {
        AlbumArtFragment fragment = new AlbumArtFragment();
        vinMedia = vinMedia1;
        if (vinMedia.getCurrentList()==null){
            fragment.songDetails = null;
        }else {
            fragment.songDetails = vinMedia.getCurrentList().get(position);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albumart, container, false);
        albumArt = (ImageView)view.findViewById(R.id.albumart_nowplaying);

        if (vinMedia.getCurrentList()!=null)songDetails=vinMedia.getCurrentList().get(vinMedia.getPosition());

        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(songDetails.get("album_id")));

            Picasso.with(getActivity()).load(uri).placeholder(R.drawable.albumart_default).error(R.drawable.albumart_default)
                    .into(albumArt);
        }catch (Exception e){
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        OnFragmentInteractionListener mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
