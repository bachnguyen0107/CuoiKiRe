package com.example.finals1.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.example.finals1.notifications.NotifHelper;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_MESSAGE = "extra_message";

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra(EXTRA_MESSAGE);
        if (msg == null) msg = "Time to practice your flashcards!";
        // Reuse NotifHelper to show a simple reminder
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotifHelper.ensureChannel(context);
            NotifHelper.showPracticeReminder(context, msg);
        }
        // Reschedule next day exact alarm based on stored hour/minute
        try {
            int hour = context.getSharedPreferences("reminder", Context.MODE_PRIVATE).getInt("hour", -1);
            int minute = context.getSharedPreferences("reminder", Context.MODE_PRIVATE).getInt("minute", -1);
            if (hour >= 0 && minute >= 0) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.HOUR_OF_DAY, hour);
                c.set(java.util.Calendar.MINUTE, minute);
                c.set(java.util.Calendar.SECOND, 0);
                c.set(java.util.Calendar.MILLISECOND, 0);
                c.add(java.util.Calendar.DAY_OF_YEAR, 1); // next day
                long nextTrigger = c.getTimeInMillis();
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent nextIntent = new Intent(context, ReminderReceiver.class);
                nextIntent.putExtra(EXTRA_MESSAGE, msg);
                PendingIntent pi = PendingIntent.getBroadcast(
                        context, 3000, nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
                );
                if (am != null) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pi);
                }
            }
        } catch (Throwable ignored) {}
    }
}
