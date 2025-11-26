package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XacNhanLichKhamActivity extends AppCompatActivity {

    private static final String TAG = "XacNhanLichKham";
    
    private RecyclerView rvLichKham;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    
    private FirestoreRepository repo;
    private XacNhanLichKhamAdapter adapter;
    private String maBacSi;
    private String currentFilter = "CHO_XAC_NHAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_nhan_lich_kham);

        maBacSi = getIntent().getStringExtra("MA_BAC_SI");
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã bác sĩ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo = new FirestoreRepository();
        initViews();
        setupRecyclerView();
        setupListeners();
        loadDanhSachLichKham();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvLichKham = findViewById(R.id.rvLichKham);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Xác nhận lịch khám");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new XacNhanLichKhamAdapter(this, new ArrayList<>(), 
            new XacNhanLichKhamAdapter.OnLichKhamActionListener() {
                @Override
                public void onXacNhan(LichKham lichKham) {
                    showConfirmDialog(lichKham, true);
                }

                @Override
                public void onTuChoi(LichKham lichKham) {
                    showConfirmDialog(lichKham, false);
                }
            });
        
        rvLichKham.setLayoutManager(new LinearLayoutManager(this));
        rvLichKham.setAdapter(adapter);
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    currentFilter = "CHO_XAC_NHAN";
                } else if (position == 1) {
                    currentFilter = "DA_XAC_NHAN";
                } else {
                    currentFilter = "TU_CHOI";
                }
                loadDanhSachLichKham();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadDanhSachLichKham() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                List<LichKham> danhSach = new ArrayList<>();
                
                for (var doc : querySnapshot.getDocuments()) {
                    LichKham lichKham = doc.toObject(LichKham.class);
                    if (lichKham != null && currentFilter.equals(lichKham.getTrangThai())) {
                        danhSach.add(lichKham);
                    }
                }

                // Sắp xếp theo thời gian: lịch cũ hơn (trước) lên đầu
                danhSach.sort((l1, l2) -> {
                    if (l1.getThoiGianKham() == null) return 1;
                    if (l2.getThoiGianKham() == null) return -1;
                    return l1.getThoiGianKham().compareTo(l2.getThoiGianKham());
                });

                progressBar.setVisibility(View.GONE);
                
                if (danhSach.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    updateEmptyMessage();
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                
                adapter.updateData(danhSach);
                Log.d(TAG, "Loaded " + danhSach.size() + " lịch khám (sorted by time)");
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "Lỗi tải danh sách", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateEmptyMessage() {
        switch (currentFilter) {
            case "CHO_XAC_NHAN":
                tvEmpty.setText("Không có lịch khám chờ xác nhận");
                break;
            case "DA_XAC_NHAN":
                tvEmpty.setText("Chưa có lịch khám nào được xác nhận");
                break;
            case "TU_CHOI":
                tvEmpty.setText("Chưa có lịch khám nào bị từ chối");
                break;
        }
    }

    private void showConfirmDialog(LichKham lichKham, boolean isApprove) {
        String title = isApprove ? "Xác nhận lịch khám" : "Từ chối lịch khám";
        String message = isApprove ? 
            "Bạn có chắc chắn muốn xác nhận lịch khám này?" : 
            "Bạn có chắc chắn muốn từ chối lịch khám này?";
        
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(isApprove ? "Xác nhận" : "Từ chối", (dialog, which) -> {
                if (isApprove) {
                    handleXacNhan(lichKham);
                } else {
                    handleTuChoi(lichKham);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void handleXacNhan(LichKham lichKham) {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", "DA_XAC_NHAN");
        
        repo.updateDocumentFields("LichKham", lichKham.getMaLichKham(), updates,
            aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "✓ Xác nhận thành công!", Toast.LENGTH_SHORT).show();
                loadDanhSachLichKham();
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi xác nhận", e);
                Toast.makeText(this, "✗ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void handleTuChoi(LichKham lichKham) {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", "TU_CHOI");
        
        repo.updateDocumentFields("LichKham", lichKham.getMaLichKham(), updates,
            aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "✓ Đã từ chối lịch khám!", Toast.LENGTH_SHORT).show();
                loadDanhSachLichKham();
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi từ chối", e);
                Toast.makeText(this, "✗ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
