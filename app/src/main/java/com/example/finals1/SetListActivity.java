package com.example.finals1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.FlashcardSet;
import com.example.finals1.data.FlashcardSetDao;
import com.example.finals1.data.User;
import com.example.finals1.data.UserDao;

import java.util.ArrayList;
import java.util.List;

public class SetListActivity extends AppCompatActivity implements SetListAdapter.OnSetClickListener {

    private RecyclerView recyclerView;
    private SetListAdapter adapter;
    private final List<FlashcardSet> items = new ArrayList<>();
    private final List<FlashcardSet> filtered = new ArrayList<>();
    private long currentUserId = -1;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_list);

        recyclerView = findViewById(R.id.recyclerSets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SetListAdapter(filtered, this);
        recyclerView.setAdapter(adapter);

        EditText edtSearch = findViewById(R.id.edtSearchSets);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    applyFilter(s.toString());
                }
            });
        }

        // Register receiver
        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, android.content.Intent intent) {
                String action = intent.getAction();
                if (BroadcastActions.ACTION_SET_ADDED.equals(action)) {
                    loadSetsAsync();
                }
            }
        };
        IntentFilter filter = new IntentFilter(BroadcastActions.ACTION_SET_ADDED);
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        resolveCurrentUserIdAndLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            try { unregisterReceiver(receiver); } catch (Exception ignored) {}
        }
    }

    private void applyFilter(String q) {
        String query = q == null ? "" : q.trim().toLowerCase();
        filtered.clear();
        for (FlashcardSet s : items) {
            String title = s.title == null ? "" : s.title.toLowerCase();
            if (query.isEmpty() || title.contains(query)) {
                filtered.add(s);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void resolveCurrentUserIdAndLoad() {
        String email = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        if (email == null || email.isEmpty()) {
            // Try from MainActivity if forwarded
            email = getSharedPreferences("session", MODE_PRIVATE).getString("email", "");
        }
        final String finalEmail = email;
        AsyncTask.execute(() -> {
            UserDao userDao = AppDatabase.getInstance(this).userDao();
            User u = finalEmail == null || finalEmail.isEmpty() ? null : userDao.findByEmail(finalEmail);
            currentUserId = (u != null) ? u.id : -1;
            runOnUiThread(this::loadSetsAsync);
        });
    }

    private void loadSetsAsync() {
        AsyncTask.execute(() -> {
            FlashcardSetDao dao = AppDatabase.getInstance(this).flashcardSetDao();
            List<FlashcardSet> all = dao.getAll();
            List<FlashcardSet> mine = new ArrayList<>();
            for (FlashcardSet s : all) {
                if (s.userId == currentUserId) {
                    mine.add(s);
                }
            }
            runOnUiThread(() -> {
                items.clear();
                items.addAll(mine);
                applyFilter( ((EditText) findViewById(R.id.edtSearchSets)).getText().toString() );
            });
        });
    }

    @Override
    public void onSetClicked(FlashcardSet set) {
        Intent i = new Intent(this, FlashcardListActivity.class);
        i.putExtra(FlashcardListActivity.EXTRA_SET_ID, set.id);
        i.putExtra(FlashcardListActivity.EXTRA_SET_TITLE, set.title);
        startActivity(i);
    }

    @Override
    public void onSetLongClicked(FlashcardSet set, int position) {
        String[] options = {"Rename", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle(set.title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(set);
                    } else if (which == 1) {
                        confirmDelete(set);
                    }
                })
                .show();
    }

    private void showRenameDialog(FlashcardSet set) {
        EditText input = new EditText(this);
        input.setHint("New title");
        input.setText(set.title);
        new AlertDialog.Builder(this)
                .setTitle("Rename set")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newTitle = input.getText().toString().trim();
                    if (newTitle.isEmpty()) return;
                    AsyncTask.execute(() -> {
                        FlashcardSetDao dao = AppDatabase.getInstance(this).flashcardSetDao();
                        dao.rename(set.id, newTitle, set.description);
                        runOnUiThread(this::loadSetsAsync);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(FlashcardSet set) {
        new AlertDialog.Builder(this)
                .setTitle("Delete set")
                .setMessage("This will delete the set and its cards. Continue?")
                .setPositiveButton("Delete", (d, w) -> {
                    AsyncTask.execute(() -> {
                        FlashcardSetDao dao = AppDatabase.getInstance(this).flashcardSetDao();
                        dao.deleteById(set.id);
                        runOnUiThread(this::loadSetsAsync);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
