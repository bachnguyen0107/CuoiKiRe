package com.example.finals1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        String email = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
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
                startActivity(i);
            });
        }
    }
}