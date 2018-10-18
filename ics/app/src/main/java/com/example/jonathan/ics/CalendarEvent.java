package com.example.jonathan.ics;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by Jonathan on 27.04.2018.
 */

public class CalendarEvent {

    private long startMillis = 0;
    private long endMillis = 0;

    private Calendar beginTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();


    private String title = "";
    private String repType = null;
    private int repAMount = 1;
    private int repInterval = 1;
    private boolean allDay = false;
    private String repDays = null;
    private String uuid=null;
    private Calendar reftime=null;
    private CalendarEvent(){

    }

    static CalendarEvent parse(String eventInICS,Context context) {
        String[] eventattributes = eventInICS.split("\r\n");
        CalendarEvent cE=new CalendarEvent();
        try{
            for (int i3 = 0; i3 < eventattributes.length; i3++) {
                String att = eventattributes[i3];
                if (att.contains("DTSTART")) {
                    cE.beginTime.setTime(parseDate(att.split(":")[1]));
                    if(!att.split(":")[1].contains("T")){
                        cE.allDay=true;
                    }
                    cE.startMillis = cE.beginTime.getTimeInMillis();
                } else if (att.contains("DTEND")) {
                    if(!att.split(":")[1].contains("T")){
                        cE.allDay=true;
                    }
                    cE.endTime.setTime(parseDate(att.split(":")[1]));
                    cE.endMillis = cE.endTime.getTimeInMillis();
                } else if (att.contains("RECURRENCE-ID")) {
                    if(!att.split(":")[1].contains("T")){
                        cE.allDay=true;
                    }
                    cE.reftime=Calendar.getInstance();
                    cE.reftime.setTime(parseDate(att.split(":")[1]));
                } else if (att.contains("SUMMARY")) {
                   // System.out.println(att);
                    cE.title = att.replace("SUMMARY;","").replace("SUMMARY:","").replace("LANGUAGE=de:","");
                } else if (att.contains("UID")) {
                   // System.out.println(att);
                    cE.uuid = att.split("UID:")[1];
                } else if (att.contains("RRULE:")) {
                    String[] subatts = att.split(":")[1].split(";");
                    try{
                        for (String t : subatts) {
                            if (t.contains("FREQ")) {
                                cE.repType = t.split("FREQ=")[1];
                            } else if (t.contains("COUNT")) {
                                cE.repAMount = Integer.parseInt(t.split("COUNT=")[1]);
                            } else if (t.contains("INTERVAL")) {
                                cE.repInterval = Integer.parseInt(t.split("INTERVAL=")[1]);
                            } else if (t.contains("BYDAY")) {
                                cE.repDays = t.split("BYDAY=")[1];
                            }
                        }
                    }catch(IndexOutOfBoundsException ioobe){
                        ioobe.printStackTrace();
                        interfaceHandler.note("Exception","calender :  "+ ioobe.getMessage(),context);
                    }
                }
            }
        }catch(IndexOutOfBoundsException ioobe){
            ioobe.printStackTrace();
            interfaceHandler.note("Exception","calender : "+" "+ ioobe.getMessage(),context);
        }
        return  cE;
    }
    private void toData(int calID, Context context){
        ContentResolver cr = context.getContentResolver();
        if(uuid!=""&&uuid!=null&&reftime!=null){
            ContentValues values = new ContentValues();
            try{
                values.put(CalendarContract.Events.DTSTART,startMillis);
            }catch (NullPointerException e){

            }
            try{
                values.put(CalendarContract.Events.DTEND, endMillis);
            }catch (NullPointerException e){

            }
            try{
                values.put(CalendarContract.Events.ALL_DAY, allDay);
            }catch (NullPointerException e){

            }

            Cursor crs=cr.query(CalendarContract.Events.CONTENT_URI,null,CalendarContract.Events.UID_2445 +" LIKE ?",new String[]{uuid},null);
            int count=0;
            while(crs.moveToNext()){
                count++;
            }
            String[] columns=crs.getColumnNames();
            for(String column:columns){
              //  System.out.println(column+" "+crs.getString(crs.getColumnIndex(column)));
            }
            System.out.println("amount : "+count);
            String[] selectionArgs =new String[]{calID+"",uuid,reftime.getTimeInMillis()+""};
            cr.update(CalendarContract.Events.CONTENT_URI,values,CalendarContract.Events.CALENDAR_ID+ " LIKE ? AND "
                    + CalendarContract.Events.UID_2445 +" LIKE ? AND "
                    + CalendarContract.Events.DTSTART +" LIKE ? ",selectionArgs);


        }
        if(!title.contains("Abgesagt")&& reftime==null) {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.ALL_DAY, allDay);
            values.put(CalendarContract.Events.DESCRIPTION, "");
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "EST");

