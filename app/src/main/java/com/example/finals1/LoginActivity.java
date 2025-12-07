package com.example.finals1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.User;
import com.example.finals1.data.UserDao;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";

    private EditText edtEmail, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> handleLogin());
        findViewById(R.id.btnRegister).setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (!isValidEmail(email)) { edtEmail.setError("Invalid email"); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) { edtPassword.setError("Password must be 6+ chars"); return; }

        AsyncTask.execute(() -> {
            try {
                UserDao dao = AppDatabase.getInstance(this).userDao();
                User existing = dao.findByEmail(email);
                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show());
                    return;
                }
                String salt = generateSalt();
                String hash = hashPassword(password, salt);
                if (TextUtils.isEmpty(hash)) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to hash password", Toast.LENGTH_SHORT).show());
                    return;
                }
                User user = new User(email, hash, salt);
                dao.insert(user);
                runOnUiThread(() -> {
                    // Persist session email after registration (optional auto-login)
                    getSharedPreferences("session", MODE_PRIVATE).edit().putString("email", email).apply();
                    Toast.makeText(this, "Registered. You can now login", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(this, "Register error: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (!isValidEmail(email)) { edtEmail.setError("Invalid email"); return; }
        if (TextUtils.isEmpty(password)) { edtPassword.setError("Enter password"); return; }

        AsyncTask.execute(() -> {
            try {
                UserDao dao = AppDatabase.getInstance(this).userDao();
                User user = dao.findByEmail(email);
                if (user == null) {
                    runOnUiThread(() -> Toast.makeText(this, "No such user. Register first.", Toast.LENGTH_SHORT).show());
                    return;
                }
                String hash = hashPassword(password, user.salt);
                if (hash.equals(user.passwordHash)) {
                    runOnUiThread(() -> {
                        // Persist session email
                        getSharedPreferences("session", MODE_PRIVATE).edit().putString("email", email).apply();
                        navigateToMain(email);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(this, "Login error: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    private String hashPassword(String password, String saltBase64) {
        try {
            byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes());
            return Base64.encodeToString(hashed, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    private void navigateToMain(String email) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
