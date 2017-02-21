package com.vinay.vinplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.helpers.VinMedia;
import com.vinay.vinplayer.helpers.VinMediaLists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static com.vinay.vinplayer.database.VinDBHelper.ALBUM;
import static com.vinay.vinplayer.database.VinDBHelper.ALBUM_ID;
import static com.vinay.vinplayer.database.VinDBHelper.ALL_INDEX;
import static com.vinay.vinplayer.database.VinDBHelper.ARTIST;
import static com.vinay.vinplayer.database.VinDBHelper.AS_FIRST;
import static com.vinay.vinplayer.database.VinDBHelper.AS_LAST;
import static com.vinay.vinplayer.database.VinDBHelper.DATA;
import static com.vinay.vinplayer.database.VinDBHelper.DATE_ADDED;
import static com.vinay.vinplayer.database.VinDBHelper.HEADP_INDEX;
import static com.vinay.vinplayer.database.VinDBHelper.IS_FAVOURITE;
import static com.vinay.vinplayer.database.VinDBHelper.NEXT_PREFERRED_ID;
import static com.vinay.vinplayer.database.VinDBHelper.NO_TIMES_PLAYED;
import static com.vinay.vinplayer.database.VinDBHelper.PLAYED_PERCENT;
import static com.vinay.vinplayer.database.VinDBHelper.RECENT_ADDED_INDEX;
import static com.vinay.vinplayer.database.VinDBHelper.RECENT_PLAY_INDEX;
import static com.vinay.vinplayer.database.VinDBHelper.SCORE;
import static com.vinay.vinplayer.database.VinDBHelper.SONG_ID;
import static com.vinay.vinplayer.database.VinDBHelper.TIME_INDEX;
import static com.vinay.vinplayer.database.VinDBHelper.TITLE;
import static com.vinay.vinplayer.database.VinDBHelper.WITHOUT_HEADP;
import static com.vinay.vinplayer.database.VinDBHelper.WITH_HEADP;
import static com.vinay.vinplayer.database.VinDBHelper.favouriteTable;
import static com.vinay.vinplayer.database.VinDBHelper.recentAddedTable;
import static com.vinay.vinplayer.database.VinDBHelper.recentPlayTable;
import static com.vinay.vinplayer.database.VinDBHelper.scoreTable;
import static com.vinay.vinplayer.database.VinDBHelper.usageDataTable;

/**
 * Created by vinaysajjanapu on 21-Feb-17.
 */

public class RecommendedTable {

    public long THIS_TIME_WEIGHT = 2;
    public long TOTAL_TIMES_WEIGHT = 2;
    public long HEADP_WEIGHT = 1;
    public long RECENT_PLAY_WEIGHT = 1;
    public long RECENT_ADDED_WEIGHT = 1;
    public long FAVOURITE_WEIGHT = 1;
    public long AS_FIRST_WEIGHT = 1;
    public long AS_LAST_WEIGHT = 1;
    public long PLAYED_PERCENT_WEIGHT = 1;

    int NEXT_SONG_ITERATIONS = 5;


    private static VinDBHelper dbHelper = null;
    private static RecommendedTable mInstance;
    private SQLiteDatabase database;
    private String crrentTimeDiv;

    long id,album_id,no_of_times,hp_times,this_time_times,score;
    String title,album,artist,data;
    Cursor cursor = null;

