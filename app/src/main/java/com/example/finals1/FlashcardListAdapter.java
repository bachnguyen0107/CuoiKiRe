package com.example.finals1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.Flashcard;

import java.util.List;

public class FlashcardListAdapter extends RecyclerView.Adapter<FlashcardListAdapter.VH> {

    public interface OnFlashcardActionListener {
        void onEditFlashcard(Flashcard card, int position);
        void onDeleteFlashcard(Flashcard card, int position);
    }

    private final List<Flashcard> items;
    private final OnFlashcardActionListener listener;

    public FlashcardListAdapter(List<Flashcard> items) {
        this(items, null);
    }

    public FlashcardListAdapter(List<Flashcard> items, OnFlashcardActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTerm, tvDefinition;
        Button btnEdit, btnDelete;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Flashcard card = items.get(position);
        holder.tvTerm.setText(card.term);
        holder.tvDefinition.setText(card.definition);
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditFlashcard(card, holder.getAdapterPosition());
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteFlashcard(card, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }
}
