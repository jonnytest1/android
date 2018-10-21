package com.example.jonathan.ics.model;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract;

import com.example.jonathan.ics.util.interfaceHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Jonathan on 27.04.2018.
 */

public class CalendarEventModel {

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
    private CalendarEventModel(){

    }

    public static CalendarEventModel parse(String eventInICS) {
        String[] eventattributes = eventInICS.split("\r\n");
        CalendarEventModel cE=new CalendarEventModel();
        try{
            for (String att : eventattributes) {
                if (att.contains("DTSTART")) {
                    cE.beginTime.setTime(parseDate(att.split(":")[1]));
                    if (!att.split(":")[1].contains("T")) {
                        cE.allDay = true;
                    }
                    cE.startMillis = cE.beginTime.getTimeInMillis();
                } else if (att.contains("DTEND")) {
                    if (!att.split(":")[1].contains("T")) {
                        cE.allDay = true;
                    }
                    cE.endTime.setTime(parseDate(att.split(":")[1]));
                    cE.endMillis = cE.endTime.getTimeInMillis();
                } else if (att.contains("RECURRENCE-ID")) {
                    if (!att.split(":")[1].contains("T")) {
                        cE.allDay = true;
                    }
                    cE.reftime = Calendar.getInstance();
                    cE.reftime.setTime(parseDate(att.split(":")[1]));
                } else if (att.contains("SUMMARY")) {
                    // System.out.println(att);
                    cE.title = att.replace("SUMMARY;", "").replace("SUMMARY:", "").replace("LANGUAGE=de:", "");
                } else if (att.contains("UID")) {
                    // System.out.println(att);
                    cE.uuid = att.split("UID:")[1];
                } else if (att.contains("RRULE:")) {
                    String[] subatts = att.split(":")[1].split(";");
                    try {
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
                    } catch (IndexOutOfBoundsException ioobe) {
                        ioobe.printStackTrace();
                        interfaceHandler.getInstance().getStorage().log(ioobe);
                    }
                }
            }
        }catch(IndexOutOfBoundsException ioobe){
            ioobe.printStackTrace();
            interfaceHandler.getInstance().getStorage().log(ioobe);
        }
        return  cE;
    }
    private List<ContentValues> toData(int calID ){

        List<ContentValues> eventList=new ArrayList<>();
        if(uuid!=null&&!"".equals(uuid)&& reftime!=null){
            ContentValues values = new ContentValues();
            try{
                values.put(CalendarContract.Events.DTSTART,startMillis);
            }catch (NullPointerException ignored){

            }
            try{
                values.put(CalendarContract.Events.DTEND, endMillis);
            }catch (NullPointerException ignored){

            }
            try{
                values.put(CalendarContract.Events.ALL_DAY, allDay);
            }catch (NullPointerException ignored){

            }
            eventList.add(values);
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
            eventList.add(values);
        }
        return eventList;
    }

    public List<ContentValues> getReminders(Uri uri) {
        List<ContentValues> eventList=new ArrayList<>();
        ContentValues reminder = new ContentValues();
        reminder.put(CalendarContract.Reminders.MINUTES,60);
        reminder.put(CalendarContract.Reminders.EVENT_ID,Objects.requireNonNull(uri.getPath()).replace("/events/",""));
        reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        eventList.add(reminder);

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
        eventList.add(reminder2);
        return eventList;
    }
    public boolean needsReminders(){
        return beginTime.get(Calendar.HOUR_OF_DAY)<10 &&
                !title.equals("Ausbildungsnachweis")&&
                !title.equals("PrintKalender")&& !allDay;
    }

    public List<ContentValues> getEvents(int calID){
        List<ContentValues> events=new ArrayList<>();
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
                    events.addAll(toData(calID));
                }else{
                    i4--;
                }
            }else{
                events.addAll(toData(calID));
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
                        interfaceHandler.getInstance().getStorage().log("unimplemented repetion type" + repType);
                        break;
                }
                startMillis+=addition;
                endMillis+=addition;
            }
        }
        return events;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUuid() {
        return uuid;
    }

    public Calendar getReftime() {
        return reftime;
    }
}
