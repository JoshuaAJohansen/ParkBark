package com.team2.csc413.parkbark;

import android.content.Context;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//used
import android.util.Log;

//TODO Check if a database exist, if exist just access it. If not, create one.

/**
 * Created by kevin on 4/24/15.
 */

public class SQLiteDatabaseAdapter {

    SQLiteCreator helper;

    public SQLiteDatabaseAdapter(Context context) {

        helper = new SQLiteCreator(context);
    }

    public long insertParkingSpot(String DATE, String TIME, double LAT, double LNG, String DURATION,
                                  String RESTRICTION) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteCreator.DATE, DATE);
        contentValues.put(SQLiteCreator.TIME, TIME);
        contentValues.put(SQLiteCreator.LAT, LAT);
        contentValues.put(SQLiteCreator.LNG, LNG);
        contentValues.put(SQLiteCreator.DURATION, DURATION);
        contentValues.put(SQLiteCreator.RESTRICTION, RESTRICTION);
        long id = db.insert(SQLiteCreator.TABLE_NAME, null, contentValues);

        return id;

    }

    //
    static class SQLiteCreator extends SQLiteOpenHelper {

        //Specify database column tags

        private static final String TABLE_NAME = "PARK_HISTORY";
        private static final String UID = "_id";
        private static final String DATE = "DATE";
        private static final String TIME = "TIME";
        private static final String LAT = "LATITUDE";
        private static final String LNG = "LONGITUDE";
        private static final String DURATION = "DURATION";
        private static final String RESTRICTION = "RESTRICTION";

        private static final String DATABASE_NAME = "parkbark.db";
        private static final int DATABASE_VERSION = 1;
        private Context context;


        //Database Creation String
        //NOTE: should be? "_integer... "_text ... with space in front of quotes for form "_id integ..."
        private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + "("
                + UID + "INTEGER PRIMARY KEY AUTOINCREMENT "
                + DATE + "TEXT NOT NULL "
                + TIME + "TEXT NOT NULL "
                + LAT + "FLOAT NOT NULL "
                + LNG + "FLOAT NOT NULL "
                + DURATION
                + RESTRICTION
                + ")";

        private static final String DROP_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXISTS";

        //Successfully calls constructor
        public SQLiteCreator(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE);
                Message.message(context, "onCreate called");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        explanation on log, used to log database creation during development
        delete this when launching application
        http://developer.android.com/reference/android/util/Log.html#w(java.lang.String, java.lang.String)
        Log.w(SQLiteCreator.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        */

            db.execSQL(DROP_TABLE);
            onCreate(db);
        }


    }
}