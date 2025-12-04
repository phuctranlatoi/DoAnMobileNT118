package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GhiChuAdapter extends RecyclerView.Adapter<GhiChuAdapter.GhiChuViewHolder> {

    private List<BenhAn> ghiChuList;
    private OnGhiChuClickListener listener;

    public interface OnGhiChuClickListener {
        void onGhiChuClick(BenhAn benhAn);
    }

    public GhiChuAdapter(List<BenhAn> ghiChuList, OnGhiChuClickListener listener) {
        this.ghiChuList = ghiChuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GhiChuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ghichu, parent, false);
        return new GhiChuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GhiChuViewHolder holder, int position) {
        BenhAn benhAn = ghiChuList.get(position);
        
        holder.tvMaBenhAn.setText("Mã bệnh án: " + (benhAn.getMaBenhAn() != null ? benhAn.getMaBenhAn() : "N/A"));
        holder.tvGhiChu.setText(benhAn.getGhiChu() != null ? benhAn.getGhiChu() : "Không có ghi chú");
        
        // Xử lý ngày khám với cả String và Timestamp
        if (benhAn.getNgayKhamAsTimestamp() != null) {
            holder.tvNgayKham.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(benhAn.getNgayKhamAsTimestamp().toDate()));
        } else if (benhAn.getNgayKham() instanceof String) {
            holder.tvNgayKham.setText((String) benhAn.getNgayKham());
        } else {
            holder.tvNgayKham.setText("N/A");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGhiChuClick(benhAn);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ghiChuList.size();
    }

    static class GhiChuViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaBenhAn, tvGhiChu, tvNgayKham;

        public GhiChuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaBenhAn = itemView.findViewById(R.id.tvMaBenhAn);
            tvGhiChu = itemView.findViewById(R.id.tvGhiChu);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
        }
    }
}
