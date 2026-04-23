package com.example.doannt118.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.ThongBao;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ViewHolder> {
    private List<ThongBao> thongBaoList;
    private OnThongBaoClickListener listener;

    public interface OnThongBaoClickListener {
        void onThongBaoClick(ThongBao thongBao);
    }

    public ThongBaoAdapter(List<ThongBao> thongBaoList, OnThongBaoClickListener listener) {
        this.thongBaoList = thongBaoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thong_bao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThongBao tb = thongBaoList.get(position);
        
        holder.tvTieuDe.setText(tb.getTieuDe());
        holder.tvNoiDung.setText(tb.getNoiDung());
        
        // Null check cho thời gian
        if (tb.getThoiGianGui() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvThoiGian.setText(sdf.format(tb.getThoiGianGui().toDate()));
        } else {
            holder.tvThoiGian.setText("Không xác định");
        }

        // Icon theo loại thông báo
        switch (tb.getLoaiThongBao()) {
            case "LICH_HEN":
                holder.ivIcon.setImageResource(R.drawable.ic_calendar);
                break;
            case "NHAC_THUOC":
                holder.ivIcon.setImageResource(R.drawable.ic_medication);
                break;
            default:
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
                break;
        }

        // Hiển thị khác nếu chưa đọc
        if (!tb.isDaDoc()) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.unread_notification));
            holder.tvTieuDe.setTypeface(null, Typeface.BOLD);
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.white));
            holder.tvTieuDe.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThongBaoClick(tb);
            }
        });
    }

    @Override
    public int getItemCount() {
        return thongBaoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        TextView tvTieuDe, tvNoiDung, tvThoiGian;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDung);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
        }
    }
}
