package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiemDanhThuocAdapter extends RecyclerView.Adapter<DiemDanhThuocAdapter.ViewHolder> {
    
    private Context context;
    private List<ChiTietDonThuoc> thuocList;
    private OnDiemDanhListener listener;
    private SimpleDateFormat timeFormat;

    public interface OnDiemDanhListener {
        void onDiemDanh(ChiTietDonThuoc thuoc, String caUong);
    }

    public DiemDanhThuocAdapter(Context context, OnDiemDanhListener listener) {
        this.context = context;
        this.thuocList = new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void setData(List<ChiTietDonThuoc> list) {
        this.thuocList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diem_danh_thuoc, parent, false);
        return new ViewHolder(view);
    }

    public void setTrangThaiDaUong(String maChiTiet, boolean daUong, Date thoiGian) {
        for (int i = 0; i < thuocList.size(); i++) {
            if (thuocList.get(i).getMaChiTiet().equals(maChiTiet)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietDonThuoc thuoc = thuocList.get(position);
        
        holder.tvTenThuoc.setText(thuoc.getTenThuoc());
        
        // Hiển thị liều dùng đầy đủ
        String lieuDung = thuoc.getLieuDungDayDu();
        if (lieuDung != null && !lieuDung.isEmpty()) {
            holder.tvLieuDung.setText(lieuDung);
            holder.tvLieuDung.setVisibility(View.VISIBLE);
        } else {
            holder.tvLieuDung.setVisibility(View.GONE);
        }
        
        // Hiển thị cách dùng
        if (thuoc.getCachDung() != null && !thuoc.getCachDung().isEmpty()) {
            holder.tvCachDung.setText(thuoc.getCachDung());
            holder.tvCachDung.setVisibility(View.VISIBLE);
        } else {
            holder.tvCachDung.setVisibility(View.GONE);
        }
        
        // Xác định ca uống
        String caUong = getCaUongHienTai();
        
        // Lưu trạng thái vào tag để tránh load lại
        Boolean daUong = (Boolean) holder.itemView.getTag();
        if (daUong == null) daUong = false;
        
        if (daUong) {
            holder.layoutDaUong.setVisibility(View.VISIBLE);
            holder.btnDiemDanh.setEnabled(false);
            holder.btnDiemDanh.setText("✓ Đã uống");
            holder.cardThuoc.setStrokeColor(context.getColor(R.color.success));
        } else {
            holder.layoutDaUong.setVisibility(View.GONE);
            holder.btnDiemDanh.setEnabled(true);
            holder.btnDiemDanh.setText("Đã uống");
            holder.cardThuoc.setStrokeColor(context.getColor(R.color.divider));
        }
        
        holder.btnDiemDanh.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDiemDanh(thuoc, caUong);
                // Cập nhật UI
                holder.itemView.setTag(true);
                holder.layoutDaUong.setVisibility(View.VISIBLE);
                holder.tvThoiGianUong.setText("Đã uống lúc " + timeFormat.format(new Date()));
                holder.btnDiemDanh.setEnabled(false);
                holder.btnDiemDanh.setText("✓ Đã uống");
                holder.cardThuoc.setStrokeColor(context.getColor(R.color.success));
            }
        });
    }

    @Override
    public int getItemCount() {
        return thuocList.size();
    }

    private String getCaUongHienTai() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) return "SANG";
        if (hour >= 11 && hour < 14) return "TRUA";
        // Từ 14h trở đi là chiều (không có ca tối)
        return "CHIEU";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardThuoc;
        TextView tvTenThuoc, tvLieuDung, tvCachDung, tvThoiGianUong;
        LinearLayout layoutDaUong;
        MaterialButton btnDiemDanh;

        ViewHolder(View itemView) {
            super(itemView);
            cardThuoc = itemView.findViewById(R.id.cardThuoc);
            tvTenThuoc = itemView.findViewById(R.id.tvTenThuoc);
            tvLieuDung = itemView.findViewById(R.id.tvLieuDung);
            tvCachDung = itemView.findViewById(R.id.tvCachDung);
            tvThoiGianUong = itemView.findViewById(R.id.tvThoiGianUong);
            layoutDaUong = itemView.findViewById(R.id.layoutDaUong);
            btnDiemDanh = itemView.findViewById(R.id.btnDiemDanh);
        }
    }
}
