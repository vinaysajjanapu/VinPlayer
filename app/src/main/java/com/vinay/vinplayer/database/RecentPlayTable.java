package com.vinay.vinplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.vinay.vinplayer.VinPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.vinay.vinplayer.database.VinDBHelper.ALBUM;
import static com.vinay.vinplayer.database.VinDBHelper.ALBUM_ID;
import static com.vinay.vinplayer.database.VinDBHelper.ARTIST;
import static com.vinay.vinplayer.database.VinDBHelper.DATA;
import static com.vinay.vinplayer.database.VinDBHelper.SONG_ID;
import static com.vinay.vinplayer.database.VinDBHelper.TITLE;
import static com.vinay.vinplayer.database.VinDBHelper.recentPlayTable;

/**
 * Created by vinaysajjanapu on 17/2/17.
 */

public class RecentPlayTable {

    public Context context;
    private static VinDBHelper dbHelper = null;
    private static RecentPlayTable mInstance;
    private SQLiteDatabase database;

    public static synchronized RecentPlayTable getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RecentPlayTable(context);
        }
        return mInstance;
    }

    public RecentPlayTable(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((VinPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    private void closeCursor(Cursor mCursor) {
        if (mCursor!=null)mCursor.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////RECENTPLAY

    public boolean addSongToRecentPlayList(HashMap<String,String> song) {
        try {
            database = dbHelper.getDb();
            database.beginTransaction();

            if (isSongAlreadyRecentPlayList(song.get("id"))) {
                String deleteSql = "DELETE FROM " + recentPlayTable + " WHERE "+ SONG_ID +" = " + song.get("id")+";";
                database.execSQL(deleteSql);
            }
            String sql = "Insert into " + recentPlayTable + " values(?,?,?,?,?,?,?);";
            SQLiteStatement insert = database.compileStatement(sql);

            try {
                insert.clearBindings();
                insert.bindLong(1,1);
                insert.bindLong(2, Long.parseLong(song.get("id")));
                insert.bindLong(3, Long.parseLong(song.get("album_id")));
                insert.bindString(4, song.get("title"));
                insert.bindString(5, song.get("album"));
                insert.bindString(6, song.get("artist"));
                insert.bindString(7, song.get("data"));
                insert.execute();

              } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            database.setTransactionSuccessful();
            database.endTransaction();

            database.beginTransaction();
            try {
                String increment_index = "update "+ recentPlayTable + " set idx = idx + 1 where idx >= 1";
                database.execSQL(increment_index);
            }catch (Exception e) {
                e.printStackTrace();
            }database.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e("XML:", e.toString());
            return false;
        } finally {
            database.endTransaction();
        }
        return true;
    }

    public ArrayList<HashMap<String,String>> getRecentPlayList(){
        ArrayList<HashMap<String,String>> RecentPlayLists = new ArrayList<>();
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + recentPlayTable + " ORDER BY idx DESC" ;
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

                    RecentPlayLists.add(song);
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }
        return RecentPlayLists;
    }

    private boolean isSongAlreadyRecentPlayList(String id) {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + recentPlayTable + " where " + SONG_ID + " = " + id ;
            database = dbHelper.getDb();
            mCursor = database.rawQuery(sqlQuery, null);
            if (mCursor != null && mCursor.getCount() >= 1) {
                closeCursor(mCursor);
                return true;
            }else
                return false;
        } catch (Exception e) {
            closeCursor(mCursor);
            e.printStackTrace();
            return false;
        }
    }




}
