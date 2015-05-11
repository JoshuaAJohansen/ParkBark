package com.team2.csc413.parkbark;

import android.app.Activity;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;

/**
 * Created by Michael on 5/8/15.
 */

/**
 * Activity after alarm is triggered
 */
public class AlertDialog extends Activity {
    MediaPlayer Three_Barks;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Three_Barks=MediaPlayer.create(this,R.raw.barksound);
        Three_Barks.start();

        android.app.AlertDialog.Builder settingDialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Times up")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("You should move your car now!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        settingDialog.show();
    }
}
