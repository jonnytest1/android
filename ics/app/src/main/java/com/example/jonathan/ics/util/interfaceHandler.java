package com.example.jonathan.ics.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;

import com.example.jonathan.ics.R;
import com.example.jonathan.ics.util.storage.Storage;

/**
 * Created by Jonathan on 28.04.2018.
 */

public class interfaceHandler {


    private static  interfaceHandler instance;

    public static void createInstance(Activity context){
            instance=new interfaceHandler(context);
    }

    public static interfaceHandler getInstance(){
        return instance;
    }

    public interfaceHandler(Activity activity){
        storage.setContext(activity);
        this.activity =activity;
    }

    private Storage storage=new Storage();

    private LocalBroadcastManager lBM;

    private Activity activity;

    public void show(String msg){
        show(msg,null);
    }
    public void show(String msg, View.OnClickListener okButton){

        Snackbar snackbar=Snackbar.make(activity.findViewById(R.id.coordinatorLayout), msg,Snackbar.LENGTH_LONG);

        View snackView = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)snackView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackView.setLayoutParams(params);

        if(okButton!=null) {
            snackbar.setAction("OK", okButton);
        }
        snackbar.show();
    }

    public void note(String text){
        note(text,"");
    }

    private boolean shouldLog(String text){
        if(text.equals("wrong input type for write")){
            return false;
        }
        String stack=Converters.toString(Thread.currentThread().getStackTrace());
        return !(stack.split("Storage.parseToList").length>2);

    }

    public void note(String text, String text2){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity)
                .setContentTitle(text);
        mBuilder.setSmallIcon(R.drawable.ic_android_black_24dp);
        mBuilder.setContentText(text2);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(text2));
        NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
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

    public void update(Enum action ,String value,Context context){
        getIBM(context).sendBroadcast(new Intent(action.toString()).putExtra("value",value));
    }

    public LocalBroadcastManager getIBM(Context context){
        if(lBM==null){
            lBM=LocalBroadcastManager.getInstance(context);
        }
        return lBM;
    }

    public Storage getStorage() {
        return storage;
    }
}
