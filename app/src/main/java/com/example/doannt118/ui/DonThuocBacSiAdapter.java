package com.example.doannt118.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.DonThuoc;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DonThuocBacSiAdapter extends RecyclerView.Adapter<DonThuocBacSiAdapter.ViewHolder> {

    private static final String TAG = "DonThuocBacSiAdapter";
    private Context context;
    private List<DonThuoc> donThuocList;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DonThuocBacSiAdapter(Context context, List<DonThuoc> donThuocList) {
        this.context = context;
        this.donThuocList = donThuocList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_don_thuoc_bac_si, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonThuoc donThuoc = donThuocList.get(position);
        
        // Load tên bệnh nhân
        loadTenBenhNhan(holder, donThuoc.getMaBenhNhan());
        
        // Ngày kê đơn
        String ngayKe = "Chưa có";
        Date ngayKeDon = null;
        if (donThuoc.getNgayKeDon() != null) {
            ngayKeDon = donThuoc.getNgayKeDon().toDate();
            ngayKe = dateFormat.format(ngayKeDon);
        } else if (donThuoc.getNgayLap() != null) {
            ngayKeDon = donThuoc.getNgayLap();
            ngayKe = dateFormat.format(ngayKeDon);
        }
        holder.tvNgayKe.setText("Ngày kê: " + ngayKe);

        // Hiển thị trạng thái tạm thời (sẽ cập nhật sau khi load ChiTietDonThuoc)
        String trangThai = donThuoc.getTrangThai();
        if (trangThai == null) trangThai = "DANG_DUNG";
        updateTrangThaiUI(holder, trangThai);
        
        // Hiển thị thời gian dùng tạm thời (sẽ cập nhật sau khi load ChiTietDonThuoc)
        holder.tvThoiGianDung.setText("Đang tải...");
        
        // Load số loại thuốc, soNgayUong từ ChiTietDonThuoc và tính tỷ lệ tuân thủ
        loadChiTietDonThuoc(holder, donThuoc, ngayKeDon);
    }
    
    private void updateTrangThaiUI(ViewHolder holder, String trangThai) {
        switch (trangThai) {
            case "DANG_DUNG":
                holder.tvTrangThai.setText("Đang dùng");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_confirmed);
                break;
            case "DA_HET":
                holder.tvTrangThai.setText("Đã hoàn thành");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_completed);
                break;
            case "DA_HUY":
                holder.tvTrangThai.setText("Đã hủy");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
            default:
                holder.tvTrangThai.setText("Đang dùng");
                holder.tvTrangThai.setBackgroundResource(R.drawable.bg_status_confirmed);
        }
    }
    
    private void updateTrangThaiDonThuoc(String maDonThuoc, String trangThai) {
        if (maDonThuoc == null || maDonThuoc.isEmpty()) return;
        
        db.collection("DonThuoc").document(maDonThuoc)
            .update("trangThai", trangThai)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated trangThai to " + trangThai + " for " + maDonThuoc))
            .addOnFailureListener(e -> Log.e(TAG, "Error updating trangThai", e));
    }

    private void loadTenBenhNhan(ViewHolder holder, String maBenhNhan) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            holder.tvTenBenhNhan.setText("Không xác định");
            return;
        }
        
        db.collection("BenhNhan").document(maBenhNhan)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String hoTen = doc.getString("hoTen");
                    holder.tvTenBenhNhan.setText(hoTen != null ? hoTen : "Không xác định");
                } else {
                    holder.tvTenBenhNhan.setText("Không xác định");
                }
            })
            .addOnFailureListener(e -> {
                holder.tvTenBenhNhan.setText("Không xác định");
                Log.e(TAG, "Error loading patient name", e);
            });
    }

    private void loadChiTietDonThuoc(ViewHolder holder, DonThuoc donThuoc, Date ngayKeDon) {
        String maDonThuoc = donThuoc.getMaDonThuoc();
        String maBenhNhan = donThuoc.getMaBenhNhan();
        
        if (maDonThuoc == null || maDonThuoc.isEmpty()) {
            holder.tvSoLoaiThuoc.setText("0 loại");
            holder.tvThoiGianDung.setText("0 ngày");
            holder.tvTyLeTuanThu.setText("0%");
            holder.progressTuanThu.setProgress(0);
            holder.tvChiTietTuanThu.setText("Chưa có dữ liệu");
            return;
        }
        
        // Load số loại thuốc và soNgayUong từ ChiTietDonThuoc
        db.collection("ChiTietDonThuoc")
            .whereEqualTo("maDonThuoc", maDonThuoc)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int soLoaiThuoc = querySnapshot.size();
                holder.tvSoLoaiThuoc.setText(soLoaiThuoc + " loại");
                
                // Lấy soNgayUong từ ChiTietDonThuoc (lấy giá trị max)
                int soNgayUong = 0;
                for (var doc : querySnapshot.getDocuments()) {
                    Long soNgay = doc.getLong("soNgayUong");
                    if (soNgay != null && soNgay > soNgayUong) {
                        soNgayUong = soNgay.intValue();
                    }
                }
                
                Log.d(TAG, "DonThuoc: maDonThuoc=" + maDonThuoc + 
                    ", ngayKeDon=" + (ngayKeDon != null ? dateFormat.format(ngayKeDon) : "null") + 
                    ", soNgayUong từ ChiTietDonThuoc=" + soNgayUong);
                
                // Hiển thị thời gian dùng
                holder.tvThoiGianDung.setText(soNgayUong + " ngày");
                
                // Kiểm tra và cập nhật trạng thái đơn thuốc nếu đã hết hạn
                String trangThai = donThuoc.getTrangThai();
                if (trangThai == null) trangThai = "DANG_DUNG";
                
                if ("DANG_DUNG".equals(trangThai) && ngayKeDon != null && soNgayUong > 0) {
                    Calendar calNgayKetThuc = Calendar.getInstance();
                    calNgayKetThuc.setTime(ngayKeDon);
                    calNgayKetThuc.add(Calendar.DAY_OF_MONTH, soNgayUong);
                    
                    Calendar calHomNay = Calendar.getInstance();
                    calHomNay.set(Calendar.HOUR_OF_DAY, 0);
                    calHomNay.set(Calendar.MINUTE, 0);
                    calHomNay.set(Calendar.SECOND, 0);
                    calHomNay.set(Calendar.MILLISECOND, 0);
                    
                    Log.d(TAG, "Checking: ngayKetThuc=" + dateFormat.format(calNgayKetThuc.getTime()) + 
                        ", homNay=" + dateFormat.format(calHomNay.getTime()) +
                        ", isBefore=" + calNgayKetThuc.getTime().before(calHomNay.getTime()));
                    
                    // Nếu ngày kết thúc < ngày hôm nay → đã hoàn thành
                    if (calNgayKetThuc.getTime().before(calHomNay.getTime())) {
                        trangThai = "DA_HET";
                        Log.d(TAG, "Updating trangThai to DA_HET for " + maDonThuoc);
                        // Cập nhật trạng thái trong Firestore
                        updateTrangThaiDonThuoc(maDonThuoc, "DA_HET");
                        // Cập nhật UI
                        updateTrangThaiUI(holder, "DA_HET");
                    }
                }
                
                // Tính số ngày đã qua từ ngày kê đơn
                int soNgayDaQua = 0;
                if (ngayKeDon != null) {
                    long diffInMillis = new Date().getTime() - ngayKeDon.getTime();
                    soNgayDaQua = (int) TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 1;
                    // Giới hạn không vượt quá số ngày uống
                    if (soNgayUong > 0 && soNgayDaQua > soNgayUong) {
                        soNgayDaQua = soNgayUong;
                    }
                    if (soNgayDaQua < 1) soNgayDaQua = 1;
                }
                
                // Tổng số ca cần uống = số ngày đã qua * 3 ca (sáng, trưa, chiều)
                int tongSoCaCanUong = soNgayDaQua * 3;
                
                // Load số ca đã uống từ XacNhanUongThuoc (bệnh nhân xác nhận)
                loadTyLeTuanThu(holder, maBenhNhan, tongSoCaCanUong, ngayKeDon, soNgayDaQua);
            })
            .addOnFailureListener(e -> {
                holder.tvSoLoaiThuoc.setText("0 loại");
                holder.tvThoiGianDung.setText("0 ngày");
                Log.e(TAG, "Error loading prescription details", e);
            });
    }

    private void loadTyLeTuanThu(ViewHolder holder, String maBenhNhan, int tongSoCaCanUong, Date ngayBatDau, int soNgayDaQua) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            holder.tvTyLeTuanThu.setText("0%");
            holder.progressTuanThu.setProgress(0);
            holder.tvChiTietTuanThu.setText("Chưa có dữ liệu");
            return;
        }
        
        // Chuẩn hóa ngày bắt đầu về 00:00:00 để so sánh chính xác
        Date ngayBatDauChuan = null;
        if (ngayBatDau != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(ngayBatDau);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            ngayBatDauChuan = cal.getTime();
        }
        
        final Date finalNgayBatDau = ngayBatDauChuan;
        
        // Query collection XacNhanUongThuoc - nơi bệnh nhân xác nhận đã uống thuốc
        db.collection("XacNhanUongThuoc")
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .whereEqualTo("daUong", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int soCaDaUong = 0;
                
                // Đếm số ca đã uống
                SimpleDateFormat keyDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                Log.d(TAG, "Query XacNhanUongThuoc: found " + querySnapshot.size() + " records for maBenhNhan=" + maBenhNhan);
                
                for (var doc : querySnapshot.getDocuments()) {
                    String maXacNhan = doc.getString("maXacNhan");
                    Log.d(TAG, "Checking record: maXacNhan=" + maXacNhan);
                    
                    if (maXacNhan != null && maXacNhan.startsWith("CA_")) {
                        // Key format: CA_[maCa]_[maBenhNhan]_[yyyy-MM-dd]
                        if (finalNgayBatDau != null) {
                            try {
                                String[] parts = maXacNhan.split("_");
                                if (parts.length >= 4) {
                                    String ngayXacNhan = parts[parts.length - 1];
                                    Date dateXacNhan = keyDateFormat.parse(ngayXacNhan);
                                    
                                    Log.d(TAG, "Comparing: ngayXacNhan=" + ngayXacNhan + ", ngayBatDau=" + keyDateFormat.format(finalNgayBatDau));
                                    
                                    // Chỉ đếm nếu ngày xác nhận >= ngày bắt đầu
                                    if (dateXacNhan != null && !dateXacNhan.before(finalNgayBatDau)) {
                                        soCaDaUong++;
                                        Log.d(TAG, "Counted! soCaDaUong=" + soCaDaUong);
                                    }
                                }
                            } catch (Exception e) {
                                // Nếu không parse được, vẫn đếm
                                soCaDaUong++;
                                Log.d(TAG, "Parse error, still counted. soCaDaUong=" + soCaDaUong);
                            }
                        } else {
                            soCaDaUong++;
                            Log.d(TAG, "No ngayBatDau, counted. soCaDaUong=" + soCaDaUong);
                        }
                    } else {
                        // Xác nhận không theo format CA_, vẫn đếm
                        soCaDaUong++;
                        Log.d(TAG, "Not CA_ format, counted. soCaDaUong=" + soCaDaUong);
                    }
                }
                
                Log.d(TAG, "Final: soCaDaUong=" + soCaDaUong + ", tongSoCaCanUong=" + tongSoCaCanUong);
                
                // Tính tỷ lệ tuân thủ
                int tyLe = 0;
                if (tongSoCaCanUong > 0) {
                    tyLe = (int) ((soCaDaUong * 100.0) / tongSoCaCanUong);
                    if (tyLe > 100) tyLe = 100;
                }
                
                holder.tvTyLeTuanThu.setText(tyLe + "%");
                holder.progressTuanThu.setProgress(tyLe);
                holder.tvChiTietTuanThu.setText("Đã uống " + soCaDaUong + "/" + tongSoCaCanUong + " ca (" + soNgayDaQua + " ngày)");
                
                // Đổi màu theo tỷ lệ
                if (tyLe >= 80) {
                    holder.tvTyLeTuanThu.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                } else if (tyLe >= 50) {
                    holder.tvTyLeTuanThu.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    holder.tvTyLeTuanThu.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                }
            })
            .addOnFailureListener(e -> {
                holder.tvTyLeTuanThu.setText("0%");
                holder.progressTuanThu.setProgress(0);
                holder.tvChiTietTuanThu.setText("Chưa có dữ liệu");
                Log.e(TAG, "Error loading compliance data", e);
            });
    }

    @Override
    public int getItemCount() {
        return donThuocList != null ? donThuocList.size() : 0;
    }

    public void updateData(List<DonThuoc> newList) {
        this.donThuocList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenBenhNhan, tvNgayKe, tvTrangThai;
        TextView tvSoLoaiThuoc, tvThoiGianDung;
        TextView tvTyLeTuanThu, tvChiTietTuanThu;
        ProgressBar progressTuanThu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenBenhNhan = itemView.findViewById(R.id.tvTenBenhNhan);
            tvNgayKe = itemView.findViewById(R.id.tvNgayKe);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvSoLoaiThuoc = itemView.findViewById(R.id.tvSoLoaiThuoc);
            tvThoiGianDung = itemView.findViewById(R.id.tvThoiGianDung);
            tvTyLeTuanThu = itemView.findViewById(R.id.tvTyLeTuanThu);
            tvChiTietTuanThu = itemView.findViewById(R.id.tvChiTietTuanThu);
            progressTuanThu = itemView.findViewById(R.id.progressTuanThu);
        }
    }
}
