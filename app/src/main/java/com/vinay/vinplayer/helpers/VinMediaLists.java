package com.vinay.vinplayer.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import com.vinay.vinplayer.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vinaysajjanapu on 31/1/17.
 */

public class VinMediaLists {

    Context context;
    private ContentResolver contentResolver;
    private String selection;
    private String sortOrder;
    private Uri uri;

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
                    h.put("audio_progress","0");
                    allSongsList.add(h);
                }
            }
        }

        cur.close();
        return allSongsList;
    }



    public ArrayList<HashMap<String, String>> getListByKey(String key, String value){

        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        for(int itr =0;itr<getAllSongsList().size();itr++){
            if (getAllSongsList().get(itr).get(key).equals(value)){
                list.add(getAllSongsList().get(itr));
            }
        }
        return list;
    }

    public ArrayList<HashMap<String, String>> getAlbumSongsList(String album_id){
        return getListByKey("album_id",album_id);
    }

    public ArrayList<HashMap<String, String>> getArtistSongsList(String artist_key){
        return getListByKey("artist_id",artist_key);
    }


    public ArrayList<HashMap<String, String>> getAlbumsList(){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        selection = MediaStore.Audio.Albums.NUMBER_OF_SONGS + "!= 0";
        sortOrder = MediaStore.Audio.AlbumColumns.ALBUM + " ASC";
        Cursor cur = contentResolver.query(uri, null, selection, null, sortOrder);
        int count = 0;
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM));
                    String album_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ID));
                    String no_of_songs = cur.getString(cur.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS));
                    String album_art = cur.getString(cur.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART));

                    HashMap<String,String> h=new HashMap<>();
                    h.put("album",album);
                    h.put("album_id",album_id);
                    h.put("no_of_songs",no_of_songs);
                    h.put("album_art",album_art);
                    list.add(h);
                }
            }
        }
        cur.close();
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


    public Bitmap getAlbumart(int album_id)
    {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getAllSongsList().get(album_id).get("data"));

        byte [] data = mmr.getEmbeddedPicture();
        if(data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        }else {
            return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }
    }
}
