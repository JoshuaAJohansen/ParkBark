package com.team2.csc413.parkbark;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

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

        /*
        LatLng loc1 = new LatLng(37.7231644, -122.47552105);
        LatLng loc2 = new LatLng(37.7231644, -123.47552105);
        addLines(loc1, loc2);
        */

        // Park Button On-Click listener
        // needs modification for current class
        Park_Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // example for testing location when location can not be retrieved from LocationManager
                //Location location = new Location("SF Parking location example");
                //location.setLatitude(37.792275);
                //location.setLongitude(-122.397089);



                if (location == null) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Location not found");
                    alertDialog.setMessage("we regret to inform you, your last known location could not be found");
                    alertDialog.setButton("Continue", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // closed
                        }
                    });
                    // Showing Alert Message
                    alertDialog.show();


                } else {
                    String streetNames = "";

                    SFParking.service.retrieveParkingList(location);

                    List park_li = SFParking.service.getParkingList();

                    if (SFParking.service.getStatus().equals("SUCCESS")) {

                        for (int i = 0; i < SFParking.service.getNum_records(); i++) {
                            SFParking.ParkingPlace place = (SFParking.ParkingPlace) park_li.get(i);

                            streetNames += place.getName() + "\n";

                            SFParking.LocationSFP locsfp = place.getLoc();

                            if (locsfp.getNumLocations() > 1) {
                                LatLng loc1 = new LatLng(locsfp.getLat1(), locsfp.getLng1());
                                LatLng loc2 = new LatLng(locsfp.getLat2(), locsfp.getLng2());
                                addLines(loc1, loc2);

                            } else {
                                LatLng loc = new LatLng(locsfp.getLat1(), locsfp.getLng1());
                                addMarker(place.getName(), loc);
                            }

                            // example of retrieving ophrs from SFParking class
                            /*
                            List ophrs = place.getOPHRS();

                            if (ophrs != null) {

                                for (int j=0; j < ophrs.size(); j++) {

                                    SFParking.OPHRS ops = (SFParking.OPHRS)ophrs.get(j);

                                    streetNames += "from: " + ops.getFrom() + "\nto: " + ops.getTo()
                                            + "\nbeggining: " + ops.getBeg() + "\nend: " + ops.getEnd() + "\n";
                                }
                            }
                            */
                        }
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Available Parking Places");
                        alertDialog.setMessage(streetNames);
                        alertDialog.setButton("Continue", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        alertDialog.show();

                    } else {
                        Log.d(null, "Application can not connect to SF Parking service");
                    }

                }

            }
        });

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

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



        }else{

            mMap.clear();

            Toast.makeText(getApplicationContext(), "Leaving Parking Spot", Toast.LENGTH_SHORT).show();

            Park_Button.setBackgroundResource(R.drawable.park_btn);

            ParkMarker = null;
        }
    }

    private void addLines(LatLng loc1, LatLng loc2) {
        mMap
                .addPolyline((new PolylineOptions())
                        .add(loc1, loc2).width(5).color(Color.BLUE)
                        .geodesic(true));
        // move camera to zoom on map
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOWER_MANHATTAN, 13));
    }

    public void addMarker(String name, LatLng loc) {
        mMap.addMarker(new MarkerOptions()
                .position(loc)
                .title(name));
    }
}
