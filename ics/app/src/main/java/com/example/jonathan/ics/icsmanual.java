package com.example.jonathan.ics;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class icsmanual extends AppCompatActivity {
    ProgressBar progressBAr=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icsmanual);

        Intent intent = getIntent();
        String action = intent.getAction();
        progressBAr = findViewById(R.id.progressBar);
        if (Intent.ACTION_VIEW.equals(action)&&intent.getType().equals("text/calendar"))
        {
            progressBAr.setProgress(1);
            Uri path=intent.getData();
            Calendar calendar=null;
            try {
                //&&path.toString().endsWith(".ics") gerts saved to inernal file with random name
                if(path!=null) {
                    InputStream is = getContentResolver().openInputStream(path);
                    if(is!=null) {
                        StringBuilder cal = new StringBuilder();
                        int t = is.read();
                        while (t != -1) {
                            cal.append(String.valueOf((char) t));
                            t = is.read();
                        }
                        CalendarBuilder builder = new CalendarBuilder();

                        try {
                            calendar = builder.build(new ByteArrayInputStream(cal.toString().getBytes(StandardCharsets.UTF_8)));
                        } catch (ParserException e) {
                            progressBAr.setBackgroundColor(Color.RED);
                            e.printStackTrace();
                            interfaceHandler.note("Exception", e.getMessage(),getBaseContext());
                        }
                        progressBAr.setProgress(10);
                    }
                    MailHandler mailHandler=new MailHandler(getBaseContext());
                    progressBAr.setProgress(40);
                    int calendarIndex=mailHandler.clear();
                    progressBAr.setProgress(60);
                    mailHandler.saveCalender(calendar,calendarIndex);
                    progressBAr.setProgress(100);
                }
            } catch (IOException e) {
                e.printStackTrace();
                interfaceHandler.note("Exception",e.getMessage(),getBaseContext());
            }catch (Exception e){
                progressBAr.setBackgroundColor(Color.RED);
                e.printStackTrace();
                interfaceHandler.note("Exception",e.getMessage(),getBaseContext());
            }
        }
    }
}
