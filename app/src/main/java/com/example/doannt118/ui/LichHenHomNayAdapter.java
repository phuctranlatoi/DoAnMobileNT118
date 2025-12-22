package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.List;

public class LichHenHomNayAdapter extends RecyclerView.Adapter<LichHenHomNayAdapter.ViewHolder> {

    private List<LichKham> lichKhamList;
    private FirestoreRepository repo;

    public LichHenHomNayAdapter(List<LichKham> lichKhamList) {
        this.lichKhamList = lichKhamList;
        this.repo = new FirestoreRepository();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lich_hen_hom_nay, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichKham lichKham = lichKhamList.get(position);

        // Hiển thị giờ khám
        if (lichKham.getGioKham() != null && !lichKham.getGioKham().isEmpty()) {
            holder.tvGioKham.setText(lichKham.getGioKham());
        } else {
            holder.tvGioKham.setText("--:--");
        }

        // Load thông tin bệnh nhân
        if (lichKham.getMaBenhNhan() != null) {
            repo.getByField("BenhNhan", "maBenhNhan", lichKham.getMaBenhNhan(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        var doc = querySnapshot.getDocuments().get(0);
                        String hoTen = doc.getString("hoTen");
                        String sdt = doc.getString("soDienThoai");
                        holder.tvTenBenhNhan.setText(hoTen != null ? hoTen : "N/A");
                        holder.tvSoDienThoai.setText(sdt != null ? sdt : "");
                    }
                },
                e -> {
                    holder.tvTenBenhNhan.setText("N/A");
                    holder.tvSoDienThoai.setText("");
                });
        }

        // Trạng thái
        holder.tvTrangThai.setText("Đã xác nhận");
    }

    @Override
    public int getItemCount() {
        return lichKhamList.size();
    }

    public void updateData(List<LichKham> newList) {
        this.lichKhamList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGioKham, tvTenBenhNhan, tvSoDienThoai, tvTrangThai;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGioKham = itemView.findViewById(R.id.tvGioKham);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvSoDienThoai = itemView.findViewById(R.id.tvSoDienThoai);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}
