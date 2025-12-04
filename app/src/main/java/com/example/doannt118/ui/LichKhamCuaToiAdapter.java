package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LichKhamCuaToiAdapter extends RecyclerView.Adapter<LichKhamCuaToiAdapter.ViewHolder> {

    private Context context;
    private List<LichKham> danhSach;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onXemChiTiet(LichKham lichKham);
    }

    public LichKhamCuaToiAdapter(Context context, List<LichKham> danhSach, OnItemClickListener listener) {
        this.context = context;
        this.danhSach = danhSach;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_kham_cua_toi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichKham lichKham = danhSach.get(position);
        
        // Trạng thái
        String trangThai = lichKham.getTrangThai();
        if ("CHO".equals(trangThai)) {
            holder.tvTrangThai.setText("⏳ Chờ xác nhận");
            holder.tvTrangThai.setTextColor(context.getColor(R.color.warning));
        } else if ("XAC_NHAN".equals(trangThai)) {
            holder.tvTrangThai.setText("✅ Đã xác nhận");
            holder.tvTrangThai.setTextColor(context.getColor(R.color.success));
            // Hiển thị mã khám nếu có
            if (lichKham.getMaKhamBenh() != null && !lichKham.getMaKhamBenh().isEmpty()) {
                holder.layoutMaKham.setVisibility(View.VISIBLE);
                holder.tvMaKham.setText(lichKham.getMaKhamBenh());
            }
        } else if ("HOAN_THANH".equals(trangThai)) {
            holder.tvTrangThai.setText("✓ Hoàn thành");
            holder.tvTrangThai.setTextColor(context.getColor(R.color.primary));
        } else if ("HUY".equals(trangThai)) {
            holder.tvTrangThai.setText("✗ Đã hủy");
            holder.tvTrangThai.setTextColor(context.getColor(R.color.danger));
            // Hiển thị lý do từ chối
            if (lichKham.getLyDoTuChoi() != null && !lichKham.getLyDoTuChoi().isEmpty()) {
                holder.layoutLyDoTuChoi.setVisibility(View.VISIBLE);
                holder.tvLyDoTuChoi.setText(lichKham.getLyDoTuChoi());
            }
        }
        
        // Ngày khám
        if (lichKham.getNgayKham() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = lichKham.getNgayKham().toDate();
            String ngayKhamStr = sdf.format(date);
            
            // Thêm giờ khám nếu có
            if (lichKham.getGioKham() != null && !lichKham.getGioKham().isEmpty()) {
                holder.tvNgayKham.setText(ngayKhamStr + " - " + lichKham.getGioKham());
            } else {
                // Fallback: hiển thị giờ từ timestamp
                SimpleDateFormat sdfTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                holder.tvNgayKham.setText(sdfTime.format(date));
            }
        }
        
        // Lý do khám
        if (lichKham.getLyDoKham() != null && !lichKham.getLyDoKham().isEmpty()) {
            holder.layoutLyDo.setVisibility(View.VISIBLE);
            holder.tvLyDoKham.setText(lichKham.getLyDoKham());
        }
        
        // Hiển thị tên bác sĩ
        if (lichKham.getTenBacSi() != null && !lichKham.getTenBacSi().isEmpty()) {
            holder.tvBacSi.setText("BS. " + lichKham.getTenBacSi());
        } else {
            holder.tvBacSi.setText("Bác sĩ: " + lichKham.getMaBacSi());
        }
        
        // Kiểm tra quá hạn (chỉ với trạng thái CHO và XAC_NHAN)
        if (("CHO".equals(lichKham.getTrangThai()) || "XAC_NHAN".equals(lichKham.getTrangThai())) 
            && lichKham.getNgayKham() != null) {
            long now = System.currentTimeMillis();
            long ngayKhamTime = lichKham.getNgayKham().toDate().getTime();
            
            // Nếu đã qua ngày khám (quá 24h)
            if (ngayKhamTime + 24 * 60 * 60 * 1000 < now) {
                holder.tvTrangThai.setText("⚠️ Quá hạn");
                holder.tvTrangThai.setTextColor(context.getColor(R.color.danger));
            }
        }
        
        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXemChiTiet(lichKham);
            }
        });
        
        holder.btnXemChiTiet.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXemChiTiet(lichKham);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    public void updateData(List<LichKham> newData) {
        this.danhSach = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTrangThai, tvNgayDangKy, tvMaKham, tvBacSi, tvNgayKham, tvLyDoKham, tvLyDoTuChoi;
        LinearLayout layoutMaKham, layoutLyDo, layoutLyDoTuChoi;
        Button btnXemChiTiet;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvNgayDangKy = itemView.findViewById(R.id.tvNgayDangKy);
            tvMaKham = itemView.findViewById(R.id.tvMaKham);
            tvBacSi = itemView.findViewById(R.id.tvBacSi);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvLyDoKham = itemView.findViewById(R.id.tvLyDoKham);
            tvLyDoTuChoi = itemView.findViewById(R.id.tvLyDoTuChoi);
            layoutMaKham = itemView.findViewById(R.id.layoutMaKham);
            layoutLyDo = itemView.findViewById(R.id.layoutLyDo);
            layoutLyDoTuChoi = itemView.findViewById(R.id.layoutLyDoTuChoi);
            btnXemChiTiet = itemView.findViewById(R.id.btnXemChiTiet);
        }
    }
}
