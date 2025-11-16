package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;
import java.util.List;

public class KhungGioAdapter extends RecyclerView.Adapter<KhungGioAdapter.ViewHolder> {

    private List<LichLamViec> lichLamViecList;
    private OnKhungGioClickListener listener;
    private int selectedPosition = -1;

    public interface OnKhungGioClickListener {
        void onKhungGioClick(LichLamViec lichLamViec, int position);
    }

    public KhungGioAdapter(List<LichLamViec> lichLamViecList, OnKhungGioClickListener listener) {
        this.lichLamViecList = lichLamViecList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_khung_gio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichLamViec lichLamViec = lichLamViecList.get(position);
        holder.tvKhungGio.setText(lichLamViec.getCaLamViec());

        // Highlight selected item
        if (position == selectedPosition) {
            holder.cardKhungGio.setCardBackgroundColor(0xFF2196F3);
            holder.tvKhungGio.setTextColor(0xFFFFFFFF);
        } else {
            holder.cardKhungGio.setCardBackgroundColor(0xFFFFFFFF);
            holder.tvKhungGio.setTextColor(0xFF2C3E50);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onKhungGioClick(lichLamViec, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lichLamViecList.size();
    }

    public LichLamViec getSelectedItem() {
        if (selectedPosition >= 0 && selectedPosition < lichLamViecList.size()) {
            return lichLamViecList.get(selectedPosition);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardKhungGio;
        TextView tvKhungGio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardKhungGio = itemView.findViewById(R.id.cardKhungGio);
            tvKhungGio = itemView.findViewById(R.id.tvKhungGio);
        }
    }
}
