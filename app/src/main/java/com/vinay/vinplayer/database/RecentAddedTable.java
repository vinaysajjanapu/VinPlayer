package com.vinay.vinplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static com.vinay.vinplayer.database.VinDBHelper.ALBUM;
import static com.vinay.vinplayer.database.VinDBHelper.ALBUM_ID;
import static com.vinay.vinplayer.database.VinDBHelper.ARTIST;
import static com.vinay.vinplayer.database.VinDBHelper.DATA;
import static com.vinay.vinplayer.database.VinDBHelper.DATE_ADDED;
import static com.vinay.vinplayer.database.VinDBHelper.SONG_ID;
import static com.vinay.vinplayer.database.VinDBHelper.TITLE;
import static com.vinay.vinplayer.database.VinDBHelper.recentAddedTable;

/**
 * Created by vinaysajjanapu on 17/2/17.
 */

public class RecentAddedTable {
    public Context context;
    private static VinDBHelper dbHelper = null;
    private static RecentAddedTable mInstance;
    private SQLiteDatabase database;

    public static synchronized RecentAddedTable getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RecentAddedTable(context);
        }
        return mInstance;
    }

    public RecentAddedTable(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((VinPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    private void closeCursor(Cursor mCursor) {
        if (mCursor!=null)mCursor.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////RECENTADDED

    public boolean updateRecentAddedList() {
        try {
            database = dbHelper.getDb();
            database.beginTransaction();
            for (HashMap<String,String> song : VinMediaLists.allSongs) {
                if (!isSongPresentInRecentlyAdded(song.get("id"))) {
                    String sql = "Insert or Replace into " + recentAddedTable + " values(?,?,?,?,?,?,?);";
                    SQLiteStatement insert = database.compileStatement(sql);

                    try {
                        insert.clearBindings();
                        insert.bindLong(1, Long.parseLong(song.get("id")));
                        insert.bindLong(2, Long.parseLong(song.get("album_id")));
                        insert.bindString(3, song.get("title"));
                        insert.bindString(4, song.get("album"));
                        insert.bindString(5, song.get("artist"));
                        insert.bindString(6, song.get("data"));
                        insert.bindLong(7, Long.parseLong(song.get("date_added"))/86400 );
                        insert.execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
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

    ///////////////////give total number less than 0 for full list;
    public ArrayList<HashMap<String,String>> getRecentAddedListList(int list_size){
        ArrayList<HashMap<String,String>> RecentAddedLists = new ArrayList<>();
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + recentAddedTable + " ORDER BY " + DATE_ADDED + " DESC" ;
            if (list_size>0)sqlQuery = sqlQuery + " LIMIT "+ list_size;
            sqlQuery += ";";
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

                    RecentAddedLists.add(song);
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }
        return RecentAddedLists;
    }

    private boolean isSongPresentInRecentlyAdded(String id) {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + recentAddedTable + " where " + SONG_ID + "=" + id ;
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
