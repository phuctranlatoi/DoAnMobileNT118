package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LichKhamAdapter extends RecyclerView.Adapter<LichKhamAdapter.LichKhamViewHolder> {
    private List<LichKham> lichKhamList;
    private Context context;
    private HashMap<String, String> benhNhanMap;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public LichKhamAdapter(Context context, List<LichKham> lichKhamList, HashMap<String, String> benhNhanMap) {
        this.context = context;
        this.lichKhamList = lichKhamList;
        this.benhNhanMap = benhNhanMap != null ? benhNhanMap : new HashMap<>();
    }

    @NonNull
    @Override
    public LichKhamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_kham, parent, false);
        return new LichKhamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichKhamViewHolder holder, int position) {
        LichKham lich = lichKhamList.get(position);

        holder.tvMaLichKham.setText(lich.getMaLichKham() != null ? lich.getMaLichKham() : "N/A");
        holder.tvTenBenhNhan.setText(benhNhanMap.getOrDefault(lich.getMaBenhNhan(), "Không rõ BN"));
        holder.tvTrangThai.setText(lich.getTrangThai() != null ? lich.getTrangThai() : "N/A");
        holder.tvNgayKham.setText(lich.getNgayKham() != null ? DATE_FORMAT.format(lich.getNgayKham()) : "N/A");

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Đã chọn lịch khám: " + lich.getMaLichKham(), Toast.LENGTH_SHORT).show();
            // Có thể thêm logic mở chi tiết bệnh án ở đây nếu cần
        });
    }

    @Override
    public int getItemCount() {
        return lichKhamList != null ? lichKhamList.size() : 0;
    }

    public void updateBenhNhanInfo(HashMap<String, String> newMap) {
        this.benhNhanMap = newMap != null ? newMap : new HashMap<>();
        notifyDataSetChanged();
    }

    public void updateData(List<LichKham> newList) {
        this.lichKhamList.clear();
        if (newList != null) {
            this.lichKhamList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public static class LichKhamViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaLichKham, tvTenBenhNhan, tvNgayKham, tvTrangThai;

        public LichKhamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaLichKham = itemView.findViewById(R.id.tvMaLichKham);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}