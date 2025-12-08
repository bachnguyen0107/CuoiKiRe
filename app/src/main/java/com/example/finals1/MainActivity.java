package com.example.finals1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.finals1.reminder.ReminderReceiver;
import android.provider.Settings;
import com.example.finals1.dictionary.DictionaryRepository;
import java.util.Locale;
import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_POST_NOTIFICATIONS = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Request notification permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            }
        }

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        String email = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        if (email != null && !email.isEmpty()) {
            getSharedPreferences("session", MODE_PRIVATE).edit().putString("email", email).apply();
        }
        if (tvWelcome != null) {
            if (email == null || email.isEmpty()) {
                tvWelcome.setText(getString(R.string.welcome_plain));
            } else {
                tvWelcome.setText(getString(R.string.welcome_with_email, email));
            }
        }


        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                getSharedPreferences("session", MODE_PRIVATE).edit().remove("email").apply();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            });
        }

        Button btnCreateSet = findViewById(R.id.btnCreateSet);
        if (btnCreateSet != null) {
            btnCreateSet.setOnClickListener(v -> {
                Intent i = new Intent(MainActivity.this, CreateSetActivity.class);
                i.putExtra(LoginActivity.EXTRA_EMAIL, getSharedPreferences("session", MODE_PRIVATE).getString("email", ""));
                startActivity(i);
            });
        }

        Button btnYourSets = findViewById(R.id.btnYourSets);
        if (btnYourSets != null) {
            btnYourSets.setOnClickListener(v -> {
                Intent i = new Intent(MainActivity.this, SetListActivity.class);
                i.putExtra(LoginActivity.EXTRA_EMAIL, getSharedPreferences("session", MODE_PRIVATE).getString("email", ""));
                startActivity(i);
            });
        }

        Button btnQuiz = findViewById(R.id.btnQuiz);
        if (btnQuiz != null) {
            btnQuiz.setOnClickListener(v -> {
                Intent i = new Intent(MainActivity.this, QuizSelectActivity.class);
                i.putExtra(LoginActivity.EXTRA_EMAIL, getSharedPreferences("session", MODE_PRIVATE).getString("email", ""));
                startActivity(i);
            });
        }

        Button btnSetReminder = findViewById(R.id.btnSetReminder);
        if (btnSetReminder != null) {
            btnSetReminder.setOnClickListener(v -> showTimePickerAndSchedule());
        }

        Button btnLookup = findViewById(R.id.btnLookup);
        EditText edtWord = findViewById(R.id.edtWord);
        if (btnLookup != null && edtWord != null) {
            btnLookup.setOnClickListener(v -> lookupWord(edtWord.getText().toString()));
        }
    }

    private void lookupWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            Toast.makeText(this, "Enter a word to lookup", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                DictionaryRepository repo = new DictionaryRepository(MainActivity.this);
                com.example.finals1.data.DictionaryEntry e = repo.lookup(word);
                runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Definition for '" + e.word + "'")
                        .setMessage(e.definition)
                        .setPositiveButton("OK", null)
                        .show());
            } catch (Exception ex) {
                runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Lookup failed")
                        .setMessage(String.valueOf(ex))
                        .setPositiveButton("OK", null)
                        .show());
            }
        }).start();
    }

    private void showTimePickerAndSchedule() {
        TimePicker picker = new TimePicker(this);
        picker.setIs24HourView(true);
        new AlertDialog.Builder(this)
                .setTitle("Daily practice time")
                .setView(picker)
                .setPositiveButton("Save", (d,w) -> {
                    try {
                        int hour = picker.getHour();
                        int minute = picker.getMinute();
                        getSharedPreferences("reminder", MODE_PRIVATE)
                                .edit().putInt("hour", hour).putInt("minute", minute).apply();
                        scheduleDailyReminder(hour, minute);
                        Toast.makeText(this, "Reminder set for " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
                    } catch (Throwable t) {
                        Log.e("Reminder", "Failed to save reminder", t);
                        new AlertDialog.Builder(this)
                                .setTitle("Error setting reminder")
                                .setMessage(String.valueOf(t))
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void scheduleDailyReminder(int hour, int minute) {
        try {
            long triggerAt = computeNextTriggerMillis(hour, minute);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra(ReminderReceiver.EXTRA_MESSAGE, "Time to practice your flashcards!");
            PendingIntent pi = PendingIntent.getBroadcast(
                    this, 3000, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            if (am == null || pi == null) {
                throw new IllegalStateException("AlarmManager or PendingIntent is null");
            }
            if (android.os.Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                // Guide user to enable exact alarms
                new AlertDialog.Builder(this)
                        .setTitle("Allow exact alarms")
                        .setMessage("To trigger at the exact time, allow exact alarms in system settings.")
                        .setPositiveButton("Open settings", (d, w) -> {
                            Intent s = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(s);
                        })
                        .setNegativeButton("Use approximate", (d, w) -> {
                            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_DAY, pi);
                            Log.i("Reminder", "Scheduled inexact daily alarm starting at millis=" + triggerAt);
                        })
                        .show();
            } else {
                // Schedule exact alarm for next occurrence; receiver should reschedule next day
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                Log.i("Reminder", "Scheduled exact alarm at millis=" + triggerAt);
            }
        } catch (Throwable t) {
            Log.e("Reminder", "Failed to schedule alarm", t);
            new AlertDialog.Builder(this)
                    .setTitle("Error scheduling alarm")
                    .setMessage(String.valueOf(t))
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private long computeNextTriggerMillis(int hour, int minute) {
        try {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.set(java.util.Calendar.HOUR_OF_DAY, hour);
            c.set(java.util.Calendar.MINUTE, minute);
            c.set(java.util.Calendar.SECOND, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
            long now = System.currentTimeMillis();
            if (c.getTimeInMillis() <= now) {
                c.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }
            return c.getTimeInMillis();
        } catch (Throwable t) {
            Log.e("Reminder", "Failed to compute trigger time", t);
            // Fallback: schedule 1 minute from now
            return System.currentTimeMillis() + 60_000L;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS) {
            // Optional: feedback
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications disabled; reminders may not appear", Toast.LENGTH_LONG).show();
            }
        }
    }
}