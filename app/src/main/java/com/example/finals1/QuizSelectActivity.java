package com.example.finals1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.FlashcardSet;
import com.example.finals1.data.FlashcardSetDao;
import com.example.finals1.data.User;
import com.example.finals1.data.UserDao;

import java.util.ArrayList;
import java.util.List;

public class QuizSelectActivity extends AppCompatActivity implements SetListAdapter.OnSetClickListener {

    private RecyclerView recyclerView;
    private SetListAdapter adapter;
    private final List<FlashcardSet> items = new ArrayList<>();
    private final List<FlashcardSet> filtered = new ArrayList<>();
    private long currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz_select);
        setTitle("Choose a set for quiz");

        recyclerView = findViewById(R.id.recyclerQuizSets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SetListAdapter(filtered, this);
        recyclerView.setAdapter(adapter);

        EditText edtSearch = findViewById(R.id.edtSearchQuizSets);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) { applyFilter(s.toString()); }
            });
        }

        resolveCurrentUserAndLoad();
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

    private void resolveCurrentUserAndLoad() {
        String email = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        if (email == null || email.isEmpty()) {
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
                applyFilter( ((EditText) findViewById(R.id.edtSearchQuizSets)).getText().toString() );
            });
        });
    }

    @Override
    public void onSetClicked(FlashcardSet set) {
        Intent i = new Intent(this, QuizActivity.class);
        i.putExtra(QuizActivity.EXTRA_SET_ID, set.id);
        i.putExtra(QuizActivity.EXTRA_SET_TITLE, set.title);
        startActivity(i);
    }

    @Override
    public void onSetLongClicked(FlashcardSet set, int position) {
        // No long click actions on quiz selection
    }
}
