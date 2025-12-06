package com.example.finals1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finals1.data.FlashcardSet;

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
        TextView tvTitle, tvDesc;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSetTitle);
            tvDesc = itemView.findViewById(R.id.tvSetDesc);
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
    }

    @Override
    public int getItemCount() { return items.size(); }
}
