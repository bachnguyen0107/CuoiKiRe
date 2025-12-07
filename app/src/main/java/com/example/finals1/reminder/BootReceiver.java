package com.example.finals1.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Reschedule the daily reminder if user set one
            int hour = context.getSharedPreferences("reminder", Context.MODE_PRIVATE).getInt("hour", -1);
            int minute = context.getSharedPreferences("reminder", Context.MODE_PRIVATE).getInt("minute", -1);
            if (hour >= 0 && minute >= 0) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.HOUR_OF_DAY, hour);
                c.set(java.util.Calendar.MINUTE, minute);
                c.set(java.util.Calendar.SECOND, 0);
                c.set(java.util.Calendar.MILLISECOND, 0);
                long now = System.currentTimeMillis();
                if (c.getTimeInMillis() <= now) {
                    c.add(java.util.Calendar.DAY_OF_YEAR, 1);
                }
                long triggerAt = c.getTimeInMillis();
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent nextIntent = new Intent(context, ReminderReceiver.class);
                nextIntent.putExtra(ReminderReceiver.EXTRA_MESSAGE, "Time to practice your flashcards!");
                PendingIntent pi = PendingIntent.getBroadcast(
                        context, 3000, nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
                );
                if (am != null) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            }
        }
    }
}
