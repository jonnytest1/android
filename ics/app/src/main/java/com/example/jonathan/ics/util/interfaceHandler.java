package com.example.jonathan.ics.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import com.example.jonathan.ics.R;
import com.example.jonathan.ics.util.storage.Storage;

/**
 * Created by Jonathan on 28.04.2018.
 */

public class interfaceHandler {

    private static Storage storage=new Storage();

    private static LocalBroadcastManager lBM;

    private static Toolbar currentToolbar;

    private static Context context;

    public static void init(Context context){
        storage.setContext(context);
    }

    public static void show(String msg,Activity activity){
        Snackbar snack = Snackbar.make(activity.findViewById(R.id.coordL),msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snack.show();
        Snackbar.make(currentToolbar, msg,Snackbar.LENGTH_LONG).show();
    }
    public static void show(String msg, View.OnClickListener okButton,Activity activity){
        Snackbar.make(currentToolbar, msg,Snackbar.LENGTH_LONG).setAction("OK",okButton).show();
    }

    public  static  void note(String text){
        note(text,"");
    }


    private static boolean shouldLog(String text){
        if(text.equals("wrong input type for write")){
            return false;
        }
        String stack=Converters.toString(Thread.currentThread().getStackTrace());
        if(stack.split("Storage.parseToList").length>2){
            return false;
        }
        return true;
    }

    public static void note(String text, String text2){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(text);
        mBuilder.setSmallIcon(R.drawable.ic_android_black_24dp);
        mBuilder.setContentText(text2);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(text2));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID=200;
        if(mNotificationManager!=null){
            if(shouldLog(text)) {
                storage.log("note_ " + text);
            }
            mNotificationManager.notify(notificationID, mBuilder.build());
        }else{
            storage.log("noteMAnage is null ?!");
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

    public static Storage getStorage() {
        return storage;
    }

    public static void setCurrentToolbar(Toolbar currentToolbar) {
        currentToolbar = currentToolbar;
    }
}
