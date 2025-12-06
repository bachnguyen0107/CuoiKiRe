package com.example.finals1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.FlashcardSet;
import com.example.finals1.data.FlashcardSetDao;

import java.util.ArrayList;
import java.util.List;

public class SetListActivity extends AppCompatActivity implements SetListAdapter.OnSetClickListener {

    private RecyclerView recyclerView;
    private SetListAdapter adapter;
    private final List<FlashcardSet> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_list);

        recyclerView = findViewById(R.id.recyclerSets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SetListAdapter(items, this);
        recyclerView.setAdapter(adapter);

        loadSetsAsync();
    }

    private void loadSetsAsync() {
        AsyncTask.execute(() -> {
            FlashcardSetDao dao = AppDatabase.getInstance(this).flashcardSetDao();
            List<FlashcardSet> all = dao.getAll();
            runOnUiThread(() -> {
                items.clear();
                items.addAll(all);
                adapter.notifyDataSetChanged();
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
