package com.example.doannt118.ui;

import android.content.Context;
import android.graphics.Color; // Import thêm
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import thêm
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// Kế thừa từ RecyclerView.Adapter
public class LichLamViecAdapter extends RecyclerView.Adapter<LichLamViecAdapter.LichLamViecViewHolder> {

    private List<LichLamViec> lichLamViecList; // Dùng List
    private Context context;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Dùng NO_POSITION (-1) làm giá trị mặc định
    private HashMap<String, String> nhanVienMap;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(LichLamViec lichLamViec);
    }

    // Constructor dùng List
    public LichLamViecAdapter(Context context, List<LichLamViec> lichLamViecList, OnItemClickListener listener, HashMap<String, String> nhanVienMap) {
        this.context = context;
        this.lichLamViecList = lichLamViecList;
        this.listener = listener;
        this.nhanVienMap = nhanVienMap != null ? nhanVienMap : new HashMap<>();
    }

    @NonNull
    @Override
    public LichLamViecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_lam_viec, parent, false);
        return new LichLamViecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichLamViecViewHolder holder, int position) {
        LichLamViec lich = lichLamViecList.get(position); // Lấy từ List

        // Hiển thị dữ liệu (thêm kiểm tra null)
        holder.tvMaLichLamViec.setText(lich.getMaLichLamViec() != null ? lich.getMaLichLamViec() : "N/A");
        holder.tvTenNhanVien.setText(nhanVienMap.getOrDefault(lich.getMaBacSi(), "Không rõ BS"));
        holder.tvCaLamViec.setText(lich.getCaLamViec() != null ? lich.getCaLamViec() : "N/A");
        holder.tvTrangThai.setText(lich.getTrangThai() != null ? lich.getTrangThai() : "N/A");

        if (lich.getNgayLamViec() != null) {
            holder.tvNgayLamViec.setText(DATE_FORMAT.format(lich.getNgayLamViec()));
        } else {
            holder.tvNgayLamViec.setText("N/A");
        }

        // Xử lý background khi item được chọn
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_background)); // Dùng màu từ colors.xml
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Màu mặc định
        }

        // Xử lý click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    // Bỏ chọn item cũ (nếu có)
                    if (selectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selectedPosition);
                    }
                    // Chọn item mới
                    selectedPosition = clickedPosition;
                    notifyItemChanged(selectedPosition); // Highlight item mới

                    // Gọi callback cho Activity
                    listener.onItemClick(lichLamViecList.get(selectedPosition));
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

    // === HÀM CẦN THÊM VÀO ===
    // Reset lựa chọn (ví dụ: sau khi load lại data hoặc hủy form)
    public void resetSelection() {
        int previousSelected = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION; // Đặt về trạng thái không chọn
        if (previousSelected != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelected); // Bỏ highlight item cũ
        }
    }
    // === KẾT THÚC HÀM CẦN THÊM ===

    // Cập nhật map tên nhân viên
    public void updateNhanVienInfo(HashMap<String, String> newMap) {
        this.nhanVienMap = newMap != null ? newMap : new HashMap<>();
        // Chỉ cần notifyDataSetChanged nếu tên BS có thể thay đổi sau khi list đã load
        // notifyDataSetChanged();
    }

    // Cập nhật toàn bộ danh sách dữ liệu
    public void updateData(List<LichLamViec> newList) {
        this.lichLamViecList.clear();
        if (newList != null) {
            this.lichLamViecList.addAll(newList);
        }
        resetSelection(); // Bỏ chọn khi load data mới
        notifyDataSetChanged(); // Vẽ lại toàn bộ RecyclerView
    }

    // ViewHolder giữ nguyên
    public static class LichLamViecViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaLichLamViec, tvTenNhanVien, tvNgayLamViec, tvCaLamViec, tvTrangThai; // Đổi tên tvKhungGio -> tvCaLamViec

        public LichLamViecViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaLichLamViec = itemView.findViewById(R.id.tvMaLichLamViec);
            tvTenNhanVien = itemView.findViewById(R.id.tvTenNhanVien);
            tvNgayLamViec = itemView.findViewById(R.id.tvNgayLamViec);
            tvCaLamViec = itemView.findViewById(R.id.tvKhungGio); // ID trong XML có thể vẫn là tvKhungGio
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}