            values.put(CalendarContract.Events.UID_2445, uuid);
            if (PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)) {
                interfaceHandler.note("missing perm",context);
                return;
            }
            System.out.println("committing");
            Uri uri= cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if(beginTime.get(Calendar.HOUR_OF_DAY)<10&&!title.equals("Ausbildungsnachweis")&&!title.equals("PrintKalender")&&allDay==false){
                ContentValues reminder = new ContentValues();
                reminder.put(CalendarContract.Reminders.MINUTES,60);
                reminder.put(CalendarContract.Reminders.EVENT_ID,uri.getPath().replace("/events/",""));
                reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                cr.insert(CalendarContract.Reminders.CONTENT_URI,reminder);

                Calendar dayBefore=Calendar.getInstance();
                dayBefore.setTimeInMillis(startMillis);
                dayBefore.set(Calendar.DAY_OF_YEAR,dayBefore.get(Calendar.DAY_OF_YEAR)-1);
                dayBefore.set(Calendar.HOUR_OF_DAY,22);

                long difference= beginTime.getTimeInMillis()-dayBefore.getTimeInMillis();
                int minutes=Math.round(difference/(60*1000));


                ContentValues reminder2 = new ContentValues();
                reminder2.put(CalendarContract.Reminders.MINUTES,minutes);
                reminder2.put(CalendarContract.Reminders.EVENT_ID,uri.getPath().replace("/events/",""));
                reminder2.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                cr.insert(CalendarContract.Reminders.CONTENT_URI,reminder2);

            }
        }
    }
    public void save(int calID, Context context){
        for(int i4=0;i4<repAMount;i4++){
            if(repDays!=null){
                Calendar day= Calendar.getInstance();
                day.setTimeInMillis(startMillis);
                int today=day.get(Calendar.DAY_OF_WEEK);
                if((repDays.contains("MO")&&today==Calendar.MONDAY)||
                        (repDays.contains("TU")&&today==Calendar.TUESDAY)||
                        (repDays.contains("WE")&&today==Calendar.WEDNESDAY)||
                        (repDays.contains("TH")&&today==Calendar.THURSDAY)||
                        (repDays.contains("FR")&&today==Calendar.FRIDAY)){
                    toData(calID,context);
                }else{
                    i4--;
                }
            }else{
                toData(calID,context);
            }

            if(repType!=null){
                int addition=0;
                switch (repType) {
                    case "DAILY":
                        addition = repInterval * 1000 * 60 * 60 * 24;
                        break;
                    case "WEEKLY":
                        if (repDays != null) {
                            addition = repInterval * 1000 * 60 * 60 * 24;
                        } else {
                            addition = repInterval * 1000 * 60 * 60 * 24 * 7;
                        }
                        break;
                    case "MONTHLY":
                        //not saving so far anyways
                        //addition = repInterval * 1000 * 60 * 60 * 24*31;
                        break;
                    default:
                        interfaceHandler.note("unimplemented repetion type" + repType,context);
                        break;
                }
                startMillis+=addition;
                endMillis+=addition;
            }
        }
        System.out.println("finished this event");
    }
    private static Date parseDate(String dateStr){
        DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.GERMAN);
        DateFormat formatWithZone = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.GERMAN);
        DateFormat formatAllDay = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
        if (!dateStr.contains("T")) {
            try {
                return formatAllDay.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            dateStr=dateStr.replace("Z","UTC");
            try {
                return formatWithZone.parse(dateStr);
            } catch (ParseException e1) {
                try {
                    return format.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }

    public Calendar getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Calendar beginTime) {
        this.beginTime = beginTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRepType() {
        return repType;
    }

    public void setRepType(String repType) {
        this.repType = repType;
    }


    public void setRepAMount(int repAMount) {
        this.repAMount = repAMount;
    }

    public int getRepInterval() {
        return repInterval;
    }

    public void setRepInterval(int repInterval) {
        this.repInterval = repInterval;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getRepDays() {
        return repDays;
    }

    public void setRepDays(String repDays) {
        this.repDays = repDays;
    }
}
