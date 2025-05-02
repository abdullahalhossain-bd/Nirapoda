package com.example.nirapod.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.nirapod.R;
import com.example.nirapod.MainActivity;
import com.example.nirapod.models.Medication;

public class NotificationHelper {

    private static final String CHANNEL_ID = "medication_reminder_channel";
    private static final String CHANNEL_NAME = "Medication Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for medication reminders";

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showMedicationNotification(Context context, Medication medication) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, medication.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications) // You'll need to create this drawable
                .setContentTitle("Medication Reminder")
                .setContentText("Time to take " + medication.getName() + " (" + medication.getDosage() + ")")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("It's time to take " + medication.getName() + " (" + medication.getDosage() + ")\n" +
                                (medication.getInstructions() != null && !medication.getInstructions().isEmpty() ?
                                        "Instructions: " + medication.getInstructions() : "")))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Add actions
        Intent takenIntent = new Intent(context, MedicationBroadcastReceiver.class);
        takenIntent.setAction(MedicationBroadcastReceiver.ACTION_MARK_TAKEN);
        takenIntent.putExtra("medication_id", medication.getId());
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId() * 10 + 1,
                takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent skipIntent = new Intent(context, MedicationBroadcastReceiver.class);
        skipIntent.setAction(MedicationBroadcastReceiver.ACTION_SKIP);
        skipIntent.putExtra("medication_id", medication.getId());
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId() * 10 + 2,
                skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.addAction(R.drawable.ic_check_circle, "Mark as Taken", takenPendingIntent); // You'll need to create this drawable
        builder.addAction(R.drawable.ic_skip_previous, "Skip", skipPendingIntent); // You'll need to create this drawable

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(medication.getId(), builder.build());
    }
}
