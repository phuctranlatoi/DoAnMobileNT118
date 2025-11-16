package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class XacNhanLichKhamAdapter extends RecyclerView.Adapter<XacNhanLichKhamAdapter.ViewHolder> {

    private Context context;
    private List<LichKham> danhSachLichKham;
    private OnLichKhamActionListener listener;
    private FirestoreRepository repo;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

    public interface OnLichKhamActionListener {
        void onXacNhan(LichKham lichKham);
        void onHuy(LichKham lichKham);
    }

    public XacNhanLichKhamAdapter(Context context, List<LichKham> danhSachLichKham, OnLichKhamActionListener listener) {
        this.context = context;
        this.danhSachLichKham = danhSachLichKham;
        this.listener = listener;
        this.repo = new FirestoreRepository();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_xac_nhan_lich_kham, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichKham lichKham = danhSachLichKham.get(position);
        
        // Hiển thị số thứ tự
        holder.tvSoThuTu.setText(String.valueOf(lichKham.getSoThuTu()));
        
        // Load tên bệnh nhân
        loadTenBenhNhan(lichKham.getMaBenhNhan(), holder.tvTenBenhNhan);
        
        // Hiển thị ngày khám
        if (lichKham.getNgayKham() != null) {
            holder.tvNgayKham.setText(dateFormat.format(lichKham.getNgayKham().toDate()));
        }
        
        // Hiển thị trạng thái
        String trangThai = lichKham.getTrangThai();
        if ("CHO".equals(trangThai)) {
            holder.tvTrangThai.setText("Chờ");
            holder.tvTrangThai.setBackgroundResource(R.drawable.badge_background);
            holder.tvTrangThai.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFA726));
            holder.layoutButtons.setVisibility(View.VISIBLE);
        } else if ("XAC_NHAN".equals(trangThai)) {
            holder.tvTrangThai.setText("Đã xác nhận");
            holder.tvTrangThai.setBackgroundResource(R.drawable.badge_success);
            holder.tvTrangThai.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            holder.layoutButtons.setVisibility(View.GONE);
        } else if ("HUY".equals(trangThai)) {
            holder.tvTrangThai.setText("Đã từ chối");
            holder.tvTrangThai.setBackgroundResource(R.drawable.badge_danger);
            holder.tvTrangThai.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE74C3C));
            holder.layoutButtons.setVisibility(View.GONE);
        } else {
            holder.tvTrangThai.setText(trangThai);
            holder.tvTrangThai.setBackgroundResource(R.drawable.badge_background);
            holder.layoutButtons.setVisibility(View.GONE);
        }
        
        // Xử lý sự kiện nút
        holder.btnXacNhan.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXacNhan(lichKham);
            }
        });
        
        holder.btnHuy.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHuy(lichKham);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSachLichKham.size();
    }

    public void updateData(List<LichKham> newData) {
        this.danhSachLichKham = newData;
        notifyDataSetChanged();
    }

    private void loadTenBenhNhan(String maBenhNhan, TextView textView) {
        repo.getByField("BenhNhan", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String hoTen = querySnapshot.getDocuments().get(0).getString("hoTen");
                    textView.setText(hoTen != null ? hoTen : "Không rõ");
                } else {
                    textView.setText("Không rõ");
                }
            },
            e -> textView.setText("Lỗi"));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSoThuTu, tvTenBenhNhan, tvNgayKham, tvTrangThai;
        Button btnXacNhan, btnHuy;
        LinearLayout layoutButtons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSoThuTu = itemView.findViewById(R.id.tvSoThuTu);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            btnXacNhan = itemView.findViewById(R.id.btnXacNhan);
            btnHuy = itemView.findViewById(R.id.btnHuy);
            layoutButtons = itemView.findViewById(R.id.layoutButtons);
        }
    }
}
