package com.vinay.vinplayer.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vinay.vinplayer.R;

import java.util.Date;

/**
 * Created by vinaysajjanapu on 9/2/17.
 */

public class VinDBHelper extends SQLiteOpenHelper {
    private Context context_;
    private static String DATABASE_NAME = "";
    private static int DATABASE_VERSION = 1;
    private String DB_PATH = "";
    private SQLiteDatabase db;


    public static String usageDataTable = "usageData";
    public static String recentPlayTable = "recentPlay";
    public static String recentAddedTable = "recentAdded";
    public static String favouriteTable = "favourites";
    public static String lastPlayTable = "lastPlay";
    public static String playlistTable = "playlist";
    public static String nextSongDataTable = "nextSongData";
    public static String scoreTable = "scoreTable";

    //////////////////////column_names
    public static String SONG_ID = "song_id";
    public static String TITLE = "title";
    public static String ALBUM = "album";
    public static String ALBUM_ID = "album_id";
    public static String ARTIST = "artist";
    public static String DATE_ADDED = "date_added";
    public static String ADDED_BEFORE = "added_before";
    public static String DATA = "data";
    public static String NO_TIMES_PLAYED = "actual_times";
    public static String WITH_HEADP = "with_head";
    public static String WITHOUT_HEADP = "without_head";
    public static String LEFT_POSITION = "position";
    public static String TIME_1 = "time1";
    public static String TIME_2 = "time2";
    public static String TIME_3 = "time3";
    public static String TIME_4 = "time4";
    public static String TIME_5 = "time5";
    public static String TIME_6 = "time6";
    public static String PLAYED_PERCENT = "played_percent";
    public static String AS_FIRST = "as_first";
    public static String AS_LAST = "as_last";
    public static String PREF1 = "pref1";
    public static String PREF2 = "pref2";
    public static String PREF3 = "pref3";

    public static String TIME_INDEX = "time_index";
    public static String HEADP_INDEX = "headp_index";
    public static String ALL_INDEX = "all_index";
    public static String RECENT_PLAY_INDEX = "recentplayindex";
    public static String RECENT_ADDED_INDEX = "recentaddedindex";
    public static String IS_FAVOURITE = "isfavourite";
    public static String NEXT_PREFERRED_ID = "nextprefferedid";
    public static String SCORE = "score";






    public VinDBHelper(Context context) {
        super(context, context.getString(R.string.db_name), null,
                Integer.parseInt(context.getString(R.string.db_version)));
        this.context_ = context;
        DATABASE_NAME = context.getResources().getString(R.string.db_name);
        DATABASE_VERSION = Integer.parseInt(context.getResources().getString(R.string.db_version));
        DB_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
        context.openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE, null);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(usageDataTableSQL());
            db.execSQL(recentPlaySQL());
            db.execSQL(favouriteSQL());
            db.execSQL(lastPlaySQL());
          //  db.execSQL(nextSongTableSQL(VinMediaLists.allSongs.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS "+usageDataTable );
            db.execSQL("DROP TABLE IF EXISTS "+recentPlayTable );
            db.execSQL("DROP TABLE IF EXISTS "+favouriteTable );
            db.execSQL("DROP TABLE IF EXISTS "+lastPlayTable );
          //  db.execSQL("DROP TABLE IF EXISTS "+nextSongDataTable );
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SQLiteDatabase getDb(){
        return db;
    }

    @Override
    public synchronized void close() {
        if (getDb() != null)
            getDb().close();
        super.close();
    }


