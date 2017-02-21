package com.vinay.vinplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.vinay.vinplayer.VinPlayer;

import java.util.ArrayList;
import java.util.HashMap;

import static com.vinay.vinplayer.database.VinDBHelper.ALBUM;
import static com.vinay.vinplayer.database.VinDBHelper.ALBUM_ID;
import static com.vinay.vinplayer.database.VinDBHelper.ARTIST;
import static com.vinay.vinplayer.database.VinDBHelper.DATA;
import static com.vinay.vinplayer.database.VinDBHelper.SONG_ID;
import static com.vinay.vinplayer.database.VinDBHelper.TITLE;

/**
 * Created by vinaysajjanapu on 17/2/17.
 */

public class PlaylistTable {
    public Context context;
    private static VinDBHelper dbHelper = null;
    private static PlaylistTable mInstance;
    private SQLiteDatabase database;

    public static synchronized PlaylistTable getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PlaylistTable(context);
        }
        return mInstance;
    }

    public PlaylistTable(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((VinPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    private void closeCursor(Cursor mCursor) {
        if (mCursor!=null)mCursor.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////PLAYLIST


    public boolean addSongToPlaylist(HashMap<String,String> song, String playlistTable) {
        try {

            database = dbHelper.getDb();
            try {
                database.execSQL(dbHelper.playlistSQL(playlistTable));
            }catch (Exception e){
                e.printStackTrace();
            }
            database.beginTransaction();

            if (!isSongAlreadyPresentInPlaylist(song.get("id"),playlistTable)) {
                String sql = "Insert or Replace into " + playlistTable + " values(?,?,?,?,?,?);";
                SQLiteStatement insert = database.compileStatement(sql);

                try {
                    insert.clearBindings();
                    insert.bindLong(1, Long.parseLong(song.get("id")));
                    insert.bindLong(2, Long.parseLong(song.get("album_id")));
                    insert.bindString(3, song.get("title"));
                    insert.bindString(4, song.get("album"));
                    insert.bindString(5, song.get("artist"));
                    insert.bindString(6, song.get("data"));
                    insert.execute();

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }else {
                Log.d("VinDB","song present in playlist");
                return false;
            }
            database.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e("XML:", e.toString());
            return false;
        } finally {
            database.endTransaction();
        }
        return true;
    }

    public boolean removeSongFromPlaylist(HashMap<String,String> song, String playlistTable) {
        try {
            if (isSongAlreadyPresentInPlaylist(song.get("id"),playlistTable)) {
                database = dbHelper.getDb();
                String sql = "DELETE FROM " + playlistTable + " WHERE " + SONG_ID + " = " + song.get("id") + ";";
                database.execSQL(sql);
            }
        } catch (Exception e) {
            Log.e("XML:", e.toString());
            return false;
        } finally {
            database.close();
        }
        return true;
    }

    public ArrayList<HashMap<String,String>> getPlaylistList(String playlistTable){
        ArrayList<HashMap<String,String>> Playlists = new ArrayList<>();
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + playlistTable + " ORDER BY "+ TITLE + " ASC;";
            database = dbHelper.getDb();
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    HashMap<String,String> song = new HashMap<>();
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(ALBUM_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(ALBUM));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST));
                    String data = cursor.getString(cursor.getColumnIndexOrThrow(DATA));

                    song.put("id",id+"");
                    song.put("album_id",album_id+"");
                    song.put("title",title);
                    song.put("album",album);
                    song.put("artist",artist);
                    song.put("data",data);

                    Playlists.add(song);
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }
        return Playlists;
    }

    private boolean isSongAlreadyPresentInPlaylist(String id, String playlistTable) {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + playlistTable + " where " + SONG_ID + "=" + id ;
            database = dbHelper.getDb();
            mCursor = database.rawQuery(sqlQuery, null);
            if (mCursor != null && mCursor.getCount() >= 1) {
                closeCursor(mCursor);
                return true;
            }
        } catch (Exception e) {
            closeCursor(mCursor);
            e.printStackTrace();
        }
        return false;
    }





}
