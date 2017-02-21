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
import static com.vinay.vinplayer.database.VinDBHelper.LEFT_POSITION;
import static com.vinay.vinplayer.database.VinDBHelper.SONG_ID;
import static com.vinay.vinplayer.database.VinDBHelper.TITLE;
import static com.vinay.vinplayer.database.VinDBHelper.lastPlayTable;

/**
 * Created by vinaysajjanapu on 17/2/17.
 */

public class LastPlayTable {

    public Context context;
    private static VinDBHelper dbHelper = null;
    private static LastPlayTable mInstance;
    private SQLiteDatabase database;

    public static synchronized LastPlayTable getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LastPlayTable(context);
        }
        return mInstance;
    }

    public LastPlayTable(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((VinPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    private void closeCursor(Cursor mCursor) {
        if (mCursor!=null)mCursor.close();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////LASTPLAY DATA

    public boolean storeLastPlayQueue(ArrayList<HashMap<String,String>> queue, int position){
        try {

            database = dbHelper.getDb();
            database.beginTransaction();
            String delete  = "DELETE FROM "+ lastPlayTable + ";";
            try {
                database.execSQL(delete);
            }catch (Exception e){
                e.printStackTrace();
            }
            for (HashMap<String,String> song : queue) {
                String sql = "Insert or Replace into " + lastPlayTable + " values(?,?,?,?,?,?,?);";
                SQLiteStatement insert = database.compileStatement(sql);

                try {
                    insert.clearBindings();
                    insert.bindLong(1, Long.parseLong(song.get("id")));
                    insert.bindLong(2, Long.parseLong(song.get("album_id")));
                    insert.bindString(3, song.get("title"));
                    insert.bindString(4, song.get("album"));
                    insert.bindString(5, song.get("artist"));
                    insert.bindString(6, song.get("data"));
                    insert.bindLong(7, position);
                    insert.execute();

                } catch (Exception e) {
                    e.printStackTrace();
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

    public ArrayList<HashMap<String,String>> getLastPlayQueue(){
        ArrayList<HashMap<String,String>> lastPlayQueue = new ArrayList<>();
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + lastPlayTable;
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
                    String position = cursor.getString(cursor.getColumnIndexOrThrow(LEFT_POSITION));

                    song.put("id",id+"");
                    song.put("album_id",album_id+"");
                    song.put("title",title);
                    song.put("album",album);
                    song.put("artist",artist);
                    song.put("data",data);
                    song.put("position",position);
                    lastPlayQueue.add(song);
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }
        return lastPlayQueue;
    }





}
