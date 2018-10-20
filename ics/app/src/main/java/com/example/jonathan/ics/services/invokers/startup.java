package com.example.jonathan.ics.services.invokers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.jonathan.ics.services.invokers.SchedulerMail.registerScheduler;

/**
 * Created by Jonathan on 24.04.2018.
 */

public class startup extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           registerScheduler(context);
        }
    }
}
