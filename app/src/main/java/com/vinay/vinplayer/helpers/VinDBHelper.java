package com.vinay.vinplayer.helpers;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vinay.vinplayer.R;

/**
 * Created by vinaysajjanapu on 9/2/17.
 */

public class VinDBHelper extends SQLiteOpenHelper {
    private Context context_;
    private static String DATABASE_NAME = "";
    private static int DATABASE_VERSION = 0;
    private String DB_PATH = "";
    private SQLiteDatabase db;

    public VinDBHelper(Context context) {
        super(context, context.getString(R.string.db_name), null,
                Integer.parseInt(context.getString(R.string.db_version)));

        this.context_ = context;
        DATABASE_NAME = context.getResources().getString(R.string.db_name);
        DATABASE_VERSION = Integer.parseInt(context.getResources().getString(R.string.db_version));
        DB_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
        context.openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE, null);

    }

    public SQLiteDatabase getDB() {
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("");
            db.execSQL("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " );
            db.execSQL("DROP TABLE IF EXISTS " );
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public synchronized void close() {
        if (getDB() != null)
            getDB().close();

        super.close();
    }


    public void openDataBase() throws SQLException {
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
