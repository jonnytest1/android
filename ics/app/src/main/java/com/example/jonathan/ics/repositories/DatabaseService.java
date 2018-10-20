package com.example.jonathan.ics.repositories;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.example.jonathan.ics.util.interfaceHandler;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class DatabaseService {

    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };


    Context context;


    ContentResolver contentResolver;
   public  DatabaseService(Context context){
       this.context=context;
       contentResolver= context.getContentResolver();

   }


    @NonNull
    public ArrayList<String> getAccounts() {
        final String[] filter= new String[]{CalendarContract.Calendars.ACCOUNT_NAME};
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),filter, null, null, null, null);
        ArrayList<String> accounts= new ArrayList<>();
        if (cursor != null) {
            while(cursor.moveToNext()){
                String accountName=cursor.getString(0);
                if(!accounts.contains(accountName)) {
                    accounts.add(accountName);
                }
            }
            cursor.close();
        }
        return accounts;
    }

    @NonNull
    public  ArrayList<String> getCalendarsForAccount(String acct) {
        final String[] filter = new String[]{CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"), filter, null, null, null, null);
        ArrayList<String> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String accN = cursor.getString(0);
                String calenderS = cursor.getString(1);
                if (!list.contains(calenderS) && accN.equals(acct)) {
                    list.add(calenderS);
                }
            }
            cursor.close();
        }
        return list;
    }

   public void update(ContentValues values, String refUUID, Calendar refTimeBegin, int refCalenderID){
       if (PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)) {
           interfaceHandler.note("missing permission to write calendar");
           interfaceHandler.getStorage().log("missing permission to write calendar");
           return;
       }
       String[] selectionArgs =new String[]{refCalenderID+"",refUUID,refTimeBegin.getTimeInMillis()+""};
       contentResolver.update(CalendarContract.Events.CONTENT_URI,values,CalendarContract.Events.CALENDAR_ID+ " LIKE ? AND "
               + CalendarContract.Events.UID_2445 +" LIKE ? AND "
               + CalendarContract.Events.DTSTART +" LIKE ? ",selectionArgs);
   }


   public Uri insertEvent(ContentValues values){
      return insert(CalendarContract.Events.CONTENT_URI, values);
   }
    public Uri insertReminder(ContentValues values){
        return insert(CalendarContract.Reminders.CONTENT_URI,values);
    }

    private Uri insert(Uri uri,ContentValues values){
        if (PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)) {
            interfaceHandler.note("missing permission to write calendar");
            interfaceHandler.getStorage().log("missing permission to write calendar");
            return null;
        }
        return contentResolver.insert(uri,values);
    }


    public int getCalendarID(String account,String calendarName ){

       String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) )";
       String[] selectionArgs = new String[]{account};

       Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"), EVENT_PROJECTION, selection, selectionArgs, null, null);
       if (cursor != null) {
           cursor.moveToFirst();
           if (cursor.getCount() > 0) {
               while (!cursor.isAfterLast()) {
                   if (cursor.getString(cursor.getColumnIndex("calendar_displayName")).equals(calendarName)) {
                       return cursor.getInt(cursor.getColumnIndex("_id"));
                   }
                   cursor.moveToNext();
               }
           }
           cursor.close();
       }
       return -1;
   }

   public void deleteEntries(String account,String calendarID){
       int calId=getCalendarID(account,calendarID);
       deleteEntries(calId);
   }

   public void deleteEntries(int calendarID){
       final Uri EVENTS_URI = Uri.parse("content://com.android.calendar/events");

       final String where = "calendar_id=?";String[] selectionArgs = new String[]{calendarID+""};

       contentResolver.delete(EVENTS_URI, where, selectionArgs);

       interfaceHandler.getStorage().log("cleared edit calender");

       ContentResolver.setMasterSyncAutomatically(false);
       ContentResolver.setMasterSyncAutomatically(true);
   }

}
