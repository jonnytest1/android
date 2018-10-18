package com.example.jonathan.ics;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Jonathan on 28.04.2018.
 */

public class interfaceHandler {


    private static SharedPreferences sP;

    private static LocalBroadcastManager lBM;


    public static void show(String msg,Activity activity){
        Snackbar snack = Snackbar.make(activity.findViewById(R.id.coordL),msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snack.show();
        //Snackbar.make(findViewById(R.id.toolbar), msg,Snackbar.LENGTH_LONG).show();
    }
    public static void show(String msg, View.OnClickListener okButton,Activity activity){
        Snackbar.make(activity.findViewById(R.id.toolbar), msg,Snackbar.LENGTH_LONG).setAction("OK",okButton).show();
    }

    public static void write(MainActivity.vars key, Object value, Context context){
        if(sP==null){
            sP=context.getSharedPreferences("data", Context.MODE_PRIVATE);
        }
        if(key.equals(MainActivity.vars.log)) {
            update(MainActivity.actions.logUpdate,"",context);
        }else{
           // push(MainActivity.vars.log, "saved " + key + " to " + value, context);
        }
        SharedPreferences.Editor editor = sP.edit();
        if(value instanceof String) {
            editor.putString(key.toString(),(String) value);
        }else if(value instanceof Set<?>){
            Set<?> set=(Set<?>) value;
            editor.putStringSet(key.toString(),set.stream().map(t->((Object) t).toString()).collect(Collectors.toSet()));
        }else {
            note("wrong input type for write",context);
        }

        editor.apply();
    }
    public static void push(Throwable value,Context context){
        MainActivity.vars key=MainActivity.vars.log;
        Set<String> currentLog=getLog(context);
        currentLog.add(value.getMessage()+"\n"+Arrays.stream(value.getStackTrace()).map(sE->sE.toString()).collect(Collectors.joining("\n")));
        write(key,currentLog,context);

    }
    public static void push(MainActivity.vars key, String value,Context context){
        String now= Arrays.stream(get(key,context).split("\n")).limit(20).collect(Collectors.joining("\n"));
        if(now==null){
            write(key,value,context);
        }else{
            write(key,value+"\n"+now,context);
        }
    }

    public static Set<String> getLog(Context context){
        if(sP==null){
            sP=context.getSharedPreferences("data",Context.MODE_PRIVATE);
        }
        return sP.getStringSet(MainActivity.vars.log.toString(),new HashSet<>());
    }

    public static String get(MainActivity.vars key,Context context){
        if(sP==null){
            sP=context.getSharedPreferences("data",Context.MODE_PRIVATE);
        }
        return sP.getString(key.toString(),null);
    }
    public  static  void note(String text,Context context){
        note(text,"",context);
    }
    public static void note(String text,String text2,Context context){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(text);
        mBuilder.setSmallIcon(R.drawable.ic_android_black_24dp);
        mBuilder.setContentText(text2);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(text2));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        int notificationID=200;
        if(mNotificationManager!=null){
            push(MainActivity.vars.log,"note_ "+text,context);
            mNotificationManager.notify(notificationID, mBuilder.build());
        }else{
            push(MainActivity.vars.log,"noteMAnage is null ?!",context);
        }
    }
    public static void update(Enum action ,String value,Context context){
        if(lBM==null){
            lBM=LocalBroadcastManager.getInstance(context);
        }
        lBM.sendBroadcast(new Intent(action.toString()).putExtra("value",value));
    }

    public static LocalBroadcastManager getIBM(Context context){
        if(lBM==null){
            lBM=LocalBroadcastManager.getInstance(context);
        }
        return lBM;
    }

}
