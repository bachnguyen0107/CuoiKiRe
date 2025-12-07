package com.example.finals1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.Flashcard;
import com.example.finals1.data.FlashcardDao;
import com.example.finals1.data.QuizResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "EXTRA_SET_ID";
    public static final String EXTRA_SET_TITLE = "EXTRA_SET_TITLE";

    private long setId;
    private TextView tvTimer, tvQuestion, tvProgress;
    private EditText edtAnswer;
    private Button btnCheck;
    private CountDownTimer timer;

    private final List<Flashcard> cards = new ArrayList<>();
    private final List<Long> wrongIds = new ArrayList<>();
    private int index = 0;
    private int correctCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);

        setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        setTitle(getIntent().getStringExtra(EXTRA_SET_TITLE));

        tvTimer = findViewById(R.id.tvTimer);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        edtAnswer = findViewById(R.id.edtAnswer);
        btnCheck = findViewById(R.id.btnCheck);

        btnCheck.setOnClickListener(v -> checkAnswer());

        if (setId != -1) {
            loadCardsAsync(setId);
        }
    }

    private void startTimer() {
        // 180 seconds countdown for the quiz
        timer = new CountDownTimer(180_000, 1_000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time: " + (millisUntilFinished / 1000) + "s");
            }
            public void onFinish() {
                endQuiz();
            }
        };
        timer.start();
    }

    private void loadCardsAsync(long setId) {
        AsyncTask.execute(() -> {
            FlashcardDao dao = AppDatabase.getInstance(this).flashcardDao();
            List<Flashcard> all = dao.getBySet(setId);
            runOnUiThread(() -> {
                cards.clear();
                cards.addAll(all);
                if (cards.isEmpty()) {
                    showEmptySetDialog();
                } else {
                    Collections.shuffle(cards);
                    index = 0;
                    correctCount = 0;
                    updateUIForCard();
                    startTimer();
                }
            });
        });
    }

    private void showEmptySetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No cards")
                .setMessage("This set has no cards to quiz.")
                .setPositiveButton("OK", (d,w) -> finish())
                .show();
    }

    private void updateUIForCard() {
        if (index >= cards.size()) {
            endQuiz();
            return;
        }
        Flashcard c = cards.get(index);
        tvQuestion.setText(c.term);
        tvProgress.setText((index + 1) + "/" + cards.size());
        edtAnswer.setText("");
    }

    private void checkAnswer() {
        if (index >= cards.size()) { endQuiz(); return; }
        Flashcard c = cards.get(index);
        String userAnswer = edtAnswer.getText().toString().trim();
        boolean isCorrect = compareDefinitions(userAnswer, c.definition);
        if (isCorrect) {
            correctCount++;
        } else {
            wrongIds.add(c.id);
        }
        showResultAndProceed(isCorrect);
    }

    private boolean compareDefinitions(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        String na = a.trim().replaceAll("\\s+", " ").toLowerCase();
        String nb = b.trim().replaceAll("\\s+", " ").toLowerCase();
        return na.equals(nb);
    }

    private void showResultAndProceed(boolean isCorrect) {
        new AlertDialog.Builder(this)
                .setTitle(isCorrect ? "Correct" : "Wrong")
                .setMessage(isCorrect ? "Good job!" : "Expected: \n" + cards.get(index).definition)
                .setPositiveButton("Next", (d,w) -> {
                    index++;
                    if (index >= cards.size()) {
                        endQuiz();
                    } else {
                        updateUIForCard();
                    }
                })
                .show();
    }

    private void endQuiz() {
        if (timer != null) timer.cancel();
        int total = cards.size();
        // Save result
        saveQuizResult(correctCount, total);
        // Persist wrong IDs for review
        String key = "last_wrong_ids_" + setId;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wrongIds.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(wrongIds.get(i));
        }
        getSharedPreferences("quiz", MODE_PRIVATE).edit().putString(key, sb.toString()).apply();
        new AlertDialog.Builder(this)
                .setTitle("Quiz finished")
                .setMessage("Score: " + correctCount + " / " + total)
                .setPositiveButton("Done", (d,w) -> finish())
                .show();
    }

    private void saveQuizResult(int correct, int total) {
        final String email = getSharedPreferences("session", MODE_PRIVATE).getString("email", "");
        AsyncTask.execute(() -> {
            QuizResult r = new QuizResult();
            r.setId = setId;
            r.userEmail = email;
            r.correctCount = correct;
            r.totalCount = total;
            r.timestampMillis = System.currentTimeMillis();
            AppDatabase.getInstance(this).quizResultDao().insert(r);
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
