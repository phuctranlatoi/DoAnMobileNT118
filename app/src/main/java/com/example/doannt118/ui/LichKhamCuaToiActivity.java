package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LichKhamCuaToiActivity extends AppCompatActivity {

    private static final String TAG = "LichKhamCuaToi";
    
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvLichKham;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    
    private FirestoreRepository repo;
    private LichKhamCuaToiAdapter adapter;
    private String maBenhNhan;
    private String currentFilter = "XAC_NHAN"; // Mặc định hiển thị đã xác nhận

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_kham_cua_toi);

        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã bệnh nhân", Toast.LENGTH_SHORT).show();
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
        tabLayout = findViewById(R.id.tabLayout);
        rvLichKham = findViewById(R.id.rvLichKham);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Set tab mặc định
        tabLayout.selectTab(tabLayout.getTabAt(1)); // Tab "Đã xác nhận"
    }

    private void setupRecyclerView() {
        adapter = new LichKhamCuaToiAdapter(this, new ArrayList<>(), lichKham -> {
            // Xem chi tiết lịch khám
            Intent intent = new Intent(this, ChiTietLichKhamActivity.class);
            intent.putExtra("MA_LICH_KHAM", lichKham.getMaLichKham());
            startActivity(intent);
        });
        
        rvLichKham.setLayoutManager(new LinearLayoutManager(this));
        rvLichKham.setAdapter(adapter);
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentFilter = "CHO";
                        break;
                    case 1:
                        currentFilter = "XAC_NHAN";
                        break;
                    case 2:
                        currentFilter = "HOAN_THANH";
                        break;
                    case 3:
                        currentFilter = "HUY";
                        break;
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

        repo.getByField("LichKham", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<LichKham> danhSach = new ArrayList<>();
                
                for (var doc : querySnapshot.getDocuments()) {
                    LichKham lichKham = doc.toObject(LichKham.class);
                    if (lichKham != null && currentFilter.equals(lichKham.getTrangThai())) {
                        danhSach.add(lichKham);
                    }
                }

                // Sắp xếp theo thời gian: gần nhất lên đầu (so với hiện tại)
                long now = System.currentTimeMillis();
                danhSach.sort((l1, l2) -> {
                    if (l1.getNgayKham() == null) return 1;
                    if (l2.getNgayKham() == null) return -1;
                    
                    long time1 = l1.getNgayKham().toDate().getTime();
                    long time2 = l2.getNgayKham().toDate().getTime();
                    
                    // Tính khoảng cách đến hiện tại
                    long diff1 = Math.abs(time1 - now);
                    long diff2 = Math.abs(time2 - now);
                    
                    // Ưu tiên lịch sắp tới (trong tương lai)
                    if (time1 >= now && time2 < now) return -1;
                    if (time1 < now && time2 >= now) return 1;
                    
                    // Cả 2 đều trong tương lai hoặc quá khứ: gần nhất lên đầu
                    return Long.compare(diff1, diff2);
                });

                progressBar.setVisibility(View.GONE);
                
                if (danhSach.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    updateEmptyMessage();
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                
                // Load tên bác sĩ cho từng lịch khám
                loadTenBacSiChoTatCa(danhSach);
                
                Log.d(TAG, "Loaded " + danhSach.size() + " lịch khám với trạng thái: " + currentFilter);
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "Lỗi tải danh sách", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadTenBacSiChoTatCa(List<LichKham> danhSach) {
        for (LichKham lichKham : danhSach) {
            if (lichKham.getMaBacSi() != null && !lichKham.getMaBacSi().isEmpty()) {
                repo.getCollection("BacSi")
                    .document(lichKham.getMaBacSi())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String tenBacSi = doc.getString("hoTen");
                            if (tenBacSi != null) {
                                lichKham.setTenBacSi(tenBacSi);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Lỗi load tên bác sĩ", e));
            }
        }
        
        adapter.updateData(danhSach);
    }

    private void updateEmptyMessage() {
        switch (currentFilter) {
            case "CHO":
                tvEmpty.setText("Chưa có lịch khám chờ xác nhận");
                break;
            case "XAC_NHAN":
                tvEmpty.setText("Chưa có lịch khám đã xác nhận");
                break;
            case "HOAN_THANH":
                tvEmpty.setText("Chưa có lịch khám hoàn thành");
                break;
            case "HUY":
                tvEmpty.setText("Chưa có lịch khám bị hủy");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại
        loadDanhSachLichKham();
    }
}
