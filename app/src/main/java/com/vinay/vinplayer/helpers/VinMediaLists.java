/**
 * Created by vinaysajjanapu on 31/1/17.
 */
package com.vinay.vinplayer.helpers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.vinay.vinplayer.BuildConfig;
import com.vinay.vinplayer.R;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;

public class VinMediaLists {

    private String selection;
    private String sortOrder;

    public static ArrayList<HashMap<String,String>> allSongs;
    public static ArrayList<HashMap<String,String>> allAlbums;
    public static ArrayList<HashMap<String,String>> allArtists;
    public static ArrayList<HashMap<String,String>> allgenres;

    private static volatile VinMediaLists Instance = null;

    public static VinMediaLists getInstance() {
        VinMediaLists localInstance = Instance;
        if (localInstance == null) {
            synchronized (VinMediaLists.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new VinMediaLists();
                }
            }
        }
        return localInstance;
    }

    public ArrayList<HashMap<String,String>> getAllSongsList(Context context) {
        return (allSongs!=null) ? allSongs : createAllSongsList(context);
    }


    public ArrayList<HashMap<String,String>> createAllSongsList(Context context){
        ArrayList<HashMap<String,String>> allSongsList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = contentResolver.query(uri, null, selection, null, sortOrder);
        int count = 0;
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String id=cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String album = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String album_id = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    String artist = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String artist_id = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
                    String date_added = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                    String duration = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String size = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    String title = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String display_name = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    String track = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
                    String year = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
                    String bookmark = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.BOOKMARK));

                    HashMap<String,String> h = new HashMap<>();
                    h.put("data",data);
                    h.put("id",id);
                    h.put("album",album);
                    h.put("album_id",album_id);
                    h.put("artist",artist);
                    h.put("artist_id",artist_id);
                    h.put("date_added",date_added);
                    h.put("duration",duration);
                    h.put("size",size);
                    h.put("title",title);
                    h.put("track",track);
                    h.put("year",year);
                    h.put("bookmark",bookmark);
                    h.put("display_name",display_name);
                    h.put("index",cur.getPosition()+"");
                    allSongsList.add(h);

            //      Log.d("song data", data);

                }
            }
        }
        if (cur!=null)
               cur.close();
        allSongs=allSongsList;
        return allSongsList;
    }


    public ArrayList<HashMap<String, String>> getListByKey(String key, String value,Context context){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        allSongs = getAllSongsList(context);
        for(int itr =0;itr<allSongs.size();itr++){
            if (allSongs.get(itr).get(key).equals(value)){
               // Log.d("matched",allSongs.get(itr).get(key));
                list.add(allSongs.get(itr));
            }
        }
        return list;
    }

    public ArrayList<HashMap<String, String>> getAlbumSongsList(String album,Context context){
        return getListByKey("album",album,context);
    }

    public ArrayList<HashMap<String, String>> getArtistSongsList(String artist,Context context){
        return getListByKey("artist",artist,context);
    }

    public ArrayList<HashMap<String, String>> getAlbumsList(Context context) {
        return (allAlbums!=null) ? allAlbums : createAlbumsList(context);
    }

        public ArrayList<HashMap<String, String>> createAlbumsList(Context context){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        selection = MediaStore.Audio.Albums.NUMBER_OF_SONGS + "!= 0";
        sortOrder = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cur = contentResolver.query(uri, null, null, null, sortOrder);
        int count = 0;
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String album = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM));

                    String album_id = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ID));
                    //String no_of_songs = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS));
