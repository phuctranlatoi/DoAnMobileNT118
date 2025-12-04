package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChiTietLichKhamActivity extends AppCompatActivity {

    private static final String TAG = "ChiTietLichKham";
    
    private Toolbar toolbar;
    private MaterialCardView cardMaKham, cardLyDoTuChoi;
    private TextView tvMaKham, tvTrangThai, tvBacSi, tvNgayKham, tvLyDoKham, tvLyDoTuChoi;
    private View layoutLyDoKham;
    private ProgressBar progressBar;
    
    private FirestoreRepository repo;
    private String maLichKham;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_lich_kham);

        maLichKham = getIntent().getStringExtra("MA_LICH_KHAM");
        if (maLichKham == null || maLichKham.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã lịch khám", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo = new FirestoreRepository();
        initViews();
        loadChiTiet();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardMaKham = findViewById(R.id.cardMaKham);
        cardLyDoTuChoi = findViewById(R.id.cardLyDoTuChoi);
        tvMaKham = findViewById(R.id.tvMaKham);
        tvTrangThai = findViewById(R.id.tvTrangThai);
        tvBacSi = findViewById(R.id.tvBacSi);
        tvNgayKham = findViewById(R.id.tvNgayKham);
        tvLyDoKham = findViewById(R.id.tvLyDoKham);
        tvLyDoTuChoi = findViewById(R.id.tvLyDoTuChoi);
        layoutLyDoKham = findViewById(R.id.layoutLyDoKham);
        progressBar = findViewById(R.id.progressBar);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadChiTiet() {
        progressBar.setVisibility(View.VISIBLE);

        repo.getCollection("LichKham")
            .document(maLichKham)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                progressBar.setVisibility(View.GONE);
                
                if (!documentSnapshot.exists()) {
                    Toast.makeText(this, "Không tìm thấy thông tin lịch khám", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                LichKham lichKham = documentSnapshot.toObject(LichKham.class);
                if (lichKham != null) {
                    hienThiThongTin(lichKham);
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi load chi tiết", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void hienThiThongTin(LichKham lichKham) {
        // Trạng thái
        String trangThai = lichKham.getTrangThai();
        if ("CHO".equals(trangThai)) {
            tvTrangThai.setText("⏳ Chờ xác nhận");
            tvTrangThai.setTextColor(getColor(R.color.warning));
        } else if ("XAC_NHAN".equals(trangThai)) {
            tvTrangThai.setText("✅ Đã xác nhận");
            tvTrangThai.setTextColor(getColor(R.color.success));
            // Hiển thị mã khám
            if (lichKham.getMaKhamBenh() != null && !lichKham.getMaKhamBenh().isEmpty()) {
                cardMaKham.setVisibility(View.VISIBLE);
                tvMaKham.setText(lichKham.getMaKhamBenh());
            }
        } else if ("HOAN_THANH".equals(trangThai)) {
            tvTrangThai.setText("✓ Hoàn thành");
            tvTrangThai.setTextColor(getColor(R.color.primary));
        } else if ("HUY".equals(trangThai)) {
            tvTrangThai.setText("✗ Đã hủy");
            tvTrangThai.setTextColor(getColor(R.color.danger));
            // Hiển thị lý do từ chối
            if (lichKham.getLyDoTuChoi() != null && !lichKham.getLyDoTuChoi().isEmpty()) {
                cardLyDoTuChoi.setVisibility(View.VISIBLE);
                tvLyDoTuChoi.setText(lichKham.getLyDoTuChoi());
            }
        }
        
        // Lý do khám
        if (lichKham.getLyDoKham() != null && !lichKham.getLyDoKham().isEmpty()) {
            layoutLyDoKham.setVisibility(View.VISIBLE);
            tvLyDoKham.setText(lichKham.getLyDoKham());
        }
        
        // Load tên bác sĩ và giờ khám từ LichLamViec
        loadTenBacSi(lichKham.getMaBacSi());
        loadGioKham(lichKham);
    }
    
    private void loadGioKham(LichKham lichKham) {
        if (lichKham.getNgayKham() == null) {
            return;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = lichKham.getNgayKham().toDate();
        String ngayKhamStr = sdf.format(date);
        
        // Ưu tiên dùng gioKham nếu có
        if (lichKham.getGioKham() != null && !lichKham.getGioKham().isEmpty()) {
            tvNgayKham.setText(ngayKhamStr + " - " + lichKham.getGioKham());
            return;
        }
        
        // Nếu không có gioKham, lấy từ LichLamViec
        if (lichKham.getMaLichLamViec() != null && !lichKham.getMaLichLamViec().isEmpty()) {
            repo.getCollection("LichLamViec")
                .document(lichKham.getMaLichLamViec())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String caLamViec = doc.getString("caLamViec");
                        if (caLamViec != null && !caLamViec.isEmpty()) {
                            tvNgayKham.setText(ngayKhamStr + " - Ca: " + caLamViec);
                        } else {
                            tvNgayKham.setText(ngayKhamStr);
                        }
                    } else {
                        tvNgayKham.setText(ngayKhamStr);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi load giờ khám", e);
                    tvNgayKham.setText(ngayKhamStr);
                });
        } else {
            // Fallback: hiển thị ngày giờ từ timestamp
            hienThiNgayKhamMacDinh(lichKham);
        }
    }
    
    private void hienThiNgayKhamMacDinh(LichKham lichKham) {
        if (lichKham.getNgayKham() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = lichKham.getNgayKham().toDate();
            tvNgayKham.setText(sdf.format(date));
        }
    }

    private void loadTenBacSi(String maBacSi) {
        repo.getCollection("BacSi")
            .document(maBacSi)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String tenBacSi = documentSnapshot.getString("hoTen");
                    if (tenBacSi != null) {
                        tvBacSi.setText("BS. " + tenBacSi);
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Lỗi load bác sĩ", e));
    }
}
