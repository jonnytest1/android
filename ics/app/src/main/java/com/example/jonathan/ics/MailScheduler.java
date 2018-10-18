package com.example.jonathan.ics;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import java.util.Date;

public class MailScheduler extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        interfaceHandler.push(MainActivity.vars.log,"scheduler: "+new Date().toLocaleString(),getBaseContext());
        startInNewThread(getBaseContext());
        return false;
    }
    public static void startInNewThread(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkHandler networkHandler = new NetworkHandler(context);
                    networkHandler.connect();
                    interfaceHandler.update(MainActivity.actions.finished, new Date().toLocaleString(), context);
                }catch(Exception e){
                    interfaceHandler.push(MainActivity.vars.log,e.getMessage(),context);
                }
            }
        }).start();
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    static JobInfo createScheduledJob(Context context){
        ComponentName serviceComponent = new ComponentName(context,MailScheduler.class );
        JobInfo.Builder builder=new JobInfo.Builder(1234,serviceComponent);
        int waitMin=15;
        // builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPeriodic(1000*60*waitMin,10000);
        return builder.build();
    }

    public static boolean registerScheduler(Context context){
        JobScheduler scheduler=context.getSystemService(JobScheduler.class);
        if(scheduler==null){
            interfaceHandler.note("failed getting schedulerService",context);
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
