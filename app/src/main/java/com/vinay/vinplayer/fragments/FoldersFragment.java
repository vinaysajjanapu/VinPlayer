package com.vinay.vinplayer.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vinay.vinplayer.R;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vinaysajjanapu on 9/2/17.
 */

public class FoldersFragment extends Fragment {

    private RecyclerView recyclerView;
    private String ROOT_DIR = "root";
    private TextView pathTextView;
    private Button back;
    FoldersAdapter foldersAdapter;
    private static String grand_parent,parent;
    private int no_albums_inpage=0;
    public static ArrayList<HashMap<String,String>> file_structure,playable_queue;

    private static volatile FoldersFragment Instance = null;
    OnFoldersFragmentInteractionListener mListener;
    private Object grandParent;

    public FoldersFragment() {
    }
    public static FoldersFragment newInstance( ) {
        return new FoldersFragment();
    }


    public static FoldersFragment getInstance() {
        FoldersFragment localInstance = Instance;
        if (localInstance == null) {
            synchronized (FoldersFragment.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new FoldersFragment();
                }
            }
        }
        return localInstance;
    }

    public void setParent(String parent) {
        FoldersFragment.parent = parent;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createFolderFileStructure();
        playable_queue = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container, false);
        grand_parent = "root";

        recyclerView=(RecyclerView) view.findViewById(R.id.pathlist);
        pathTextView=(TextView) view.findViewById(R.id.path);
        back = (Button)view.findViewById(R.id.folder_back);

        if (parent==null)parent="storage";
        foldersAdapter = new FoldersAdapter(getActivity(),getPageItems(parent));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(foldersAdapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!grand_parent.equals(ROOT_DIR)) {
                    foldersAdapter = new FoldersAdapter(getActivity(), getPageItems(grand_parent));
                    recyclerView.setAdapter(foldersAdapter);
                }
            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void createFolderFileStructure() {
        file_structure = new ArrayList<>();
        String[] path;
        for (int i = 0; i<VinMediaLists.getInstance().getAllSongsList(getActivity()).size();i++){
            path = VinMediaLists.getInstance().getAllSongsList(getActivity()).get(i).get("data").split("/");
            HashMap<String,String> h = new HashMap<>();
            for (int j=0;j<path.length;j++) {
                if (j==0)
                    h.put("pos",i+"");
                else if (j==1)
                    h.put("root",path[j]);
                else h.put(path[j-1],path[j]);

              //  Log.d("path",h.toString());
            }
            file_structure.add(h);
        }
    }

    private int getFileStructureSize(){
        return file_structure.size();
    }

    private ArrayList<HashMap<String, String>> getPageItems(String parent) {
        getGrandParent(parent);
        setParent(parent);
        pathTextView.setText(parent);

        no_albums_inpage = 0;
        ArrayList<HashMap<String, String>> pageItems = new ArrayList<>();
        ArrayList<HashMap<String, String>> tracks = new ArrayList<>();
        if (playable_queue != null) playable_queue.clear();
        for (int i = 0; i < getFileStructureSize(); i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            String name = file_structure.get(i).get(parent);
            String position = file_structure.get(i).get("pos");
            if ((name != null) && !(name.isEmpty())) {
                if (!alreadyPresent(pageItems, name)) {
                    if (!alreadyPresent(tracks, name)) {
                        hashMap.put("pos", position);
                        hashMap.put("name", name);
                        if (isAudio(name)) {
                            tracks.add(hashMap);
                        } else {
                            no_albums_inpage++;
                            pageItems.add(hashMap);
                        }
                    }
                }
            }
        }
        Collections.sort(tracks, new Comparator<Map<String, String>>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override public int compare(Map<String, String> a, Map<String, String> b) {
                return a.get("name").compareTo(b.get("name"));
            }
        });
        Collections.sort(pageItems, new Comparator<Map<String, String>>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override public int compare(Map<String, String> a, Map<String, String> b) {
                return a.get("name").compareTo(b.get("name"));
            }
        });

        for (int y=0;y<tracks.size();y++)
            addtoPlayableQueue(Integer.parseInt(tracks.get(y).get("pos")));

        pageItems.addAll(tracks);
        return pageItems;
    }

    private void addtoPlayableQueue(int position) {
        //Log.d("playable queue","updated  "+position);
        playable_queue.add(VinMediaLists.allSongs.get(position));
    }


    private boolean alreadyPresent(ArrayList<HashMap<String, String>> pageItems, String name) {
        for (int itr=0;itr<pageItems.size();itr++){
            for (Map.Entry<String,String> entry:pageItems.get(itr).entrySet()){
                if (entry.getValue().equals(name)){
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isAudio(String name){
        String[] audio_extensions = {".mp3", ".wav",".m4a", ".amr", ".flac", ".mp2",".ogg",".oga", ".mp4",".wave",
                                ".aac", ".3gp", ".m4b", ".3ga", ".awb", ".mid", ".midi", ".ota",".xmf",
                                 ".rtttl", ".rtx", ".imy", ".opus",".mxmf"};
        for (String ext : audio_extensions) {
            if (name.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public void getGrandParent(String parent) {
        for (int k=0; k<getFileStructureSize();k++){
            for (Map.Entry<String,String> entry : file_structure.get(k).entrySet()){
                if (entry.getValue().equals(parent)){
                    grand_parent = entry.getKey();
                    if(!grand_parent.equals("pos"))return;
                }
            }
        }
    }

    public interface OnFoldersFragmentInteractionListener {
        // TODO: Update argument type and name
        void OnFoldersFragmentInteraction(int i);

    }



    public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.ViewHolder>  {

        private  ArrayList<HashMap<String,String>> mValues;
        Context context;

        public FoldersAdapter(Context context, ArrayList<HashMap<String,String>> items) {
            mValues = items;
            this.context=context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //      vinMediaLists = new VinMediaLists(context);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_list_item, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(final FoldersAdapter.ViewHolder holder, final int position) {

            if (mValues!=null) {
                holder.list_text.setText(mValues.get(position).get("name"));
                try {
                    final Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri uri = ContentUris.withAppendedId(sArtworkUri,
                            Long.parseLong(VinMediaLists.allSongs.get(
                                    Integer.parseInt(mValues.get(position).get("pos"))).get("album_id")));
                    if (isAudio(mValues.get(position).get("name")))
                            Picasso.with(context).load(uri)
                                    .placeholder(R.drawable.albumart_default)
                                    .error(R.drawable.albumart_default)
                                    .into(holder.thumb);
                    else Picasso.with(context).load(uri+"xyz123error")
                            .placeholder(R.drawable.icon_folder)
                            .error(R.drawable.icon_folder)
                            .into(holder.thumb);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (isAudio(mValues.get(position).get("name"))){
                            VinMedia.getInstance().updateTempQueue(playable_queue,getActivity());
                            VinMedia.getInstance().updateQueue(false,getActivity());
                            VinMedia.getInstance().startMusic(position-no_albums_inpage,getActivity());
                        }else {
                            foldersAdapter = new FoldersAdapter(
                                    getActivity(),getPageItems(mValues.get(position).get("name")));
                            recyclerView.setAdapter(foldersAdapter);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return (mValues!=null)?mValues.size():0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView list_text;
            public final ImageView thumb;
            public HashMap<String,String> mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                list_text = (TextView)view.findViewById(R.id.folder_list_item_textview);
                thumb = (ImageView)view.findViewById(R.id.folder_list_item_thumb);
            }


        }



    }

}
