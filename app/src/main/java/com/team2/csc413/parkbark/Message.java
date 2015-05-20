package com.team2.csc413.parkbark;



import android.content.Context;
import android.widget.Toast;

/**
 * Message class to toast when database methods are called during testing
 */
public class Message {
    /**
     * Send a message for testing
     * @param context current context
     * @param message the string to be displayed.
     */
    public static void message(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}