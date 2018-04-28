package com.example.jonathan.ics;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;

/**
 * Created by Jonathan on 28.04.2018.
 */

public class interfaceHandler {


    static SharedPreferences sP;

    static LocalBroadcastManager lBM;

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
    public static void write(MainActivity.vars key, String value,Context context){
        if(sP==null){
            sP=context.getSharedPreferences("data", Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sP.edit();
        editor.putString(key.toString(),value);
        editor.commit();
        try {
            show("saved " + key + " to " + value, (Activity) context);
        }catch (ClassCastException e){
            return;
        }
    }
    public static String get(MainActivity.vars key,Context context){
        if(sP==null){
            sP=context.getSharedPreferences("data",Context.MODE_PRIVATE);
        }
        return sP.getString(key.toString(),null);
    }
    public static void note(String text,Context context){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(text);
        mBuilder.setSmallIcon(R.drawable.ic_android_black_24dp);
        mBuilder.setContentText("yay");
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        int notificationID=200;
        mNotificationManager.notify(notificationID, mBuilder.build());
    }
    public static void update(Enum action ,String value,Context context){
        if(lBM==null){
            lBM=LocalBroadcastManager.getInstance(context);
        }
        lBM.sendBroadcast(new Intent(action.toString()).putExtra("value",value));
    }

    public static String update(Enum action , final String value, String extra2, final Context context){
        if(lBM==null){
            lBM=LocalBroadcastManager.getInstance(context);
        }
        final String[] tmp = {null};

        if(action.toString().equals("com")&&!value.contains("Return")){
            lBM.sendBroadcast(new Intent(action.toString()).putExtra("value",value).putExtra("value2",extra2));
        }else{
            lBM.sendBroadcast(new Intent(action.toString()).putExtra("value",value).putExtra("value2",extra2));
        }
        return tmp[0];
    }

        public static LocalBroadcastManager getIBM(Context context){
            if(lBM==null){
                lBM=LocalBroadcastManager.getInstance(context);
            }
            return lBM;
        }

}
