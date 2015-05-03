package com.team2.csc413.parkbark;

import android.content.Context;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//used
import android.util.Log;

//TODO Create method for reading from database
//TODO Create method for deleting the database (from setting)

/**
 * Created by kevin on 4/24/15.
 */

//
public class SQLiteDatabaseAdapter extends SQLiteOpenHelper {

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
            + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DATE + " TEXT NOT NULL, "
            + TIME + " TEXT NOT NULL, "
            + LAT + " FLOAT NOT NULL, "
            + LNG + " FLOAT NOT NULL, "
            + DURATION + ", "
            + RESTRICTION
            + ")";

    private static final String DROP_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXISTS";

    public SQLiteDatabaseAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Creates database and the table using the string, DATABASE_CREATE
     *
     * @param db         The database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.d("SQLTag", "Created table");
    }


    /**
     * Drop's the table of the database and calls onCreate to create
     * new table with new schema
     *
     * @param db         The database
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    //TODO find way to generate current date and time.
    /**
     * Inserts row with data of parking spot to the database. Information
     * is generated through location information.
     *
     * @param DATE          date when the car was parked day/month/year.
     * @param TIME          time car was parked using military time
     * @param LATITUDE      gps coordinates for latitude where car was parked
     * @param LONGITUDE     gps coordinates for longitude where car was parked
     * @param DURATION      duration the car was intended to be parked
     * @param RESTRICTION   parking restrictions for current location
     */
    public void insertParkingSpot(String DATE, String TIME, double LATITUDE, double LONGITUDE,
                                  String DURATION, String RESTRICTION)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("SQLTag", "insertParkingSpot: getWritableDatabase()");

        ContentValues contentValues = new ContentValues();
        contentValues.put(DATE, DATE);
        contentValues.put(TIME, TIME);
        contentValues.put(LAT, LATITUDE);
        contentValues.put(LNG, LONGITUDE);
        contentValues.put(DURATION, DURATION);
        contentValues.put(RESTRICTION, RESTRICTION);

        db.insert(TABLE_NAME, null, contentValues);

        db.close();
        Log.d("SQLTag", "insertParkingSpot: close()");

    }

}
