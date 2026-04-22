package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.ThongBao;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ThongBaoActivity extends AppCompatActivity {
    private RecyclerView rvThongBao;
    private ThongBaoAdapter adapter;
    private List<ThongBao> thongBaoList;
    private FirestoreRepository repo;
    private String maBenhNhan;
    private String maBacSi;
    private String userType; // "benhnhan" hoặc "bacsi"
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);

        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");
        userType = getIntent().getStringExtra("USER_TYPE");
        
        // Debug log
        android.util.Log.d("ThongBaoActivity", "maBenhNhan: " + maBenhNhan);
        android.util.Log.d("ThongBaoActivity", "maBacSi: " + maBacSi);
        android.util.Log.d("ThongBaoActivity", "userType: " + userType);
        
        // Nếu không có userType, tự động xác định
        if (userType == null) {
            userType = (maBenhNhan != null) ? "benhnhan" : "bacsi";
        }
        
        android.util.Log.d("ThongBaoActivity", "Final userType: " + userType);
        
        repo = new FirestoreRepository();

        setupToolbar();
        initViews();
        listenToThongBao();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }
    }

    private void initViews() {
        rvThongBao = findViewById(R.id.rvThongBao);
        thongBaoList = new ArrayList<>();
        adapter = new ThongBaoAdapter(thongBaoList, this::markAsRead);
        rvThongBao.setLayoutManager(new LinearLayoutManager(this));
        rvThongBao.setAdapter(adapter);
    }

    private void listenToThongBao() {
        Query query;
        
        // Tạo query dựa trên loại người dùng
        if ("bacsi".equals(userType)) {
            // Bác sĩ: lấy thông báo có maBacSi
            android.util.Log.d("ThongBaoActivity", "Query for bacsi with maBacSi: " + maBacSi);
            query = repo.getCollection("ThongBao")
                    .whereEqualTo("maBacSi", maBacSi);
        } else {
            // Bệnh nhân: lấy thông báo có maBenhNhan
            android.util.Log.d("ThongBaoActivity", "Query for benhnhan with maBenhNhan: " + maBenhNhan);
            query = repo.getCollection("ThongBao")
                    .whereEqualTo("maBenhNhan", maBenhNhan);
        }
        
        listenerRegistration = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                android.util.Log.e("ThongBaoActivity", "Error loading notifications: " + error.getMessage());
                Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                android.util.Log.d("ThongBaoActivity", "Received " + snapshots.size() + " notifications");
                thongBaoList.clear();
                List<ThongBao> tempList = snapshots.toObjects(ThongBao.class);
                
                // Debug log từng thông báo
                for (ThongBao tb : tempList) {
                    android.util.Log.d("ThongBaoActivity", "Notification: " + tb.getMaThongBao() + " - " + tb.getTieuDe());
                }
                
                // Sắp xếp theo thời gian trong code thay vì query
                tempList.sort((t1, t2) -> {
                    if (t1.getThoiGianGui() == null) return 1;
                    if (t2.getThoiGianGui() == null) return -1;
                    return t2.getThoiGianGui().compareTo(t1.getThoiGianGui());
                });
                
                thongBaoList.addAll(tempList);
                adapter.notifyDataSetChanged();
                
                android.util.Log.d("ThongBaoActivity", "Updated adapter with " + thongBaoList.size() + " notifications");
            } else {
                android.util.Log.d("ThongBaoActivity", "Snapshots is null");
            }
        });
    }

    private void markAsRead(ThongBao thongBao) {
        // Mở chi tiết thông báo
        android.content.Intent intent = new android.content.Intent(this, ChiTietThongBaoActivity.class);
        intent.putExtra("maThongBao", thongBao.getMaThongBao());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
