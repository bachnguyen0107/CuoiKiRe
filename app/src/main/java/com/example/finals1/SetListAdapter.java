package com.example.finals1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.AppDatabase;
import com.example.finals1.data.FlashcardSet;
import com.example.finals1.data.QuizResult;

import java.util.List;

public class SetListAdapter extends RecyclerView.Adapter<SetListAdapter.VH> {

    public interface OnSetClickListener {
        void onSetClicked(FlashcardSet set);
        void onSetLongClicked(FlashcardSet set, int position);
    }

    private final List<FlashcardSet> items;
    private final OnSetClickListener listener;

    public SetListAdapter(List<FlashcardSet> items, OnSetClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvPercent;
        ProgressBar progressBar;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSetTitle);
            tvDesc = itemView.findViewById(R.id.tvSetDesc);
            progressBar = itemView.findViewById(R.id.progressSet);
            tvPercent = itemView.findViewById(R.id.tvSetPercent);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FlashcardSet set = items.get(position);
        holder.tvTitle.setText(set.title);
        holder.tvDesc.setText(set.description == null ? "" : set.description);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSetClicked(set);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onSetLongClicked(set, holder.getAdapterPosition());
            return true;
        });

        // Load last quiz result for this set and current user
        String email = holder.itemView.getContext().getSharedPreferences("session", holder.itemView.getContext().MODE_PRIVATE)
                .getString("email", "");
        new Thread(() -> {
            QuizResult last = AppDatabase.getInstance(holder.itemView.getContext())
                    .quizResultDao().getLastForUserAndSet(email, set.id);
            int percent = (last == null || last.totalCount == 0) ? 0 : (int) Math.round((last.correctCount * 100.0) / last.totalCount);
            holder.itemView.post(() -> {
                if (holder.progressBar != null) holder.progressBar.setProgress(percent);
                if (holder.tvPercent != null) holder.tvPercent.setText(percent + "%");
            });
        }).start();
    }

    @Override
    public int getItemCount() { return items.size(); }
}
