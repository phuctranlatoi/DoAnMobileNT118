package com.example.doannt118.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LichLamViecAdapter extends RecyclerView.Adapter<LichLamViecAdapter.LichLamViecViewHolder> {

    private List<LichLamViec> lichLamViecList;
    private Context context;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private HashMap<String, String> nhanVienMap;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Interface click item
    public interface OnItemClickListener {
        void onItemClick(LichLamViec lichLamViec);
    }

    // Constructor đầy đủ 4 tham số
    public LichLamViecAdapter(Context context, List<LichLamViec> lichLamViecList, OnItemClickListener listener, HashMap<String, String> nhanVienMap) {
        this.context = context;
        this.lichLamViecList = lichLamViecList;
        this.listener = listener;
        this.nhanVienMap = nhanVienMap != null ? nhanVienMap : new HashMap<>();
    }

    // ✅ Constructor phụ 2 tham số (để không lỗi khi bạn khởi tạo đơn giản)
    public LichLamViecAdapter(Context context, List<LichLamViec> lichLamViecList) {
        this(context, lichLamViecList, null, new HashMap<>());
    }

    @NonNull
    @Override
    public LichLamViecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_lam_viec, parent, false);
        return new LichLamViecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichLamViecViewHolder holder, int position) {
        LichLamViec lich = lichLamViecList.get(position);

        holder.tvMaLichLamViec.setText(lich.getMaLichLamViec() != null ? lich.getMaLichLamViec() : "N/A");
        holder.tvTenNhanVien.setText(nhanVienMap.getOrDefault(lich.getMaBacSi(), "Không rõ BS"));
        holder.tvCaLamViec.setText(lich.getCaLamViec() != null ? lich.getCaLamViec() : "N/A");
        holder.tvTrangThai.setText(lich.getTrangThai() != null ? lich.getTrangThai() : "N/A");

        if (lich.getNgayLamViec() != null) {
            holder.tvNgayLamViec.setText(DATE_FORMAT.format(lich.getNgayLamViec()));
        } else {
            holder.tvNgayLamViec.setText("N/A");
        }

        // Highlight item được chọn
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_background));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                // Bỏ chọn cũ
                if (selectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(selectedPosition);
                }
                // Cập nhật item được chọn
                selectedPosition = clickedPosition;
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onItemClick(lichLamViecList.get(selectedPosition));
                } else {
                    // Nếu chưa set listener → chỉ hiển thị thông báo tạm
                    Toast.makeText(context, "Đã chọn: " + lich.getMaLichLamViec(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return lichLamViecList != null ? lichLamViecList.size() : 0;
    }

    public LichLamViec getSelectedItem() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < lichLamViecList.size()) {
            return lichLamViecList.get(selectedPosition);
        }
        return null;
    }

    // Reset lựa chọn (dùng khi load lại data)
    public void resetSelection() {
        int previousSelected = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (previousSelected != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelected);
        }
    }

    // Cập nhật map tên nhân viên
    public void updateNhanVienInfo(HashMap<String, String> newMap) {
        this.nhanVienMap = newMap != null ? newMap : new HashMap<>();
    }

    // Cập nhật danh sách dữ liệu
    public void updateData(List<LichLamViec> newList) {
        this.lichLamViecList.clear();
        if (newList != null) {
            this.lichLamViecList.addAll(newList);
        }
        resetSelection();
        notifyDataSetChanged();
    }

    // ViewHolder
    public static class LichLamViecViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaLichLamViec, tvTenNhanVien, tvNgayLamViec, tvCaLamViec, tvTrangThai;

        public LichLamViecViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaLichLamViec = itemView.findViewById(R.id.tvMaLichLamViec);
            tvTenNhanVien = itemView.findViewById(R.id.tvTenNhanVien);
            tvNgayLamViec = itemView.findViewById(R.id.tvNgayLamViec);
            tvCaLamViec = itemView.findViewById(R.id.tvKhungGio); // Nếu trong layout tên ID là tvKhungGio
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}
