package com.team2.csc413.parkbark;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

/**
 * Created by Michael on 5/7/15.
 */
public class AlarmReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "This is alarm", Toast.LENGTH_LONG).show();
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        Intent intentALT = new Intent("android.intent.action.ALERT");
        intentALT.setClass(context, com.team2.csc413.parkbark.AlertDialog.class);
        intentALT.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentALT);

    }
}
