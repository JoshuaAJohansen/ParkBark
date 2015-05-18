package com.team2.csc413.parkbark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.database.Cursor;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.support.v4.widget.DrawerLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.sql.Time;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//TODO Include Park button that store park location through SQLite


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    GoogleMap mMap;
    Marker ParkMarker = null;
    ImageButton Park_Button = null;
    ImageButton TimeToWalk_Button = null;
    ImageButton Alarm_Btn = null;
    MediaPlayer One_Bark;
    //MediaPlayer Barks;
    int setNotification = 0;
    boolean parked = false;
    boolean alarmOn;
    boolean barkOn;
    boolean vibrateOn;
    boolean walkOn;
 
    public static final String SETTINGS = "AppPref";
    
    SQLiteDatabaseAdapter dbAdapter;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //load preferences from settings. Default value = true
        SharedPreferences loadSettings = getSharedPreferences(SETTINGS,MODE_PRIVATE);
        alarmOn = loadSettings.getBoolean("ALARM", true);
        barkOn = loadSettings.getBoolean("BARK", true);
        vibrateOn = loadSettings.getBoolean("VIBRATE", true);
        walkOn = loadSettings.getBoolean("WALK", true);

        dbAdapter = new SQLiteDatabaseAdapter(this);


        Park_Button = (ImageButton) findViewById(R.id.Park_Btn);
        TimeToWalk_Button = (ImageButton) findViewById(R.id.TimeToWalk_Btn);
        Alarm_Btn = (ImageButton) findViewById(R.id.Alarm_Btn);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        //initialize map fragment
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        setMapUI();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        TimeToWalk_Button.setEnabled(false);

        //Park Button On-Click listener
        Park_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setParkMarker();
            }
        });

        Alarm_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setNotification == 0) {
                    setNotification = 1;
                    showTimerDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "Notification already set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Set parking when tapping on the screen
         */
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng currentlocation) {
                if (ParkMarker == null) {

                    //LatLng PARKED = new LatLng(location.getLatitude(), location.getLongitude());
                    ParkMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentlocation)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

                    String Text = "Parking Location: \n" +
                            "Latitude: " + currentlocation.latitude + "\n" +
                            "Longitude: " + currentlocation.longitude;

                    //Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Car parked", Toast.LENGTH_SHORT).show();
                    addRemoteParkingSpot(currentlocation);

                    Park_Button.setBackgroundResource(R.drawable.leave_btn);
                    parked = true;
                    TimeToWalk_Button.setEnabled(true);
                } else {
                    ParkMarker.remove();

                    Toast.makeText(getApplicationContext(), "Leaving Parking Spot", Toast.LENGTH_SHORT).show();

                    Park_Button.setBackgroundResource(R.drawable.park_btn);

                    ParkMarker = null;
                    parked = false;
                    TimeToWalk_Button.setEnabled(false);
                }
            }
        });

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        switch (position) {
            case 0:
                break;
            case 1:
                showHistoryParking();
                break;
            case 2:
                mMap.clear();
                SFParking.service.drawParking(mMap);
                break;
        }


        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                mMap.clear();
                if (parked == true){
                LatLng PARKED = getLatLng();
                ParkMarker = mMap.addMarker(new MarkerOptions()
                        .position(PARKED)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
                }
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_navigation){
            navigate();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    private void setMapUI() {
        mMap.setMyLocationEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(16)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to north
                    .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }

    private void setParkMarker() {
        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            Toast.makeText(getApplicationContext(), "Cannot find Location: location == NULL", Toast.LENGTH_SHORT).show();
        } else if (ParkMarker == null) {

            LatLng PARKED = new LatLng(location.getLatitude(), location.getLongitude());
            ParkMarker = mMap.addMarker(new MarkerOptions()
                    .position(PARKED)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

            String Text = "Parking Location: \n" +
                    "Latitude: " + location.getLatitude() + "\n" +
                    "Longitude: " + location.getLongitude();

            Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();

            Park_Button.setBackgroundResource(R.drawable.leave_btn);
            parked = true;
            addParkingSpot();
            TimeToWalk_Button.setEnabled(true);

            // Adding a media player and sound to media player
            // On the start button click even the sound will start
            //One_Bark = MediaPlayer.create(MainActivity.this,R.raw.onebark);

            //One_Bark.start();


            // Sound for alarm
            //Barks=MediaPlayer.create(MainActivity.this,R.raw.barksound);
            //Barks.start();
        } else {
            //mMap.clear();
            ParkMarker.remove();
            Toast.makeText(getApplicationContext(), "Leaving Parking Spot", Toast.LENGTH_SHORT).show();

            Park_Button.setBackgroundResource(R.drawable.park_btn);

            ParkMarker = null;
            parked = false;
            TimeToWalk_Button.setEnabled(false);
        }
    }

    /**
     * Calls insertParkingSpot
     */
    public void addParkingSpot() {
        Log.d("SQLTag", "Enter SQL function");


        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MMM:dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        
        String date = dateFormat.format(c.getTime());
        String time = timeFormat.format(c.getTime());
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String duration = "duration";
        String restriction = "restriction";
        dbAdapter.insertParkingSpot(date, time, lat, lng, duration, restriction);
    }

    public void navigate() {
        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        SFParking.service.retrieveParkingList(location);

        List park_li2 = SFParking.service.getParkingList();

        String[] parkingPlaces = new String[SFParking.service.getNum_records()];

        final LatLng[] parkingLoc = new LatLng[SFParking.service.getNum_records()];

        for (int x = 0; x < SFParking.service.getNum_records(); x++) {
            SFParking.ParkingPlace place = (SFParking.ParkingPlace) park_li2.get(x);

            parkingPlaces[x] = place.getName();

            SFParking.LocationSFP locsfp = place.getLoc();
            parkingLoc[x] = new LatLng(locsfp.getLatPrime(), locsfp.getLngPrime());
        }

        AlertDialog.Builder MyListAlertDialog = new AlertDialog.Builder(MainActivity.this);

        MyListAlertDialog.setTitle("Available Parking Places");

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Available Parking Places");

        builder.setItems(parkingPlaces, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(), "This is num:"+which, Toast.LENGTH_LONG).show();
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr=" + location.getLatitude()+ "," + location.getLongitude() + "&daddr=" + parkingLoc[which].latitude + "," + parkingLoc[which].longitude));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
        builder.show();
    }

    /**
     * Set up timer with an AlertDialog
     */
    private void showTimerDialog() {
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);

        View customDialog = inflater.inflate(R.layout.notification_dialog, null);

        final EditText editHour = (EditText) customDialog.findViewById(R.id.edit_hour);
        final EditText editMin = (EditText) customDialog.findViewById(R.id.edit_min);

        editHour.setRawInputType(Configuration.KEYBOARD_QWERTY);
        editMin.setRawInputType(Configuration.KEYBOARD_QWERTY);

        AlertDialog.Builder settingDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Set notification time")
                .setView(customDialog)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final TextView timer = (TextView) findViewById(R.id.timer);
                        int hour = 0;
                        int minute = 0;
                        if (editHour.getText().toString().equals("")) {
                            hour = 0;
                        } else {
                            hour = Integer.valueOf(editHour.getText().toString());
                        }
                        if (editMin.getText().toString().equals("")) {
                            minute = 0;
                        } else {
                            minute = Integer.valueOf(editMin.getText().toString());
                        }
                        int totalSec = (hour * 60 * 60) + (minute * 60);
                        Long time = new GregorianCalendar().getTimeInMillis() + (totalSec * 1000);

                        AlarmManager alarm = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, AlarmReciever.class);
                        PendingIntent pending = PendingIntent.getBroadcast(MainActivity.this, 1, intent, 0);
                        alarm.set(AlarmManager.RTC_WAKEUP, time, pending);

                        new CountDownTimer(totalSec * 1000, 1000) {

                            @Override
                            public void onTick(long l) {
                                int hourRemain = (int) ((l / 1000) / 3600);
                                int minRemain = (int) (((l / 1000) - (hourRemain * 3600)) / 60);
                                if (hourRemain < 10 && minRemain > 10) {
                                    timer.setText("Time remain " + "0" + hourRemain + ":" + minRemain);
                                }
                                if (hourRemain > 10 && minRemain < 10) {
                                    timer.setText("Time remain " + hourRemain + ":" + "0" + minRemain);
                                }
                                if (hourRemain < 10 && minRemain < 10) {
                                    timer.setText("Time remain " + "0" + hourRemain + ":" + "0" + minRemain);
                                }
                                if (hourRemain < 10 && minRemain < 1) {
                                    timer.setText("Time remain less than 1 minute!");
                                }
                            }

                            @Override
                            public void onFinish() {
                                setNotification = 0;
                                timer.setText("");
                                /*new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Times up")
                                        .setIcon(android.R.drawable.ic_dialog_info)
                                        .setPositiveButton("Ok", null)
                                        .show();*/
                            }
                        }.start();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setNotification = 0;
                        dialog.cancel();
                    }
                });
        settingDialog.show();
    }

    public void addRemoteParkingSpot(LatLng remoteMarker) {

        Log.d("SQLTag", "Enter SQL function");

        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MMM:dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


        String date = dateFormat.format(c.getTime());
        String time = timeFormat.format(c.getTime());
        double lat = remoteMarker.latitude;
        double lng = remoteMarker.longitude;
        String duration = "duration";
        String restriction = "restriction";

        dbAdapter.insertParkingSpot(date, time, lat, lng, duration, restriction);

    }
    
    /**
     * Displays markers on map of all parked locations stored in database.
     * Markers are only visible in Parking History Tab.
     */
    public void showHistoryParking() {
        String getUID, getDATE, getTIME, getDURATION, getRESTRICTION;
        double getLAT, getLNG;

        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        Cursor cursor = dbAdapter.getAllParkingSpot();

        if (cursor.moveToFirst()) {
            do {
                getUID = cursor.getString(0);
                getDATE = cursor.getString(1);
                getTIME = cursor.getString(2);
                getLAT = cursor.getFloat(3);
                getLNG = cursor.getFloat(4);
                getDURATION = cursor.getString(5);
                getRESTRICTION = cursor.getString(6);

                LatLng marker = new LatLng(getLAT, getLNG);
                Marker historyMarker = mMap.addMarker(new MarkerOptions()
                        .position(marker));

            } while (cursor.moveToNext());
        }
        cursor.close();
    }


    /**
     * Function selects from the database the Latitude and Longitude
     * of last parked location
     *
     * @return LatLng object of the most recent parked location in database
     */
    public LatLng getLatLng() {
        LatLng myLatLng;
        double myLNG, myLAT;

        Cursor cursor = dbAdapter.getAllParkingSpot();

        if (cursor.moveToLast()) {
            myLAT = cursor.getFloat(3);
            myLNG = cursor.getFloat(4);
            myLatLng = new LatLng(myLAT, myLNG);
            cursor.close();
            return myLatLng;
        }
        myLatLng = new LatLng(-1, -1);
        cursor.close();
        return myLatLng;
    }

    /**
     * Selects latitude from last row of database and returns it
     *
     * @return double myLAT or -1.0 if database is empty
     */
    public double getLAT() {
        double myLAT;
        Cursor cursor = dbAdapter.getAllParkingSpot();

        if (cursor.moveToLast()) {
            myLAT = cursor.getDouble(3);
            cursor.close();
            return myLAT;
        } else return (-1.0);
    }

    /**
     * Selects longitude from last row of database and returns it
     *
     * @return double myLNG or -1.0 if database is empty
     */
    public double getLNG() {
        double myLNG;
        Cursor cursor = dbAdapter.getAllParkingSpot();

        if (cursor.moveToLast()) {

            myLNG = cursor.getDouble(4);
            cursor.close();
            return myLNG;
        } else return -1.0;
    }

    /**
     * Function that calculates and does a toast for the time to walk back to car.
     *
     * @param v Current view of program
     */
    public void timeToWalk(View v) {
        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        int Radius = 6371;

        double lat1 = location.getLatitude();
        double lng1 = location.getLongitude();

        double lat2 = getLAT();
        double lng2 = getLNG();

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2)
                * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2)
                * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = Radius * c * 1000;

        double seconds = distance / 1.4;

        double minutes = seconds / 60;

        int finalTime = (int) Math.ceil(minutes);

        Toast.makeText(MainActivity.this, "Time to Walk to Car is " + finalTime + " minutes", Toast.LENGTH_SHORT).show();
    }

}
