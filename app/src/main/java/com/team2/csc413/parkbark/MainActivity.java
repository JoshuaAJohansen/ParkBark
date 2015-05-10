package com.team2.csc413.parkbark;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import android.media.MediaPlayer;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.*;

//TODO Include Park button that store park location through SQLite



public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    GoogleMap mMap;
    Marker ParkMarker = null;
    ImageButton Park_Button = null;
    int setNotification = 0;
    ImageButton Alarm_Btn = null;

    MediaPlayer One_Bark;
    //MediaPlayer Barks;


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

        Park_Button = (ImageButton) findViewById(R.id.Park_Btn);
        Alarm_Btn = (ImageButton)findViewById(R.id.Alarm_Btn);

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


        // Park Button On-Click listener
        // needs modification for current class
        Park_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setParkMarker();
            }
        });

        Alarm_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setNotification == 0){
                    setNotification = 1;
                    showTimerDialog();
                }else {
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

                    Park_Button.setBackgroundResource(R.drawable.leave_btn);


                } else {
                    ParkMarker.remove();

                    Toast.makeText(getApplicationContext(), "Leaving Parking Spot", Toast.LENGTH_SHORT).show();

                    Park_Button.setBackgroundResource(R.drawable.park_btn);

                    ParkMarker = null;
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
                break;
            case 2:
                mMap.clear();
                SFParking.service.drawParking(mMap /* , getLayoutInflater() */);
                break;
            case 3:
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
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section_sfpark);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

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


    private void setMapUI(){
        mMap.setMyLocationEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
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

    private void setParkMarker(){
        LocationManager locationmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null){
            Toast.makeText(getApplicationContext(), "Cannot find Location: location == NULL", Toast.LENGTH_SHORT).show();
        }else if(ParkMarker == null) {

            LatLng PARKED = new LatLng (location.getLatitude(), location.getLongitude());
            ParkMarker = mMap.addMarker(new MarkerOptions()
                    .position(PARKED)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

            String Text = "Parking Location: \n" +
                    "Latitude: " + location.getLatitude() + "\n" +
                    "Longitude: " + location.getLongitude();

            Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();

            Park_Button.setBackgroundResource(R.drawable.leave_btn);

            // Adding a media player and sound to media player
            // On the start button click even the sound will start
            One_Bark=MediaPlayer.create(MainActivity.this,R.raw.onebark);

            One_Bark.start();

            // Sound for alarm
            //Barks=MediaPlayer.create(MainActivity.this,R.raw.barksound);
            //Barks.start();

        }else{

            mMap.clear();

            Toast.makeText(getApplicationContext(), "Leaving Parking Spot", Toast.LENGTH_SHORT).show();

            Park_Button.setBackgroundResource(R.drawable.park_btn);

            ParkMarker = null;
        }
    }

    public void navigate(){
        final Location location = new Location("");
        location.setLatitude(37.792275);
        location.setLongitude(-122.397089);
        SFParking.service.retrieveParkingList(location);
        List park_li2 = SFParking.service.getParkingList();
        String[] parkingPlaces = new String[SFParking.service.getNum_records()];
        final LatLng[] parkingLoc = new LatLng[SFParking.service.getNum_records()];
        for (int x = 0; x < SFParking.service.getNum_records(); x++) {
            SFParking.ParkingPlace place = (SFParking.ParkingPlace) park_li2.get(x);
            parkingPlaces[x] = place.getName();
            SFParking.LocationSFP locsfp = place.getLoc();
            parkingLoc[x] = new LatLng(locsfp.getLat1(), locsfp.getLng1());
        }

        AlertDialog.Builder MyListAlertDialog = new AlertDialog.Builder(MainActivity.this);
        MyListAlertDialog.setTitle("Available Parking Places");

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Available Parking Places");
        builder.setItems(parkingPlaces, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(), "This is num:"+which, Toast.LENGTH_LONG).show();
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr=" + location.getLatitude() + "," + location.getLongitude() + "&daddr=" + parkingLoc[which].latitude + "," + parkingLoc[which].longitude));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
        builder.show();
    }

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
                        Long time = new GregorianCalendar().getTimeInMillis() + (totalSec*1000);

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
}


