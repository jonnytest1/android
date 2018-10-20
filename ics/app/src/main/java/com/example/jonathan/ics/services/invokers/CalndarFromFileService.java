package com.example.jonathan.ics.services.invokers;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.example.jonathan.ics.Activities.CustomActivity;
import com.example.jonathan.ics.Activities.log.LogActivity;
import com.example.jonathan.ics.R;
import com.example.jonathan.ics.services.MailHandler;
import com.example.jonathan.ics.util.interfaceHandler;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class CalndarFromFileService extends CustomActivity {
    ProgressBar progressBar =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icsmanual);

        Intent intent = getIntent();
        String action = intent.getAction();
        progressBar = findViewById(R.id.progressBar);
        if (Intent.ACTION_VIEW.equals(action)&&intent.getType().equals("text/calendar"))
        {
            progressBar.setProgress(1);
            Uri path=intent.getData();
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

                        final Calendar calendar = builder.build(new ByteArrayInputStream(cal.toString().getBytes(StandardCharsets.UTF_8)));
                        progressBar.setProgress(10);
                        new Thread(()->{
                            MailHandler mailHandler=new MailHandler(getBaseContext());
                            progressBar.setProgress(40);
                            int calendarIndex=mailHandler.clear();
                            progressBar.setProgress(60);
                            mailHandler.saveCalender(calendar,calendarIndex, progressBar);
                            progressBar.setProgress(100);
                            interfaceHandler.getStorage().log("completed calendar");
                            gotoClass(LogActivity.class);
                        }).start();
                    }
                }
            } catch (ParserException e) {
                progressBar.setBackgroundColor(Color.RED);
                e.printStackTrace();
                interfaceHandler.getStorage().log(e);
            } catch (IOException e) {
                interfaceHandler.getStorage().log(e);
                e.printStackTrace();
            }catch (Exception e){
                progressBar.setBackgroundColor(Color.RED);
                e.printStackTrace();
                interfaceHandler.getStorage().log(e);
            }
        }
    }
}
