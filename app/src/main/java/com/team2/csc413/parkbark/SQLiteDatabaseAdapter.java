package com.team2.csc413.parkbark;



import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

/**
 * The SQlite adapter that serve as interface between SQLite database and Activity
 */
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

    /**
     * Constructor that access the SQLiteDatabaseAdapter
     * @param context The context of main activity
     */
    public SQLiteDatabaseAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Creates database and the table using the string, DATABASE_CREATE
     *
     * @param db        The database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.d("SQLTag", "Created table");
    }


    /**
     * Drops the table of the database and calls onCreate to create
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

    /**
     * Inserts row with data of parking spot to the database. Information
     * is generated through location information.
     *
     * @param date          date when the car was parked day/month/year.
     * @param time          time car was parked using military time
     * @param LATITUDE      gps coordinates for latitude where car was parked
     * @param LONGITUDE     gps coordinates for longitude where car was parked
     * @param duration      duration the car was intended to be parked
     * @param restriction   parking restrictions for current location
     */
    public void insertParkingSpot(String date, String time, double LATITUDE, double LONGITUDE,
                                  String duration, String restriction)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("SQLTag", "insertParkingSpot: getWritableDatabase()");

        ContentValues contentValues = new ContentValues();
        contentValues.put(DATE, date);
        contentValues.put(TIME, time);
        contentValues.put(LAT, LATITUDE);
        contentValues.put(LNG, LONGITUDE);
        contentValues.put(DURATION, duration);
        contentValues.put(RESTRICTION, restriction);

        db.insert(TABLE_NAME, null, contentValues);

        db.close();
        Log.d("SQLTag", "insertParkingSpot: close()");

    }

    /**
     * Method returns a cursor of the readable database query
     *
     * @return db.query(TABLE_NAME, new String[] {UID, DATE, TIME, LAT, LNG, DURATION, RESTRICTION},
            null, null, null, null, null, null)
     */
    public Cursor getAllParkingSpot(){

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(TABLE_NAME, new String[] {UID, DATE, TIME, LAT, LNG, DURATION, RESTRICTION},
                null, null, null, null, null, null);

    }

    /**
     * After function is called table values in database will be set to null
     */
    public void deleteParkingHistory(){

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME, null, null);
        Log.d("SQLTag", "Delete table");

    }

}