//                    String album_art = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART));

                    HashMap<String,String> h=new HashMap<>();
                    h.put("album",album);
                    h.put("album_id",album_id);
                    //h.put("no_of_songs",no_of_songs);
  //                  h.put("album_art",album_art);
                    if (!list.contains(h))list.add(h);
                }
            }
        }
        cur.close();

        allAlbums = list;
        return list;
    }



    public ArrayList<HashMap<String, String>> getArtistsList(Context context) {
        return (allArtists!=null) ? allArtists : createArtistsList(context);
    }

    public ArrayList<HashMap<String, String>> createArtistsList(Context context){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        selection = MediaStore.Audio.Albums.NUMBER_OF_SONGS + "!= 0";
        sortOrder = MediaStore.Audio.Artists.ARTIST;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cur = contentResolver.query(uri, null, null, null, sortOrder);
        int count = 0;
        int num_songs = 0,index=0;
        if(cur != null) {
            count = cur.getCount();
            if (count > 0) {
                HashMap<String, String> h2 = new HashMap<>();
                while (cur.moveToNext()) {
                    String artist = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST));
                    HashMap<String, String> h = new HashMap<>();
                    h.put("artist", artist);
                    Log.d("artistname",artist);
                    if (!list.contains(h)) {
                        list.add(h);
                    }
                }
            }
            cur.close();
        }

        allArtists = list;
        return list;
    }


    public ArrayList<HashMap<String,String>> createAdvancedArtistList(Context context){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        if(allArtists==null)
            createArtistsList(context);
        HashMap<String,String> artist_detail;
        int count;
        for (int itr = 0; itr < allArtists.size(); itr++){
            artist_detail = new HashMap<>();
            String artist = allArtists.get(itr).get("artist");
            artist_detail.put("artist",artist);
            count = 0;
            for (int m=0; m< allSongs.size(); m++ ){
                if (allSongs.get(m).get("artist").equals(artist)){
                    String val = allSongs.get(m).get("album_id");
                    if (!artist_detail.containsValue(val))
                        artist_detail.put("album_id"+(++count),val);
                }
            }
            artist_detail.put("num_songs",count+"");
            list.add(artist_detail);
        }
        allArtists = list;
        return list;
    }


    public ArrayList<HashMap<String, String>> getGenresList(Context context) {
        return (allgenres!=null) ? allgenres : createAdvancedGenresList(context);
    }

    public ArrayList<HashMap<String, String>> createGenresList(Context context){
        int index;
        long genreId;
        Uri uri;
        Cursor genrecursor;
        Cursor tempcursor;
        String[] proj1 = {MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID};
        String[] proj2={MediaStore.Audio.Media.DISPLAY_NAME};
        String result;
        HashMap<String,String> genre;
        genrecursor = context.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, proj1, null, null, null);
        ArrayList<HashMap<String,String>> genres = new ArrayList<HashMap<String, String>>();



        if (genrecursor!=null && genrecursor.moveToFirst()) {
            do {
                genre = new HashMap<>();
                index = genrecursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
                if (BuildConfig.DEBUG) // Log.i("Tag-Genre name", genrecursor.getString(index));
                genre.put("genre",genrecursor.getString(index));
                if(genre.get("genre").equalsIgnoreCase("")) {
                    genre.put("genre","no-genre");
                }

                index = genrecursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
                genreId = Long.parseLong(genrecursor.getString(index));
                uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);

                tempcursor =  context.getContentResolver().query(uri, proj2, null, null, null);
                if (BuildConfig.DEBUG) if (tempcursor != null) {
                }
                int i;
                if (tempcursor != null && tempcursor.moveToFirst()) {
                    i=0;
                    do {
                        index = tempcursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                         genre.put("display_name_"+i, tempcursor.getString(index));
                         i++;
                    } while (tempcursor.moveToNext());
                    tempcursor.close();
                }
                    if (!genres.contains(genre)) {
                        genres.add(genre);

                    }
                }
                while(genrecursor.moveToNext());
        }
        genrecursor.close();
        allgenres=genres;
     return genres;
    }
    public ArrayList<HashMap<String, String>> getGenreSongsList(int pos,Context context){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        if (Integer.parseInt(allgenres.get(pos).get("num_songs"))!=0) {
            for(int itr =0;itr<allSongs.size();itr++){
                for(int z=0;z<allgenres.get(pos).size();z++) {
                    if (allSongs.get(itr).get("display_name").equals(allgenres.get(pos).get("display_name_" + z))) {
                        list.add(allSongs.get(itr));
                    }
                }
            }
        }
        list.size();
        // Log.d("genre_list",list.size()+"   "+list.toString());
        return list;
    }


    public ArrayList<HashMap<String,String>> createAdvancedGenresList(Context context){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        if(allgenres==null)
            createGenresList(context);
        HashMap<String,String> genre_detail;
        int count;
        for (int itr = 0; itr < allgenres.size(); itr++){
            genre_detail = new HashMap<>();
            String genre = allgenres.get(itr).get("genre");
            genre_detail.put("genre",genre);
            count = 0;
            for (int m=0; m< allSongs.size(); m++ ){
                for(int z=0;z<allgenres.get(itr).size();z++) {
                    String disp = allgenres.get(itr).get("display_name_" + z);
                    if (allSongs.get(m).get("display_name").equals(disp)) {
                        String val = allSongs.get(m).get("album_id");
                        if (!genre_detail.containsValue(val)) {
                            genre_detail.put("album_id" + (++count), val);
                            genre_detail.put("display_name_"+z,disp);
                        }
                    }
                }
            }
            genre_detail.put("num_songs",count+"");
            if (count!=0)
                list.add(genre_detail);
        }
        allgenres = list;
        return list;
    }



    public Bitmap getAlbumart(Long album_id,Context context)
    {

        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }else {
                bm = BitmapFactory.decodeResource(context.getResources(),R.drawable.albumart_default);
            }
        } catch (Exception e) {
            bm = BitmapFactory.decodeResource(context.getResources(),R.drawable.albumart_default);
        }
        return bm;
    }

    public Boolean isAlbumArtAvailable(String album_id,Context context){
        Long album = Long.parseLong(album_id);
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }
}
