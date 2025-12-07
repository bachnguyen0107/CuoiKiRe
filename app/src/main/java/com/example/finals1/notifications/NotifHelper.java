package com.example.finals1.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.finals1.MainActivity;
import com.example.finals1.R;

public final class NotifHelper {
    private NotifHelper() {}

    public static final String CHANNEL_ID = "practice_reminder_channel";

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Practice reminders", NotificationManager.IMPORTANCE_DEFAULT);
                ch.setDescription("Daily reminders to practice flashcards");
                ch.enableLights(true);
                ch.setLightColor(Color.BLUE);
                nm.createNotificationChannel(ch);
            }
        }
    }

    public static void showPracticeReminder(Context ctx, String message) {
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return;
        Intent contentIntent = new Intent(ctx, MainActivity.class);
        PendingIntent contentPi = PendingIntent.getActivity(
                ctx, 2000, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Practice time")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(contentPi);
        try {
            NotificationManagerCompat.from(ctx).notify(2000, b.build());
        } catch (SecurityException ignored) {}
    }
}

