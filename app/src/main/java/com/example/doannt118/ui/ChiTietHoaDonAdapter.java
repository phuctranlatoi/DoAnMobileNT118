package com.example.doannt118.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietHoaDon;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.List;

public class ChiTietHoaDonAdapter extends RecyclerView.Adapter<ChiTietHoaDonAdapter.ViewHolder> {

    private Context context;
    private List<ChiTietHoaDon> chiTietList;
    private FirestoreRepository repo;

    public ChiTietHoaDonAdapter(Context context, List<ChiTietHoaDon> chiTietList) {
        this.context = context;
        this.chiTietList = chiTietList;
        this.repo = new FirestoreRepository();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chi_tiet_hoa_don, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietHoaDon chiTiet = chiTietList.get(position);
        
        // Load tên dược phẩm
        repo.getByField("DuocPham", "maDuocPham", chiTiet.getMaDuocPham(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String tenDuocPham = querySnapshot.getDocuments().get(0).getString("tenDuocPham");
                        String donViTinh = querySnapshot.getDocuments().get(0).getString("donViTinh");
                        holder.tvTenThuoc.setText(tenDuocPham);
                        holder.tvSoLuong.setText("Số lượng: " + chiTiet.getSoLuong() + " " + donViTinh);
                    }
                },
                e -> Log.e("ChiTietHoaDonAdapter", "Lỗi tải dược phẩm: ", e));
        
        holder.tvDonGia.setText(String.format("Đơn giá: %,.0f đ", chiTiet.getDonGia()));
        holder.tvThanhTien.setText(String.format("Thành tiền: %,.0f đ", chiTiet.getSoLuong() * chiTiet.getDonGia()));
    }

    @Override
    public int getItemCount() {
        return chiTietList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenThuoc, tvSoLuong, tvDonGia, tvThanhTien;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenThuoc = itemView.findViewById(R.id.tvTenThuoc);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvDonGia = itemView.findViewById(R.id.tvDonGia);
            tvThanhTien = itemView.findViewById(R.id.tvThanhTien);
        }
    }
}
