package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LichSuKhamAdapter extends RecyclerView.Adapter<LichSuKhamAdapter.ViewHolder> {

    private Context context;
    private List<BenhAn> danhSach;
    private OnItemClickListener listener;
    private com.example.doannt118.repository.FirestoreRepository repository;

    public interface OnItemClickListener {
        void onXemChiTiet(BenhAn benhAn);
    }

    public LichSuKhamAdapter(Context context, List<BenhAn> danhSach, OnItemClickListener listener) {
        this.context = context;
        this.danhSach = danhSach;
        this.listener = listener;
        this.repository = new com.example.doannt118.repository.FirestoreRepository();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_su_kham, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BenhAn benhAn = danhSach.get(position);
        
        // Format ngày khám
        if (benhAn.getNgayKhamAsTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = benhAn.getNgayKhamAsTimestamp().toDate();
            holder.tvNgayKham.setText(sdf.format(date));
        } else if (benhAn.getNgayKham() instanceof String) {
            holder.tvNgayKham.setText((String) benhAn.getNgayKham());
        } else {
            holder.tvNgayKham.setText("N/A");
        }
        
        // Hiển thị chuẩn đoán
        if (benhAn.getChanDoan() != null && !benhAn.getChanDoan().isEmpty()) {
            holder.tvChuanDoan.setText("Chuẩn đoán: " + benhAn.getChanDoan());
        } else {
            holder.tvChuanDoan.setText("Chuẩn đoán: Chưa có");
        }
        
        // Trạng thái mặc định
        holder.tvTrangThai.setText("Hoàn thành");
        
        // Load tên bác sĩ
        loadBacSiInfo(benhAn.getMaBacSi(), holder.tvBacSi);
        
        // Click trên toàn bộ card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXemChiTiet(benhAn);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    public void updateData(List<BenhAn> newData) {
        this.danhSach = newData;
        notifyDataSetChanged();
    }
    
    private void loadBacSiInfo(String maBacSi, TextView textView) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            textView.setText("BS. N/A");
            return;
        }
        
        repository.getByField("BacSi", "maBacSi", maBacSi,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    com.google.firebase.firestore.DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String hoTen = doc.getString("hoTen");
                    textView.setText("BS. " + (hoTen != null ? hoTen : maBacSi));
                } else {
                    textView.setText("BS. " + maBacSi);
                }
            },
            e -> textView.setText("BS. " + maBacSi)
        );
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNgayKham, tvBacSi, tvChuanDoan, tvTrangThai;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvBacSi = itemView.findViewById(R.id.tvBacSi);
            tvChuanDoan = itemView.findViewById(R.id.tvChuanDoan);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}
