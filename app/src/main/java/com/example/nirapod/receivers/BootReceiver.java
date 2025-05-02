package com.example.nirapod.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.nirapod.utils.AlarmHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule all medication alarms after device reboot
            AlarmHelper.scheduleAllMedications(context);
        }
    }
}