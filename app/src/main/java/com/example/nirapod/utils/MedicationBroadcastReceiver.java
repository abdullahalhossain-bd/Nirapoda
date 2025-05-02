package com.example.nirapod.utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.nirapod.database.MedicationDBHelper;
import com.example.nirapod.models.Medication;

public class MedicationBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_MEDICATION_ALARM = "com.example.nirapod.ACTION_MEDICATION_ALARM";
    public static final String ACTION_MARK_TAKEN = "com.example.nirapod.ACTION_MARK_TAKEN";
    public static final String ACTION_SKIP = "com.example.nirapod.ACTION_SKIP";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int medicationId = intent.getIntExtra("medication_id", -1);

        if (medicationId == -1) {
            return; // Invalid medication ID
        }

        MedicationDBHelper dbHelper = new MedicationDBHelper(context);

        if (ACTION_MEDICATION_ALARM.equals(action)) {
            // This is a medication alarm
            Medication medication = dbHelper.getMedicationById(medicationId);
            if (medication != null) {
                // Show notification
                NotificationHelper.showMedicationNotification(context, medication);
            }
        } else if (ACTION_MARK_TAKEN.equals(action)) {
            // User marked medication as taken from notification
            dbHelper.updateMedicationStatus(medicationId, "Taken");

            // Dismiss the notification
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(medicationId);

            // Broadcast to update UI if the app is open
            Intent updateIntent = new Intent("com.example.nirapod.MEDICATION_STATUS_UPDATED");
            context.sendBroadcast(updateIntent);
        } else if (ACTION_SKIP.equals(action)) {
            // User skipped medication from notification
            dbHelper.updateMedicationStatus(medicationId, "Skipped");

            // Dismiss the notification
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(medicationId);

            // Broadcast to update UI if the app is open
            Intent updateIntent = new Intent("com.example.nirapod.MEDICATION_STATUS_UPDATED");
            context.sendBroadcast(updateIntent);
        }
    }
}