    public static synchronized RecommendedTable getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RecommendedTable(context);
        }
        return mInstance;
    }

    public Context context;

    public RecommendedTable(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((VinPlayer) context.getApplicationContext()).DB_HELPER;
        }
    }

    private void closeCursor(Cursor mCursor) {
        if (mCursor!=null)mCursor.close();
    }

    public void createRecommendationsList() {

        String droptable = "DROP TABLE IF EXISTS " + scoreTable + ";";

        database = dbHelper.getDb();
        try{
            database.execSQL(droptable);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            database.execSQL(createScoreTableQuery());
        }catch (Exception e){
            e.printStackTrace();
        }
        total_index_update(database);
        headp_index_update(database);
        percentPlayed_index_update(database);
        time_index_update(database);
        as_first_index_update(database);
        as_last_index_update(database);
        recentplay_index_update(database);
        recentaddedIndex_update(database);
        favouriteupdate(database);
        updateScore(database);

    }

    private void total_index_update(SQLiteDatabase database){
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + usageDataTable + " ORDER BY " + NO_TIMES_PLAYED + " DESC;";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    album_id = cursor.getLong(cursor.getColumnIndexOrThrow(ALBUM_ID));
                    title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                    album = cursor.getString(cursor.getColumnIndexOrThrow(ALBUM));
                    artist = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST));
                    data = cursor.getString(cursor.getColumnIndexOrThrow(DATA));

                    String sql = "Insert or Replace into " + scoreTable + " values(?,?,?,?," +
                            "?,?,?,?," +
                            "?,?,?,?," +
                            "?,?,?,?,?);";
                    SQLiteStatement insert = database.compileStatement(sql);
                    database.beginTransaction();
                    try {
                        insert.clearBindings();
                        insert.bindLong(1, id);
                        insert.bindLong(2, album_id);
                        insert.bindString(3, title);
                        insert.bindString(4, album);
                        insert.bindString(5, artist);
                        insert.bindString(6, data);
                        insert.bindLong(7,0);
                        insert.bindLong(8,0);
                        insert.bindLong(9,cursor.getPosition());
                        insert.bindLong(10,0);
                        insert.bindLong(11,0);
                        insert.bindLong(12,0);
                        insert.bindLong(13,0);
                        insert.bindLong(14,0);
                        insert.bindLong(15,0);
                        insert.bindLong(16,0);
                        insert.bindLong(17,0);
                        insert.execute();
                    }catch (Exception e){
                        e.printStackTrace();
                    }database.setTransactionSuccessful();
                    database.endTransaction();
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }


    }

    private void headp_index_update(SQLiteDatabase database){
        String head;
        if (VinMedia.getInstance().isHeadPhonePlugged())head = WITH_HEADP;
        else head = WITHOUT_HEADP;
        try {
            String sqlQuery = "Select * from " + usageDataTable + " ORDER BY " + head + " DESC;";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long no = cursor.getLong(cursor.getColumnIndexOrThrow(head));
                    if (no!=0) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += head + " = " + cursor.getPosition() + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void time_index_update(SQLiteDatabase database){
        try {
            int t = getCrrentTimeDiv();
            String sqlQuery = "Select * from " + usageDataTable + " ORDER BY " + "time" + t + " DESC;";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long no = cursor.getLong(cursor.getColumnIndexOrThrow("time" + t));
                    if (no!=0) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += TIME_INDEX + " = " + cursor.getPosition() + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void percentPlayed_index_update(SQLiteDatabase database){
        try {
            String sqlQuery = "Select * from " + usageDataTable;
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long as_f = cursor.getLong(cursor.getColumnIndexOrThrow(PLAYED_PERCENT));
                    long num = cursor.getLong(cursor.getColumnIndexOrThrow(NO_TIMES_PLAYED));

                    long percent = as_f / num ;
                    if (as_f!=0) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += PLAYED_PERCENT + " = " + percent + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String sqlQuery = "Select * from " + scoreTable + " ORDER BY " + PLAYED_PERCENT + " DESC";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long as_f = cursor.getLong(cursor.getColumnIndexOrThrow(PLAYED_PERCENT));

                    if (as_f!=0) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += PLAYED_PERCENT + " = " + cursor.getPosition() + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void as_first_index_update(SQLiteDatabase database){
        try {
            String sqlQuery = "Select * from " + usageDataTable + " ORDER BY " + AS_FIRST + " DESC;";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long as_f = cursor.getLong(cursor.getColumnIndexOrThrow(AS_FIRST));
                    if (as_f!=0) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += AS_FIRST + " = " + cursor.getPosition() + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void as_last_index_update(SQLiteDatabase database){
        try {
            String sqlQuery = "Select * from " + usageDataTable + " ORDER BY " + AS_LAST + " DESC;";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    long as_l = cursor.getLong(cursor.getColumnIndexOrThrow(AS_FIRST));
                    if (as_l!=0) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += AS_LAST + " = " + cursor.getPosition() + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recentplay_index_update(SQLiteDatabase database){

        try {
            String sqlQuery = "Select * from " + recentPlayTable + " ORDER BY idx DESC;";
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    if(isPresentAlready(id)) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += RECENT_PLAY_INDEX + " = " + cursor.getPosition() + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {

                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                        long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(ALBUM_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(ALBUM));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST));
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(DATA));

                        String sql = "Insert or Replace into " + scoreTable + " values(?,?,?,?," +
                                "?,?,?,?," +
                                "?,?,?,?," +
                                "?,?,?,?,?);";
                        SQLiteStatement insert = database.compileStatement(sql);
                        database.beginTransaction();
                        try {
                            insert.clearBindings();
                            insert.bindLong(1, id);
                            insert.bindLong(2, album_id);
                            insert.bindString(3, title);
                            insert.bindString(4, album);
                            insert.bindString(5, artist);
                            insert.bindString(6, data);
                            insert.bindLong(7, 0);
                            insert.bindLong(8, 0);
                            insert.bindLong(9, 0);
                            insert.bindLong(10, cursor.getPosition());
                            insert.bindLong(11, 0);
                            insert.bindLong(12, 0);
                            insert.bindLong(13, 0);
                            insert.bindLong(14, 0);
                            insert.bindLong(15, 0);
                            insert.bindLong(16, 0);
                            insert.bindLong(17, 0);
                            insert.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recentaddedIndex_update(SQLiteDatabase database){
        try {
            String sqlQuery = "Select * from " + recentAddedTable + " ORDER BY " + DATE_ADDED + " DESC "+
                    "LIMIT "+ VinMediaLists.allSongs.size()/4;
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    if(isPresentAlready(id)) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += RECENT_ADDED_INDEX + " = " + cursor.getPosition() + " WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {

                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                        long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(ALBUM_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(ALBUM));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST));
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(DATA));

                        String sql = "Insert or Replace into " + scoreTable + " values(?,?,?,?," +
                                "?,?,?,?," +
                                "?,?,?,?," +
                                "?,?,?,?,?);";
                        SQLiteStatement insert = database.compileStatement(sql);
                        database.beginTransaction();
                        try {
                            insert.clearBindings();
                            insert.bindLong(1, id);
                            insert.bindLong(2, album_id);
                            insert.bindString(3, title);
                            insert.bindString(4, album);
                            insert.bindString(5, artist);
                            insert.bindString(6, data);
                            insert.bindLong(7, 0);
                            insert.bindLong(8, 0);
                            insert.bindLong(9, 0);
                            insert.bindLong(10, 0);
                            insert.bindLong(11, cursor.getPosition());
                            insert.bindLong(12, 0);
                            insert.bindLong(13, 0);
                            insert.bindLong(14, 0);
                            insert.bindLong(15, 0);
                            insert.bindLong(16, 0);
                            insert.bindLong(17, 0);
                            insert.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void favouriteupdate(SQLiteDatabase database){
        try {
            String sqlQuery = "Select * from " + favouriteTable ;
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                    if(isPresentAlready(id)) {
                        String allsql = "UPDATE " + scoreTable + " SET ";
                        allsql += IS_FAVOURITE + " = 1 WHERE " + SONG_ID + " = " + id + " ;";
                        try {
                            database.execSQL(allsql);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID));
                        long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(ALBUM_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(ALBUM));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST));
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(DATA));

                        String sql = "Insert or Replace into " + scoreTable + " values(?,?,?,?," +
                                "?,?,?,?," +
                                "?,?,?,?," +
                                "?,?,?,?,?);";
                        SQLiteStatement insert = database.compileStatement(sql);
                        database.beginTransaction();
                        try {
                            insert.clearBindings();
                            insert.bindLong(1, id);
                            insert.bindLong(2, album_id);
                            insert.bindString(3, title);
                            insert.bindString(4, album);
                            insert.bindString(5, artist);
                            insert.bindString(6, data);
                            insert.bindLong(7, 0);
                            insert.bindLong(8, 0);
                            insert.bindLong(9, 0);
                            insert.bindLong(10, 0);
                            insert.bindLong(11, cursor.getPosition());
                            insert.bindLong(12, 0);
                            insert.bindLong(13, 0);
                            insert.bindLong(14, 0);
                            insert.bindLong(15, 0);
                            insert.bindLong(16, 0);
                            insert.bindLong(17, 0);
                            insert.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateScore(SQLiteDatabase database){

        try {
            String sqlQuery = "Select * from " + scoreTable ;
            cursor = database.rawQuery(sqlQuery, null);
            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    String allsql = "UPDATE " + scoreTable + " SET ";
                    allsql += SCORE + " = " + THIS_TIME_WEIGHT + " * " + TIME_INDEX +
                            " + " + HEADP_WEIGHT + " * " + HEADP_INDEX +
                            " + " + TOTAL_TIMES_WEIGHT + " * " + ALL_INDEX +
                            " + " + RECENT_PLAY_WEIGHT + " * " + RECENT_PLAY_INDEX +
                            " + " + RECENT_ADDED_WEIGHT + "* " + RECENT_ADDED_INDEX +
                            " + " + FAVOURITE_WEIGHT + " * " + IS_FAVOURITE +
                            " + " + AS_FIRST_WEIGHT + " * " + AS_FIRST +
                            " + " + AS_LAST_WEIGHT + " * " + AS_LAST +
                            " + " + PLAYED_PERCENT_WEIGHT + " * " + PLAYED_PERCENT +
                            ";";
                    try {
                        database.execSQL(allsql);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getCrrentTimeDiv() {
        Date d = new Date();
        long h = (d.getTime() / 1000 / 60 / 60) % 24;
        return (int)h/4;
    }

    private boolean isPresentAlready(long id) {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + scoreTable + " where " + SONG_ID + "=" + id ;
            database = dbHelper.getDb();
            mCursor = database.rawQuery(sqlQuery, null);
            if (mCursor != null && mCursor.getCount() >= 1) {
                closeCursor(mCursor);
                return true;
            }else return false;
        } catch (Exception e) {
            closeCursor(mCursor);
            e.printStackTrace();
        }
        return false;
    }

    private String createScoreTableQuery() {
        String sql = "CREATE TABLE IF NOT EXISTS " + scoreTable + " (" +
                SONG_ID + " INTEGER NOT NULL PRIMARY KEY,"+ //1
                ALBUM_ID + " INTEGER NOT NULL," + //2
                TITLE + " TEXT NOT NULL," +         //3
                ALBUM + " TEXT NOT NULL," +             //4
                ARTIST + " TEXT NOT NULL," +            //5
                DATA + " TEXT NOT NULL, " +             //6
                TIME_INDEX + " INTEGER NOT NULL DEFAULT 0, " +        //7
                HEADP_INDEX + " INTEGER NOT NULL DEFAULT 0, " +       //8
                ALL_INDEX + " INTEGER NOT NULL DEFAULT 0, " +         //9
                RECENT_PLAY_INDEX+" INTEGER NOT NULL DEFAULT 0, " +   //10
                RECENT_ADDED_INDEX+ " INTEGER NOT NULL DEFAULT 0, " +     //11
                IS_FAVOURITE + " INTEGER NOT NULL DEFAULT 0, "+     //12
                NEXT_PREFERRED_ID + " INTEGER NOT NULL DEFAULT 0, "+       //13
                SCORE + " INTEGER NOT NULL DEFAULT 0, "+   //14
                AS_FIRST + " INTEGER NOT NULL DEFAULT 0, "+ //15
                AS_LAST + " INTEGER NOT NULL DEFAULT 0, "+  //16
                PLAYED_PERCENT + " INTEGER NOT NULL DEFAULT 0 );";  //17
        return sql;
    }

    public ArrayList<HashMap<String,String>> getRecommendedList(){
        ArrayList<HashMap<String,String>> recommendedList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String sqlQuery = "Select * from " + scoreTable + " ORDER BY SCORE DESC" ;
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
                    recommendedList.add(song);
                }
            }
            closeCursor(cursor);
        } catch (Exception e) {
            closeCursor(cursor);
            e.printStackTrace();
        }

        for (int q = 0; q < recommendedList.size()-(NEXT_SONG_ITERATIONS+2);q++){
            for (int w = 2; w < NEXT_SONG_ITERATIONS+2; w++){
                long to = NextSongDataTable.getInstance(context)
                        .getPreferredId(Long.parseLong(recommendedList.get(q).get("id")));
                if (to == Long.parseLong(recommendedList.get(q+w).get("id"))){
                    Collections.swap(recommendedList,q+1,q+w);
                }
            }
        }

        return recommendedList;
    }


}
