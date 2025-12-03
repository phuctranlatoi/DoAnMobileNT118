package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import java.util.List;

public class BenhNhanAdapter extends RecyclerView.Adapter<BenhNhanAdapter.BenhNhanViewHolder> {

    private List<BenhNhan> benhNhanList;
    private OnBenhNhanClickListener listener;

    public interface OnBenhNhanClickListener {
        void onBenhNhanClick(BenhNhan benhNhan);
    }

    public BenhNhanAdapter(List<BenhNhan> benhNhanList, OnBenhNhanClickListener listener) {
        this.benhNhanList = benhNhanList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BenhNhanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_benh_nhan, parent, false);
        return new BenhNhanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BenhNhanViewHolder holder, int position) {
        BenhNhan benhNhan = benhNhanList.get(position);
        holder.tvHoTen.setText("Họ tên: " + (benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "N/A"));
//        holder.tvMaBenhNhan.setText("Mã BN: " + (benhNhan.getMaBenhNhan() != null ? benhNhan.getMaBenhNhan() : "N/A"));
        holder.tvSoDienThoai.setText("SĐT: " + (benhNhan.getSoDienThoai() != null ? benhNhan.getSoDienThoai() : "N/A"));
        holder.itemView.setOnClickListener(v -> listener.onBenhNhanClick(benhNhan));
    }

    @Override
    public int getItemCount() {
        return benhNhanList.size();
    }

    static class BenhNhanViewHolder extends RecyclerView.ViewHolder {
        TextView tvHoTen, tvMaBenhNhan, tvSoDienThoai;

        public BenhNhanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHoTen = itemView.findViewById(R.id.tvHoTen);
            // tvMaBenhNhan = itemView.findViewById(R.id.tvMaBenhNhan);
            tvSoDienThoai = itemView.findViewById(R.id.tvSoDienThoai);
        }
    }
}