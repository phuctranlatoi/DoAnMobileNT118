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
    private String currentFilter = "CHO";

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
                    currentFilter = "CHO";
                } else if (position == 1) {
                    currentFilter = "XAC_NHAN";
                } else {
                    currentFilter = "HUY";
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
                    if (lichKham != null) {
                        Log.d(TAG, "LichKham: " + lichKham.getMaLichKham() + " - TrangThai: " + lichKham.getTrangThai() + " - Filter: " + currentFilter);
                        if (currentFilter.equals(lichKham.getTrangThai())) {
                            danhSach.add(lichKham);
                        }
                    }
                }

                // Sắp xếp theo thời gian: lịch cũ hơn (trước) lên đầu
                danhSach.sort((l1, l2) -> {
                    if (l1.getNgayKham() == null) return 1;
                    if (l2.getNgayKham() == null) return -1;
                    return l1.getNgayKham().compareTo(l2.getNgayKham());
                });

                progressBar.setVisibility(View.GONE);
                
                if (danhSach.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    updateEmptyMessage();
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                
                adapter.updateData(danhSach);
                Log.d(TAG, "Loaded " + danhSach.size() + " lịch khám với trạng thái '" + currentFilter + "' (sorted by time)");
                
                // Debug: Hiển thị tổng số lịch khám theo từng trạng thái
                int countCho = 0, countXacNhan = 0, countHuy = 0, countKhac = 0;
                for (var doc : querySnapshot.getDocuments()) {
                    LichKham lk = doc.toObject(LichKham.class);
                    if (lk != null) {
                        String tt = lk.getTrangThai();
                        if ("CHO".equals(tt)) countCho++;
                        else if ("XAC_NHAN".equals(tt)) countXacNhan++;
                        else if ("HUY".equals(tt)) countHuy++;
                        else countKhac++;
                    }
                }
                Log.d(TAG, "Tổng số lịch: CHO=" + countCho + ", XAC_NHAN=" + countXacNhan + ", HUY=" + countHuy + ", Khác=" + countKhac);
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
            case "CHO":
                tvEmpty.setText("Không có lịch khám chờ xác nhận");
                break;
            case "XAC_NHAN":
                tvEmpty.setText("Chưa có lịch khám nào được xác nhận");
                break;
            case "HUY":
                tvEmpty.setText("Chưa có lịch khám nào bị từ chối");
                break;
        }
    }

    private void showConfirmDialog(LichKham lichKham, boolean isApprove) {
        if (isApprove) {
            new AlertDialog.Builder(this)
                .setTitle("Xác nhận lịch khám")
                .setMessage("Bạn có chắc chắn muốn xác nhận lịch khám này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> handleXacNhan(lichKham))
                .setNegativeButton("Hủy", null)
                .show();
        } else {
            showDialogTuChoiVoiLyDo(lichKham);
        }
    }
    
    private void showDialogTuChoiVoiLyDo(LichKham lichKham) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tu_choi_lich_kham, null);
        com.google.android.material.textfield.TextInputEditText edtLyDo = 
            dialogView.findViewById(R.id.edtLyDo);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Từ chối lịch khám")
            .setView(dialogView)
            .setPositiveButton("Từ chối", null)
            .setNegativeButton("Hủy", null)
            .create();
        
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String lyDo = edtLyDo.getText().toString().trim();
                if (lyDo.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập lý do từ chối", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleTuChoi(lichKham, lyDo);
                dialog.dismiss();
            });
        });
        
        dialog.show();
    }

    private void handleXacNhan(LichKham lichKham) {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", "XAC_NHAN");
        
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

    private void handleTuChoi(LichKham lichKham, String lyDo) {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", "HUY");
        updates.put("lyDoTuChoi", lyDo);
        
        repo.updateDocumentFields("LichKham", lichKham.getMaLichKham(), updates,
            aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "✓ Đã từ chối lịch khám!", Toast.LENGTH_SHORT).show();
                
                // Gửi thông báo cho bệnh nhân
                guiThongBaoTuChoiChoBenhNhan(lichKham.getMaBenhNhan(), lyDo);
                
                loadDanhSachLichKham();
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi từ chối", e);
                Toast.makeText(this, "✗ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void guiThongBaoTuChoiChoBenhNhan(String maBenhNhan, String lyDo) {
        String tieuDe = "Lịch khám bị từ chối";
        String noiDung = "Bác sĩ đã từ chối lịch khám của bạn. Lý do: " + lyDo;
        
        com.example.doannt118.utils.NotificationHelper.guiThongBaoChoBenhNhan(
            this,
            maBenhNhan,
            tieuDe,
            noiDung,
            "LICH_HEN",
            ""
        );
    }
}
