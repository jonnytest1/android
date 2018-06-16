package com.example.jonathan.ics;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.example.jonathan.ics.Calendar2.EVENT_PROJECTION;

public class icsmanual extends AppCompatActivity {
    ProgressBar progressBAr=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icsmanual);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        progressBAr = (ProgressBar) findViewById(R.id.progressBar);
        if (Intent.ACTION_VIEW.equals(action))
        {
            Uri path=intent.getData();
            Calendar calendar=null;
            try {
                    InputStream is=getContentResolver().openInputStream(path);
                    CalendarBuilder builder=new CalendarBuilder();

                    try {
                        calendar=builder.build(is);
                    } catch (ParserException e) {
                        e.printStackTrace();
                    }
                progressBAr.setProgress(10);
            } catch (IOException e) {
                e.printStackTrace();
                interfaceHandler.note("Exception",e.getMessage());
            }
            try {
                try {
                    MailHandler mailHandler=new MailHandler(getBaseContext());
                    progressBAr.setProgress(40);
                    int calendarIndex=mailHandler.clear();
                    progressBAr.setProgress(60);
                    mailHandler.saveCalender(calendar,calendarIndex);
                    progressBAr.setProgress(100);
                } catch (ParseException e) {
                    progressBAr.setProgress(100);
                    progressBAr.setBackgroundColor(Color.RED);
                    e.printStackTrace();
                    interfaceHandler.note("Exception",e.getMessage());
                }

            }catch (Exception e){
                e.printStackTrace();
                interfaceHandler.note("Exception",e.getMessage());
            }
        }
    }
}
