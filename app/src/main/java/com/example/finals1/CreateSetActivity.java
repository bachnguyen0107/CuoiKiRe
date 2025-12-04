package com.example.finals1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.Flashcard;
import com.example.finals1.data.FlashcardDao;
import com.example.finals1.data.FlashcardSet;
import com.example.finals1.data.FlashcardSetDao;

public class CreateSetActivity extends AppCompatActivity {

    private EditText edtTitle, edtDescription;
    private LinearLayout rowsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_set);

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        rowsContainer = findViewById(R.id.rowsContainer);

        Button btnAddRow = findViewById(R.id.btnAddRow);
        Button btnSaveSet = findViewById(R.id.btnSaveSet);

        btnAddRow.setOnClickListener(v -> addRow());
        btnSaveSet.setOnClickListener(v -> saveSet());

        // Start with two rows by default
        addRow();
        addRow();
    }

    private void addRow() {
        View row = getLayoutInflater().inflate(R.layout.item_term_definition_row, rowsContainer, false);
        rowsContainer.addView(row);
    }

    private void saveSet() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Title required");
            return;
        }
        // Collect term-definition pairs
        int count = rowsContainer.getChildCount();
        if (count < 2) {
            Toast.makeText(this, "Add at least two cards", Toast.LENGTH_SHORT).show();
            return;
        }
        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            FlashcardSetDao setDao = db.flashcardSetDao();
            FlashcardDao cardDao = db.flashcardDao();
            long setId = setDao.insert(new FlashcardSet(title, description));
            for (int i = 0; i < count; i++) {
                View row = rowsContainer.getChildAt(i);
                EditText edtTerm = row.findViewById(R.id.edtTerm);
                EditText edtDef = row.findViewById(R.id.edtDefinition);
                String term = edtTerm.getText().toString().trim();
                String def = edtDef.getText().toString().trim();
                if (!TextUtils.isEmpty(term) && !TextUtils.isEmpty(def)) {
                    cardDao.insert(new Flashcard(setId, term, def));
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Set saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

