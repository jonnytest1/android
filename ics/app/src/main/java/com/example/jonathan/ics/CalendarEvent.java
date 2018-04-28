package com.example.jonathan.ics;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

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

    long startMillis = 0;
    long endMillis = 0;

    Calendar beginTime = Calendar.getInstance();
    Calendar endTime = Calendar.getInstance();

    DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.GERMAN);
    DateFormat formatAllDay = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
    String title = "";
    String repType = null;
    int repAMount = 1;
    int repInterval = 1;
    boolean allDay = false;
    String repDays = null;

    CalendarEvent(){

    }

    static CalendarEvent parse(String eventInICS,int j) {
        String[] eventattributes = eventInICS.split("\r\n");
        CalendarEvent cE=new CalendarEvent();
        for (int i3 = 0; i3 < eventattributes.length; i3++) {
            System.out.println("event:" + j + "att:" + i3);
            String att = eventattributes[i3];
            if (att.contains("DTSTART")) {
                String Stime = att.split(":")[1].replace("Z", "");
                Date Sdate=null;
                if (!Stime.contains("T")) {
                    cE.setAllDay(true);
                    try {
                        Sdate = cE.formatAllDay.parse(Stime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Sdate = cE.format.parse(Stime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                cE.beginTime.setTime(Sdate);
                cE.startMillis = cE.beginTime.getTimeInMillis();
            } else if (att.contains("DTEND")) {
                String Etime = att.split(":")[1].replace("Z", "");
                Date Edate=null;
                if (!Etime.contains("T")) {
                    try {
                        Edate = cE.formatAllDay.parse(Etime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Edate = cE.format.parse(Etime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                cE.endTime.setTime(Edate);
                cE.endMillis = cE.endTime.getTimeInMillis();
            } else if (att.contains("SUMMARY")) {
                cE.title = att.split("LANGUAGE=de:")[1];
            } else if (att.contains("RRULE:")) {
                String[] subatts = att.split(":")[1].split(";");
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
            }
        }
        return  cE;
    }
    public void toData(int calID, Context context){
        ContentValues values = new ContentValues();
        ContentResolver cr =  context.getContentResolver();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE,title);
        values.put(CalendarContract.Events.ALL_DAY,allDay);
        values.put(CalendarContract.Events.DESCRIPTION, "");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "EST");
        if (PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)) {
            return;
        }
        System.out.println("committing");
        cr.insert(CalendarContract.Events.CONTENT_URI, values);
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
                if(repType.equals("DAILY")){
                    addition=repInterval*1000*60*60*24;
                }else if(repType.equals("WEEKLY")){
                    if(repDays!=null){
                        addition =repInterval*1000*60*60*24;
                    }else {
                        addition = repInterval * 1000 * 60 * 60 * 24 * 7;
                    }
                }else{
                    System.out.println("unimplemented repetion type"+repType);
                }
                startMillis+=addition;
                endMillis+=addition;
            }
        }
        System.out.println("finished this event");
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

    public DateFormat getFormat() {
        return format;
    }

    public void setFormat(DateFormat format) {
        this.format = format;
    }

    public DateFormat getFormatAllDay() {
        return formatAllDay;
    }

    public void setFormatAllDay(DateFormat formatAllDay) {
        this.formatAllDay = formatAllDay;
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

    public int getRepAMount() {
        return repAMount;
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
