package com.example.doannt118.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import com.example.doannt118.repository.FirestoreRepository;

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
        
        if (lich.getNgayLamViec() != null) {
            holder.tvNgayLamViec.setText(DATE_FORMAT.format(lich.getNgayLamViec()));
        } else {
            holder.tvNgayLamViec.setText("N/A");
        }

        // Tính số lượng slot và số lượng đã đặt
        int soLuongToiDa = tinhSoSlotTuCaLamViec(lich.getCaLamViec());
        
        // Đếm số lượng bệnh nhân đã đặt lịch cho ca này
        demSoLuongBenhNhanDaDat(lich, holder, soLuongToiDa);

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

    // Tính số slot từ ca làm việc (ví dụ: "14:00-18:00" = 8 slots)
    private int tinhSoSlotTuCaLamViec(String caLamViec) {
        if (caLamViec == null || !caLamViec.contains("-")) {
            return 8; // Mặc định
        }
        
        try {
            String[] parts = caLamViec.split("-");
            String[] startParts = parts[0].trim().split(":");
            String[] endParts = parts[1].trim().split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            int totalMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
            return totalMinutes / 30; // Mỗi slot 30 phút
        } catch (Exception e) {
            return 8; // Mặc định nếu có lỗi
        }
    }
    
    // Đếm số lượng bệnh nhân đã đặt lịch cho ca làm việc này
    private void demSoLuongBenhNhanDaDat(LichLamViec lich, LichLamViecViewHolder holder, int tongSoSlot) {
        // Hiển thị loading trước
        holder.tvTrangThai.setText("Đang tải...");
        holder.tvTrangThai.setBackgroundResource(R.drawable.badge_warning);
        
        // Tạo FirestoreRepository để query
        com.example.doannt118.repository.FirestoreRepository repo = 
            new com.example.doannt118.repository.FirestoreRepository();
        
        // Tạo date range cho ngày làm việc sử dụng Timestamp
        java.util.Calendar startCal = java.util.Calendar.getInstance();
        startCal.setTime(lich.getNgayLamViec());
        startCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        startCal.set(java.util.Calendar.MINUTE, 0);
        startCal.set(java.util.Calendar.SECOND, 0);
        startCal.set(java.util.Calendar.MILLISECOND, 0);
        final long startMillis = startCal.getTimeInMillis();

        java.util.Calendar endCal = java.util.Calendar.getInstance();
        endCal.setTime(lich.getNgayLamViec());
        endCal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        endCal.set(java.util.Calendar.MINUTE, 59);
        endCal.set(java.util.Calendar.SECOND, 59);
        endCal.set(java.util.Calendar.MILLISECOND, 999);
        final long endMillis = endCal.getTimeInMillis();
        
        // Query đơn giản: chỉ lấy theo maBacSi, sau đó filter theo ngày trong code
        repo.getCollection("LichKham")
            .whereEqualTo("maBacSi", lich.getMaBacSi())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int soDaDat = 0;
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.LichKham lichKham = doc.toObject(com.example.doannt118.model.LichKham.class);
                    if (lichKham == null) continue;
                    
                    // Filter theo ngày trong code
                    com.google.firebase.Timestamp ngayKham = lichKham.getNgayKham();
                    if (ngayKham == null) continue;
                    
                    long ngayKhamMillis = ngayKham.toDate().getTime();
                    if (ngayKhamMillis < startMillis || ngayKhamMillis > endMillis) continue;
                    
                    if (!"HUY".equals(lichKham.getTrangThai()) && // Không tính lịch đã hủy
                        lichKham.getGioKham() != null && 
                        !lichKham.getGioKham().isEmpty()) {
                        
                        // Kiểm tra xem giờ khám có nằm trong ca làm việc này không
                        if (gioKhamTrongCaLamViec(lichKham.getGioKham(), lich.getCaLamViec())) {
                            soDaDat++;
                            Log.d("LichLamViecAdapter", "Found booked slot: " + lichKham.getGioKham() + " in ca: " + lich.getCaLamViec() + " - Status: " + lichKham.getTrangThai());
                        }
                    }
                }
                
                Log.d("LichLamViecAdapter", "Total booked for ca " + lich.getCaLamViec() + ": " + soDaDat + "/" + tongSoSlot);
                
                // Cập nhật UI
                String soLuongText = soDaDat + "/" + tongSoSlot + " BN";
                holder.tvTrangThai.setText(soLuongText);
                
                // Đổi màu theo tỷ lệ đặt
                if (soDaDat == 0) {
                    holder.tvTrangThai.setBackgroundResource(R.drawable.badge_success); // Xanh - trống
                } else if (soDaDat >= tongSoSlot) {
                    holder.tvTrangThai.setBackgroundResource(R.drawable.badge_danger); // Đỏ - đầy
                } else {
                    holder.tvTrangThai.setBackgroundResource(R.drawable.badge_warning); // Vàng - một phần
                }
            })
            .addOnFailureListener(e -> {
                // Lỗi - hiển thị mặc định
                Log.e("LichLamViecAdapter", "Error counting booked slots: " + e.getMessage());
                holder.tvTrangThai.setText("0/" + tongSoSlot + " BN");
                holder.tvTrangThai.setBackgroundResource(R.drawable.badge_success);
            });
    }
    
    // Kiểm tra xem giờ khám có nằm trong ca làm việc không
    private boolean gioKhamTrongCaLamViec(String gioKham, String caLamViec) {
        if (gioKham == null || caLamViec == null) return false;
        
        try {
            // Chuẩn hóa strings (loại bỏ khoảng trắng)
            gioKham = gioKham.trim().replaceAll("\\s+", "");
            caLamViec = caLamViec.trim().replaceAll("\\s+", "");
            
            // Lấy giờ bắt đầu từ gioKham (ví dụ: "14:00-14:30" -> "14:00")
            String gioStart = gioKham.split("-")[0].trim();
            
            // Lấy khoảng thời gian ca làm việc (ví dụ: "14:00-18:00")
            String[] caParts = caLamViec.split("-");
            String caStart = caParts[0].trim();
            String caEnd = caParts[1].trim();
            
            // Parse thời gian để so sánh chính xác
            int gioStartMinutes = parseTimeToMinutes(gioStart);
            int caStartMinutes = parseTimeToMinutes(caStart);
            int caEndMinutes = parseTimeToMinutes(caEnd);
            
            // So sánh: giờ bắt đầu của slot phải >= giờ bắt đầu ca và < giờ kết thúc ca
            boolean result = gioStartMinutes >= caStartMinutes && gioStartMinutes < caEndMinutes;
            Log.d("LichLamViecAdapter", "gioKhamTrongCaLamViec: " + gioKham + " in " + caLamViec + " = " + result + 
                " (gioStart=" + gioStartMinutes + ", caStart=" + caStartMinutes + ", caEnd=" + caEndMinutes + ")");
            return result;
        } catch (Exception e) {
            Log.e("LichLamViecAdapter", "Error in gioKhamTrongCaLamViec: " + e.getMessage());
            return false;
        }
    }
    
    // Parse thời gian HH:mm thành số phút từ 00:00
    private int parseTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
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
