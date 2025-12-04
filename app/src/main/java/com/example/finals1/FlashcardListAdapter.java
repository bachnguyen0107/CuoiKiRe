package com.example.finals1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.Flashcard;

import java.util.List;

public class FlashcardListAdapter extends RecyclerView.Adapter<FlashcardListAdapter.VH> {

    private final List<Flashcard> items;

    public FlashcardListAdapter(List<Flashcard> items) {
        this.items = items;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTerm, tvDefinition;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
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
    }

    @Override
    public int getItemCount() { return items.size(); }
}
