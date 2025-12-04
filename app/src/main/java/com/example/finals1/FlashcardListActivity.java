package com.example.finals1;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.Flashcard;
import com.example.finals1.data.FlashcardDao;

import java.util.ArrayList;
import java.util.List;

public class FlashcardListActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "EXTRA_SET_ID";
    public static final String EXTRA_SET_TITLE = "EXTRA_SET_TITLE";

    private RecyclerView recyclerView;
    private FlashcardListAdapter adapter;
    private final List<Flashcard> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard_list);

        setTitle(getIntent().getStringExtra(EXTRA_SET_TITLE));

        recyclerView = findViewById(R.id.recyclerFlashcards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlashcardListAdapter(items);
        recyclerView.setAdapter(adapter);

        long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        if (setId != -1) {
            loadCardsAsync(setId);
        }
    }

    private void loadCardsAsync(long setId) {
        AsyncTask.execute(() -> {
            FlashcardDao dao = AppDatabase.getInstance(this).flashcardDao();
            List<Flashcard> all = dao.getBySet(setId);
            runOnUiThread(() -> {
                items.clear();
                items.addAll(all);
                adapter.notifyDataSetChanged();
            });
        });
    }
}

