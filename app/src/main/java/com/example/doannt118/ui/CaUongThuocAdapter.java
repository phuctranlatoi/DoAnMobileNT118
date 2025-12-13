package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.model.XacNhanUongThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import android.widget.Button;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CaUongThuocAdapter extends RecyclerView.Adapter<CaUongThuocAdapter.ViewHolder> {
    
    public static class CaUongThuoc {
        private String tenCa;
        private String iconCa;
        private String maCa;
        private List<ChiTietDonThuoc> danhSachThuoc;
        private boolean daUong;
        private Date thoiGianUong;
        
        public CaUongThuoc(String tenCa, String iconCa, String maCa, List<ChiTietDonThuoc> danhSachThuoc) {
            this.tenCa = tenCa;
            this.iconCa = iconCa;
            this.maCa = maCa;
            this.danhSachThuoc = danhSachThuoc != null ? danhSachThuoc : new ArrayList<>();
            this.daUong = false;
        }
        
        // Getters and setters
        public String getTenCa() { return tenCa; }
        public String getIconCa() { return iconCa; }
        public String getMaCa() { return maCa; }
        public List<ChiTietDonThuoc> getDanhSachThuoc() { return danhSachThuoc; }
        public boolean isDaUong() { return daUong; }
        public void setDaUong(boolean daUong) { this.daUong = daUong; }
        public Date getThoiGianUong() { return thoiGianUong; }
        public void setThoiGianUong(Date thoiGianUong) { this.thoiGianUong = thoiGianUong; }
    }
    
    public interface OnXacNhanCaListener {
        void onXacNhanCa(CaUongThuoc caUong);
    }
    
    private Context context;
    private List<CaUongThuoc> danhSachCa;
    private OnXacNhanCaListener listener;
    private SimpleDateFormat timeFormat;
    private FirestoreRepository repository;
    private String maBenhNhan;

    public CaUongThuocAdapter(Context context, String maBenhNhan, OnXacNhanCaListener listener) {
        this.context = context;
        this.maBenhNhan = maBenhNhan;
        this.danhSachCa = new ArrayList<>();
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.repository = new FirestoreRepository();
    }
    
    public void setData(List<CaUongThuoc> list) {
        this.danhSachCa = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
        
        // Kiểm tra trạng thái đã uống cho từng ca
        for (int i = 0; i < this.danhSachCa.size(); i++) {
            checkTrangThaiCa(this.danhSachCa.get(i), i);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ca_uong_thuoc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CaUongThuoc caUong = danhSachCa.get(position);
        
        // Hiển thị thông tin ca (không có khung giờ)
        String tenCa = com.example.doannt118.utils.CaUongThuocManager.getTenCa(caUong.getMaCa());
        holder.tvTenCa.setText(tenCa);
        holder.tvSoLuongThuoc.setText(caUong.getDanhSachThuoc().size() + " loại thuốc");
        
        // Hiển thị danh sách thuốc trong ca
        ThuocTrongCaAdapter thuocAdapter = new ThuocTrongCaAdapter(context);
        thuocAdapter.setData(caUong.getDanhSachThuoc());
        holder.rvThuocTrongCa.setLayoutManager(new LinearLayoutManager(context));
        holder.rvThuocTrongCa.setAdapter(thuocAdapter);
        
        // Cập nhật UI dựa trên trạng thái
        updateUITrangThai(holder, caUong, position);
    }

    @Override
    public int getItemCount() {
        return danhSachCa.size();
    }
    
    private void checkTrangThaiCa(CaUongThuoc caUong, int position) {
        // Tạo ngày hôm nay để so sánh
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String ngayHomNay = dateFormat.format(new Date());
        
        // Tạo key unique cho ca uống + ngày
        String keyXacNhanCa = "CA_" + caUong.getMaCa() + "_" + maBenhNhan + "_" + ngayHomNay;
        
        // Kiểm tra trong database XacNhanUongThuoc
        repository.getCollection("XacNhanUongThuoc").document(keyXacNhanCa).get()
            .addOnSuccessListener(documentSnapshot -> {
                boolean daUong = false;
                Date thoiGianUong = null;
                
                if (documentSnapshot.exists()) {
                    XacNhanUongThuoc xacNhan = documentSnapshot.toObject(XacNhanUongThuoc.class);
                    if (xacNhan != null && xacNhan.isDaUong()) {
                        daUong = true;
                        if (xacNhan.getThoiGianXacNhan() != null) {
                            thoiGianUong = xacNhan.getThoiGianXacNhan().toDate();
                        }
                    }
                }
                
                // Cập nhật trạng thái
                caUong.setDaUong(daUong);
                caUong.setThoiGianUong(thoiGianUong);
                
                // Cập nhật UI
                notifyItemChanged(position);
            })
            .addOnFailureListener(e -> {
                // Nếu lỗi thì mặc định chưa uống
                caUong.setDaUong(false);
                caUong.setThoiGianUong(null);
                notifyItemChanged(position);
            });
    }
    
    private void updateUITrangThai(ViewHolder holder, CaUongThuoc caUong, int position) {
        if (caUong.isDaUong()) {
            // Đã uống - hiển thị trạng thái hoàn thành
            holder.tvTrangThai.setBackgroundResource(R.drawable.status_completed_background);
            holder.tvTrangThai.setText("Đã hoàn thành");
            holder.tvTrangThai.setTextColor(context.getColor(android.R.color.white));
            
            holder.layoutDaUong.setVisibility(View.VISIBLE);
            if (caUong.getThoiGianUong() != null) {
                // Hiển thị thời gian và ngày rõ ràng
                SimpleDateFormat fullDateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                holder.tvThoiGianUong.setText("Thời gian uống: " + fullDateFormat.format(caUong.getThoiGianUong()));
            } else {
                holder.tvThoiGianUong.setText("Thời gian uống: Đã xác nhận");
            }
            
            holder.btnXacNhanCa.setEnabled(false);
            holder.btnXacNhanCa.setText("Đã hoàn thành");
            holder.btnXacNhanCa.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                context.getColor(android.R.color.darker_gray)));
            
            holder.cardCaUong.setBackgroundColor(context.getColor(R.color.completed_background_light));
            holder.itemView.setAlpha(0.9f);
            
        } else {
            // Chưa uống - hiển thị trạng thái chờ
            holder.tvTrangThai.setBackgroundResource(R.drawable.status_pending_background);
            holder.tvTrangThai.setText("Chưa uống");
            holder.tvTrangThai.setTextColor(context.getColor(android.R.color.white));
            
            holder.layoutDaUong.setVisibility(View.GONE);
            
            holder.btnXacNhanCa.setEnabled(true);
            holder.btnXacNhanCa.setText("Xác nhận đã uống hết ca này");
            holder.btnXacNhanCa.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                context.getColor(R.color.primary_green)));
            
            holder.cardCaUong.setBackgroundColor(context.getColor(android.R.color.white));
            holder.itemView.setAlpha(1.0f);
            
            // Set click listener
            holder.btnXacNhanCa.setOnClickListener(v -> {
                showConfirmDialog(caUong, () -> {
                    // Vô hiệu hóa nút ngay lập tức
                    holder.btnXacNhanCa.setEnabled(false);
                    holder.btnXacNhanCa.setText("Đang lưu...");
                    
                    // Lưu xác nhận ca
                    luuXacNhanCa(caUong, holder, position);
                });
            });
        }
    }
    
    private void showConfirmDialog(CaUongThuoc caUong, Runnable onConfirm) {
        String message = "Bạn có chắc chắn đã uống hết tất cả " + 
                        caUong.getDanhSachThuoc().size() + " loại thuốc trong " + 
                        caUong.getTenCa().toLowerCase() + " không?";
        
        new androidx.appcompat.app.AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog)
            .setTitle("Xác nhận uống thuốc")
            .setMessage(message)
            .setPositiveButton("✅ Đã uống hết", (dialog, which) -> {
                onConfirm.run();
            })
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();
    }
    
    private void luuXacNhanCa(CaUongThuoc caUong, ViewHolder holder, int position) {
        // Tạo ngày hôm nay
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String ngayHomNay = dateFormat.format(new Date());
        
        // Tạo key unique cho ca uống + ngày
        String keyXacNhanCa = "CA_" + caUong.getMaCa() + "_" + maBenhNhan + "_" + ngayHomNay;
        
        // Tạo bản ghi xác nhận uống thuốc cho cả ca
        XacNhanUongThuoc xacNhan = new XacNhanUongThuoc();
        xacNhan.setMaXacNhan(keyXacNhanCa);
        xacNhan.setMaChiTietDonThuoc("CA_" + caUong.getMaCa()); // Đánh dấu đây là xác nhận ca
        xacNhan.setMaBenhNhan(maBenhNhan);
        xacNhan.setDaUong(true);
        xacNhan.setThoiGianXacNhan(Timestamp.now());
        xacNhan.setGhiChu("Xác nhận uống hết " + caUong.getTenCa().toLowerCase() + 
                         " (" + caUong.getDanhSachThuoc().size() + " loại thuốc) ngày " + 
                         new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        
        // Lưu vào database
        repository.addDocument("XacNhanUongThuoc", keyXacNhanCa, xacNhan,
            aVoid -> {
                // Thành công - cập nhật trạng thái
                Date now = new Date();
                caUong.setDaUong(true);
                caUong.setThoiGianUong(now);
                
                // Cập nhật UI
                updateUITrangThai(holder, caUong, position);
                
                // Hiệu ứng thành công
                animateSuccess(holder.itemView);
                
                // Toast thông báo
                android.widget.Toast.makeText(context, 
                    "✅ Đã xác nhận uống hết " + caUong.getTenCa().toLowerCase(), 
                    android.widget.Toast.LENGTH_SHORT).show();
                
                // Gọi callback
                if (listener != null) {
                    listener.onXacNhanCa(caUong);
                }
            },
            e -> {
                // Lỗi - khôi phục trạng thái nút
                holder.btnXacNhanCa.setEnabled(true);
                holder.btnXacNhanCa.setText("Xác nhận đã uống hết ca này");
                android.widget.Toast.makeText(context, 
                    "❌ Lỗi lưu dữ liệu: " + e.getMessage(), 
                    android.widget.Toast.LENGTH_LONG).show();
            }
        );
    }
    
    private void animateSuccess(View itemView) {
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(itemView, "scaleX", 1.0f, 1.05f, 1.0f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(itemView, "scaleY", 1.0f, 1.05f, 1.0f);
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(600);
        animatorSet.start();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardCaUong;
        TextView tvTenCa, tvSoLuongThuoc, tvTrangThai, tvThoiGianUong;
        LinearLayout layoutDaUong;
        RecyclerView rvThuocTrongCa;
        Button btnXacNhanCa;

        ViewHolder(View itemView) {
            super(itemView);
            cardCaUong = itemView.findViewById(R.id.cardCaUong);
            tvTenCa = itemView.findViewById(R.id.tvTenCa);
            tvSoLuongThuoc = itemView.findViewById(R.id.tvSoLuongThuoc);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvThoiGianUong = itemView.findViewById(R.id.tvThoiGianUong);
            layoutDaUong = itemView.findViewById(R.id.layoutDaUong);
            rvThuocTrongCa = itemView.findViewById(R.id.rvThuocTrongCa);
            btnXacNhanCa = itemView.findViewById(R.id.btnXacNhanCa);
        }
    }
}