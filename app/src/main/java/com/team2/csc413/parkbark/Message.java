package com.team2.csc413.parkbark;

/**
 * Message class to toast when database methods are called during testing
 */

import android.content.Context;
import android.widget.Toast;

public class Message {
    public static void message(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}