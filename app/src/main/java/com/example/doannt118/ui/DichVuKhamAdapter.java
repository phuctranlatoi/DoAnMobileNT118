package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.DichVuKham;

import java.util.ArrayList;
import java.util.List;

public class DichVuKhamAdapter extends RecyclerView.Adapter<DichVuKhamAdapter.ViewHolder> {
    
    private List<DichVuKham> danhSach = new ArrayList<>();
    private List<DichVuKham> danhSachChon = new ArrayList<>();
    private OnTongTienChangedListener listener;
    
    public interface OnTongTienChangedListener {
        void onTongTienChanged(long tongTien);
    }
    
    public DichVuKhamAdapter(OnTongTienChangedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dich_vu_kham, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DichVuKham dv = danhSach.get(position);
        
        holder.tvTenDichVu.setText(dv.getTenDichVu());
        holder.tvMoTa.setText(dv.getMoTa());
        holder.tvGiaTien.setText(dv.getGiaTienFormatted());
        
        holder.cbChon.setChecked(danhSachChon.contains(dv));
        
        holder.cbChon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!danhSachChon.contains(dv)) {
                    danhSachChon.add(dv);
                }
            } else {
                danhSachChon.remove(dv);
            }
            if (listener != null) {
                listener.onTongTienChanged(tinhTongTien());
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            holder.cbChon.setChecked(!holder.cbChon.isChecked());
        });
    }
    
    @Override
    public int getItemCount() {
        return danhSach.size();
    }
    
    public void updateData(List<DichVuKham> newList) {
        this.danhSach = newList;
        notifyDataSetChanged();
    }
    
    public List<DichVuKham> getDanhSachChon() {
        return danhSachChon;
    }
    
    public long getTongTien() {
        return tinhTongTien();
    }
    
    private long tinhTongTien() {
        long tong = 0;
        for (DichVuKham dv : danhSachChon) {
            tong += dv.getGiaTien();
        }
        return tong;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbChon;
        TextView tvTenDichVu, tvMoTa, tvGiaTien;
        
        ViewHolder(View itemView) {
            super(itemView);
            cbChon = itemView.findViewById(R.id.cbChon);
            tvTenDichVu = itemView.findViewById(R.id.tvTenDichVu);
            tvMoTa = itemView.findViewById(R.id.tvMoTa);
            tvGiaTien = itemView.findViewById(R.id.tvGiaTien);
        }
    }
}
