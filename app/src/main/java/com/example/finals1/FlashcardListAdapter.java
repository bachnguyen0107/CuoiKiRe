package com.example.finals1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
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
        View cardFront, cardBack, cardContainer;
        boolean showingFront = true;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardFront = itemView.findViewById(R.id.cardFront);
            cardBack = itemView.findViewById(R.id.cardBack);
            cardContainer = itemView.findViewById(R.id.cardContainer);
        }
        void bindFlipHandlers() {
            View.OnClickListener flip = v -> flipCard();
            cardFront.setOnClickListener(flip);
            cardBack.setOnClickListener(flip);
        }
        void flipCard() {
            final View showView = showingFront ? cardBack : cardFront;
            final View hideView = showingFront ? cardFront : cardBack;
            // First half: rotate hideView to 90 degrees
            ViewPropertyAnimator anim1 = hideView.animate().rotationY(90f).setDuration(150);
            anim1.setListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    hideView.setVisibility(View.GONE);
                    showView.setRotationY(270f); // continue from backside
                    showView.setVisibility(View.VISIBLE);
                    // Second half: rotate showView back to 360 (0)
                    showView.animate().rotationY(360f).setDuration(150).setListener(null).start();
                    showingFront = !showingFront;
                }
            });
            anim1.start();
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        VH vh = new VH(v);
        vh.bindFlipHandlers();
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Flashcard card = items.get(position);
        holder.tvTerm.setText(card.term);
        holder.tvDefinition.setText(card.definition);
        // ensure front is visible initially
        holder.cardFront.setVisibility(View.VISIBLE);
        holder.cardFront.setRotationY(0f);
        holder.cardBack.setVisibility(View.GONE);
        holder.cardBack.setRotationY(180f);
        holder.showingFront = true;
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
