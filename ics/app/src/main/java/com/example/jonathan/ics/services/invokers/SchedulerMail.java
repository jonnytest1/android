package com.example.jonathan.ics.services.invokers;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import com.example.jonathan.ics.Activities.settings.SettingsActivity;
import com.example.jonathan.ics.services.MailServerService;
import com.example.jonathan.ics.util.interfaceHandler;

import java.util.Date;

public class SchedulerMail extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            interfaceHandler.getInstance().getStorage().log("scheduler: " + new Date().toLocaleString());
            startInNewThread(getBaseContext());
            return false;
        }catch(Exception e){
            interfaceHandler.getInstance().getStorage().log(e);
            return false;
        }
    }
    public static void startInNewThread(final Context context){
        new Thread(() -> {
            try {
                MailServerService mailServerService = new MailServerService(context);
                mailServerService.checkMails();
                interfaceHandler.getInstance().update(SettingsActivity.actions.finished, new Date().toLocaleString(), context);
            }catch(Exception e){
                interfaceHandler.getInstance().getStorage().log(e);
            }
        }).start();
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    static JobInfo createScheduledJob(Context context){
        ComponentName serviceComponent = new ComponentName(context,SchedulerMail.class );
        JobInfo.Builder builder=new JobInfo.Builder(1234,serviceComponent);
        int waitMin=15;
        // builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPeriodic(1000*60*waitMin,10000);
        return builder.build();
    }

    public static boolean registerScheduler(Context context){
        JobScheduler scheduler=context.getSystemService(JobScheduler.class);
        if(scheduler==null){
            interfaceHandler.getInstance().note("failed getting schedulerService");
            return false;
        }
        if(scheduler.getPendingJob(1234)==null){
            scheduler.schedule(createScheduledJob(context));
            return true;
        }else{
            scheduler.cancel(1234);
            scheduler.schedule(createScheduledJob(context));
            return true;
        }
    }

}
