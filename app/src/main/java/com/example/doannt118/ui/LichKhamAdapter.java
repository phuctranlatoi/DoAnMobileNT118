package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LichKhamAdapter extends RecyclerView.Adapter<LichKhamAdapter.LichKhamViewHolder> {

    private List<LichKham> lichKhamList;
    private OnLichKhamActionListener listener;
    private FirestoreRepository repo;

    public interface OnLichKhamActionListener {
        void onHuyLichKham(LichKham lichKham);
    }

    public LichKhamAdapter(List<LichKham> lichKhamList, OnLichKhamActionListener listener) {
        this.lichKhamList = lichKhamList;
        this.listener = listener;
        this.repo = new FirestoreRepository();
    }

    @NonNull
    @Override
    public LichKhamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lichkham, parent, false);
        return new LichKhamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichKhamViewHolder holder, int position) {
        LichKham lichKham = lichKhamList.get(position);
        
        // Hiển thị ngày khám
        if (lichKham.getNgayKham() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvNgayKham.setText(sdf.format(lichKham.getNgayKham().toDate()));
        }

        // Hiển thị trạng thái
        String trangThai = lichKham.getTrangThai();
        if ("CHO".equals(trangThai)) {
            holder.tvTrangThai.setText("Chờ xác nhận");
            holder.tvTrangThai.setBackgroundResource(R.drawable.badge_background);
            holder.btnHuy.setVisibility(View.VISIBLE);
        } else if ("XAC_NHAN".equals(trangThai)) {
            holder.tvTrangThai.setText("Đã xác nhận");
            holder.tvTrangThai.setBackgroundColor(0xFF4CAF50);
            holder.btnHuy.setVisibility(View.GONE);
        } else if ("HOAN_THANH".equals(trangThai)) {
            holder.tvTrangThai.setText("Hoàn thành");
            holder.tvTrangThai.setBackgroundColor(0xFF9E9E9E);
            holder.btnHuy.setVisibility(View.GONE);
        } else {
            holder.tvTrangThai.setText("Đã hủy");
            holder.tvTrangThai.setBackgroundColor(0xFFE74C3C);
            holder.btnHuy.setVisibility(View.GONE);
        }

        // Hiển thị số thứ tự
        holder.tvSoThuTu.setText("Số thứ tự: " + lichKham.getSoThuTu());

        // Load khung giờ từ LichLamViec
        if (lichKham.getMaLichLamViec() != null) {
            repo.getByField("LichLamViec", "maLichLamViec", lichKham.getMaLichLamViec(),
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String caLamViec = querySnapshot.getDocuments().get(0).getString("caLamViec");
                            holder.tvKhungGio.setText(caLamViec != null ? caLamViec : "N/A");
                        }
                    },
                    e -> holder.tvKhungGio.setText("N/A"));
        }

        // Load tên bác sĩ
        if (lichKham.getMaBacSi() != null) {
            repo.getByField("BacSi", "maBacSi", lichKham.getMaBacSi(),
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String hoTen = querySnapshot.getDocuments().get(0).getString("hoTen");
                            holder.tvBacSi.setText("BS. " + (hoTen != null ? hoTen : "N/A"));
                        }
                    },
                    e -> holder.tvBacSi.setText("BS. N/A"));
        }

        // Xử lý nút hủy
        holder.btnHuy.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHuyLichKham(lichKham);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lichKhamList.size();
    }

    static class LichKhamViewHolder extends RecyclerView.ViewHolder {
        TextView tvNgayKham, tvTrangThai, tvKhungGio, tvBacSi, tvSoThuTu;
        Button btnHuy;

        public LichKhamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvKhungGio = itemView.findViewById(R.id.tvKhungGio);
            tvBacSi = itemView.findViewById(R.id.tvBacSi);
            tvSoThuTu = itemView.findViewById(R.id.tvSoThuTu);
            btnHuy = itemView.findViewById(R.id.btnHuy);
        }
    }
}
