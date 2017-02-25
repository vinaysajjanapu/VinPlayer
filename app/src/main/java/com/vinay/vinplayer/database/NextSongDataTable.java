package com.vinay.vinplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.HashMap;
import static com.vinay.vinplayer.database.VinDBHelper.PREF1;
import static com.vinay.vinplayer.database.VinDBHelper.SONG_ID;
import static com.vinay.vinplayer.database.VinDBHelper.nextSongDataTable;

/**
 * Created by vinaysajjanapu on 17/2/17.
 */

public class NextSongDataTable {
    public Context context;
    private static VinDBHelper dbHelper = null;
    private static NextSongDataTable mInstance;
    private SQLiteDatabase database;

    public static synchronized NextSongDataTable getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NextSongDataTable(context);
        }
        return mInstance;
    }

    public NextSongDataTable(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((VinPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    private void closeCursor(Cursor mCursor) {
        if (mCursor!=null)mCursor.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////NEXTSONG DATA

    public boolean initializeNextSongData() {
        try {
            database = dbHelper.getDb();
            database.execSQL(new VinDBHelper(context).nextSongTableSQL(VinMediaLists.allSongs.size()));
            database.beginTransaction();
            String sql2 = "";

            for (int k=1; k<=VinMediaLists.allSongs.size();k++){
                sql2 += ", 0 " ;
            }
            sql2 += ");";

            for (int j=1;j<=VinMediaLists.allSongs.size();j++) {
                String sql1 = "INSERT INTO " + nextSongDataTable + " VALUES (" + (j)+", "+
                        VinMediaLists.allSongs.get(j-1).get("id") + ", 0 ";
                try {
                    database.execSQL(sql1+sql2);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
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


    private int numColumnsNextSongTable(){
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + nextSongDataTable +";";
            database = dbHelper.getDb();
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                return cursor.getColumnCount();
            }
            closeCursor(cursor);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public void checkNextSongTableCompatibility(){
        int newSize = VinMediaLists.allSongs.size();
        if (newSize > numColumnsNextSongTable()){
            updateColumnsNextSongData(newSize);
            updateRowsNextSongData();
        }
    }

    private void updateRowsNextSongData() {
        int z = oldsize;
        for (int i=0; i<VinMediaLists.allSongs.size();i++){
            if (!isAlreadyPresent(Long.parseLong(VinMediaLists.allSongs.get(i).get("id")))){
                try {
                    database = dbHelper.getDb();
                    database.beginTransaction();
                    String sql2 = "";

                    for (int k=1; k<=VinMediaLists.allSongs.size();k++){
                        sql2 += ", 0 " ;
                    }
                    sql2 += ");";

                        String sql1 = "INSERT INTO " + nextSongDataTable + " VALUES (" + (++z)+", "+
                                VinMediaLists.allSongs.get(i).get("id") + ", 0 ";
                        try {
                            database.execSQL(sql1+sql2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    database.setTransactionSuccessful();

                } catch (Exception e) {
                    Log.e("XML:", e.toString());
                } finally {
                    database.endTransaction();
                }
            }
        }
    }

    private boolean isAlreadyPresent(long id) {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + nextSongDataTable + " where " + SONG_ID + "=" + id ;
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

    private static int oldsize = 0;
    private void updateColumnsNextSongData(int newSize){
        oldsize = numColumnsNextSongTable();
        database = dbHelper.getDb();
        for (int m = oldsize+1; m <= newSize; m++  ){
            String addcolumn = "ALTER TABLE "+ nextSongDataTable +
                    " ADD COLUMN " + "col"+ m + " INTEGER NOT NULL DEFAULT 0;" ;
            try {
                database.execSQL(addcolumn);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    public boolean addNextSongData(Long from_id,Long to_id) {
        try {
            String mainSql = "UPDATE "+ nextSongDataTable +" SET " ;
            String update = "";
            String finalSql = " WHERE " + SONG_ID + " = " + from_id + ";";
            long index = getIndexfromID(to_id);
            if (index!=-1) {
                update = update + "col" + (index) + " = " + "col" + (index)  + " + " + "1 ";
                try {
                    database.execSQL(mainSql + update + finalSql);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e("XML:", e.toString());
            return false;
        }
        return true;
    }

    private Long getIndexfromID(Long to_id) {
        long index = -1;
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + nextSongDataTable + " WHERE " + SONG_ID + " = " + to_id + ";";
            database = dbHelper.getDb();
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor.moveToFirst()) {
                index = cursor.getLong(cursor.getColumnIndexOrThrow("idx"));
            }
            closeCursor(cursor);
        }catch (Exception e){
            e.printStackTrace();
        }
        return index;
    }

    private Long getIDfromIndex(Long idx,SQLiteDatabase database) {
        long id = 0;
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + nextSongDataTable + " WHERE " + " idx " + " = " + idx + ";";
             cursor = database.rawQuery(sqlQuery, null);
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
            }
            closeCursor(cursor);
        }catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }

    public void updatePrefsInTable(){
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + nextSongDataTable + " ORDER BY idx ASC;" ;
            database = dbHelper.getDb();
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    HashMap<String,String> song = new HashMap<>();
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long pref = cursor.getPosition();
                    for(int mx = 1; mx <= VinMediaLists.allSongs.size(); mx++){
                        if (cursor.getLong(cursor.getColumnIndexOrThrow("col"+mx))>pref){
                            pref = cursor.getLong(cursor.getColumnIndexOrThrow("col"+mx));
                        }
                    }

                    String sql = "UPDATE " + nextSongDataTable + " SET ";
                    sql += PREF1 + " = " + getIDfromIndex(pref,database) + " WHERE "+ SONG_ID + " = " + id + ";";
                    try {
                        database.execSQL(sql);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }
    }

    public Long getPreferredId(Long id){
        long id1 = 0;
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + nextSongDataTable + " WHERE " + SONG_ID + " = " + id + ";";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(PREF1));
            }
            closeCursor(cursor);
        }catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }


}
