package com.example.jonathan.ics;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeBodyPart;

public class Calendar2 extends IntentService implements Runnable{

    static boolean islistening=false;
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    @Override
    public void run() {

         start();
    }

    enum actions{
        check, com
    }

    public Calendar2() {

        super("Calender");
    }
    public Folder emailFolder;
    public MessageCountListener mCL=new MessageCountListener() {
        @Override
        public void messagesAdded(MessageCountEvent e) {
            interfaceHandler.note("new mails",getBaseContext());
            interfaceHandler.update(MainActivity.actions.started,"",getBaseContext());
            interfaceHandler.update(MainActivity.actions.OnError,"new Mails",getBaseContext());
            Message[] messages = e.getMessages();
            System.out.println("messages.length---" + messages.length);
            checkMails(messages);
            //close the store and folder objects
            update();
        }

        @Override
        public void messagesRemoved(MessageCountEvent e) {

        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }
    Thread t=null;
    ArrayList<Thread> threads=new ArrayList<>();
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        //interfaceHandler.note("onStartCommand",getBaseContext());
        final Calendar2 ref=this;
        t = new Thread(ref);
        threads.add(t);
        t.start();

        IntentFilter iFilter=new IntentFilter();
        for(actions i : actions.values()){
            iFilter.addAction(i.toString());
        }
        interfaceHandler.getIBM(getBaseContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(actions.check.toString())){
                    if(emailFolder!=null) {
                        emailFolder.removeMessageCountListener(mCL);
                    }
                    t = new Thread(ref);
                    threads.add(t);
                    t.start();
                }else if(action.equals(actions.com.toString())){
                    String instruction = intent.getStringExtra("value");
                    switch (instruction){
                        case "requestStatus":
                            interfaceHandler.update(MainActivity.actions.comR,instruction+"Return", islistening?"true":"false",getBaseContext());
                            break;
                    }
                }
            }
        },iFilter);


        return super.onStartCommand(intent, flags, startId);
    }

    void start(){
        registerMailListener(0);
    }

    private int clear() {
        System.out.println("clearing");
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) )";
        String account= interfaceHandler.get(MainActivity.vars.account,getBaseContext());

        if(account==null){
            interfaceHandler.update(MainActivity.actions.OnError,"account not defined",getBaseContext());
            return -1;
        }

        String calenderToDelete= interfaceHandler.get(MainActivity.vars.calender,getBaseContext());
        if(calenderToDelete==null){
            interfaceHandler.update(MainActivity.actions.OnError,"calender is not defined",getBaseContext());
            return -1;
        }
        String[] selectionArgs = new String[]{account};

        Context context = getApplicationContext();
        Cursor cursor;
        cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), EVENT_PROJECTION, selection, selectionArgs, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }


        if (cursor != null && cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex("calendar_displayName")).equals(calenderToDelete)) {

                    Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
                    int calenderID = cursor.getInt(cursor.getColumnIndex("_id"));
                    context.getContentResolver().delete(CALENDAR_URI, "calendar_id=" + calenderID, null);
                    return calenderID;
                }
                cursor.moveToNext();
            }
        }
        return -1;
    }

    void registerMailListener(int feedback) {
        System.out.println("started");
        System.out.println();
        String host = "imap.gmail.com";// change accordingly
        String user =  interfaceHandler.get(MainActivity.vars.mail,getBaseContext());
        String password = interfaceHandler.get(MainActivity.vars.password,getBaseContext());
        if(user==null||password==null){
            interfaceHandler.update(MainActivity.actions.OnError,"password or user is null",getBaseContext());
            return;
        }
        try {
            //create properties field
            Properties properties = new Properties();

            properties.put("mail.pop3.host", host);
            properties.put("mail.pop3.port", "993");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");
            System.out.println("sendnig request to host");
            store.connect(host, user, password);
            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);
            Message[] msg=emailFolder.getMessages();
            if(!msg[msg.length-1].getFlags().contains(Flags.Flag.SEEN)){
                checkMails(new Message[]{msg[msg.length-1]});
                update();
            }
            for(int i=msg.length-2;i>-1;i--){
                msg[i].getContent();
            }


            emailFolder.addMessageCountListener(mCL);
            //interfaceHandler.note("registered Listener",getBaseContext());
            islistening=true;
            interfaceHandler.update(MainActivity.actions.registered,"",getBaseContext());

            IdleThread idleThread = new IdleThread(emailFolder,user,password);
            idleThread.setDaemon(false);
            idleThread.start();
            idleThread.join();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            Log.i("MainActivity1", e.getMessage());
            interfaceHandler.note(e.getMessage(),getBaseContext());
        } catch (MessagingException e) {
            if(e.getMessage().contains("[AUTHENTICATIONFAILED]")){
                if(feedback==1){
                    interfaceHandler.note("wrong user credentials",getBaseContext());
                    interfaceHandler.update(MainActivity.actions.OnError,"wrong user Credentials",getBaseContext());
                }
            }else{
                interfaceHandler.note(e.getMessage(),getBaseContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("MainActivity1", "error" + e.getMessage());
            interfaceHandler.note(e.getMessage(),getBaseContext());
        }
    }

    void update(){
        Calendar now = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat("HH:mm:ss'\n'dd.MM.yyyy", Locale.GERMAN);
        String nowS=format.format(now.getTime());
        interfaceHandler.update(MainActivity.actions.finished,nowS,getBaseContext());
    }
    void checkMails(Message[] messages) {
        System.out.println("checking mails");
        int n = messages.length;
        String basMail=interfaceHandler.get(MainActivity.vars.basMail,getBaseContext());
        if(basMail==null){
            interfaceHandler.update(MainActivity.actions.OnError,"basMail is null",getBaseContext());
            return;
        }
        for (int i = n - 1; i >= 0; i--) {
            Message message = messages[i];
            try {
                if (message.getFrom()[0].toString().toLowerCase().contains(basMail.toLowerCase()) && message.getSubject().contains("Kalender von")) {
                    String contentType = message.getContentType();
                    if (contentType.contains("multipart")) {

                        Multipart multiPart = (Multipart) message.getContent();

                        for (int x = 0; x < multiPart.getCount(); x++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(x);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                InputStream is = part.getInputStream();

                                String str = "";
                                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                                str += s.hasNext() ? s.next() : "";

                                String[] events = str.split("BEGIN:VEVENT");

                                try {
                                    saveCalender(events,clear());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        //emailFolder.setFlags(messages,new Flags(Flags.Flag.SEEN),true);
                        return;
                    }
                }
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
                System.out.println(e);
            }
        }
        interfaceHandler.update(MainActivity.actions.OnError,"emails not from correct mail double Check basMail",getBaseContext());
    }
    void saveCalender(String[] events, int calID) throws ParseException {
        System.out.println("saving to calender");
        for (int j = 1; j < events.length; j++) {

            CalendarEvent cE=CalendarEvent.parse(events[j],j);
            cE.save(calID,getBaseContext());
        }
        System.out.println("finished calender");
    }
}

