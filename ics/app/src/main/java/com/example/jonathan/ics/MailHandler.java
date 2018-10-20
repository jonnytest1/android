package com.example.jonathan.ics;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import static com.example.jonathan.ics.MainActivity.EVENT_PROJECTION;

public class MailHandler {

    Context context;

    MailHandler(Context context){
        this.context=context;
    }

    void update(){
        Calendar now = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat("HH:mm:ss'\n'dd.MM.yyyy", Locale.GERMAN);
        String nowS=format.format(now.getTime());
        interfaceHandler.update(MainActivity.actions.finished,nowS,context);
    }
    void checkMails(Message[] messages) {
        System.out.println("checking mails");
        String basMail=interfaceHandler.get(MainActivity.vars.basMail,context);
        if(basMail==null){
            interfaceHandler.note("basMail is null",context);
            return;
        }
        for (Message message: messages) {
            try {
                if (message.getFrom()[0].toString().toLowerCase().contains(basMail.toLowerCase())
                            && message.getSubject().contains("Kalender von")
                                    && message.getContentType().contains("multipart")) {
                        Multipart multiPart = (Multipart) message.getContent();
                        for (int x = 0; x < multiPart.getCount(); x++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(x);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                InputStream is = part.getInputStream();
                                CalendarBuilder builder=new CalendarBuilder();
                                net.fortuna.ical4j.model.Calendar calendar=builder.build(is);
                                saveCalender(calendar,clear());
                                break;
                            }
                        }
                        return;
                }
            } catch (MessagingException | IOException e) {
                interfaceHandler.pushLog("Messageing Exception in mail Handler",context);
                e.printStackTrace();
            }catch(Exception e){
                interfaceHandler.pushLog("Other Exception in mail Handler",context);
                e.printStackTrace();
            }
        }
        interfaceHandler.note("emails not from correct mail or wrongly formated double Check basMail", context);
    }
    void saveCalender(net.fortuna.ical4j.model.Calendar calendar, int calID)  {
        ComponentList cs = calendar.getComponents();
        System.out.println("saving to calender");
        for (Object c: cs) {
            if (c instanceof VEvent) {
                CalendarEvent cEvent=CalendarEvent.parse(c.toString(),context);
                System.out.println("parsed");
                cEvent.save(calID,context);
            }
        }
        System.out.println("finished calender");
        ContentResolver.setMasterSyncAutomatically(false);
        //interfaceHandler.note("cleared mail folder");
        ContentResolver.setMasterSyncAutomatically(true);
    }

    int clear() {
        System.out.println("clearing");
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) )";
        String account= interfaceHandler.get(MainActivity.vars.account,context);

        if(account==null){
            interfaceHandler.update(MainActivity.actions.OnError,"account not defined",context);
            return -1;
        }

        String calenderToDelete= interfaceHandler.get(MainActivity.vars.calender,context);
        if(calenderToDelete==null){
            interfaceHandler.update(MainActivity.actions.OnError,"calender is not defined",context);
            return -1;
        }
        String[] selectionArgs = new String[]{account};

        Cursor cursor;
        cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), EVENT_PROJECTION, selection, selectionArgs, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if ( cursor.getCount() > 0) {
                while (!cursor.isAfterLast()) {
                    if (cursor.getString(cursor.getColumnIndex("calendar_displayName")).equals(calenderToDelete)) {

                        Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
                        int calenderID = cursor.getInt(cursor.getColumnIndex("_id"));
                        context.getContentResolver().delete(CALENDAR_URI, "calendar_id=" + calenderID, null);
                        ContentResolver.setMasterSyncAutomatically(false);
                        //interfaceHandler.note("cleared mail folder");
                        ContentResolver.setMasterSyncAutomatically(true);
                        return calenderID;
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        ContentResolver.setMasterSyncAutomatically(false);
        //interfaceHandler.note("cleared mail folder");
        ContentResolver.setMasterSyncAutomatically(true);
        return -1;
    }

    /*public void save(VEvent event,int calID,Context context){
        ContentResolver cr = context.getContentResolver();
        if(event.getSummary()==null){
            System.out.println(event.toString());
            ContentValues values = new ContentValues();
            try{
                values.put(CalendarContract.Events.DTSTART,event.getStartDate().getDate().getTime());
            }catch (NullPointerException e){

            }
            try{
                values.put(CalendarContract.Events.DTEND, event.getEndDate().getDate().getTime());
            }catch (NullPointerException e){

            }
            try{
                values.put(CalendarContract.Events.TITLE, event.getSummary().getValue());
            }catch (NullPointerException e){

            }
            try{
                values.put(CalendarContract.Events.ALL_DAY, !event.getStartDate().getDate().toString().contains("T"));
            }catch (NullPointerException e){

            }
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            Cursor crs =cr.query(CalendarContract.Events.CONTENT_URI,null,null,null,null);
            while(!crs.isLast()){
                crs.moveToNext();
                System.out.println(crs.getString(crs.getColumnIndex(CalendarContract.Events.UID_2445)));
            }
            String[] selectionArgs =new String[]{calID+"",event.getUid().getValue()};
            cr.update(CalendarContract.Events.CONTENT_URI,values,CalendarContract.Events.CALENDAR_ID+ " LIKE ? AND "+CalendarContract.Events.UID_2445 +" LIKE ?",selectionArgs);
        }else if(event.getSummary()!=null&&!event.getSummary().toString().contains("Abgesagt")) {
            ContentValues values = new ContentValues();

            values.put(CalendarContract.Events.DTSTART,event.getStartDate().getDate().getTime());
            values.put(CalendarContract.Events.DTEND, event.getEndDate().getDate().getTime());
            values.put(CalendarContract.Events.TITLE, event.getSummary().getValue());
            values.put(CalendarContract.Events.ALL_DAY, !event.getStartDate().getDate().toString().contains("T"));
            values.put(CalendarContract.Events.DESCRIPTION, "");
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "EST");
            values.put(CalendarContract.Events.UID_2445, event.getUid().getValue());
            if (PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)) {
                interfaceHandler.note("missing perm");
                return;
            }
            System.out.println("committing");
            cr.insert(CalendarContract.Events.CONTENT_URI, values);
        }
    }*/
}
