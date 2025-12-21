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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DichVuKhamAdapter extends RecyclerView.Adapter<DichVuKhamAdapter.ViewHolder> {
    
    private List<DichVuKham> danhSach = new ArrayList<>();
    private List<DichVuKham> danhSachChon = new ArrayList<>();
    private Set<String> maDichVuDaChon = new HashSet<>(); // Lưu mã dịch vụ đã chọn
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
        
        // Clear listener trước để tránh trigger không mong muốn
        holder.cbChon.setOnCheckedChangeListener(null);
        
        // Kiểm tra xem dịch vụ này có trong danh sách đã chọn không
        boolean isSelected = maDichVuDaChon.contains(dv.getMaDichVu());
        holder.cbChon.setChecked(isSelected);
        
        // Set listener sau khi đã set trạng thái
        holder.cbChon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Thêm vào cả Set và List
                maDichVuDaChon.add(dv.getMaDichVu());
                if (!danhSachChon.contains(dv)) {
                    danhSachChon.add(dv);
                }
            } else {
                // Xóa khỏi cả Set và List
                maDichVuDaChon.remove(dv.getMaDichVu());
                removeSelectedService(dv);
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
        // Cập nhật lại tổng tiền sau khi thay đổi data
        if (listener != null) {
            listener.onTongTienChanged(tinhTongTien());
        }
    }
    
    // Kiểm tra dịch vụ có được chọn không dựa trên mã dịch vụ
    private boolean isServiceSelected(DichVuKham dichVu) {
        if (dichVu == null || dichVu.getMaDichVu() == null) {
            return false;
        }
        
        for (DichVuKham selected : danhSachChon) {
            if (selected != null && selected.getMaDichVu() != null && 
                selected.getMaDichVu().equals(dichVu.getMaDichVu())) {
                return true;
            }
        }
        return false;
    }
    
    // Xóa dịch vụ khỏi danh sách đã chọn dựa trên mã dịch vụ
    private void removeSelectedService(DichVuKham dichVu) {
        if (dichVu == null || dichVu.getMaDichVu() == null) {
            return;
        }
        
        danhSachChon.removeIf(selected -> 
            selected != null && selected.getMaDichVu() != null &&
            selected.getMaDichVu().equals(dichVu.getMaDichVu()));
    }
    
    public List<DichVuKham> getDanhSachChon() {
        return danhSachChon;
    }
    
    public long getTongTien() {
        return tinhTongTien();
    }
    
    // Method để debug - in ra danh sách đã chọn
    public void printSelectedServices() {
        System.out.println("=== DANH SÁCH DỊCH VỤ ĐÃ CHỌN ===");
        System.out.println("Số lượng trong Set: " + maDichVuDaChon.size());
        System.out.println("Số lượng trong List: " + danhSachChon.size());
        System.out.println("Mã dịch vụ trong Set: " + maDichVuDaChon);
        for (DichVuKham dv : danhSachChon) {
            System.out.println("- " + dv.getMaDichVu() + ": " + dv.getTenDichVu() + " - " + dv.getGiaTienFormatted());
        }
        System.out.println("Tổng tiền: " + String.format("%,d đ", tinhTongTien()));
        System.out.println("=====================================");
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
