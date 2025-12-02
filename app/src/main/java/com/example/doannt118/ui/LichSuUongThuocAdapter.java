package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.LichUongThuoc;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LichSuUongThuocAdapter extends RecyclerView.Adapter<LichSuUongThuocAdapter.ViewHolder> {
    private Context context;
    private List<LichUongThuoc> lichSuList;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public LichSuUongThuocAdapter(Context context) {
        this.context = context;
        this.lichSuList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void setData(List<LichUongThuoc> list) {
        this.lichSuList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_su_uong_thuoc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichUongThuoc lich = lichSuList.get(position);
        
        String ngayGio = "";
        if (lich.getNgayUong() != null) {
            ngayGio = dateFormat.format(lich.getNgayUong());
        }
        ngayGio += " - Ca " + (lich.getCaUong() != null ? lich.getCaUong().toLowerCase() : "");
        holder.tvNgayGio.setText(ngayGio);
        
        String trangThai = lich.getTrangThai();
        if ("DA_UONG".equals(trangThai)) {
            holder.tvTrangThai.setText("Đã uống");
            holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
        } else if ("BO_QUA".equals(trangThai)) {
            holder.tvTrangThai.setText("Bỏ qua");
            holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.textSecondary));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.textSecondary));
        } else {
            holder.tvTrangThai.setText("Chờ xác nhận");
            holder.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.textSecondary));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.textSecondary));
        }
        
        if (lich.getThoiGianXacNhan() != null) {
            holder.tvThoiGian.setText(timeFormat.format(lich.getThoiGianXacNhan().toDate()));
        } else {
            holder.tvThoiGian.setText("");
        }
        
        holder.tvDanhSachThuoc.setText("Xem chi tiết đơn thuốc");
    }

    @Override
    public int getItemCount() {
        return lichSuList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvNgayGio, tvTrangThai, tvThoiGian, tvDanhSachThuoc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvNgayGio = itemView.findViewById(R.id.tvNgayGio);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            tvDanhSachThuoc = itemView.findViewById(R.id.tvDanhSachThuoc);
        }
    }
}
