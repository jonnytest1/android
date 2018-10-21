package com.example.jonathan.ics.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.ProgressBar;

import com.example.jonathan.ics.Activities.settings.SettingsActivity;
import com.example.jonathan.ics.model.CalendarEventModel;
import com.example.jonathan.ics.repositories.DatabaseService;
import com.example.jonathan.ics.util.interfaceHandler;
import com.example.jonathan.ics.util.storage.Storage;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

public class MailHandler {

    private Context context;

    public MailHandler(Context context){
        this.context=context;
    }

    void handleMessages(Message message){
        Calendar cal=getFirstCalendarFromMessages(message);
        if(cal!=null){
            int calendarId=clear();
            saveCalender(cal,calendarId, null);
        }else{
            interfaceHandler.getInstance().getStorage().log("didnt get Calendar from Message");
        }
    }

    private boolean isMatchingCriteria(Message message,String basMail){
        try {
            return message.getFrom()[0].toString().toLowerCase().contains(basMail.toLowerCase())
                    && message.getSubject().contains("Kalender von")
                    && message.getContentType().contains("multipart");
        } catch (MessagingException e) {
            interfaceHandler.getInstance().getStorage().log(e);
            return false;
        }
    }

    private Calendar getFirstCalendarFromMessages(Message message) {
        System.out.println("checking mails");
        String basMail=interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.basMail);
        if(basMail==null){
            interfaceHandler.getInstance().note("basMail is null");
            return null;
        }
        try {
            if (isMatchingCriteria(message,basMail)) {
                Multipart multiPart = (Multipart) message.getContent();
                for (int x = 0; x < multiPart.getCount(); x++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(x);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        InputStream is = part.getInputStream();
                        CalendarBuilder builder=new CalendarBuilder();
                        return builder.build(is);
                    }
                }
                interfaceHandler.getInstance().getStorage().log("no attachment on mail");
                return null;
            }
        } catch (MessagingException | IOException e) {
            interfaceHandler.getInstance().getStorage().log("Messageing Exception in mail Handler");
            e.printStackTrace();
        }catch(Exception e){
            interfaceHandler.getInstance().getStorage().log("Other Exception in mail Handler");
            e.printStackTrace();
        }
        interfaceHandler.getInstance().note("emails not from correct mail or wrongly formated double Check basMail");
        return null;
    }
    public void saveCalender(Calendar calendar, int calID, ProgressBar progressBar)  {
        ComponentList calendarComponents = calendar.getComponents();
        System.out.println("saving to calender");
        DatabaseService databaseService=new DatabaseService(context);
        List<VEvent> calendarEvents=new ArrayList<>();
        for (Object c: calendarComponents) {
            if (c instanceof VEvent) {
                calendarEvents.add((VEvent) c);
            }
        }
        int amount=calendarEvents.size();
        for (int i=0;i<calendarEvents.size();i++) {
            CalendarEventModel cEvent=CalendarEventModel.parse(calendarEvents.get(i).toString());
            List<ContentValues> events = cEvent.getEvents(calID);
            for(ContentValues contentValue:events){
                if(cEvent.getReftime()!=null){
                    databaseService.update(contentValue,cEvent.getUuid(),cEvent.getReftime(),calID);
                }else {
                    Uri eventUri = databaseService.insertEvent(contentValue);
                    if(cEvent.needsReminders()){
                        for(ContentValues reminder:cEvent.getReminders(eventUri)){
                            databaseService.insertReminder(reminder);
                        }
                    }
                }
            }
            if(progressBar!=null){
                int percentForSaving=40;
                int percentBeforeSaving=60;
                double percentOfSaving=(double)i/(double)amount;
                int totalPercent=(int)(percentBeforeSaving+percentOfSaving*percentForSaving);
                progressBar.setProgress(totalPercent);
            }
        }
        System.out.println("finished calender");
        ContentResolver.setMasterSyncAutomatically(false);
        ContentResolver.setMasterSyncAutomatically(true);
    }

    public int clear() {
        System.out.println("clearing");

        String account= interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.account);
        if(account==null){
            interfaceHandler.getInstance().update(SettingsActivity.actions.OnError,"account not defined",context);
            return -1;
        }

        String calenderToDelete= interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.calender);
        if(calenderToDelete==null){
            interfaceHandler.getInstance().update(SettingsActivity.actions.OnError,"calender is not defined",context);
            return -1;
        }

        int calenderID= new DatabaseService(context).getCalendarID(account,calenderToDelete);
        if(calenderID!=-1) {
            new DatabaseService(context).deleteEntries(calenderID);

            ContentResolver.setMasterSyncAutomatically(false);
            ContentResolver.setMasterSyncAutomatically(true);
        }
        return calenderID;
    }

}
