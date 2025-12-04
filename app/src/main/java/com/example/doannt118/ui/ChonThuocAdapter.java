package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.DuocPham;
import java.util.List;

public class ChonThuocAdapter extends RecyclerView.Adapter<ChonThuocAdapter.ViewHolder> {
    
    private List<DuocPham> danhSachThuoc;
    private OnThuocClickListener listener;

    public interface OnThuocClickListener {
        void onThuocClick(DuocPham duocPham);
    }

    public ChonThuocAdapter(List<DuocPham> danhSachThuoc, OnThuocClickListener listener) {
        this.danhSachThuoc = danhSachThuoc;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chon_thuoc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DuocPham thuoc = danhSachThuoc.get(position);
        holder.tvTenThuoc.setText(thuoc.getTenDuocPham());
        holder.tvDonVi.setText(thuoc.getDonViTinh());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThuocClick(thuoc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSachThuoc.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenThuoc, tvDonVi;

        ViewHolder(View itemView) {
            super(itemView);
            tvTenThuoc = itemView.findViewById(R.id.tvTenThuoc);
            tvDonVi = itemView.findViewById(R.id.tvDonVi);
        }
    }
}
