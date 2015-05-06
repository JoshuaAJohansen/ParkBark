package com.team2.csc413.parkbark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;


public class Settings extends ActionBarActivity {

    Boolean alarm;
    Boolean bark;
    Boolean vibrate;
    Boolean clearHistory;
    public static final String SETTINGS = "AppPref";
    ToggleButton toggleAlarm;
    CheckBox checkBark;
    CheckBox checkVibrate;
    ToggleButton buttonClear;
    FrameLayout notificationFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        clearHistory = false;

        //link button and frame ids
        toggleAlarm = (ToggleButton) findViewById(R.id.toggleAlarm);
        checkBark = (CheckBox) findViewById(R.id.checkBark);
        checkVibrate = (CheckBox) findViewById(R.id.checkVibrate);
        buttonClear = (ToggleButton) findViewById(R.id.buttonClear);
        notificationFrame = (FrameLayout) findViewById(R.id.frameNotification);

        //load preferences
        SharedPreferences loadSettings = getSharedPreferences(SETTINGS,MODE_PRIVATE);
        alarm = loadSettings.getBoolean("ALARM", true);
        bark = loadSettings.getBoolean("BARK", true);
        vibrate = loadSettings.getBoolean("VIBRATE", true);
        toggleAlarm.setChecked(alarm); //set button positions
        checkBark.setChecked(bark);
        checkVibrate.setChecked(vibrate);
        buttonClear.setChecked(false);

        if (alarm){
            notificationFrame.setVisibility(View.VISIBLE);
        } else  {
            notificationFrame.setVisibility(View.GONE);
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
            SharedPreferences saveSettings = getSharedPreferences(SETTINGS,MODE_PRIVATE);
            SharedPreferences.Editor editor = saveSettings.edit();
            editor.putBoolean("ALARM",alarm);
            editor.putBoolean("BARK",bark);
            editor.putBoolean("VIBRATE",vibrate);
            editor.putBoolean("CLEAR",clearHistory);
            editor.commit();

            if (clearHistory){
                //clear sqlite database here
            }

            //Intent i = new Intent(Settings.this, MainActivity.class);
            //startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onToggleClicked(View view) {
        // Check button position
        boolean on = ((ToggleButton) view).isChecked();

        switch (view.getId()) {
            case R.id.toggleAlarm:
                if (on) {
                    alarm = true;
                    notificationFrame.setVisibility(View.VISIBLE);
                } else {
                    alarm = false;
                    notificationFrame.setVisibility(View.GONE);
                }
                break;

                case R.id.buttonClear:
                    if (on)
                        clearHistory = true;
                    else
                        clearHistory = false;
                break;

        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
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
