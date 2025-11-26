package com.example.doannt118.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LichLamViecNewAdapter extends RecyclerView.Adapter<LichLamViecNewAdapter.ViewHolder> {

    private Context context;
    private List<LichLamViec> danhSachLichLamViec;
    private Map<String, Integer> soBenhNhanMap; // Map lưu số bệnh nhân cho mỗi lịch làm việc
    private FirestoreRepository repo;

    public LichLamViecNewAdapter(Context context, List<LichLamViec> danhSachLichLamViec) {
        this.context = context;
        this.danhSachLichLamViec = danhSachLichLamViec;
        this.soBenhNhanMap = new HashMap<>();
        this.repo = new FirestoreRepository();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_lam_viec_new, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichLamViec lichLamViec = danhSachLichLamViec.get(position);
        
        // Hiển thị ca làm việc với loại hình
        String loaiHinh = lichLamViec.getLoaiHinh();
        String caLamViec = lichLamViec.getCaLamViec();
        
        if ("ONLINE".equals(loaiHinh)) {
            holder.tvCaLamViec.setText("🌐 " + caLamViec + " (Online)");
            holder.tvLoaiHinh.setText("Online");
            holder.tvLoaiHinh.setBackgroundResource(R.drawable.badge_info);
            holder.tvLoaiHinh.setVisibility(View.VISIBLE);
        } else {
            holder.tvCaLamViec.setText("🏥 " + caLamViec + " (Tại phòng)");
            holder.tvLoaiHinh.setText("Tại phòng");
            holder.tvLoaiHinh.setBackgroundResource(R.drawable.badge_primary);
            holder.tvLoaiHinh.setVisibility(View.VISIBLE);
        }
        
        // Load số bệnh nhân
        loadSoBenhNhan(lichLamViec, holder);
        
        // Xử lý click để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, XemChiTietLichKhamActivity.class);
            intent.putExtra("MA_LICH_LAM_VIEC", lichLamViec.getMaLichLamViec());
            intent.putExtra("CA_LAM_VIEC", lichLamViec.getCaLamViec());
            intent.putExtra("LOAI_HINH", lichLamViec.getLoaiHinh());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return danhSachLichLamViec.size();
    }

    public void updateData(List<LichLamViec> newData) {
        this.danhSachLichLamViec = newData;
        this.soBenhNhanMap.clear();
        notifyDataSetChanged();
    }

    private void loadSoBenhNhan(LichLamViec lichLamViec, ViewHolder holder) {
        String maLichLamViec = lichLamViec.getMaLichLamViec();
        
        // Kiểm tra cache
        if (soBenhNhanMap.containsKey(maLichLamViec)) {
            updateSoBenhNhanUI(holder, soBenhNhanMap.get(maLichLamViec));
            return;
        }
        
        // Load từ Firestore
        repo.getByField("LichKham", "maLichLamViec", maLichLamViec,
            querySnapshot -> {
                int count = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String trangThai = doc.getString("trangThai");
                    // Chỉ đếm lịch khám chưa hủy
                    if (!"HUY".equals(trangThai)) {
                        count++;
                    }
                }
                soBenhNhanMap.put(maLichLamViec, count);
                updateSoBenhNhanUI(holder, count);
            },
            e -> {
                holder.tvSoBenhNhan.setText("0/6 bệnh nhân");
                holder.tvTrangThai.setText("Còn trống");
                holder.tvTrangThai.setBackgroundResource(R.drawable.badge_success);
            });
    }

    private void updateSoBenhNhanUI(ViewHolder holder, int soBenhNhan) {
        int soLuongToiDa = 6;
        holder.tvSoBenhNhan.setText(soBenhNhan + "/" + soLuongToiDa + " bệnh nhân");
        
        if (soBenhNhan >= soLuongToiDa) {
            holder.tvTrangThai.setText("Đã đầy");
            holder.tvTrangThai.setBackgroundResource(R.drawable.badge_danger);
        } else {
            holder.tvTrangThai.setText("Còn trống");
            holder.tvTrangThai.setBackgroundResource(R.drawable.badge_success);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCaLamViec, tvSoBenhNhan, tvTrangThai, tvLoaiHinh;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCaLamViec = itemView.findViewById(R.id.tvCaLamViec);
            tvSoBenhNhan = itemView.findViewById(R.id.tvSoBenhNhan);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvLoaiHinh = itemView.findViewById(R.id.tvLoaiHinh);
        }
    }
}
