package com.team2.csc413.parkbark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;


public class Settings extends ActionBarActivity {

Boolean alarm;
    public static final String SETTINGS = "AppPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //link button ids
        ToggleButton toggleAlarm = (ToggleButton) findViewById(R.id.toggleAlarm);

        //load preferences
        SharedPreferences loadSettings = getSharedPreferences(SETTINGS,MODE_PRIVATE);
        alarm = loadSettings.getBoolean("ALARM", true);
        toggleAlarm.setChecked(alarm); //set button positions
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
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.save) {
            SharedPreferences saveSettings = getSharedPreferences(SETTINGS,MODE_PRIVATE);
            SharedPreferences.Editor editor = saveSettings.edit();
            editor.putBoolean("ALARM",alarm);
            editor.apply();

            Intent i = new Intent(Settings.this, MainActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onToggleClicked(View view) {
        // Check button position
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            alarm = true;
            //startActivity(new Intent(Settings.this, MainActivity.class));
        } else {
            alarm = false;
        }
    }

}
