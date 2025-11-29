package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChiTietLichKhamAdapter extends RecyclerView.Adapter<ChiTietLichKhamAdapter.ViewHolder> {

    private Context context;
    private List<LichKham> lichKhamList;
    private HashMap<String, String> benhNhanMap;
    private com.example.doannt118.repository.FirestoreRepository repo;

    public ChiTietLichKhamAdapter(Context context, List<LichKham> lichKhamList, HashMap<String, String> benhNhanMap) {
        this.context = context;
        this.lichKhamList = lichKhamList;
        this.benhNhanMap = benhNhanMap;
        this.repo = new com.example.doannt118.repository.FirestoreRepository();
    }

    public ChiTietLichKhamAdapter(Context context, List<LichKham> lichKhamList) {
        this.context = context;
        this.lichKhamList = lichKhamList;
        this.benhNhanMap = new HashMap<>();
        this.repo = new com.example.doannt118.repository.FirestoreRepository();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chi_tiet_lich_kham, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichKham lichKham = lichKhamList.get(position);
        
        // Hiển thị số thứ tự
        holder.tvSoThuTu.setText("STT: " + lichKham.getSoThuTu());
        
        // Hiển thị tên bệnh nhân
        String tenBenhNhan = benhNhanMap.get(lichKham.getMaBenhNhan());
        if (tenBenhNhan != null) {
            holder.tvTenBenhNhan.setText(tenBenhNhan);
        } else {
            // Load từ Firestore nếu chưa có trong cache
            loadTenBenhNhan(lichKham.getMaBenhNhan(), holder.tvTenBenhNhan);
        }
        
        // Hiển thị ngày khám
        if (lichKham.getNgayKham() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvNgayKham.setText(sdf.format(lichKham.getNgayKham().toDate()));
        } else {
            holder.tvNgayKham.setText("N/A");
        }
        
        // Hiển thị trạng thái
        String trangThai = lichKham.getTrangThai();
        if ("CHO".equals(trangThai)) {
            holder.tvTrangThai.setText("Chờ xác nhận");
            holder.tvTrangThai.setTextColor(0xFFFFA726);
        } else if ("XAC_NHAN".equals(trangThai)) {
            holder.tvTrangThai.setText("Đã xác nhận");
            holder.tvTrangThai.setTextColor(0xFF4CAF50);
        } else if ("HOAN_THANH".equals(trangThai)) {
            holder.tvTrangThai.setText("Hoàn thành");
            holder.tvTrangThai.setTextColor(0xFF9E9E9E);
        } else {
            holder.tvTrangThai.setText("Đã hủy");
            holder.tvTrangThai.setTextColor(0xFFE74C3C);
        }
    }

    private void loadTenBenhNhan(String maBenhNhan, TextView textView) {
        repo.getByField("BenhNhan", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String hoTen = querySnapshot.getDocuments().get(0).getString("hoTen");
                    if (hoTen != null) {
                        benhNhanMap.put(maBenhNhan, hoTen);
                        textView.setText(hoTen);
                    } else {
                        textView.setText("N/A");
                    }
                } else {
                    textView.setText("N/A");
                }
            },
            e -> textView.setText("Lỗi"));
    }

    @Override
    public int getItemCount() {
        return lichKhamList.size();
    }

    public void updateBenhNhanInfo(HashMap<String, String> newMap) {
        this.benhNhanMap = newMap;
        notifyDataSetChanged();
    }

    public void updateData(List<LichKham> newList) {
        this.lichKhamList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSoThuTu, tvTenBenhNhan, tvNgayKham, tvTrangThai;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSoThuTu = itemView.findViewById(R.id.tvSoThuTu);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvNgayKham = itemView.findViewById(R.id.tvNgayKham);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}
