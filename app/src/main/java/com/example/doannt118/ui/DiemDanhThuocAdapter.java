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
    private String caUongCuThe; // Ca uống cụ thể cho adapter này

    public interface OnDiemDanhListener {
        void onDiemDanh(ChiTietDonThuoc thuoc, String caUong);
    }

    public DiemDanhThuocAdapter(Context context, String caUong, OnDiemDanhListener listener) {
        this.context = context;
        this.caUongCuThe = caUong;
        this.thuocList = new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    // Constructor với maBenhNhan
    public DiemDanhThuocAdapter(Context context, String caUong, String maBenhNhan, OnDiemDanhListener listener) {
        this.context = context;
        this.caUongCuThe = caUong;
        this.thuocList = new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        // Lưu maBenhNhan vào SharedPreferences để sử dụng sau
        if (maBenhNhan != null && !maBenhNhan.isEmpty()) {
            android.content.SharedPreferences prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE);
            prefs.edit().putString("maBenhNhan", maBenhNhan).apply();
        }
    }
    
    // Constructor cũ để tương thích ngược
    public DiemDanhThuocAdapter(Context context, OnDiemDanhListener listener) {
        this(context, getCaUongHienTaiStatic(), listener);
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
        
        // Sử dụng ca uống cụ thể của adapter này
        String caUong = caUongCuThe;
        
        // Kiểm tra xem thuốc có được chỉ định uống ở ca này không
        boolean duocUongCaNay = kiemTraDuocUongCaNay(thuoc, caUong);
        
        // Lý thuyết: Nếu thuốc xuất hiện trong adapter này thì đã được filter đúng ca rồi
        // Nhưng vẫn kiểm tra để đảm bảo an toàn
        if (!duocUongCaNay) {
            // Trường hợp này không nên xảy ra nếu logic filter đúng
            android.util.Log.e("DiemDanhAdapter", "LỖI: Thuốc " + thuoc.getTenThuoc() + 
                              " xuất hiện trong adapter ca " + caUong + " nhưng không được chỉ định uống ca này!");
            
            // Ẩn item này vì không nên hiển thị
            holder.itemView.setVisibility(View.GONE);
            return;
        }
        
        // Thuốc được chỉ định uống ở ca này - hiển thị bình thường
        holder.itemView.setVisibility(View.VISIBLE);
        holder.btnDiemDanh.setVisibility(View.VISIBLE);
        
        // Kiểm tra trạng thái đã uống từ database
        checkTrangThaiDaUong(thuoc, caUong, holder, position);
    }

    @Override
    public int getItemCount() {
        return thuocList.size();
    }

    private void showConfirmDialog(ChiTietDonThuoc thuoc, String caUong, Runnable onConfirm) {
        new androidx.appcompat.app.AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog)
            .setTitle("Xác nhận uống thuốc")
            .setMessage("Bạn có chắc chắn đã uống " + thuoc.getTenThuoc() + " đúng liều lượng?")
            .setPositiveButton("✅ Đã uống", (dialog, which) -> {
                onConfirm.run();
            })
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();
    }
    
    private void animateSuccess(View itemView) {
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(itemView, "scaleX", 1.0f, 1.05f, 1.0f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(itemView, "scaleY", 1.0f, 1.05f, 1.0f);
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(600);
        animatorSet.start();
    }
    
    private void animateCompleted(View itemView) {
        android.animation.ObjectAnimator alpha = android.animation.ObjectAnimator.ofFloat(itemView, "alpha", 1.0f, 0.8f, 1.0f);
        alpha.setDuration(800);
        alpha.start();
    }
    
    private void animatePending(View button) {
        android.animation.ObjectAnimator glow = android.animation.ObjectAnimator.ofFloat(button, "alpha", 1.0f, 0.7f, 1.0f);
        glow.setDuration(2000);
        glow.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        glow.start();
    }

    private void checkTrangThaiDaUong(ChiTietDonThuoc thuoc, String caUong, ViewHolder holder, int position) {
        // Kiểm tra trước xem thuốc có được chỉ định uống ở ca này không
        boolean duocUongCaNay = kiemTraDuocUongCaNay(thuoc, caUong);
        
        if (!duocUongCaNay) {
            // Thuốc không được chỉ định uống ở ca này - cập nhật UI ngay
            updateUITrangThai(holder, false, null, thuoc, caUong, position);
            return;
        }
        
        // Tạo ngày hôm nay để so sánh
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String ngayHomNay = dateFormat.format(new java.util.Date());
        
        // Tạo key unique cho thuốc + ca uống + ngày
        String keyXacNhan = thuoc.getMaChiTiet() + "_" + caUong + "_" + ngayHomNay;
        
        android.util.Log.d("DiemDanhThuocAdapter", "Checking key: " + keyXacNhan + " for medication: " + thuoc.getTenThuoc());
        
        // Kiểm tra trong database XacNhanUongThuoc
        com.example.doannt118.repository.FirestoreRepository repo = new com.example.doannt118.repository.FirestoreRepository();
        
        // Kiểm tra bằng document ID trực tiếp thay vì query
        repo.getCollection("XacNhanUongThuoc").document(keyXacNhan).get()
            .addOnSuccessListener(documentSnapshot -> {
                boolean daUong = false;
                java.util.Date thoiGianUong = null;
                
                if (documentSnapshot.exists()) {
                    com.example.doannt118.model.XacNhanUongThuoc xacNhan = documentSnapshot.toObject(com.example.doannt118.model.XacNhanUongThuoc.class);
                    if (xacNhan != null && xacNhan.isDaUong()) {
                        daUong = true;
                        if (xacNhan.getThoiGianXacNhan() != null) {
                            thoiGianUong = xacNhan.getThoiGianXacNhan().toDate();
                        }
                        android.util.Log.d("DiemDanhThuocAdapter", "Found confirmation record - marking as taken");
                    }
                } else {
                    android.util.Log.d("DiemDanhThuocAdapter", "No confirmation record found - marking as not taken");
                }
                
                // Cập nhật UI dựa trên trạng thái
                updateUITrangThai(holder, daUong, thoiGianUong, thuoc, caUong, position);
            })
            .addOnFailureListener(e -> {
                // Nếu lỗi thì mặc định chưa uống
                android.util.Log.e("DiemDanhThuocAdapter", "Error checking medication status", e);
                updateUITrangThai(holder, false, null, thuoc, caUong, position);
            });
    }
    
    private void updateUITrangThai(ViewHolder holder, boolean daUong, java.util.Date thoiGianUong, 
                                  ChiTietDonThuoc thuoc, String caUong, int position) {
        // Kiểm tra lại xem thuốc có được chỉ định uống ở ca này không
        boolean duocUongCaNay = kiemTraDuocUongCaNay(thuoc, caUong);
        
        if (!duocUongCaNay) {
            // Thuốc không được chỉ định uống ở ca này
            holder.btnDiemDanh.setVisibility(View.GONE);
            holder.layoutDaUong.setVisibility(View.VISIBLE);
            holder.tvThoiGianUong.setText("Không uống ca " + caUong.toLowerCase());
            holder.cardThuoc.setCardBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            holder.itemView.setAlpha(0.6f);
            return;
        }
        
        // Clear any existing click listener first
        holder.btnDiemDanh.setOnClickListener(null);
        holder.btnDiemDanh.setVisibility(View.VISIBLE);
        
        if (daUong) {
            // Đã uống - không cho phép thay đổi
            holder.layoutDaUong.setVisibility(View.VISIBLE);
            holder.btnDiemDanh.setEnabled(false);
            holder.btnDiemDanh.setText("✅ Đã hoàn thành");
            holder.btnDiemDanh.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
            holder.cardThuoc.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FFF4"));
            holder.itemView.setAlpha(0.9f);
            
            if (thoiGianUong != null) {
                holder.tvThoiGianUong.setText("Đã uống lúc " + timeFormat.format(thoiGianUong));
            } else {
                holder.tvThoiGianUong.setText("Đã xác nhận uống");
            }
            
            // Hiệu ứng hoàn thành
            animateCompleted(holder.itemView);
        } else {
            // Chưa uống - cho phép xác nhận
            holder.layoutDaUong.setVisibility(View.GONE);
            holder.btnDiemDanh.setEnabled(true);
            holder.btnDiemDanh.setText("✓ Đã uống");
            holder.btnDiemDanh.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
            holder.cardThuoc.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
            holder.itemView.setAlpha(1.0f);
            
            // Hiệu ứng chờ xác nhận
            animatePending(holder.btnDiemDanh);
            
            // Set click listener for confirmation
            holder.btnDiemDanh.setOnClickListener(v -> {
                // Hiệu ứng nhấn nút
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        
                        // Hiển thị dialog xác nhận
                        showConfirmDialog(thuoc, caUong, () -> {
                            // Vô hiệu hóa nút ngay lập tức để tránh double click
                            holder.btnDiemDanh.setEnabled(false);
                            holder.btnDiemDanh.setText("Đang lưu...");
                            
                            // Lưu vào database
                            luuXacNhanUongThuoc(thuoc, caUong, holder, position);
                        });
                    });
            });
        }
    }
    
    private void luuXacNhanUongThuoc(ChiTietDonThuoc thuoc, String caUong, ViewHolder holder, int position) {
        // Kiểm tra lại xem thuốc có được chỉ định uống ở ca này không
        boolean duocUongCaNay = kiemTraDuocUongCaNay(thuoc, caUong);
        
        if (!duocUongCaNay) {
            holder.btnDiemDanh.setEnabled(true);
            holder.btnDiemDanh.setText("✓ Đã uống");
            android.widget.Toast.makeText(context, "❌ Thuốc này không được chỉ định uống ở ca " + caUong.toLowerCase(), android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        
        // Lấy mã bệnh nhân từ SharedPreferences hoặc Intent
        android.content.SharedPreferences prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE);
        String maBenhNhan = prefs.getString("maBenhNhan", "");
        
        if (maBenhNhan.isEmpty()) {
            // Fallback: lấy từ activity nếu có
            if (context instanceof QuanLyUongThuocActivity) {
                android.content.Intent intent = ((android.app.Activity) context).getIntent();
                maBenhNhan = intent.getStringExtra("MA_BENH_NHAN");
            } else if (context instanceof android.app.Activity) {
                android.content.Intent intent = ((android.app.Activity) context).getIntent();
                maBenhNhan = intent.getStringExtra("MA_BENH_NHAN");
            }
        }
        
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            holder.btnDiemDanh.setEnabled(true);
            holder.btnDiemDanh.setText("✓ Đã uống");
            android.widget.Toast.makeText(context, "❌ Không tìm thấy thông tin bệnh nhân", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        
        // Tạo key unique cho thuốc + ca uống + ngày (giống như trong checkTrangThaiDaUong)
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String ngayHomNay = dateFormat.format(new java.util.Date());
        String keyXacNhan = thuoc.getMaChiTiet() + "_" + caUong + "_" + ngayHomNay;
        
        // Tạo bản ghi xác nhận uống thuốc
        com.example.doannt118.model.XacNhanUongThuoc xacNhan = new com.example.doannt118.model.XacNhanUongThuoc();
        xacNhan.setMaXacNhan(keyXacNhan); // Sử dụng key unique làm ID
        xacNhan.setMaChiTietDonThuoc(thuoc.getMaChiTiet());
        xacNhan.setMaBenhNhan(maBenhNhan);
        xacNhan.setDaUong(true);
        xacNhan.setThoiGianXacNhan(com.google.firebase.Timestamp.now());
        xacNhan.setGhiChu("Điểm danh ca " + caUong.toLowerCase() + " ngày " + 
                         new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date()));
        
        android.util.Log.d("DiemDanhThuocAdapter", "Saving confirmation with key: " + keyXacNhan);
        
        com.example.doannt118.repository.FirestoreRepository repo = new com.example.doannt118.repository.FirestoreRepository();
        
        // Sử dụng key unique làm document ID
        repo.addDocument("XacNhanUongThuoc", keyXacNhan, xacNhan,
            aVoid -> {
                // Thành công - cập nhật UI
                java.util.Date now = new java.util.Date();
                holder.layoutDaUong.setVisibility(View.VISIBLE);
                holder.tvThoiGianUong.setText("Đã uống lúc " + timeFormat.format(now));
                holder.btnDiemDanh.setEnabled(false);
                holder.btnDiemDanh.setText("✅ Đã hoàn thành");
                holder.btnDiemDanh.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                holder.cardThuoc.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FFF4"));
                holder.itemView.setAlpha(0.9f);
                
                // Hiệu ứng thành công
                animateSuccess(holder.itemView);
                
                // Toast thông báo
                android.widget.Toast.makeText(context, "✅ Đã xác nhận uống " + thuoc.getTenThuoc(), android.widget.Toast.LENGTH_SHORT).show();
                
                android.util.Log.d("DiemDanhThuocAdapter", "Successfully saved confirmation: " + keyXacNhan);
                
                // Gọi callback nếu có
                if (listener != null) {
                    listener.onDiemDanh(thuoc, caUong);
                }
            },
            e -> {
                // Lỗi - khôi phục trạng thái nút
                holder.btnDiemDanh.setEnabled(true);
                holder.btnDiemDanh.setText("✓ Đã uống");
                holder.btnDiemDanh.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                android.util.Log.e("DiemDanhThuocAdapter", "Error saving medication confirmation: " + keyXacNhan, e);
                android.widget.Toast.makeText(context, "❌ Lỗi lưu dữ liệu: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            }
        );
    }

    /**
     * Kiểm tra xem thuốc có được chỉ định uống ở ca hiện tại không
     * CHỈ cho phép uống ở ca mà bác sĩ đã chỉ định - KHÔNG có fallback
     */
    private boolean kiemTraDuocUongCaNay(ChiTietDonThuoc thuoc, String caUong) {
        // Kiểm tra xem có thông tin ca uống không
        boolean coThongTinCaUong = thuoc.isUongSang() || thuoc.isUongTrua() || 
                                   thuoc.isUongChieu() || thuoc.isUongToi();
        
        if (!coThongTinCaUong) {
            // LOGIC TẠM THỜI: Cho phép dữ liệu cũ uống ở ca sáng và chiều để test
            // TODO: Xóa logic này khi đã có dữ liệu mới với thông tin ca uống đầy đủ
            android.util.Log.w("DiemDanhAdapter", "🔧 LOGIC TẠM THỜI: Thuốc " + thuoc.getTenThuoc() + 
                              " không có thông tin ca uống - cho phép uống ở ca " + caUong + 
                              " (chỉ sáng và chiều)");
            return "SANG".equals(caUong.toUpperCase()) || "CHIEU".equals(caUong.toUpperCase());
        }
        
        // Kiểm tra chính xác theo ca được chỉ định bởi bác sĩ
        boolean duocUong = false;
        
        switch (caUong.toUpperCase()) {
            case "SANG":
                duocUong = thuoc.isUongSang();
                break;
            case "TRUA":
                duocUong = thuoc.isUongTrua();
                break;
            case "CHIEU":
                duocUong = thuoc.isUongChieu();
                break;
            case "TOI":
                duocUong = thuoc.isUongToi();
                break;
            default:
                duocUong = false;
        }
        
        android.util.Log.d("DiemDanhAdapter", "Thuốc " + thuoc.getTenThuoc() + 
                          " - Ca " + caUong + ": " + (duocUong ? "ĐƯỢC PHÉP" : "KHÔNG ĐƯỢC PHÉP") + 
                          " (Sáng:" + thuoc.isUongSang() + ", Trưa:" + thuoc.isUongTrua() + 
                          ", Chiều:" + thuoc.isUongChieu() + ", Tối:" + thuoc.isUongToi() + ")");
        
        return duocUong;
    }

    private String getCaUongHienTai() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) return "SANG";
        if (hour >= 11 && hour < 14) return "TRUA";
        // Từ 14h trở đi là chiều (không có ca tối)
        return "CHIEU";
    }
    
    // Static method để tương thích ngược
    private static String getCaUongHienTaiStatic() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) return "SANG";
        if (hour >= 11 && hour < 14) return "TRUA";
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
