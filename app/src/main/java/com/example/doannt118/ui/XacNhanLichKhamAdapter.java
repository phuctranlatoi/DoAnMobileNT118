package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class XacNhanLichKhamAdapter extends RecyclerView.Adapter<XacNhanLichKhamAdapter.ViewHolder> {

    private Context context;
    private List<LichKham> lichKhamList;
    private OnLichKhamActionListener listener;
    private FirestoreRepository repo;

    public interface OnLichKhamActionListener {
        void onXacNhan(LichKham lichKham);
        void onTuChoi(LichKham lichKham);
    }

    public XacNhanLichKhamAdapter(Context context, List<LichKham> lichKhamList, OnLichKhamActionListener listener) {
        this.context = context;
        this.lichKhamList = lichKhamList;
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
        LichKham lichKham = lichKhamList.get(position);
        
        // Hiển thị thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvThoiGian.setText(sdf.format(lichKham.getNgayKham().toDate()));
        
        // Hiển thị số thứ tự
        holder.tvLyDo.setText("STT: " + lichKham.getSoThuTu());
        
        // Load tên bệnh nhân
        repo.getByField("BenhNhan", "maBenhNhan", lichKham.getMaBenhNhan(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String hoTen = querySnapshot.getDocuments().get(0).getString("hoTen");
                        String sdt = querySnapshot.getDocuments().get(0).getString("soDienThoai");
                        holder.tvBenhNhan.setText(hoTen);
                        holder.tvSoDienThoai.setText("SĐT: " + sdt);
                    }
                },
                e -> holder.tvBenhNhan.setText("Bệnh nhân"));
        
        // Hiển thị/ẩn nút dựa vào trạng thái
        String trangThai = lichKham.getTrangThai();
        if ("CHO_XAC_NHAN".equals(trangThai)) {
            holder.btnXacNhan.setVisibility(View.VISIBLE);
            holder.btnTuChoi.setVisibility(View.VISIBLE);
            holder.tvTrangThai.setVisibility(View.GONE);
        } else {
            holder.btnXacNhan.setVisibility(View.GONE);
            holder.btnTuChoi.setVisibility(View.GONE);
            holder.tvTrangThai.setVisibility(View.VISIBLE);
            
            if ("DA_XAC_NHAN".equals(trangThai)) {
                holder.tvTrangThai.setText("✓ Đã xác nhận");
                holder.tvTrangThai.setBackgroundResource(R.drawable.badge_success);
            } else if ("TU_CHOI".equals(trangThai)) {
                holder.tvTrangThai.setText("✗ Đã từ chối");
                holder.tvTrangThai.setBackgroundResource(R.drawable.badge_danger);
            }
        }
        
        // Xử lý sự kiện
        holder.btnXacNhan.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXacNhan(lichKham);
            }
        });
        
        holder.btnTuChoi.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTuChoi(lichKham);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lichKhamList.size();
    }

    public void updateData(List<LichKham> newData) {
        this.lichKhamList = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvBenhNhan, tvSoDienThoai, tvThoiGian, tvLyDo, tvTrangThai;
        Button btnXacNhan, btnTuChoi;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvBenhNhan = itemView.findViewById(R.id.tvBenhNhan);
            tvSoDienThoai = itemView.findViewById(R.id.tvSoDienThoai);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            tvLyDo = itemView.findViewById(R.id.tvLyDo);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            btnXacNhan = itemView.findViewById(R.id.btnXacNhan);
            btnTuChoi = itemView.findViewById(R.id.btnTuChoi);
        }
    }
}
