package com.example.doannt118.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private OnStatusChangeListener listener;

    public interface OnStatusChangeListener {
        void onStatusChanged();
    }

    public LichSuUongThuocAdapter(Context context, OnStatusChangeListener listener) {
        this.context = context;
        this.lichSuList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.listener = listener;
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
        String caUong = lich.getCaUong() != null ? lich.getCaUong() : "";
        switch (caUong) {
            case "SANG": caUong = "Ca sáng"; break;
            case "TRUA": caUong = "Ca trưa"; break;
            case "CHIEU": caUong = "Ca chiều"; break;
            case "TOI": caUong = "Ca tối"; break;
            default: caUong = "Ca " + caUong.toLowerCase(); break;
        }
        ngayGio += " - " + caUong;
        holder.tvNgayGio.setText(ngayGio);
        
        String trangThai = lich.getTrangThai();
        if ("DA_UONG".equals(trangThai)) {
            holder.tvTrangThai.setText("✅ Đã hoàn thành");
            holder.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
            holder.tvTrangThai.setBackgroundResource(R.drawable.status_success_background);
            updateIconBackground(holder.ivIcon, "#4CAF50", "#388E3C");
            holder.tvThoiGian.setVisibility(View.VISIBLE);
            holder.layoutButtons.setVisibility(View.GONE);
            holder.itemView.setAlpha(0.95f);
            
            // Hiệu ứng thành công với animation
            animateSuccess(holder.itemView);
        } else if ("BO_QUA".equals(trangThai)) {
            holder.tvTrangThai.setText("❌ Đã bỏ qua");
            holder.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
            holder.tvTrangThai.setBackgroundResource(R.drawable.status_skip_background);
            updateIconBackground(holder.ivIcon, "#FF5722", "#D32F2F");
            holder.tvThoiGian.setVisibility(View.VISIBLE);
            holder.layoutButtons.setVisibility(View.GONE);
            holder.itemView.setAlpha(0.95f);
            
            // Hiệu ứng bỏ qua
            animateSkip(holder.itemView);
        } else {
            holder.tvTrangThai.setText("⏱️ Chờ xác nhận");
            holder.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            holder.tvTrangThai.setBackgroundResource(R.drawable.status_background);
            updateIconBackground(holder.ivIcon, "#667EEA", "#764BA2");
            holder.tvThoiGian.setVisibility(View.GONE);
            holder.layoutButtons.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
            
            // Hiệu ứng chờ xác nhận với glow effect
            animatePending(holder.tvTrangThai, holder.itemView);
        }
        
        if (lich.getThoiGianXacNhan() != null) {
            holder.tvThoiGian.setText("Xác nhận lúc: " + timeFormat.format(lich.getThoiGianXacNhan().toDate()));
        } else {
            holder.tvThoiGian.setText("");
        }
        
        holder.tvDanhSachThuoc.setText("Đơn thuốc: " + lich.getMaDonThuoc());
        
        // Xử lý click nút với hiệu ứng
        holder.btnDaUong.setOnClickListener(v -> {
            // Hiệu ứng nhấn nút
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                    showConfirmDialog(lich, "DA_UONG", position, 
                        "Xác nhận đã uống thuốc", 
                        "Bạn có chắc chắn đã uống thuốc theo đúng liều lượng?",
                        "✅ Đã uống", "#27AE60");
                });
        });
        
        holder.btnBoQua.setOnClickListener(v -> {
            // Hiệu ứng nhấn nút
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                    showConfirmDialog(lich, "BO_QUA", position,
                        "Xác nhận bỏ qua", 
                        "Bạn có chắc chắn muốn bỏ qua lần uống thuốc này?",
                        "⏭️ Bỏ qua", "#E74C3C");
                });
        });
    }
    

    
    private void updateStatus(LichUongThuoc lich, String newStatus, int position) {
        android.util.Log.d("LichSuUongThuocAdapter", "updateStatus: maLichUong=" + lich.getMaLichUong() + ", newStatus=" + newStatus);
        
        if (lich.getMaLichUong() == null || lich.getMaLichUong().isEmpty()) {
            android.widget.Toast.makeText(context, "Lỗi: Mã lịch uống không hợp lệ!", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Không cần hiển thị loading state vì sẽ reload ngay sau khi cập nhật
        
        com.example.doannt118.repository.FirestoreRepository repo = new com.example.doannt118.repository.FirestoreRepository();
        
        com.google.firebase.Timestamp now = com.google.firebase.Timestamp.now();
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("trangThai", newStatus);
        updates.put("thoiGianXacNhan", now);
        
        android.util.Log.d("LichSuUongThuocAdapter", "Updating document: " + lich.getMaLichUong() + " with data: " + updates);
        
        repo.updateDocumentFields("LichUongThuoc", lich.getMaLichUong(), updates,
            aVoid -> {
                android.util.Log.d("LichSuUongThuocAdapter", "Update successful for: " + lich.getMaLichUong());
                
                String message = "DA_UONG".equals(newStatus) ? 
                    "✅ Đã xác nhận uống thuốc!" : "⏭️ Đã bỏ qua lần uống này!";
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
                
                // Reload toàn bộ dữ liệu từ Firestore để đảm bảo đồng bộ
                if (listener != null) {
                    listener.onStatusChanged();
                }
            },
            e -> {
                android.util.Log.e("LichSuUongThuocAdapter", "Update failed for: " + lich.getMaLichUong(), e);
                android.widget.Toast.makeText(context, "❌ Lỗi cập nhật: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                
                // Lỗi sẽ được hiển thị qua Toast message
            }
        );
    }

    private void showConfirmDialog(LichUongThuoc lich, String newStatus, int position, 
                                  String title, String message, String buttonText, String buttonColor) {
        new androidx.appcompat.app.AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonText, (dialog, which) -> {
                updateStatus(lich, newStatus, position);
            })
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();
    }

    private void animateSuccess(View itemView) {
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(itemView, "scaleX", 1.0f, 1.02f, 1.0f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(itemView, "scaleY", 1.0f, 1.02f, 1.0f);
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(600);
        animatorSet.start();
    }
    
    private void animateSkip(View itemView) {
        android.animation.ObjectAnimator alpha = android.animation.ObjectAnimator.ofFloat(itemView, "alpha", 1.0f, 0.7f, 1.0f);
        alpha.setDuration(800);
        alpha.start();
    }
    
    private void animatePending(View statusView, View itemView) {
        // Hiệu ứng glow cho status
        android.animation.ObjectAnimator glow = android.animation.ObjectAnimator.ofFloat(statusView, "alpha", 1.0f, 0.6f, 1.0f);
        glow.setDuration(1500);
        glow.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        glow.start();
        
        // Hiệu ứng elevation cho card
        android.animation.ObjectAnimator elevation = android.animation.ObjectAnimator.ofFloat(itemView, "elevation", 12f, 16f, 12f);
        elevation.setDuration(2000);
        elevation.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        elevation.start();
    }

    private void updateIconBackground(ImageView icon, String startColor, String endColor) {
        try {
            android.graphics.drawable.GradientDrawable gradient = new android.graphics.drawable.GradientDrawable();
            gradient.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            gradient.setColors(new int[]{
                android.graphics.Color.parseColor(startColor),
                android.graphics.Color.parseColor(endColor)
            });
            gradient.setGradientType(android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT);
            gradient.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
            
            icon.getParent().getParent(); // CardView -> LinearLayout
            ((android.view.View) icon.getParent()).setBackground(gradient);
        } catch (Exception e) {
            // Fallback to solid color
            android.graphics.drawable.GradientDrawable fallback = new android.graphics.drawable.GradientDrawable();
            fallback.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            fallback.setColor(android.graphics.Color.parseColor(startColor));
            ((android.view.View) icon.getParent()).setBackground(fallback);
        }
    }

    @Override
    public int getItemCount() {
        return lichSuList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvNgayGio, tvTrangThai, tvThoiGian, tvDanhSachThuoc;
        LinearLayout layoutButtons;
        com.google.android.material.button.MaterialButton btnDaUong, btnBoQua;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvNgayGio = itemView.findViewById(R.id.tvNgayGio);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            tvDanhSachThuoc = itemView.findViewById(R.id.tvDanhSachThuoc);
            layoutButtons = itemView.findViewById(R.id.layoutButtons);
            btnDaUong = itemView.findViewById(R.id.btnDaUong);
            btnBoQua = itemView.findViewById(R.id.btnBoQua);
        }
    }
}
