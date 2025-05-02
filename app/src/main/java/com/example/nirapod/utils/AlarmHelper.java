package com.example.nirapod.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.nirapod.database.MedicationDBHelper;
import com.example.nirapod.models.Medication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmHelper {

    // Schedule an alarm for a medication
    public static void scheduleAlarm(Context context, Medication medication) {
        if (medication.getTime() == null || medication.getTime().isEmpty()) {
            return; // No time specified for this medication (as needed)
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MedicationBroadcastReceiver.class);
        intent.setAction(MedicationBroadcastReceiver.ACTION_MEDICATION_ALARM);
        intent.putExtra("medication_id", medication.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Parse time string to get hour and minute
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Date date;
        try {
            date = sdf.parse(medication.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        if (date == null) return;

        Calendar calendar = Calendar.getInstance();
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(date);

        // Set alarm time
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);

        // If the time is already passed for today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Check if we need to schedule for specific days
        if ("Specific days".equals(medication.getFrequency()) && medication.getDays() != null) {
            // Find the next day that matches one of the specified days
            while (!isDayIncluded(calendar, medication.getDays())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }
    }

    // Cancel an alarm for a medication
    public static void cancelAlarm(Context context, int medicationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MedicationBroadcastReceiver.class);
        intent.setAction(MedicationBroadcastReceiver.ACTION_MEDICATION_ALARM);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medicationId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    // Helper method to check if a specific day is included in the comma-separated days string
    private static boolean isDayIncluded(Calendar calendar, String daysString) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String dayAbbreviation;

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dayAbbreviation = "Mon";
                break;
            case Calendar.TUESDAY:
                dayAbbreviation = "Tue";
                break;
            case Calendar.WEDNESDAY:
                dayAbbreviation = "Wed";
                break;
            case Calendar.THURSDAY:
                dayAbbreviation = "Thu";
                break;
            case Calendar.FRIDAY:
                dayAbbreviation = "Fri";
                break;
            case Calendar.SATURDAY:
                dayAbbreviation = "Sat";
                break;
            case Calendar.SUNDAY:
                dayAbbreviation = "Sun";
                break;
            default:
                return false;
        }

        return daysString.contains(dayAbbreviation);
    }

    // Schedule all medications from the database
    public static void scheduleAllMedications(Context context) {
        // This method would be called when the app starts or the device reboots
        MedicationDBHelper dbHelper = new MedicationDBHelper(context);
        for (Medication medication : dbHelper.getAllMedications()) {
            scheduleAlarm(context, medication);
        }
    }
}