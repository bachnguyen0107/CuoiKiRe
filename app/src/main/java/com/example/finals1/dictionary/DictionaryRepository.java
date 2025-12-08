package com.example.finals1.dictionary;

import android.content.Context;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.DictionaryDao;
import com.example.finals1.data.DictionaryEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Simple repository that fetches a definition over HTTP and caches it in Room.
 * Public API used: https://api.dictionaryapi.dev/api/v2/entries/en/<word>
 */
public class DictionaryRepository {
    private final OkHttpClient client = new OkHttpClient();
    private final DictionaryDao dao;

    public DictionaryRepository(Context ctx) {
        this.dao = AppDatabase.getInstance(ctx).dictionaryDao();
    }

    public DictionaryEntry lookup(String word) throws IOException {
        if (word == null || word.trim().isEmpty()) return null;
        word = word.trim().toLowerCase();
        // Try cache first
        DictionaryEntry cached = dao.getLatest(word);
        if (cached != null) {
            return cached;
        }
        // Fetch from public API
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
        Request req = new Request.Builder().url(url).get().build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code());
            }
            String body = resp.body() != null ? new String(resp.body().bytes(), StandardCharsets.UTF_8) : "";
            String def = extractFirstDefinition(body);
            DictionaryEntry entry = new DictionaryEntry();
            entry.word = word;
            entry.definition = def != null ? def : "Definition not found";
            entry.fetchedAtMillis = System.currentTimeMillis();
            dao.insert(entry);
            return dao.getLatest(word);
        }
    }

    // Very small JSON extraction without adding a JSON library
    // Looks for something like: "definition":"..."
    private String extractFirstDefinition(String json) {
        if (json == null) return null;
        // Find first occurrence of "definition":"..."
        String needle = "\"definition\":";
        int i = json.indexOf(needle);
        if (i == -1) return null;
        int start = json.indexOf('"', i + needle.length());
        if (start == -1) return null;
        start += 1;
        int end = json.indexOf('"', start);
        if (end == -1) return null;
        String raw = json.substring(start, end);
        // Unescape basic sequences
        return raw.replace("\\\"", "\"").replace("\\n", " ").trim();
    }
}

