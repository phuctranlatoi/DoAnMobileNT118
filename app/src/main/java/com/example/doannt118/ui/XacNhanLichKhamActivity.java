package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class XacNhanLichKhamActivity extends AppCompatActivity {

    private static final String TAG = "XacNhanLichKham";
    
    private RecyclerView rvLichKham;
    private TextView tvThongBao;
    private ProgressBar progressBar;
    private Button btnFilterCho, btnFilterDaXacNhan;
    private ImageView btnBack;
    
    private FirestoreRepository repo;
    private XacNhanLichKhamAdapter adapter;
    private String maTaiKhoan;
    private String maBacSi;
    private String currentFilter = "CHO"; // CHO hoặc XAC_NHAN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_nhan_lich_kham);

        // Nhận dữ liệu từ Intent
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");

        if (maTaiKhoan == null || maBacSi == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo
        repo = new FirestoreRepository();
        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Load dữ liệu
        loadDanhSachLichKham();
    }

    private View loadingOverlay, layoutEmpty;
    
    private void initViews() {
        rvLichKham = findViewById(R.id.rvLichKham);
        tvThongBao = findViewById(R.id.tvThongBao);
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnFilterCho = findViewById(R.id.btnFilterCho);
        btnFilterDaXacNhan = findViewById(R.id.btnFilterDaXacNhan);
        
        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new XacNhanLichKhamAdapter(this, new ArrayList<>(), 
            new XacNhanLichKhamAdapter.OnLichKhamActionListener() {
                @Override
                public void onXacNhan(LichKham lichKham) {
                    handleXacNhan(lichKham);
                }

                @Override
                public void onHuy(LichKham lichKham) {
                    handleHuy(lichKham);
                }
            });
        
        rvLichKham.setLayoutManager(new LinearLayoutManager(this));
        rvLichKham.setAdapter(adapter);
    }

    private void setupListeners() {
        btnFilterCho.setOnClickListener(v -> {
            currentFilter = "CHO";
            updateFilterButtons();
            loadDanhSachLichKham();
        });

        btnFilterDaXacNhan.setOnClickListener(v -> {
            currentFilter = "XAC_NHAN";
            updateFilterButtons();
            loadDanhSachLichKham();
        });
    }

    private void updateFilterButtons() {
        if ("CHO".equals(currentFilter)) {
            btnFilterCho.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
            btnFilterCho.setTextColor(0xFFFFFFFF);
            btnFilterDaXacNhan.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            btnFilterDaXacNhan.setTextColor(0xFF7F8C8D);
        } else {
            btnFilterDaXacNhan.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
            btnFilterDaXacNhan.setTextColor(0xFFFFFFFF);
            btnFilterCho.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            btnFilterCho.setTextColor(0xFF7F8C8D);
        }
    }

    private void loadDanhSachLichKham() {
        loadingOverlay.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                List<LichKham> danhSach = new ArrayList<>();
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    LichKham lichKham = doc.toObject(LichKham.class);
                    if (lichKham != null && currentFilter.equals(lichKham.getTrangThai())) {
                        danhSach.add(lichKham);
                    }
                }

                loadingOverlay.setVisibility(View.GONE);
                
                if (danhSach.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    if ("CHO".equals(currentFilter)) {
                        tvThongBao.setText("Không có lịch khám chờ xác nhận");
                    } else {
                        tvThongBao.setText("Chưa có lịch khám nào được xác nhận");
                    }
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }
                
                adapter.updateData(danhSach);
                Log.d(TAG, "Loaded " + danhSach.size() + " lịch khám với trạng thái: " + currentFilter);
            },
            e -> {
                loadingOverlay.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi tải danh sách lịch khám", e);
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                layoutEmpty.setVisibility(View.VISIBLE);
                tvThongBao.setText("Lỗi tải dữ liệu");
            });
    }

    private void handleXacNhan(LichKham lichKham) {
        loadingOverlay.setVisibility(View.VISIBLE);
        
        repo.updateDocumentFields("LichKham", lichKham.getMaLichKham(),
            java.util.Collections.singletonMap("trangThai", "XAC_NHAN"),
            aVoid -> {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(this, "✓ Xác nhận lịch khám thành công!", Toast.LENGTH_SHORT).show();
                logActivity("Xác nhận lịch khám: " + lichKham.getMaLichKham());
                loadDanhSachLichKham();
            },
            e -> {
                loadingOverlay.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi xác nhận lịch khám", e);
                Toast.makeText(this, "✗ Xác nhận thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void handleHuy(LichKham lichKham) {
        // Hiển thị dialog xác nhận trước khi hủy
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận từ chối")
            .setMessage("Bạn có chắc chắn muốn từ chối lịch khám này?")
            .setPositiveButton("Từ chối", (dialog, which) -> {
                loadingOverlay.setVisibility(View.VISIBLE);
                
                repo.updateDocumentFields("LichKham", lichKham.getMaLichKham(),
                    java.util.Collections.singletonMap("trangThai", "HUY"),
                    aVoid -> {
                        loadingOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "✓ Đã từ chối lịch khám!", Toast.LENGTH_SHORT).show();
                        logActivity("Từ chối lịch khám: " + lichKham.getMaLichKham());
                        loadDanhSachLichKham();
                    },
                    e -> {
                        loadingOverlay.setVisibility(View.GONE);
                        Log.e(TAG, "Lỗi hủy lịch khám", e);
                        Toast.makeText(this, "✗ Từ chối thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy bỏ", null)
            .show();
    }

    private void logActivity(String tenHoatDong) {
        String maLichSu = UUID.randomUUID().toString();
        LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoan, tenHoatDong, new Date(), "Bác sĩ " + tenHoatDong);
        repo.logActivity(lichSu);
    }
}
