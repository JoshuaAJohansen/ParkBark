package com.team2.csc413.parkbark;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//TODO Check if a database exist, if exist just access it. If not, create one.

/**
 * Created by kevin on 4/24/15.
 */
public class SQLiteCreator extends SQLiteOpenHelper {

    //Specify database column tags
    private static final String DATABASE_NAME = "parkbark.db";
    private static final String TABLE_NAME = "PARK_HISTORY";
    private static final String UID = "_id";
    private static final String DATE = "DATE";
    private static final String TIME = "TIME";
    private static final String LAT = "LATITUDE";
    private static final String LNG = "LONGITUDE";
    private static final String DURATION = "TIME_PARKED";
    private static final String RESTRICTION = "RESTRICTION";
    private static final int DATABASE_VERSION = 1;

    public SQLiteCreator(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Database Creation String
    private static final String DATABASE_CREATE = "CREATE TABLE" + TABLE_NAME + "("
            + UID + "integer primary key autoincrement,"
            + DATE + "text not null, "
            + TIME + "text not null, "
            + LAT + "not null"
            + LNG + "not null"
            + DURATION
            + RESTRICTION
            + ")";

    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(DATABASE_CREATE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }


}
