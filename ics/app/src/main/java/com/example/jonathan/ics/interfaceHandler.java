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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        Snackbar.make(activity.findViewById(R.id.toolbar), msg,Snackbar.LENGTH_LONG).show();
    }
    public static void show(String msg, View.OnClickListener okButton,Activity activity){
        Snackbar.make(activity.findViewById(R.id.toolbar), msg,Snackbar.LENGTH_LONG).setAction("OK",okButton).show();
    }

    public static void write(MainActivity.vars key, String value, Context context){
        setSp(context);
        if(key.equals(MainActivity.vars.log)) {
            update(MainActivity.actions.logUpdate,"",context);
        }
        SharedPreferences.Editor editor = sP.edit();
        editor.putString(key.toString(),value);

        editor.commit();
        editor.apply();
    }
    public static void pushLog(LoggingElement value,Context context){
        List<LoggingElement> currentLog=getLog(context);
        Collections.reverse(currentLog);
        currentLog.add(value);
        Collections.reverse(currentLog);
        write(MainActivity.vars.log,new Gson().toJson(currentLog),context);
    }
    public static void pushLog(String value,Context context){
        pushLog(new LoggingElement(value),context);
    }
    public static void pushLog(Throwable value,Context context){
        pushLog(new LoggingElement(value),context);
    }

    public static List<LoggingElement> getLog(Context context){
        String logString=get(MainActivity.vars.log,context);
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<LoggingElement> list=mapper.readValue(logString,new TypeReference<List<LoggingElement>>(){});
            if(list.size()>0&& !(list.get(0) instanceof  LoggingElement)){
                return new ArrayList<>();
            }
            return (List<LoggingElement>) list;
        } catch (IOException | NullPointerException |ClassCastException e) {
           return new ArrayList<>();
        }
    }

    public static String get(MainActivity.vars key,Context context){
        setSp(context);
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
        int notificationID=200;
        if(mNotificationManager!=null){
            if(!text.equals("wrong input type for write")) {
                pushLog("note_ " + text, context);
            }
            mNotificationManager.notify(notificationID, mBuilder.build());
        }else{
            pushLog("noteMAnage is null ?!",context);
        }
    }

    public static void update(Enum action ,String value,Context context){
        if(lBM==null){
            lBM=LocalBroadcastManager.getInstance(context);
        }
        lBM.sendBroadcast(new Intent(action.toString()).putExtra("value",value));
    }

    private static void setSp(Context context){
        if(sP==null){
            sP=context.getApplicationContext().getSharedPreferences("data",Context.MODE_PRIVATE);
        }
    }

    public static LocalBroadcastManager getIBM(Context context){
        if(lBM==null){
            lBM=LocalBroadcastManager.getInstance(context);
        }
        return lBM;
    }
}