    public void openDataBase() throws SQLException {
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String recentPlaySQL(){
        String sql = "CREATE TABLE IF NOT EXISTS " + recentPlayTable + " (" +
                "idx" + " INTEGER , "+
                SONG_ID + " INTEGER NOT NULL,"+
                ALBUM_ID + " INTEGER NOT NULL," +
                TITLE + " TEXT NOT NULL," +
                ALBUM + " TEXT NOT NULL," +
                ARTIST + " TEXT NOT NULL," +
                DATA + " TEXT NOT NULL);";
        return sql;
    }


    private String favouriteSQL(){
        String sql = "CREATE TABLE IF NOT EXISTS " + favouriteTable + " (" +
                SONG_ID + " INTEGER NOT NULL PRIMARY KEY,"+
                ALBUM_ID + " INTEGER NOT NULL," +
                TITLE + " TEXT NOT NULL," +
                ALBUM + " TEXT NOT NULL," +
                ARTIST + " TEXT NOT NULL," +
                DATA + " TEXT NOT NULL);";
        return sql;
    }




    public String lastPlaySQL(){
        String sql = "CREATE TABLE IF NOT EXISTS " + lastPlayTable + " (" +
                SONG_ID + " INTEGER NOT NULL PRIMARY KEY,"+
                ALBUM_ID + " INTEGER NOT NULL," +
                TITLE + " TEXT NOT NULL," +
                ALBUM + " TEXT NOT NULL," +
                ARTIST + " TEXT NOT NULL," +
                DATA + " TEXT NOT NULL, "+
                LEFT_POSITION + " INTEGER NOT NULL);";
        return sql;
    }


    private String usageDataTableSQL(){
        String sql = "CREATE TABLE IF NOT EXISTS " + usageDataTable +
                " (" +
                SONG_ID + " INTEGER NOT NULL PRIMARY KEY,"+   //1
                ALBUM_ID + " INTEGER NOT NULL," +            //2
                TITLE + " TEXT NOT NULL," +                 //3
                ALBUM + " TEXT NOT NULL," +                //4
                ARTIST + " TEXT NOT NULL," +                     //5
                DATA + " TEXT NOT NULL,"+                       //6
                TIME_1 + " INTEGER DEFAULT 0,"+                //7
                TIME_2 + " INTEGER DEFAULT 0,"+               //8
                TIME_3 + " INTEGER DEFAULT 0,"+              //9
                TIME_4 + " INTEGER DEFAULT 0,"+             //10
                TIME_5 + " INTEGER DEFAULT 0,"+                 //11
                TIME_6 + " INTEGER DEFAULT 0,"+                //12
                PLAYED_PERCENT + " INTEGER DEFAULT 0,"+       //13
                AS_FIRST + " INTEGER DEFAULT 0,"+            //14
                AS_LAST + " INTEGER DEFAULT 0,"+            //15
                NO_TIMES_PLAYED + " INTEGER DEFAULT 0,"+            //16
                WITH_HEADP + " INTEGER DEFAULT 0,"+                //17
                WITHOUT_HEADP + " INTEGER DEFAULT 0" +            //18
                ");";
        return sql;
    }

    public String playlistSQL(String playlistName){
        String sql = "CREATE TABLE IF NOT EXISTS " + playlistName + " (" +
                SONG_ID + " INTEGER NOT NULL PRIMARY KEY,"+
                ALBUM_ID + " INTEGER NOT NULL," +
                TITLE + " TEXT NOT NULL," +
                ALBUM + " TEXT NOT NULL," +
                ARTIST + " TEXT NOT NULL," +
                DATA + " TEXT NOT NULL);";
        return sql;
    }

    public String nextSongTableSQL(int size){
        String sql ="CREATE TABLE IF NOT EXISTS " + nextSongDataTable + " (" +
                "idx" + " INTEGER PRIMARY KEY AUTOINCREMENT , "+
                SONG_ID + " INTEGER NOT NULL, "+
                PREF1 + " INTEGER NOT NULL DEFAULT 0 ";
        for (int i=0;i<size;i++)
            sql += ",col"+i+" INTEGER DEFAULT 0";

        sql += ");";

        return sql;
    }

    public int getCurrentTimeDiv(){
        return new Date().getHours()%4;
    }

}
