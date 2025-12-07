package com.example.finals1;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.Flashcard;
import com.example.finals1.data.FlashcardDao;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class FlashcardListActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "EXTRA_SET_ID";
    public static final String EXTRA_SET_TITLE = "EXTRA_SET_TITLE";

    private RecyclerView recyclerView;
    private FlashcardListAdapter adapter;
    private final List<Flashcard> items = new ArrayList<>();
    private long setId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard_list);

        setTitle(getIntent().getStringExtra(EXTRA_SET_TITLE));

        recyclerView = findViewById(R.id.recyclerFlashcards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlashcardListAdapter(items, new FlashcardListAdapter.OnFlashcardActionListener() {
            @Override
            public void onEditFlashcard(Flashcard card, int position) {
                showEditDialog(card);
            }

            @Override
            public void onDeleteFlashcard(Flashcard card, int position) {
                confirmDelete(card);
            }
        });
        recyclerView.setAdapter(adapter);

        setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        if (setId != -1) {
            loadCardsAsync(setId);
        }

        Button btnAdd = findViewById(R.id.btnAddFlashcard);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showAddDialog());
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

    private void showEditDialog(Flashcard card) {
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_flashcard, null);
        EditText edtTerm = v.findViewById(R.id.edtTerm);
        EditText edtDefinition = v.findViewById(R.id.edtDefinition);
        edtTerm.setText(card.term);
        edtDefinition.setText(card.definition);
        new AlertDialog.Builder(this)
                .setTitle("Edit flashcard")
                .setView(v)
                .setPositiveButton("Save", (d,w) -> {
                    String term = edtTerm.getText().toString().trim();
                    String def = edtDefinition.getText().toString().trim();
                    if (term.isEmpty() || def.isEmpty()) return;
                    AsyncTask.execute(() -> {
                        FlashcardDao dao = AppDatabase.getInstance(this).flashcardDao();
                        card.term = term;
                        card.definition = def;
                        dao.update(card);
                        runOnUiThread(() -> loadCardsAsync(setId));
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Flashcard card) {
        new AlertDialog.Builder(this)
                .setTitle("Delete flashcard")
                .setMessage("Are you sure you want to delete this card?")
                .setPositiveButton("Delete", (d,w) -> {
                    AsyncTask.execute(() -> {
                        FlashcardDao dao = AppDatabase.getInstance(this).flashcardDao();
                        dao.deleteById(card.id);
                        runOnUiThread(() -> loadCardsAsync(setId));
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_flashcard, null);
        EditText edtTerm = v.findViewById(R.id.edtTerm);
        EditText edtDefinition = v.findViewById(R.id.edtDefinition);
        edtTerm.setText("");
        edtDefinition.setText("");
        new AlertDialog.Builder(this)
                .setTitle("Add flashcard")
                .setView(v)
                .setPositiveButton("Save", (d,w) -> {
                    String term = edtTerm.getText().toString().trim();
                    String def = edtDefinition.getText().toString().trim();
                    if (term.isEmpty() || def.isEmpty()) return;
                    AsyncTask.execute(() -> {
                        FlashcardDao dao = AppDatabase.getInstance(this).flashcardDao();
                        dao.insert(new Flashcard(setId, term, def));
                        runOnUiThread(() -> loadCardsAsync(setId));
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
