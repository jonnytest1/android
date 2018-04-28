package com.example.jonathan.ics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

/**
 * Created by Jonathan on 24.04.2018.
 */

public class startup extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //interfaceHandler.note(intent.getAction().toString(),context);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent( context , Calendar2.class);
            context.startService(serviceIntent);
        }
    }
}
