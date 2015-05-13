package com.team2.csc413.parkbark;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;


public class Settings extends ActionBarActivity {

    boolean alarm;
    boolean bark;
    boolean vibrate;
    boolean walk;
    public static final String SETTINGS = "AppPref";
    ToggleButton toggleAlarm;
    CheckBox checkBark;
    CheckBox checkVibrate;
    FrameLayout notificationFrame;
    FrameLayout walkingFrame;
    ToggleButton toggleWalk;
    SQLiteDatabaseAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //link button and frame ids
        toggleAlarm = (ToggleButton) findViewById(R.id.toggleAlarm);
        checkBark = (CheckBox) findViewById(R.id.checkBark);
        checkVibrate = (CheckBox) findViewById(R.id.checkVibrate);
        toggleWalk = (ToggleButton) findViewById(R.id.toggleWalk);
        notificationFrame = (FrameLayout) findViewById(R.id.frameNotification);
        walkingFrame = (FrameLayout) findViewById(R.id.frameWalk);

        //load preferences
        SharedPreferences loadSettings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        alarm = loadSettings.getBoolean("ALARM", true);
        bark = loadSettings.getBoolean("BARK", true);
        vibrate = loadSettings.getBoolean("VIBRATE", true);
        walk = loadSettings.getBoolean("WALK", true);

        //set button positions
        toggleAlarm.setChecked(alarm);
        checkBark.setChecked(bark);
        checkVibrate.setChecked(vibrate);
        toggleWalk.setChecked(walk);

        //hide notification and walking frames if alarm is off
        if (alarm) {
            notificationFrame.setVisibility(View.VISIBLE);
            walkingFrame.setVisibility(View.VISIBLE);
        } else {
            notificationFrame.setVisibility(View.GONE);
            walkingFrame.setVisibility(View.GONE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.saveSettings) {
            //store variables with preferences
            SharedPreferences saveSettings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
            SharedPreferences.Editor editor = saveSettings.edit();
            editor.putBoolean("ALARM", alarm);
            editor.putBoolean("BARK", bark);
            editor.putBoolean("VIBRATE", vibrate);
            editor.putBoolean("WALK", walk);
            editor.commit();

            Toast.makeText(getApplicationContext(), "Settings Saved",
                    Toast.LENGTH_LONG).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onToggleClicked(View view) {
        // Check toggle button position
        boolean on = ((ToggleButton) view).isChecked();

        switch (view.getId()) {
            //alarm button pressed
            case R.id.toggleAlarm:
                if (on) {
                    alarm = true;
                    //hide notif and walk tabs
                    notificationFrame.setVisibility(View.VISIBLE);
                    walkingFrame.setVisibility(View.VISIBLE);
                } else {
                    alarm = false;
                    //show notif and walk tabs
                    notificationFrame.setVisibility(View.GONE);
                    walkingFrame.setVisibility(View.GONE);
                }
                break;

            //walk button pressed
            case R.id.toggleWalk:
                if (on) {
                    walk = true;
                } else {
                    walk = false;
                }
                break;

        }
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.clear:
                AlertDialog.Builder clearWarning = new AlertDialog.Builder(this);

                clearWarning.setTitle("Clear History");
                clearWarning.setMessage("Are you sure you want to delete all saved parking spots?");

                //selected no, do nothing
                clearWarning.setPositiveButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });

                //selected yes
                clearWarning.setNegativeButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(getApplicationContext(), "Insert code - Clear database",
                                Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                    }

                });

                AlertDialog alert = clearWarning.create();
                alert.show();

                break;

        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.checkBark:
                if (checked)
                    bark = true;
                else
                    bark = false;
                break;
            case R.id.checkVibrate:
                if (checked)
                    vibrate = true;
                else
                    vibrate = false;
                break;
        }
    }
}
