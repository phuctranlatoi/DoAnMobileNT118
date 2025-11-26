package com.example.doannt118.ui;

import android.content.Intent;
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
import com.example.doannt118.model.HoaDon;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class DanhSachHoaDonActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvHoaDon;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvTongTien;
    private HoaDonAdapter adapter;
    private FirestoreRepository repo;
    private String maBenhNhan;
    private List<HoaDon> hoaDonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_hoa_don);

        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã bệnh nhân!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo = new FirestoreRepository();
        hoaDonList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadHoaDon();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvHoaDon = findViewById(R.id.rvHoaDon);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTongTien = findViewById(R.id.tvTongTien);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new HoaDonAdapter(this, hoaDonList, hoaDon -> {
            Intent intent = new Intent(this, ChiTietHoaDonActivity.class);
            intent.putExtra("MA_HOA_DON", hoaDon.getMaHoaDon());
            intent.putExtra("MA_BENH_AN", hoaDon.getMaBenhAn());
            startActivity(intent);
        });
        rvHoaDon.setLayoutManager(new LinearLayoutManager(this));
        rvHoaDon.setAdapter(adapter);
    }

    private void loadHoaDon() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repo.getHoaDonByBenhNhan(maBenhNhan,
                querySnapshot -> {
                    hoaDonList.clear();
                    double tongTatCa = 0;
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        HoaDon hoaDon = doc.toObject(HoaDon.class);
                        if (hoaDon != null) {
                            hoaDonList.add(hoaDon);
                            tongTatCa += hoaDon.getTongTien();
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                    if (hoaDonList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvTongTien.setVisibility(View.GONE);
                    } else {
                        tvTongTien.setText(String.format("Tổng chi phí: %,.0f đ", tongTatCa));
                        tvTongTien.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                },
                e -> {
                    Log.e("DanhSachHoaDon", "Lỗi tải hóa đơn: ", e);
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
