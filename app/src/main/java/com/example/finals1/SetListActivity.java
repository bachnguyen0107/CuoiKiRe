package com.example.finals1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
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
}

