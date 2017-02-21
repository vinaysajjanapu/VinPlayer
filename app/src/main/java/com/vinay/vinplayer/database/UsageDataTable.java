package com.vinay.vinplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.helpers.VinMedia;

import java.util.ArrayList;
import java.util.HashMap;

import static com.vinay.vinplayer.database.VinDBHelper.ALBUM;
import static com.vinay.vinplayer.database.VinDBHelper.ALBUM_ID;
import static com.vinay.vinplayer.database.VinDBHelper.ARTIST;
import static com.vinay.vinplayer.database.VinDBHelper.AS_FIRST;
import static com.vinay.vinplayer.database.VinDBHelper.AS_LAST;
import static com.vinay.vinplayer.database.VinDBHelper.DATA;
import static com.vinay.vinplayer.database.VinDBHelper.NO_TIMES_PLAYED;
import static com.vinay.vinplayer.database.VinDBHelper.PLAYED_PERCENT;
import static com.vinay.vinplayer.database.VinDBHelper.SONG_ID;
import static com.vinay.vinplayer.database.VinDBHelper.TITLE;
import static com.vinay.vinplayer.database.VinDBHelper.WITHOUT_HEADP;
import static com.vinay.vinplayer.database.VinDBHelper.WITH_HEADP;
import static com.vinay.vinplayer.database.VinDBHelper.usageDataTable;

/**
 * Created by vinaysajjanapu on 17/2/17.
 */

public class UsageDataTable {

    public Context context;
    private static VinDBHelper dbHelper = null;
    private static UsageDataTable mInstance;
    private SQLiteDatabase database;

    public static synchronized UsageDataTable getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UsageDataTable(context);
        }
        return mInstance;
    }

    public UsageDataTable(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((VinPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    private void closeCursor(Cursor mCursor) {
        if (mCursor!=null)mCursor.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////USAGE DATA

    public boolean updateUsageData(HashMap<String,String> song, int timeOfPlay, int percentPlayed,
                                   boolean asFirst, boolean asLast, boolean isHeadP_plugged){

        boolean crossedThreshold = (percentPlayed >= VinMedia.playPercentThreshold);
        try {
            database = dbHelper.getDb();
            database.beginTransaction();

            if (! isPresentInRecord(song.get("id"))) {
                String sql = "Insert or Replace into " + usageDataTable + " values(?,?,?,?,?,?," +
                        "?,?,?,?,?,?," +
                        "?,?,?,?,?,?);";
                SQLiteStatement insert = database.compileStatement(sql);

                try {
                    insert.clearBindings();
                    insert.bindLong(1, Long.parseLong(song.get("id")));
                    insert.bindLong(2, Long.parseLong(song.get("album_id")));
                    insert.bindString(3, song.get("title"));
                    insert.bindString(4, song.get("album"));
                    insert.bindString(5, song.get("artist"));
                    insert.bindString(6, song.get("data"));

                    for (int i=1;i<=6;i++)insert.bindLong(6+i,0);

                    if (crossedThreshold)insert.bindLong(6+timeOfPlay,1);

                    insert.bindLong(13, percentPlayed);

                    if (asFirst && crossedThreshold)insert.bindLong(14,1);
                    else insert.bindLong(14,0);

                    if (asLast && crossedThreshold)insert.bindLong(15,1);
                    else insert.bindLong(15,0);

                    if (crossedThreshold) {
                        insert.bindLong(16,1);
                        if (isHeadP_plugged) {
                            insert.bindLong(17, 1);
                            insert.bindLong(18, 0);
                        } else {
                            insert.bindLong(17, 0);
                            insert.bindLong(18, 1);
                        }
                    }else {
                        insert.bindLong(16,0);
                        insert.bindLong(17, 0);
                        insert.bindLong(18, 0);
                    }
                    insert.execute();

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                database.setTransactionSuccessful();
            }else {
                String mainSql = "UPDATE "+ usageDataTable +" SET " ;
                String update = "";
                String finalSql = " WHERE " + SONG_ID + " = " + song.get("id");

                if (crossedThreshold) {
                    update = update + "time" + timeOfPlay + " = " + "time" + timeOfPlay +" + "+ "1 ,";
                    update = update + PLAYED_PERCENT + " = " + PLAYED_PERCENT + " + " + percentPlayed + ", " ;
                    if (asFirst)update = update + AS_FIRST + " = " + AS_FIRST + " + " + "1, ";
                    if (asLast)update = update + AS_LAST + " = " + AS_LAST + " + " + "1, ";
                    update = update + NO_TIMES_PLAYED + " = " + NO_TIMES_PLAYED + " + " + "1 ";
                    if (isHeadP_plugged)
                        update = update + ", " + WITH_HEADP + " = " + WITH_HEADP + " + " + "1 ";
                    else
                        update = update + ", " + WITHOUT_HEADP + " = " + WITHOUT_HEADP + " + " + "1 ";

                    try {
                        database.execSQL(mainSql+update+finalSql);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                database.setTransactionSuccessful();
            }

        } catch (Exception e) {
            Log.e("XML:", e.toString());
            return false;
        } finally {
            database.endTransaction();
        }
        return true;
    }

    public ArrayList<HashMap<String,String>> getMostPlayedList(){
        return getUsageData(NO_TIMES_PLAYED);
    }
    public ArrayList<HashMap<String,String>> getMostPlayedAtThisTimeList(){
        return getUsageData("time"+dbHelper.getCurrentTimeDiv());
    }
    public ArrayList<HashMap<String,String>> getHeadPhoneStateList(){
        if (VinMedia.getInstance().isHeadPhonePlugged())
            return getUsageData(WITH_HEADP);
        else
            return getUsageData(WITHOUT_HEADP);
    }

    private ArrayList<HashMap<String,String>> getUsageData(String sortByColumn){
        ArrayList<HashMap<String,String>> usageList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + usageDataTable + " ORDER BY "+ sortByColumn + " DESC;";
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
                    usageList.add(song);
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }
        return usageList;
    }

    private boolean isPresentInRecord(String id) {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + usageDataTable + " where " + SONG_ID + "=" + id ;
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
