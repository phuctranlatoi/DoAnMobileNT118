package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.DichVuKham;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChonDichVuKhamActivity extends AppCompatActivity {
    
    private RecyclerView rvDichVu;
    private TextView tvTongTien;
    private Button btnXacNhan;
    private TabLayout tabLayout;
    private DichVuKhamAdapter adapter;
    private FirestoreRepository repo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_dich_vu_kham);
        
        repo = new FirestoreRepository();
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        rvDichVu = findViewById(R.id.rvDichVu);
        tvTongTien = findViewById(R.id.tvTongTien);
        btnXacNhan = findViewById(R.id.btnXacNhan);
        tabLayout = findViewById(R.id.tabLayout);
        
        setupRecyclerView();
        setupTabs();
        setupButton();
        
        // Load dịch vụ khám cơ bản đầu tiên
        loadDichVu("KHAM_CO_BAN");
    }
    
    private void setupRecyclerView() {
        adapter = new DichVuKhamAdapter(tongTien -> {
            tvTongTien.setText(String.format("%,d đ", tongTien));
        });
        rvDichVu.setLayoutManager(new LinearLayoutManager(this));
        rvDichVu.setAdapter(adapter);
    }
    
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Khám cơ bản"));
        tabLayout.addTab(tabLayout.newTab().setText("Chuyên sâu"));
        tabLayout.addTab(tabLayout.newTab().setText("Xét nghiệm"));
        tabLayout.addTab(tabLayout.newTab().setText("Chụp chiếu"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String loai = getLoaiFromPosition(tab.getPosition());
                loadDichVu(loai);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupButton() {
        btnXacNhan.setOnClickListener(v -> {
            List<DichVuKham> selected = adapter.getDanhSachChon();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent result = new Intent();
            result.putExtra("DICH_VU_CHON", (Serializable) selected);
            result.putExtra("TONG_TIEN", adapter.getTongTien());
            setResult(RESULT_OK, result);
            finish();
        });
    }
    
    private String getLoaiFromPosition(int position) {
        switch (position) {
            case 0: return "KHAM_CO_BAN";
            case 1: return "KHAM_CHUYEN_SAU";
            case 2: return "XET_NGHIEM";
            case 3: return "CHUP_CHIEU";
            default: return "KHAM_CO_BAN";
        }
    }
    
    private void loadDichVu(String loai) {
        repo.getByField("DichVuKham", "loaiDichVu", loai,
            querySnapshot -> {
                List<DichVuKham> list = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    DichVuKham dv = doc.toObject(DichVuKham.class);
                    if (dv != null && dv.isActive()) {
                        list.add(dv);
                    }
                }
                adapter.updateData(list);
            },
            e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
