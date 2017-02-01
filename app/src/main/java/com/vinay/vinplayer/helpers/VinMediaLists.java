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
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.vinay.vinplayer.R;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;

public class VinMediaLists {

    Context context;
    private ContentResolver contentResolver;
    private String selection;
    private String sortOrder;
    private Uri uri;
    public static ArrayList<HashMap<String,String>> allSongs;
    public static ArrayList<HashMap<String,String>> allAlbums;
    public static ArrayList<HashMap<String,String>> allArtists;


    public VinMediaLists(Context context){
        this.context=context;
    }

    public ArrayList<HashMap<String,String>> getAllSongsList(){
        ArrayList<HashMap<String,String>> allSongsList = new ArrayList<>();
        contentResolver = context.getContentResolver();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
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
                    // Add code to get more column here
                    String id=cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID));
                    String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String album_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String artist_key = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_KEY));
                    String artist_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                    String date_added = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                    String duration = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String size = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String track = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TRACK));
                    String year = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.YEAR));
                    String bookmark = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.BOOKMARK));

                    HashMap<String,String> h=new HashMap<>();
                    h.put("data",data);
                    h.put("id",id);
                    h.put("album",album);
                    h.put("album_id",album_id);
                    h.put("artist",artist);
                    h.put("artist_key",artist_key);
                    h.put("artist_id",artist_id);
                    h.put("date_added",date_added);
                    h.put("duration",duration);
                    h.put("size",size);
                    h.put("title",title);
                    h.put("track",track);
                    h.put("year",year);
                    h.put("bookmark",bookmark);
                    allSongsList.add(h);
                }
            }
        }

        cur.close();
        allSongs=allSongsList;
        return allSongsList;
    }



    public ArrayList<HashMap<String, String>> getListByKey(String key, String value){

        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        for(int itr =0;itr<allSongs.size();itr++){
            if (allSongs.get(itr).get(key).equals(value)){
                Log.d("matched",allSongs.get(itr).get(key));
                list.add(allSongs.get(itr));
            }
        }
        return list;
    }

    public ArrayList<HashMap<String, String>> getAlbumSongsList(String album){
        return getListByKey("album",album);
    }

    public ArrayList<HashMap<String, String>> getArtistSongsList(String artist_key){
        return getListByKey("artist_key",artist_key);
    }


    public ArrayList<HashMap<String, String>> getAlbumsList(){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        selection = MediaStore.Audio.Albums.NUMBER_OF_SONGS + "!= 0";
        sortOrder = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        if(contentResolver==null){ contentResolver = context.getContentResolver();}
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

    public ArrayList<HashMap<String, String>> getArtistsList(){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        selection = MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS + "!= 0";
        sortOrder = MediaStore.Audio.ArtistColumns.ARTIST + " ASC";
        Cursor cur = contentResolver.query(uri, null, selection, null, sortOrder);
        int count = 0;
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST));
                    String artist_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST_KEY));
                    String no_of_albums = cur.getString(cur.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS));
                    String no_of_tracks = cur.getString(cur.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS));

                    HashMap<String,String> h=new HashMap<>();
                    h.put("artist",artist);
                    h.put("artist_id",artist_id);
                    h.put("no_of_albums",no_of_albums);
                    h.put("no_of_tracks",no_of_tracks);
                    list.add(h);
                }
            }
        }
        cur.close();
        allArtists = list;
        return list;
    }


    public ArrayList<HashMap<String, String>> getGenresList(){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        selection = MediaStore.Audio.Albums.NUMBER_OF_SONGS + "!= 0";
      //  sortOrder = MediaStore.Audio.AlbumColumns.ALBUM + " ASC";
        Cursor cur = contentResolver.query(uri, null, selection, null, null);
        int count = 0;
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String genre = cur.getString(cur.getColumnIndex(MediaStore.Audio.GenresColumns.NAME));
                   // String genre = cur.getString(cur.getColumnIndex(MediaStore.Audio.Genres.Members.));
                    HashMap<String,String> h=new HashMap<>();
                    h.put("genre",genre);
                    list.add(h);
                }
            }
        }
        cur.close();
        return list;
    }

}
