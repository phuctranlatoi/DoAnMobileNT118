package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class XemChiTietLichKhamActivity extends AppCompatActivity {

    private static final String TAG = "XemChiTietLichKham";
    
    private RecyclerView rvChiTietLichKham;
    private TextView tvCaLamViec, tvEmpty;
    private ProgressBar progressBar;
    private ImageView btnBack;
    
    private FirestoreRepository repo;
    private ChiTietLichKhamAdapter adapter;
    private String maLichLamViec;
    private String caLamViec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_chi_tiet_lich_kham);

        // Nhận dữ liệu từ Intent
        maLichLamViec = getIntent().getStringExtra("MA_LICH_LAM_VIEC");
        caLamViec = getIntent().getStringExtra("CA_LAM_VIEC");

        if (maLichLamViec == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin lịch làm việc!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo
        repo = new FirestoreRepository();
        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Load dữ liệu
        loadChiTietLichKham();
    }

    private void initViews() {
        rvChiTietLichKham = findViewById(R.id.rvChiTietLichKham);
        tvCaLamViec = findViewById(R.id.tvCaLamViec);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        
        if (caLamViec != null) {
            tvCaLamViec.setText("Ca làm việc: " + caLamViec);
        }
    }

    private void setupRecyclerView() {
        adapter = new ChiTietLichKhamAdapter(this, new ArrayList<>());
        rvChiTietLichKham.setLayoutManager(new LinearLayoutManager(this));
        rvChiTietLichKham.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadChiTietLichKham() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repo.getByField("LichKham", "maLichLamViec", maLichLamViec,
            querySnapshot -> {
                List<LichKham> danhSach = new ArrayList<>();
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    LichKham lichKham = doc.toObject(LichKham.class);
                    if (lichKham != null && !"HUY".equals(lichKham.getTrangThai())) {
                        danhSach.add(lichKham);
                    }
                }

                progressBar.setVisibility(View.GONE);
                
                if (danhSach.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                
                adapter.updateData(danhSach);
                Log.d(TAG, "Loaded " + danhSach.size() + " lịch khám");
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi tải chi tiết lịch khám", e);
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
            });
    }
}
