package com.example.jonathan.ics;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Calendar2 extends IntentService implements Runnable{




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
        check, com,test
    }
    public Calendar2() {
        super("");
    }

    public NetworkHandler networkHandler =null;


    Thread t=null;
    ArrayList<Thread> threads=new ArrayList<>();
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        //interfaceHandler.note("onStartCommand",getBaseContext());

        networkHandler =new NetworkHandler(getBaseContext());
        final Calendar2 ref=this;
        t = new Thread(ref);
        t.setName("calendarViaStartCommand");
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

                if(actions.check.toString().equals(action)){
                    networkHandler.removeListener();
                    t = new Thread(ref);
                    t.setName("calendarViaReceive");
                    threads.add(t);
                    t.start();
                }else if(actions.com.toString().equals(action)){
                    String instruction = intent.getStringExtra("value");
                    switch (instruction){
                        case "requestStatus":
                            interfaceHandler.update(MainActivity.actions.comR,instruction+"Return", networkHandler.islistening?"true":"false",getBaseContext());
                            break;
                    }
                }else if(actions.test.toString().equals(action)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Folder f1 = networkHandler.store.getFolder("INBOX");
                                f1.open(Folder.READ_WRITE);

                                if(networkHandler.mostRecentMessages==null) {
                                    File f = new File(getCacheDir() + "/cal.ics");
                                    if (!f.exists()) try {
                                        InputStream is = getAssets().open("cal.ics");
                                        int size = is.available();
                                        byte[] buffer = new byte[size];
                                        is.read(buffer);
                                        is.close();


                                        FileOutputStream fos = new FileOutputStream(f);
                                        fos.write(buffer);
                                        fos.close();
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    final MimeMessage mm = new MimeMessage((Session) null);
                                    mm.setText("Test content.");
                                    String basMAil = interfaceHandler.get(MainActivity.vars.basMail, getBaseContext());
                                    mm.setFrom(new InternetAddress(basMAil));
                                    mm.setRecipients(Message.RecipientType.TO, "test@example.com");

                                    Multipart multipart = new MimeMultipart();
                                    MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                                    String fileName = "cal.ics";
                                    String path = f.getPath();
                                    //  /data/user/0/com.example.jonathan.ics/cache/cal.ics
                                    DataSource source = new FileDataSource(path); // ex : "C:\\test.pdf"
                                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                                    attachmentBodyPart.setFileName(fileName); // ex : "test.pdf"

                                    multipart.addBodyPart(attachmentBodyPart); // add the attachement part

                                    mm.setContent(multipart);
                                    final String subjectString = "test message. Kalender von";

                                    mm.setSubject(subjectString);
                                    final Message[] messages = new Message[]{
                                            mm
                                    };

                                    f1.appendMessages(messages);
                                }else{
                                    f1.appendMessages(networkHandler.mostRecentMessages);
                                }
                            } catch (MessagingException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        },iFilter);
        startForeground(-22331,new Notification());
        return START_STICKY;
    }



    @Override
    public void onDestroy(){
        interfaceHandler.note("destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        startForeground(1234,new Notification());
    }

    void start(){
        networkHandler.registerMailListener(0);
    }


}

